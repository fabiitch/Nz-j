package com.nz.nzj.utils;

import lombok.experimental.UtilityClass;

@UtilityClass
public class TimeConverter {

    public static final long MILLIS_PER_SECOND = 1_000;
    public static final long NANOS_PER_SECOND = 1_000_000_000;
    public static final long NANOS_PER_MILLI = 1_000_000;

    public static long secondsToMillis(double seconds) {
        return (long) (seconds * MILLIS_PER_SECOND);
    }

    public static long secondsToNanos(double seconds) {
        return (long) (seconds * NANOS_PER_SECOND);
    }

    public static double millisToSeconds(long millis) {
        return millis / (double) MILLIS_PER_SECOND;
    }

    public static long millisToNanos(long millis) {
        return millis * NANOS_PER_MILLI;
    }

    public static double nanosToSeconds(long nanos) {
        return nanos / (double) NANOS_PER_SECOND;
    }

    public static long nanosToMillis(long nanos) {
        return nanos / NANOS_PER_MILLI;
    }

}
