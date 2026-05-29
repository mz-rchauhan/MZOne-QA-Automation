package core.base;

import observability.TestObservabilityEngine;
import org.openqa.selenium.*;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.ui.Select;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import resilience.RetryEngine;


import java.util.ArrayList;
import java.util.List;
import java.util.Objects;


/**
 * Unified Selenium Commands - Central hub for all WebDriver operations.
 */
public class SeleniumCommands {

    private static final Logger LOGGER = LoggerFactory.getLogger(SeleniumCommands.class);

    // Configuration constants
    private static final int RETRY_COUNT = 3;
    private static final long RETRY_DELAY_MS = 250L;
    private static final String ELEMENT_NOT_FOUND = "Element not found: ";

    /**
     * Clicks an element located by the given By locator.
     */
    public void click(By locator) {
        executeObserved("click", locator.toString(),
                () -> WaitManager.waitForClickable(locator).click());
    }

    /**
     * Clicks a specific WebElement.
     *
     * @param element The WebElement to click
     */
    public void click(WebElement element) {
        Objects.requireNonNull(element, "WebElement cannot be null");
        executeObserved("click", describe(element), element::click);
    }

    /**
     * Safely clicks an element with fallback to JavaScript click.
     *
     * @param locator The element locator
     */
    public void safeClick(By locator) {
        try {
            click(locator);
        } catch (ElementClickInterceptedException e) {
            LOGGER.warn("Element click intercepted, falling back to JavaScript click: {}", locator);
            clickWithJavaScript(locator);
        }
    }

    /**
     * Safely clicks a WebElement with JavaScript fallback.
     *
     * @param element The WebElement to click
     */
    public void safeClick(WebElement element) {
        try {
            click(element);
        } catch (ElementClickInterceptedException e) {
            LOGGER.warn("Element click intercepted, using JavaScript click for: {}", describe(element));
            clickWithJavaScript(element);
        }
    }

    /**
     * Clicks an element using JavaScript (useful for hidden or intercepted elements).
     *
     * @param locator The element locator
     */
    public void clickWithJavaScript(By locator) {
        executeObserved("jsClick", locator.toString(),
                () -> executeJS("arguments[0].click();", WaitManager.waitForPresence(locator)));
    }

    /**
     * Clicks a WebElement using JavaScript.
     *
     * @param element The WebElement to click
     */
    public void clickWithJavaScript(WebElement element) {
        Objects.requireNonNull(element, "WebElement cannot be null");
        executeObserved("jsClick", describe(element), () -> executeJS("arguments[0].click();", element));
    }

    /**
     * Double-clicks an element.
     *
     * @param locator The element locator
     */
    public void doubleClick(By locator) {
        executeObserved("doubleClick", locator.toString(),
                () -> new Actions(DriverManager.getDriver())
                        .doubleClick(WaitManager.waitForClickable(locator))
                        .perform());
    }

    /**
     * Double-clicks a WebElement.
     *
     * @param element The WebElement to double-click
     */
    public void doubleClick(WebElement element) {
        Objects.requireNonNull(element, "WebElement cannot be null");
        executeObserved("doubleClick", describe(element),
                () -> new Actions(DriverManager.getDriver()).doubleClick(element).perform());
    }

    /**
     * Right-clicks (context click) an element.
     *
     * @param locator The element locator
     */
    public void rightClick(By locator) {
        executeObserved("rightClick", locator.toString(),
                () -> new Actions(DriverManager.getDriver())
                        .contextClick(WaitManager.waitForClickable(locator))
                        .perform());
    }

    /**
     * Right-clicks a WebElement.
     *
     * @param element The WebElement to right-click
     */
    public void rightClick(WebElement element) {
        Objects.requireNonNull(element, "WebElement cannot be null");
        executeObserved("rightClick", describe(element),
                () -> new Actions(DriverManager.getDriver()).contextClick(element).perform());
    }

    /**
     * Clicks at specific coordinates relative to viewport.
     *
     * @param x The x-coordinate
     * @param y The y-coordinate
     */
    public void clickAtCoordinates(int x, int y) {
        executeObserved("clickAtCoordinates", x + "," + y,
                () -> new Actions(DriverManager.getDriver())
                        .moveByOffset(x, y)
                        .click()
                        .perform());
    }

    /**
     * Retries clicking an element to handle StaleElementReferenceException.
     *
     * @param locator The element locator
     */
    public void retryClick(By locator) {
        RetryEngine.executeWithRetry(
                () -> click(locator),
                "retryClick:" + locator,
                RETRY_COUNT,
                RETRY_DELAY_MS);
    }

    /**
     * Enters text into an element (clears then types).
     *
     * @param locator The element locator
     * @param text    The text to enter
     */
    public void enterText(By locator, String text) {
        Objects.requireNonNull(text, "Text cannot be null");
        executeObserved("enterText", locator.toString(), () -> {
            WebElement element = WaitManager.waitForVisible(locator);
            element.clear();
            element.sendKeys(text);
        });
    }

    /**
     * Enters text into a WebElement.
     *
     * @param element The WebElement
     * @param text    The text to enter
     */
    public void enterText(WebElement element, String text) {
        Objects.requireNonNull(element, "WebElement cannot be null");
        Objects.requireNonNull(text, "Text cannot be null");
        executeObserved("enterText", describe(element), () -> {
            element.clear();
            element.sendKeys(text);
        });
    }

