package observability;

import org.slf4j.Logger;
import core.base.LoggerManager;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * TestObservabilityEngine — Centralized test metrics and observability tracker.
 */
public class TestObservabilityEngine {

    private static final Logger LOGGER = LoggerManager.getLogger(TestObservabilityEngine.class);
    private static final int MAX_METRICS_HISTORY = 100;

    private static final ThreadLocal<String> currentScenario = new ThreadLocal<>();
    private static final ThreadLocal<Long> scenarioStartMs = new ThreadLocal<>();
    private static final ThreadLocal<List<StepMetric>> stepMetrics = ThreadLocal.withInitial(ArrayList::new);
    private static final Map<String, List<StepMetric>> allMetrics = new ConcurrentHashMap<>();

    private TestObservabilityEngine() {
    }

    // ==========================================
    // Scenario Lifecycle
    // ==========================================

    public static void startScenario(String scenarioName) {
        currentScenario.set(scenarioName);
        scenarioStartMs.set(System.currentTimeMillis());
        stepMetrics.set(new ArrayList<>());
        LOGGER.debug("[Observability] Scenario started: '{}'", scenarioName);
    }

    public static void endScenario(boolean passed) {
        String name = currentScenario.get();
        long duration = System.currentTimeMillis() - (scenarioStartMs.get() != null ? scenarioStartMs.get() : 0L);
        LOGGER.info("[Observability] Scenario '{}' ended in {}ms | PASS={}", name, duration, passed);
        if (name != null) {
            if (allMetrics.size() >= MAX_METRICS_HISTORY) {
                String firstKey = allMetrics.keySet().iterator().next();
                allMetrics.remove(firstKey);
            }
            allMetrics.put(name, new ArrayList<>(stepMetrics.get()));
        }
        PerformanceProfiler.recordScenarioDuration(name, duration);
        FlakyTestDetector.recordScenario(name, passed);
    }

    // ==========================================
    // Step Tracking
    // ==========================================

    public static void recordStep(String stepText, long durationMs, boolean passed) {
        StepMetric metric = new StepMetric(stepText, durationMs, passed);
        List<StepMetric> metrics = stepMetrics.get();
        if (metrics != null)
            metrics.add(metric);
        FlakyTestDetector.record(stepText, passed);
        PerformanceProfiler.recordStepDuration(stepText, durationMs);
    }

    public static void recordLocatorHealingEvent(String fieldLabel, String strategy) {
        LOGGER.info("[Observability] Locator healed: field='{}' strategy='{}'", fieldLabel, strategy);
        recordStep("[HEALING] " + fieldLabel + " via " + strategy, 0, true);
    }

    public static void recordRetryAttempt(String action, int attempt) {
        LOGGER.info("[Observability] Retry: action='{}' attempt={}", action, attempt);
    }

    // ==========================================
    // Query
    // ==========================================

    public static List<String> getFlakyCandidates() {
        return FlakyTestDetector.getFlakyCandidates();
    }

    public static Map<String, List<StepMetric>> getAllMetrics() {
        return Collections.unmodifiableMap(allMetrics);
    }

    // ==========================================
    // Step Metric Model
    // ==========================================

    public static class StepMetric {
        public final String step;
        public final long durationMs;
        public final boolean passed;

        public StepMetric(String step, long durationMs, boolean passed) {
            this.step = step;
            this.durationMs = durationMs;
            this.passed = passed;
        }
    }
}
