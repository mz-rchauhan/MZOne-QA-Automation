package resilience;
 
import org.openqa.selenium.WebElement;
import java.util.Map;
 
public final class LocatorConfidenceCalculator {

    private LocatorConfidenceCalculator() {
    }
 
    /**
     * Calculate confidence score for a candidate element.
     */
    public static double calculate(WebElement element, Map<String, String> signature) {
        return LocatorScoring.score(element, signature);
    }
 
    public static double score(WebElement element, Map<String, String> signature) {
        return LocatorScoring.score(element, signature);
    }
}
