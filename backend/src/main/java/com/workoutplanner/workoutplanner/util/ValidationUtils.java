package com.workoutplanner.workoutplanner.util;

/**
 * Utility methods for input validation and sanitization.
 */
public final class ValidationUtils {

    private ValidationUtils() {
    }

    /**
     * Escapes SQL LIKE wildcards to prevent wildcard abuse in search queries.
     */
    public static String sanitizeLikeWildcards(String input) {
        if (input == null) {
            return "";
        }
        return input.replace("\\", "\\\\")
                   .replace("%", "\\%")
                   .replace("_", "\\_");
    }
}
