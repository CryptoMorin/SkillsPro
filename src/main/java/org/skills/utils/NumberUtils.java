package org.skills.utils;

public class NumberUtils {
    public static int toInt(final String str, final int defaultValue) {
        try {
            return Integer.parseInt(str);
        } catch (final RuntimeException e) {
            return defaultValue;
        }
    }

    public static double toDouble(final String str, final double defaultValue) {
        try {
            return Double.parseDouble(str);
        } catch (final RuntimeException e) {
            return defaultValue;
        }
    }
}
