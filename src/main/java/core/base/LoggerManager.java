package core.base;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;


/**
 * Unified Log Manager
 * Provides integration with SLF4J (Logback) and ExtentReports.
 */
public final class LoggerManager {

    private static final Logger ROOT = LoggerFactory.getLogger("FRAMEWORK");
    

    /**
     * Get a class-specific logger.
     */
    public static Logger getLogger() {
        return ROOT;
    }

    public static Logger getLogger(Class<?> clazz) {
        return LoggerFactory.getLogger(clazz);
    }


    public static void info(Logger logger, String message) {
        logger.info(message);
        //ExtentReportManager.logInfo(message);
    }

    public static void pass(Logger logger, String message) {
        logger.info("[PASS] " + message);
        //ExtentReportManager.logPass(message);
    }

    public static void fail(Logger logger, String message) {
        logger.error("[FAIL] " + message);
       // ExtentReportManager.logFail(message);
    }

    public static void warn(Logger logger, String message) {
        logger.warn(message);
      //  ExtentReportManager.logInfo("WARN: " + message);
    }

    public static void error(Logger logger, String message, Throwable t) {
        logger.error(message, t);
       // ExtentReportManager.logFail(message, t);
    }



    public static void stepLog(String step) {
        ROOT.info("[STEP] {}", step);
    }

    public static void actionLog(String action, String target, String value) {
        ROOT.info("[ACTION] {} | target='{}' value='{}'", action, target, value != null ? value : "-");
    }

    public static void performanceLog(String label, long durationMs) {
        ROOT.info("[PERF] {} = {}ms", label, durationMs);
    }


    public static void setScenarioContext(String scenarioName) {
        MDC.put("scenario", scenarioName);
        MDC.put("thread", String.valueOf(Thread.currentThread().getId()));
    }

    public static void clearContext() {
        MDC.clear();
    }


    public static void info(Logger logger, String template, Object... args) {
        String formatted = format(template, args);
        logger.info(template, args);
        //ExtentReportManager.logInfo(formatted);
    }

    private static String format(String template, Object... args) {
        String formatted = template;
        if (args != null) {
            for (Object arg : args) {
                formatted = formatted.replaceFirst("\\{}",
                        java.util.regex.Matcher.quoteReplacement(String.valueOf(arg)));
            }
        }
        return formatted;
    }


    public static void warn(Logger logger, String template, Object... args) {
        logger.warn(template, args);
        //ExtentReportManager.logInfo("WARN: " + format(template, args));
    }


    /** Structured error log entry. */
    public static void errorLog(String message, Throwable cause) {
        ROOT.error("[ERROR] {} | cause='{}'", message, cause != null ? cause.getMessage() : "unknown", cause);
    }

    public static void healingLog(String fieldLabel, String strategy, double confidence) {
        ROOT.info("[HEAL] field='{}' strategy='{}' confidence={}%", fieldLabel, strategy, confidence);
    }



}
