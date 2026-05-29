package stepDefinitions;

import core.base.TestContext;
import core.sf.SalesforceCommands;
import io.cucumber.java.en.*;
import pages.actions.CasesPage;
import pages.actions.GenericActions;

public class CaseSteps {

    SalesforceCommands sfCommand = new SalesforceCommands();
    CasesPage casePage=new CasesPage();
    GenericActions genericAction=new GenericActions();

    @Then("Store the {string} from confirmation message as {string}")
    public void storeCaseNumber(String key,String contextKey) {
        casePage.extractAndStoreCaseNumber(key, contextKey);
    }

    @Then("store the case record id from confirmation message as {string}")
    public void storeCaseRecordId(String contextKey) {
        casePage.extractAndStoreCaseRecordId(contextKey);
    }

    @Then("navigate to case record using stored id {string}")
    public void navigateToCaseByStoredId(String contextKey) throws Exception {
        String recordId = TestContext.retrieveString(contextKey);
        sfCommand.openRecord(recordId);
    }
}
