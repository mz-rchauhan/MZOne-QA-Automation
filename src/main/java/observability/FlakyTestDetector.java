package observability;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import core.base.LoggerManager;

import java.io.File;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * FlakyTestDetector — Tracks pass/fail history per step to identify
 */
public class FlakyTestDetector {

    private static final Logger LOGGER = LoggerManager.getLogger(FlakyTestDetector.class);
    private static final String REPORT_PATH = "target/flaky-report.json";

    // stepText → [passed, failed] counts
    private static final ConcurrentHashMap<String, int[]> history = new ConcurrentHashMap<>();
    // scenarioName → passed
    private static final ConcurrentHashMap<String, Boolean> scenarioHistory = new ConcurrentHashMap<>();

    static {
        loadHistory();
    }

    private FlakyTestDetector() {
    }

    @SuppressWarnings("unchecked")
    private static void loadHistory() {
        File f = new File(REPORT_PATH);
        if (f.exists()) {
            try {
                List<Map<String, Object>> report = new ObjectMapper().readValue(f, List.class);
                for (Map<String, Object> entry : report) {
                    String step = (String) entry.get("step");
                    int passed = (int) entry.get("passed");
                    int failed = (int) entry.get("failed");
                    history.put(step, new int[] { passed, failed });
                }
                LOGGER.info("[FlakyDetector] Loaded {} steps from history.", report.size());
            } catch (Exception e) {
                LOGGER.warn("[FlakyDetector] Could not load history: {}", e.getMessage());
            }
        }
    }

    public static void record(String stepText, boolean passed) {
        history.compute(stepText, (k, v) -> {
            if (v == null)
                v = new int[] { 0, 0 };
            if (passed)
                v[0]++;
            else
                v[1]++;
            return v;
        });
    }

    public static void recordScenario(String scenarioName, boolean passed) {
        if (scenarioName != null) {
            scenarioHistory.put(scenarioName, passed);
        }
    }

    public static boolean isFlaky(String stepText) {
        int[] counts = history.get(stepText);
        if (counts == null || counts[0] + counts[1] < 2)
            return false;
        double totalRuns = counts[0] + counts[1];
        double failRate = counts[1] / totalRuns;
        return failRate > 0.20 && failRate < 0.80;
    }

    public static List<String> getFlakyCandidates() {
        List<String> flaky = new ArrayList<>();
        history.forEach((step, counts) -> {
            if (isFlaky(step))
                flaky.add(step);
        });
        return flaky;
    }

    public static void generateFlakyReport() {
        List<Map<String, Object>> report = new ArrayList<>();
        history.forEach((step, counts) -> {
            if (isFlaky(step)) {
                Map<String, Object> entry = new LinkedHashMap<>();
                entry.put("step", step);
                entry.put("passed", counts[0]);
                entry.put("failed", counts[1]);
                entry.put("failRate", String.format("%.1f%%", (double) counts[1] / (counts[0] + counts[1]) * 100));
                report.add(entry);
            }
        });
        if (report.isEmpty()) {
            LOGGER.info("[FlakyDetector] No flaky steps detected.");
            return;
        }
        try {
            new File("target").mkdirs();
            new ObjectMapper().writerWithDefaultPrettyPrinter().writeValue(new File(REPORT_PATH), report);
            LOGGER.info("[FlakyDetector] Flaky report written: {} flaky steps → {}", report.size(), REPORT_PATH);
        } catch (Exception e) {
            LOGGER.warn("[FlakyDetector] Could not generate report: {}", e.getMessage());
        }
    }
}
