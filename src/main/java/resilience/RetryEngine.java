package resilience;

import core.base.LoggerManager;
import core.base.WaitManager;
import observability.TestObservabilityEngine;
import org.slf4j.Logger;

import java.util.concurrent.Callable;
import java.time.Duration;

import core.base.ConfigManager;
public final class RetryEngine {
    private static final Logger LOGGER = LoggerManager.getLogger(RetryEngine.class);
    private static final int DEFAULT_MAX_RETRIES = ConfigManager.getInt("retry.max.attempts", 3);
    private static final long DEFAULT_BACKOFF_MS = ConfigManager.getLong("retry.backoff.ms", 1000L);

    private RetryEngine() {
    }

    public static void executeWithRetry(Runnable task, String taskName) {
        executeWithRetry(task, taskName, DEFAULT_MAX_RETRIES, DEFAULT_BACKOFF_MS);
    }

    public static void executeWithRetry(Runnable task, String taskName, int maxRetries, long backoffMs) {
        executeWithRetry(() -> {
            task.run();
            return null;
        }, taskName, maxRetries, backoffMs);
    }

    public static <T> T executeWithRetry(Callable<T> task, String taskName, int maxRetries) {
        return executeWithRetry(task, taskName, maxRetries, DEFAULT_BACKOFF_MS);
    }

    public static <T> T executeWithRetry(Callable<T> task, String taskName, int maxRetries, long backoffMs) {
        int attempt = 0;
        while (true) {
            attempt++;
            TestObservabilityEngine.recordRetryAttempt(taskName, attempt);
            try {
                return task.call();
            } catch (InterruptedException interruptedException) {
                Thread.currentThread().interrupt();
                throw new RuntimeException(
                        "RetryEngine: Task '" + taskName + "' interrupted on attempt " + attempt,
                        interruptedException);
            } catch (RuntimeException error) {
                if (attempt >= maxRetries) {
                    throw new RuntimeException(
                            "RetryEngine: Task '" + taskName + "' failed after " + maxRetries + " attempts", error);
                }
                long delay = backoffMs * (long) Math.pow(2, attempt - 1);
                LOGGER.warn("RetryEngine: task '{}' failed on attempt {}. Retrying in {} ms. Cause: {}",
                        taskName, attempt, delay, error.getMessage());
                sleep(delay, taskName);
            } catch (Exception error) {
                if (attempt >= maxRetries) {
                    throw new RuntimeException(
                            "RetryEngine: Task '" + taskName + "' failed after " + maxRetries + " attempts", error);
                }
                long delay = backoffMs * (long) Math.pow(2, attempt - 1);
                LOGGER.warn("RetryEngine: task '{}' threw a checked exception on attempt {}. Retrying in {} ms. Cause: {}",
                        taskName, attempt, delay, error.getMessage());
                sleep(delay, taskName);
            }
        }
    }

    private static void sleep(long millis, String taskName) {
        if (millis <= 0L) {
            return;
        }
        WaitManager.pause(Duration.ofMillis(millis), "Retry backoff for " + taskName);
    }
}
