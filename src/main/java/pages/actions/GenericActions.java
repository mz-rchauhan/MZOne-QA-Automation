package pages.actions;

import core.base.SeleniumCommands;
import core.base.TestContext;
import core.base.WaitManager;
import org.openqa.selenium.*;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.Color;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.Assert;

import pages.actionsHelper.GenericActionsHelper;
import utils.GenericJavaUtils;
import io.cucumber.datatable.DataTable;

import java.text.SimpleDateFormat;
import java.util.*;
import java.util.List;

import static utils.GenericJavaUtils.selectAnyRandomNumber;


public class GenericActions {
    private static final Logger LOGGER = LoggerFactory.getLogger(GenericActions.class);
    private final SeleniumCommands seleniumCommands;

    public GenericActions() {
        seleniumCommands = new SeleniumCommands();
    }


    public String calculateDate(String date) {
        String val1;
        String day;
        if (date.contains("-")) {
            val1 = date.split("-")[0];
            day = date.split("-")[1];
            int days = Integer.parseInt(day.split(" ")[1]);
            return GenericJavaUtils.getDate(Integer.parseInt("-" + days));
        } else {
            val1 = date.split("\\+")[0];
            day = date.split("\\+")[1];
            int days = Integer.parseInt(day.split(" ")[1]);
            String returnDate = GenericJavaUtils.getDate(Integer.parseInt("+" + days));
            System.out.println("Return Date = " + returnDate);
            return returnDate;
        }

    }


