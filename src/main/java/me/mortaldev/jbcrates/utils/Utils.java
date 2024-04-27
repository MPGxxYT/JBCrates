package me.mortaldev.jbcrates.utils;

public class Utils {
    /**
     * Returns the given value clamped between the minimum and maximum values.
     *
     * @param value The value to be clamped.
     * @param min The minimum value.
     * @param max The maximum value.
     * @return The clamped value. If the value is less than the minimum value, the minimum value is returned.
     * If the value is greater than the maximum value, the maximum value is returned. Otherwise, the value itself is returned.
     */
    public static int clamp(int value, int min, int max) {
        return Math.max(min, Math.min(max, value));
    }
}
