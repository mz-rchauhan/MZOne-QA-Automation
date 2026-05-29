package stepDefinitions;


import core.base.ConfigManager;
import core.base.DriverManager;
import core.base.TestContext;
import core.sf.SalesforceCommands;
import io.cucumber.datatable.DataTable;
import io.cucumber.java.PendingException;
import io.cucumber.java.en.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pages.actions.GenericActions;
import utils.ExcelUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class GenericSFSteps {
    private static final Logger LOGGER = LoggerFactory.getLogger(GenericSFSteps.class);

    private SalesforceCommands sfCommand= new SalesforceCommands();
    private GenericActions genericActions=new GenericActions();

    @And("Navigate to the Setup page")
    public void navigateToSetup() throws Exception {
        sfCommand.navigateToSetup();
    }


    @When("Search the {string} from setup menu and open the {string} record")
    public void searchTheFromSetupMenuAndOpenTheRecord(String setupMenuName, String recordName) {
        try {

            sfCommand.searchAndOpenSetupRecord(setupMenuName, recordName);

        } catch (Exception e) {
            throw new RuntimeException("Cucumber Step Failed: Could not navigate to " + recordName + " under " + setupMenuName, e);
        }
    }

    /**
     * Verifies the current active segment in the Salesforce Path UI.
     */
    @Then("Validate the record path status should be {string}")
    public void verifyRecordPathStatus(String expectedStatus) {
        try {
            sfCommand.verifyCurrentPathStatus(expectedStatus);
        } catch (AssertionError ae) {
            LOGGER.error("Path Status Mismatch: {}", ae.getMessage());
            throw ae;
        }
    }

    @Then("Verify that user change the record owner to {string} sucessfully")
    public void verifyRecordOwnerChangeSuccess(String newOwnerName) throws Exception {
        LOGGER.info("Step: Verifying successful owner change to {}", newOwnerName);
        // Passing the Global Map ensures the captured name is stored for later steps
        sfCommand.changeOwnerSuccessfully(newOwnerName);

    }


    @When("Click the {string} action button on the setup details page")
    public void ClickActionButtonOnSetupDetailsPage(String buttonName) {
        System.out.println("Executing Step: Clicking action button '" + buttonName + "'");
        try {
            sfCommand.clickSetupActionButtonOnDetailsPage(buttonName);
        } catch (Exception e) {
            throw new RuntimeException("Cucumber Step Failed: Could not click button " + buttonName, e);
        }
    }

    @When("Search for {string} in Setup Quick Find and open the {string} record")
    public void searchForInQuickFindAndOpenRecord(String setupMenuName, String recordName) {

        try {
            // Call the wrapper method we created that handles the search, click, iframe, and record selection
            sfCommand.openSetupMenuItemFromQuickFind(setupMenuName);
            sfCommand.openRecordFromSetupTable(recordName);

        } catch (Exception e) {
            throw new RuntimeException("Cucumber Step Failed: Could not navigate to " + recordName + " under " + setupMenuName, e);
        }
    }
    @And("Search for the {string} object with name {string}")
    public void SearchForTheObjectWithName(String objName, String recordName) throws Exception {



        if (recordName != null &&
                recordName.toLowerCase().startsWith("test context:")) {

            String contextKey = recordName.split(":", 2)[1].trim();

            LOGGER.info("Resolving Test Context value for key '{}'", contextKey);

            recordName = TestContext.retrieveString(contextKey);

            LOGGER.info("Resolved Test Context key '{}' to value '{}'",
                    contextKey, recordName);

            if (recordName == null || recordName.trim().isEmpty()) {

                throw new RuntimeException(
                        "No value found in Test Context for key: " + contextKey
                );
            }
        }
        genericActions.searchRecord(objName, recordName);
    }

    @Then("verify following fields are visible on the page:")
    public void verifyFieldsVisible(DataTable dt) {
        List<String> fields = extractFieldColumn(dt);
        LOGGER.info("Step: assertFieldsVisible — {} fields", fields.size());
        sfCommand.assertFieldsVisible(fields);
    }

    @Then("verify following fields are not visible on the page:")
    public void verifyFieldsNotVisible(DataTable dt) {
        List<String> fields = extractFieldColumn(dt);
        LOGGER.info("Step: assertFieldsNotVisible — {} fields", fields.size());
        sfCommand.assertFieldsNotVisible(fields);
    }


    @Then("verify following field values on the page:")
    public void verifyFieldValues(DataTable dt) {
        LOGGER.info("Step: assertFieldValues — {} rows", dt.asMaps().size());
        List<Map<String, String>> list = dt.asMaps(String.class, String.class);

        if (list == null || list.isEmpty()) {
            throw new IllegalArgumentException("The input DataTable for filling details cannot be null or empty.");
        }
        sfCommand.assertFieldValues(list);
    }

    @Then("validate layout fields from sheet {string} for object {string}")
    public void validateFromSheet(String sheetName, String profile) {
        LOGGER.info("Step: validateFromSheet — sheet='{}' object='{}'", sheetName, profile);
        String filepath = "src/test/resources/data/TestData.xlsx";

        List<HashMap<String, String>> excelList = new ExcelUtils().readData(filepath, profile);
        List<Map<String, String>> mappedList = new ArrayList<>(excelList);
        sfCommand.assertFieldValues(mappedList);
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Private helpers
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Extracts the "Field" column from a single-column DataTable.
     * Strips the header row automatically.
     */
    private List<String> extractFieldColumn(DataTable dt) {
        return dt.asList()
                .stream()
                .filter(f -> !f.equalsIgnoreCase("Field"))
                .map(String::trim)
                .filter(f -> !f.isEmpty())
                .collect(Collectors.toList());
    }
}