    /**
     * Appends text to an element (without clearing).
     *
     * @param locator The element locator
     * @param text    The text to append
     */
    public void appendText(By locator, String text) {
        Objects.requireNonNull(text, "Text cannot be null");
        executeObserved("appendText", locator.toString(),
                () -> WaitManager.waitForVisible(locator).sendKeys(text));
    }

    /**
     * Appends text to a WebElement.
     *
     * @param element The WebElement
     * @param text    The text to append
     */
    public void appendText(WebElement element, String text) {
        Objects.requireNonNull(element, "WebElement cannot be null");
        Objects.requireNonNull(text, "Text cannot be null");
        executeObserved("appendText", describe(element), () -> element.sendKeys(text));
    }

    /**
     * Clears text from an input element.
     *
     * @param locator The element locator
     */
    public void clearText(By locator) {
        executeObserved("clearText", locator.toString(),
                () -> WaitManager.waitForVisible(locator).clear());
    }

    /**
     * Clears text from a WebElement.
     *
     * @param element The WebElement
     */
    public void clearText(WebElement element) {
        Objects.requireNonNull(element, "WebElement cannot be null");
        executeObserved("clearText", describe(element), element::clear);
    }

    /**
     * Clears and enters text into an input element using alternative method.
     * Uses Ctrl+A followed by text entry.
     *
     * @param locator The element locator
     * @param text    The text to enter
     */
    public void clearAndEnterText(By locator, String text) {
        Objects.requireNonNull(text, "Text cannot be null");
        executeObserved("clearAndEnterText", locator.toString(), () -> {
            WebElement element = WaitManager.waitForVisible(locator);
            element.sendKeys(Keys.CONTROL, "a");
            element.sendKeys(text);
        });
    }

    /**
     * Enters text using JavaScript (bypasses Selenium input filtering).
     *
     * @param locator The element locator
     * @param text    The text to enter
     */
    public void enterTextWithJS(By locator, String text) {
        Objects.requireNonNull(text, "Text cannot be null");
        executeObserved("enterTextWithJS", locator.toString(), () -> {
            WebElement element = WaitManager.waitForPresence(locator);
            executeJS("arguments[0].value = arguments[1];", element, text);
        });
    }

    /**
     * Enters text using JavaScript (bypasses Selenium input filtering).
     *
     * @param element The Web element
     * @param text    The text to enter
     */
    public void enterTextWithJS(WebElement element, String text) {
        Objects.requireNonNull(text, "Text cannot be null");
        executeObserved("enterTextWithJS", element.toString(), () -> {
            executeJS("arguments[0].value = arguments[1];", element, text);
        });
    }

    /**
     * Sends keys to an element (raw key input without clearing).
     *
     * @param locator The element locator
     * @param keys    The keys to send
     */
    public void sendKeys(By locator, CharSequence... keys) {
        executeObserved("sendKeys", locator.toString(),
                () -> DriverManager.getDriver().findElement(locator).sendKeys(keys));
    }

    /**
     * Sends keys to a WebElement.
     *
     * @param element The WebElement
     * @param keys    The keys to send
     */
    public void sendKeys(WebElement element, CharSequence... keys) {
        Objects.requireNonNull(element, "WebElement cannot be null");
        executeObserved("sendKeys", describe(element), () -> element.sendKeys(keys));
    }

    /**
     * Presses the Enter key on an element.
     *
     * @param locator The element locator
     */
    public void pressEnter(By locator) {
        sendKeys(locator, Keys.ENTER);
    }

    /**
     * Presses the Enter key on an element.
     *
     * @param ele The element locator
     */
    public void pressEnter(WebElement ele) {
        sendKeys(ele, Keys.ENTER);
    }

    /**
     * Presses the Tab key on an element.
     *
     * @param locator The element locator
     */
    public void pressTab(By locator) {
        sendKeys(locator, Keys.TAB);
    }

    /**
     * Presses the Escape key on an element.
     *
     * @param locator The element locator
     */
    public void pressEscape(By locator) {
        sendKeys(locator, Keys.ESCAPE);
    }

    /**
     * Submits a form element.
     *
     * @param locator The form element locator
     */
    public void submitForm(By locator) {
        executeObserved("submitForm", locator.toString(),
                () -> WaitManager.waitForPresence(locator).submit());
    }


    /**
     * Opens a new window and navigates to the exact same URL as the current window.
     */
    public void duplicateCurrentWindow() {
        try {
            // 1. Grab the URL of the current window
            String currentUrl = getCurrentUrl();
            LOGGER.info("Duplicating current window. Saving URL: {}", currentUrl);

            // 2. Open a new window and switch to it
            DriverManager.getDriver().switchTo().newWindow(WindowType.WINDOW); // Use WindowType.TAB if you prefer a tab

            navigateTo(currentUrl);

            LOGGER.info("Successfully duplicated the window and loaded the URL.");

        } catch (Exception e) {
            LOGGER.error("Failed to duplicate the current window. Error: {}", e.getMessage());
            throw new RuntimeException("Could not duplicate window", e);
        }
    }

    /**
     * Hovers over an element.
     *
     * @param locator The element locator
     */
    public void hover(By locator) {
        executeObserved("hover", locator.toString(),
                () -> new Actions(DriverManager.getDriver())
                        .moveToElement(WaitManager.waitForVisible(locator))
                        .perform());
    }

    /**
     * Hovers over a WebElement.
     *
     * @param element The WebElement
     */
    public void hover(WebElement element) {
        Objects.requireNonNull(element, "WebElement cannot be null");
        executeObserved("hover", describe(element),
                () -> new Actions(DriverManager.getDriver()).moveToElement(element).perform());
    }

