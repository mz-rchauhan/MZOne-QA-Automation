package core.base;


import io.github.bonigarcia.wdm.WebDriverManager;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebDriverException;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.edge.EdgeDriver;
import org.openqa.selenium.edge.EdgeOptions;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.interactions.Actions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.util.Locale;

/**
 * Thread-safe WebDriver Manager for parallel test execution.
 * Manages browser initialization, lifecycle, and per-thread instances using ThreadLocal.
 */
public final class DriverManager {

    private static final Logger LOGGER = LoggerFactory.getLogger(DriverManager.class);

    // Timeout defaults
    private static final int DEFAULT_IMPLICIT_WAIT_SECONDS = 0;
    private static final int DEFAULT_PAGE_LOAD_TIMEOUT_SECONDS = 60;
    private static final int DEFAULT_SCRIPT_TIMEOUT_SECONDS = 30;

    // Browser identifiers
    private static final String BROWSER_CHROME = "chrome";
    private static final String BROWSER_FIREFOX = "firefox";
    private static final String BROWSER_EDGE = "edge";

    // ThreadLocal storage for per-thread WebDriver instances
    private static final ThreadLocal<WebDriver> DRIVER = new ThreadLocal<>();

    // ──────────────────────────────────────────────────────────────────────────

    private DriverManager() {
        throw new AssertionError("Cannot instantiate DriverManager");
    }

    /**
     * Initializes the WebDriver based on configuration settings.
     * @throws FrameworkException if browser initialization fails
     */
    public static void initDriver() {
        if (hasActiveDriver()) {
            LOGGER.debug("WebDriver already initialized for thread: {}", 
                    Thread.currentThread().getId());
            return;
        }

        try {
            // Read configuration
            String configuredBrowser = ConfigManager.getProperty("browser", BROWSER_CHROME);
            String browserType = normalizeBrowser(configuredBrowser);
            boolean headless = Boolean.parseBoolean(
                    System.getProperty("headless",
                            ConfigManager.getProperty("headless", "false")));

            LOGGER.info("Initializing {} browser for thread {}. Headless mode: {}",
                    browserType, Thread.currentThread().getId(), headless);

            // Create driver
            WebDriver webDriver = createDriver(browserType, headless);
            
            // Apply timeouts
            applyTimeouts(webDriver);
            
            // Maximize window (if not headless)
            if (!headless) {
                try {
                    webDriver.manage().window().maximize();
                } catch (WebDriverException e) {
                    LOGGER.debug("Window maximize skipped: {}", e.getMessage());
                }
            }

            // Store in ThreadLocal
            DRIVER.set(webDriver);
            LOGGER.info("WebDriver initialized successfully for thread: {}",
                    Thread.currentThread().getId());

        } catch (Exception e) {
            quitDriver();
            throw new FrameworkException(
                    "Failed to initialize WebDriver for thread: " + Thread.currentThread().getId(),
                    e);
        }
    }

    /**
     * Creates a WebDriver instance for the specified browser.
     *
     * @param browserType The browser type (chrome, firefox, edge)
     * @param headless    Whether to run in headless mode
     * @return The WebDriver instance
     * @throws FrameworkException if browser not supported or creation fails
     */
    private static WebDriver createDriver(String browserType, boolean headless) {
        return switch (browserType.toLowerCase(Locale.ENGLISH)) {
            case BROWSER_CHROME -> createChromeDriver(headless);
            case BROWSER_FIREFOX -> createFirefoxDriver(headless);
            case BROWSER_EDGE -> createEdgeDriver(headless);
            default -> throw new FrameworkException(
                    "Unsupported browser type: " + browserType +
                    ". Supported: chrome, firefox, edge");
        };
    }

    /**
     * Creates a Chrome WebDriver instance.
     *
     * @param headless Whether to run in headless mode
     * @return The Chrome WebDriver instance
     */
    private static WebDriver createChromeDriver(boolean headless) {
        WebDriverManager.chromedriver().setup();
        ChromeOptions options = new ChromeOptions();

        if (headless) {
            options.addArguments("--headless=new");
        }

        // Common options for stability
        options.addArguments(
                "--no-sandbox",
                "--disable-dev-shm-usage",
                "--disable-gpu",
                "--start-maximized",
                "--window-size=1920,1080"
        );

        // Disable notifications
        options.addArguments("--disable-notifications");

        // Accept SSL certificates
        options.setAcceptInsecureCerts(true);

        LOGGER.debug("Creating Chrome driver with headless={}", headless);
        return new ChromeDriver(options);
    }

    /**
     * Creates a Firefox WebDriver instance.
     *
     * @param headless Whether to run in headless mode
     * @return The Firefox WebDriver instance
     */
    private static WebDriver createFirefoxDriver(boolean headless) {
        WebDriverManager.firefoxdriver().setup();
        FirefoxOptions options = new FirefoxOptions();

        if (headless) {
            options.addArguments("--headless");
        }

        // Common options for stability
        options.addArguments("--width=1920", "--height=1080");

        // Accept SSL certificates
        options.setAcceptInsecureCerts(true);

        LOGGER.debug("Creating Firefox driver with headless={}", headless);
        return new FirefoxDriver(options);
    }

