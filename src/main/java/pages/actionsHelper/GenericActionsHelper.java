package pages.actionsHelper;


import org.openqa.selenium.By;


public class GenericActionsHelper {


    public static By APP_LAUNCHER_ICON_HOME_PAGE = By.xpath("(//div[@class='slds-icon-waffle'])[last()]");
    public static By VIEW_ALL_BUTTON = By.xpath("//button[text()='View All']");
    public static By SEARCH_APPS_OR_ITEMS_BOX = By.xpath("//input[contains(@placeholder,'Search apps or items...')]");
    public static By GLOBAL_SEARCH_BOX = By.xpath("//div[@data-aura-class='forceSearchAssistant']//button[@aria-label='Search']");
    public static By SALEFORCE_ICON_NAVIGATE_TO_HOME = By.xpath("//span/img[@title='User']");
    public static By RECORD_CLOSE_ICON = By.xpath("//div[contains(@class,'close slds-col--bump-left')]/button/lightning-primitive-icon");


    /**
     * XPath locator for Google LLC Account in global search results.
     * This covers multiple Salesforce Lightning search result layouts:
     * 1. Instant search results (dropdown)
     * 2. Global search results page (list view)
     * 3. Search results table
     */
    public static By GOOGLE_LLC_ACCOUNT_SEARCH_RESULT = By.xpath(
            "(//a[@title='Google LLC'])[1] | " +
                    "(//a[contains(@title,'Google LLC') and @data-refid='recordId'])[1] | " +
                    "(//span[text()='Google LLC']/ancestor::a[@data-refid='recordId'])[1] | " +
                    "(//lightning-formatted-text[text()='Google LLC']/ancestor::a)[1] | " +
                    "(//table[contains(@class,'slds-table')]//tbody//tr//th//a[contains(text(),'Google LLC')])[1] | " +
                    "(//ul[contains(@class,'listbox')]//li//a[contains(@title,'Google LLC')])[1] | " +
                    "(//div[contains(@class,'instant-result')]//a[contains(text(),'Google LLC')])[1]"
    );

    /**
     * Returns a dynamic XPath locator for a specific account name in global search results.
     * This method handles multiple Salesforce Lightning search result layouts.
     *
     * @param accountName The account name to search for (e.g., "Google LLC")
     * @return By locator for the account in search results
     */
    public static By getAccountInGlobalSearchResult(String accountName) {
        return By.xpath(
                "(//a[@title='" + accountName + "'])[1] | " +
                        "(//a[contains(@title,'" + accountName + "') and @data-refid='recordId'])[1] | " +
                        "(//span[text()='" + accountName + "']/ancestor::a[@data-refid='recordId'])[1] | " +
                        "(//lightning-formatted-text[text()='" + accountName + "']/ancestor::a)[1] | " +
                        "(//table[contains(@class,'slds-table')]//tbody//tr//th//a[contains(text(),'" + accountName + "')])[1] | " +
                        "(//ul[contains(@class,'listbox')]//li//a[contains(@title,'" + accountName + "')])[1] | " +
                        "(//div[contains(@class,'instant-result')]//a[contains(text(),'" + accountName + "')])[1]"
        );
    }

    /**
     * Returns XPath locator for instant search results (dropdown suggestions).
     *
     * @param accountName The account name to search for
     * @return By locator for instant search result
     */
    public static By getInstantSearchResult(String accountName) {
        return By.xpath(
                "(//div[contains(@class,'instant-result')]//a[contains(text(),'" + accountName + "')])[1] | " +
                        "(//ul[contains(@class,'listbox')]//li//a[contains(@title,'" + accountName + "')])[1] | " +
                        "(//a[@data-refid='recordId'][contains(@title,'" + accountName + "')])[1] | " +
                        "(//forceSearch-instant-result-item//a[contains(@title,'" + accountName + "')])[1]"
        );
    }

    /**
     * Returns XPath locator for global search results page (after pressing Enter).
     *
     * @param accountName The account name to search for
     * @return By locator for search results page
     */
    public static By getGlobalSearchPageResult(String accountName) {
        return By.xpath(
                "(//a[@title='" + accountName + "'])[1] | " +
                        "(//a[contains(@title,'" + accountName + "') and @data-refid='recordId'])[1] | " +
                        "(//span[text()='" + accountName + "']/ancestor::a[@data-refid='recordId'])[1] | " +
                        "(//lightning-formatted-text[text()='" + accountName + "']/ancestor::a)[1]"
        );
    }

    public static By BUTTON(String button) {
        By btn;
        if (button.equalsIgnoreCase("Next"))
            btn = By.xpath("//button[@aria-label='Next'] | (//*[not(contains(@class,'slds-hide'))]//button[text()=\"" + button + "\"]) | //button[text()='Next']");
        else if (button.equalsIgnoreCase("Previous") | button.equalsIgnoreCase("Certifications"))
            btn = By.xpath("//button[@aria-label='" + button + "'] | //button[text()='" + button + "']");
        else if (button.equalsIgnoreCase(("Save All")))
            btn = By.xpath("(//*[text()='Save All'])[1]");
        else
            btn = By.xpath(
                    "(" +
                            "//*[not(contains(@class,'slds-hide'))]//button//*[normalize-space()='" + button + "']" +
                            " | " +
                            "//*[not(contains(@class,'slds-hide'))]//button[normalize-space()='" + button + "']" +
                            " | " +
                            "//button[normalize-space()='" + button + "']" +
                            " | " +
                            "//*[normalize-space()='" + button + "']/parent::*[@class='slds-radio_button__label']" +
                            " | " +
                            "//div[@title='" + button + "']" +
                            " | " +
                            "//button[@title='" + button + "']" +
                            " | " +
                            "//span[normalize-space()='" + button + "']" +
                            ")[last()]"
            );
        System.out.println(btn);
        return btn;
    }

