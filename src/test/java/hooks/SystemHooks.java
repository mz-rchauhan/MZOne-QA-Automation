package hooks;

import core.base.*;
import io.cucumber.java.After;
import io.cucumber.java.Before;
import io.cucumber.java.Scenario;
import observability.ExtentReportManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.nio.file.Files;


public class SystemHooks {
    // FIXED: was LoggerFactory.getLogger(ScreenshotService.class) — wrong class
    private static final Logger LOGGER = LoggerManager.getLogger(SystemHooks.class);

    private final SeleniumCommands sc = new SeleniumCommands();

    @Before(order = 1000)
    public void setup(Scenario scenario) {
        ConfigManager.setEnvironment("qa");
        DriverManager.initDriver();
        LoggerManager.setScenarioContext(scenario.getName());
        ExtentReportManager.createTest(scenario.getName());
        LOGGER.info("=== Scenario START: {} ===", scenario.getName());
    }

    @After(order = 1000)
    public void tearDown(Scenario scenario) {
        try {
            if (scenario.isFailed()) {
                byte[] screenshot = sc.captureScreenshotAsBytes();
                scenario.attach(screenshot, "image/png", "Failure_Screenshot");
                ExtentReportManager.logFail("Scenario failed: " + scenario.getName());
                LOGGER.error("=== Scenario FAILED: {} ===", scenario.getName());
            } else {
                ExtentReportManager.logPass("Scenario passed: " + scenario.getName());
                LOGGER.info("=== Scenario PASSED: {} ===", scenario.getName());
            }
        } catch (Exception e) {
            LOGGER.warn("Teardown issue: {}", e.getMessage());
        } finally {
            LoggerManager.clearContext();
            TestContext.clear();
            //DriverManager.quitDriver();
        }
    }
}
