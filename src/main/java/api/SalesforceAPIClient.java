package api;

import com.fasterxml.jackson.databind.ObjectMapper;
import core.base.ConfigManager;
import core.base.DriverManager;
import core.base.LoggerManager;
import io.restassured.RestAssured;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;
import org.openqa.selenium.Cookie;
import org.slf4j.Logger;

import java.util.List;
import java.util.Map;

/**
 * Salesforce REST API Client.
 * Handles all HTTP-level communication with the Salesforce REST API.
 * Token and instance URL are resolved from ConfigManager (config-driven, no hardcoding).
 */
public class SalesforceAPIClient {

    private static final Logger LOGGER = LoggerManager.getLogger(SalesforceAPIClient.class);
    private static final ObjectMapper MAPPER = new ObjectMapper();
    private static final String API_VERSION = ConfigManager.getProperty("sf.api.version", "v59.0");

    private final String accessToken;
    private final String instanceUrl;

    public SalesforceAPIClient() {
        Cookie sidCookie = DriverManager.getDriver().manage().getCookieNamed("sid");
        if (sidCookie == null) {
            throw new RuntimeException("Salesforce 'sid' cookie not found. Ensure login is complete before creating SalesforceAPIClient.");
        }
        this.accessToken = sidCookie.getValue();
        // Strip trailing slash to prevent double-slash in REST paths
        String rawUrl = ConfigManager.getProperty("org.url", "https://test.salesforce.com");
        this.instanceUrl = rawUrl.endsWith("/") ? rawUrl.substring(0, rawUrl.length() - 1) : rawUrl;
    }

    public SalesforceAPIClient(String accessToken, String instanceUrl) {
        this.accessToken = accessToken;
        this.instanceUrl = instanceUrl;
    }

    // ── Core Request Builder ──────────────────────────────────────────────────

    private RequestSpecification baseRequest() {
        return RestAssured.given()
                .baseUri(instanceUrl)
                .basePath("/services/data/" + API_VERSION)   // FIXED: was /lifecycle/data/
                .header("Authorization", "Bearer " + accessToken)
                .header("Content-Type", "application/json");
    }

    private RequestSpecification toolingRequest() {
        return RestAssured.given()
                .baseUri(instanceUrl)
                .basePath("/services/data/" + API_VERSION + "/tooling")
                .header("Authorization", "Bearer " + accessToken)
                .header("Content-Type", "application/json");
    }

    // ── SOQL ─────────────────────────────────────────────────────────────────

    public Response getRecordsByQuery(String soql) {
        Response response = baseRequest().queryParam("q", soql).get("/query/");
        LoggerManager.info(LOGGER, "SOQL Response: {} | Query: {}", response.statusCode(), soql);
        return response;
    }

    public List<Map<String, Object>> executeSOQL(String soql) {
        return getRecordsByQuery(soql).jsonPath().getList("records");
    }

    public String queryRecordId(String soql) {
        try {
            return getRecordsByQuery(soql).jsonPath().getString("records[0].Id");
        } catch (Exception e) {
            LoggerManager.warn(LOGGER, "No Id found for query: {}", soql);
            return null;
        }
    }

    // ── CRUD ─────────────────────────────────────────────────────────────────

    public Response getRecordById(String objectName, String recordId) {
        Response response = baseRequest().get("/sobjects/" + objectName + "/" + recordId);
        LoggerManager.info(LOGGER, "GET {}/{} → {}", objectName, recordId, response.statusCode());
        return response;
    }

    public String createRecord(String objectName, Map<String, ?> payload) {
        try {
            Response response = baseRequest().body(MAPPER.writeValueAsString(payload))
                    .post("/sobjects/" + objectName);
            LoggerManager.info(LOGGER, "CREATE {} → {}", objectName, response.statusCode());
            return response.jsonPath().getString("id");
        } catch (Exception e) {
            throw new RuntimeException("Failed to create record for: " + objectName, e);
        }
    }

    public Response updateRecord(String objectName, String recordId, Map<String, ?> payload) {
        try {
            Response response = baseRequest().body(MAPPER.writeValueAsString(payload))
                    .patch("/sobjects/" + objectName + "/" + recordId);
            LoggerManager.info(LOGGER, "UPDATE {}/{} → {}", objectName, recordId, response.statusCode());
            return response;
        } catch (Exception e) {
            throw new RuntimeException("Failed to update record: " + recordId, e);
        }
    }

    public Response deleteRecord(String objectName, String recordId) {
        Response response = baseRequest().delete("/sobjects/" + objectName + "/" + recordId);
        LoggerManager.info(LOGGER, "DELETE {}/{} → {}", objectName, recordId, response.statusCode());
        return response;
    }

    // ── Describe ─────────────────────────────────────────────────────────────

    public Response describeObject(String objectName) {
        return baseRequest().get("/sobjects/" + objectName + "/describe");
    }

    // ── Tooling ──────────────────────────────────────────────────────────────

    public Response toolingQuery(String soql) {
        return toolingRequest().queryParam("q", soql).get("/query/");
    }

    public Response executeAnonymousApex(String apexCode) {
        Response response = toolingRequest().queryParam("anonymousBody", apexCode)
                .get("/executeAnonymous/");
        if (response.statusCode() != 200) {
            LoggerManager.fail(LOGGER, "executeAnonymous failed: " + response.getBody().asString());
        }
        return response;
    }

    // ── Accessors ─────────────────────────────────────────────────────────────

    public String getInstanceUrl()  { return instanceUrl; }
    public String getAccessToken()  { return accessToken; }
    public String getApiVersion()   { return API_VERSION; }
}
