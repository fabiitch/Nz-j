package com.nz.nzj.thread.retry;


import java.util.Arrays;
import java.util.Objects;
import java.util.function.Predicate;

public final class RetryPolicy {

    private final long[] delaysMs;
    private final Predicate<Throwable> retryOnException;

    public RetryPolicy(long[] delaysMs, Predicate<Throwable> retryOnException) {
        Objects.requireNonNull(delaysMs, "delaysMs");
        Objects.requireNonNull(retryOnException, "retryOnException");

        if (delaysMs.length == 0) {
            throw new IllegalArgumentException("delaysMs must not be empty");
        }

        this.delaysMs = Arrays.copyOf(delaysMs, delaysMs.length);
        this.retryOnException = retryOnException;
    }

    public long[] delaysMs() {
        return Arrays.copyOf(delaysMs, delaysMs.length);
    }

    public int maxAttempts() {
        return delaysMs.length;
    }

    public long delayMs(int attemptIndex) {
        return delaysMs[attemptIndex];
    }

    public boolean shouldRetryOnException(Throwable throwable) {
        return retryOnException.test(throwable);
    }

    public static RetryPolicy fixedDelays(long... delaysMs) {
        return new RetryPolicy(delaysMs, throwable -> true);
    }

    public RetryPolicy retryOnException(Predicate<Throwable> predicate) {
        return new RetryPolicy(delaysMs, predicate);
    }
}