    public void fillMandatoryDetails(List<Map<String, String>> list) throws Exception {

        String dataType = "";
        String fieldLabel = "";
        String fieldName = "";
        String data = "";

        for (int i = 0; i < list.size(); i++) {
            dataType = list.get(i).get("Data Type");
            fieldLabel = list.get(i).get("Field Label");
            fieldName = list.get(i).get("Field Name");
            if (fieldName == null) {
                LOGGER.error("Found a NULL field name while filling mandatory details. Skipping...");
                continue; // or return
            }
            switch (dataType) {
                case "Text":
                    data = list.get(i).get("Value");
                    seleniumCommands.enterText(GenericActionsHelper.textFieldXpath(fieldName), data);
                    break;

                case "Random Text":
                    data = list.get(i).get("Value");
                    Random random = new Random();
                    int num = 10 + random.nextInt(999);
                    data = data + "" + num;
                    WebElement ele = WaitManager.waitForVisible(GenericActionsHelper.textFieldXpath(list.get(i).get("Field Name")));
                    ele.sendKeys(Keys.chord(Keys.CONTROL, "a", Keys.DELETE));
                    seleniumCommands.enterText(GenericActionsHelper.textFieldXpath(list.get(i).get("Field Name")), data);
                    seleniumCommands.pressTab(GenericActionsHelper.textFieldXpath(list.get(i).get("Field Name")));
                    break;
                case "Rich Text":
                    // 1. Get the initial label and value from the DataTable
                    data = list.get(i).get("Value");
                    String label = list.get(i).get("Field Name");
                    By richTextLocator = GenericActionsHelper.getRichTextXpath(label);
                    WebElement richTextElement = seleniumCommands.findElement(richTextLocator);
                    seleniumCommands.scrollToElement(richTextElement);
                    setRichText(richTextElement, data);
                    break;
                case "Random Value":
                    data = list.get(i).get("Value");
                    if (list.get(i).get("Field Name").equalsIgnoreCase("Email")) {
                        data = "test" + selectAnyRandomNumber(100, 999) + "@abc.com";
                    } else if (list.get(i).get("Field Name").equalsIgnoreCase("Zip Code")) {
                        data = String.valueOf(selectAnyRandomNumber(10000, 99999));
                    } else if (list.get(i).get("Field Name").equalsIgnoreCase("SSN") ||
                            list.get(i).get("Field Name").equalsIgnoreCase("Check Number/Transaction ID")) {
                        data = String.valueOf(selectAnyRandomNumber(100000000, 999999999));
                    } else if (list.get(i).get("Field Name").equalsIgnoreCase("Telephone Number") ||
                            list.get(i).get("Field Name").equalsIgnoreCase("Phone")) {
                        data = "1234" + selectAnyRandomNumber(100000, 999999);
                    }
                    seleniumCommands.enterText(GenericActionsHelper.textFieldXpath(list.get(i).get("Field Name")), data);
                    break;
                case "Number":
                    data = list.get(i).get("Value");
                    By numberLocator = GenericActionsHelper.textFieldXpath(fieldName);
                    WebElement numberField = WaitManager.waitForVisible(numberLocator);
                    seleniumCommands.scrollToElement(numberLocator);
                    Actions actions = new Actions(core.base.DriverManager.getDriver());
                    try {
                        seleniumCommands.click(numberLocator);
                    } catch (Exception e) {
                        seleniumCommands.click(numberLocator);
                    }

                    actions.moveToElement(numberField).click().perform();
                    for (char ch : data.toCharArray()) {
                        switch (ch) {
                            case '0':
                                actions.sendKeys(Keys.NUMPAD0).perform();
                                break;
                            case '1':
                                actions.sendKeys(Keys.NUMPAD1).perform();
                                break;
                            case '2':
                                actions.sendKeys(Keys.NUMPAD2).perform();
                                break;
                            case '3':
                                actions.sendKeys(Keys.NUMPAD3).perform();
                                break;
                            case '4':
                                actions.sendKeys(Keys.NUMPAD4).perform();
                                break;
                            case '5':
                                actions.sendKeys(Keys.NUMPAD5).perform();
                                break;
                            case '6':
                                actions.sendKeys(Keys.NUMPAD6).perform();
                                break;
                            case '7':
                                actions.sendKeys(Keys.NUMPAD7).perform();
                                break;
                            case '8':
                                actions.sendKeys(Keys.NUMPAD8).perform();
                                break;
                            case '9':
                                actions.sendKeys(Keys.NUMPAD9).perform();
                                break;
                            default:
                                actions.sendKeys(String.valueOf(ch)).perform();
                        }
                    }
                    // Trigger validation/auto-population
                    actions.sendKeys(Keys.TAB).perform();
                    break;
                case "Date":
                    data = list.get(i).get("Value");

                    seleniumCommands.scrollToElement(GenericActionsHelper.dateFieldXpathSpecific(list.get(i).get("Field Name")));
                    try {
                        seleniumCommands.click(GenericActionsHelper.dateFieldXpathSpecific(list.get(i).get("Field Name")));

                    } catch (Exception e) {
                        seleniumCommands.click(GenericActionsHelper.dateFieldXpathSpecific(list.get(i).get("Field Name")));

                    }
                    seleniumCommands.enterText(GenericActionsHelper.dateFieldXpathSpecific(list.get(i).get("Field Name")), data);
//
                    break;
                case "Date with Hyphen":
                    data = list.get(i).get("Value");
                    String date;
                    date = calculateDate(data);
                    data = date.replace("/", "-");
                    System.out.println(date);
                    seleniumCommands.click(GenericActionsHelper.dateField(list.get(i).get("Field Name")));
                    seleniumCommands.enterText(GenericActionsHelper.dateField(list.get(i).get("Field Name")), data);
                    //core.base.TestContext.store(coreLabel, data);
                    break;
                case "Date as Today":
                    data = list.get(i).get("Value");
                    String currentFieldName = list.get(i).get("Field Name");
                    data = calculateDate(data);

                    seleniumCommands.click(GenericActionsHelper.dateFieldXpathSpecific(currentFieldName));
                    core.base.WaitManager.waitForVisible(GenericActionsHelper.dateFieldXpathSpecific(list.get(i).get("Field Name")));
                    seleniumCommands.enterText(GenericActionsHelper.dateFieldXpathSpecific(list.get(i).get("Field Name")), data);
                    core.base.TestContext.store(fieldLabel, data);
                    break;
                case "Date SF":
                    data = list.get(i).get("Value");
                    data = calculateDate(data);
                    seleniumCommands.clickWithJavaScript(GenericActionsHelper.dateField(list.get(i).get("Field Name")));
                    seleniumCommands.enterText(GenericActionsHelper.dateField(list.get(i).get("Field Name")), data);
                    core.base.TestContext.store(fieldLabel, data);
                    break;
                case "multi-select picklist":
                    data = list.get(i).get("Value");
                    seleniumCommands.scrollToElement(By.xpath("//h1[text()='" + fieldName + "']//following::div//span[text()='" + data + "']"));
                    seleniumCommands.clickWithJavaScript(By.xpath("//h1[text()='" + fieldName + "']//following::div//span[text()='" + data + "']"));
                    break;
                case "Dropdown with scroll":
                    data = list.get(i).get("Value");
                    core.base.WaitManager.waitForVisible(GenericActionsHelper.FillDropdownXpath(list.get(i).get("Field Name")));
                    seleniumCommands.scrollToElement(GenericActionsHelper.FillDropdownXpath(list.get(i).get("Field Name")));
                    seleniumCommands.safeClick(GenericActionsHelper.FillDropdownXpath(list.get(i).get("Field Name")));
                    seleniumCommands.clickWithJavaScript(GenericActionsHelper.dropdownElement(data, list.get(i).get("Field Name")));
                    break;
                case "Dropdown without scroll":
                    data = list.get(i).get("Value");
                    core.base.WaitManager.waitForVisible(GenericActionsHelper.FillDropdownXpath(list.get(i).get("Field Name")));
                    try {
                        seleniumCommands.click(GenericActionsHelper.FillDropdownXpath(list.get(i).get("Field Name")));
                    } catch (Exception ex) {
                        seleniumCommands.clickWithJavaScript(GenericActionsHelper.FillDropdownXpath(list.get(i).get("Field Name")));
                    }

                    core.base.WaitManager.waitForVisible(GenericActionsHelper.dropdownElement(data, list.get(i).get("Field Name")));

                    try {
                        seleniumCommands.click(GenericActionsHelper.dropdownElement(data, list.get(i).get("Field Name")));
                    } catch (Exception e) {
                        seleniumCommands.clickWithJavaScript(GenericActionsHelper.dropdownElement(data, list.get(i).get("Field Name")));

                    }
                    break;
                case "Dropdown with tab":
                    data = list.get(i).get("Value");
                    core.base.WaitManager.waitForVisible(GenericActionsHelper.FillDropdownXpath(list.get(i).get("Field Name")));
                    seleniumCommands.scrollToElement(GenericActionsHelper.FillDropdownXpath(list.get(i).get("Field Name")));
                    seleniumCommands.clickWithJavaScript(GenericActionsHelper.FillDropdownXpath(list.get(i).get("Field Name")));
                    Actions act1 = new Actions(core.base.DriverManager.getDriver());
                    act1.sendKeys(Keys.ARROW_DOWN).sendKeys(Keys.ARROW_DOWN).perform();
                    act1.sendKeys(Keys.ENTER).perform();

                    break;
                case "Dropdown Actions":
                    data = list.get(i).get("Value");
                    Actions a = new Actions(core.base.DriverManager.getDriver());
                    core.base.WaitManager.waitForVisible(GenericActionsHelper.FillDropdownXpath(list.get(i).get("Field Name")));
                    a.moveToElement(core.base.DriverManager.getDriver().findElement(GenericActionsHelper.FillDropdownXpath(list.get(i).get("Field Name")))).click().build().perform();
                    core.base.WaitManager.waitForVisible(GenericActionsHelper.dropdownElement(data, list.get(i).get("Field Name")));
                    seleniumCommands.scrollToElement(GenericActionsHelper.dropdownElement(data, list.get(i).get("Field Name")));
                    a.moveToElement(core.base.DriverManager.getDriver().findElement(GenericActionsHelper.dropdownElement(data, list.get(i).get("Field Name")))).click().build().perform();
                    break;

                case "Dropdown Javascript":
                    data = list.get(i).get("Value");
                    By dropdownTrigger = GenericActionsHelper.FillDropdownXpath(fieldName);
                    By dropdownOption = GenericActionsHelper.dropdownElement(data, fieldName);

                    WaitManager.waitForVisible(dropdownTrigger);
                    seleniumCommands.scrollToElement(dropdownTrigger);
                    seleniumCommands.click(dropdownTrigger);

                    // Wait for the dynamic option to appear before clicking
                    WaitManager.waitForVisible(dropdownOption);
                    seleniumCommands.scrollToElement(dropdownOption);
                    seleniumCommands.safeClick(dropdownOption);

                    LOGGER.info("Successfully selected [{}] from dropdown [{}]", data, fieldName);
                    break;
                case "Dropdown Backend":
                    data = list.get(i).get("Value");
                    core.base.WaitManager.waitForVisible(GenericActionsHelper.FillDropdownXpathBackend(list.get(i).get("Field Name")));
                    seleniumCommands.scrollToElement(GenericActionsHelper.FillDropdownXpathBackend(list.get(i).get("Field Name")));
                    try {
                        seleniumCommands.click(GenericActionsHelper.FillDropdownXpathBackend(list.get(i).get("Field Name")));
                    } catch (Exception e) {
                        seleniumCommands.clickWithJavaScript(GenericActionsHelper.FillDropdownXpathBackend(list.get(i).get("Field Name")));
                    }
                    core.base.WaitManager.waitForVisible(GenericActionsHelper.dropdownElement(data, list.get(i).get("Field Name")));
                    seleniumCommands.scrollToElement(GenericActionsHelper.dropdownElement(data, list.get(i).get("Field Name")));
                    try {
                        seleniumCommands.click(GenericActionsHelper.dropdownElement(data, list.get(i).get("Field Name")));
                    } catch (Exception e) {
                        seleniumCommands.clickWithJavaScript(GenericActionsHelper.dropdownElement(data, list.get(i).get("Field Name")));
                    }
                    break;
                case "Textarea":
                    data = list.get(i).get("Value");
                    core.base.WaitManager.waitForVisible(GenericActionsHelper.textarea(fieldName));
                    seleniumCommands.clickWithJavaScript(GenericActionsHelper.textarea(fieldName));

                    seleniumCommands.enterText(GenericActionsHelper.textarea(fieldName), list.get(i).get("Value"));
                    seleniumCommands.pressTab(GenericActionsHelper.textarea(fieldName));
                    break;
                case "Text using placeholder":
                    data = list.get(i).get("Value");
                    core.base.WaitManager.waitForVisible(GenericActionsHelper.textUsingPlaceholder(list.get(i).get("Field Name")));
                    try {
                        seleniumCommands.clearText(GenericActionsHelper.textUsingPlaceholder(list.get(i).get("Field Name")));
                    } catch (Exception e) {
                        System.out.println("unable to clear field");
                    }
                    seleniumCommands.enterText(GenericActionsHelper.textUsingPlaceholder(list.get(i).get("Field Name")), data);
                    seleniumCommands.pressTab(GenericActionsHelper.textUsingPlaceholder(list.get(i).get("Field Name")));
                    break;
                case "Checkbox":
                    data = list.get(i).get("Value");
                    core.base.WaitManager.waitForVisible(GenericActionsHelper.checkBoxXpath(list.get(i).get("Field Name")));

                    try {
                        seleniumCommands.click(GenericActionsHelper.checkBoxXpath(list.get(i).get("Field Name")));
                    } catch (Exception e) {
                        seleniumCommands.clickWithJavaScript(GenericActionsHelper.checkBoxXpath(list.get(i).get("Field Name")));
                    }
                    break;
                case "Radiobutton":
                    data = list.get(i).get("Value");
                    core.base.WaitManager.waitForVisible(GenericActionsHelper.radioButton(list.get(i).get("Field Name"), data));
                    seleniumCommands.clickWithJavaScript(GenericActionsHelper.radioButton(list.get(i).get("Field Name"), data));
                    break;
                case "Phone":
                    data = list.get(i).get("Value");
                    core.base.WaitManager.waitForVisible(GenericActionsHelper.phoneFieldInput(list.get(i).get("Field Name")));
                    seleniumCommands.enterText(GenericActionsHelper.phoneFieldInput(list.get(i).get("Field Name")), data);
                    seleniumCommands.pressTab(GenericActionsHelper.phoneFieldInput(list.get(i).get("Field Name")));
                    break;
                case "ClearText":
                    data = list.get(i).get("Value");
                    core.base.WaitManager.waitForVisible(GenericActionsHelper.textFieldXpath(list.get(i).get("Field Name")));
                    seleniumCommands.clearText(GenericActionsHelper.textFieldXpath(list.get(i).get("Field Name")));
                    seleniumCommands.pressTab(GenericActionsHelper.textFieldXpath(list.get(i).get("Field Name")));
                    break;
                case "lookup":
                    data = list.get(i).get("Value");
                    seleniumCommands.scrollToElement(By.xpath("//*[text()='" + fieldName + "']/../..//following-sibling::div//input"));
                    seleniumCommands.click(By.xpath("//*[text()='" + fieldName + "']/../..//following-sibling::div//input"));
                    seleniumCommands.enterText(By.xpath("//*[text()=\"" + fieldName + "\"]/../..//following-sibling::div//input"), data);
                    actions = new Actions(core.base.DriverManager.getDriver());
                    actions.sendKeys(Keys.SPACE).perform();
                    actions.sendKeys(Keys.BACK_SPACE).perform();
                    seleniumCommands.click(By.xpath("//li//span[contains(text(),'" + data + "')]"));
                    break;
                case "lookup backend":
                    data = list.get(i).get("Value");
                    seleniumCommands.scrollToElement(By.xpath("(//*[text()='" + fieldName + "']/../..//following-sibling::div//input)[last()]"));
                    seleniumCommands.enterText(By.xpath("(//*[text()='" + fieldName + "']/../..//following-sibling::div//input)[last()]"), data);
                    seleniumCommands.clickWithJavaScript(By.xpath("(//*[text()='" + fieldName + "']/../..//following-sibling::div//input)[last()]"));
                    seleniumCommands.clickWithJavaScript(By.xpath("//span[text()='Show more results for \"" + data + "\"']"));
                    seleniumCommands.clickWithJavaScript(By.xpath("//a[text()=\"" + data + "\"]"));
                    data = core.base.DriverManager.getDriver().findElement(By.xpath("(//*[text()='" + fieldName + "']/../..//following-sibling::div//input)[last()]")).getAttribute("value");
                    seleniumCommands.pressTab(By.xpath("(//*[text()='" + fieldName + "']/../..//following-sibling::div//input)[last()]"));
                    break;
                case "lookup click":
                    data = list.get(i).get("Value");
                    seleniumCommands.scrollToElement(GenericActionsHelper.lookUpXpath(fieldName));
                    WebElement element = core.base.DriverManager.getDriver().findElement(GenericActionsHelper.lookUpXpath(fieldName));
                    element.sendKeys(Keys.chord(Keys.CONTROL, "a", Keys.DELETE));
                    seleniumCommands.enterText(GenericActionsHelper.lookUpXpath(fieldName), data);
                    try {
                        seleniumCommands.click(GenericActionsHelper.lookUpXpath(fieldName));
                    } catch (Exception e) {
                        seleniumCommands.clickWithJavaScript(GenericActionsHelper.lookUpXpath(fieldName));
                    }

                    Actions ac = new Actions(core.base.DriverManager.getDriver());
                    ac.sendKeys(Keys.SPACE).perform();
                    ac.sendKeys(Keys.BACK_SPACE).perform();
                    ac.sendKeys(Keys.ARROW_DOWN).perform();
                    ac.sendKeys(Keys.ENTER).perform();
                    break;
                case "lookup specific":
                    data = list.get(i).get("Value");
                    seleniumCommands.scrollToElement(By.xpath("//*[text()='" + fieldName + "']/../..//following-sibling::div"));
                    seleniumCommands.clickWithJavaScript(By.xpath("//*[text()='" + fieldName + "']/../..//following-sibling::div//input"));
                    seleniumCommands.enterText(By.xpath("//*[text()='" + fieldName + "']/../..//following-sibling::div//input"), data);
                    seleniumCommands.clickWithJavaScript(By.xpath("//*[text()='" + fieldName + "']/../..//following-sibling::div//input"));
                    actions = new Actions(core.base.DriverManager.getDriver());
                    actions.sendKeys(Keys.ARROW_DOWN).perform();
                    actions.sendKeys(Keys.ENTER).perform();
                    break;
                case "lookup double-click":
                    data = list.get(i).get("Value");
                    seleniumCommands.scrollToElement(By.xpath("//*[text()='" + fieldName + "']"));
                    seleniumCommands.enterText(GenericActionsHelper.lookUpXpath(fieldName), data);
                    Actions ac1 = new Actions(core.base.DriverManager.getDriver());
                    ac1.sendKeys(Keys.SPACE).perform();
                    ac1.sendKeys(Keys.BACK_SPACE).perform();
                    core.base.WaitManager.waitForVisible(By.xpath("//span[contains(text(),'" + data + "')]"));
                    ac1.moveToElement(core.base.DriverManager.getDriver().findElement(By.xpath("//span[contains(text(),'" + data + "')]"))).click().build().perform();
                    break;
                case "lookup using placeholder":
                    data = list.get(i).get("Value");
                    String data1 = "";
                    seleniumCommands.enterText(GenericActionsHelper.lookupUsingPlaceholder(list.get(i).get("Field Name")), data);
                default:
                    break;
            }


            //Value Transformation: Check if the UI value needs to be mapped to the system value
            String systemValue = data;


            if (!fieldName.isEmpty()) {
                core.base.TestContext.store(fieldLabel, data);
                System.out.print("Mine Value replace " + systemValue);
            }

            if (fieldLabel != null && !fieldLabel.isEmpty() && !fieldLabel.equals(fieldName)) {
                core.base.TestContext.store(fieldLabel, data);
            }

            if (list.get(i).get("Value").contains("<test>")) {
                core.base.TestContext.remove(fieldLabel);
            }

        }
    }

