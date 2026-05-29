package observability;

import core.base.ConfigManager;
import core.base.DriverManager;
import org.openqa.selenium.JavascriptExecutor;
import org.slf4j.Logger;
import core.base.LoggerManager;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * PerformanceProfiler — Measures and reports execution timing.
 */
public class PerformanceProfiler {

    private static final Logger LOGGER = LoggerManager.getLogger(PerformanceProfiler.class);
    private static final int MAX_MAP_SIZE = 100;
    private static final long SLOW_THRESHOLD_MS = ConfigManager.getLong("performance.slow.threshold.ms", 5000L);

    private static final ThreadLocal<Map<String, Long>> timers = ThreadLocal.withInitial(LinkedHashMap::new);
    private static final ConcurrentHashMap<String, Long> stepDurations = new ConcurrentHashMap<>();
    private static final ConcurrentHashMap<String, Long> scenarioDurations = new ConcurrentHashMap<>();

    private PerformanceProfiler() {
    }

    // ==========================================
    // Manual Timers
    // ==========================================

    public static void startTimer(String label) {
        timers.get().put(label, System.currentTimeMillis());
        LOGGER.trace("[Perf] Timer started: '{}'", label);
    }

    public static long stopTimer(String label) {
        Long start = timers.get().remove(label);
        if (start == null)
            return 0L;
        long elapsed = System.currentTimeMillis() - start;
        LOGGER.debug("[Perf] Timer '{}' = {}ms", label, elapsed);
        return elapsed;
    }

    // ==========================================
    // Recording
    // ==========================================

    public static void recordStepDuration(String stepText, long durationMs) {
        if (stepDurations.size() >= MAX_MAP_SIZE) {
            String firstKey = stepDurations.keySet().iterator().next();
            stepDurations.remove(firstKey);
        }
        stepDurations.put(stepText, durationMs);
        if (durationMs > SLOW_THRESHOLD_MS) {
            LOGGER.warn("[Perf] Slow step ({}ms > threshold {}ms): '{}'", durationMs, SLOW_THRESHOLD_MS, stepText);
        }
    }

    public static void recordScenarioDuration(String scenarioName, long durationMs) {
        if (scenarioName != null) {
            if (scenarioDurations.size() >= MAX_MAP_SIZE) {
                String firstKey = scenarioDurations.keySet().iterator().next();
                scenarioDurations.remove(firstKey);
            }
            scenarioDurations.put(scenarioName, durationMs);
        }
    }

    // ==========================================
    // Page Load (Navigation Timing API)
    // ==========================================

    public static long getPageLoadTime() {
        try {
            JavascriptExecutor js = (JavascriptExecutor) DriverManager.getDriver();
            Object result = js.executeScript(
                    "return window.performance.timing.loadEventEnd - window.performance.timing.navigationStart;");
            long loadMs = result instanceof Long ? (Long) result : Long.parseLong(result.toString());
            LOGGER.info("[Perf] Page load time: {}ms", loadMs);
            return loadMs;
        } catch (Exception e) {
            LOGGER.warn("[Perf] Could not measure page load time: {}", e.getMessage());
            return -1;
        }
    }

    // ==========================================
    // Query
    // ==========================================

    public static List<String> getSlowTests(long thresholdMs) {
        List<String> slow = new ArrayList<>();
        stepDurations.forEach((step, ms) -> {
            if (ms > thresholdMs)
                slow.add(step + " (" + ms + "ms)");
        });
        scenarioDurations.forEach((scenario, ms) -> {
            if (ms > thresholdMs)
                slow.add("[SCENARIO] " + scenario + " (" + ms + "ms)");
        });
        return slow;
    }

    public static long getApiLatency(String endpointFragment) {
        // API latencies supplied by NetworkMonitor — query by URL fragment
        return NetworkMonitor.getLatencyForEndpoint(endpointFragment);
    }
}