    /**
     * Hovers over an element using JavaScript (useful when Actions fails).
     *
     * @param locator The element locator
     */
    public void hoverWithJS(By locator) {
        executeObserved("hoverWithJS", locator.toString(), () -> {
            WebElement element = WaitManager.waitForPresence(locator);
            executeJS("arguments[0].scrollIntoView(true);", element);
            new Actions(DriverManager.getDriver()).moveToElement(element).perform();
        });
    }

    /**
     * Drag and drop an element to another.
     *
     * @param sourceLocator      The source element locator
     * @param destinationLocator The destination element locator
     */
    public void dragAndDrop(By sourceLocator, By destinationLocator) {
        executeObserved("dragAndDrop", sourceLocator + " -> " + destinationLocator, () -> {
            WebElement source = WaitManager.waitForPresence(sourceLocator);
            WebElement destination = WaitManager.waitForPresence(destinationLocator);
            new Actions(DriverManager.getDriver()).dragAndDrop(source, destination).perform();
        });
    }

    /**
     * Drag and drop a WebElement to another.
     *
     * @param source      The source WebElement
     * @param destination The destination WebElement
     */
    public void dragAndDrop(WebElement source, WebElement destination) {
        Objects.requireNonNull(source, "Source element cannot be null");
        Objects.requireNonNull(destination, "Destination element cannot be null");
        executeObserved("dragAndDrop", describe(source) + " -> " + describe(destination),
                () -> new Actions(DriverManager.getDriver()).dragAndDrop(source, destination).perform());
    }

    /**
     * Scrolls to an element and centers it in viewport.
     *
     * @param locator The element locator
     */
    public void scrollToElement(By locator) {
        executeObserved("scrollToElement", locator.toString(), () ->
                executeJS("arguments[0].scrollIntoView({behavior:'instant',block:'center'});",
                        WaitManager.waitForPresence(locator)));
    }

    /**
     * Scrolls to a WebElement.
     *
     * @param element The WebElement
     */
    public void scrollToElement(WebElement element) {
        Objects.requireNonNull(element, "WebElement cannot be null");
        executeObserved("scrollToElement", describe(element),
                () -> executeJS("arguments[0].scrollIntoView(true);", element));
    }

    /**
     * Scrolls to the top of the page.
     */
    public void scrollToTop() {
        executeObserved("scrollToTop", "document", () -> executeJS("window.scrollTo(0, 0);"));
    }

    /**
     * Scrolls to the bottom of the page.
     */
    public void scrollToBottom() {
        executeObserved("scrollToBottom", "document",
                () -> executeJS("window.scrollTo(0, document.body.scrollHeight);"));
    }

    /**
     * Scrolls by a specific amount.
     *
     * @param xOffset The horizontal offset
     * @param yOffset The vertical offset
     */
    public void scrollBy(int xOffset, int yOffset) {
        executeObserved("scrollBy", xOffset + "," + yOffset,
                () -> executeJS("window.scrollBy(" + xOffset + ", " + yOffset + ");"));
    }

    /**
     * Scrolls within an element container.
     *
     * @param element The scrollable element
     * @param pixels  The number of pixels to scroll down
     */
    public void scrollInElement(WebElement element, int pixels) {
        Objects.requireNonNull(element, "WebElement cannot be null");
        executeObserved("scrollInElement", describe(element) + ":" + pixels,
                () -> executeJS("arguments[0].scrollTop += " + pixels + ";", element));
    }

    /**
     * Scrolls repeatedly until an element is visible.
     *
     * @param locator The element locator
     */
    public void scrollUntilElementVisible(By locator) {
        executeObserved("scrollUntilElementVisible", locator.toString(), () -> {
            int attempts = 0;
            while (attempts < 10 && !isElementPresent(locator)) {
                executeJS("window.scrollBy(0, window.innerHeight * 0.8);");
                attempts++;
            }
            if (!isElementPresent(locator)) {
                throw new FrameworkException("Element not visible after scrolling: " + locator);
            }
        });
    }

    /**
     * Switches to a frame by locator.
     *
     * @param locator The frame element locator
     */
    public void switchToFrame(By locator) {
        executeObserved("switchToFrame", locator.toString(),
                () -> DriverManager.getDriver().switchTo()
                        .frame(WaitManager.waitForPresence(locator)));
    }

    /**
     * Switches to a frame by WebElement.
     *
     * @param frameElement The frame WebElement
     */
    public void switchToFrame(WebElement frameElement) {
        Objects.requireNonNull(frameElement, "Frame element cannot be null");
        executeObserved("switchToFrame", describe(frameElement),
                () -> DriverManager.getDriver().switchTo().frame(frameElement));
    }

    /**
     * Switches to a frame by index.
     *
     * @param frameIndex The frame index
     */
    public void switchToFrame(int frameIndex) {
        executeObserved("switchToFrame", "index:" + frameIndex,
                () -> DriverManager.getDriver().switchTo().frame(frameIndex));
    }

    /**
     * Switches to a frame by name or ID.
     *
     * @param frameNameOrId The frame name or ID attribute
     */
    public void switchToFrameByNameOrId(String frameNameOrId) {
        Objects.requireNonNull(frameNameOrId, "Frame name/ID cannot be null");
        executeObserved("switchToFrameByNameOrId", frameNameOrId,
                () -> DriverManager.getDriver().switchTo().frame(frameNameOrId));
    }

