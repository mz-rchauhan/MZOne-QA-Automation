package core.sf;


import core.base.*;

import org.openqa.selenium.*;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.slf4j.Logger;
import org.testng.Assert;
import pages.actionsHelper.GenericActionsHelper;


import java.time.Duration;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SalesforceCommands extends SeleniumCommands {

    private static final Logger LOGGER = LoggerManager.getLogger(SalesforceCommands.class);

    // ── Navigation ────────────────────────────────────────────────────────────


    /**
     * Navigates to a Salesforce record by its 15/18-char record ID.
     * Derives base URL from current browser session.
     */
    public void openRecord(String recordId) throws Exception {
        String baseUrl = extractSalesforceBaseUrl();
        String recordUrl = baseUrl + recordId;
        LOGGER.info("Opening record: {}", recordUrl);
        navigateTo(recordUrl);

        if (!waitForUrlToContain(recordId.substring(0, 15), 15)) {
            throw new RuntimeException("URL did not update to record ID: " + recordId);
        }
        SalesforceWaitManager.waitForSalesforceSpinner();
        SalesforceWaitManager.waitForRecordPageLoad();
        LOGGER.info("Landed on record: {}", recordId);
    }

    /**
     * Navigates to Salesforce Setup home.
     */
    public void navigateToSetup() throws Exception {
        String setupUrl = extractSalesforceBaseUrl() + "lightning/setup/SetupOneHome/home";
        LOGGER.info("Navigating to Setup: {}", setupUrl);
        navigateTo(setupUrl);

        if (!waitForUrlToContain("SetupOneHome", 15)) {
            throw new RuntimeException("Did not land on Setup page. Current: " + getCurrentUrl());
        }
        SalesforceWaitManager.waitForSalesforceSpinner();
        SalesforceWaitManager.waitForRecordPageLoad();
        WaitManager.waitForVisible(By.xpath("//div[contains(@class,'appName')]//span[text()='Setup']"));
        LOGGER.info("Landed on Setup page.");
    }

    // ── App Launcher ──────────────────────────────────────────────────────────

    public void selectAppFromAppLauncher(String appName) throws Exception {
        searchFromAppLauncher(appName);
        try {
            clickWithJavaScript(GenericActionsHelper.appLinkAppLauncher(appName));
        } catch (Exception e) {
            LOGGER.warn("App '{}' not found or user lacks permission.", appName);
        }
    }

    public void searchFromAppLauncher(String appName) throws Exception {
        WaitManager.waitForVisible(GenericActionsHelper.APP_LAUNCHER_ICON_HOME_PAGE);
        safeClick(GenericActionsHelper.APP_LAUNCHER_ICON_HOME_PAGE);
        WaitManager.waitForVisible(GenericActionsHelper.VIEW_ALL_BUTTON);
        click(GenericActionsHelper.VIEW_ALL_BUTTON);
        WaitManager.waitForVisible(GenericActionsHelper.SEARCH_APPS_OR_ITEMS_BOX);
        enterText(GenericActionsHelper.SEARCH_APPS_OR_ITEMS_BOX, appName);
    }

    // ── Setup Navigation ──────────────────────────────────────────────────────

    /**
     * Types in Quick Find and opens the setup sidebar item.
     */
    public void openSetupMenuItemFromQuickFind(String menuItemName) {
        try {
            LOGGER.info("Quick Find: '{}'", menuItemName);
            By quickFindInput = By.xpath("//input[@placeholder='Quick Find']");
            WebElement searchBox = WaitManager.waitForVisible(quickFindInput);
            searchBox.clear();
            enterText(searchBox, menuItemName);

            By sidebarItem = By.xpath(
                    "//div[@title=\"" + menuItemName + "\"]//a//*[text()=\"" + menuItemName + "\"]/ancestor-or-self::a");
            safeClick(sidebarItem);
            WaitManager.waitForVisible(By.xpath("//h1//span[text()=\"" + menuItemName + "\"]"));
            LOGGER.info("Opened Setup menu item: '{}'", menuItemName);
        } catch (Exception e) {
            LOGGER.error("Failed to open Setup menu item '{}': {}", menuItemName, e.getMessage());
            throw new RuntimeException("Could not open Setup menu item: " + menuItemName, e);
        }
    }

    /**
     * Searches Setup Quick Find, opens a specific record from the resulting list.
     */
    public void searchAndOpenSetupRecord(String setupMenuName, String recordName) {
        try {
            By quickFindInput = By.xpath(
                    "//input[@placeholder='Search Setup' or contains(@class,'setupSearchInput')]");
            WebElement searchBox = WaitManager.waitForClickable(quickFindInput);
            click(searchBox);
            sendKeys(quickFindInput, recordName);
            safeClick(By.xpath("//li[contains(@class,'lookup__item SEARCH_OPTION')]"));

            By sidebarItem = By.xpath(
                    "//a[@title=\"" + setupMenuName + "\"]//*[text()='" + setupMenuName + "']/ancestor-or-self::a");
            WaitManager.waitForClickable(sidebarItem);
            clickWithJavaScript(sidebarItem);

            By recordLocator = By.xpath(
                    "//a[text()='" + recordName + "' or contains(@title,'" + recordName + "')]");
            WaitManager.waitForVisible(recordLocator);
            scrollUntilElementVisible(recordLocator);
            click(recordLocator);

            switchToFrame(By.xpath("//iframe[contains(@title,'Salesforce - Enterprise Edition')]"));
            WaitManager.waitForVisible(By.xpath("//h2[contains(text(),\"" + recordName + "\")]"));
            LOGGER.info("Opened Setup record '{}' under '{}'.", recordName, setupMenuName);
            switchToDefaultContent();
        } catch (Exception e) {
            LOGGER.error("Failed to open Setup record '{}': {}", recordName, e.getMessage());
            throw new RuntimeException(e);
        }
    }

    /**
     * Clicks a Setup action button (Login, Edit, Reset Password) inside the classic iframe.
     */
    public void clickSetupActionButtonOnDetailsPage(String buttonName) {
        try {
            LOGGER.info("Clicking Setup action button: '{}'", buttonName);
            switchToFrame(By.xpath("//iframe[contains(@title,'Salesforce - Enterprise Edition')]"));
            By buttonLocator = By.xpath(
                    "//input[(@type='button' or @type='submit') and " +
                            "(@title='" + buttonName + "' or normalize-space(@value)='" + buttonName + "')]");
            WaitManager.waitForClickable(buttonLocator);
            click(buttonLocator);
            switchToDefaultContent();
            LOGGER.info("Clicked Setup button: '{}'", buttonName);
        } catch (Exception e) {
            LOGGER.error("Failed to click Setup button '{}': {}", buttonName, e.getMessage());
            throw new RuntimeException("Could not click Setup button: " + buttonName, e);
        }
    }

    /**
     * Opens a record from a Setup iframe table (e.g. Experience Cloud sites list).
     */
    public void openRecordFromSetupTable(String recordName) {
        try {
            By iframeLocator = By.xpath("//iframe[contains(@name,'vfFrameId')]");
            switchToFrame(iframeLocator);
            By recordLink = By.xpath("//table//a[normalize-space(text())='" + recordName + "']");
            click(WaitManager.waitForVisible(recordLink));
            switchToDefaultContent();
            WaitManager.waitForPageLoad();
            LOGGER.info("Opened Setup table record: '{}'", recordName);
        } catch (Exception e) {
            LOGGER.error("Failed to open Setup table record '{}': {}", recordName, e.getMessage());
            throw new RuntimeException("Could not open record: " + recordName, e);
        } finally {
            switchToDefaultContent();
        }
    }

    // ── Global Search ─────────────────────────────────────────────────────────

    /**
     * Searches and opens a Salesforce record via global search.
     */
    public void searchRecord(String objName, String recordName) {
        try {
            LOGGER.info("Global search: object='{}' record='{}'", objName, recordName);
            WaitManager.waitForClickable(GenericActionsHelper.GLOBAL_SEARCH_BOX);
            WaitManager.waitForClickable(By.xpath("//span[contains(@aria-label,\"Global Header\")]"));
            safeClick(GenericActionsHelper.GLOBAL_SEARCH_BOX);

            WebElement searchByObjField = WaitManager.waitForVisible(
                    By.xpath("//label[text()='Search by object type']//following::input[@data-value='Search: All']"));
            safeClick(searchByObjField);
            clickWithJavaScript(
                    By.xpath("//ul[@aria-label='Suggested For You']//span[text()='" + objName + "']"));

            WebElement globalSearchField = WaitManager.waitForVisible(
                    By.xpath("//div[@class='forceSearchAssistantDialog']//input[@placeholder='Search...']"));
            enterText(globalSearchField, recordName);
            globalSearchField.sendKeys(Keys.ENTER);

            By record = By.xpath("//a[@title=\"" + recordName + "\" and @data-refid]//parent::span");
            try {
                scrollUntilElementVisible(record);
                click(record);
            } catch (Exception e) {
                clickWithJavaScript(
                        By.xpath("//li[contains(@class,'forceSearchScopeItem')]//a[@title=\"" + objName + "\"]"));
                WaitManager.waitForVisible(record);
                click(record);
            }

            WaitManager.waitForVisible(
                    By.xpath("//h1//lightning-formatted-text[contains(text(),'" + recordName + "')]"));
            LOGGER.info("Opened '{}' of type '{}'.", recordName, objName);
        } catch (Exception e) {
            LOGGER.error("searchRecord failed: {}", e.getMessage());
            throw new RuntimeException(e);
        }
    }

    public void searchFromGlobalSearchBox(String searchFor) throws Exception {
        refreshPage();
        By path = By.xpath("//button[text()='Search...']");
        WaitManager.waitForVisible(path);
        clickWithJavaScript(path);
        WebElement input = WaitManager.waitForVisible(
                By.xpath("//input[contains(@placeholder,'Search...') and @type='search' and not(contains(@placeholder,'list'))]"));
        enterText(input, searchFor);
        new Actions(DriverManager.getDriver()).sendKeys(Keys.ENTER).perform();
        By result = By.xpath("(//a[@title='" + searchFor + "'])[last()]");
        try {
            scrollToElement(result);
            click(result);
        } catch (Exception e) {
            clickWithJavaScript(result);
        }
        WaitManager.waitForPageLoad();
    }

    // ── Table Actions ─────────────────────────────────────────────────────────

    public void openEditModalFromTable(String name) {
        try {
            LOGGER.info("Opening Edit modal for: '{}'", name);
            By actionMenuLocator = By.xpath(
                    "(//tr[contains(.,\"" + name + "\")])[last()]//button[contains(@class,'slds-button_icon-border')]");
            scrollUntilElementVisible(actionMenuLocator);
            clickWithJavaScript(WaitManager.waitForVisible(actionMenuLocator));

            By editOption = By.xpath(
                    "(//a[@role='menuitem' and (@title='Edit' or .//span[text()='Edit'])])[last()]");
            click(editOption);
            WaitManager.waitForVisible(By.xpath("(//h2[contains(text(),'Edit')])[last()]"));
            LOGGER.info("Edit modal opened for '{}'.", name);
        } catch (Exception e) {
            LOGGER.error("Failed to open Edit modal for '{}': {}", name, e.getMessage());
            throw new RuntimeException("Could not open Edit Modal for " + name, e);
        }
    }

    public String getTableValueByLabel(String rowBase, String label) {
        By path = By.xpath(rowBase + "//td[@data-label=\"" + label + "\"]//*[text()]");
        return getText(path).trim();
    }

    public boolean isCheckboxMarkedInTable(String rowBase, String label, boolean expectedState) {
        String stateStr = expectedState ? "True" : "False";
        By iconPath = By.xpath(
                rowBase + "//td[@data-label=\"" + label + "\"]//*[@title=\"" + stateStr + "\" or @alt=\"" + stateStr + "\"]");
        try {
            return verifyElementDisplayed(iconPath);
        } catch (Exception e) {
            return false;
        }
    }

    public String getValueFromTable(String rowIdentifier, String columnLabel) {
        String xpath = "//tr[contains(.,\"" + rowIdentifier + "\")]//td[@data-label=\"" + columnLabel + "\"]";
        try {
            scrollUntilElementVisible(By.xpath(xpath));
            String value = WaitManager.waitForVisible(By.xpath(xpath)).getText().trim();
            LOGGER.info("Table value → Row:'{}' Col:'{}' = '{}'", rowIdentifier, columnLabel, value);
            return value;
        } catch (Exception e) {
            throw new RuntimeException("Table element not found: Row[" + rowIdentifier + "] Col[" + columnLabel + "]");
        }
    }

    public void validateListViewRowCount(int expectedCount) {
        By tableRows = By.xpath(
                "//div[contains(@class,'listViewContent ')]//table[contains(@class,'slds-table')]//tbody//tr");
        try {
            List<WebElement> rows = WaitManager.waitForAllVisible(tableRows);
            int actualCount = rows.size();
            LOGGER.info("Row count — expected: {} | actual: {}", expectedCount, actualCount);
            Assert.assertEquals(actualCount, expectedCount,
                    "Row count mismatch in listViewContent table!");
        } catch (Exception e) {
            LOGGER.error("Failed to count rows: {}", e.getMessage());
            throw e;
        }
    }

    // ── Lightning Field / Modal Actions ───────────────────────────────────────

    public void selectPicklistValueInModal(String labelText, String valueToSelect) {
        try {
            LOGGER.info("Picklist '{}' → '{}'", labelText, valueToSelect);
            WebElement trigger = WaitManager.waitForVisible(
                    By.xpath("//lightning-picklist[.//label[text()='" + labelText + "']]//button"));
            safeClick(trigger);

            By option = By.xpath(
                    "//lightning-base-combobox-item[@data-value='" + valueToSelect + "'] | " +
                            "//lightning-base-combobox-item//span[@title='" + valueToSelect + "']");
            try {
                click(option);
            } catch (Exception e) {
                clickWithJavaScript(trigger);
                clickWithJavaScript(option);
            }
        } catch (Exception e) {
            LOGGER.error("Picklist selection failed for '{}': {}", valueToSelect, e.getMessage());
            throw new RuntimeException("Picklist selection failed", e);
        }
    }

    public String getPicklistValueInModal(String labelText) {
        String xpath = "//label[text()='" + labelText + "']/following-sibling::div//button//span | " +
                "//lightning-picklist[.//label[text()='" + labelText + "']]//button/span";
        try {
            String value = getText(By.xpath(xpath)).trim();
            LOGGER.info("Picklist '{}' current value: '{}'", labelText, value);
            return value;
        } catch (Exception e) {
            LOGGER.warn("Could not read picklist '{}': {}", labelText, e.getMessage());
            return "";
        }
    }

    public void editDateValueInModal(String labelText, String dateValue) {
        try {
            By dateInputPath = By.xpath(
                    "//lightning-input[.//label[text()='" + labelText + "']]//input");
            scrollUntilElementVisible(dateInputPath);
            WebElement dateInput = WaitManager.waitForVisible(dateInputPath);
            click(dateInput);
            dateInput.sendKeys(Keys.chord(Keys.CONTROL, "a"), Keys.BACK_SPACE);
            enterText(dateInput, dateValue);
            dateInput.sendKeys(Keys.TAB);
            LOGGER.info("Date field '{}' set to '{}'", labelText, dateValue);
        } catch (Exception e) {
            throw new RuntimeException("Date field edit failed for: " + labelText, e);
        }
    }

    public boolean isCheckboxCheckedInModal(String labelText) {
        try {
            By xpath = By.xpath("//lightning-input[.//span[text()='" + labelText + "']]//input");
            List<WebElement> elements = WaitManager.waitForAllVisible(xpath);
            if (!elements.isEmpty()) {
                return (Boolean) ((JavascriptExecutor) DriverManager.getDriver())
                        .executeScript("return arguments[0].checked;", elements.get(0));
            }
            return false;
        } catch (Exception e) {
            return false;
        }
    }

    public void setCheckboxInModal(String labelText, boolean targetState) {
        boolean currentState = isCheckboxCheckedInModal(labelText);
        if (currentState != targetState) {
            By checkboxLabel = By.xpath(
                    "//lightning-input[.//span[text()='" + labelText + "']]//label");
            WebElement element = WaitManager.waitForVisible(checkboxLabel);
            clickWithJavaScript(element);
            LOGGER.info("Checkbox '{}' toggled to: {}", labelText, targetState);
        } else {
            LOGGER.info("Checkbox '{}' already in state: {}", labelText, targetState);
        }
    }

    public String getFieldValueFromLightningPage(String fieldLabel) {
        By labelLocator = By.xpath(
                "//div[contains(@class,'slds-form-element')]//span[text()='" + fieldLabel + "'] | " +
                        "//label[text()='" + fieldLabel + "']");
        By valueXpath;
        try {
            if (fieldLabel.equalsIgnoreCase("Status")) {
                valueXpath = By.xpath(
                        "//*[normalize-space(text())=\"" + fieldLabel + "\"]/parent::div" +
                                "/following-sibling::div[1]//span//lightning-formatted-text");
                scrollUntilElementVisible(valueXpath);
            } else {
                valueXpath = By.xpath(
                        "//span[text()='" + fieldLabel + "']/parent::div//following-sibling::div" +
                                "//*[@data-output-element-id='output-field'] | " +
                                "//span[text()='" + fieldLabel + "']/parent::div//following-sibling::div//*[text()]");
                scrollUntilElementVisible(labelLocator);
            }

            if (!isElementPresent(valueXpath)) return "";

            String value = getText(valueXpath).trim();
            if (value.equals("—") || value.equals("-") || value.isEmpty()) return "";

            LOGGER.info("Field '{}' = '{}'", fieldLabel, value);
            return value;
        } catch (Exception e) {
            LOGGER.warn("Could not read field '{}': {}", fieldLabel, e.getMessage());
            return "";
        }
    }

    // ── Path / Status ─────────────────────────────────────────────────────────

    public String performStatusChangeFlow(String targetStatus) throws Exception {
        LOGGER.info("Path status change to: '{}'", targetStatus);
        By stageBy = By.xpath(
                "//li[contains(@class,'slds-path__item')]//span[text()='" + targetStatus + "'] | " +
                        "//a[@slot='link' and @title='" + targetStatus + "']");
        WebElement stageElement;
        try {
            stageElement = WaitManager.waitForClickable(stageBy);
        } catch (Exception e) {
            scrollUntilElementVisible(stageBy);
            stageElement = WaitManager.waitForClickable(stageBy);
        }
        click(stageElement);

        By actionBtn = By.xpath(
                "//button[contains(@class,'slds-path__mark-complete') or contains(@class,'pathButton')]");
        click(WaitManager.waitForVisible(actionBtn));

        By activeStatus = By.xpath(
                "//li[contains(@class,'slds-is-active')]//span[contains(@class,'slds-path__title')]");
        return getText(activeStatus);
    }

    public void verifyCurrentPathStatus(String expectedStatus) {

        By activePathXpath = By.xpath(
                "(//a[@aria-selected='true']" +
                        "//span[contains(@class,'slds-path__title') " +
                        "and normalize-space()='" + expectedStatus + "'" +
                        " and not(contains(@class,'slds-hide'))]" +
                        ")[last()]"
        );
        try {
            waitForPageLoad();
            WebElement activeSegment = WaitManager.waitForVisible(activePathXpath);

            Assert.assertTrue(
                    activeSegment.isDisplayed(),
                    "Active path status not displayed: " + expectedStatus
            );

            LOGGER.info("Path status verified successfully: '{}'", expectedStatus);

        } catch (Exception e) {

            throw new AssertionError(
                    "Path UI does not reflect expected status: " + expectedStatus,
                    e
            );
        }
    }
    // ── Related List ──────────────────────────────────────────────────────────

    public void openRelatedList(String relatedListLabel) {
        try {
            By viewAllLink = By.xpath(
                    "//article[.//span[@title='" + relatedListLabel + "']]//span[text()='View All']");
            By headerLink = By.xpath(
                    "//article[.//span[@title='" + relatedListLabel + "']]//h2//a | " +
                            "//article[.//span[text()='" + relatedListLabel + "']]//h2//a");

            if (!WaitManager.waitForAllPresent(viewAllLink).isEmpty()) {
                clickWithJavaScript(viewAllLink);
            } else if (!WaitManager.waitForAllPresent(headerLink).isEmpty()) {
                clickWithJavaScript(headerLink);
            } else {
                throw new RuntimeException("No link found for related list: " + relatedListLabel);
            }
            WaitManager.waitForVisible(By.xpath("//h1[contains(text(),'" + relatedListLabel + "')]"));
        } catch (Exception e) {
            LOGGER.error("Failed to open related list '{}': {}", relatedListLabel, e.getMessage());
            throw e;
        }
    }

    // ── Change Owner ──────────────────────────────────────────────────────────

    public void changeOwnerSuccessfully(String name) {
        try {
            performChangeOwnerAction(name);
            WebElement toast = WaitManager.waitForVisible(
                    By.xpath("//div[@data-key='success']//*[contains(@class,'toastMessage')]"));
            Assert.assertTrue(getText(toast).contains("owns the record"),
                    "Owner change toast mismatch: " + getText(toast));
            click(GenericActionsHelper.closeBtnOnToast("success"));
            LOGGER.info("Owner change to '{}' verified.", name);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public void verifyChangeOwnerFails(String newOwnerName) {
        try {
            performChangeOwnerAction(newOwnerName);
            By errorLocator = By.xpath(
                    "//span[contains(@class,'changeOwnerErrorMessage') and contains(.,'permission to take that action')]");
            String actualError = getText(WaitManager.waitForVisible(errorLocator));
            Assert.assertTrue(actualError.contains("permission to take that action"),
                    "Error message mismatch: " + actualError);
            LOGGER.info("Negative test passed. Error: {}", actualError);
        } catch (Exception e) {
            throw new RuntimeException("Expected owner change to fail but it did not.", e);
        }
    }

    private void performChangeOwnerAction(String newOwnerName) throws Exception {
        By trigger = By.xpath("//button[@title='Change Owner' or @name='changeOwner']");
        safeClick(trigger);
        WaitManager.waitForVisible(By.xpath("//h1[text()='Change Owner'] | //h2[text()='Change Owner']"));

        WebElement lookupInput = WaitManager.waitForVisible(
                By.xpath("//input[contains(@placeholder,'Search Users')]"));
        lookupInput.clear();
        // Send full name at once — wait for dropdown instead of Thread.sleep per char
        enterText(lookupInput, newOwnerName);

        By resultLocator = By.xpath(
                "//div[@role='listbox']//*[@title='" + newOwnerName + "'] | " +
                        "//div[@role='listbox']//lightning-base-combobox-item[contains(.,'" + newOwnerName + "')]");
        click(WaitManager.waitForVisible(resultLocator));
        click(By.xpath("//div[contains(@class,'modal-footer')]//button[text()='Change Owner']"));
    }

    // ── Internal Helpers ──────────────────────────────────────────────────────

    private String extractSalesforceBaseUrl() {
        String currentUrl = getCurrentUrl();
        String tag = "force.com/";
        if (currentUrl != null && currentUrl.contains(tag)) {
            return currentUrl.substring(0, currentUrl.indexOf(tag) + tag.length());
        }
        // Fallback to config — handles case where browser is not yet on SF domain
        return ConfigManager.getProperty("org.url", "");
    }

    private boolean waitForUrlToContain(String fraction, int timeoutSeconds) {
        try {
            WebDriverWait wait = new WebDriverWait(DriverManager.getDriver(), Duration.ofSeconds(timeoutSeconds));
            return wait.until(ExpectedConditions.urlContains(fraction));
        } catch (Exception e) {
            LOGGER.warn("Timeout waiting for URL to contain: {}", fraction);
            return false;
        }
    }
    /**
     * Verifies every field label in the list IS visible on the current page.
     * @param fieldLabels list of field label strings visible in the UI
     */
    public void assertFieldsVisible(List<String> fieldLabels) {
        LOGGER.info("assertFieldsVisible: checking {} fields", fieldLabels.size());
        List<String> failures = new ArrayList<>();

        for (String field : fieldLabels) {
            try {
                boolean visible = isFieldLabelOnPage(field);
                if (visible) {
                    LOGGER.info("[PASS] Visible: '{}'", field);
                } else {
                    LOGGER.error("[FAIL] NOT visible: '{}'", field);
                    failures.add(field);
                }
            } catch (Exception e) {
                LOGGER.error("[FAIL] Exception checking '{}': {}", field, e.getMessage());
                failures.add(field);
            }
        }

        if (!failures.isEmpty()) {
            throw new AssertionError(
                    "\nFields expected VISIBLE but NOT found on page ("
                            + failures.size() + "/" + fieldLabels.size() + "):\n  → "
                            + String.join("\n  → ", failures));
        }
        LOGGER.info("assertFieldsVisible: all {} passed.", fieldLabels.size());
    }

    /**
     * Verifies every field label in the list is NOT visible on the current page.
     * @param fieldLabels list of field label strings that should be hidden
     */
    public void assertFieldsNotVisible(List<String> fieldLabels) {
        LOGGER.info("assertFieldsNotVisible: checking {} fields", fieldLabels.size());
        List<String> failures = new ArrayList<>();

        for (String field : fieldLabels) {
            try {
                boolean visible = isFieldLabelOnPage(field);
                if (!visible) {
                    LOGGER.info("[PASS] Correctly hidden: '{}'", field);
                } else {
                    LOGGER.error("[FAIL] Should be hidden but IS visible: '{}'", field);
                    failures.add(field);
                }
            } catch (Exception e) {
                // Not in DOM = not visible = PASS for this method
                LOGGER.info("[PASS] Correctly hidden (not in DOM): '{}'", field);
            }
        }

        if (!failures.isEmpty()) {
            throw new AssertionError(
                    "\nFields expected HIDDEN but found VISIBLE on page ("
                            + failures.size() + "/" + fieldLabels.size() + "):\n  → "
                            + String.join("\n  → ", failures));
        }
        LOGGER.info("assertFieldsNotVisible: all {} passed.", fieldLabels.size());
    }

    /**
     * Keyword-driven field value validation on the current page.
     * @param rows DataTable rows or Excel rows as List<Map<String, String>>
     */
    public void assertFieldValues(List<Map<String, String>> rows) {
        LOGGER.info("assertFieldValues: processing {} rows", rows.size());
        List<String> failures = new ArrayList<>();

        for (Map<String, String> row : rows) {
            String field       = trimSafe(row.get("Field"));
            String action      = trimSafe(row.getOrDefault("Action", "")).toUpperCase();
            String expectedVal = trimSafe(row.getOrDefault("ExpectedValue", ""));
            String contextKey  = trimSafe(row.getOrDefault("ContextKey", ""));

            try {
                String result = processValueKeyword(field, action, expectedVal, contextKey);
                LOGGER.info("[PASS] {} '{}' → {}", action, field, result);
            } catch (AssertionError | Exception e) {
                LOGGER.error("[FAIL] {} '{}' → {}", action, field, e.getMessage());
                failures.add(String.format("%-25s | %-30s | %s", field, action, e.getMessage()));
            }
        }

        if (!failures.isEmpty()) {
            throw new AssertionError(
                    "\nField value failures (" + failures.size() + "/" + rows.size() + "):\n  → "
                            + String.join("\n  → ", failures));
        }
        LOGGER.info("assertFieldValues: all {} passed.", rows.size());
    }

    /**
     * Checks whether a field label is visible anywhere on the current Lightning page.
     * Covers detail view labels, form labels, and modal labels.
     */
    private boolean isFieldLabelOnPage(String fieldLabel) {
        By locator = By.xpath(
                "//span[normalize-space(text())='" + fieldLabel + "'] | " +
                        "//label[normalize-space(text())='" + fieldLabel + "'] | " +
                        "//div[contains(@class,'slds-form-element')]" +
                        "//*[normalize-space(text())='" + fieldLabel + "']"
        );
        List<WebElement> elements = findElements(locator);
        return !elements.isEmpty() && elements.stream().anyMatch(WebElement::isDisplayed);
    }

    /**
     * Keyword processor — executes one action for one field row.
     * Returns a description string on success, throws on failure.
     */
    private String processValueKeyword(String field, String action,
                                       String expectedVal, String contextKey) {
        switch (action) {

            case "ASSERT_VALUE": {
                String actual = getFieldValueFromLightningPage(field);
                if (!normalize(actual).equals(normalize(expectedVal))) {
                    throw new AssertionError(
                            "expected='" + expectedVal + "' actual='" + actual + "'");
                }
                return "'" + actual + "' matches";
            }

            case "ASSERT_VALUE_FROM_CONTEXT": {
                if (contextKey.isEmpty()) {
                    throw new AssertionError("ContextKey is empty");
                }
                String stored = TestContext.retrieveString(contextKey);
                if (stored == null) {
                    throw new AssertionError(
                            "No value in TestContext for key: '" + contextKey + "'");
                }
                String actual = getFieldValueFromLightningPage(field);
                if (!normalize(actual).equals(normalize(stored))) {
                    throw new AssertionError(
                            "context[" + contextKey + "]='" + stored
                                    + "' actual='" + actual + "'");
                }
                return "'" + actual + "' matches context[" + contextKey + "]";
            }

            case "ASSERT_BLANK": {
                String actual = getFieldValueFromLightningPage(field);
                if (actual != null && !actual.isBlank()) {
                    throw new AssertionError(
                            "expected BLANK but actual='" + actual + "'");
                }
                return "correctly blank";
            }

            case "ASSERT_NOT_BLANK": {
                String actual = getFieldValueFromLightningPage(field);
                if (actual == null || actual.isBlank()) {
                    throw new AssertionError("expected a value but field is blank");
                }
                return "has value='" + actual + "'";
            }

            case "STORE_VALUE": {
                if (contextKey.isEmpty()) {
                    throw new AssertionError("ContextKey is empty — cannot store value");
                }
                String actual = getFieldValueFromLightningPage(field);
                TestContext.store(contextKey, actual);
                return "stored in context[" + contextKey + "]='" + actual + "'";
            }

            default:
                throw new AssertionError("Unknown keyword: '" + action + "'");
        }
    }

    private String normalize(String val) {
        if (val == null) return "";
        return val.trim().replaceAll("\\s+", " ");
    }

    private String trimSafe(String val) {
        return val == null ? "" : val.trim();
    }
}