    /**
     * Creates an Edge WebDriver instance.
     *
     * @param headless Whether to run in headless mode
     * @return The Edge WebDriver instance
     */
    private static WebDriver createEdgeDriver(boolean headless) {
        WebDriverManager.edgedriver().setup();
        EdgeOptions options = new EdgeOptions();

        if (headless) {
            options.addArguments("--headless=new");
        }

        // Common options for stability
        options.addArguments(
                "--no-sandbox",
                "--disable-dev-shm-usage",
                "--disable-gpu",
                "--start-maximized",
                "--window-size=1920,1080"
        );

        // Accept SSL certificates
        options.setAcceptInsecureCerts(true);

        LOGGER.debug("Creating Edge driver with headless={}", headless);
        return new EdgeDriver(options);
    }

    /**
     * Applies configured timeouts to the WebDriver.
     *
     * @param driver The WebDriver instance
     */
    private static void applyTimeouts(WebDriver driver) {
        int implicitWait = ConfigManager.getInt(
                "implicit.wait.timeout", DEFAULT_IMPLICIT_WAIT_SECONDS);
        int pageLoadTimeout = ConfigManager.getInt(
                "page.load.timeout", DEFAULT_PAGE_LOAD_TIMEOUT_SECONDS);
        int scriptTimeout = ConfigManager.getInt(
                "script.timeout", DEFAULT_SCRIPT_TIMEOUT_SECONDS);

        try {
            driver.manage().timeouts()
                    .implicitlyWait(Duration.ofSeconds(implicitWait));
            driver.manage().timeouts()
                    .pageLoadTimeout(Duration.ofSeconds(pageLoadTimeout));
            driver.manage().timeouts()
                    .scriptTimeout(Duration.ofSeconds(scriptTimeout));

            LOGGER.debug("Applied timeouts - Implicit: {}s, PageLoad: {}s, Script: {}s",
                    implicitWait, pageLoadTimeout, scriptTimeout);
        } catch (WebDriverException e) {
            LOGGER.warn("Failed to apply timeouts: {}", e.getMessage());
        }
    }

    // ──────────────────────────────────────────────────────────────────────────
    // DRIVER ACCESS
    // ──────────────────────────────────────────────────────────────────────────

    /**
     * Gets the WebDriver instance for the current thread.
     *
     * @return The WebDriver instance
     * @throws FrameworkException if driver not initialized
     */
    public static WebDriver getDriver() {
        WebDriver driver = DRIVER.get();
        if (driver == null) {
            throw new FrameworkException(
                    "WebDriver not initialized for thread: " + Thread.currentThread().getId() +
                    ". Call DriverManager.initDriver() first.");
        }
        return driver;
    }

    /**
     * Checks if an active WebDriver instance exists for the current thread.
     *
     * @return true if driver is initialized, false otherwise
     */
    public static boolean hasActiveDriver() {
        return DRIVER.get() != null;
    }

    /**
     * Gets the WebDriver without throwing exception if not initialized.
     *
     * @return The WebDriver instance, or null if not initialized
     */
    public static WebDriver getDriverOrNull() {
        return DRIVER.get();
    }

    // ──────────────────────────────────────────────────────────────────────────
    // CLEANUP
    // ──────────────────────────────────────────────────────────────────────────

    /**
     * Quits the WebDriver and cleans up the ThreadLocal storage.
     * Safe to call even if driver not initialized.
     */
    public static void quitDriver() {
        WebDriver driver = DRIVER.get();
        if (driver != null) {
            try {
                LOGGER.info("Closing WebDriver for thread: {}", Thread.currentThread().getId());
                driver.quit();
                LOGGER.info("WebDriver closed successfully");
            } catch (WebDriverException e) {
                LOGGER.error("Error closing WebDriver: {}", e.getMessage());
            } finally {
                DRIVER.remove();
            }
        }
    }



    /**
     * Clears browser cache and cookies.
     * Does not close the browser.
     */
    public static void clearBrowserData() {
        try {
            WebDriver driver = getDriver();
            driver.manage().deleteAllCookies();
            LOGGER.info("Cleared all cookies");
        } catch (WebDriverException e) {
            LOGGER.warn("Failed to clear browser data: {}", e.getMessage());
        }
    }

    // ──────────────────────────────────────────────────────────────────────────
    // UTILITY METHODS
    // ──────────────────────────────────────────────────────────────────────────

    /**
     * Normalizes browser name to lowercase and removes extra spaces.
     *
     * @param browserName The browser name to normalize
     * @return The normalized browser name
     */
    private static String normalizeBrowser(String browserName) {
        if (browserName == null || browserName.isBlank()) {
            return BROWSER_CHROME;
        }
        return browserName.trim().toLowerCase(Locale.ENGLISH);
    }

    /**
     * Gets the current thread ID.
     *
     * @return The current thread ID
     */
    public static long getCurrentThreadId() {
        return Thread.currentThread().getId();
    }

    /**
     * Returns an Actions instance bound to the current thread's driver.
     * Used by AutoRecoveryEngine and SeleniumCommands for keyboard/mouse sequences.
     */
    public static Actions getActions() {
        return new Actions(getDriver());
    }
}
