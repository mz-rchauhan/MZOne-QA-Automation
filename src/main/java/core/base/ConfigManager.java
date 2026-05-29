package core.base;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.io.IOException;
import java.io.InputStream;
import java.util.Objects;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Consolidated Configuration Manager - Multi-Org Support
 */
public final class ConfigManager {

    private static final Logger LOGGER = LoggerFactory.getLogger(ConfigManager.class);

    // Singleton instance
    private static ConfigManager instance;

    // Organization-specific properties cache (org -> properties)
    private static final ConcurrentHashMap<String, Properties> ORG_CONFIGS = new ConcurrentHashMap<>();

    // Current environment (thread-safe)
    private static final ThreadLocal<String> CURRENT_ENV = ThreadLocal.withInitial(() -> "qa");

    // Property file location
    private static final String CONFIG_PATH = "config/application";
    private static final String CONFIG_EXTENSION = ".properties";

    // Default environment if not specified
    private static final String DEFAULT_ENVIRONMENT = "qa";

    // Supported organizations
    public enum Organization {
        QA("qa"),
        UAT("uat"),
        PROD("prod");

        private final String value;

        Organization(String value) {
            this.value = value;
        }

        public String getValue() {
            return value;
        }

        public static Organization fromString(String value) {
            for (Organization org : Organization.values()) {
                if (org.value.equalsIgnoreCase(value)) {
                    return org;
                }
            }
            LOGGER.warn("Unknown organization: {}. Using default: {}", value, DEFAULT_ENVIRONMENT);
            return QA;
        }
    }

    /**
     * Private constructor for singleton pattern
     */
    private ConfigManager() {
        loadDefaultConfiguration();
    }

    /**
     * Get singleton instance
     * 
     * @return ConfigManager instance
     */
    public static synchronized ConfigManager getInstance() {
        if (instance == null) {
            instance = new ConfigManager();
        }
        return instance;
    }

    /**
     * Set organization/environment at runtime
     */
    public static synchronized void setEnvironment(String environment) {
        Objects.requireNonNull(environment, "Environment cannot be null");

        String env = environment.toLowerCase().trim();
        Organization org = Organization.fromString(env);

        try {
            // Load organization-specific config if not cached
            if (!ORG_CONFIGS.containsKey(org.getValue())) {
                loadOrganizationConfig(org.getValue());
            }

            CURRENT_ENV.set(org.getValue());
            LOGGER.info("Environment set to: {}", org.getValue().toUpperCase());
            LOGGER.info("Organization: {}", getProperty("org.name", org.getValue().toUpperCase()));
            LOGGER.info("Base URL: {}", getProperty("org.url", "NOT CONFIGURED"));

        } catch (Exception e) {
            LOGGER.error("Error setting environment: {}", org.getValue(), e);
            throw new RuntimeException("Failed to set environment to: " + org.getValue(), e);
        }
    }

    /**
     * Get current environment
     * 
     * @return current environment/organization (qa, uat, prod)
     */
    public static String getCurrentEnvironment()
    {
        return CURRENT_ENV.get();
    }

    /**
     * Get current organization object
     * 
     * @return Organization enum
     */
    public static Organization getCurrentOrganization()
    {
        return Organization.fromString(CURRENT_ENV.get());
    }

    /**
     * Get property value for current organization
     * 
     * @param key property key
     * @return property value or throws exception
     * @throws IllegalArgumentException if key not found
     */
    public static String getProperty(String key) {
        String value = getPropertyInternal(key);
        if (value == null || value.trim().isEmpty()) {
            LOGGER.error("Required property '{}' not found for organization: {}", 
                        key, getCurrentEnvironment());
            throw new IllegalArgumentException(
                    "Required property '" + key + "' not found for organization: " + getCurrentEnvironment());
        }
        return value.trim();
    }

    /**
     * Get property value with default fallback
     * 
     * @param key property key
     * @param defaultValue default value if key not found
     * @return property value or default
     */
    public static String getProperty(String key, String defaultValue) {
        String value = getPropertyInternal(key);
        if (value == null || value.trim().isEmpty()) {
            LOGGER.debug("Property '{}' not found, using default: '{}'", key, defaultValue);
            return defaultValue;
        }
        return value.trim();
    }

