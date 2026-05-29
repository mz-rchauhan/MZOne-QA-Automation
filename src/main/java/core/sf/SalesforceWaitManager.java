package core.sf;

import core.base.ConfigManager;
import core.base.DriverManager;

import core.base.WaitManager;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;


public class SalesforceWaitManager{

    private static final Logger LOGGER = LoggerFactory.getLogger(SalesforceWaitManager.class);
    private static final int DEFAULT_EXPLICIT_WAIT_SECONDS = 30;
    private static final int DEFAULT_TIMEOUT = ConfigManager.getInt(
            "explicit.wait.timeout", DEFAULT_EXPLICIT_WAIT_SECONDS);
    // Salesforce Lightning Locators
    private static final By LIGHTNING_SPINNER = By.xpath(
            "//div[contains(@class,'slds-spinner_container') and not(contains(@class,'slds-hide'))]");
    private static final By AURA_LOADING = By.xpath(
            "//*[contains(@class,'auraLoadingBox') and not(contains(@class,'hide'))]");
    private static final By RECORD_HEADER = By.xpath(
            "//h1[@class='slds-page-header__title'] | //records-highlights2");
    private static final By MODAL_DIALOG = By.xpath("//section[@role='dialog']");

    public static void waitForAuraLoading() {
        JavascriptExecutor js = (JavascriptExecutor) DriverManager.getDriver();
        try {
            new WebDriverWait(DriverManager.getDriver(), Duration.ofSeconds(DEFAULT_TIMEOUT))
                    .until(driver -> Boolean.TRUE.equals(js.executeScript(
                            "return (typeof $A === 'undefined') || (!$A.getContext() || $A.getContext().getNumPendingXHRs() === 0);")));
        } catch (TimeoutException error) {
            LOGGER.debug("Aura loading wait timed out: {}", error.getMessage());
        }
    }

    public static void waitForLWCSpinners() {
        try {
            new WebDriverWait(DriverManager.getDriver(), Duration.ofSeconds(DEFAULT_TIMEOUT))
                    .until(ExpectedConditions.invisibilityOfElementLocated(LIGHTNING_SPINNER));
            LOGGER.debug("Lightning spinners cleared");
        } catch (TimeoutException error) {
            LOGGER.debug("Lightning spinner still visible after timeout: {}", error.getMessage());
        }
    }

    public static void waitForSalesforceSpinner() {
        try {
            new WebDriverWait(DriverManager.getDriver(), Duration.ofSeconds(DEFAULT_TIMEOUT))
                    .until(ExpectedConditions.invisibilityOfElementLocated(AURA_LOADING));
            LOGGER.debug("Salesforce spinner cleared");
        } catch (TimeoutException error) {
            LOGGER.debug("Salesforce spinner still visible after timeout: {}", error.getMessage());
        }
    }

    public static void waitForLightningLoad() {
        WaitManager.waitForPageLoad();
        waitForAuraLoading();
        waitForLWCSpinners();
        waitForSalesforceSpinner();
        waitForLightningDocumentReady(DriverManager.getDriver());
    }

    public static void waitForLightningRender() {
        WebDriver driver = DriverManager.getDriver();
        try {
            WebDriverWait shortWait = new WebDriverWait(driver, Duration.ofSeconds(10));
            try {
                shortWait.until(ExpectedConditions.visibilityOfElementLocated(LIGHTNING_SPINNER));
            } catch (TimeoutException ignored) {
                LOGGER.debug("Lightning spinner did not appear before render check.");
            }
            new WebDriverWait(driver, Duration.ofSeconds(DEFAULT_TIMEOUT))
                    .until(ExpectedConditions.invisibilityOfElementLocated(LIGHTNING_SPINNER));
            new WebDriverWait(driver, Duration.ofSeconds(DEFAULT_TIMEOUT))
                    .until(ExpectedConditions.invisibilityOfElementLocated(AURA_LOADING));
            waitForLightningDocumentReady(driver);
            LOGGER.info("Lightning render complete.");
        } catch (TimeoutException error) {
            LOGGER.warn("waitForLightningRender timed out: {}", error.getMessage());
        }
    }

    public static void waitForRecordPageLoad() {
        try {
            waitForLightningRender();
            new WebDriverWait(DriverManager.getDriver(), Duration.ofSeconds(DEFAULT_TIMEOUT))
                    .until(ExpectedConditions.visibilityOfElementLocated(RECORD_HEADER));
            LOGGER.info("Record page fully loaded.");
        } catch (TimeoutException error) {
            LOGGER.warn("waitForRecordPageLoad timed out: {}", error.getMessage());
        }
    }

    public static void waitForPageTransition() {
        String oldUrl = DriverManager.getDriver().getCurrentUrl();
        waitForLightningLoad();
        String newUrl = DriverManager.getDriver().getCurrentUrl();
        if (!oldUrl.equals(newUrl)) {
            LOGGER.info("Page transition: {} -> {}", oldUrl, newUrl);
        }
    }

    public static void reachSteadyState() {
        WaitManager.waitForDomStable();
        waitForLightningLoad();
    }
    public static void waitForModalOpen() {
        try {
            new WebDriverWait(DriverManager.getDriver(), Duration.ofSeconds(DEFAULT_TIMEOUT))
                    .until(ExpectedConditions.visibilityOfElementLocated(MODAL_DIALOG));
            waitForLightningRender();
            LOGGER.info("Modal dialog opened.");
        } catch (TimeoutException error) {
            LOGGER.warn("waitForModalOpen timed out: {}", error.getMessage());
        }
    }

    private static void waitForLightningDocumentReady(WebDriver driver) {
        try {
            new WebDriverWait(driver, Duration.ofSeconds(DEFAULT_TIMEOUT)).until(d ->
                    "complete".equals(((JavascriptExecutor) d).executeScript("return document.readyState")));
        } catch (TimeoutException error) {
            LOGGER.debug("Document ready wait timed out: {}", error.getMessage());
        }
    }



}
