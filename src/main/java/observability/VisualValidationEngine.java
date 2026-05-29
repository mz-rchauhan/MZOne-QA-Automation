package observability;

import core.base.DriverManager;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.slf4j.Logger;
import core.base.LoggerManager;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

/**
 * VisualValidationEngine — Screenshot-based visual regression testing.
 */
public class VisualValidationEngine {

    private static final Logger LOGGER = LoggerManager.getLogger(VisualValidationEngine.class);
    private static final String BASELINE_DIR = "src/test/resources/visual-baselines/";
    private static final String DIFF_DIR = "target/visual-diffs/";
    private static final List<String> visualResults = Collections.synchronizedList(new ArrayList<>());

    private VisualValidationEngine() {
    }

    public static void captureBaseline(String name) {
        try {
            new File(BASELINE_DIR).mkdirs();
            byte[] bytes = ((TakesScreenshot) DriverManager.getDriver()).getScreenshotAs(OutputType.BYTES);
            Files.write(Paths.get(BASELINE_DIR + name + ".png"), bytes);
            LOGGER.info("[Visual] Baseline saved: {}.png", name);
        } catch (Exception e) {
            LOGGER.warn("[Visual] captureBaseline failed for '{}': {}", name, e.getMessage());
        }
    }

    /**
     * Compare current viewport against saved baseline.
     *
     * @param name Baseline name (matches captureBaseline name)
     * @return Pixel difference percentage (0.0 = identical)
     */
    public static double compareScreenshots(String name) {
        try {
            byte[] currentBytes = ((TakesScreenshot) DriverManager.getDriver()).getScreenshotAs(OutputType.BYTES);
            BufferedImage current = ImageIO.read(new ByteArrayInputStream(currentBytes));
            BufferedImage baseline = ImageIO.read(new File(BASELINE_DIR + name + ".png"));

            int w = Math.min(current.getWidth(), baseline.getWidth());
            int h = Math.min(current.getHeight(), baseline.getHeight());
            long diffPixels = 0, totalPixels = (long) w * h;

            for (int x = 0; x < w; x++) {
                for (int y = 0; y < h; y++) {
                    if (current.getRGB(x, y) != baseline.getRGB(x, y))
                        diffPixels++;
                }
            }
            double diffPct = (double) diffPixels / totalPixels * 100;
            LOGGER.info("[Visual] '{}' diff: {}/{} pixels = {}%", name, diffPixels, totalPixels, String.format("%.2f", diffPct));
            return diffPct;
        } catch (Exception e) {
            LOGGER.warn("[Visual] compareScreenshots failed for '{}': {}", name, e.getMessage());
            return 100.0; // Treat as full mismatch on error
        }
    }

    public static void assertVisualMatch(String name, double tolerancePct) {
        double diff = compareScreenshots(name);
        String result = String.format("'%s' diff=%.2f%% (threshold=%.2f%%)", name, diff, tolerancePct);
        if (diff <= tolerancePct) {
            LOGGER.info("[Visual] PASS — {}", result);
            visualResults.add("PASS: " + result);
        } else {
            LOGGER.error("[Visual] FAIL — {}", result);
            visualResults.add("FAIL: " + result);
            org.testng.Assert.fail("[VisualValidation] " + result);
        }
    }

    public static void generateVisualReport() {
        if (visualResults.isEmpty())
            return;
        try {
            new File("target").mkdirs();
            try (PrintWriter pw = new PrintWriter(new FileWriter("target/visual-report.txt"))) {
                pw.println("=== Visual Validation Report ===");
                visualResults.forEach(pw::println);
            }
            LOGGER.info("[Visual] Visual report written → target/visual-report.txt");
        } catch (Exception e) {
            LOGGER.warn("[Visual] generateVisualReport failed: {}", e.getMessage());
        }
    }
}
