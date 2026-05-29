package stepDefinitions;

import core.base.TestContext;
import core.sf.SalesforceCommands;
import io.cucumber.datatable.DataTable;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Then;
import pages.actions.GenericActions;

import java.util.List;
import java.util.Map;

public class GenericSteps {
    SalesforceCommands sfCommand = new SalesforceCommands();
    GenericActions genericAction=new GenericActions();

    @And("Click on the {string} link")
    public void clickOnTheLink(String name) throws Exception {
        try {
            genericAction.clickOnLink(name);
        } catch (Exception e) {
            String simpleFailureMessage = String.format(
                    "FAILURE: Not able to click the link '%s'. Details: %s",
                    name, e.getMessage());
            // 2. Re-throw the exception to fail the test, including the original error for the stack trace.
            throw new AssertionError(simpleFailureMessage, e);
        }
    }
    @And("Fill the below details :")
    public void fillBelowDetails(DataTable dt) throws Exception {
        List<Map<String, String>> list = dt.asMaps(String.class, String.class);

        if (list == null || list.isEmpty()) {
            throw new IllegalArgumentException("The input DataTable for filling details cannot be null or empty.");
        }
        genericAction.fillMandatoryDetails(list);
    }

    @And("Upload file in {string}")
    public void uploadFileForDocumentOnDocumentsPage(String fieldName) throws Throwable {
        genericAction.uploadFileOnDocumentsPage(fieldName);
        }

    @And("Click on {string} button")
    public void clickOnButton(String btnName) throws Exception {
        genericAction.click_on_button(btnName);
    }

    @Then("Validate following fields:")
    public void validateFollowingFields(DataTable dt) throws Exception {
        List<Map<String, String>> list = dt.asMaps(String.class, String.class);

        // Add validation check here
        if (list == null || list.isEmpty()) {
            throw new IllegalArgumentException("Validation DataTable cannot be null or empty.");
        }
       genericAction.verifyFieldValues(list);
    }

    @And("Switch to the {int} tab opened")
    public void userSwitchesToTab(int index) throws Exception {
        sfCommand.switchToTab(index);
    }
    @And("Refresh page")
    public void refreshPage()
    {
        sfCommand.refreshPage();
    }


}
