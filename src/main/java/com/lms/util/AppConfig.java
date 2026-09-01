package com.lms.util;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * Reads /app.properties (src/main/resources/app.properties) - the optional
 * config for Google Sign-In and password-reset email.
 *
 * Unlike DBConnection, this is tolerant of the file being missing entirely
 * so the app still boots (with those features simply disabled) even if
 * nobody has set up app.properties yet.
 */
public class AppConfig {

    private static final Properties PROPS = new Properties();

    static {
        try (InputStream is = AppConfig.class.getResourceAsStream("/app.properties")) {
            if (is != null) {
                PROPS.load(is);
            }
        } catch (IOException e) {
            // Config is optional - fall back to an empty Properties instance.
        }
    }

    private AppConfig() {
    }

    public static String get(String key) {
        return PROPS.getProperty(key);
    }

    public static String get(String key, String defaultValue) {
        String value = PROPS.getProperty(key);
        return (value == null || value.isBlank()) ? defaultValue : value;
    }

    public static int getInt(String key, int defaultValue) {
        String value = PROPS.getProperty(key);
        if (value == null || value.isBlank()) {
            return defaultValue;
        }
        try {
            return Integer.parseInt(value.trim());
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }

    /** True only if every given key is present and non-blank. */
    public static boolean isConfigured(String... keys) {
        for (String key : keys) {
            String value = PROPS.getProperty(key);
            if (value == null || value.isBlank()) {
                return false;
            }
        }
        return true;
    }
}
