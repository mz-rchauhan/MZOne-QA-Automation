package core.base;

import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.StaleElementReferenceException;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.FluentWait;
import org.openqa.selenium.support.ui.Wait;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.util.List;
import java.util.function.BooleanSupplier;
import java.util.concurrent.locks.LockSupport;

/**
 * Unified wait manager for browser.
 */
public final class WaitManager {

    private static final Logger LOGGER = LoggerFactory.getLogger(WaitManager.class);
    private static final int DEFAULT_EXPLICIT_WAIT_SECONDS = 30;
    private static final int DEFAULT_SHORT_WAIT_SECONDS = 10;
    private static final long DEFAULT_POLLING_MS = 500L;

    private static final int DEFAULT_TIMEOUT = ConfigManager.getInt(
            "explicit.wait.timeout", DEFAULT_EXPLICIT_WAIT_SECONDS);
    private static final int SHORT_TIMEOUT = DEFAULT_SHORT_WAIT_SECONDS;
    private static final Duration DEFAULT_POLLING = Duration.ofMillis(DEFAULT_POLLING_MS);

    // Salesforce Lightning Locators
    private static final By LIGHTNING_SPINNER = By.xpath(
            "//div[contains(@class,'slds-spinner_container') and not(contains(@class,'slds-hide'))]");
    private static final By AURA_LOADING = By.xpath(
            "//*[contains(@class,'auraLoadingBox') and not(contains(@class,'hide'))]");
    private static final By TOAST_SUCCESS = By.xpath(
            "//div[@data-key='success']//*[contains(@class,'toastMessage')]");
    private static final By TOAST_ERROR = By.xpath(
            "//div[@data-key='error']//*[contains(@class,'toastMessage')]");
    private static final By LOOKUP_DROPDOWN = By.xpath(
            "//div[@role='listbox'] | //lightning-base-combobox-item");
    private static final By PICKLIST_DROPDOWN = By.xpath(
            "//lightning-base-combobox-item[@role='option']");
    private static final By RECORD_HEADER = By.xpath(
            "//h1[@class='slds-page-header__title'] | //records-highlights2");
    private static final By MODAL_DIALOG = By.xpath("//section[@role='dialog']");


    // ──────────────────────────────────────────────────────────────────────────────
    // SINGLE ELEMENT WAITS
    // ──────────────────────────────────────────────────────────────────────────────

    public static Wait<WebDriver> getWait() {
        return getWait(DEFAULT_TIMEOUT);
    }

    public static Wait<WebDriver> getWait(int timeoutSeconds) {
        return new FluentWait<>(DriverManager.getDriver())
                .withTimeout(Duration.ofSeconds(timeoutSeconds))
                .pollingEvery(DEFAULT_POLLING)
                .ignoring(NoSuchElementException.class)
                .ignoring(StaleElementReferenceException.class);
    }

    /**
     * Wait for element to be visible
     */
    public static WebElement waitForVisible(By locator) {
        return waitForVisible(locator, Duration.ofSeconds(DEFAULT_TIMEOUT));
    }

    public static WebElement waitForVisible(By locator, Duration timeout) {
        try {
            return new WebDriverWait(DriverManager.getDriver(), timeout)
                    .until(ExpectedConditions.visibilityOfElementLocated(locator));
        } catch (TimeoutException error) {
            throw new FrameworkTimeoutException("Element not visible: " + locator, timeout, error);
        }
    }

    /**
     * Wait for element to be clickable
     */
    public static WebElement waitForClickable(By locator) {
        return waitForClickable(locator, Duration.ofSeconds(DEFAULT_TIMEOUT));
    }



    public static WebElement waitForClickable(By locator, Duration timeout) {
        try {
            LOGGER.debug("Waiting for clickable: {} (timeout: {}s)", locator, timeout.getSeconds());
            return new WebDriverWait(DriverManager.getDriver(), timeout)
                    .until(ExpectedConditions.elementToBeClickable(locator));
        } catch (TimeoutException error) {
            LOGGER.error("Element not clickable within {}s: {}", timeout.getSeconds(), locator);
            throw new FrameworkTimeoutException("Element not clickable: " + locator, timeout, error);
        }
    }

    /**
     * Wait for element to be present in DOM
     */
    public static WebElement waitForPresence(By locator) {
        try {
            return getWait().until(ExpectedConditions.presenceOfElementLocated(locator));
        } catch (TimeoutException error) {
            throw new FrameworkTimeoutException(
                    "Element not present: " + locator, Duration.ofSeconds(DEFAULT_TIMEOUT), error);
        }
    }

    /**
     * Wait for element to be invisible
     */
    public static boolean waitForInvisible(By locator) {
        try {
            return getWait().until(ExpectedConditions.invisibilityOfElementLocated(locator));
        } catch (TimeoutException error) {
            throw new FrameworkTimeoutException(
                    "Element remained visible: " + locator, Duration.ofSeconds(DEFAULT_TIMEOUT), error);
        }
    }