    /**
     * Optimized click handler that supports standard buttons and specialized Salesforce actions.
     * Implements a fallback mechanism to JavaScript click if standard interaction fails.
     *
     * @param button Label of the button to be clicked.
     */
    public void click_on_button(String button) throws Exception {

        seleniumCommands.waitForPageLoad();
        //seleniumCommands.waitForSfSpinnerToDisappear();

        By locator;

        /*
         * Resolve Locator Strategy
         */
        if (button.equalsIgnoreCase("New")) {

            /*
             * Targets visible New button from page header/list view
             * Avoids hidden modal/footer buttons
             */
            locator = By.xpath(
                    "(//button[normalize-space()='New' or .//*[normalize-space()='New']]" +
                            "[not(@disabled)])[last()]"
            );

        } else if (button.matches("(?i)Cancel|Submit|Save & Next")) {

            /*
             * Handles standard Lightning buttons with nested spans
             */
            locator = By.xpath(
                    "//button[normalize-space()='" + button + "' or .//*[normalize-space()='" + button + "']]"
            );

        }
        else if (button.matches("(?i)Accept|Close Case")) {

            /*
             * Handles standard Lightning buttons with nested spans
             */

            locator = By.xpath(
                    "(//runtime_platform_actions-actions-ribbon//button[normalize-space()='" + button + "' or .//*[normalize-space()='" + button + "']])[last()]"
            );

        }
        else {

            /*
             * Default centralized locator strategy
             */
            locator = GenericActionsHelper.BUTTON(button);
        }

        try {

            LOGGER.info("Attempting to click button: '{}'", button);

            /*
             * Primary click attempt
             */
            try {
                seleniumCommands.scrollUntilElementVisible(locator);
                seleniumCommands.click(locator);
                seleniumCommands.waitForPageLoad();

                LOGGER.info(
                        "Button '{}' clicked successfully using standard click.",
                        button
                );

            } catch (Exception clickException) {

                LOGGER.warn(
                        "Standard click failed for '{}'. Retrying with JavaScript click.",
                        button
                );

                seleniumCommands.clickWithJavaScript(locator);
                seleniumCommands.waitForPageLoad();

                LOGGER.info(
                        "Button '{}' clicked successfully using JavaScript click.",
                        button
                );
            }

            //seleniumCommands.waitForSfSpinnerToDisappear();

        } catch (Exception e) {

            LOGGER.error(
                    "Failed to click button '{}'. Error: {}",
                    button,
                    e.getMessage(),
                    e
            );

            throw new RuntimeException(
                    "Functional Failure: Unable to click button '" + button + "'",
                    e
            );
        }
    }



    public void verifyToastMessageAndClose(String toastType, String expectedMessage) throws Exception {
        try {

            WebElement toastEle = core.base.WaitManager.waitForVisible(GenericActionsHelper.toast(toastType));


            seleniumCommands.verifyText(GenericActionsHelper.toast(toastType), expectedMessage);

            // Close the toast
            WebElement toastCloseBtn = core.base.WaitManager.waitForVisible(GenericActionsHelper.closeBtnOnToast(toastType));
            seleniumCommands.click(toastCloseBtn);
            LOGGER.info("Toast closed successfully.");

        } catch (Exception e) {
            throw new Exception("Failed to verify toast message: " + e.getMessage());
        }
    }

    public void selectAppFromAppLauncherMain(String appName) throws Throwable {
        this.searchFromAppLauncher(appName);
        try {
            seleniumCommands.clickWithJavaScript(GenericActionsHelper.appLinkAppLauncher(appName));
        } catch (Exception e) {
            System.out.println("Either the application is not present or the user does not have permission to use it");
        }
    }

    public String getCurrentApplication() throws Throwable {
        By xpath = By.xpath("//*[contains(@class,'app-name')]/span");
        String currentApplication = seleniumCommands.getText(xpath);
        return currentApplication;
    }

    public void searchFromAppLauncher(String appName) throws Exception {

        core.base.WaitManager.waitForVisible(GenericActionsHelper.APP_LAUNCHER_ICON_HOME_PAGE);
        try {
            seleniumCommands.click(GenericActionsHelper.APP_LAUNCHER_ICON_HOME_PAGE);
        } catch (Exception e) {
            seleniumCommands.clickWithJavaScript(GenericActionsHelper.APP_LAUNCHER_ICON_HOME_PAGE);
        }
        core.base.WaitManager.waitForVisible(GenericActionsHelper.VIEW_ALL_BUTTON);
        seleniumCommands.click(GenericActionsHelper.VIEW_ALL_BUTTON);
        core.base.WaitManager.waitForVisible(GenericActionsHelper.SEARCH_APPS_OR_ITEMS_BOX);
        seleniumCommands.enterText(GenericActionsHelper.SEARCH_APPS_OR_ITEMS_BOX, appName);
    }


    public static String getStartOrEndDateOfMonth(String startOrEnd) {
        SimpleDateFormat dt = new SimpleDateFormat("M/d/yyyy");
        Calendar calendar = Calendar.getInstance();
        if (startOrEnd.equalsIgnoreCase("Start"))
            calendar.set(Calendar.DAY_OF_MONTH, 1);
        else
            calendar.set(Calendar.DAY_OF_MONTH, calendar.getActualMaximum(Calendar.DAY_OF_MONTH));
        Date date = calendar.getTime();
        System.out.println(dt.format(date));
        String datee = (String) dt.format(date);
        System.out.println(datee);
        return datee;
    }

    public void clickListItem(String listItem) throws Exception {
        core.base.WaitManager.waitForVisible(GenericActionsHelper.listItem(listItem));
        seleniumCommands.click(GenericActionsHelper.listItem(listItem));
    }


    public void requiredFieldsPresenceOrAbsence(String presenceOrAbsence, DataTable dt) throws Exception {
        List<Map<String, String>> list = dt.asMaps(String.class, String.class);
        for (int i = 0; i < list.size(); i++) {
            if (presenceOrAbsence.equalsIgnoreCase("presence")) {
                core.base.WaitManager.waitForVisible(GenericActionsHelper.requiredFields(list.get(i).get("Field Name")));
            } else {
                WaitManager.waitForInvisible(GenericActionsHelper.requiredFields(list.get(i).get("Field Name")));
            }
        }
    }


    public void searchFromGlobalSearchBox(String searchFor) throws Exception {
        seleniumCommands.refreshPage();
        By path = By.xpath("//button[text()='Search...']");
        core.base.WaitManager.waitForVisible(path);
        seleniumCommands.clickWithJavaScript(path);
        path = By.xpath("//input[contains(@placeholder,'Search...') and @type='search' and not(contains(@placeholder,'list'))]");
        core.base.WaitManager.waitForVisible(path);
        seleniumCommands.enterText(path, searchFor);
        Actions actions = new Actions(core.base.DriverManager.getDriver());
        actions.sendKeys(Keys.ENTER).perform();
        try {
            seleniumCommands.scrollToElement(By.xpath("(//a[@title='" + searchFor + "'])[last()]"));
            seleniumCommands.click(By.xpath("(//a[@title='" + searchFor + "'])[last()]"));
        } catch (Exception e) {
            seleniumCommands.clickWithJavaScript(By.xpath("(//a[@title='" + searchFor + "'])[last()]"));
        }
    }