    public static By hyperLink(String link) throws Exception {
        By xpath = By.xpath("//a[contains(text(),'" + link + "')]| //a//*[text()='" + link + "'] | //a[text()='" + link + "'] | (//span[contains(text(),'" + link + "')])[1] | //a[@title='" + link + "'] | //button[text()='" + link + "'] | //span[normalize-space(text())='" + link + "'] | //span[text()='" + link + "']//parent::span[@title='" + link + "'] | //a/span/slot[contains(text(),'" + link + "')]");
        return xpath;
    }

    public static By checkBoxXpathBackend(String checkBox) {
        By xpath;
        xpath = By.xpath("(//*[text()='" + checkBox + "']//preceding::input)[last()]");
        return xpath;
    }

    /**
     * Returns dynamic XPath for toast messages.
     *
     * @param toastType Type of toast: "success", "error", "warning", "info"
     * @return By locator for the toast element
     */
    public static By toast(String toastType) {
        String xpath = String.format("//div[@data-key='%s']//*[contains(@class,'toastMessage')]", toastType);
        return By.xpath(xpath);
    }

    public static By closeBtnOnToast(String toastType) {
        String xpath = String.format("//div[@data-key='%s'and @data-aura-class='forceToastMessage']//*[contains(@aria-label,'Close')]", toastType);
        return By.xpath(xpath);
    }


    public static By textFieldXpath_Last(String textFieldName) {
        By xpath = By.xpath("//label[text()=\"" + textFieldName + "\"]//..//following-sibling::td//input | (//*[text()=\"" + textFieldName + "\"]/../..//following-sibling::div//input)[last()] | //label[text()=\"" + textFieldName + "\"]//ancestor::div//textarea | //span//div[text()=\"" + textFieldName + "\"]//ancestor::div//textarea");
        return xpath;
    }

    public static By textFieldXpath_LastSecond(String textFieldName) {
        By xpath = By.xpath("//label[text()=\"" + textFieldName + "\"]//..//following-sibling::td//input | (//*[text()=\"" + textFieldName + "\"]/../..//following-sibling::div//input)[last()-1] | //label[text()=\"" + textFieldName + "\"]//ancestor::div//textarea | //span//div[text()=\"" + textFieldName + "\"]//ancestor::div//textarea");
        return xpath;
    }

    public static By ICON(String checklistName, String iconName) {
        By btn;
        btn = By.xpath("(//*[text()=\"" + checklistName + "\"]//ancestor::tr//following-sibling::td)[last()]//span[text()='" + iconName + "'] | (//*[text()=\"" + checklistName + "\"]//ancestor::tr//following-sibling::td)[last()]//button[@title='" + iconName + "']");
        return btn;
    }


    public static By requiredTextFieldXpath(String textFieldName) {
        By xpath = By.xpath("(//label//abbr[@title='Required']//following-sibling::span[text()=\"" + textFieldName + "\"]//following::input[@class='vlocity-input slds-input'])[1]");
        return xpath;
    }


    public static By FillDropdownXpath(String fieldName) throws InterruptedException {
        By xpath = By.xpath("(//*[contains(text(),\"" + fieldName + "\")]/following::div//select)[1]");
        System.out.println(xpath);
        return xpath;
    }

    public static By FillDropdownXpathBackend(String fieldName) {
        By xpath = By.xpath("//span[contains(text(),'" + fieldName + "')]/..//following-sibling::div//descendant::a");

        return xpath;
    }

    public static By text(String txt) {
        if (txt.contains("\"")) {

            By textXpath = By.xpath("//*[text()='" + txt + "'] | //*[contains(text(),'" + txt + "')]");
            return textXpath;
        } else {
            By textXpath = By.xpath("//*[text()=\"" + txt + "\"] | //*[contains(text(),\"" + txt + "\")]");
            return textXpath;
        }

    }

    public static By relationshipsText(String txt) {
        By textXpath = By.xpath("(//*[contains(text(),\"" + txt + "\")])[last()]");
        return textXpath;
    }

    public static By link(String link, String docname) {

        By textXpath = By.xpath("//*[text()='" + docname + "']//following::a[text()='" + link + "']");
        return textXpath;
    }

    public static By viewFormSF(String txt) {

        By xpath = By.xpath("//*[@class='notifications']//lightning-formatted-rich-text[normalize-space()=\"" + txt + "\"] | //h1[text()='View Form']/ancestor::div[@class='slds-modal__container']//*[text()=\"" + txt + "\"]");
        return xpath;
    }

    public static By collapsedSection(String txt) {

        By xpath = By.xpath("//*[text()='" + txt + "']/ancestor::button[@aria-expanded='false']");
        return xpath;
    }

    public static By bellIcon(String text) {

        By xpath = By.xpath("//li[contains(@class,'unread') or contains(@class,'read')]//div[@class='notification-content']//*[contains(text(),'" + text + "')]");
        return xpath;
    }


    public static By textHavingDoubleText(String txt) {
        By xpath = By.xpath("//*[contains(text(),'" + txt + "')]");
        return xpath;
    }

    public static By phoneFieldInput(String field) {
        By xpath = By.xpath("//vlocity_ins-omniscript-telephone//*[text()='" + field + "']/..//following-sibling::div//input");
        return xpath;
    }

    public static By locatorLink(String link) {
        By linkLocator = By.xpath("//a[contains(text(),'" + link + "')] | //a//*[text()='" + link + "'] | //*[text()='" + link + "'] | (//*[@class='action-template']//*[text()='" + link + "'])[last()]");
        return linkLocator;
    }

