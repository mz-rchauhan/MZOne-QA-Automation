package observability;

import core.base.ConfigManager;
import org.slf4j.Logger;
import core.base.LoggerManager;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Canonical framework health monitor under the observability layer.
 */
public final class FrameworkHealthMonitor {

    private static final Logger LOGGER = LoggerManager.getLogger(FrameworkHealthMonitor.class);
    private static final Map<String, Long> testExecutionTimes = new ConcurrentHashMap<>();
    private static final Map<String, Integer> flakyTestCounts = new ConcurrentHashMap<>();
    private static final Map<String, Integer> locatorFailureCounts = new ConcurrentHashMap<>();
    private static final List<String> slowTests = java.util.Collections.synchronizedList(new ArrayList<>());

    private static final long SLOW_TEST_THRESHOLD_MS = Long.parseLong(
            ConfigManager.getProperty("slow.test.threshold.ms", "30000"));

    private FrameworkHealthMonitor() {
    }

    public static void recordTestExecution(String testName, long durationMs) {
        testExecutionTimes.put(testName, durationMs);
        if (durationMs > SLOW_TEST_THRESHOLD_MS) {
            slowTests.add(testName);
            LOGGER.warn("[HealthMonitor] SLOW TEST detected: {} took {}ms (threshold: {}ms)",
                    testName, durationMs, SLOW_TEST_THRESHOLD_MS);
        }
    }

    public static void recordFlakyTest(String testName) {
        flakyTestCounts.merge(testName, 1, Integer::sum);
        LOGGER.warn("[HealthMonitor] Flaky test recorded: {} (total flaky count: {})",
                testName, flakyTestCounts.get(testName));
    }

    public static void recordLocatorFailure(String locatorDescription) {
        locatorFailureCounts.merge(locatorDescription, 1, Integer::sum);
    }

    public static String generateHealthReport() {
        StringBuilder report = new StringBuilder();
        report.append("\n==================================================\n");
        report.append("FRAMEWORK HEALTH MONITOR REPORT\n");
        report.append("==================================================\n\n");

        report.append("SLOW TESTS (threshold: ").append(SLOW_TEST_THRESHOLD_MS).append("ms)\n");
        report.append("--------------------------------------------------\n");
        if (slowTests.isEmpty()) {
            report.append("  - No slow tests detected.\n");
        } else {
            for (String test : slowTests) {
                report.append("  - ").append(test).append(" -> ").append(testExecutionTimes.get(test)).append("ms\n");
            }
        }

        report.append("\nFLAKY TESTS\n");
        report.append("--------------------------------------------------\n");
        if (flakyTestCounts.isEmpty()) {
            report.append("  - No flaky tests detected.\n");
        } else {
            flakyTestCounts.entrySet().stream()
                    .sorted(Map.Entry.<String, Integer>comparingByValue().reversed())
                    .forEach(entry -> report.append("  - ").append(entry.getKey())
                            .append(" -> flaky ").append(entry.getValue()).append(" times\n"));
        }

        report.append("\nUNSTABLE LOCATORS\n");
        report.append("--------------------------------------------------\n");
        if (locatorFailureCounts.isEmpty()) {
            report.append("  - No unstable locators detected.\n");
        } else {
            locatorFailureCounts.entrySet().stream()
                    .sorted(Map.Entry.<String, Integer>comparingByValue().reversed())
                    .limit(10)
                    .forEach(entry -> report.append("  - ").append(entry.getKey())
                            .append(" -> failed ").append(entry.getValue()).append(" times\n"));
        }

        report.append("\nEXECUTION SUMMARY\n");
        report.append("--------------------------------------------------\n");
        report.append("  Total tests tracked: ").append(testExecutionTimes.size()).append("\n");
        report.append("  Slow tests: ").append(slowTests.size()).append("\n");
        report.append("  Flaky tests: ").append(flakyTestCounts.size()).append("\n");
        report.append("  Unstable locators: ").append(locatorFailureCounts.size()).append("\n");

        if (!testExecutionTimes.isEmpty()) {
            long avgTime = testExecutionTimes.values().stream().mapToLong(Long::longValue).sum() / testExecutionTimes.size();
            long maxTime = testExecutionTimes.values().stream().mapToLong(Long::longValue).max().orElse(0);
            report.append("  Average execution time: ").append(avgTime).append("ms\n");
            report.append("  Slowest test: ").append(maxTime).append("ms\n");
        }

        String fullReport = report.toString();
        LOGGER.info(fullReport);
        return fullReport;
    }

    public static void reset() {
        testExecutionTimes.clear();
        flakyTestCounts.clear();
        locatorFailureCounts.clear();
        slowTests.clear();
    }
}