    /**
     * Wait for list of elements to be visible
     */
    public static List<WebElement> waitForAllVisible(By locator) {
        return waitForAllVisible(locator, Duration.ofSeconds(DEFAULT_TIMEOUT));
    }

    public static List<WebElement> waitForAllVisible(By locator, Duration timeout) {
        try {
            return new WebDriverWait(DriverManager.getDriver(), timeout)
                    .until(ExpectedConditions.visibilityOfAllElementsLocatedBy(locator));
        } catch (TimeoutException error) {
            throw new FrameworkTimeoutException(
                    "Elements not visible: " + locator, timeout, error);
        }
    }

    /**
     * Wait for list of elements to be present in DOM
     */
    public static List<WebElement> waitForAllPresent(By locator) {
        return waitForAllPresent(locator, Duration.ofSeconds(DEFAULT_TIMEOUT));
    }

    public static List<WebElement> waitForAllPresent(By locator, Duration timeout) {
        try {
            return new WebDriverWait(DriverManager.getDriver(), timeout)
                    .until(ExpectedConditions.presenceOfAllElementsLocatedBy(locator));
        } catch (TimeoutException error) {
            throw new FrameworkTimeoutException(
                    "Elements not present: " + locator, timeout, error);
        }
    }

    /**
     * Wait for specific number of elements
     */
    public static List<WebElement> waitForElementCount(By locator, int expectedCount) {
        return waitForElementCount(locator, expectedCount, Duration.ofSeconds(DEFAULT_TIMEOUT));
    }

    public static List<WebElement> waitForElementCount(By locator, int expectedCount, Duration timeout) {
        try {
            LOGGER.debug("Waiting for {} elements: {}", expectedCount, locator);
            new WebDriverWait(DriverManager.getDriver(), timeout)
                    .until(driver -> driver.findElements(locator).size() == expectedCount);
            return DriverManager.getDriver().findElements(locator);
        } catch (TimeoutException error) {
            int actualCount = DriverManager.getDriver().findElements(locator).size();
            throw new FrameworkTimeoutException(
                    "Expected " + expectedCount + " elements but found " + actualCount + ": " + locator,
                    timeout, error);
        }
    }

    /**
     * Wait for minimum number of elements
     */
    public static List<WebElement> waitForMinimumElements(By locator, int minCount) {
        return waitForMinimumElements(locator, minCount, Duration.ofSeconds(DEFAULT_TIMEOUT));
    }

    public static List<WebElement> waitForMinimumElements(By locator, int minCount, Duration timeout) {
        try {
            LOGGER.debug("Waiting for minimum {} elements: {}", minCount, locator);
            new WebDriverWait(DriverManager.getDriver(), timeout)
                    .until(driver -> driver.findElements(locator).size() >= minCount);
            return DriverManager.getDriver().findElements(locator);
        } catch (TimeoutException error) {
            int actualCount = DriverManager.getDriver().findElements(locator).size();
            throw new FrameworkTimeoutException(
                    "Expected at least " + minCount + " elements but found " + actualCount + ": " + locator,
                    timeout, error);
        }
    }

    /**
     * Wait for elements with text
     */
    public static List<WebElement> waitForElementsWithText(By locator, String text) {
        return waitForElementsWithText(locator, text, Duration.ofSeconds(DEFAULT_TIMEOUT));
    }

    public static List<WebElement> waitForElementsWithText(By locator, String text, Duration timeout) {
        try {
            LOGGER.debug("Waiting for elements with text '{}': {}", text, locator);
            new WebDriverWait(DriverManager.getDriver(), timeout)
                    .until(driver -> {
                        List<WebElement> elements = driver.findElements(locator);
                        return elements.stream().anyMatch(e -> e.getText().contains(text));
                    });
            return DriverManager.getDriver().findElements(locator);
        } catch (TimeoutException error) {
            throw new FrameworkTimeoutException(
                    "Elements with text '" + text + "' not found: " + locator, timeout, error);
        }
    }

    /**
     * Wait for clickable elements in list
     */
    public static List<WebElement> waitForAllClickable(By locator) {
        return waitForAllClickable(locator, Duration.ofSeconds(DEFAULT_TIMEOUT));
    }

    public static List<WebElement> waitForAllClickable(By locator, Duration timeout) {
        try {
            LOGGER.debug("Waiting for all clickable elements: {}", locator);
            new WebDriverWait(DriverManager.getDriver(), timeout)
                    .until(driver -> {
                        List<WebElement> elements = driver.findElements(locator);
                        return !elements.isEmpty() && elements.stream().allMatch(WebElement::isEnabled);
                    });
            return DriverManager.getDriver().findElements(locator);
        } catch (TimeoutException error) {
            throw new FrameworkTimeoutException(
                    "Not all elements are clickable: " + locator, timeout, error);
        }
    }