    /**
     * Switches to default content (exits all frames).
     */
    public void switchToDefaultContent() {
        executeObserved("switchToDefaultContent", "default",
                () -> DriverManager.getDriver().switchTo().defaultContent());
    }

    /**
     * Switches to parent frame.
     */
    public void switchToParentFrame() {
        executeObserved("switchToParentFrame", "parent",
                () -> DriverManager.getDriver().switchTo().parentFrame());
    }

    /**
     * Switches to a window by title (partial match).
     *
     * @param windowTitle The window title or part of it
     */
    public void switchToWindowByTitle(String windowTitle) {
        Objects.requireNonNull(windowTitle, "Window title cannot be null");
        executeObserved("switchToWindowByTitle", windowTitle, () -> {
            WebDriver driver = DriverManager.getDriver();
            for (String handle : driver.getWindowHandles()) {
                driver.switchTo().window(handle);
                if (driver.getTitle().contains(windowTitle)) {
                    return;
                }
            }
            throw new FrameworkException("Window with title containing '" + windowTitle + "' not found");
        });
    }


    /**
     * Opens a new tab and navigates to the exact same URL as the current tab.
     */
    public void duplicateCurrentTab() {
        try {
            // 1. Grab the URL of the current tab
            String currentUrl = getCurrentUrl();
            LOGGER.info("Duplicating current tab. Saving URL: {}", currentUrl);
            // 2. Open a new tab and switch to it
            DriverManager.getDriver().switchTo().newWindow(WindowType.TAB);

            // 3. Navigate to the saved URL
            navigateTo(currentUrl);

            LOGGER.info("Successfully duplicated the tab and loaded the URL.");

        } catch (Exception e) {
            LOGGER.error("Failed to duplicate the current tab. Error: {}", e.getMessage());
            throw new RuntimeException("Could not duplicate tab", e);
        }
    }

    /**
     * Switches to the latest opened tab/window.
     */
    public void switchToNewTab() {
        executeObserved("switchToNewTab", "latest", () -> {
            WebDriver driver = DriverManager.getDriver();
            List<String> tabs = new ArrayList<>(driver.getWindowHandles());
            if (tabs.size() < 2) {
                throw new FrameworkException("Only one tab open, cannot switch to new tab");
            }
            driver.switchTo().window(tabs.get(tabs.size() - 1));
            WaitManager.waitForPageLoad();
        });
    }

    /**
     * Switches to the original (first) tab.
     */
    public void switchToOriginalTab() {
        executeObserved("switchToOriginalTab", "first", () -> {
            WebDriver driver = DriverManager.getDriver();
            List<String> tabs = new ArrayList<>(driver.getWindowHandles());
            driver.switchTo().window(tabs.get(0));
        });
    }

    /**
     * Switches to a tab by index.
     *
     * @param tabIndex The tab index (0-based)
     */
    public void switchToTab(int tabIndex) {
        executeObserved("switchToTab", String.valueOf(tabIndex), () -> {
            WebDriver driver = DriverManager.getDriver();
            List<String> tabs = new ArrayList<>(driver.getWindowHandles());
            if (tabIndex >= tabs.size()) {
                throw new FrameworkException("Tab index " + tabIndex + " not available");
            }
            driver.switchTo().window(tabs.get(tabIndex));
            WaitManager.waitForPageLoad();
        });
    }

    /**
     * Closes the current tab/window and switches to original.
     */
    public void closeCurrentTabAndReturnToOriginal() {
        executeObserved("closeCurrentTabAndReturnToOriginal", "close", () -> {
            DriverManager.getDriver().close();
            switchToOriginalTab();
        });
    }

    /**
     * Closes all windows except the current one.
     * Useful for cleanup when multiple windows are opened.
     */
    public static void closeOtherWindows() {
        WebDriver driver = DriverManager.getDriver();
        String currentWindowHandle = driver.getWindowHandle();
        for (String windowHandle : driver.getWindowHandles()) {
            if (!windowHandle.equals(currentWindowHandle)) {
                driver.switchTo().window(windowHandle);
                driver.close();
            }
        }
        driver.switchTo().window(currentWindowHandle);
        LOGGER.debug("Closed other windows, returned to original window");
    }


    /**
     * Closes the current window.
     */
    public void closeCurrentWindow() {
        executeObserved("closeCurrentWindow", "current",
                () -> DriverManager.getDriver().close());
    }

    /**
     * Accepts an alert dialog.
     */
    public void acceptAlert() {
        executeObserved("acceptAlert", "confirm",
                () -> DriverManager.getDriver().switchTo().alert().accept());
    }

    /**
     * Dismisses an alert dialog.
     */
    public void dismissAlert() {
        executeObserved("dismissAlert", "cancel",
                () -> DriverManager.getDriver().switchTo().alert().dismiss());
    }

    /**
     * Gets the text from an alert dialog.
     *
     * @return The alert message text
     */
    public String getAlertText() {
        return executeAndObserve("getAlertText", "text",
                () -> DriverManager.getDriver().switchTo().alert().getText());
    }

    /**
     * Enters text into an alert dialog.
     *
     * @param text The text to enter
     */
    public void sendKeysToAlert(String text) {
        Objects.requireNonNull(text, "Text cannot be null");
        executeObserved("sendKeysToAlert", text,
                () -> DriverManager.getDriver().switchTo().alert().sendKeys(text));
    }

