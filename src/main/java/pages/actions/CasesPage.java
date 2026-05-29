package pages.actions;

import core.base.SeleniumCommands;
import core.base.TestContext;
import org.openqa.selenium.By;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CasesPage {
    private static final Logger LOGGER = LoggerFactory.getLogger(CasesPage.class);
    private final SeleniumCommands seleniumCommands;

    public CasesPage() {
        seleniumCommands = new SeleniumCommands();
    }

    public void extractAndStoreCaseNumber(String key, String contextKey) {
        LOGGER.info("Extracting case number from Flow confirmation message...");

        By anchorLocator = By.xpath(
                "//lightning-formatted-rich-text" +
                        "//p[contains(.,'" + contextKey + "')]//a"
        );
        try {

            String caseNumber = seleniumCommands.getText(anchorLocator);

            if (caseNumber.isEmpty()) {
                throw new RuntimeException(
                        "Case number anchor found but text is empty. " +
                                "Check if value is inside href attribute instead.");
            }

            TestContext.store(key, caseNumber);
            LOGGER.info("Case number '{}' stored in TestContext['{}']",
                    caseNumber, contextKey);

        } catch (Exception e) {
            LOGGER.error("Failed to extract case number: {}", e.getMessage());
            throw new RuntimeException(
                    "Could not extract case number from confirmation message.", e);
        }
    }

    public void extractAndStoreCaseRecordId(String contextKey) {
        LOGGER.info("Extracting case record ID from Flow confirmation message...");

        By anchorLocator = By.xpath(
                "//lightning-formatted-rich-text" +
                        "//p[contains(.,'" + contextKey + "')]//a"
        );

        try {

            String href = seleniumCommands.getAttribute(anchorLocator, "href");

            if (href == null || href.isEmpty()) {
                throw new RuntimeException(
                        "Case link found but href attribute is empty.");
            }

            LOGGER.info("Raw href: '{}'", href);

            String recordId = href.substring(href.lastIndexOf("/") + 1).trim();

            if (recordId.isEmpty()) {
                throw new RuntimeException(
                        "Could not parse record ID from href: '" + href + "'");
            }

            TestContext.store(contextKey, recordId);
            LOGGER.info("Record ID '{}' stored in TestContext['{}']",
                    recordId, contextKey);

        } catch (Exception e) {
            LOGGER.error("Failed to extract case record ID: {}", e.getMessage());
            throw new RuntimeException(
                    "Could not extract case record ID from confirmation message.", e);
        }
    }
}