    public static By checkbox(String link) {
        By checkbox;
        checkbox = By.xpath("//input[@type='checkbox' and @value=\"" + link + "\"]" +
                "| //*[text()=\"" + link + "\"]/parent::label/preceding-sibling::input[@type='checkbox']");
        return checkbox;
    }


    public static By getActualPicklistValues(String field) {
        By values = By.xpath("//label[contains(text(),'" + field + "')]/following-sibling::select/option");
        return values;
    }


    public static By picklistField(String field) {
        By values = By.xpath("//label[contains(text(),'" + field + "')]/following-sibling::select");
        return values;
    }

    public static By verifyExactText(String txt) {
        By textXpath = By.xpath("//*[text()=\"" + txt + "\"]");
        return textXpath;
    }


    public static By clickButton(String record, String button) {
        By btn = By.xpath("//*[normalize-space()='" + record + "']/parent::tr//following-sibling::td//button//div" +
                "[normalize-space()='" + button + "']");
        return btn;
    }


    public static By appLinkAppLauncher(String appName) {
        String xpath;
        if (appName.contains("--HelpText"))
            xpath = "(//*[(text()='" + appName.split("--")[0] + "')])/preceding-sibling::a";
        else if (appName.contains("Cases"))
            xpath = "(//*[text()='App Launcher'])[last()]//following::*[text()='" + appName + "']//ancestor::a";
        else
            xpath = "(//*[(text()='" + appName + "')])//ancestor::a";
        By appLink = By.xpath(xpath);
        return appLink;
    }

    public static By objectLinkAppLauncher(String appName) {
        By appLink = By.xpath("//mark[text()='" + appName + "']");
        return appLink;
    }


    public static By linkContains(String link) {
        By xpath = By.xpath("//span[contains(text(),'" + link + "')] | //th//a[contains(text(),'" + link + "')]");
        return xpath;
    }


    public static By Lightning_TAB(String tabName) {
        By tab = By.xpath("(//a[text()='" + tabName + "']) |(//*[text()='" + tabName + "'])");
        return tab;
    }


    public static By fieldLabel(String fieldName) {
        By xpath = By.xpath("//*[text()='" + fieldName + "']");

        return xpath;
    }

    public static By pencilIconPresenceAbsence(String fieldName) {
        By xpath = By.xpath("//*[text()='" + fieldName + "']/ancestor::dd//button[contains(@title,'Edit')]");
        return xpath;
    }

    public static By fieldLabel_absence(String fieldName) {
        By xpath = By.xpath("//omnistudio-omniscript-text[@class='slds-p-right_small slds-m-bottom_xx-small slds-show_inline-block slds-size_12-of-12 slds-medium-size_12-of-12']//*[text()='" + fieldName + "']");
        return xpath;
    }

    public static By requiredFieldLabel(String fieldName) {
        By xpath = By.xpath("//*[text()='" + fieldName + "']//ancestor::omnistudio-omniscript-text//abbr[@title='Required']");
        return xpath;
    }

    public static By question(String fieldName) {
        By xpath = By.xpath("//*[text()=\"" + fieldName + "\"]/ancestor::omnistudio-omniscript-radio[@class = \"slds-p-right_small slds-m-bottom_xx-small slds-show_inline-block slds-size_12-of-12 slds-medium-size_12-of-12\"]");
        return xpath;
    }

    public static By textFieldXpath(String textFieldName) {
        // We construct the multi-part XPath, ensuring EVERY branch uses the textFieldName
        String complexXpath =
                // 1. Classic table layout
                "//label[contains(text(),'" + textFieldName + "')]/parent::*//following-sibling::td//input" +
                        // 2. Generic div layout (taking the first input)
                        " | (//*[contains(text(),'" + textFieldName + "')]//parent::*/parent::div//following::input)[1]" +
                        // 3. Textarea with label
                        " | //label[contains(text(),'" + textFieldName + "')]//ancestor::div//textarea" +
                        // 4. Textarea with span/div label
                        " | //span/div[contains(text(),'" + textFieldName + "')]//ancestor::div//textarea" +
                        // 5. OmniStudio block (Added //input so it actually types into the field)
                        " | //*[contains(text(),'" + textFieldName + "')]//ancestor::omnistudio-omniscript-text-block//input" +
                        // 6. Lightning file upload
                        " | (//*[contains(text(),'" + textFieldName + "')]//ancestor::lightning-file-upload//following::input)[1]" +
                        // 7. OmniStudio text (FIXED: Tied this to the specific label/text!)
                        " | //*[contains(text(),'" + textFieldName + "')]//following-sibling::omnistudio-omniscript-text//input";

        return By.xpath(complexXpath);
    }


    public static By textxpath(String textFieldName) {
        By xpath = By.xpath("//span[text()=\"" + textFieldName + "\"]//following::input");
        return xpath;
    }


    public static By NumberFieldXpath(String textFieldName) {
        By xpath = By.xpath("(//label[text()=\"" + textFieldName + "\"]//..//following-sibling::td//input | //*[text()=\"" + textFieldName + "\"]/../..//following-sibling::div//input//parent::div)[last()]");
        return xpath;


    }

    public static By dateFieldXpathSpecific(String dateFieldName) {
        By xpath;

        xpath = By.xpath("(//*[contains(text(),'" + dateFieldName + "')]//following::input)[1]");
        return xpath;
    }

    public static By dateField(String dateFieldName) {
        By xpath = By.xpath("(//*[text()='" + dateFieldName + "'])[last()]/..//following-sibling::*//input");
        return xpath;
    }