    /**
     * Validates the current browser URL against the expected URL, ignoring query parameters.
     *
     * @param expectedUrl The part of the URL expected (e.g., "checkout/step2").
     */
    public void validateCurrentUrl(String expectedUrl) {
        String actualUrl = seleniumCommands.getCurrentUrl();

        // 1. Clean up URLs to be safe (e.g., remove trailing slashes and query parameters)
        String cleanedActualUrl = actualUrl.split("\\?")[0].replaceAll("/$", "");
        String cleanedExpectedUrl = expectedUrl.replaceAll("/$", "");

        LOGGER.info("VALIDATION: Checking URL. Expected: '{}', Actual: '{}'", cleanedExpectedUrl, cleanedActualUrl);

        if (!cleanedActualUrl.contains(cleanedExpectedUrl)) {

            String errorMessage = String.format(
                    "URL VALIDATION FAILED: The current URL does not contain the expected path/URL. " +
                            "Expected to contain: '%s'. Actual URL found: '%s'.",
                    cleanedExpectedUrl, actualUrl
            );
            LOGGER.error(errorMessage);
            throw new AssertionError(errorMessage);
        }

        LOGGER.info("SUCCESS: Current URL '{}' contains the expected path '{}'.", actualUrl, expectedUrl);
    }

    public void uploadFileOnDocumentsPage(String fieldName) throws Exception {
        WebElement uploadElement = WaitManager.waitForVisible(By.xpath("//*[contains(text(), \"" + fieldName + "\")]/parent::*//input[@type='file']"));
        uploadElement.sendKeys(System.getProperty("user.dir") + "/src/main/resources/uploadFiles/" + "test.png");
        if (WaitManager.waitForAllVisible(By.xpath("//h1[text()='Upload Files']")).size() > 0) {
            WaitManager.waitForVisible(By.xpath("//*[@variant=\"success\"]"));
            click_on_button("Done");
        } else {
            click_on_button("Done");
        }
    }


    public void clickOnLink(String name) throws Exception {
        if (name.equalsIgnoreCase("Upload Files")) {
            seleniumCommands.safeClick(GenericActionsHelper.hyperLink(name));

        } else {
            seleniumCommands.safeClick(GenericActionsHelper.hyperLink(name));
        }
    }


    /**
     * Searches and opens a Salesforce record by name and object type.
     * - Opens the global search.
     * - Filters by object type.
     * - Enters the record name and selects the matching result.
     *
     * @param recordName The name of the record to search for.
     * @param objName    The Salesforce object type (e.g., Account, Contact).
     */
    /*
    public void searchRecord(String objName, String recordName) {
        try {
            seleniumCommands.waitForPageLoad();
            LOGGER.info("Opening global search...");
            WaitManager.waitForClickable(GenericActionsHelper.GLOBAL_SEARCH_BOX);
            try {
                WaitManager.waitForClickable(By.xpath("//span[contains(@aria-label,\"Global Header\")]"));
                seleniumCommands.click(GenericActionsHelper.GLOBAL_SEARCH_BOX);
            }
            catch (Exception e) {
                seleniumCommands.safeClick(GenericActionsHelper.GLOBAL_SEARCH_BOX);
            }

            LOGGER.info("Entering record name '{}'", recordName);
            WebElement globalSearchField=WaitManager.waitForVisible(By.xpath("//div[@class='forceSearchAssistantDialog']//input[@placeholder='Search...']"));
            seleniumCommands.enterText(globalSearchField, recordName);
            globalSearchField.sendKeys(Keys.ENTER);



            LOGGER.info("Selecting the record from results");
            By record = By.xpath("//a[@title=\""+recordName+"\" and @data-refid]//parent::span");

            try{

                seleniumCommands.scrollUntilElementVisible(record);

                seleniumCommands.click(record);

            } catch (Exception e) {

                seleniumCommands.safeClick(By.xpath("//li[contains(@class, 'forceSearchScopeItem')]//a[@title=\""+objName+"\"]"));

                WaitManager.waitForVisible(record);

                seleniumCommands.click(record);

            }



            WaitManager.waitForVisible(By.xpath("//h1//lightning-formatted-text[contains(text(),'"+recordName+"')]"));
            LOGGER.info("Successfully opened '{}' record of type '{}'.", recordName, objName);

        } catch (Exception e) {
            LOGGER.error("Failed to search and open '{}' record of type '{}': {}", recordName, objName, e.getMessage());
            throw new RuntimeException(e);

        }

    }


     */
    public void searchRecord(String objName, String recordName) {
        try {
            //SeleniumCommands.getInstance().waitForSfSpinnerToDisappear();
            LOGGER.info("Opening global search...");

            WaitManager.waitForVisible(By.xpath("//span[contains(@aria-label,\"Global Header\")]"));
            seleniumCommands.click(GenericActionsHelper.GLOBAL_SEARCH_BOX);


            LOGGER.info("Selecting object type filter: '{}'", objName);
            WebElement searchByObjField = WaitManager.waitForVisible(
                    By.xpath("//label[text()='Search by object type']//following::input[@data-value='Search: All']")

            );

            seleniumCommands.clickWithJavaScript(searchByObjField);
            //enterText(searchByObjField, objName);
            LOGGER.info("Choosing object '{}' from suggestions", objName);
            seleniumCommands.clickWithJavaScript(By.xpath("//ul[@aria-label='Suggested For You']//span[text()='" + objName + "']"));


            LOGGER.info("Entering record name '{}'", recordName);
            WebElement globalSearchField = WaitManager.waitForVisible(By.xpath("//div[@class='forceSearchAssistantDialog']//input[@placeholder='Search...']"));
            seleniumCommands.enterTextWithJS(globalSearchField, recordName);
            globalSearchField.sendKeys(Keys.ENTER);


            LOGGER.info("Selecting the record from results");
            By record = By.xpath("//a[@title=\"" + recordName + "\" and @data-refid]");

            try {

                //seleniumCommands.scrollUntilElementVisible(record);

                seleniumCommands.click(record);

            } catch (Exception e) {
                seleniumCommands.safeClick(GenericActionsHelper.GLOBAL_SEARCH_BOX);
                seleniumCommands.sendKeys(globalSearchField, recordName);
                globalSearchField.sendKeys(Keys.ENTER);

                seleniumCommands.clickWithJavaScript(By.xpath("//li[contains(@class, 'forceSearchScopeItem')]//a[@title=\"" + objName + "\"]"));
                seleniumCommands.safeClick(record);

            }


            WaitManager.waitForVisible(By.xpath("//h1//lightning-formatted-text[contains(text(),'" + recordName + "')]"));
            LOGGER.info("Successfully opened '{}' record of type '{}'.", recordName, objName);

        } catch (Exception e) {
            LOGGER.error("Failed to search and open '{}' record of type '{}': {}", recordName, objName, e.getMessage());
            throw new RuntimeException(e);

        }

    }


    /**
     * A generic retry wrapper that attempts to execute a task multiple times.
     *
     * @param task        The logic to execute (Lambda expression)
     * @param taskName    A descriptive name for logging
     * @param maxAttempts Number of times to try
     */
    public void executeWithRetry(Runnable task, String taskName, int maxAttempts) {
        int count = 0;
        while (count < maxAttempts) {
            try {
                count++;
                LOGGER.info("Attempt {}/{}: Executing task [{}]", count, maxAttempts, taskName);
                task.run();
                return; // Success! Exit the method.
            } catch (Exception e) {
                LOGGER.warn("Attempt {} failed for [{}]: {}", count, taskName, e.getMessage());
                if (count >= maxAttempts) {
                    LOGGER.error("All {} attempts failed for [{}].", maxAttempts, taskName);
                    throw e;
                }
                //seleniumCommands.waitForSfSpinnerToDisappear();
            }
        }
    }

    /**
     * Closes Split View if it is present.
     * - Clicks Split View when found.
     * - Otherwise continues normally.
     */
    public void closeSplitViewIfPresent() {
        try {
            WebElement splitView = core.base.DriverManager.getDriver().findElement(By.xpath("//span[normalize-space(text())='Split view mode']"));
            seleniumCommands.click(splitView);
            LOGGER.debug("Closed Split View Mode");
        } catch (org.openqa.selenium.NoSuchElementException e) {
            LOGGER.debug("Split View Mode not present, continuing...");
        }
    }


    public void clickEditButton() throws Exception {
        core.base.WaitManager.waitForVisible(By.xpath("(//a[@title='Show one more action'])[last()]"));
        seleniumCommands.clickWithJavaScript(By.xpath("(//a[@title='Show one more action'])[last()]"));

        core.base.WaitManager.waitForVisible(By.xpath("//div[text()='Edit']"));
        seleniumCommands.clickWithJavaScript(By.xpath("//div[text()='Edit']"));
    }

    public void setRichText(WebElement richText, String text) {
        JavascriptExecutor js = (JavascriptExecutor) core.base.DriverManager.getDriver();

        js.executeScript(
                "arguments[0].value = arguments[1]; " +
                        "arguments[0].dispatchEvent(new Event('input', { bubbles: true })); " +
                        "arguments[0].dispatchEvent(new Event('change', { bubbles: true }));",
                richText, text
        );
    }


    public void verifyAbsencePicklistValues(String name, DataTable dt) throws Exception {
        core.base.WaitManager.waitForVisible(GenericActionsHelper.FillDropdownXpath(name));
        seleniumCommands.scrollToElement(GenericActionsHelper.FillDropdownXpath(name));

        try {
            seleniumCommands.click(GenericActionsHelper.FillDropdownXpath(name));
        } catch (Exception e) {
            seleniumCommands.clickWithJavaScript(GenericActionsHelper.FillDropdownXpath(name));

        }

    }