    /**
     * Wait for displayed elements in list
     */
    public static List<WebElement> waitForAllDisplayed(By locator) {
        return waitForAllDisplayed(locator, Duration.ofSeconds(DEFAULT_TIMEOUT));
    }

    public static List<WebElement> waitForAllDisplayed(By locator, Duration timeout) {
        try {
            new WebDriverWait(DriverManager.getDriver(), timeout)
                    .until(driver -> {
                        List<WebElement> elements = driver.findElements(locator);
                        return !elements.isEmpty() && elements.stream().allMatch(WebElement::isDisplayed);
                    });
            return DriverManager.getDriver().findElements(locator);
        } catch (TimeoutException error) {
            throw new FrameworkTimeoutException(
                    "Not all elements are displayed: " + locator, timeout, error);
        }
    }

    /**
     * Wait for any element in list to be visible
     */
    public static List<WebElement> waitForAnyVisible(By locator) {
        return waitForAnyVisible(locator, Duration.ofSeconds(DEFAULT_TIMEOUT));
    }

    public static List<WebElement> waitForAnyVisible(By locator, Duration timeout) {
        try {
            new WebDriverWait(DriverManager.getDriver(), timeout)
                    .until(driver -> {
                        List<WebElement> elements = driver.findElements(locator);
                        return elements.stream().anyMatch(WebElement::isDisplayed);
                    });
            return DriverManager.getDriver().findElements(locator);
        } catch (TimeoutException error) {
            throw new FrameworkTimeoutException(
                    "No visible elements found: " + locator, timeout, error);
        }
    }

    /**
     * Wait for list elements to have attribute value
     */
    public static List<WebElement> waitForElementsWithAttribute(By locator, String attributeName, String attributeValue) {
        return waitForElementsWithAttribute(locator, attributeName, attributeValue, Duration.ofSeconds(DEFAULT_TIMEOUT));
    }

    public static List<WebElement> waitForElementsWithAttribute(By locator, String attributeName, String attributeValue, Duration timeout) {
        try {
            new WebDriverWait(DriverManager.getDriver(), timeout)
                    .until(driver -> {
                        List<WebElement> elements = driver.findElements(locator);
                        return elements.stream()
                                .anyMatch(e -> attributeValue.equals(e.getAttribute(attributeName)));
                    });
            return DriverManager.getDriver().findElements(locator);
        } catch (TimeoutException error) {
            throw new FrameworkTimeoutException(
                    "Elements with attribute " + attributeName + "=" + attributeValue + " not found: " + locator,
                    timeout, error);
        }
    }

    /**
     * Wait for list to be empty
     */
    public static void waitForElementsEmpty(By locator) {
        waitForElementsEmpty(locator, Duration.ofSeconds(DEFAULT_TIMEOUT));
    }

    public static void waitForElementsEmpty(By locator, Duration timeout) {
        try {
            new WebDriverWait(DriverManager.getDriver(), timeout)
                    .until(driver -> driver.findElements(locator).isEmpty());
            LOGGER.debug("All elements cleared: {}", locator);
        } catch (TimeoutException error) {
            int count = DriverManager.getDriver().findElements(locator).size();
            throw new FrameworkTimeoutException(
                    "Elements still present (" + count + "): " + locator, timeout, error);
        }
    }

    public static void waitForPageLoad() {
        new WebDriverWait(DriverManager.getDriver(), Duration.ofSeconds(DEFAULT_TIMEOUT))
                .until(driver -> "complete".equals(
                        ((JavascriptExecutor) driver).executeScript("return document.readyState")));
    }

    public static void waitForAjax() {
        JavascriptExecutor js = (JavascriptExecutor) DriverManager.getDriver();
        try {
            new WebDriverWait(DriverManager.getDriver(), Duration.ofSeconds(DEFAULT_TIMEOUT))
                    .until(driver -> {
                        Boolean jQueryDone = (Boolean) js.executeScript(
                                "return (typeof jQuery === 'undefined') || (jQuery.active === 0)");
                        Boolean docReady = "complete".equals(js.executeScript("return document.readyState"));
                        return Boolean.TRUE.equals(jQueryDone) && Boolean.TRUE.equals(docReady);
                    });
        } catch (TimeoutException error) {
            LOGGER.warn("AJAX/jQuery did not complete within timeout.");
        }
    }

    public static void waitForNetworkIdle() {
        JavascriptExecutor js = (JavascriptExecutor) DriverManager.getDriver();
        try {
            new WebDriverWait(DriverManager.getDriver(), Duration.ofSeconds(10))
                    .until(driver -> Boolean.TRUE.equals(js.executeScript(
                            "return window.performance.getEntriesByType('resource').filter(r => r.duration === 0).length === 0")));
        } catch (TimeoutException error) {
            LOGGER.warn("Network idle timeout. Continuing...");
        }
    }

