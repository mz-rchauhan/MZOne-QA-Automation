package core.base;

import java.time.Duration;

/**
 * Timeout-specific framework exception.
 * Thrown by WaitManager when elements/conditions do not resolve within the configured timeout.
 */
public class FrameworkTimeoutException extends FrameworkException {

    private final Duration timeout;

    public FrameworkTimeoutException(String message, Duration timeout, Throwable cause) {
        super(message + " [timeout=" + timeout.getSeconds() + "s]", cause);
        this.timeout = timeout;
    }

    public Duration getTimeout() {
        return timeout;
    }
}