    public String fetchValueFromUI(String field, String section) throws Exception {
        String value = "";

        value = seleniumCommands.getText(By.xpath("(//*[@aria-label=\"" + section + "\"]//*[contains(text(),\"" + field + "\")]/following::span)[1]" +
                "| (//*[contains(text(),\"" + section + "\")]/ancestor::button/following-sibling::div//*[text()='" + field + "']/../../../../../following-sibling::div//div)[last()] | ((//*[contains(text(),'" + section + "')])[last()]//following::*[contains(text(),'" + field + "')]/following::span)[1] | (//*[text()='" + section + "'])[last()]//following::*[contains(text(),'" + field + "')]/following::span | //*[text()='" + section + "']//following::*[text()='" + field + "']//following::span"));
        System.out.println("Value fetched from UI: " + value);
        return value;
    }

    public By dataOnUI(String fieldName, String section, String data) throws Exception {
        By xpath = By.xpath("(//*[contains(text(),\"" + section + "\")]//following::*[text()='" + fieldName + "']//following::span[text()='" + data + "']) | (//*[contains(text(),\"" + section + "\")]//following::*[text()='" + fieldName + "']//following::span/div[text()='" + data + "'])");
        return xpath;
    }


    /**
     * Ensures a collapsible section is open by checking its state and clicking the trigger only if necessary.
     * * NOTE: This requires two working helper methods in GenericActionsObjects:
     * 1. collapsedSection(name): Locator for the clickable header/trigger.
     * 2. sectionContent(name): Locator for a stable, visible element *inside* the section (used to check state).
     * * @param sectionName The name or label of the section to open.
     *
     * @throws Exception if the trigger cannot be located or the section fails to open after clicking.
     */
    public void openCollapsedSection(String sectionName) throws Exception {

        By locator = GenericActionsHelper.collapsedSection(sectionName);

        if (seleniumCommands.verifyElementDisplayed(locator)) { // Use a short wait (e.g., 3s)

            seleniumCommands.scrollUntilElementVisible(locator);

            try {
                seleniumCommands.click(locator);
                System.out.println("Successfully clicked and opened section: " + sectionName);

            } catch (Exception e) {

                System.out.println("Native click failed on section trigger. Falling back to JS click.");
                seleniumCommands.clickWithJavaScript(locator);
                System.out.println("Successfully clicked (JS fallback) and opened section: " + sectionName);

            }
        } else {
            System.out.println("Section trigger '" + sectionName + "' is NOT visible (assumed open/not present). Skipping click action.");
        }
    }

    public void click_on_tab(String tab) throws Exception {

        try {
            core.base.WaitManager.waitForClickable(GenericActionsHelper.tab(tab));
            seleniumCommands.click(GenericActionsHelper.tab(tab));
        } catch (Exception e) {
            seleniumCommands.scrollUntilElementVisible(GenericActionsHelper.tab(tab));
            seleniumCommands.clickWithJavaScript(GenericActionsHelper.tab(tab));
        }
    }

    public void click_on_detailPage_tab(String tab) throws Exception {
        try {
            seleniumCommands.scrollToElement(GenericActionsHelper.detailPageTab(tab));
            core.base.WaitManager.waitForVisible(GenericActionsHelper.detailPageTab(tab));
            seleniumCommands.click(GenericActionsHelper.detailPageTab(tab));
        } catch (Exception e) {
            seleniumCommands.scrollUntilElementVisible(GenericActionsHelper.detailPageTab(tab));
            seleniumCommands.clickWithJavaScript(GenericActionsHelper.detailPageTab(tab));
        }
    }

    public void validateAndUncheckCheckbox(String field) throws Exception {
        Boolean status;
        status = seleniumCommands.isCheckboxChecked(By.xpath("//span[text()='" + field + "']//preceding-sibling::span"));
        if (status) {
            try {
                seleniumCommands.click(By.xpath("//span[text()='" + field + "']//preceding-sibling::span"));
            } catch (Exception e) {
                seleniumCommands.clickWithJavaScript(By.xpath("//span[text()='" + field + "']//preceding-sibling::span"));
            }
        }
    }

    public void compareBackendMaps(Map<String, String> expectedMap, Map<String, String> actualMap) {
        List<String> failures = new ArrayList<>();

        LOGGER.info("--- STARTING BACKEND DATA COMPARISON ---");

        for (String field : expectedMap.keySet()) {
            String expected = expectedMap.get(field);
            String actual = actualMap.get(field);

            // 1. Handle Salesforce Boolean Mapping (1/0 to true/false)
            if (actual != null) {
                if (actual.equals("1")) actual = "true";
                if (actual.equals("0")) actual = "false";
            }

            // 2. Normalization: Treat nulls as empty strings
            String safeExpected = (expected == null) ? "" : expected.trim();
            String safeActual = (actual == null) ? "" : actual.trim();

            // 3. Comparison Logic
            boolean isMatch = safeExpected.equalsIgnoreCase(safeActual);

            // 4. LOGGING: Side-by-Side Debugging
            if (isMatch) {
                LOGGER.info("OK   | Field: [{}] | Expected: [{}] | Actual: [{}]", field, safeExpected, safeActual);
            } else {
                String errorMsg = String.format("FAIL | Field: [%s] | Expected: [%s] | Actual: [%s]", field, safeExpected, safeActual);
                LOGGER.error(errorMsg);
                failures.add(errorMsg);
            }
        }

        LOGGER.info("--- END BACKEND DATA COMPARISON ---");

        if (!failures.isEmpty()) {
            String errorMessage = "Backend data mismatch found. Total failures: " + failures.size() + "\n" + String.join("\n", failures);
            Assert.fail(errorMessage);
        }
    }

    /**
     * Logic to handle collapsible sections and field validation
     */
    public void expandSectionIfCollapsed(String sectionName) {
        // Locator for the section header button
        WebElement sectionElement;
        By sectionTrigger = By.xpath("//button[.//span[text()='" + sectionName + "']] | //h3[.//span[text()='" + sectionName + "']]");
        try {
            seleniumCommands.scrollUntilElementVisible(sectionTrigger);
            sectionElement = core.base.WaitManager.waitForVisible(sectionTrigger);
        } catch (Exception e) {
            seleniumCommands.scrollUntilElementVisible(sectionTrigger);
            sectionElement = core.base.DriverManager.getDriver().findElement(sectionTrigger);
        }


        // Check the 'aria-expanded' attribute to see if we need to click it
        String isExpanded = sectionElement.getAttribute("aria-expanded");

        if (isExpanded != null && isExpanded.equalsIgnoreCase("false")) {
            LOGGER.info("Section '{}' is collapsed. Expanding now...", sectionName);
            seleniumCommands.click(sectionElement);
        } else {
            LOGGER.info("Section '{}' is already expanded or not collapsible.", sectionName);
        }
    }

    /**
     * Validates that a field value is present and not just a dash or empty
     */
    public void verifyFieldIsNotBlank(String fieldLabel) {
        // 1. Find the field value.
        // In Lightning, values are often in a <span> or <a> following the label
        By fieldPath = By.xpath("//span[text()='" + fieldLabel + "']/parent::div/following-sibling::div//*[self::a or self::span][not(text()='')]");

        try {
            WebElement valElement = core.base.WaitManager.waitForVisible(fieldPath);
            String value = seleniumCommands.getText(valElement).trim();

            // Salesforce often puts a "-" for empty fields
            if (value.isEmpty() || value.equals("-")) {
                throw new RuntimeException("Field '" + fieldLabel + "' is blank or contains only a dash.");
            }

            LOGGER.info("Field '{}' validated with value: {}", fieldLabel, value);
        } catch (Exception e) {
            throw new RuntimeException("Validation failed: Could not find a populated value for field: " + fieldLabel);
        }
    }

    public void verifyNotPrePopulatedField(DataTable dt) throws Exception {
        List<Map<String, String>> list = dt.asMaps(String.class, String.class);
        String field = "";
        for (int i = 0; i < list.size(); i++) {
            field = list.get(i).get("Field Name");
            String text = "";
            System.out.println("=======================================" + text.length());
            core.base.TestContext.store(field, text);
            Assert.assertEquals(text.length(), 0);
        }
    }

    public void verifyPicklistValues(String name, DataTable dt) throws Exception {
        core.base.WaitManager.waitForVisible(GenericActionsHelper.FillDropdownXpath(name));
        seleniumCommands.scrollToElement(GenericActionsHelper.FillDropdownXpath(name));
        try {
            seleniumCommands.click(GenericActionsHelper.FillDropdownXpath(name));
        } catch (Exception e) {
            seleniumCommands.clickWithJavaScript(GenericActionsHelper.FillDropdownXpath(name));

        }
        List<Map<String, String>> list = dt.asMaps(String.class, String.class);
        for (int i = 0; i < list.size(); i++) {
            String opt = list.get(i).get("Values");
        }
    }