    public static By requiredFields(String fieldName) {
//        By xpath = By.xpath("//label//*[text()='" + fieldName + "']//preceding-sibling::abbr[text()='*']");
        By xpath = By.xpath("//label//*[text()='" + fieldName + "']//preceding-sibling::abbr[text()='*'] | //*[text()" +
                "='" + fieldName + "']//*[text()='*']|//*[text()='" + fieldName + "']//preceding-sibling::abbr[text()='*']|//*[text()='" + fieldName + "']//*[contains(text(),'** Required')]");
        return xpath;
    }

    public static By textFieldInSection(String textField, String sectionName) {

        return By.xpath("//*[text()='" + sectionName + "']//ancestor::div//vlocity_ins-omniscript-text[@data-omni-key='" + textField + "'] |" +
                "//*[text()='" + sectionName + "']//ancestor::div//vlocity_ins-omniscript-text//*[text()='" + textField + "'] ");
    }

    public static By linkageReviewRelationship(String fieldName, String value) {

        By xpath = By.xpath("(//*[text()='" + fieldName + "']//ancestor::td)[last()]//following-sibling::td//div[text()='" + value + "']");
        return xpath;
    }

    public static By documentsAbsence(String fieldName, String sectionName) {

        By xpath = By.xpath("(//*[text()='" + sectionName + "'])[last()]//following::*[text()='" + fieldName + "']");
        return xpath;
    }

    public static By dropdownFieldInSection(String dropdownField, String sectionName) {
        By dropdownXpath = By.xpath("//*[text()='" + sectionName + "']//ancestor::div//vlocity_ins-omniscript-select[@data-omni-key='" + dropdownField + "']");
        return dropdownXpath;
    }

    public static By phoneFieldInSection(String phone, String section) {
        By phoneXpath = By.xpath("//*[text()='" + section + "']//ancestor::div//*[text()='" + phone + "']");
        return phoneXpath;
    }

    public static By emailFieldInSection(String email, String section) {
        By emailXpath = By.xpath("//*[text()='" + section + "']//ancestor::div//*[text()='" + email + "']");
        return emailXpath;
    }

    public static By radioButtonFieldInSection(String text, String section) {
        By emailXpath = By.xpath("//*[text()='" + section + "']//following::div//*[text()='" + text + "']");
        return emailXpath;
    }

    public static By fillTextFieldOfSection(String fieldName, String section) {
        By xpath;
        xpath = By.xpath("(//*[contains(text(),\"" + section + "\")])[last()]//following::*[text()='" + fieldName + "']/following-sibling::*//input | (//*[contains(text(),'" + section + "')])[last()]//following::div[@id='card-body']//*[text()='" + fieldName + "']//following::div//input | (//*[contains(text(),'" + section + "')])[last()]//following::*[text()='" + fieldName + "']/ancestor::c-input//input | (//*[contains(text(),'" + section + "')])[last()]//following::*[text()='" + fieldName + "']/../../following-sibling::*//input |(//*[contains(text(),'" + section + "')])[last()]//following::*[text()='" + fieldName + "']/../../following-sibling::*//textarea");
        return xpath;
    }

    public static By fillTextareaFieldOfSection(String fieldName, String section) {
        By xpath = By.xpath("(//*[text()='" + section + "'])//following::*[text()='" + fieldName + "']//following::textarea" + "| //*[text()=\"" + section + "\"]/ancestor::omnistudio-omniscript-radio/following-sibling::omnistudio-omniscript-textarea//*[text()=\"" + fieldName + "\"]/../following-sibling::*//textarea");
        return xpath;

    }


    //Check on the last xpath
    public static By readOnlyField(String fieldName) {
        By xpath;
        xpath = By.xpath("//*[text()='" + fieldName + "']//following::*[@aria-invalid='false']" + "| //*[text()='" + fieldName + "']/parent::*//input[@disabled]" + "| //*[text()='" + fieldName + "']/parent::*//button[@disabled]" +
                "| //*[text()='" + fieldName + "']/ancestor::omnistudio-omniscript-telephone[@aria-disabled]" + "| //*[text()='" + fieldName + "']/ancestor::div/following-sibling::div//input  |  //*[contains(text(),'" + fieldName + "')]//parent::div//input |//*[text()='" + fieldName + "']/ancestor::c-input//child::input[@aria-readonly=\"true\"]| //label[contains(text(),'" + fieldName + "')]/following-sibling::div//button/span | //*[contains(text(),'" + fieldName + "')]//parent::div | //*[text()='" + fieldName + "']//following::input");
        return xpath;
    }

    public static By readOnlyFieldSection(String section, String fieldName) {
        By xpath;
        xpath = By.xpath("//*[text()=\"" + section + "\"]/ancestor::omnistudio-omniscript-text-block/following-sibling::omnistudio-omniscript-text[@aria-readonly=\"true\"]//*[text()=\"" + fieldName + "\"]/../../following-sibling::div//input" +
                "| //*[text()=\"" + section + "\"]/ancestor::omnistudio-omniscript-text-block/following-sibling::omnistudio-omniscript-select[@aria-readonly=\"true\"]//*[text()=\"" + fieldName + "\"]/../../following-sibling::div//input" +
                "| //*[text()=\"" + section + "\"]/following-sibling::div//omnistudio-omniscript-text[@aria-readonly=\"true\"]//*[text()=\"" + fieldName + "\"]/../../following-sibling::div//input " +
                "| //*[text()=\"" + section + "\"]/following-sibling::div//omnistudio-omniscript-select[@aria-readonly=\"true\"]//*[text()=\"" + fieldName + "\"]/../../following-sibling::div//input[@readonly] | //*[text()=\"" + section + "\"]//following::*[@class='slds-text-title slds-truncate' and @title=\"" + fieldName + "\"]");

        return xpath;
    }

