package stepDefinitions;

import core.base.ConfigManager;
import core.base.DriverManager;
import core.sf.SalesforceCommands;
import io.cucumber.java.PendingException;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import pages.actions.LoginPage;

public class LoginStep {
    private LoginPage loginPage = new LoginPage();

    @Given("user navigate to login page")
    public void user_navigate_to_login_page() {
        DriverManager.getDriver().get(ConfigManager.getProperty("org.url", "https://login.salesforce.com/"));
    }
    
    @When("user enter username from configuration")
    public void user_enter_username_config() {
        loginPage.enterUserName(ConfigManager.getProperty("admin.username", ""));
    }
    
    @When("user enter password from configuration")
    public void user_enter_password_config() {
        loginPage.enterPassword(ConfigManager.getProperty("admin.password", ""));
    }
    
    @When("click on login button")
    public void click_on_login_button() {
        loginPage.clickOnLogin();
    }
    
    @Then("user navigate to HomePage")
    public void user_navigate_to_home_page() {
    }
    @Given("Login as an System admin")
    public void loginAsAdmin()
    {
        DriverManager.getDriver().get(ConfigManager.getProperty("org.url", "https://login.salesforce.com/"));
        loginPage.enterUserName(ConfigManager.getProperty("admin.username", ""));
        loginPage.enterPassword(ConfigManager.getProperty("admin.password", ""));
        loginPage.clickOnLogin();
    }


    @When("Log in as {string} on the Lightning site")
    public void logInAsUserOnLightningSite(String userName) throws Exception {
        String siteName=ConfigManager.getProperty("org.siteURL", "");
        loginPage.loginAsUserAndOpenLightningSite(userName, siteName);
    }

    @When("Log in as user with username as {string}")
    public void logInAsUserusername(String profile) throws Exception {
        loginPage.loginAsUserWithUserName(profile);
    }

    @When("Log in as user with user name as {string}")
    public void logInAsUseruser_name(String profile) throws Exception {
        loginPage.loginAsUserWithUser_Name(profile);
    }

    @And("I log out as user")
    public void logOutAsUser() {
        loginPage.logOutAsUser();
    }
}