    /*
    public void verifyFieldValues(List<Map<String, String>> list) throws Exception {
        StringBuilder errorCollector = new StringBuilder();

        for (Map<String, String> row : list) {
            String coreFieldLabel = row.get("Field Name").trim();
            String expValue = (row.get("Value") != null) ? row.get("Value").trim() : "";
            String expectedValue = "";
            By labelLocator = By.xpath("//div[contains(@class,'slds-form-element')]//span[text()='" + coreFieldLabel + "'] | //label[text()='" + coreFieldLabel + "']");
            try {
                // 1. Resolve Data (Handling Auto-set, Maps, and Campaigns)
                if (expValue.equalsIgnoreCase("auto")) {
                    expectedValue = GenericActionsHelper.getAutoSetValue(coreFieldLabel);
                }
                else if (expValue.equalsIgnoreCase("FromMap")) {
                    expectedValue = (String) core.base.TestContext.retrieve(coreFieldLabel);
                }
                else if (expValue.toLowerCase().contains("campaign")) {
                    expectedValue = (String) core.base.TestContext.retrieve(expValue);
                }
                else {
                    expectedValue = expValue;
                }
                seleniumCommands.scrollForElementWithScreen(labelLocator);
                // 2. Verification Logic
                if (expectedValue == null || expectedValue.isEmpty() || expectedValue.equals("—")) {
                    // Using a getter to verify it's actually blank rather than just verifying absence
                    String actual = SalesforceCommands.getInstance().getFieldValueFromLightningPage(coreFieldLabel);
                    if (!actual.isEmpty()) {
                        errorCollector.append("[-] Field [").append(coreFieldLabel)
                                .append("] expected blank but found: [").append(actual).append("]\n");
                    }

                } else if (expectedValue.equalsIgnoreCase("Yes") || expectedValue.equalsIgnoreCase("No")) {
                    // CHECKBOX CHECK
                    By locator = GenericActionsHelper.checkBox(coreFieldLabel);
                    String expectedState = expectedValue.equalsIgnoreCase("Yes") ? "True" : "False";

                    seleniumCommands.scrollForElementWithScreen(locator);
                    String actualState = seleniumCommands.isChecked(locator);

                    if (!actualState.equalsIgnoreCase(expectedState)) {
                        errorCollector.append("[-] Checkbox [").append(coreFieldLabel)
                                .append("] mismatch -> Expected: ").append(expectedValue)
                                .append(", Found: ").append(actualState).append("\n");
                    }

                } else {
                    /*
                    // STANDARD TEXT CHECK
                    //By locator = GenericActionsHelper.fieldsOnDetailPage(coreFieldLabel, expectedValue);
                    try {
                        String actualValue = SalesforceCommands.getInstance().getFieldValueFromLightningPage(coreFieldLabel);
                       //System.out.print("Check 1:"+seleniumCommands.getText(locator));
                        //seleniumCommands.scrollForElementWithScreen(locator);
                        //seleniumCommands.verifyText(locator, expectedValue);

                    } catch (AssertionError ae) {
                        // Capture actual failure without losing details
                        errorCollector.append("[-] Field [").append(coreFieldLabel)
                                .append("] mismatch -> Expected: [").append(expectedValue)
                                .append("]. UI did not match.\n");
                    }


                    String actualValue = SalesforceCommands.getInstance().getFieldValueFromLightningPage(coreFieldLabel);
                    if (!actualValue.equalsIgnoreCase(expectedValue)) {
                        errorCollector.append(String.format("[-] Field [%s] Mismatch | Expected: [%s] | Found: [%s]\n",
                                coreFieldLabel, expectedValue, actualValue));
                    } else {
                        System.out.println("Passed: " + coreFieldLabel + " matches [" + actualValue + "]");
                    }
                }

            } catch (Exception e) {
                // Catching individual field processing errors so loop continues
                errorCollector.append("[!] Error processing [").append(coreFieldLabel)
                        .append("]: ").append(e.getMessage()).append("\n");
            }
        }

        // 3. Final Assertion (The "Fail-at-the-end" logic)
        if (errorCollector.length() > 0) {
            throw new AssertionError("Test Failures identified:\n" + errorCollector.toString());
        }
    }
*/

    public void verifyFieldValues(List<Map<String, String>> list) throws Exception {

        for (Map<String, String> row : list) {

            String fieldLabel = row.get("Field Name").trim();
            String value = row.get("Value");

            String expectedValue = value != null ? value.trim() : "";

            // Resolve dynamic value from TestContext
            if ("FromTextContext".equalsIgnoreCase(expectedValue)) {
                expectedValue = TestContext.retrieveString(fieldLabel);
            }
            By fieldLocator = By.xpath(
                    "(//*[normalize-space(text())='" + fieldLabel + "'])[last()]"
            );

            try {

                // =========================================================
                // BLANK / HIDDEN FIELD VALIDATION
                // =========================================================
                if (expectedValue == null || expectedValue.trim().isEmpty()) {



                    seleniumCommands.scrollForElementWithScreen(fieldLocator);

                    By valueLocator = GenericActionsHelper.fieldsOnDetailPage(fieldLabel);

                    seleniumCommands.verifyElementNotDisplayed(valueLocator);

                    LOGGER.info("Verified field is hidden/blank: {}", fieldLabel);

                    continue;
                }

                // =========================================================
                // CHECKBOX VALIDATION
                // =========================================================
                if ("Yes".equalsIgnoreCase(expectedValue)
                        || "No".equalsIgnoreCase(expectedValue)) {
                    seleniumCommands.scrollForElementWithScreen(fieldLocator);

                    By checkboxLocator = GenericActionsHelper.checkBox(fieldLabel);

                    seleniumCommands.scrollForElementWithScreen(checkboxLocator);

                    boolean expectedState = "Yes".equalsIgnoreCase(expectedValue);

                    boolean actualState =
                            seleniumCommands.isCheckboxChecked(checkboxLocator);

                    if (actualState != expectedState) {

                        throw new AssertionError(
                                "Checkbox state mismatch → Field: " + fieldLabel +
                                        " | Expected: " + expectedState +
                                        " | Actual: " + actualState
                        );
                    }

                    LOGGER.info(
                            "Verified checkbox field: {} = {}",
                            fieldLabel,
                            expectedValue
                    );

                    continue;
                }

                // =========================================================
                // TEXT / PICKLIST / LOOKUP / NORMAL FIELD VALIDATION
                // =========================================================
                By locator =
                        GenericActionsHelper.fieldsOnDetailPage(fieldLabel);
                seleniumCommands.scrollForElementWithScreen(fieldLocator);
                WaitManager.waitForVisible(locator);

                seleniumCommands.verifyText(locator, expectedValue);

                LOGGER.info(
                        "Verified field: {} | Expected Value: {}",
                        fieldLabel,
                        expectedValue
                );

            } catch (TimeoutException e) {

                throw new AssertionError(
                        "Timeout while verifying field: " + fieldLabel,
                        e
                );

            } catch (NoSuchElementException e) {

                throw new AssertionError(
                        "Field not found on UI: " + fieldLabel,
                        e
                );

            } catch (AssertionError e) {

                throw e;

            } catch (Exception e) {

                throw new AssertionError(
                        "Verification failed for field: " + fieldLabel +
                                " | Reason: " + e.getMessage(),
                        e
                );
            }
        }
    }

    public void verifyCheckboxState(String fieldLabel, String expectedValue) {
        // 1. Locate the checkbox container/input based on the label
        // Salesforce specific: often finds the checkbox input near the label text
        By checkboxLocator = By.xpath("//span[text()='" + fieldLabel + "']/parent::div//input[@type='checkbox'] | " +
                "//label[text()='" + fieldLabel + "']/following-sibling::div//input");

        try {
            seleniumCommands.scrollUntilElementVisible(checkboxLocator);

            // 2. Get the actual state
            boolean isChecked = core.base.WaitManager.waitForVisible(checkboxLocator).isSelected();
            String actualState = isChecked ? "Yes" : "No";

            // 3. Compare
            if (!actualState.equalsIgnoreCase(expectedValue)) {
                throw new AssertionError("Checkbox Mismatch! Field: " + fieldLabel +
                        " | Expected: " + expectedValue +
                        " | Actual: " + actualState);
            }

            LOGGER.info("Checkbox [{}] verified successfully as {}", fieldLabel, actualState);

        } catch (Exception e) {
            throw new RuntimeException("Could not verify checkbox for field: " + fieldLabel + ". Error: " + e.getMessage());
        }
    }

    public void fillSavedDetails(DataTable dt, Map<String, String> dataMap) throws Exception {
        List<Map<String, String>> list = dt.asMaps(String.class, String.class);
        String dataType = "";
        String data = "";
        String field = "";
        String fieldLabel = "";
        for (int i = 0; i < list.size(); i++) {
            dataType = list.get(i).get("Data Type");
            field = list.get(i).get("Field Name");
            fieldLabel = list.get(i).get("Field Label");
            data = (String) core.base.TestContext.retrieve(list.get(i).get("Field Name"));
            Actions action1 = new Actions(core.base.DriverManager.getDriver());
            switch (dataType) {
                case "Text":
                    core.base.WaitManager.waitForVisible(By.xpath("//mat-label[normalize-space()='" + field + "']//ancestor::*/input | //input[@placeholder='" + field + "']"));
                    seleniumCommands.enterText(By.xpath("//mat-label[normalize-space()='" + field + "']//ancestor::*/input | //input[@placeholder='" + field + "']"), data);
                    action1.sendKeys(Keys.TAB).build().perform();
                    break;
                case "Text using Field Label":
                    data = (String) core.base.TestContext.retrieve(fieldLabel);
                    System.out.println(data);
                    core.base.WaitManager.waitForVisible(By.xpath("(//*[text()='" + field + "'])[last()]//following::div//input"));
                    seleniumCommands.enterText(By.xpath("(//*[text()='" + field + "'])[last()]//following::div//input"), data);
//                    action1.sendKeys(Keys.TAB).build().perform();
                    break;
                case "Dropdown using Field Label":
                    data = (String) core.base.TestContext.retrieve(fieldLabel);
                    System.out.println("Data " + data);
                    core.base.WaitManager.waitForVisible(By.xpath("//*[text()=\"" + field + "\"]/../../following-sibling::div//input"));
                    seleniumCommands.click(By.xpath("//*[text()=\"" + field + "\"]/ancestor::omnistudio-omniscript-select//input"));
                    seleniumCommands.click(By.xpath("//*[text()=\"" + field + "\"]/ancestor::omnistudio-omniscript-select//*[text()=\"" + data + "\"]"));
                    action1.sendKeys(Keys.TAB).build().perform();

                    break;
                case "lookup click":
                    seleniumCommands.scrollToElement(By.xpath("//*[text()='" + field + "']/../..//following-sibling::div"));
                    seleniumCommands.enterText(By.xpath("//*[text()='" + field + "']/../..//following-sibling::div//input"), data);
                    seleniumCommands.clickWithJavaScript(By.xpath("//*[text()='" + field + "']/../..//following-sibling::div//input"));
                    Actions ac = new Actions(core.base.DriverManager.getDriver());
                    ac.sendKeys(Keys.ARROW_DOWN).perform();
                    ac.sendKeys(Keys.ENTER).perform();
                    data = core.base.DriverManager.getDriver().findElement(By.xpath("//*[text()='" + field + "']/../..//following-sibling::div//input")).getAttribute("value");
                    break;
                case "Number using Field Label":
                    data = (String) core.base.TestContext.retrieve(fieldLabel);
                    seleniumCommands.click(By.xpath("//*[text()='" + field + "']/ancestor::*[@data-omni-input]//input"));
                    Actions actions = new Actions(core.base.DriverManager.getDriver());
                    char[] digits = data.toCharArray();
                    for (int j = 0; j < digits.length; j++) {
                        switch (digits[j]) {
                            case '0':
                                actions.sendKeys(Keys.NUMPAD0);
                                break;
                            case '1':
                                actions.sendKeys(Keys.NUMPAD1);
                                break;
                            case '2':
                                actions.sendKeys(Keys.NUMPAD2);
                                break;
                            case '3':
                                actions.sendKeys(Keys.NUMPAD3);
                                break;
                            case '4':
                                actions.sendKeys(Keys.NUMPAD4);
                                break;
                            case '5':
                                actions.sendKeys(Keys.NUMPAD5);
                                break;
                            case '6':
                                actions.sendKeys(Keys.NUMPAD6);
                                break;
                            case '7':
                                actions.sendKeys(Keys.NUMPAD7);
                                break;
                            case '8':
                                actions.sendKeys(Keys.NUMPAD8);
                                break;
                            case '9':
                                actions.sendKeys(Keys.NUMPAD9);
                                break;
                        }
                    }
                    actions.sendKeys(Keys.TAB).sendKeys(Keys.TAB).perform();
                    break;

                default:
                    break;

            }
        }
    }