    /**
     * Selects an option by visible text.
     *
     * @param locator The select element locator
     * @param text    The option text to select
     */
    public void selectByVisibleText(By locator, String text) {
        Objects.requireNonNull(text, "Text cannot be null");
        executeObserved("selectByVisibleText", locator + ":" + text, () -> {
            Select select = new Select(WaitManager.waitForVisible(locator));
            select.selectByVisibleText(text);
        });
    }

    /**
     * Selects an option by value.
     *
     * @param locator The select element locator
     * @param value   The option value to select
     */
    public void selectByValue(By locator, String value) {
        Objects.requireNonNull(value, "Value cannot be null");
        executeObserved("selectByValue", locator + ":" + value, () -> {
            Select select = new Select(WaitManager.waitForVisible(locator));
            select.selectByValue(value);
        });
    }

    /**
     * Selects an option by index.
     *
     * @param locator The select element locator
     * @param index   The option index (0-based)
     */
    public void selectByIndex(By locator, int index) {
        executeObserved("selectByIndex", locator + ":" + index, () -> {
            Select select = new Select(WaitManager.waitForVisible(locator));
            select.selectByIndex(index);
        });
    }

    /**
     * Gets the currently selected option text.
     *
     * @param locator The select element locator
     * @return The selected option text
     */
    public String getSelectedOptionText(By locator) {
        return executeAndObserve("getSelectedOptionText", locator.toString(), () -> {
            Select select = new Select(WaitManager.waitForVisible(locator));
            return select.getFirstSelectedOption().getText().trim();
        });
    }

    /**
     * Gets the currently selected option value.
     *
     * @param locator The select element locator
     * @return The selected option value
     */
    public String getSelectedOptionValue(By locator) {
        return executeAndObserve("getSelectedOptionValue", locator.toString(), () -> {
            Select select = new Select(WaitManager.waitForVisible(locator));
            return select.getFirstSelectedOption().getAttribute("value");
        });
    }

    /**
     * Gets all available options in a select element.
     *
     * @param locator The select element locator
     * @return List of option texts
     */
    public List<String> getAllOptions(By locator) {
        return executeAndObserve("getAllOptions", locator.toString(), () -> {
            Select select = new Select(WaitManager.waitForVisible(locator));
            List<String> options = new ArrayList<>();
            for (WebElement option : select.getOptions()) {
                options.add(option.getText().trim());
            }
            return options;
        });
    }

    /**
     * Gets all selected options (for multi-select).
     *
     * @param locator The select element locator
     * @return List of selected option texts
     */
    public List<String> getAllSelectedOptions(By locator) {
        return executeAndObserve("getAllSelectedOptions", locator.toString(), () -> {
            Select select = new Select(WaitManager.waitForVisible(locator));
            List<String> selected = new ArrayList<>();
            for (WebElement option : select.getAllSelectedOptions()) {
                selected.add(option.getText().trim());
            }
            return selected;
        });
    }

    /**
     * Deselects all options (for multi-select).
     *
     * @param locator The select element locator
     */
    public void deselectAll(By locator) {
        executeObserved("deselectAll", locator.toString(), () -> {
            Select select = new Select(WaitManager.waitForVisible(locator));
            select.deselectAll();
        });
    }

    /**
     * Deselects an option by visible text.
     *
     * @param locator The select element locator
     * @param text    The option text to deselect
     */
    public void deselectByVisibleText(By locator, String text) {
        Objects.requireNonNull(text, "Text cannot be null");
        executeObserved("deselectByVisibleText", locator + ":" + text, () -> {
            Select select = new Select(WaitManager.waitForVisible(locator));
            select.deselectByVisibleText(text);
        });
    }


    /**
     * Verifies if an element is displayed.
     *
     * @param locator The element locator
     */
    public boolean verifyElementDisplayed(By locator) {
        try {
            return WaitManager.waitForVisible(locator).isDisplayed();
        } catch (TimeoutException | NoSuchElementException | StaleElementReferenceException e) {
            LOGGER.debug("Element not displayed: {}", locator);
            return false;
        }
    }

    /**
     * Verifies if a WebElement is displayed.
     *
     * @param element The WebElement
     */
    public boolean verifyElementDisplayed(WebElement element) {
        try {
            return element.isDisplayed();
        } catch (WebDriverException e) {
            return false;
        }
    }

    /**
     * Verifies if an element is NOT displayed.
     *
     * @param locator The element locator
     */
    public boolean verifyElementNotDisplayed(By locator) {
        try {
            return WaitManager.waitForInvisible(locator);
        } catch (Exception e) {
            return true; // Element not found = not displayed
        }
    }