    public static By fillDropdownFieldOfSection(String fieldName, String section) {
        By xpath;
        xpath = By.xpath("//*[text()=\"" + section + "\"]//ancestor::div//*[@data-omni-key='" + fieldName + "']//ancestor::label/..//following-sibling::div//input" +
                "| //*[text()=\"" + section + "\"]//following-sibling::div//vlocity_ins-omniscript-select//*[text()='" + fieldName + "']//ancestor::*[@data-label='true']//following-sibling::div//input | " +
                "//*[text()=\"" + section + "\"]//ancestor::div//*[@data-omni-key='" + fieldName + "']//ancestor::label/..//following-sibling::div//input | " +
                "//*[text()=\"" + section + "\"]/following::*[text()=\"" + fieldName + "\"]/../..//following-sibling::div//input/..|//*[text()=\"" + section + "\"]/following::*[text()=\"" + fieldName + "\"]/../..//following-sibling::div//button");
        System.out.println(xpath);
        return xpath;
    }


    public static By textarea(String fieldName) {
        By xpath;
        xpath = By.xpath("//*[text()='" + fieldName + "']//parent::div//following-sibling::div/textarea|//*[text()='" + fieldName + "']//parent::label//ancestor::vlocity_ins-omniscript-textarea[not(contains(@class,'slds-hide'))]//following-sibling::div/textarea" +
                "| //*[text()='" + fieldName + "']/ancestor::omnistudio-omniscript-text-block/following-sibling::omnistudio-omniscript-textarea//textarea|//*[text()='" + fieldName + "']//parent::label/following-sibling::textarea | //*[text()='" + fieldName + "']//following::div//textarea");
        return xpath;
    }

    public static By textUsingPlaceholder(String fieldName) {
        By xpath = By.xpath("//input[@placeholder = '" + fieldName + "'] | //div[@data-placeholder = '" + fieldName + "']");
        System.out.println(xpath);
        return xpath;
    }

    public static By lookupUsingPlaceholder(String fieldName) {
        By xpath = By.xpath("//input[contains(@placeholder, '" + fieldName + "')]");
        return xpath;
    }

    public static By lookUpXpath(String fieldName) {
        By xpath = By.xpath("(//*[text()='" + fieldName + "']/../..//following::div//input[@aria-autocomplete='list'])[1]");
        return xpath;
    }

    public static By checkBox(String checkBox) {
        By xpath = By.xpath("//*[text()='" + checkBox + "']//preceding-sibling::span | //span[text()='" + checkBox + "']/..//preceding-sibling::input | " +
                "//*[contains(text(),'" + checkBox + "')]//ancestor::*//preceding-sibling::vlocity_ins-omniscript-checkbox//input");
        return xpath;
    }

    public static By checkBoxXpath(String checkBox) {
        By xpath;
        xpath = By.xpath("(//*[text()=\"" + checkBox + "\"]//preceding-sibling::span)[last()] |" +
                "//*[contains(text(),\"" + checkBox + "\")]//ancestor::*//preceding-sibling::vlocity_ins-omniscript-checkbox//input |" +
                " (//*[contains(text(),'" + checkBox + "')]/..//following-sibling::input)[last()]");

        System.out.println(xpath);
        return xpath;
    }

    public static By checkBoxXpathAttestation(String checkBox) {
        By xpath;
        xpath = By.xpath("//*[contains(text(),\"" + checkBox + "\")]//preceding-sibling::span |" +
                "//*[contains(text(),\"" + checkBox +
                "\")]//ancestor::*//preceding-sibling::vlocity_ins-omniscript-checkbox//input | (//*[contains(text(),'" + checkBox + "')]/..//following-sibling::input)[last()]");
        return xpath;
    }

    public static By checkBoxXpathSpecific(String checkBox) {
        By xpath;
        xpath = By.xpath("(//*[text()=\"" + checkBox + "\"]//preceding-sibling::span)[last()]");
        return xpath;
    }

    public static By radioButton(String fieldName, String value) {

        By xpath;
        xpath = By.xpath("//*[contains(normalize-space(text()),'" + fieldName + "')]//parent::div/following::label[contains(text(),'" + value + "')]/preceding-sibling::input[@type='radio']");

        return xpath;
    }


    public static By radioButtonLast(String fieldName, String value) {
        By xpath;
        xpath = By.xpath("//*[contains(text(),'" + fieldName + "')]//following::*[text()='" + value + "']");

        return xpath;
    }


    public static By radioWithApostrophe(String fieldName, String value) {
        By xpath;

        xpath = By.xpath("//*[contains(text(),'" + fieldName + "')]//following::*[text()='" + value + "']");

        return xpath;
    }


    public static By header(String headerValue) {
        By xpath;

        xpath = By.xpath("//h1//*[text()='" + headerValue + "']  | //h1[text()='" + headerValue + "']  | " +
                "//h1[text()=\"" + headerValue + "\"]");
        return xpath;
    }

    public static By selectPickListValueForQuestion(String pickList, String question) {
        By xpath = null;
        xpath = By.xpath("//label[text()='" + question + "']//following-sibling::div//button//following::span[text()='" + pickList + "']");
        return xpath;
    }

    public static By selectPickListValueForSpecificQuestion(String section, String fieldName, String value) {
        By xpath = By.xpath("//*[text()='" + section + "']//following::*[text()='" + fieldName + "']/../..//following-sibling::div//li//*[text()='" + value + "']" +
                "| (//*[contains(text(),'" + section + "')]//following::*[text()='" + fieldName + "'])/../..//following-sibling::div//*[text()='" + value + "']");
        return xpath;
    }

