package core.base;

/**
 * Base runtime exception for all framework-level errors.
 * Wraps Selenium and configuration failures into a consistent exception hierarchy.
 */
public class FrameworkException extends RuntimeException {

    public FrameworkException(String message) {
        super(message);
    }

    public FrameworkException(String message, Throwable cause) {
        super(message, cause);
    }
}