    /**
     * Get integer property value
     * 
     * @param key property key
     * @param defaultValue default value if key not found or invalid
     * @return integer property value or default
     */
    public static int getIntProperty(String key, int defaultValue) {
        String value = getPropertyInternal(key);
        try {
            return value != null && !value.trim().isEmpty() ? 
                    Integer.parseInt(value.trim()) : defaultValue;
        } catch (NumberFormatException e) {
            LOGGER.warn("Property '{}' is not a valid integer, using default: {}", key, defaultValue);
            return defaultValue;
        }
    }

    /**
     * Get boolean property value
     * 
     * @param key property key
     * @param defaultValue default value if key not found
     * @return boolean property value or default
     */
    public static boolean getBooleanProperty(String key, boolean defaultValue) {
        String value = getPropertyInternal(key);
        if (value == null || value.trim().isEmpty()) {
            return defaultValue;
        }
        return Boolean.parseBoolean(value.trim());
    }

    /**
     * Get double property value
     * 
     * @param key property key
     * @param defaultValue default value if key not found or invalid
     * @return double property value or default
     */
    public static double getDoubleProperty(String key, double defaultValue) {
        String value = getPropertyInternal(key);
        try {
            return value != null && !value.trim().isEmpty() ? 
                    Double.parseDouble(value.trim()) : defaultValue;
        } catch (NumberFormatException e) {
            LOGGER.warn("Property '{}' is not a valid double, using default: {}", key, defaultValue);
            return defaultValue;
        }
    }

    /**
     * Get long property value
     * 
     * @param key property key
     * @param defaultValue default value if key not found or invalid
     * @return long property value or default
     */
    public static long getLongProperty(String key, long defaultValue) {
        String value = getPropertyInternal(key);
        try {
            return value != null && !value.trim().isEmpty() ? 
                    Long.parseLong(value.trim()) : defaultValue;
        } catch (NumberFormatException e) {
            LOGGER.warn("Property '{}' is not a valid long, using default: {}", key, defaultValue);
            return defaultValue;
        }
    }

    /**
     * Check if property exists
     * 
     * @param key property key
     * @return true if property exists and not empty
     */
    public static boolean propertyExists(String key) {
        String value = getPropertyInternal(key);
        return value != null && !value.trim().isEmpty();
    }

    /**
     * Get organization-specific property
     * 
     * Example: getOrgProperty("qa", "org.username")
     * 
     * @param organization organization name
     * @param key property key
     * @param defaultValue default if not found
     * @return property value
     */
    public static String getOrgProperty(String organization, String key, String defaultValue) {
        try {
            Organization org = Organization.fromString(organization);
            if (!ORG_CONFIGS.containsKey(org.getValue())) {
                loadOrganizationConfig(org.getValue());
            }
            Properties props = ORG_CONFIGS.get(org.getValue());
            String value = props.getProperty(key);
            return value != null && !value.trim().isEmpty() ? value.trim() : defaultValue;
        } catch (Exception e) {
            LOGGER.warn("Error getting property from organization {}: {}", organization, key, e);
            return defaultValue;
        }
    }

    /**
     * Get all properties for current organization
     * 
     * @return Properties object for current org
     */
    public static Properties getAllProperties() {
        return ORG_CONFIGS.get(getCurrentEnvironment());
    }

    /**
     * Reload configuration for current organization
     * Useful for dynamic configuration updates.
     */
    public static synchronized void reloadConfiguration() {
        try {
            String currentEnv = getCurrentEnvironment();
            LOGGER.info("Reloading configuration for: {}", currentEnv);
            ORG_CONFIGS.remove(currentEnv);
            loadOrganizationConfig(currentEnv);
            LOGGER.info("Configuration reloaded successfully");
        } catch (Exception e) {
            LOGGER.error("Error reloading configuration", e);
            throw new RuntimeException("Failed to reload configuration", e);
        }
    }

    /**
     * Get credentials object for current organization
     *
     * @return CredentialsConfig object with username, password, org name
     */
    public static CredentialsConfig getCredentials() {
        return new CredentialsConfig(
                getProperty("org.username", ""),
                getProperty("org.password", ""),
                getProperty("org.name", getCurrentEnvironment())
        );
    }