    public static By selectPickListValueForSpecificQues(String section, String fieldName, String value) {
        By xpath = By.xpath("(//*[text()='" + section + "'])[last()]//following::*[text()='" + fieldName + "']//following::*[text()='" + value + "']");
        return xpath;
    }

    public static By selectPiclistValue(String picklist) {
        By xpath = By.xpath("//li//span[text()='" + picklist + "']/../parent::div");
        return xpath;
    }

    public static By newPageVerification(String pgName) {
        By xpath = By.xpath("(//*[text()=\"" + pgName + "\"])[last()]//following::tbody//a/parent::*[@data-navigation='enable']");
        return xpath;
    }

    public static By picklistValues(String field, String value) {
        By xpath;

        xpath = By.xpath("(//*[text()=\"" + field + "\"])[last()]/../../following-sibling::div//*[text()=\"" + value + "\"]" + "| (//*[text()=\"" + field + "\"])[last()]/following-sibling::div//*[text()=\"" + value + "\"]" + "| //*[text()=\"" + field + "\"]/following-sibling::select//*[text()=\"" + value + "\"] | (//label//span[text()='" + field + "']//ancestor::omnistudio-omniscript-select[@data-omni-key='Modpr_AliasType'])[last()]//following::div//ul//li//span[text()=\"" + value + "\"] | //label[text()=\"" + field + "\"]/ancestor::lightning-picklist//lightning-base-combobox-item//*[text()='" + value + "']");

        return xpath;
    }

    public static By section(String section_Name) {
        By xpath = By.xpath("//*//strong[normalize-space(text())='" + section_Name + "'] | //*//strong[contains(text(),'" + section_Name + "')]" +
                "| //section[@class=\"slds-modal slds-fade-in-open lpi-modal\"]//*[text()=\"" + section_Name + "\"]");
        return xpath;
    }


    public static By Logout_Link = By.xpath("//*[text()='Logout']");


    public static By listItem(String itemInList) {
        By xpath = By.xpath("//button//following-sibling::*[text()='" + itemInList + "']");
        return xpath;
    }

    public static By getRichTextXpath(String label) {
        By xpath;

        // Use short-circuit OR (||) and search for unique keywords
        // This ensures that even if the label was swapped for Dreamforce, it hits the right 'following' logic
        if (label.contains("outcomes") || label.contains("ACV impact") || label.contains("additional context")) {
            // Use normalize-space to ignore extra spaces, newlines, or invisible characters in the UI
            xpath = By.xpath("(//*[contains(normalize-space(text()), '" + label + "')]/following::lightning-input-rich-text)[1]");
        } else {
            xpath = By.xpath("(//*[contains(normalize-space(text()), '" + label + "')]/preceding::lightning-input-rich-text)[1]");
        }

        return xpath;
    }

    public static By linkOfSection(String link, String Section) {
        By xpath = By.xpath("//*[text()='" + Section + "']//ancestor::vlocity_ins-output-field/..//following-sibling::div//span[text()='" + link + "']");
        return xpath;
    }

    public static By checkThePageCheckbox(String pageName) {
        By xpath = By.xpath("//*[text()='Qualifications Attestation']//following-sibling::div//vlocity_ins-omniscript-checkbox//label//span");
        return xpath;
    }


    public static By questionxpath(String fieldName, String data) {
        By xpath = By.xpath("//*[contains(text(),\"" + fieldName + "\")]//following::*[text()=\"" + data + "\"]//preceding-sibling::*");
        return xpath;
    }

    public static By dropdownElement(String picklistValue, String fieldName) {
        By xpath;

        xpath = By.xpath("(//li//*[contains(text(),\"" + picklistValue + "\")] | //option[text()=\"" + picklistValue + "\"] | //*[text()=\"" + picklistValue + "\"] | //option[contains(text(),\"" + picklistValue + "\")])[last()]");

        return xpath;
    }


    public static By getFieldValue(String field) {
        By xpath;

        xpath = By.xpath("(//*[text()='" + field + "']/ancestor::omnistudio-omniscript-text//input)[1]" +
                "| (//*[text()='" + field + "']/ancestor::omnistudio-omniscript-email//input)[1]" +
                "| (//*[text()='" + field + "']/ancestor::omnistudio-omniscript-formula//input)[1]" +
                "| (//*[text()='" + field + "']/ancestor::omnistudio-omniscript-select//input)[1]" +
                "| (//*[text()='" + field + "']/ancestor::omnistudio-omniscript-date//input)[1]" +
                "| (//*[text()='" + field + "']/ancestor::omnistudio-omniscript-checkbox//input)[1]" +
                "| (//*[text()='" + field + "']/ancestor::omnistudio-omniscript-telephone//input)[1]" +
                "| (//*[text()='" + field + "']/ancestor::omnistudio-omniscript-radio//input)[1]" +
                "| (//*[text()=\"" + field + "\"]/ancestor::lightning-input//input)[1]" +
                "| //*[contains(text(),'" + field + "')]/parent::div//input | //label[contains(text(),'" + field + "')]/following-sibling::div//button/span" +
                "|(//*[text()='" + field + "'])[last()]/ancestor::omnistudio-output-field/following::span" +
                "| //*[text()='" + field + "']/../following-sibling::div//span[@class=\"uiOutputDate\"]");
        System.out.println(xpath);
        return xpath;
    }

