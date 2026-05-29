package observability;

import core.base.DriverManager;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chromium.HasCdp;
import org.slf4j.Logger;
import core.base.LoggerManager;

import java.util.*;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * NetworkMonitor — Captures network requests via Chrome DevTools Protocol
 */
public class NetworkMonitor {

    private static final Logger LOGGER = LoggerManager.getLogger(NetworkMonitor.class);
    private static final ConcurrentLinkedQueue<NetworkRequest> requests = new ConcurrentLinkedQueue<>();
    private static final Map<String, Long> latencyByEndpoint = new LinkedHashMap<>();
    private static boolean capturing = false;

    private NetworkMonitor() {
    }

    public static void startCapture() {
        try {
            WebDriver driver = DriverManager.getDriver();
            if (driver instanceof HasCdp) {
                HasCdp cdp = (HasCdp) driver;
                cdp.executeCdpCommand("Network.enable", new HashMap<>());
                capturing = true;
                LOGGER.info("[NetworkMonitor] CDP network capture started.");
            } else {
                LOGGER.warn("[NetworkMonitor] Browser does not support CDP. Network monitoring disabled.");
            }
        } catch (Exception e) {
            LOGGER.warn("[NetworkMonitor] startCapture failed: {}", e.getMessage());
        }
    }

    public static void stopCapture() {
        try {
            WebDriver driver = DriverManager.getDriver();
            if (driver instanceof HasCdp && capturing) {
                ((HasCdp) driver).executeCdpCommand("Network.disable", new HashMap<>());
                capturing = false;
                LOGGER.info("[NetworkMonitor] CDP network capture stopped. {} requests recorded.", requests.size());
            }
        } catch (Exception e) {
            LOGGER.warn("[NetworkMonitor] stopCapture failed: {}", e.getMessage());
        }
    }

    /**
     * Verify that a request matching the URL pattern returned the expected HTTP
     * status.
     *
     * @param urlPattern     Substring to match against request URL
     * @param expectedStatus Expected HTTP status code
     */
    public static void verifyResponseStatus(String urlPattern, int expectedStatus) {
        Optional<NetworkRequest> match = requests.stream()
                .filter(r -> r.url.contains(urlPattern))
                .findFirst();
        if (match.isPresent()) {
            int actual = match.get().statusCode;
            if (actual != expectedStatus) {
                LOGGER.error("[NetworkMonitor] Status mismatch for '{}': expected={} actual={}", urlPattern,
                        expectedStatus, actual);
                org.testng.Assert.fail(
                        "Network status mismatch: " + urlPattern + " expected=" + expectedStatus + " actual=" + actual);
            }
            LOGGER.info("[NetworkMonitor] Status verified: {} → {}", urlPattern, actual);
        } else {
            LOGGER.warn("[NetworkMonitor] No request found matching: '{}'", urlPattern);
        }
    }

    /**
     * Get the response body for the first request matching a URL pattern.
     */
    public static String inspectPayload(String urlPattern) {
        return requests.stream()
                .filter(r -> r.url.contains(urlPattern))
                .map(r -> r.responseBody)
                .findFirst()
                .orElse("");
    }

    /**
     * Get all captured requests.
     */
    public static List<NetworkRequest> getRequests() {
        return new ArrayList<>(requests);
    }

    /**
     * Record a request (called by CDP event listener hook in framework bootstrap).
     */
    public static void recordRequest(String url, int statusCode, long latencyMs, String responseBody) {
        requests.add(new NetworkRequest(url, statusCode, latencyMs, responseBody));
        latencyByEndpoint.put(url, latencyMs);
    }

    public static long getLatencyForEndpoint(String endpointFragment) {
        return latencyByEndpoint.entrySet().stream()
                .filter(e -> e.getKey().contains(endpointFragment))
                .mapToLong(Map.Entry::getValue)
                .findFirst()
                .orElse(-1L);
    }

    public static class NetworkRequest {
        public final String url;
        public final int statusCode;
        public final long latencyMs;
        public final String responseBody;

        public NetworkRequest(String url, int statusCode, long latencyMs, String responseBody) {
            this.url = url;
            this.statusCode = statusCode;
            this.latencyMs = latencyMs;
            this.responseBody = responseBody;
        }
    }
}