    /**
     * Reset to default environment (for test cleanup)
     */
    public static synchronized void reset() {
        CURRENT_ENV.set(DEFAULT_ENVIRONMENT);
        LOGGER.info("Configuration reset to default environment: {}", DEFAULT_ENVIRONMENT);
    }

    /**
     * Clear all cached configurations (rarely needed)
     */
    public static synchronized void clearCache() {
        ORG_CONFIGS.clear();
        LOGGER.info("Configuration cache cleared");
    }

    /**
     * Load default configuration (QA)
     */
    private static void loadDefaultConfiguration() {
        try {
            LOGGER.info("Loading default configuration (QA)");
            loadOrganizationConfig(DEFAULT_ENVIRONMENT);
            CURRENT_ENV.set(DEFAULT_ENVIRONMENT);
        } catch (Exception e) {
            LOGGER.error("Error loading default configuration", e);
            throw new RuntimeException("Failed to load default configuration", e);
        }
    }

    /**
     * Load organization-specific configuration file
     * 
     * @param organization organization name
     * @throws IOException if property file not found
     */
    private static void loadOrganizationConfig(String organization) throws IOException {
        Properties props = new Properties();
        String filename = CONFIG_PATH + "-" + organization + CONFIG_EXTENSION;

        try (InputStream input = ConfigManager.class.getClassLoader()
                .getResourceAsStream(filename)) {
            if (input == null) {
                LOGGER.warn("Configuration file not found: {}. Trying default application.properties", filename);
                // Try loading default application.properties
                try (InputStream defaultInput = ConfigManager.class.getClassLoader()
                        .getResourceAsStream("config/application.properties")) {
                    if (defaultInput != null) {
                        props.load(defaultInput);
                        LOGGER.info("Loaded default application.properties");
                    } else {
                        throw new IOException("No configuration file found for organization: " + organization);
                    }
                }
            } else {
                props.load(input);
                LOGGER.info("Loaded configuration for organization: {}", organization);
            }

            ORG_CONFIGS.put(organization, props);
        }
    }

    /**
     * Internal method to get property from current organization
     * 
     * @param key property key
     * @return property value or null
     */
    private static String getPropertyInternal(String key) {
        try {
            String env = getCurrentEnvironment();
            if (!ORG_CONFIGS.containsKey(env)) {
                loadOrganizationConfig(env);
            }
            Properties props = ORG_CONFIGS.get(env);
            return props.getProperty(key);
        } catch (Exception e) {
            LOGGER.error("Error getting property: {}", key, e);
            return null;
        }
    }

    /**
     * Credentials configuration holder (SECURE - don't log passwords)
     * Holds username, password, and organization name for login operations.
     */
    public static class CredentialsConfig {
        private final String username;
        private final String password;
        private final String organization;

        public CredentialsConfig(String username, String password, String organization) {
            this.username = username;
            this.password = password;
            this.organization = organization;
        }

        /**
         * Get username
         * @return username for login
         */
        public String getUsername() {
            return username;
        }

        /**
         * Get password
         * @return password for login
         */
        public String getPassword() {
            return password;
        }

        /**
         * Get organization name
         * @return organization/environment name
         */
        public String getOrganization() {
            return organization;
        }

        @Override
        public String toString() {
            // Don't print password in toString for security
            return "CredentialsConfig{" + "organization='" + organization + '\'' + "}";
        }
    }

    // ──────────────────────────────────────────────────────────────────────────
    // ALIAS METHODS — bridge legacy call sites to correct method names
    // ──────────────────────────────────────────────────────────────────────────

    /** Alias for getIntProperty — satisfies DriverManager, RetryEngine, ExecutionPipeline call sites. */
    public static int getInt(String key, int defaultValue) {
        return getIntProperty(key, defaultValue);
    }

    /** Alias for getLongProperty — satisfies RetryEngine, ExecutionPipeline, PerformanceProfiler call sites. */
    public static long getLong(String key, long defaultValue) {
        return getLongProperty(key, defaultValue);
    }

    /** Alias for getCurrentEnvironment — satisfies ExtentReportManager call site. */
    public static String getActiveEnvironment() {
        return getCurrentEnvironment();
    }

    /** Explicit-wait timeout in seconds — satisfies ExceptionHandler.handleTimeoutException. */
    public static int getExplicitWaitTimeout() {
        return getIntProperty("explicit.wait.timeout", 30);
    }
}
