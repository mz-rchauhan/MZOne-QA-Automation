package resilience;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.slf4j.Logger;

import java.io.File;
import java.util.*;

/**
 * LocatorSuggestionEngine — Suggests stable locators when all healing
 * strategies fail.
 * <p>
 * Analyses live DOM to find stable attributes (id, name, data-*, aria-*) and
 * generates recommended XPath expressions. Writes results to
 * target/locator-suggestions.json
 * so developers can update their metadata YAML files.
 */
import core.base.LoggerManager;
import core.base.DriverManager;
public class LocatorSuggestionEngine {

    private static final Logger LOGGER = LoggerManager.getLogger(LocatorSuggestionEngine.class);
    private static final String REPORT_PATH = "target/locator-suggestions.json";
    private static final List<Map<String, String>> suggestions = new ArrayList<>();

    private LocatorSuggestionEngine() {
    }

    /**
     * Analyse DOM around a label and suggest the most stable locator.
     *
     * @param fieldLabel  Label of the field that failed to locate
     * @param pageContext Page/context description (e.g., "AccountPage")
     * @return Suggested XPath string
     */
    public static String suggestLocator(String fieldLabel, String pageContext) {
        LOGGER.warn("[SuggestionEngine] Generating suggestion for '{}' on '{}'", fieldLabel, pageContext);

        List<LocatorScoring> candidates = analyzeDOM(fieldLabel);
        String suggestion;

        if (!candidates.isEmpty()) {
            LocatorScoring best = candidates.stream()
                    .max(java.util.Comparator.comparingDouble(c -> c.confidenceScore))
                    .orElse(null);
            suggestion = best != null ? best.locator.toString() : buildFallbackSuggestion(fieldLabel);
        } else {
            suggestion = buildFallbackSuggestion(fieldLabel);
        }

        LOGGER.info("[SuggestionEngine] Suggested: {}", suggestion);
        recordSuggestion(fieldLabel, pageContext, suggestion);
        return suggestion;
    }

    /**
     * Analyse the DOM for elements that might correspond to a field label.
     *
     * @param fieldLabel Expected field label text
     * @return List of scored LocatorCandidates
     */
    public static List<LocatorScoring> analyzeDOM(String fieldLabel) {
        List<LocatorScoring> candidates = new ArrayList<>();
        Map<String, String> signature = Map.of("text", fieldLabel);

        // Scan stable-attribute selectors
        List<String> stablePatterns = Arrays.asList(
                "//*[@aria-label='" + fieldLabel + "']",
                "//*[@title='" + fieldLabel + "']",
                "//*[@name='" + fieldLabel + "']",
                "//input[@placeholder='" + fieldLabel + "']",
                "//*[@data-field='" + fieldLabel + "']",
                "//*[contains(@aria-label,'" + fieldLabel + "')]");

        for (String xpath : stablePatterns) {
            try {
                List<WebElement> found = DriverManager.getDriver().findElements(By.xpath(xpath));
                for (WebElement el : found) {
                    double score = LocatorConfidenceCalculator.score(el, signature);
                    candidates.add(new LocatorScoring(By.xpath(xpath), el, score, "attribute"));
                }
            } catch (Exception ignored) {
            }
        }
        return candidates;
    }

    /**
     * Write all collected suggestions to the JSON report file.
     */
    public static void generateReport() {
        if (suggestions.isEmpty()) {
            LOGGER.info("[SuggestionEngine] No suggestions to write.");
            return;
        }
        try {
            new File("target").mkdirs();
            new ObjectMapper().writerWithDefaultPrettyPrinter().writeValue(new File(REPORT_PATH), suggestions);
            LOGGER.info("[SuggestionEngine] Report written to: {}", REPORT_PATH);
        } catch (Exception e) {
            LOGGER.warn("[SuggestionEngine] Could not write report: {}", e.getMessage());
        }
    }

    private static void recordSuggestion(String fieldLabel, String pageContext, String suggestion) {
        Map<String, String> entry = new LinkedHashMap<>();
        entry.put("field", fieldLabel);
        entry.put("page", pageContext);
        entry.put("suggestedXPath", suggestion);
        entry.put("timestamp", java.time.Instant.now().toString());
        suggestions.add(entry);
    }

    private static String buildFallbackSuggestion(String fieldLabel) {
        return "//label[text()='" + fieldLabel + "']/following-sibling::div//input | " +
                "//*[@aria-label='" + fieldLabel + "']";
    }
}
