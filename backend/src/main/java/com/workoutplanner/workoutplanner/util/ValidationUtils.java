package com.workoutplanner.workoutplanner.util;

public final class ValidationUtils {

    private ValidationUtils() {
        // Private constructor to prevent instantiation
    }

    /**
     * Sanitize string input to escape SQL LIKE wildcards.
     * Prevents wildcard abuse in search queries.
     * 
     * @param input the input string to sanitize
     * @return sanitized string with escaped wildcards
     */
    public static String sanitizeLikeWildcards(String input) {
        if (input == null) {
            return "";
        }
        // Escape special SQL LIKE wildcards
        // Escape backslash first to avoid double-escaping
        return input.replace("\\", "\\\\")
                   .replace("%", "\\%")
                   .replace("_", "\\_");
    }

    /**
     * Sanitize string for logging to prevent log injection.
     * 
     * @param input the input string to sanitize
     * @return sanitized string with newlines and tabs replaced
     */
    public static String sanitizeForLogging(String input) {
        if (input == null) {
            return "null";
        }
        // Remove potentially dangerous characters
        return input.replaceAll("[\\r\\n\\t]", "_");
    }
}
