package com.nz.nzj.utils.math;

import lombok.experimental.UtilityClass;

@UtilityClass
public final class PercentUtils {
    public static final float ZERO = 0f;
    public static final float ONE_HUNDRED = 100f;

    /**
     * Returns percentage of part relative to total.
     * Example: part=25, total=200 -> 12.5
     */
    public static float percent(float part, float total) {
        if (total == 0f) return 0f;
        return (part / total) * 100f;
    }

    public static int percentInt(float part, float total) {
        return (int) percent(part, total);
    }

    /**
     * Returns ratio between 0 and 1
     * Example: part=25, total=100 -> 0.25
     */
    public static float ratio(float part, float total) {
        if (total == 0f) return 0f;
        return part / total;
    }

    /**
     * Returns value from percentage.
     * Example: percent=25, total=200 -> 50
     */
    public static float valueFromPercent(float percent, float total) {
        return (percent * total) / 100f;
    }

    /**
     * Percent difference relative to reference.
     * Example: a=120, b=100 -> +20%
     */
    public static float percentDifference(float value, float reference) {
        if (reference == 0f) {
            if (value == 0f) return 0f;
            return 100f;
        }
        return percent(value - reference, reference);
    }

    public static int percentDifferenceInt(float value, float reference) {
        return (int) percentDifference(value, reference);
    }

    /**
     * Increase value by percent.
     * Example: 100 +20% -> 120
     */
    public static float addPercent(float value, float percent) {
        return value + (value * percent / 100f);
    }

    /**
     * Decrease value by percent.
     * Example: 100 -20% -> 80
     */
    public static float removePercent(float value, float percent) {
        return value - (value * percent / 100f);
    }

    /**
     * Ratio clamped between 0 and 1
     */
    public static float ratioClamped(float part, float total) {
        if (total == 0f) return 0f;
        return Math.min(1f, part / total);
    }
}
