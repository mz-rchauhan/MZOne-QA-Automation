package resilience;

import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

import java.util.Map;

import org.slf4j.Logger;
 
/**
 * LocatorCandidate — Value object holding a locator, its resolved element, and
 * confidence score.
 * Used by LocatorHealingEngine to rank and select the best healing strategy.
 */
import core.base.LoggerManager;
public class LocatorScoring {
 
    private static final Logger LOGGER = LoggerManager.getLogger(LocatorScoring.class);

    public final By locator;
    public final WebElement element;
    public final double confidenceScore; // 0-100
    public final String strategy; // "text" | "attribute" | "dom-position" | "tag"

    public LocatorScoring(By locator, WebElement element, double confidenceScore) {
        this.locator = locator;
        this.element = element;
        this.confidenceScore = confidenceScore;
        this.strategy = deriveStrategy(confidenceScore);
    }

    public LocatorScoring(By locator, WebElement element, double confidenceScore, String strategy) {
        this.locator = locator;
        this.element = element;
        this.confidenceScore = confidenceScore;
        this.strategy = strategy;
    }

    private static String deriveStrategy(double score) {
        if (score >= 80)
            return "text";
        if (score >= 60)
            return "attribute";
        if (score >= 40)
            return "dom-position";
        return "tag";
    }

    @Override
    public String toString() {
        return String.format("LocatorCandidate[strategy='%s', score=%.1f, locator=%s]",
                strategy, confidenceScore, locator);
    }
    /**
     * Calculate a confidence score for a WebElement candidate.
     *
     * @param element   Candidate element found on the page
     * @param signature Expected attributes/text map (e.g., {"aria-label": "Account
     *                  Name", "tag": "input"})
     * @return Normalised confidence score 0–100
     */
    public static double score(WebElement element, Map<String, String> signature) {
        double rawScore = 0;
        double maxScore = 0;

        if (element == null || signature == null || signature.isEmpty()) {
            return 0;
        }

        try {
            // Text content match → +50
            maxScore += 50;
            String expectedText = signature.getOrDefault("text", "");
            String actualText = safeText(element);
            if (!expectedText.isEmpty() && actualText.toLowerCase().contains(expectedText.toLowerCase())) {
                rawScore += 50;
                LOGGER.trace("[Confidence] Text match +50: '{}'", expectedText);
            }

            // Attribute matches → +30 each
            for (Map.Entry<String, String> entry : signature.entrySet()) {
                String attrKey = entry.getKey();
                if (attrKey.equals("text") || attrKey.equals("tag") || attrKey.equals("position"))
                    continue;
                maxScore += 30;
                String expectedVal = entry.getValue();
                String actualVal = safeAttr(element, attrKey);
                if (actualVal != null && actualVal.toLowerCase().contains(expectedVal.toLowerCase())) {
                    rawScore += 30;
                    LOGGER.trace("[Confidence] Attr '{}' match +30", attrKey);
                }
            }

            // Tag name match → +10
            maxScore += 10;
            String expectedTag = signature.getOrDefault("tag", "");
            if (!expectedTag.isEmpty()) {
                String actualTag = safeTag(element);
                if (actualTag.equalsIgnoreCase(expectedTag)) {
                    rawScore += 10;
                    LOGGER.trace("[Confidence] Tag match +10: '{}'", expectedTag);
                }
            }

            // DOM position hint → +20 (if "position" key present and within range)
            String positionHint = signature.get("position");
            maxScore += 20;
            if (positionHint == null) {
                rawScore += 20; // no constraint — give full position score
            }

        } catch (Exception e) {
            LOGGER.warn("[Confidence] Scoring error: {}", e.getMessage());
        }

        double normalised = maxScore > 0 ? (rawScore / maxScore) * 100 : 0;
        LOGGER.debug("[Confidence] Score: {}/{} → {}%", (int) rawScore, (int) maxScore, (int) normalised);
        return normalised;
    }

    // ==========================================
    // Safe attribute readers
    // ==========================================

    private static String safeText(WebElement el) {
        try {
            return el.getText() != null ? el.getText() : "";
        } catch (Exception e) {
            return "";
        }
    }

    private static String safeAttr(WebElement el, String attr) {
        try {
            return el.getAttribute(attr);
        } catch (Exception e) {
            return null;
        }
    }

    private static String safeTag(WebElement el) {
        try {
            return el.getTagName();
        } catch (Exception e) {
            return "";
        }
    }

    /**
     * Calculate a raw similarity score (0.0–1.0) between a candidate element and
     * an expected attribute signature.
     *
     * @param element  Candidate WebElement found on the page
     * @param expected Map of expected attribute key → value pairs
     * @return Similarity score between 0.0 (no match) and 1.0 (full match)
     */
    public static double calculateSimilarity(WebElement element, Map<String, String> expected) {
        if (element == null || expected == null || expected.isEmpty())
            return 0.0;
        int totalChecks = 0, matches = 0;

        for (Map.Entry<String, String> entry : expected.entrySet()) {
            String key = entry.getKey();
            String expectedValue = entry.getValue();
            totalChecks++;

            String actualValue = null;
            try {
                if ("text".equalsIgnoreCase(key)) {
                    actualValue = element.getText();
                } else if ("tag".equalsIgnoreCase(key)) {
                    actualValue = element.getTagName();
                } else {
                    actualValue = element.getAttribute(key);
                }
            } catch (Exception e) {
                LOGGER.trace("[Similarity] Could not read '{}' from element.", key);
            }

            if (actualValue != null && actualValue.toLowerCase().contains(expectedValue.toLowerCase())) {
                matches++;
            }
        }
        double similarity = totalChecks > 0 ? (double) matches / totalChecks : 0.0;
        LOGGER.debug("[Similarity] {}/{} attributes matched → similarity={}", matches, totalChecks, similarity);
        return similarity;
    }


}
