package com.doctordark.hcf.util;

public final class NameUtils {

    private NameUtils() {
    }

    /**
     * Retrieves a more friendly looking name for a string, used mainly for
     * enums. Replaces underscores with spaces and ensures capitals are at the
     * start of every word.
     *
     * @param str the string to make better looking.
     * @return a pretty version of the passed string.
     */
    public static String getPrettyName(String str) {
        char[] chars = str.trim().toCharArray();
        for (int i = 0; i < chars.length; i++) {
            // First character must always be uppercase.
            if (i == 0) {
                chars[i] = Character.toUpperCase(chars[i]);
            }

            // Replace all underscores with spaces.
            else if (chars[i] == '_') {
                chars[i] = ' ';
            }

            // First character of a word must always be uppercase.
            else if (chars[i - 1] == ' ') {
                chars[i] = Character.toUpperCase(chars[i]);
            }

            // Other characters should be lowercase.
            else {
                chars[i] = Character.toLowerCase(chars[i]);
            }
        }
        return String.valueOf(chars);
    }

}