    public static By tab(String tab) {
        By xpath;
        if (tab.contains("--tab")) {
            tab = tab.split("--")[0];
            xpath = By.xpath("(//a[text()='" + tab + "'])[last()] | (//a//*[text()='" + tab + "'])[last()]");
        } else
            xpath = By.xpath("(//span[@class='title' and text()='" + tab + "']) | (//*[text()='" + tab + "'])[last()]");
        System.out.println(xpath);
        return xpath;
    }

    public static By detailPageTab(String tab) {
        By xpath = By.xpath("//*[@data-page-type='RecordPage']//a[text()=\"" + tab + "\"]");
        System.out.println(xpath);
        return xpath;
    }

    public static By buttonSF(String name) {
        By xpath = xpath = By.xpath("(//button//*[text()=\"" + name + "\"]) | //button[text()=\"" + name + "\"]");
        return xpath;
    }

    public static By checkBoxXpathLast(String checkBox) {
        By xpath = By.xpath("//*[contains(text(),\"" + checkBox + "\")]//preceding::span[@class='slds-checkbox_faux']");
        return xpath;
    }

    public static By answerToQuestionSectionXpath(String ans, String ques, String sec) {
        By xpath;
//        xpath = By.xpath(" //*[text()=\""+sec+"\"]//ancestor::*/following-sibling::*//*[text()=\""+ques+"\"]//following-sibling::div//input[@value=\""+ans+"\"]");
        xpath = By.xpath("(//*[text()='" + sec + "'])[last()]//following::omnistudio-omniscript-radio//span[text()='" + ques + "']//following::div//input[@value='" + ans + "'] | (//*[text()='" + sec + "'])[last()]//following::*[text()='" + ques + "']//following::input[@value='" + ans + "']");
        return xpath;

    }

    public static By selectOptionToQuestionSectionDropdownXpath(String option, String ques, String sec) {
        By xpath;
        xpath = By.xpath("//strong[text()=\"" + sec + "\"]//ancestor::omnistudio-omniscript-text-block/following-sibling::omnistudio-omniscript-select//*[text()=\"" + ques + "\"]/../../following-sibling::div//input");
        return xpath;
    }

    public static By selectOptionToQuestionSectionDropdownValueXpath(String option, String ques, String sec) {
        By xpath;
        xpath = By.xpath("//strong[text()=\"" + sec + "\"]//ancestor::omnistudio-omniscript-text-block/following-sibling::omnistudio-omniscript-select//*[text()=\"" + ques + "\"]/../../following-sibling::div//li//*[text()=\"" + option + "\"]");
        return xpath;
    }

    public static By buttonPresenceOrAbsence(String button) {
        By xpath;
        if (button.equalsIgnoreCase("Previous") | button.equalsIgnoreCase("Request Approval"))
            xpath = By.xpath("//button[@aria-label='" + button + "'] | //button[text()=\"" + button + "\"]");
        else
            xpath = By.xpath("//*[text()=\"" + button + "\"]/ancestor::omnistudio-omniscript-remote-action[@class = \"slds-p-right_small slds-m-bottom_xx-small slds-show_inline-block slds-size_12-of-12 slds-medium-size_3-of-12\"]");
        return xpath;
    }

    public static By sectionAbsence(String section_Name) {
        By xpath = By.xpath("//section[@class=\"slds-p-right_small slds-m-bottom_xx-small slds-show_inline-block slds-size_12-of-12 slds-medium-size_12-of-12\"]//*[text()=\"" + section_Name + "\"]");
        return xpath;
    }

    public static By sectionHeaderAbsence(String section_Name) {
        By xpath = By.xpath("//*[@class=\"slds-p-right_small slds-m-bottom_xx-small slds-show_inline-block slds-size_12-of-12 slds-medium-size_12-of-12\"]//*[text()=\"" + section_Name + "\"]");
        return xpath;
    }

    public static By readonlyfieldabsence(String field_name) {
        By xpath = By.xpath("//*[text()=\"" + field_name + "\"]/ancestor::div[@class='slds-grid']/following-sibling::div//input | //*[text()='First Name']/ancestor::fieldset//following-sibling::div[@class='slds-radio']");
        System.out.println(xpath);
        return xpath;
    }

    public static By dropdownFieldForSection(String section, String field) {
        By xpath;

        xpath = By.xpath("//*[contains(text(),\"" + section + "\")]/ancestor::omnistudio-omniscript-text-block/following::*//*[text()=\"" + field + "\"]/ancestor::c-modpr-override-address-type//lightning-icon");
        return xpath;
    }

    public static By dropdownFieldForSectionValue(String section, String field, String value1, String value2) {
        By xpath;
        if (section.contains("License Address") | section.contains("Contact At Address")) {
            xpath = By.xpath("//*[contains(text(),\"" + section + "\")]/ancestor::omnistudio-omniscript-text-block/following::*//*[text()=\"" + field + "\"]/ancestor::c-modpr-override-address-type//*[contains(text(),\"" + value1 + "\") and contains(text(),\"" + value2 + "\")]");
        } else
            xpath = By.xpath("//*[contains(text(),\"" + section + "\")]/ancestor::omnistudio-omniscript-text-block/following::*//*[text()=\"" + field + "\"]/ancestor::omnistudio-omniscript-select//*[contains(text(),\"" + value1 + "\") and contains(text(),\"" + value2 + "\")]");
        return xpath;
    }

    public static By pencilIcon(String checklistName, String iconName) {
        By btn = By.xpath("//*[text()=\"" + checklistName + "\"]//ancestor::tr//following-sibling::td//lightning-icon[@title='" + iconName + "']");
        return btn;
    }