    public static void waitForDomStable() {
        waitForPageLoad();
        waitForAjax();
        waitForNetworkIdle();
    }




    public static String waitForToastMessage(int timeoutSeconds) {
        try {
            WebDriverWait wait = new WebDriverWait(DriverManager.getDriver(), Duration.ofSeconds(timeoutSeconds));
            try {
                WebElement toast = wait.until(ExpectedConditions.visibilityOfElementLocated(TOAST_SUCCESS));
                String message = toast.getText().trim();
                LOGGER.info("Toast (success): {}", message);
                return message;
            } catch (TimeoutException ignored) {
                WebElement toast = wait.until(ExpectedConditions.visibilityOfElementLocated(TOAST_ERROR));
                String message = toast.getText().trim();
                LOGGER.warn("Toast (error): {}", message);
                return message;
            }
        } catch (TimeoutException error) {
            LOGGER.warn("No toast detected in {}s", timeoutSeconds);
            return "";
        }
    }

    public static String waitForToastMessage() {
        return waitForToastMessage(DEFAULT_TIMEOUT);
    }

    public static void waitForToastSync() {
        try {
            new WebDriverWait(DriverManager.getDriver(), Duration.ofSeconds(2))
                    .until(ExpectedConditions.presenceOfElementLocated(TOAST_SUCCESS));
            waitForInvisible(TOAST_SUCCESS);
        } catch (TimeoutException ignored) {
            LOGGER.debug("Toast success banner was not present during sync wait.");
        }
    }

    public static void waitForLookupResults() {
        try {
            new WebDriverWait(DriverManager.getDriver(), Duration.ofSeconds(DEFAULT_TIMEOUT))
                    .until(ExpectedConditions.visibilityOfElementLocated(LOOKUP_DROPDOWN));
            LOGGER.info("Lookup results appeared.");
        } catch (TimeoutException error) {
            LOGGER.warn("waitForLookupResults timed out: {}", error.getMessage());
        }
    }

    public static void waitForPicklistLoad() {
        try {
            new WebDriverWait(DriverManager.getDriver(), Duration.ofSeconds(DEFAULT_TIMEOUT))
                    .until(driver -> !driver.findElements(PICKLIST_DROPDOWN).isEmpty());
            LOGGER.info("Picklist options available.");
        } catch (TimeoutException error) {
            LOGGER.warn("waitForPicklistLoad timed out: {}", error.getMessage());
        }
    }

    public static void waitForModalClose() {
        try {
            new WebDriverWait(DriverManager.getDriver(), Duration.ofSeconds(DEFAULT_TIMEOUT))
                    .until(ExpectedConditions.invisibilityOfElementLocated(MODAL_DIALOG));
            LOGGER.info("Modal dialog closed.");
        } catch (TimeoutException error) {
            LOGGER.warn("waitForModalClose timed out: {}", error.getMessage());
        }
    }

    public static void waitForFrameAndSwitch(By iframeLocator) {
        try {
            new WebDriverWait(DriverManager.getDriver(), Duration.ofSeconds(DEFAULT_TIMEOUT))
                    .until(ExpectedConditions.frameToBeAvailableAndSwitchToIt(iframeLocator));
            LOGGER.info("Switched to iframe: {}", iframeLocator);
        } catch (TimeoutException error) {
            LOGGER.error("Could not switch to iframe: {}", iframeLocator);
            throw new FrameworkTimeoutException(
                    "Frame not available for switch: " + iframeLocator,
                    Duration.ofSeconds(DEFAULT_TIMEOUT),
                    error);
        }
    }



    public static void pause(Duration duration, String reason) {
        if (duration == null || duration.isZero() || duration.isNegative()) {
            return;
        }
        LOGGER.debug("Pausing for {} ms. Reason: {}", duration.toMillis(), reason);

        // FIX: Use busy-wait loop instead of LockSupport (guarantees full duration)
        long endTime = System.nanoTime() + duration.toNanos();
        while (System.nanoTime() < endTime) {
            Thread.onSpinWait(); // CPU-efficient busy-wait
        }

        if (Thread.currentThread().isInterrupted()) {
            Thread.currentThread().interrupt();
        }
    }

    public static boolean waitUntil(String description, Duration timeout, Duration pollingInterval,
            BooleanSupplier condition) {
        long deadline = System.nanoTime() + timeout.toNanos();
        while (System.nanoTime() < deadline) {
            if (condition.getAsBoolean()) {
                return true;
            }
            pause(pollingInterval, description);
        }
        LOGGER.warn("Condition did not complete within {} ms: {}", timeout.toMillis(), description);
        return false;
    }


}