    /**
     * Verifies if an element is enabled.
     *
     * @param locator The element locator
     */
    public boolean verifyElementEnabled(By locator) {
        try {
            return WaitManager.waitForVisible(locator).isEnabled();
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Verifies if a WebElement is enabled.
     *
     * @param element The WebElement
     */
    public boolean verifyElementEnabled(WebElement element) {
        try {
            return element.isEnabled();
        } catch (WebDriverException e) {
            return false;
        }
    }

    /**
     * Verifies if an element is disabled.
     *
     * @param locator The element locator
     */
    public boolean verifyElementDisabled(By locator) {
        try {
            return !WaitManager.waitForVisible(locator).isEnabled();
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Checks if an element is present in the DOM.
     *
     * @param locator The element locator
     */
    public boolean isElementPresent(By locator) {
        return !DriverManager.getDriver().findElements(locator).isEmpty();
    }

    /**
     * Gets the count of elements matching a locator.
     *
     * @param locator The element locator
     */
    public int getElementCount(By locator) {
        return DriverManager.getDriver().findElements(locator).size();
    }

    /**
     * Verifies if element count matches expected count.
     *
     * @param locator       The element locator
     * @param expectedCount The expected element count
     */
    public boolean verifyElementCount(By locator, int expectedCount) {
        return getElementCount(locator) == expectedCount;
    }

    /**
     * Gets the visible text of an element.
     *
     * @param locator The element locator
     */
   public String getText(By locator) {

            return executeAndObserve(
                    "getText",
                    locator.toString(),
                    () -> {

                        WebElement element = WaitManager.waitForVisible(locator);

                        String text = (String) executeJS(
                                "return arguments[0].innerText || " +
                                        "arguments[0].textContent || '';",
                                element
                        );

                        return text != null ? text.trim() : "";
                    }
            );
        }

    /**
     * Gets the text of a WebElement.
     *
     * @param element The WebElement
     */
    public String getText(WebElement element) {
        Objects.requireNonNull(element, "WebElement cannot be null");
        return element.getText().trim();
    }

    /**
     * Gets the value of an attribute from an element.
     *
     * @param locator   The element locator
     * @param attribute The attribute name
     */
    public String getAttribute(By locator, String attribute) {
        Objects.requireNonNull(attribute, "Attribute name cannot be null");
        return executeAndObserve("getAttribute", locator + ":" + attribute,
                () -> WaitManager.waitForPresence(locator).getAttribute(attribute));
    }

    /**
     * Gets the value of an attribute from a WebElement.
     *
     * @param element   The WebElement
     * @param attribute The attribute name
     */
    public String getAttribute(WebElement element, String attribute) {
        Objects.requireNonNull(element, "WebElement cannot be null");
        Objects.requireNonNull(attribute, "Attribute name cannot be null");
        return element.getAttribute(attribute);
    }

    /**
     * Gets the CSS value of an element.
     *
     * @param locator      The element locator
     * @param propertyName The CSS property name
     */
    public String getCSSValue(By locator, String propertyName) {
        Objects.requireNonNull(propertyName, "Property name cannot be null");
        return executeAndObserve("getCSSValue", locator + ":" + propertyName,
                () -> WaitManager.waitForPresence(locator).getCssValue(propertyName));
    }

    /**
     * Gets the tag name of an element.
     *
     * @param locator The element locator
     */
    public String getTagName(By locator) {
        return executeAndObserve("getTagName", locator.toString(),
                () -> WaitManager.waitForPresence(locator).getTagName());
    }

    /**
     * Gets the size (width and height) of an element.
     *
     * @param locator The element locator
     */
    public Dimension getElementSize(By locator) {
        return executeAndObserve("getElementSize", locator.toString(),
                () -> WaitManager.waitForPresence(locator).getSize());
    }

    /**
     * Gets the location of an element.
     *
     * @param locator The element locator
     */
    public Point getElementLocation(By locator) {
        return executeAndObserve("getElementLocation", locator.toString(),
                () -> WaitManager.waitForPresence(locator).getLocation());
    }

    public void scrollForElementWithScreen(By locator) throws InterruptedException {

        try {
            List<WebElement> elements = findElements(locator);
            if (!elements.isEmpty() && elements.get(0).isDisplayed()) {
                executeJS("arguments[0].scrollIntoView({block: 'center'});", elements.get(0));
                return;
            }
        } catch (StaleElementReferenceException ignored) {
            // If stale, proceed to the main loop to re-find and scroll.
        }


        executeJS("window.scrollTo(0, 0);");
        Thread.sleep(100);

        long lastScrollPosition = -1;

        while (true) {
            try {
                List<WebElement> elements = findElements(locator);

                if (!elements.isEmpty()) {
                    WebElement ele = elements.get(0);
                    if (ele.isDisplayed()) {
                        executeJS("arguments[0].scrollIntoView({block: 'center'});", ele);
                        return;
                    }
                }
            } catch (StaleElementReferenceException ignored) {
                // Retry on the next iteration
            }

            // 2. Get the current scroll position before scrolling
            long currentScrollPosition = (long) executeJS("return window.pageYOffset;");
            // 3. Check for End of Page (BREAK CONDITION)
            if (currentScrollPosition == lastScrollPosition) {
                throw new NoSuchElementException("Element located by " + locator.toString() +
                        " was not found after scrolling to the end of the page.");
            }
            executeJS("window.scrollBy(0,200);");
            Thread.sleep(100);

            lastScrollPosition = currentScrollPosition;
        }

    }

    /**
     * Verifies if text of an element matches expected text.
     *
     * @param locator      The element locator
     * @param expectedText The expected text
     */
    public void verifyText(By locator, String expectedText) {

        String actualText = getText(locator);

        if (!actualText.trim().equals(expectedText.trim())) {

            throw new AssertionError(
                    "Text mismatch." +
                            " Expected: [" + expectedText + "]" +
                            " Actual: [" + actualText + "]"
            );
        }
    }

    /**
     * Verifies if text of an element matches (case-insensitive).
     *
     * @param locator      The element locator
     * @param expectedText The expected text
     */
    public boolean verifyTextIgnoreCase(By locator, String expectedText) {
        try {
            String actualText = getText(locator);
            return actualText.equalsIgnoreCase(expectedText);
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Verifies if text of an element contains a substring.
     *
     * @param locator The element locator
     * @param subtext The substring to verify
     */
    public boolean verifyTextContains(By locator, String subtext) {
        try {
            String actualText = getText(locator);
            return actualText.contains(subtext);
        } catch (Exception e) {
            return false;
        }
    }

    // ──────────────────────────────────────────────────────────────────────────
    // FILE & SCREENSHOT OPERATIONS
    // ──────────────────────────────────────────────────────────────────────────

    /**
     * Uploads a file using a file input element.
     *
     * @param locator  The file input element locator
     * @param filePath The path to the file to upload
     */
    public void uploadFile(By locator, String filePath) {
        Objects.requireNonNull(filePath, "File path cannot be null");
        sendKeys(locator, filePath);
    }

    /**
     * Captures a screenshot as bytes.
     *
     * @return The screenshot bytes
     */
    public byte[] captureScreenshotAsBytes() {
        return executeAndObserve("captureScreenshot", "bytes",
                () -> ((TakesScreenshot) DriverManager.getDriver())
                        .getScreenshotAs(OutputType.BYTES));
    }

    /**
     * Captures a screenshot as Base64 string.
     *
     * @return The screenshot as Base64 string
     */
    public String captureScreenshotAsBase64() {
        return executeAndObserve("captureScreenshot", "base64",
                () -> ((TakesScreenshot) DriverManager.getDriver())
                        .getScreenshotAs(OutputType.BASE64));
    }


    /**
     * Executes JavaScript code.
     *
     * @param script The JavaScript code to execute
     * @param args   The arguments to pass to the script
     */
    public Object executeJS(String script, Object... args) {
        Objects.requireNonNull(script, "Script cannot be null");
        return ((JavascriptExecutor) DriverManager.getDriver()).executeScript(script, args);
    }

    /**
     * Executes asynchronous JavaScript code.
     *
     * @param script The JavaScript code to execute
     * @param args   The arguments to pass to the script
     */
    public Object executeAsyncJS(String script, Object... args) {
        Objects.requireNonNull(script, "Script cannot be null");
        return ((JavascriptExecutor) DriverManager.getDriver()).executeAsyncScript(script, args);
    }

    /**
     * Highlights an element visually on the page.
     *
     * @param locator The element locator
     */
    public void highlightElement(By locator) {
        executeObserved("highlightElement", locator.toString(), () -> {
            WebElement element = WaitManager.waitForPresence(locator);
            executeJS("arguments[0].style.border='3px solid yellow'; arguments[0].style.backgroundColor='yellow';",
                    element);
        });
    }

    /**
     * Removes highlighting from an element.
     *
     * @param locator The element locator
     */
    public void removeHighlight(By locator) {
        executeObserved("removeHighlight", locator.toString(), () -> {
            WebElement element = WaitManager.waitForPresence(locator);
            executeJS("arguments[0].style.border=''; arguments[0].style.backgroundColor='';", element);
        });
    }

    /**
     * Gets element's inner HTML.
     *
     * @param locator The element locator
     * @return The inner HTML
     */
    public String getElementInnerHTML(By locator) {
        return executeAndObserve("getElementInnerHTML", locator.toString(),
                () -> (String) executeJS("return arguments[0].innerHTML;",
                        WaitManager.waitForPresence(locator)));
    }

    /**
     * Gets element's outer HTML.
     *
     * @param locator The element locator
     * @return The outer HTML
     */
    public String getElementOuterHTML(By locator) {
        return executeAndObserve("getElementOuterHTML", locator.toString(),
                () -> (String) executeJS("return arguments[0].outerHTML;",
                        WaitManager.waitForPresence(locator)));
    }

    /**
     * Sets element's inner HTML.
     *
     * @param locator The element locator
     * @param html    The HTML to set
     */
    public void setElementInnerHTML(By locator, String html) {
        Objects.requireNonNull(html, "HTML cannot be null");
        executeObserved("setElementInnerHTML", locator.toString(), () ->
                executeJS("arguments[0].innerHTML = arguments[1];",
                        WaitManager.waitForPresence(locator), html));
    }

    /**
     * Navigates to a URL.
     *
     * @param url The URL to navigate to
     */
    public void navigateTo(String url) {
        Objects.requireNonNull(url, "URL cannot be null");
        executeObserved("navigateTo", url,
                () -> DriverManager.getDriver().navigate().to(url));
    }

    /**
     * Refreshes the current page.
     */
    public void refreshPage() {
        executeObserved("refreshPage", "F5",
                () -> DriverManager.getDriver().navigate().refresh());
        waitForPageLoad();
    }

    /**
     * Navigates back to the previous page.
     */
    public void navigateBack() {
        executeObserved("navigateBack", "<-",
                () -> DriverManager.getDriver().navigate().back());
    }

    /**
     * Navigates forward to the next page.
     */
    public void navigateForward() {
        executeObserved("navigateForward", "->",
                () -> DriverManager.getDriver().navigate().forward());
    }

    /**
     * Gets the current URL.
     *
     * @return The current page URL
     */
    public String getCurrentUrl() {
        return executeAndObserve("getCurrentUrl", "url",
                () -> DriverManager.getDriver().getCurrentUrl());
    }

    /**
     * Gets the current page title.
     *
     * @return The page title
     */
    public String getPageTitle() {
        return executeAndObserve("getPageTitle", "title",
                () -> DriverManager.getDriver().getTitle());
    }

    /**
     * Verifies if the current URL matches the expected URL.
     *
     * @param expectedUrl The expected URL
     * @return true if URL matches, false otherwise
     */
    public boolean verifyUrl(String expectedUrl) {
        try {
            String currentUrl = getCurrentUrl();
            return currentUrl.equals(expectedUrl) || currentUrl.contains(expectedUrl);
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Verifies if the page title matches the expected title.
     *
     * @param expectedTitle The expected page title
     * @return true if title matches, false otherwise
     */
    public boolean verifyPageTitle(String expectedTitle) {
        try {
            return getPageTitle().equals(expectedTitle);
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Deletes all cookies.
     */
    public void deleteAllCookies() {
        executeObserved("deleteAllCookies", "all",
                () -> DriverManager.getDriver().manage().deleteAllCookies());
    }

    /**
     * Clears browser cache and cookies.
     */
    public void clearBrowserCache() {
        deleteAllCookies();
        LOGGER.info("Browser cache and cookies cleared");
    }

    /**
     * Gets the page source HTML.
     *
     * @return The page source
     */
    public String getPageSource() {
        return executeAndObserve("getPageSource", "html",
                () -> DriverManager.getDriver().getPageSource());
    }

    /**
     * Waits for page to fully load.
     */
    public void waitForPageLoad() {
        WaitManager.waitForPageLoad();
    }



    /**
     * Pauses execution for specified duration.
     *
     * @param milliseconds The duration in milliseconds
     */
    public void pauseExecution(long milliseconds) {
        WaitManager.pause(java.time.Duration.ofMillis(milliseconds), "SeleniumCommands fallback pause");
    }

    /**
     * Finds elements by locator.
     *
     * @param locator The element locator
     * @return List of WebElements
     */
    public List<WebElement> findElements(By locator) {
        return DriverManager.getDriver().findElements(locator);
    }

    /**
     * Finds element by locator (single).
     *
     * @param locator The element locator
     * @return The WebElement, or null if not found
     */
    public WebElement findElement(By locator) {
        try {
            return DriverManager.getDriver().findElement(locator);
        } catch (NoSuchElementException e) {
            return null;
        }
    }

    /**
     * Checks if a checkbox is checked.
     *
     * @param locator The checkbox locator
     * @return true if checked, false otherwise
     */
    public boolean isCheckboxChecked(By locator) {
        try {
            return WaitManager.waitForPresence(locator).isSelected();
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Gets the device pixel ratio.
     *
     * @return The DPR value
     */
    public double getDevicePixelRatio() {
        Object dpr = executeJS("return window.devicePixelRatio || 1;");
        return dpr instanceof Number ? ((Number) dpr).doubleValue() : 1.0d;
    }

    /**
     * Gets viewport width.
     *
     * @return The viewport width in pixels
     */
    public int getViewportWidth() {
        Object width = executeJS("return window.innerWidth;");
        return width instanceof Number ? ((Number) width).intValue() : 0;
    }

    /**
     * Gets viewport height.
     *
     * @return The viewport height in pixels
     */
    public int getViewportHeight() {
        Object height = executeJS("return window.innerHeight;");
        return height instanceof Number ? ((Number) height).intValue() : 0;
    }


    /**
     * Wraps element interactions with observability logging.
     *
     * @param action   The action name
     * @param target   The target element/locator
     * @param runnable The operation to execute
     */
    private void executeObserved(String action, String target, Runnable runnable) {
        long startTime = System.currentTimeMillis();
        try {
            runnable.run();
            long duration = System.currentTimeMillis() - startTime;
            TestObservabilityEngine.recordStep(action + ":" + target, duration, true);
            LOGGER.debug("Action completed - {} on {}", action, target);
        } catch (RuntimeException e) {
            long duration = System.currentTimeMillis() - startTime;
            TestObservabilityEngine.recordStep(action + ":" + target, duration, false);
            LOGGER.error("Action failed - {} on {}: {}", action, target, e.getMessage());
            throw e;
        }
    }

    /**
     * Wraps element value retrieval with observability logging.
     *
     * @param action   The action name
     * @param target   The target element/locator
     * @param supplier The operation to execute
     * @return The result value
     */
    private <T> T executeAndObserve(String action, String target, java.util.function.Supplier<T> supplier) {
        long startTime = System.currentTimeMillis();
        try {
            T result = supplier.get();
            long duration = System.currentTimeMillis() - startTime;
            TestObservabilityEngine.recordStep(action + ":" + target, duration, true);
            return result;
        } catch (RuntimeException e) {
            long duration = System.currentTimeMillis() - startTime;
            TestObservabilityEngine.recordStep(action + ":" + target, duration, false);
            LOGGER.error("Action failed - {} on {}: {}", action, target, e.getMessage());
            throw e;
        }
    }

    /**
     * Gets a descriptive string for a WebElement.
     *
     * @param element The WebElement
     * @return A description string
     */
    private String describe(WebElement element) {
        try {
            String tag = element.getTagName();
            String text = element.getText();
            if (text != null && !text.isBlank()) {
                return tag + ":" + text.substring(0, Math.min(20, text.length()));
            }
            String id = element.getAttribute("id");
            if (id != null && !id.isBlank()) {
                return tag + "#" + id;
            }
            String name = element.getAttribute("name");
            if (name != null && !name.isBlank()) {
                return tag + "[name=" + name + "]";
            }
            return tag;
        } catch (WebDriverException e) {
            return "WebElement";
        }
    }
}