    public void clickTab(String object) throws Exception {
        core.base.WaitManager.waitForVisible(By.xpath("//a/span[text()='" + object + "']|//li/a[text()='" + object + "']"));
        try {
            seleniumCommands.click(By.xpath("//a/span[text()='" + object + "'] |(//a[text()='" + object + "'])[last()]"));
        } catch (Exception e) {
            seleniumCommands.clickWithJavaScript(By.xpath("//a/span[text()='" + object + "'] |(//a[text()='" + object + "'])[last()]"));
        }
    }

    public void fieldsPopulatedWithData(DataTable dataTable) throws Exception {
        List<Map<String, String>> list = dataTable.asMaps(String.class, String.class);
        String expectedData = "";
        String field = "";
        String fieldLabel = "";
        String section = "";
        String actualData = "";
        for (int i = 0; i < list.size(); i++) {
            section = list.get(i).get("Section");
            fieldLabel = list.get(i).get("Field Label");
            field = fieldLabel.split("--")[0];

            if (field.contains("Phone Number") | field.contains("Telephone Number") | field.contains("Fax Number") | field.contains("School Telephone number"))
                actualData = actualData.replace("(", "").replace(") ", "").replace("-", "");
            System.out.println("Field: " + field);
            System.out.println("Value fetched from UI: " + actualData);
            System.out.println("Value fetched from MAP: " + expectedData);
            Assert.assertEquals(actualData, expectedData);
            core.base.TestContext.store(fieldLabel, actualData);
        }
    }


    public void enterTextInBody(String txt) throws Exception {
        seleniumCommands.enterText(By.xpath("(//body)[last()]"), txt);
    }

    public String getRandomElement(List<String> list) {
        Random rand = new Random();
        return list.get(rand.nextInt(list.size()));
    }

    public void setRandomValue(String field) throws Exception {
        List<String> list = new ArrayList<String>();
        switch (field) {
            case "State":
                list.add("Alaska");
                list.add("Alabama");
                list.add("Arizona");
                break;
            case "Country":
                list.add("Aland Islands");
                list.add("United States");
                list.add("Canada");
                break;
        }
        String value = getRandomElement(list);
        Actions actions = new Actions(core.base.DriverManager.getDriver());
        actions.moveToElement(WaitManager.waitForVisible(By.xpath("//*[text()=\"" + field + "\"]/ancestor::label/../following-sibling::div//input"))).click().build().perform();

        actions.moveToElement(WaitManager.waitForVisible(By.xpath("//*[text()=\"" + field + "\"]/ancestor::label/../following-sibling::div//li//*[text()=\"" + value + "\"]"))).click().build().perform();
//        clickElementJavascript(By.xpath("//*[text()=\""+field+"\"]/ancestor::label/../following-sibling::div//li//*[text()=\""+value+"\"]"));
        seleniumCommands.pressTab(By.xpath("//*[text()=\"" + field + "\"]/ancestor::label/../following-sibling::div//input"));
        core.base.TestContext.store(field + "--Personal Information", value);

    }


    public void compareValue(String val1, String val2, String action) {
        if (core.base.TestContext.contains(val1)) {
            val1 = (String) core.base.TestContext.retrieve(val1);
        }
        if (core.base.TestContext.contains(val2)) {
            val2 = (String) core.base.TestContext.retrieve(val2);
        }
        if (action.contains("Same")) {
            Boolean result = val1.equalsIgnoreCase(val2);
            Assert.assertEquals("true", result.toString());
        } else if (action.contains("Different")) {
            Boolean result = val1.equalsIgnoreCase(val2);
            Assert.assertEquals("false", result.toString());
        }

    }


    public void verifyMultiSelectPicklistValue(String field, DataTable dt) throws Exception {
        List<Map<String, String>> list = dt.asMaps(String.class, String.class);
        for (int i = 0; i < list.size(); i++) {
            String opt = list.get(i).get("Values");
            core.base.WaitManager.waitForVisible(By.xpath("(//div[text()='" + field + "']//following-sibling::div//span[text()='" + opt + "'])[last()]"));
            System.out.println("value found=> " + opt);
        }
    }

    public void verify_Button_Is_Selected(String field) {
        WebElement button = core.base.DriverManager.getDriver().findElement(By.xpath("//button[contains(@title,\"" + field + "\")]"));
        String color = button.getCssValue("background-color");
        String c = Color.fromString(color).asHex();
        Assert.assertEquals(c, "#3e5677", "Button is not pre-selected");

    }

    public void navigateToAppOnBE(String application) throws Throwable {
        seleniumCommands.switchToDefaultContent();
        core.base.WaitManager.waitForVisible(GenericActionsHelper.APP_LAUNCHER_ICON_HOME_PAGE);
        core.base.WaitManager.waitForClickable(GenericActionsHelper.APP_LAUNCHER_ICON_HOME_PAGE);
        seleniumCommands.click(GenericActionsHelper.APP_LAUNCHER_ICON_HOME_PAGE);
        core.base.WaitManager.waitForVisible(GenericActionsHelper.VIEW_ALL_BUTTON);
        core.base.WaitManager.waitForClickable(GenericActionsHelper.VIEW_ALL_BUTTON);
        seleniumCommands.clickWithJavaScript(GenericActionsHelper.VIEW_ALL_BUTTON);
        core.base.WaitManager.waitForVisible(GenericActionsHelper.SEARCH_APPS_OR_ITEMS_BOX);
        core.base.WaitManager.waitForClickable(GenericActionsHelper.SEARCH_APPS_OR_ITEMS_BOX);
        seleniumCommands.enterText(GenericActionsHelper.SEARCH_APPS_OR_ITEMS_BOX, application);
        selectAppFromAppLauncherMain(application);
    }

    /**
     * Navigates to a Salesforce app by name.
     * - Checks if already on the target app.
     * - If not, opens App Launcher and selects the app.
     *
     * @param name The Salesforce app name.
     */
    public void openApplication(String name) {

        try {
            //WaitManager.waitForSfSpinnerToDisappear();
            LOGGER.info("Checking if already on the '{}' app page...", name);
            core.base.WaitManager.waitForVisible(By.xpath("//div[@class='slds-global-header__logo']"));
            boolean alreadyOnApp = !core.base.DriverManager.getDriver().findElements(By.xpath("//h1[contains(@class, 'slds-context-bar__app-name')]//span[text()='" + name + "']")).isEmpty();

            if (alreadyOnApp) {
                LOGGER.info("Already on '{}' app page. No navigation required.", name);
                return;

            }
            LOGGER.info("'{}' app not detected. Opening App Launcher...", name);

            By launcherBtn = By.xpath("//button[@title='App Launcher']");

            seleniumCommands.click(launcherBtn);

            WebElement searchBox = core.base.WaitManager.waitForVisible(By.xpath("//input[@type='search' and contains(@placeholder,'Search apps and items...')]"));

            seleniumCommands.enterText(searchBox, name);

            core.base.WaitManager.waitForVisible(By.xpath("//h3[contains(text(),'Apps')]"));

            By appLocator = By.xpath("//a[@data-label='" + name + "']//b");

            WebElement appName = core.base.WaitManager.waitForVisible(appLocator);

            LOGGER.info("App '{}' found. Navigating now...", name);

            seleniumCommands.click(appName);

            //seleniumCommands.waitForSfSpinnerToDisappear();
            core.base.WaitManager.waitForVisible(By.xpath("//one-appnav"));
            //core.base.WaitManager.waitForVisible(By.xpath("//div[@class='maincontent ']"));
            core.base.WaitManager.waitForVisible(By.xpath("//h1[contains(@class, 'slds-context-bar__app-name')]//span[text()=\"" + name + "\"]"));
            LOGGER.info("Successfully navigated to '{}' app.", name);

        } catch (Exception e) {

            LOGGER.error("Failed to open application '{}': {}", name, e.getMessage(), e);

            throw new RuntimeException("Failed to open application: " + name, e);

        }

    }

