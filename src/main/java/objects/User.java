package objects;

import api.SalesforceAPIClient;
import core.base.LoggerManager;
import org.slf4j.Logger;

public class User {
    private static final Logger LOGGER = LoggerManager.getLogger(User.class);

    private  SalesforceAPIClient sfAPIClient;

    public SalesforceAPIClient getSfAPIClient() {

        if (sfAPIClient == null) {
            sfAPIClient = new SalesforceAPIClient();
        }

        return sfAPIClient;
    }


    /**
     * Fetches Salesforce UserId for a given username via SOQL.
     */
    public String fetchUserId(String username) {
        String soql = String.format("SELECT Id FROM User WHERE Username = '%s' AND IsActive = true LIMIT 1", username);
        String userId = sfAPIClient.queryRecordId(soql);
        if (userId == null || userId.isEmpty()) {
            throw new RuntimeException("No active Salesforce user found for username: " + username);
        }
        LoggerManager.info(LOGGER, "Fetched UserId '{}' for username '{}'", userId, username);
        return userId;
    }


    /**
     * Builds the Salesforce Lightning Setup URL to Login As a specific user.
     * Format: <instanceUrl>/lightning/setup/ManageUsers/page?address=/<userId>%3Freturn%3DloginAs
     */
    public String buildLoginAsUrl(String instanceUrl,String userId) {
        String sanitizedUrl = instanceUrl.endsWith("/")
                ? instanceUrl.substring(0, instanceUrl.length() - 1)
                : instanceUrl;

        String url = sanitizedUrl +
                "/lightning/setup/ManageUsers/page?address=/" +
                userId;

        LoggerManager.info(LOGGER, "Built User Detail URL: {}", url);

        return url;
    }

    /**
     * Builds a direct Lightning record view URL.
     * Format: <instanceUrl>/lightning/r/<ObjectName>/<RecordId>/view
     */
    public String buildRecordUrl(String instanceUrl, String objectName, String recordId) {
        return instanceUrl.replaceAll("/$", "") +
                "/lightning/r/" + objectName + "/" + recordId + "/view";
    }

    /**
     * Builds a Lightning Setup page URL.
     * Format: <instanceUrl>/lightning/setup/<SetupPage>/home
     */
    public String buildSetupUrl(String instanceUrl, String setupPage) {
        return instanceUrl.replaceAll("/$", "") +
                "/lightning/setup/" + setupPage + "/home";
    }
}

