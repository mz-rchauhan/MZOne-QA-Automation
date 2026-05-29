package observability;

import core.base.ConfigManager;
import core.base.DriverManager;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import com.aventstack.extentreports.ExtentReports;
import com.aventstack.extentreports.ExtentTest;
import com.aventstack.extentreports.reporter.ExtentSparkReporter;
import com.aventstack.extentreports.reporter.configuration.Theme;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Thread-safe Extent Report Manager for test reporting and observability.
 */
public final class ExtentReportManager {

    private static final Logger LOGGER = LoggerFactory.getLogger(ExtentReportManager.class);

    private static ExtentReports extent;
    private static final ThreadLocal<ExtentTest> TEST = new ThreadLocal<>();
    private static final Object INIT_LOCK = new Object();
    private static volatile boolean initialized = false;

    // Report configuration
    private static final String REPORT_DIRECTORY = "test-output";
    private static final String REPORT_FILE_NAME = "ExtentReport.html";

    // ──────────────────────────────────────────────────────────────────────────

    private ExtentReportManager() {
        throw new AssertionError("Cannot instantiate ExtentReportManager");
    }

    // ──────────────────────────────────────────────────────────────────────────
    // INITIALIZATION
    // ──────────────────────────────────────────────────────────────────────────

    /**
     * Initializes Extent Reports.
     * Thread-safe, safe to call multiple times.
     */
    public static void initReports() {
        if (!initialized) {
            synchronized (INIT_LOCK) {
                if (!initialized) {
                    setupReporter();
                    initialized = true;
                    LOGGER.info("Extent Reports initialized");
                }
            }
        }
    }

    /**
     * Sets up the Extent Report instance.
     */
    private static void setupReporter() {
        try {
            // Create report directory if not exists
            File reportDir = new File(REPORT_DIRECTORY);
            if (!reportDir.exists()) {
                reportDir.mkdirs();
            }

            // Set up report file path
            String reportPath = REPORT_DIRECTORY + File.separator + REPORT_FILE_NAME;

            // Create Spark reporter
            ExtentSparkReporter spark = new ExtentSparkReporter(reportPath);
            spark.config().setDocumentTitle(ConfigManager.getProperty(
                    "report.title", "Automation Test Report"));
            spark.config().setReportName(ConfigManager.getProperty(
                    "report.name", "Test Execution Results"));
            spark.config().setTheme(Theme.DARK);
            spark.config().setTimeStampFormat("dd/MM/yyyy HH:mm:ss");
            spark.config().setCss(getCustomCSS());

            // Create Extent Reports
            extent = new ExtentReports();
            extent.attachReporter(spark);

            // Set system information
            setSystemInfo();

            LOGGER.info("Extent Report created at: {}", reportPath);
        } catch (Exception e) {
            LOGGER.error("Failed to initialize Extent Reports", e);
            throw new RuntimeException("Failed to initialize Extent Reports", e);
        }
    }

