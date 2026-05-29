package resilience;

import core.base.ConfigManager;
import core.base.DriverManager;
import core.base.FrameworkException;
import core.base.FrameworkTimeoutException;
import core.base.ScreenshotService;
import core.base.WaitManager;
import org.openqa.selenium.*;
import org.openqa.selenium.interactions.Actions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.Assert;
import org.testng.asserts.SoftAssert;

import java.time.Duration;

/**
 * Centralized exception handler for all framework-level errors.
 * Single point of truth for exception handling, recovery, and logging.
 *
 * Key Responsibilities:
 * - Handle Selenium-specific exceptions
 * - Capture evidence (screenshots) on failures
 * - Provide recovery strategies for transient failures
 * - Support soft and hard assertions
 * - Log detailed error information
 *
 * Usage:
 * <pre>
 *     try {
 *         // Perform operation
 *         clickElement(locator);
 *     } catch (NoSuchElementException e) {
 *         ExceptionHandler.handleNoSuchElementException(e, "Login button");
 *     } catch (TimeoutException e) {
 *         ExceptionHandler.handleTimeoutException(e, "Element visibility");
 *     }
 *
 *     // Soft assertions
 *     ExceptionHandler.softAssert("Value check", expectedValue, actualValue);
 *     ExceptionHandler.assertAllSoft();
 * </pre>
 *
 * @author Framework Team
 * @version 3.0
 */
