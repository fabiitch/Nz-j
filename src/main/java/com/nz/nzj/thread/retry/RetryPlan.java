package com.nz.nzj.thread.retry;

import java.util.Arrays;
import java.util.Objects;

public final class RetryPlan {

    private final long[] delaysMs;

    private RetryPlan(long[] delaysMs) {
        Objects.requireNonNull(delaysMs, "delaysMs");

        if (delaysMs.length == 0) {
            throw new IllegalArgumentException("delaysMs must not be empty");
        }

        this.delaysMs = Arrays.copyOf(delaysMs, delaysMs.length);
    }

    public static RetryPlan of(long... delaysMs) {
        return new RetryPlan(delaysMs);
    }

    public long[] delaysMs() {
        return Arrays.copyOf(delaysMs, delaysMs.length);
    }
}