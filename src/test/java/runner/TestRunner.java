package runner;

import io.cucumber.testng.AbstractTestNGCucumberTests;
import io.cucumber.testng.CucumberOptions;
import observability.ExtentReportManager;
import org.testng.annotations.AfterSuite;
import org.testng.annotations.BeforeSuite;
import org.testng.annotations.DataProvider;

@CucumberOptions(
        features = "src/test/resources/Features",
        glue     = {"stepDefinitions", "hooks"},
        plugin   = {
                "pretty",
                "html:build/reports/cucumber/html-report.html",
                "json:build/reports/cucumber/Cucumber.json",
                "junit:build/reports/cucumber/Cucumber.xml",
                "com.aventstack.extentreports.cucumber.adapter.ExtentCucumberAdapter:"
        },
        monochrome = true
)
public class TestRunner extends AbstractTestNGCucumberTests {

    /**
     * Override to enable parallel scenario execution.
     * Remove parallel=true to run sequentially.
     */
    @Override
    @DataProvider(parallel = false)
    public Object[][] scenarios() {
        return super.scenarios();
    }

    /**
     * FIXED: Extent Reports were not flushing — flushReports() was never called.
     * This guarantees the HTML report is written even if scenarios fail.
     */
    @AfterSuite(alwaysRun = true)
    public void afterSuite() {
        ExtentReportManager.flushReports();
    }
}