public final class ExceptionHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(ExceptionHandler.class);

    // ThreadLocal for soft assertions (per-thread instance)
    private static final ThreadLocal<SoftAssert> SOFT_ASSERTS = 
            ThreadLocal.withInitial(SoftAssert::new);

    // ──────────────────────────────────────────────────────────────────────────

    private ExceptionHandler() {
        throw new AssertionError("Cannot instantiate ExceptionHandler");
    }

    // ──────────────────────────────────────────────────────────────────────────
    // SELENIUM EXCEPTION HANDLERS
    // ──────────────────────────────────────────────────────────────────────────

    /**
     * Handles NoSuchElementException.
     * Captures screenshot and logs error.
     *
     * @param e       The exception
     * @param context The element context/locator
     * @throws FrameworkException Always throws with wrapped exception
     */
    public static void handleNoSuchElementException(NoSuchElementException e, String context) {
        LOGGER.error("Element not found: {}", context, e);
        ScreenshotService.capture("no_such_element_" + System.currentTimeMillis());
        throw new FrameworkException(
                "Element not found: " + context +
                " | Error: " + e.getMessage(), e);
    }

    /**
     * Handles StaleElementReferenceException.
     * This exception typically requires a retry from the caller.
     *
     * @param e       The exception
     * @param context The element context
     * @throws FrameworkException Always throws with wrapped exception
     */
    public static void handleStaleElementException(StaleElementReferenceException e, String context) {
        LOGGER.warn("Stale element reference on '{}'. Retry recommended.", context, e);
        throw new FrameworkException(
                "Stale element reference: " + context +
                ". The element reference is no longer valid and action should be retried.",
                e);
    }

    /**
     * Handles TimeoutException.
     * Captures screenshot and logs detailed error information.
     *
     * @param e       The exception
     * @param context The wait context
     * @throws FrameworkTimeoutException Always throws with timeout information
     */
    public static void handleTimeoutException(TimeoutException e, String context) {
        LOGGER.error("Timeout waiting for '{}'. Duration exceeded.", context, e);
        ScreenshotService.capture("timeout_" + System.currentTimeMillis());
        
        Duration timeout = Duration.ofSeconds(ConfigManager.getExplicitWaitTimeout());
        throw new FrameworkTimeoutException(
                "Timeout: " + context +
                " | Configured timeout: " + timeout.getSeconds() + "s",
                timeout, e);
    }

    /**
     * Handles ElementClickInterceptedException.
     * Attempts recovery before failing.
     *
     * @param e       The exception
     * @param context The element context
     * @throws FrameworkException After recovery attempt fails
     */
    public static void handleElementClickInterceptedException(ElementClickInterceptedException e, 
                                                              String context) {
        LOGGER.warn("Click intercepted on '{}'. Attempting recovery...", context, e);
        
        try {
            // Attempt to stabilize the page
            WaitManager.waitForPageLoad();
            Thread.sleep(500); // Brief pause
        } catch (InterruptedException ie) {
            Thread.currentThread().interrupt();
            LOGGER.debug("Recovery interrupted");
        } catch (Exception recoveryError) {
            LOGGER.debug("Page stabilization failed: {}", recoveryError.getMessage());
        }
        
        ScreenshotService.capture("click_intercepted_" + System.currentTimeMillis());
        throw new FrameworkException(
                "Element click intercepted: " + context +
                " | Error: " + e.getMessage() +
                " | Another element was blocking the click.",
                e);
    }

    /**
     * Handles ElementNotInteractableException.
     * Element exists but cannot be interacted with.
     *
     * @param e       The exception
     * @param context The element context
     * @throws FrameworkException Always throws
     */
    public static void handleElementNotInteractableException(ElementNotInteractableException e, 
                                                            String context) {
        LOGGER.error("Element not interactable: {}", context, e);
        ScreenshotService.capture("not_interactable_" + System.currentTimeMillis());
        throw new FrameworkException(
                "Element not interactable: " + context +
                " | The element exists but cannot be interacted with." +
                " | May be hidden or disabled.",
                e);
    }

    /**
     * Handles InvalidElementStateException.
     * Element is in an invalid state for the operation.
     *
     * @param e       The exception
     * @param context The element context
     * @throws FrameworkException Always throws
     */
    public static void handleInvalidElementStateException(InvalidElementStateException e, 
                                                         String context) {
        LOGGER.error("Invalid element state: {}", context, e);
        throw new FrameworkException(
                "Invalid element state: " + context +
                " | Element is in an invalid state for the requested operation.",
                e);
    }

    /**
     * Handles WebDriverException (generic Selenium error).
     * Captures screenshot and logs error.
     *
     * @param e       The exception
     * @param context The operation context
     * @throws FrameworkException Always throws
     */
    public static void handleWebDriverException(WebDriverException e, String context) {
        LOGGER.error("WebDriver error during {}: {}", context, e.getMessage(), e);
        if (DriverManager.hasActiveDriver()) {
            ScreenshotService.capture("webdriver_error_" + System.currentTimeMillis());
        }
        throw new FrameworkException(
                "WebDriver error: " + context +
                " | Error: " + e.getMessage(),
                e);
    }

    /**
     * Handles generic exception.
     * Logs error and wraps in FrameworkException.
     *
     * @param e       The exception
     * @param context The operation context
     * @throws FrameworkException Always throws
     */
    public static void handleGenericException(Exception e, String context) {
        LOGGER.error("Error during {}: {}", context, e.getMessage(), e);
        if (DriverManager.hasActiveDriver()) {
            ScreenshotService.capture("error_" + System.currentTimeMillis());
        }
        throw new FrameworkException(
                "Error: " + context +
                " | Error: " + e.getMessage(),
                e);
    }

    // ──────────────────────────────────────────────────────────────────────────
    // ASSERTION METHODS
    // ──────────────────────────────────────────────────────────────────────────

    /**
     * Performs a hard assertion (fails immediately).
     *
     * @param message The assertion message
     * @param actual  The actual value
     * @param expected The expected value
     */
    public static void assertEquals(String message, Object actual, Object expected) {
        try {
            Assert.assertEquals(actual, expected, message);
        } catch (AssertionError e) {
            LOGGER.error("Assertion failed: {}", message);
            ScreenshotService.capture("assertion_failure_" + System.currentTimeMillis());
            throw e;
        }
    }

    /**
     * Performs a hard assertion for boolean.
     *
     * @param message The assertion message
     * @param condition The condition to assert
     */
    public static void assertTrue(String message, boolean condition) {
        try {
            Assert.assertTrue(condition, message);
        } catch (AssertionError e) {
            LOGGER.error("Assertion failed: {}", message);
            ScreenshotService.capture("assertion_failure_" + System.currentTimeMillis());
            throw e;
        }
    }

    /**
     * Performs a hard assertion (false condition).
     *
     * @param message The assertion message
     * @param condition The condition that should be false
     */
    public static void assertFalse(String message, boolean condition) {
        try {
            Assert.assertFalse(condition, message);
        } catch (AssertionError e) {
            LOGGER.error("Assertion failed: {}", message);
            throw e;
        }
    }

    /**
     * Performs a hard assertion (not null).
     *
     * @param message The assertion message
     * @param object The object that should not be null
     */
    public static void assertNotNull(String message, Object object) {
        try {
            Assert.assertNotNull(object, message);
        } catch (AssertionError e) {
            LOGGER.error("Assertion failed: {}", message);
            throw e;
        }
    }

    /**
     * Performs a hard assertion (null).
     *
     * @param message The assertion message
     * @param object The object that should be null
     */
    public static void assertNull(String message, Object object) {
        try {
            Assert.assertNull(object, message);
        } catch (AssertionError e) {
            LOGGER.error("Assertion failed: {}", message);
            throw e;
        }
    }

    /**
     * Fails a test with a message.
     *
     * @param message The failure message
     */
    public static void failTest(String message) {
        LOGGER.error("Test failed: {}", message);
        ScreenshotService.capture("test_failure_" + System.currentTimeMillis());
        Assert.fail(message);
    }

    /**
     * Fails a test with a message and cause.
     *
     * @param message The failure message
     * @param cause The cause exception
     */
    public static void failTest(String message, Throwable cause) {
        LOGGER.error("Test failed: {}", message, cause);
        ScreenshotService.capture("test_failure_" + System.currentTimeMillis());
        Assert.fail(message, cause);
    }

    // ──────────────────────────────────────────────────────────────────────────
    // SOFT ASSERTION METHODS
    // ──────────────────────────────────────────────────────────────────────────

    /**
     * Performs a soft assertion (collects failures, doesn't fail immediately).
     *
     * @param message The assertion message
     * @param actual The actual value
     * @param expected The expected value
     */
    public static void softAssertEquals(String message, Object actual, Object expected) {
        SoftAssert soft = SOFT_ASSERTS.get();
        soft.assertEquals(actual, expected, message);
        if (!actual.equals(expected)) {
            LOGGER.warn("Soft assertion failed: {}", message);
        }
    }

    /**
     * Performs a soft assertion for boolean.
     *
     * @param message The assertion message
     * @param condition The condition to assert
     */
    public static void softAssertTrue(String message, boolean condition) {
        SoftAssert soft = SOFT_ASSERTS.get();
        soft.assertTrue(condition, message);
        if (!condition) {
            LOGGER.warn("Soft assertion failed: {}", message);
        }
    }

    /**
     * Performs a soft assertion (false condition).
     *
     * @param message The assertion message
     * @param condition The condition that should be false
     */
    public static void softAssertFalse(String message, boolean condition) {
        SoftAssert soft = SOFT_ASSERTS.get();
        soft.assertFalse(condition, message);
        if (condition) {
            LOGGER.warn("Soft assertion failed: {}", message);
        }
    }

    /**
     * Asserts all collected soft assertions.
     * Fails if any soft assertion failed.
     *
     * @throws AssertionError If any soft assertion failed
     */
    public static void assertAllSoft() {
        SoftAssert soft = SOFT_ASSERTS.get();
        try {
            soft.assertAll();
        } finally {
            // Reset for next test
            SOFT_ASSERTS.remove();
        }
    }

    /**
     * Resets soft assertions for current thread.
     */
    public static void resetSoftAssertions() {
        SOFT_ASSERTS.remove();
    }

    // ──────────────────────────────────────────────────────────────────────────
    // RECOVERY METHODS
    // ──────────────────────────────────────────────────────────────────────────

    /**
     * Attempts to recover from an unexpected modal/popup.
     * Tries to close the modal using common patterns.
     */
    public static void closeUnexpectedPopup() {
        AutoRecoveryEngine.closeUnexpectedPopup();
    }

    /**
     * Attempts to refresh the page after a failure.
     * Useful for transient failures.
     */
    public static void refreshAndRecover() {
        LOGGER.info("Attempting page refresh for recovery");
        try {
            DriverManager.getDriver().navigate().refresh();
            WaitManager.waitForPageLoad();
            LOGGER.info("Page refreshed successfully");
        } catch (Exception e) {
            LOGGER.error("Failed to refresh page: {}", e.getMessage());
        }
    }

    /**
     * Attempts to navigate back and recover.
     */
    public static void navigateBackAndRecover() {
        LOGGER.info("Attempting to navigate back for recovery");
        try {
            DriverManager.getDriver().navigate().back();
            WaitManager.waitForPageLoad();
            LOGGER.info("Navigated back successfully");
        } catch (Exception e) {
            LOGGER.error("Failed to navigate back: {}", e.getMessage());
        }
    }

    /**
     * Clears all browser cookies and refreshes.
     * Useful for session-related failures.
     */
    public static void clearCookiesAndRecover() {
        LOGGER.info("Clearing cookies for recovery");
        try {
            DriverManager.getDriver().manage().deleteAllCookies();
            DriverManager.getDriver().navigate().refresh();
            WaitManager.waitForPageLoad();
            LOGGER.info("Cookies cleared and page refreshed");
        } catch (Exception e) {
            LOGGER.error("Failed to clear cookies: {}", e.getMessage());
        }
    }

    /**
     * Re-throws a RuntimeException after logging and capturing a screenshot.
     * Used by ExecutionPipeline to mark task failures without swallowing context.
     *
     * @param message Descriptive failure message
     * @param cause   The original RuntimeException
     */
    public static void rethrowAsFail(String message, RuntimeException cause) {
        LOGGER.error("{}: {}", message, cause.getMessage(), cause);
        if (DriverManager.hasActiveDriver()) {
            ScreenshotService.capture("pipeline_failure_" + System.currentTimeMillis());
        }
        throw cause;
    }
}
