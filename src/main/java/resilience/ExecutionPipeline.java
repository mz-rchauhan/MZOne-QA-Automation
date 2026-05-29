package resilience;

import observability.TestObservabilityEngine;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.slf4j.Logger;

import java.util.function.Consumer;

import core.base.ConfigManager;
import core.base.DriverManager;
import core.base.LoggerManager;
public final class ExecutionPipeline {
    private static final Logger LOGGER = LoggerManager.getLogger(ExecutionPipeline.class);
    private static final int DEFAULT_MAX_RETRIES = ConfigManager.getInt("pipeline.max.retries", 3);
    private static final long DEFAULT_BACKOFF_MS = ConfigManager.getLong("pipeline.retry.backoff.ms", 1000L);

    private ExecutionPipeline() {
    }

    public static void executeTask(String taskName, Runnable task) {
        long startMs = System.currentTimeMillis();
        try {
            RetryEngine.executeWithRetry(task, taskName, DEFAULT_MAX_RETRIES, DEFAULT_BACKOFF_MS);
            TestObservabilityEngine.recordStep(taskName, System.currentTimeMillis() - startMs, true);
        } catch (RuntimeException error) {
            recoverIfPossible();
            TestObservabilityEngine.recordStep(taskName, System.currentTimeMillis() - startMs, false);
            ExceptionHandler.rethrowAsFail("[Pipeline] Task '" + taskName + "' failed", error);
        }
    }

    public static void executeAction(String actionName, Runnable action) {
        executeTask(actionName, action);
    }

    public static void executeWithElement(By primary, Consumer<WebElement> action, String fieldLabel) {
        executeTask("Element:" + fieldLabel,
                () -> action.accept(LocatorHealingEngine.findWithHealing(primary, fieldLabel)));
    }

    private static void recoverIfPossible() {
        if (!DriverManager.hasActiveDriver()) {
            return;
        }
        try {
            AutoRecoveryEngine.closeUnexpectedPopup();
            AutoRecoveryEngine.recoverFromModalOverlay();
            AutoRecoveryEngine.restoreSessionState();
            AutoRecoveryEngine.recoverScenario();
        } catch (RuntimeException error) {
            LOGGER.warn("Recovery pipeline could not complete: {}", error.getMessage());
        }
    }
}