    public void clickLink(String element, String page) throws Exception {
        JavascriptExecutor jse = (JavascriptExecutor) core.base.DriverManager.getDriver();
        int count = Integer.parseInt(element);
        By hyperlink = By.xpath("((//*[text()='" + page + "'])[last()]//following::tbody//th//a)[" + count + "]");
        core.base.WaitManager.waitForVisible(hyperlink);
        try {
            seleniumCommands.click(hyperlink);
        } catch (Exception e) {
            seleniumCommands.clickWithJavaScript(hyperlink);
        }


    }


    public void verifyPresenceAbsenceText(String field) throws Exception {
        core.base.WaitManager.waitForVisible(GenericActionsHelper.text(field));
        //seleniumCommands.highlightText(GenericActionsObjects.text(field));
        //scrollDownToElement(GenericActionsObjects.text(field), "");
        //verifyPresence(GenericActionsObjects.text(field));
    }

    /*
        public void loginToSalesforceEnvviaUI() {
            // 1. Resolve the environment from System Properties or Default

            String currentEnv = System.getProperty(ApplicationConstants.ENV_KEY, ApplicationConstants.DEFAULT_ENV);

            // Normalize to handle case sensitivity (e.g., org62 vs ORG62)
            SalesforceConnectorUtil.Environment env = SalesforceConnectorUtil.Environment.valueOf(currentEnv.toUpperCase());

            // 2. Fetch the Connector Config (credentials are already injected into properties.json by ConfigUtilInitializer)
            ConnectorConfig config = SalesforceConnectorUtil.getConnectorConfig(env);

            if (config == null) {
                throw new RuntimeException("Login Fail: Could not find configuration for environment: " + env);
            }

            String username = config.getUsername();
            String password = config.getPassword();
            boolean isJenkins = System.getenv("JENKINS_HOME") != null || System.getenv("BUILD_ID") != null;


            try {
                // 4. BYPASS THE CACHE: Fetch URL directly from JSON
                // We do this because the static 'loginUrl' in the utility gets "stuck" after the first run
                String loginUrl = "";
                try {
                    loginUrl = SalesforceConnectorUtil.readProperties()
                            .getAsJsonObject("credentials")
                            .getAsJsonObject(env.name())
                            .get("testEnvironment")
                            .getAsString();

                    LOGGER.info("Successfully retrieved fresh URL for {}: {}", env, loginUrl);
                } catch (Exception e) {
                    LOGGER.warn("Direct JSON lookup failed, falling back to cached utility: {}", e.getMessage());
                    loginUrl = SalesforceConnectorUtil.getLoginUrl(env);
                }

                // 5. Navigate to Salesforce
                LOGGER.info("Navigating to login URL: {}", loginUrl);
                core.base.DriverManager.getDriver().get(loginUrl);
                if (!seleniumCommands.isDisplayed(By.id("username"))) {
                    if (getCurrentUrl().contains("lightning")) {
                        LOGGER.info("Already logged in (Lightning detected). Skipping login interaction.");
                        return;
                    }
                }

                // 6. Perform Login Interaction
                LOGGER.info("Logging into {} with user: {}", env, username);
                Login.getInstance().enterCredentialsAndLogin(username, password);

                // 7. Verification: Wait for Salesforce Lightning Home Page
                LOGGER.info("Step: Verifying login success...");
                seleniumCommands.waitForSfSpinnerToDisappear();
                boolean isLoggedin = waitFor(() -> {
                    // Check 1: URL should contain 'lightning'
                    boolean urlCorrect = getCurrentUrl().contains("lightning");

                    // Check 2: The profile avatar/button should be in the DOM
                    // This is more reliable than checking for the specific "View profile" span text
                    boolean elementPresent = !core.base.DriverManager.getDriver().findElements(
                            By.xpath("//button[contains(@class, 'branding-user-profile')] | //span[text()='View profile']")
                    ).isEmpty();


                    return urlCorrect && elementPresent;
                }, 20);
                core.base.WaitManager.waitForClickable(By.xpath("//span[text()='View profile']//ancestor::button"));
                if (isLoggedin) {
                    LOGGER.info("SUCCESS: User '{}' is logged into {} successfully.", config.getUsername(), env);
                } else {
                    throw new RuntimeException("Login failed: Home page not reached. Current URL: " + getCurrentUrl());
                }

            } catch (Exception e) {
                LOGGER.error("FAILURE: Login process failed for environment {}. Details: {}", env, e.getMessage());
                throw new RuntimeException("Login Fail: " + e.getMessage());
            }
        }



        public void SelectOptionFromMenu(String value) {
            try {
                core.base.WaitManager.waitForVisible(genericActionsHelper.gearIcon);
                seleniumCommands.clickWithJavaScript(genericActionsHelper.gearIcon);
                By option = seleniumCommands.dynamicXpath(value, genericActionsHelper.setUpOption);
                core.base.WaitManager.waitForVisible(option);
                seleniumCommands.clickWithJavaScript(option);
                seleniumCommands.switchToTab("Second");
                core.base.WaitManager.waitForVisible(genericActionsHelper.quickSearch);
            } catch (Exception e) {
                System.out.print("Exception: " + e.getMessage());
            }
        }



        public void loginByProfileName(String profile) {
            String currentEnv = System.getProperty(ApplicationConstants.ENV_KEY, ApplicationConstants.DEFAULT_ENV);
            LOGGER.info("Current environment resolved to: {}", currentEnv);

            // 2. Read properties and safely navigate JSON
            JsonObject root = SalesforceConnectorUtil.readProperties();

            // Validate credentials block exists
            if (!root.has("credentials")) {
                throw new RuntimeException("The 'credentials' block is missing from properties.json");
            }

            JsonObject envNode = root.getAsJsonObject("credentials").getAsJsonObject(currentEnv.toUpperCase());

            if (envNode == null) {
                throw new RuntimeException("Environment '" + currentEnv + "' not found under 'credentials' in properties.json");
            }

            // 3. Resolve Environment Enum and Profiles
            SalesforceConnectorUtil.Environment env = SalesforceConnectorUtil.Environment.valueOf(currentEnv.toUpperCase());
            JsonObject profiles = envNode.getAsJsonObject("profiles");

            if (profiles == null || !profiles.has(profile)) {
                throw new RuntimeException("Profile '" + profile + "' not found in " + currentEnv + " configuration.");
            }

            // 4. Fetch User ID via API (credentials are already injected into properties.json by ConfigUtilInitializer)
            String username = profiles.get(profile).getAsString();
            core.base.TestContext.store("loggedInUsername", username);
            LOGGER.info("Attempting to log in as profile: {} (Username: {})", profile, username);

            String userId = SalesforceApiUtil.getObjectId("User", "Username", username, env);

            if (userId == null || userId.isEmpty()) {
                throw new RuntimeException("Could not find Salesforce User ID for username: " + username);
            }

            try {
                refreshPage();
                SalesforceUiUtil.getInstance().navigateToObject(userId, getHomepageUrl());
                seleniumCommands.waitForSfSpinnerToDisappear();
                waitFor(() -> getCurrentUrl().contains(userId), 15);
    // Now perform the interaction
                SalesforceUiUtil.getInstance().loginAsUser(userId, getCurrentUrl());
            } catch (Exception e) {
                refreshPage();
                seleniumCommands.waitForSfSpinnerToDisappear();
                if (!seleniumCommands.isDisplayed(By.xpath("//a[contains(text(),'Log out as')]"))) {
                    SalesforceUiUtil.getInstance().logInAsUserFromUserRecordPage(userId);
                }
            }

            LOGGER.info("Successfully logged in as {}", profile);
        }




        public void navigateToSetupAndOpenSetting(String gearOptionType, String settingToOpen) throws Exception {
            SelectOptionFromMenu(gearOptionType);
            seleniumCommands.enterText(genericActionsHelper.quickSearch, settingToOpen);
            By searchSetting = seleniumCommands.dynamicXpath(settingToOpen, genericActionsHelper.pathByTitle);
            core.base.WaitManager.waitForVisible(searchSetting);
            seleniumCommands.scrollToElement(searchSetting);
            seleniumCommands.click(searchSetting);
        }

     */
    public void verifyEyeIcon() throws Exception {
        core.base.WaitManager.waitForVisible(By.xpath("//button[@title ='Toggle password visibility to change Press enter or space']"));
    }

    public void verifyPasswordIsMasked(String text) throws Exception {
        core.base.WaitManager.waitForVisible(By.xpath("//label[text()='" + text + "']/following-sibling::div/input[@type='password']"));

    }

    public void verifypasswordgetsUnmasked(String text) throws Exception {
        seleniumCommands.click(By.xpath("//button[@title ='Toggle password visibility to change Press enter or space']"));
        core.base.WaitManager.waitForVisible(By.xpath("//label[text()='" + text + "']/following-sibling::div/input[@type='text']"));

    }

    public void verifypasswordgetsmasked(String text) throws Exception {
        seleniumCommands.click(By.xpath("//button[@title ='Toggle password visibility to change Press enter or space']"));
        core.base.WaitManager.waitForVisible(By.xpath("//label[text()='" + text + "']/following-sibling::div/input[@type='password']"));

    }

    public void valdiateURL(String URL) throws Exception {
        String currentURL = seleniumCommands.getCurrentUrl();
        System.out.println("URL--->" + currentURL);
        if (!currentURL.contains(URL))
            throw new Exception("Help button is not navigating to the mentioned URL");
    }

    public void valdiateObjInApplication(String obj, String app) throws Exception {
        By moreXpath = By.xpath("//*[text()='More']/ancestor::one-app-nav-bar-menu-button");
        By objXpath = By.xpath("//*[text()='" + obj + "']/ancestor::one-app-nav-bar-menu-item");
        seleniumCommands.click(moreXpath);
        seleniumCommands.click(objXpath);
    }

}