    public static By appStatusXpath(String appNumber, String status) {
        By xpath;
        xpath = By.xpath("//*[text()=\"" + appNumber + "\"]/..//*[text()=\"" + status + "\"] | //*[text()='" + appNumber + "']//ancestor::td//following-sibling::td//*[text()='" + status + "']");
//        xpath = By.xpath("//*[text()='"+appNumber+"']//ancestor::div[contains(@class,'slds-col table-row slds-size')]//div//omnistudio-output-field//span[@title='"+status+"']");
        return xpath;
    }


    public static By clickOnLink(String object, String role) {
        By xpath = By.xpath("(//*[text()='" + role + "']//ancestor::*[@data-label ='Roles']//preceding::*[@data-label='Business Relationships Name'])[last()]//span[contains(text(),'" + object + "')]");
        return xpath;
    }


    public static By tableHeader(String table_Header) {
        By label;
        label = By.xpath("(//th//span[contains(text(),'" + table_Header + "')])[last()]|//th[contains(text(),'" + table_Header + "')] | (//th//*[text()='" + table_Header + "'])[last()]");
        return label;
    }

    public static By nextButtonXpath(String buttonName) {
        By btn = By.xpath("//button[@aria-label='" + buttonName + "']");
        return btn;
    }

    public static By saveAndNextButtonXpath(String buttonName) {
        By btn = By.xpath("//span[text()='" + buttonName + "']");
        return btn;
    }

    public static By textOnDocumentsPage(String section, String txt) {
        By xpath = By.xpath("//*[text()='" + section + "']//following::*[text()='" + txt + "']");
        return xpath;
    }

    public static By textSection(String section) {
        By xpath = By.xpath("//h1[text()='" + section + "'] | //h2[text()='" + section + "']");
        return xpath;
    }

    public static By nonMandatoryFields(String fieldName) {
        By xpath = By.xpath("//*[text()='" + fieldName + "']//*[@title ='required']");
        return xpath;
    }

    public static By linkageReviewSection(String section, String fieldName) {
        By xpath = By.xpath("//*[text()='" + section + "']/parent::div[@class='slds-text-heading_small']/following-sibling::div[1]//th/div[text()='" + fieldName + "']");
        return xpath;
    }


    public static By linkageReviewbuttons(String section, String btn) {
        By xpath = By.xpath("//*[text()='" + section + "']/parent::div[contains(@class,'slds-text-heading_small')]/following-sibling::div[1]//td//button[text()='" + btn + "']");
        if (section.contains("--Due Diligence")) {
            section = section.split("--")[0];
            xpath = By.xpath("//*[text()='" + section + "']/ancestor::tr//button[@part='button button-icon']");
        }
        return xpath;
    }


    public static By linkageReviewStatus(String section, String status) {
        By xpath = By.xpath("//*[text()='" + section + "']/parent::div[contains(@class,'slds-text-heading_small')]/following-sibling::div[1]//td//button//span[text()='" + status + "']");
        return xpath;
    }

    public static By fieldsOnDetailPage(String Fieldname) {
        By xpath;

            xpath = By.xpath(
                    "(" +

                            "//*[normalize-space(text())=\"" + Fieldname + "\"]" +
                            "/ancestor::*[contains(@class,'slds-form-element')]" +
                            "//lightning-formatted-text" +

                            " | " +

                            "//*[normalize-space(text())=\"" + Fieldname + "\"]" +
                            "/ancestor::*[contains(@class,'slds-form-element')]" +
                            "//lightning-formatted-number" +

                            " | " +

                            "//*[normalize-space(text())=\"" + Fieldname + "\"]" +
                            "/ancestor::*[contains(@class,'slds-form-element')]" +
                            "//a[contains(@class,'outputLookupLink')]" +

                            ")[last()]"
            );

        return xpath;
    }

    public static By radioButtonXpath(String radioButton) {
        By textXpath = By.xpath("//*[text()='" + radioButton + "']//ancestor::omnistudio-omniscript-radio[not(contains(@class,'slds-hide'))]");
        return textXpath;
    }


    public By quickSearch = By.xpath("//*[contains(@class,'onesetupSetupNavTree')]//input");
    public By pathByTitle = By.xpath("//*[@title='%s']");
    public By gearIcon = By.xpath("//*[contains(@class,'forceHeaderMenuTrigger')]//a[contains(@class,'setup')]");
    public By setUpOption = By.xpath("//*[contains(@class,'uiMenuList')]//a[@title='%s']");

    public static By selectOptionFromDropdownToQuestion(String question) {
        By xpath;
        xpath = By.xpath("//*[text()='" + question + "']/../..//following-sibling::div//input");
        return xpath;
    }

    public static By selectLawfulPickListValueForQuestion(String pickList, String question) {
        By xpath = null;
        xpath = By.xpath("//*[text()='" + question + "']/../..//following-sibling::div//input//following::div//ul//*[text()='" + pickList + "']");
        return xpath;
    }

    public static By selectVisaOptionFromDropdownToQuestion(String question) {
        By xpath;
        xpath = By.xpath("//omnistudio-omniscript-block[@data-omni-key='StudentVisaBlock']//*[text()='" + question + "']/../..//following-sibling::div//input");
        return xpath;
    }

    public static By selectVisaPickListValueForQuestion(String pickList, String question) {
        By xpath = null;
        xpath = By.xpath("//omnistudio-omniscript-block[@data-omni-key='StudentVisaBlock']//*[text()='" + question + "']/../..//following-sibling::div//input//following::div//*[text()='" + pickList + "']");
        return xpath;
    }

    public static By selectVisaBlockOptionFromDropdownToQuestion(String question) {
        By xpath;
        xpath = By.xpath("//omnistudio-omniscript-block[@data-omni-key='VisaBlock']//*[text()='" + question + "']/../..//following-sibling::div//input");
        return xpath;
    }


}
