package resilience;

import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.slf4j.Logger;


import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import core.base.LoggerManager;
import core.base.ConfigManager;
import core.base.DriverManager;
import core.base.WaitManager;

public final class LocatorHealingEngine {

    private static final Logger LOGGER = LoggerManager.getLogger(LocatorHealingEngine.class);
    private static final double CONFIDENCE_THRESHOLD = Double
            .parseDouble(ConfigManager.getProperty("healing.confidence.threshold", "40"));

    private LocatorHealingEngine() {
    }

    /**
     * Find an element using a primary locator with automatic healing fallback.
     *
     * @param primary      Primary By locator
     * @param alternatives Ordered list of fallback locators
     * @param signature    Expected element attributes for confidence scoring
     * @param fieldLabel   Human-readable field name (for logging)
     * @return WebElement — best healing candidate
     */
    public static WebElement findWithHealing(By primary,
            List<By> alternatives,
            Map<String, String> signature,
            String fieldLabel) {
        // 1. Try primary locator first
        try {
            WebElement el = WaitManager.waitForVisible(primary);
            LOGGER.debug("[Healing] Primary locator succeeded for '{}'", fieldLabel);
            return el;
        } catch (Exception e) {
            LOGGER.warn("[Healing] Primary locator failed for '{}': {}", fieldLabel, e.getMessage());
        }

        // 2. Score all alternatives
        List<LocatorScoring> scored = new ArrayList<>();
        for (By alt : alternatives) {
            try {
                List<WebElement> found = DriverManager.getDriver().findElements(alt);
                for (WebElement candidate : found) {
                    double confidence = LocatorConfidenceCalculator.score(candidate, signature);
                    scored.add(new LocatorScoring(alt, candidate, confidence));
                }
            } catch (Exception e) {
                LOGGER.trace("[Healing] Alternative failed: {}", alt);
            }
        }

        // 3. Select best candidate above threshold
        LocatorScoring best = scored.stream()
                .filter(c -> c.confidenceScore >= CONFIDENCE_THRESHOLD)
                .max(java.util.Comparator.comparingDouble(c -> c.confidenceScore))
                .orElse(null);

        if (best != null) {
            LOGGER.info(String.format("[Healing] Healed '%s' via %s (confidence=%.1f%%)",
                    fieldLabel, best.locator, best.confidenceScore));
            logHealingEvent(fieldLabel, best);
            return best.element;
        }

        // 4. All candidates failed — delegate to suggestion engine
        LOGGER.error("[Healing] All locators exhausted for '{}'. Requesting suggestion.", fieldLabel);
        LocatorSuggestionEngine.suggestLocator(fieldLabel, "auto-suggest");
        throw new org.openqa.selenium.NoSuchElementException(
                "[HealingEngine] Could not locate element: " + fieldLabel);
    }

    /**
     * Simplified overload using ElementRepository fallbacks (no alternative list
     * required).
     */
    public static WebElement findWithHealing(By primary, String fieldLabel) {
        List<By> defaults = List.of(
                By.xpath("//label[text()='" + fieldLabel + "']/following-sibling::div//input"),
                By.xpath("//label[text()='" + fieldLabel + "']/following-sibling::div//button"),
                By.xpath("//*[text()='" + fieldLabel + "']/following::input[1]"),
                By.xpath("//*[@aria-label='" + fieldLabel + "'] | //*[@title='" + fieldLabel + "']"));
        return findWithHealing(primary, defaults, Map.of("text", fieldLabel), fieldLabel);
    }

    private static void logHealingEvent(String fieldLabel, LocatorScoring best) {
        try {
            observability.TestObservabilityEngine.recordLocatorHealingEvent(
                    fieldLabel, best.locator.toString());
        } catch (Exception e) {
            LOGGER.debug("[Healing] Could not log healing event: {}", e.getMessage());
        }
    }
}
