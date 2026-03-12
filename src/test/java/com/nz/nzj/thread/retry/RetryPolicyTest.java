package com.nz.nzj.thread.retry;

import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RetryPolicyTest {

    @Test
    void constructorShouldRejectNullDelays() {
        assertThrows(NullPointerException.class, () -> new RetryPolicy(null, throwable -> true));
    }

    @Test
    void constructorShouldRejectNullRetryPredicate() {
        assertThrows(NullPointerException.class, () -> new RetryPolicy(new long[]{1L}, null));
    }

    @Test
    void constructorShouldRejectEmptyDelays() {
        assertThrows(IllegalArgumentException.class, () -> new RetryPolicy(new long[0], throwable -> true));
    }

    @Test
    void delaysMsShouldReturnDefensiveCopy() {
        RetryPolicy policy = new RetryPolicy(new long[]{10L, 20L}, throwable -> true);

        long[] firstRead = policy.delaysMs();
        long[] secondRead = policy.delaysMs();

        assertNotSame(firstRead, secondRead);
        firstRead[0] = 999L;

        assertArrayEquals(new long[]{10L, 20L}, policy.delaysMs());
    }

    @Test
    void constructorShouldDefensivelyCopyProvidedDelays() {
        long[] delays = {10L, 20L};
        RetryPolicy policy = new RetryPolicy(delays, throwable -> true);

        delays[0] = 999L;

        assertArrayEquals(new long[]{10L, 20L}, policy.delaysMs());
    }

    @Test
    void maxAttemptsShouldMatchNumberOfDelays() {
        RetryPolicy policy = new RetryPolicy(new long[]{5L, 15L, 30L}, throwable -> true);

        assertEquals(3, policy.maxAttempts());
    }

    @Test
    void delayMsShouldReturnDelayForRequestedAttemptIndex() {
        RetryPolicy policy = new RetryPolicy(new long[]{5L, 15L, 30L}, throwable -> true);

        assertEquals(5L, policy.delayMs(0));
        assertEquals(15L, policy.delayMs(1));
        assertEquals(30L, policy.delayMs(2));
    }

    @Test
    void fixedDelaysShouldRetryOnAnyException() {
        RetryPolicy policy = RetryPolicy.fixedDelays(10L, 20L);

        assertTrue(policy.shouldRetryOnException(new RuntimeException("runtime")));
        assertTrue(policy.shouldRetryOnException(new IOException("io")));
    }

    @Test
    void retryOnExceptionShouldReturnNewPolicyWithNewPredicateAndKeepInitialPolicyUnchanged() {
        RetryPolicy initial = new RetryPolicy(new long[]{10L, 20L}, throwable -> false);

        RetryPolicy updated = initial.retryOnException(IllegalStateException.class::isInstance);

        assertFalse(initial.shouldRetryOnException(new IllegalStateException("initial must stay false")));
        assertTrue(updated.shouldRetryOnException(new IllegalStateException("updated must retry")));
        assertFalse(updated.shouldRetryOnException(new IllegalArgumentException("updated must not retry")));
        assertArrayEquals(initial.delaysMs(), updated.delaysMs());
    }
}