    /**
     * Sets system information in the report.
     */
    private static void setSystemInfo() {
        extent.setSystemInfo("Environment", ConfigManager.getActiveEnvironment().toUpperCase());
        extent.setSystemInfo("Browser", System.getProperty("browser",
                ConfigManager.getProperty("browser", "Chrome")));
        extent.setSystemInfo("Operating System", System.getProperty("os.name"));
        extent.setSystemInfo("OS Architecture", System.getProperty("os.arch"));
        extent.setSystemInfo("OS Version", System.getProperty("os.version"));
        extent.setSystemInfo("Java Version", System.getProperty("java.version"));
        extent.setSystemInfo("Java Vendor", System.getProperty("java.vendor"));
        extent.setSystemInfo("User Name", System.getProperty("user.name"));
        extent.setSystemInfo("User TimeZone", System.getProperty("user.timezone"));
        extent.setSystemInfo("Test Execution Started", LocalDateTime.now().format(
                DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss")));
    }

    /**
     * Gets custom CSS for report styling.
     *
     * @return CSS string
     */
    private static String getCustomCSS() {
        return ".m-r-10 { margin-right: 10px; } " +
               ".text-center { text-align: center; } " +
               ".badge { padding: 5px 10px; border-radius: 3px; }";
    }

    /**
     * Gets the Extent Reports instance.
     * Initializes if not already done.
     *
     * @return The ExtentReports instance
     */
    private static ExtentReports getReporter() {
        if (extent == null) {
            initReports();
        }
        return extent;
    }

    // ──────────────────────────────────────────────────────────────────────────
    // TEST CONTEXT MANAGEMENT
    // ──────────────────────────────────────────────────────────────────────────

    /**
     * Creates a test in the report.
     *
     * @param testName      The test name
     * @param description   The test description
     * @return The ExtentTest instance
     */
    public static ExtentTest createTest(String testName, String description) {
        ExtentTest extentTest = getReporter().createTest(testName, description);
        TEST.set(extentTest);
        LOGGER.info("Test created in report: {}", testName);
        return extentTest;
    }

    /**
     * Creates a test in the report.
     *
     * @param testName The test name
     * @return The ExtentTest instance
     */
    public static ExtentTest createTest(String testName) {
        return createTest(testName, "");
    }

    /**
     * Gets the current test instance from ThreadLocal.
     *
     * @return The ExtentTest instance, or null if not set
     */
    public static ExtentTest getTest() {
        return TEST.get();
    }

    /**
     * Removes the current test from ThreadLocal.
     * Should be called after test completion.
     */
    public static void unloadTest() {
        TEST.remove();
    }

    // ──────────────────────────────────────────────────────────────────────────
    // LOGGING METHODS
    // ──────────────────────────────────────────────────────────────────────────

    /**
     * Logs an information message.
     *
     * @param message The message to log
     */
    public static void logInfo(String message) {
        ExtentTest test = getTest();
        if (test != null) {
            test.info(message);
            LOGGER.info(message);
        }
    }

    /**
     * Logs a pass message.
     *
     * @param message The message to log
     */
    public static void logPass(String message) {
        ExtentTest test = getTest();
        if (test != null) {
            test.pass(message);
            LOGGER.info("PASS: {}", message);
        }
    }

    /**
     * Logs a fail message.
     *
     * @param message The message to log
     */
    public static void logFail(String message) {
        ExtentTest test = getTest();
        if (test != null) {
            test.fail(message);
            attachScreenshot("Failure Screenshot");
            LOGGER.error("FAIL: {}", message);
        }
    }

    /**
     * Logs a fail message with exception.
     *
     * @param message   The message to log
     * @param throwable The exception/throwable
     */
    public static void logFail(String message, Throwable throwable) {
        ExtentTest test = getTest();
        if (test != null) {
            test.fail(message);
            if (throwable != null) {
                test.fail(throwable);
            }
            attachScreenshot("Exception Screenshot");
            LOGGER.error("FAIL: {}", message, throwable);
        }
    }

    /**
     * Logs a warning message.
     *
     * @param message The message to log
     */
    public static void logWarning(String message) {
        ExtentTest test = getTest();
        if (test != null) {
            test.warning(message);
            LOGGER.warn(message);
        }
    }

    /**
     * Logs a skip message.
     *
     * @param message The message to log
     */
    public static void logSkip(String message) {
        ExtentTest test = getTest();
        if (test != null) {
            test.skip(message);
            LOGGER.warn("SKIP: {}", message);
        }
    }

    // ──────────────────────────────────────────────────────────────────────────
    // SCREENSHOT OPERATIONS
    // ──────────────────────────────────────────────────────────────────────────

    /**
     * Attaches a screenshot to the current test.
     * Uses Base64 encoding for embedded images.
     *
     * @param screenshotName The name/title of the screenshot
     */
    public static void attachScreenshot(String screenshotName) {
        ExtentTest test = getTest();
        if (test != null && DriverManager.hasActiveDriver()) {
            try {
                String base64Screenshot = ((TakesScreenshot) DriverManager.getDriver())
                        .getScreenshotAs(OutputType.BASE64);
                test.addScreenCaptureFromBase64String(base64Screenshot, screenshotName);
                LOGGER.debug("Screenshot attached: {}", screenshotName);
            } catch (Exception e) {
                LOGGER.warn("Failed to capture screenshot: {}", e.getMessage());
            }
        }
    }

    /**
     * Attaches a screenshot from file path.
     *
     * @param screenshotName The name/title of the screenshot
     * @param filePath       The file path of the screenshot
     */
    public static void attachScreenshotFromFile(String screenshotName, String filePath) {
        ExtentTest test = getTest();
        if (test != null) {
            try {
                test.addScreenCaptureFromPath(filePath, screenshotName);
                LOGGER.debug("Screenshot file attached: {}", screenshotName);
            } catch (Exception e) {
                LOGGER.warn("Failed to attach screenshot file: {}", e.getMessage());
            }
        }
    }

    // ──────────────────────────────────────────────────────────────────────────
    // REPORT FINALIZATION
    // ──────────────────────────────────────────────────────────────────────────

    /**
     * Flushes and finalizes the Extent Reports.
     * Must be called at the end of test execution.
     */
    public static void flushReports() {
        synchronized (INIT_LOCK) {
            if (extent != null) {
                try {
                    extent.flush();
                    LOGGER.info("Extent Reports flushed successfully");
                } catch (Exception e) {
                    LOGGER.error("Error flushing Extent Reports", e);
                }
            }
        }
    }

    /**
     * Closes and finalizes all report resources.
     */
    public static void closeReports() {
        flushReports();  // Ensure flush happens first
        unloadTest();    // Clean up ThreadLocal<ExtentTest>
        synchronized (INIT_LOCK) {
            try {
                if (extent != null) {
                    // Optional: Add any pre-close operations
                    LOGGER.debug("Closing Extent Reports resources...");
                    extent = null;
                    LOGGER.debug("Extent Reports instance cleared");
                }
            } finally {
                initialized = false;
                LOGGER.info("Extent Reports closed successfully");
            }
        }
    }
}
