package core.base;

import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileOutputStream;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Screenshot capture service.
 * Saves screenshots to target/screenshots/ with timestamp-based naming.
 * Safe to call even when no driver is active — will log warning and return null.
 */
public final class ScreenshotService {

    private static final Logger LOGGER = LoggerFactory.getLogger(ScreenshotService.class);
    private static final String SCREENSHOT_DIR = "target/screenshots";

    private ScreenshotService() {
    }

    /**
     * Captures a screenshot and saves it to disk.
     *
     * @param name Base name for the screenshot file (no extension needed)
     * @return The absolute file path, or null if capture failed
     */
    public static String capture(String name) {
        if (!DriverManager.hasActiveDriver()) {
            LOGGER.warn("No active driver — screenshot skipped for: {}", name);
            return null;
        }
        try {
            new File(SCREENSHOT_DIR).mkdirs();
            String timestamp = LocalDateTime.now()
                    .format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss_SSS"));
            String safeFileName = (name == null ? "screenshot" : name.replaceAll("[^a-zA-Z0-9_\\-]", "_"));
            String filePath = SCREENSHOT_DIR + "/" + safeFileName + "_" + timestamp + ".png";

            byte[] bytes = ((TakesScreenshot) DriverManager.getDriver())
                    .getScreenshotAs(OutputType.BYTES);

            try (FileOutputStream fos = new FileOutputStream(filePath)) {
                fos.write(bytes);
            }
            LOGGER.info("Screenshot saved: {}", filePath);
            return filePath;
        } catch (Exception e) {
            LOGGER.warn("Screenshot capture failed for '{}': {}", name, e.getMessage());
            return null;
        }
    }
}
