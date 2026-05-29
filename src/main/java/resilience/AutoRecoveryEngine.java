package resilience;

import java.util.function.Supplier;

import core.base.TestContext;
import core.sf.SalesforceWaitManager;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.slf4j.Logger;


import core.base.LoggerManager;
import core.base.ConfigManager;
import core.base.DriverManager;
import core.base.WaitManager;
import pages.actions.LoginPage;

public final class AutoRecoveryEngine {

    private static final Logger LOGGER = LoggerManager.getLogger(AutoRecoveryEngine.class);

    /**
     * Dismiss any visible unexpected popup or alert.
     */
    public static void closeUnexpectedPopup() {
        try {
            WebDriver driver = DriverManager.getDriver();
            // Check native browser alert
            driver.switchTo().alert().dismiss();
            LOGGER.info("[Recovery] Dismissed native browser alert.");
            return;
        } catch (Exception e) {
            // No native alert present
        }
        try {
            // Lightning modal close button
            By closeBtn = By.xpath("//button[@title='Close'] | //button[contains(@class,'modal-close')]");
            if (!DriverManager.getDriver().findElements(closeBtn).isEmpty()) {
                DriverManager.getDriver().findElement(closeBtn).click();
                WaitManager.waitForPageLoad();
                LOGGER.info("[Recovery] Closed Lightning modal/overlay.");
            }
        } catch (Exception e) {
            LOGGER.debug("[Recovery] No unexpected popup found to close.");
        }
    }

    /**
     * Re-find and return a stale element using a fallback By locator.
     *
     * @param elementSupplier Supplier that re-finds the element
     * @param fallback        Fallback locator if supplier throws
     * @return Fresh WebElement
     */
    public static org.openqa.selenium.WebElement recoverFromStaleElement(
            Supplier<org.openqa.selenium.WebElement> elementSupplier, By fallback) {
        try {
            return elementSupplier.get();
        } catch (Exception e) {
            LOGGER.warn("[Recovery] Stale element — re-finding via fallback locator.");
            return WaitManager.waitForVisible(fallback);
        }
    }

    /**
     * Retry the last action via RetryEngine.
     *
     * @param action   Action to retry
     * @param taskName Human-readable name for logs
     */
    public static void retryLastAction(Runnable action, String taskName) {
        LOGGER.info("[Recovery] Retrying last action: '{}'", taskName);
        RetryEngine.executeWithRetry(action, taskName);
    }

    /**
     * Refresh the page and retry an action.
     *
     * @param action   Action to retry after refresh
     * @param taskName Human-readable name
     */
    public static void refreshPageAndRetry(Runnable action, String taskName) {
        LOGGER.info("[Recovery] Refreshing page before retrying: '{}'", taskName);
        DriverManager.getDriver().navigate().refresh();
        WaitManager.waitForPageLoad();
        RetryEngine.executeWithRetry(action, taskName);
    }

    /**
     * Clear any blocking modal overlay using Escape key and Lightning
     * stabilisation.
     */
    public static void recoverFromModalOverlay() {
        try {
            org.openqa.selenium.interactions.Actions actions = DriverManager.getActions();
            if (actions != null) {
                actions.sendKeys(org.openqa.selenium.Keys.ESCAPE).perform();
            }
            WaitManager.waitForPageLoad();
            LOGGER.info("[Recovery] Sent Escape to dismiss modal overlay.");
        } catch (Exception e) {
            LOGGER.debug("[Recovery] recoverFromModalOverlay: no overlay or escape failed.");
        }
    }

    // ==========================================
    // Scenario-Level Recovery (Phase 3.2)
    // ==========================================

    /**
     * Full scenario re-stabilisation: dismiss popups, wait for Lightning, restore
     * URL.
     */
    public static void recoverScenario() {
        LOGGER.info("[Recovery] Starting full scenario recovery sequence.");
        closeUnexpectedPopup();
        recoverFromModalOverlay();
        SalesforceWaitManager.waitForLightningLoad();
        SalesforceWaitManager.waitForSalesforceSpinner();
        LOGGER.info("[Recovery] Scenario recovery complete.");
    }

    /**
     * Reset browser state: clear cookies and local/session storage.
     * Use when you want a clean slate without quitting the driver.
     */
    public static void resetDriverContext() {
        LOGGER.info("[Recovery] Resetting driver context (cookies + storage).");
        try {
            WebDriver driver = DriverManager.getDriver();
            driver.manage().deleteAllCookies();
            ((JavascriptExecutor) driver).executeScript("window.localStorage.clear();");
            ((JavascriptExecutor) driver).executeScript("window.sessionStorage.clear();");
            LOGGER.info("[Recovery] Driver context cleared.");
        } catch (Exception e) {
            LOGGER.warn("[Recovery] resetDriverContext failed: {}", e.getMessage());
        }
    }

    /**
     * Navigate back to the last stored URL from TestContext.
     */
    public static void reopenLastPage() {
        String lastUrl = TestContext.retrieveString("lastUrl");
        if (lastUrl != null && !lastUrl.isBlank()) {
            LOGGER.info("[Recovery] Reopening last page: {}", lastUrl);
            DriverManager.getDriver().get(lastUrl);
            WaitManager.waitForPageLoad();
        } else {
            LOGGER.warn("[Recovery] No 'lastUrl' in TestContext — cannot reopen last page.");
        }
    }

    /**
     * Detect if the current URL has drifted from the expected URL and navigate
     * back.
     *
     * @param expectedUrlFragment Fragment expected in current URL (e.g.,
     *                            "/lightning/r/Account")
     */
    public static void recoverFromUnexpectedNavigation(String expectedUrlFragment) {
        String currentUrl = DriverManager.getDriver().getCurrentUrl();
        if (!currentUrl.contains(expectedUrlFragment)) {
            LOGGER.warn("[Recovery] URL drift detected. Current: '{}'. Expected fragment: '{}'",
                    currentUrl, expectedUrlFragment);
            reopenLastPage();
        }
    }

    /**
     * Detect and recover from an expired Salesforce session (redirected to login
     * page).
     * Re-logs in using EnvironmentManager credentials.
     */

    public static void restoreSessionState() {
        String currentUrl = DriverManager.getDriver().getCurrentUrl();
        String loginUrl = ConfigManager.getProperty("sf.login.url", "login.salesforce.com");

        if (currentUrl.contains("/login") || currentUrl.contains(loginUrl) || currentUrl.contains("axment")) {
            LOGGER.warn("[Recovery] Session appears expired (URL: {}). Attempting re-login.", currentUrl);
            try {
                LoginPage loginPage = new LoginPage();
                loginPage.enterUserName(ConfigManager.getProperty("username", ""));
                loginPage.enterPassword(ConfigManager.getProperty("password", ""));
                loginPage.clickOnLogin();
                LOGGER.info("[Recovery] Session restored via re-login.");
            } catch (Exception e) {
                LOGGER.error("[Recovery] restoreSessionState failed: {}", e.getMessage());
            }
        }
        }


}
