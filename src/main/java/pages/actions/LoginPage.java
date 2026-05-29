package pages.actions;

import api.SalesforceAPIClient;
import core.base.ConfigManager;
import core.base.LoggerManager;
import core.base.SeleniumCommands;
import core.base.WaitManager;
import core.sf.SalesforceCommands;
import core.sf.SalesforceWaitManager;
import objects.User;
import org.openqa.selenium.By;
import org.slf4j.Logger;
import pages.actionsHelper.GenericActionsHelper;
import utils.GenericJavaUtils;

import java.time.Duration;

/**
 * LoginPage — handles all login, logout, and Login-As operations.
 */
public class LoginPage {

    private static final Logger LOGGER = LoggerManager.getLogger(LoginPage.class);

    // ── Dependencies ──────────────────────────────────────────────────────────
    private final SeleniumCommands sc;
    private final SalesforceCommands sfCommand;
    private final User user;

    // ── Locators ──────────────────────────────────────────────────────────────
    private final By usernameLocator  = By.id("username");
    private final By passwordLocator  = By.id("password");
    private final By loginBtnLocator  = By.id("Login");
    private final By otpPageLocator   = By.xpath("//h2[text()='Verify Your Identity']");
    private final By otpInputLocator  = By.xpath("(//label[text()=\"Verification Code\"]//following::input)[1]");
    private final By verifyBtnLocator = By.xpath("//input[@value=\"Verify\"]");
    private final By logoutAs         = By.xpath("//a[contains(text(), 'Log out as')]");

    private static final int SESSION_COOKIE_TIMEOUT_SECONDS = 30;

    // ── Constructor ───────────────────────────────────────────────────────────

    public LoginPage() {
        this.sc        = new SeleniumCommands();
        this.sfCommand = new SalesforceCommands();
        this.user      = new User();
    }

    // ── Basic Login ───────────────────────────────────────────────────────────

    /**
     * Types the username into the Salesforce login username field.
     *
     * @param username Salesforce login username
     */
    public void enterUserName(String username) {
        sc.sendKeys(usernameLocator, username);
    }

    /**
     * Types the password into the Salesforce login password field.
     *
     * @param pass Salesforce login password
     */
    public void enterPassword(String pass) {
        sc.sendKeys(passwordLocator, pass);
    }

    /**
     * Clicks the Login button and handles MFA if the OTP screen appears.
     */
    public void clickOnLogin() {
        sc.click(loginBtnLocator);

        // Short pause for MFA redirect to begin rendering
        WaitManager.pause(Duration.ofMillis(500), "Waiting for post-login redirect");

        // Handle MFA / OTP screen if it appears
        if (sc.isElementPresent(otpPageLocator)) {
            LOGGER.info("MFA screen detected — entering OTP.");
            String mfaKey = ConfigManager.getProperty("mfa.key");
            sc.sendKeys(otpInputLocator, GenericJavaUtils.generateOTP(mfaKey));
            sc.click(verifyBtnLocator);
        }
        LOGGER.info("Waiting for Salesforce session cookie after login...");
        boolean cookieReady = WaitManager.waitUntil(
                "Salesforce sid cookie after login",
                Duration.ofSeconds(SESSION_COOKIE_TIMEOUT_SECONDS),
                Duration.ofMillis(500),
                () -> {
                    try {
                        var sid = core.base.DriverManager.getDriver()
                                .manage()
                                .getCookieNamed("sid");
                        return sid != null && !sid.getValue().isEmpty();
                    } catch (Exception e) {
                        return false;
                    }
                });

        if (!cookieReady) {
            LOGGER.warn("Session cookie not detected within {}s — login may have failed or been delayed.",
                    SESSION_COOKIE_TIMEOUT_SECONDS);
        } else {
            LOGGER.info("Session cookie confirmed — login complete.");
        }
    }

    public void logOutAsUser() {
        sc.safeClick(logoutAs);
    }



    /**
     * Logs in as a specific user using the legacy UI search flow.
     *
     * @param profile Exact Salesforce username or display name to search for
     */
    public void loginAsUserWithUser_Name(String profile) throws Exception {
        sfCommand.navigateToSetup();
        String username=resolveUsername(profile);
        sfCommand.searchAndOpenSetupRecord("Users", username);
        sfCommand.clickSetupActionButtonOnDetailsPage("Login");
        sfCommand.refreshPage();
        SalesforceWaitManager.waitForLightningRender();
        WaitManager.waitForVisible(logoutAs);
    }

    /**
     * Logs in as the user identified by the given alias using the API-driven flow.
     *
     * @param profile Logical alias defined in config (e.g. "Employee", "ES_agent", "admin")
     */
    public void loginAsUserWithUserName(String profile) throws Exception {
        LOGGER.info("=== Login As User: alias='{}' ===", profile);

        // Step 1: resolve alias → username
        String username = resolveUsername(profile);

        // Step 2 & 3: fetch UserId + build Login-As URL
        //   User.resolveLoginAsUrl() wraps both steps for convenience,
        //   but we keep the steps explicit here for traceability.
        String userId       = user.fetchUserId(username);
        //String instanceUrl  = user.getInstanceUrl();   // FIXED: was sc.getCurrentUrl()
       // String loginAsUrl   = user.buildLoginAsUrl(instanceUrl, userId);

        LOGGER.info("Navigating directly to Login-As URL for alias '{}'", profile);

        // Step 4: navigate directly — no manual Setup search needed
        //sfCommand.navigateTo(loginAsUrl);

        // Step 5: click Login in the classic iframe
        sfCommand.clickSetupActionButtonOnDetailsPage("Login");

        // Step 6: confirm Login-As is active
        WaitManager.waitForVisible(logoutAs);
        LOGGER.info("=== Login As '{}' completed successfully ===", profile);
    }



    /**
     * Logs in as a user and opens a specific Lightning Experience Community site.
     *
     * @param profile  Logical alias defined in config (e.g. "Employee")
     * @param siteName Display name of the Experience Cloud site to open
     */
    public void loginAsUserAndOpenLightningSite(String profile, String siteName) throws Exception {
        sfCommand.navigateToSetup();
        sfCommand.openSetupMenuItemFromQuickFind("All Sites");
        sc.duplicateCurrentTab();
        loginAsUserWithUser_Name(profile);
        sc.switchToOriginalTab();
        sfCommand.openRecordFromSetupTable(siteName);
        sc.switchToNewTab();
    }

    // ── Private Helpers ───────────────────────────────────────────────────────

    /**
     * Resolves a logical alias to a Salesforce username from config.
     *
     * Config file example:
     *   Employee=ajais@mindzcloud.com.mzone.mzoneqa
     *   ES_agent=nchudiwale@mindzcloud.com.mzone.mzoneqa
     *   admin=lkhobragade@mindzcloud.com.mzone.mzoneqa
     *
     * The alias key matches the config key directly (case-sensitive).
     *
     * @param profile Config key / alias for the user
     * @return Resolved Salesforce username string
     * @throws RuntimeException if the alias is not defined in config
     */
    private String resolveUsername(String profile) {
        String username = ConfigManager.getProperty(profile, "");
        if (username.isEmpty()) {
            throw new RuntimeException(
                    "No username configured for alias '" + profile + "'. " +
                            "Add '" + profile + "=<salesforce_username>' to your properties file.");
        }
        LoggerManager.info(LOGGER, "Resolved alias '{}' → username '{}'", profile, username);
        return username;
    }
}