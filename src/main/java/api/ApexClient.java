package api;

import com.sforce.soap.apex.ExecuteAnonymousResult;
import com.sforce.soap.apex.LogCategory;
import com.sforce.soap.apex.LogCategoryLevel;
import com.sforce.soap.apex.LogInfo;
import com.sforce.soap.apex.LogType;
import com.sforce.soap.apex.SoapConnection;
import com.sforce.soap.partner.PartnerConnection;
import com.sforce.ws.ConnectionException;
import com.sforce.ws.ConnectorConfig;
import core.base.ConfigManager;
import core.base.LoggerManager;
import org.slf4j.Logger;

import java.util.ArrayList;
import java.util.List;

/**
 * ApexClient — Executes anonymous Apex and retrieves debug logs via SOAP.
 * Uses Salesforce WSC (force-wsc + force-apex-api + force-partner-api).
 *
 * Required pom.xml dependencies:
 *   com.force.api:force-wsc:59.0.0
 *   com.force.api:force-partner-api:59.0.0
 *   com.force.api:force-apex-api:59.0.0
 */
public class ApexClient {

    private static final Logger LOGGER = LoggerManager.getLogger(ApexClient.class);

    private final String username;
    private final String password;
    private final String authEndpoint;

    private PartnerConnection partnerConnection;
    private SoapConnection soapConnection;

    public ApexClient() {
        this.username     = ConfigManager.getProperty("admin.username", "");
        this.password     = ConfigManager.getProperty("admin.password", "");
        this.authEndpoint = ConfigManager.getProperty("sf.auth.endpoint",
                "https://test.salesforce.com/services/Soap/u/59.0");
    }

    public ApexClient(String username, String password, String authEndpoint) {
        this.username     = username;
        this.password     = password;
        this.authEndpoint = authEndpoint;
    }

    // ── Connection ────────────────────────────────────────────────────────────

    /**
     * Lazily initializes the Partner + Apex SOAP connections.
     * Synchronized to prevent race conditions in parallel test runs.
     */
    private synchronized void initConnections() throws ConnectionException {
        if (soapConnection != null) return;

        // Step 1: Partner login to obtain session
        ConnectorConfig partnerConfig = new ConnectorConfig();
        partnerConfig.setUsername(username);
        partnerConfig.setPassword(password);
        partnerConfig.setAuthEndpoint(authEndpoint);
        partnerConfig.setTraceMessage(false);
        partnerConfig.setPrettyPrintXml(true);

        partnerConnection = new PartnerConnection(partnerConfig);

        // Step 2: Build Apex SOAP connection using partner session
        ConnectorConfig apexConfig = new ConnectorConfig();
        apexConfig.setSessionId(partnerConnection.getSessionHeader().getSessionId());
        apexConfig.setServiceEndpoint(
                partnerConfig.getServiceEndpoint().replace("/Soap/u/", "/Soap/s/")
        );

        soapConnection = new SoapConnection(apexConfig);
        LoggerManager.info(LOGGER, "SOAP connections initialized successfully.");
    }

    // ── Public API ────────────────────────────────────────────────────────────

    /**
     * Executes an anonymous Apex block and returns true on success.
     */
    public boolean executeAnonymous(String apexCode) {
        try {
            initConnections();
            ExecuteAnonymousResult result = soapConnection.executeAnonymous(apexCode);
            if (result.isSuccess()) {
                LoggerManager.info(LOGGER, "Apex execution successful.");
                return true;
            }
            String error = result.getCompiled() ? result.getExceptionMessage() : result.getCompileProblem();
            LoggerManager.fail(LOGGER, "Apex execution failed: " + error);
            return false;
        } catch (ConnectionException e) {
            LoggerManager.error(LOGGER, "Connection failed during Apex execution.", e);
            return false;
        }
    }

    /**
     * Executes Apex containing System.debug() calls and returns the debug output lines.
     */
    public List<String> executeAndGetDebugLogs(String apexCode) {
        List<String> debugMessages = new ArrayList<>();
        try {
            initConnections();

            LogInfo[] logs = {new LogInfo()};
            logs[0].setCategory(LogCategory.Apex_code);
            logs[0].setLevel(LogCategoryLevel.Debug);
            soapConnection.setDebuggingHeader(logs, LogType.Debugonly);

            ExecuteAnonymousResult result = soapConnection.executeAnonymous(apexCode);
            if (result.isSuccess()) {
                String fullLog = soapConnection.getDebuggingInfo().getDebugLog();
                if (fullLog != null) {
                    for (String line : fullLog.split("\\R")) {
                        if (line.contains("|USER_DEBUG|")) {
                            String[] parts = line.split("\\|DEBUG\\|");
                            if (parts.length > 1) {
                                debugMessages.add(parts[1].trim());
                            }
                        }
                    }
                }
            } else {
                LoggerManager.fail(LOGGER, "Apex execution for debug logs failed.");
            }
        } catch (ConnectionException e) {
            LoggerManager.error(LOGGER, "Error retrieving Apex debug logs.", e);
        }
        return debugMessages;
    }
}
