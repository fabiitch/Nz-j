package com.nz.nzj.thread.retry;

import org.junit.jupiter.api.Test;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Deque;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.Callable;
import java.util.concurrent.Delayed;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RetrySchedulerUtilsTest {

    @Test
    void retryUntilPresentShouldRejectNullScheduler() {
        RetryPolicy policy = RetryPolicy.fixedDelays(0L);

        assertThrows(NullPointerException.class, () -> RetrySchedulerUtils.retryUntilPresent(
                null,
                policy,
                Optional::empty,
                value -> {
                },
                throwable -> {
                },
                () -> {
                }
        ));
    }

    @Test
    void retryUntilPresentShouldRejectNullPolicy() {
        FakeScheduledExecutorService scheduler = new FakeScheduledExecutorService();

        assertThrows(NullPointerException.class, () -> RetrySchedulerUtils.retryUntilPresent(
                scheduler,
                null,
                Optional::empty,
                value -> {
                },
                throwable -> {
                },
                () -> {
                }
        ));
    }

    @Test
    void retryUntilPresentShouldRejectNullSupplier() {
        FakeScheduledExecutorService scheduler = new FakeScheduledExecutorService();
        RetryPolicy policy = RetryPolicy.fixedDelays(0L);

        assertThrows(NullPointerException.class, () -> RetrySchedulerUtils.retryUntilPresent(
                scheduler,
                policy,
                null,
                value -> {
                },
                throwable -> {
                },
                () -> {
                }
        ));
    }

    @Test
    void retryUntilPresentShouldRejectNullOnSuccess() {
        FakeScheduledExecutorService scheduler = new FakeScheduledExecutorService();
        RetryPolicy policy = RetryPolicy.fixedDelays(0L);

        assertThrows(NullPointerException.class, () -> RetrySchedulerUtils.retryUntilPresent(
                scheduler,
                policy,
                Optional::empty,
                null,
                throwable -> {
                },
                () -> {
                }
        ));
    }

    @Test
    void retryUntilPresentShouldRejectNullOnFailure() {
        FakeScheduledExecutorService scheduler = new FakeScheduledExecutorService();
        RetryPolicy policy = RetryPolicy.fixedDelays(0L);

        assertThrows(NullPointerException.class, () -> RetrySchedulerUtils.retryUntilPresent(
                scheduler,
                policy,
                Optional::empty,
                value -> {
                },
                null,
                () -> {
                }
        ));
    }

    @Test
    void retryUntilPresentShouldRejectNullOnExhausted() {
        FakeScheduledExecutorService scheduler = new FakeScheduledExecutorService();
        RetryPolicy policy = RetryPolicy.fixedDelays(0L);

        assertThrows(NullPointerException.class, () -> RetrySchedulerUtils.retryUntilPresent(
                scheduler,
                policy,
                Optional::empty,
                value -> {
                },
                throwable -> {
                },
                null
        ));
    }

    @Test
    void shouldInvokeSuccessOnceWhenSupplierIsPresentImmediately() {
        FakeScheduledExecutorService scheduler = new FakeScheduledExecutorService();
        RetryPolicy policy = RetryPolicy.fixedDelays(0L, 10L);
        AtomicInteger successCount = new AtomicInteger();
        AtomicInteger failureCount = new AtomicInteger();
        AtomicInteger exhaustedCount = new AtomicInteger();
        AtomicReference<String> successValue = new AtomicReference<>();

        RetryHandle handle = RetrySchedulerUtils.retryUntilPresent(
                scheduler,
                policy,
                () -> Optional.of("ok"),
                value -> {
                    successCount.incrementAndGet();
                    successValue.set(value);
                },
                throwable -> failureCount.incrementAndGet(),
                exhaustedCount::incrementAndGet
        );

        scheduler.runAll();

        assertEquals(1, successCount.get());
        assertEquals("ok", successValue.get());
        assertEquals(0, failureCount.get());
        assertEquals(0, exhaustedCount.get());
        assertTrue(handle.isCancelled());
    }

    @Test
    void shouldRetryUntilPresentAndInvokeSuccessOnce() {
        FakeScheduledExecutorService scheduler = new FakeScheduledExecutorService();
        RetryPolicy policy = RetryPolicy.fixedDelays(1L, 2L, 3L);
        AtomicInteger supplierCalls = new AtomicInteger();
        AtomicInteger successCount = new AtomicInteger();
        AtomicInteger failureCount = new AtomicInteger();
        AtomicInteger exhaustedCount = new AtomicInteger();
        AtomicReference<String> successValue = new AtomicReference<>();

        RetryHandle handle = RetrySchedulerUtils.retryUntilPresent(
                scheduler,
                policy,
                () -> {
                    int call = supplierCalls.incrementAndGet();
                    if (call < 3) {
                        return Optional.empty();
                    }
                    return Optional.of("eventual-success");
                },
                value -> {
                    successCount.incrementAndGet();
                    successValue.set(value);
                },
                throwable -> failureCount.incrementAndGet(),
                exhaustedCount::incrementAndGet
        );

        scheduler.runAll();

        assertEquals(3, supplierCalls.get());
        assertEquals(1, successCount.get());
        assertEquals("eventual-success", successValue.get());
        assertEquals(0, failureCount.get());
        assertEquals(0, exhaustedCount.get());
        assertTrue(handle.isCancelled());
    }

    @Test
    void shouldInvokeExhaustedOnceWhenAllAttemptsReturnEmpty() {
        FakeScheduledExecutorService scheduler = new FakeScheduledExecutorService();
        RetryPolicy policy = RetryPolicy.fixedDelays(1L, 2L, 3L);
        AtomicInteger successCount = new AtomicInteger();
        AtomicInteger failureCount = new AtomicInteger();
        AtomicInteger exhaustedCount = new AtomicInteger();

        RetryHandle handle = RetrySchedulerUtils.retryUntilPresent(
                scheduler,
                policy,
                Optional::empty,
                value -> successCount.incrementAndGet(),
                throwable -> failureCount.incrementAndGet(),
                exhaustedCount::incrementAndGet
        );

        scheduler.runAll();

        assertEquals(1, exhaustedCount.get());
        assertEquals(0, successCount.get());
        assertEquals(0, failureCount.get());
        assertTrue(handle.isCancelled());
    }

    @Test
    void shouldInvokeFailureOnceWhenExceptionIsNotRetryable() {
        FakeScheduledExecutorService scheduler = new FakeScheduledExecutorService();
        RetryPolicy policy = new RetryPolicy(new long[]{0L, 10L}, throwable -> false);
        IllegalStateException expected = new IllegalStateException("boom");
        AtomicInteger successCount = new AtomicInteger();
        AtomicInteger failureCount = new AtomicInteger();
        AtomicInteger exhaustedCount = new AtomicInteger();
        AtomicReference<Throwable> failureThrowable = new AtomicReference<>();

        RetryHandle handle = RetrySchedulerUtils.retryUntilPresent(
                scheduler,
                policy,
                () -> {
                    throw expected;
                },
                value -> successCount.incrementAndGet(),
                throwable -> {
                    failureCount.incrementAndGet();
                    failureThrowable.set(throwable);
                },
                exhaustedCount::incrementAndGet
        );

        scheduler.runAll();

        assertEquals(1, failureCount.get());
        assertEquals(expected, failureThrowable.get());
        assertEquals(0, successCount.get());
        assertEquals(0, exhaustedCount.get());
        assertTrue(handle.isCancelled());
    }

    @Test
    void shouldRetryOnRetryableExceptionThenSucceed() {
        FakeScheduledExecutorService scheduler = new FakeScheduledExecutorService();
        RetryPolicy policy = new RetryPolicy(new long[]{5L, 10L}, IllegalStateException.class::isInstance);
        AtomicInteger attempts = new AtomicInteger();
        AtomicInteger successCount = new AtomicInteger();
        AtomicInteger failureCount = new AtomicInteger();
        AtomicInteger exhaustedCount = new AtomicInteger();

        RetryHandle handle = RetrySchedulerUtils.retryUntilPresent(
                scheduler,
                policy,
                () -> {
                    if (attempts.getAndIncrement() == 0) {
                        throw new IllegalStateException("retry me");
                    }
                    return Optional.of("ok");
                },
                value -> successCount.incrementAndGet(),
                throwable -> failureCount.incrementAndGet(),
                exhaustedCount::incrementAndGet
        );

        scheduler.runAll();

        assertEquals(1, successCount.get());
        assertEquals(0, failureCount.get());
        assertEquals(0, exhaustedCount.get());
        assertTrue(handle.isCancelled());
    }

    @Test
    void cancelBeforeExecutionShouldPreventCallbacksAndSupplierExecution() {
        FakeScheduledExecutorService scheduler = new FakeScheduledExecutorService();
        RetryPolicy policy = RetryPolicy.fixedDelays(0L, 10L);
        AtomicInteger supplierCalls = new AtomicInteger();
        AtomicInteger successCount = new AtomicInteger();
        AtomicInteger failureCount = new AtomicInteger();
        AtomicInteger exhaustedCount = new AtomicInteger();

        RetryHandle handle = RetrySchedulerUtils.retryUntilPresent(
                scheduler,
                policy,
                () -> {
                    supplierCalls.incrementAndGet();
                    return Optional.of("value");
                },
                value -> successCount.incrementAndGet(),
                throwable -> failureCount.incrementAndGet(),
                exhaustedCount::incrementAndGet
        );

        handle.cancel();
        scheduler.runAll();

        assertTrue(handle.isCancelled());
        assertEquals(0, supplierCalls.get());
        assertEquals(0, successCount.get());
        assertEquals(0, failureCount.get());
        assertEquals(0, exhaustedCount.get());
    }

    @Test
    void cancelShouldBeIdempotent() {
        FakeScheduledExecutorService scheduler = new FakeScheduledExecutorService();
        RetryHandle handle = RetrySchedulerUtils.retryUntilPresent(
                scheduler,
                RetryPolicy.fixedDelays(0L),
                Optional::empty,
                value -> {
                },
                throwable -> {
                },
                () -> {
                }
        );

        handle.cancel();
        handle.cancel();
        handle.cancel();

        assertTrue(handle.isCancelled());
    }

    @Test
    void closeShouldBehaveLikeCancel() throws Exception {
        FakeScheduledExecutorService scheduler = new FakeScheduledExecutorService();
        AtomicInteger supplierCalls = new AtomicInteger();

        RetryHandle handle = RetrySchedulerUtils.retryUntilPresent(
                scheduler,
                RetryPolicy.fixedDelays(0L),
                () -> {
                    supplierCalls.incrementAndGet();
                    return Optional.of("value");
                },
                value -> {
                },
                throwable -> {
                },
                () -> {
                }
        );

        handle.close();
        scheduler.runAll();

        assertTrue(handle.isCancelled());
        assertEquals(0, supplierCalls.get());
    }

    @Test
    void shouldScheduleNewAttemptAfterEmptyResultAndRetryableException() {
        FakeScheduledExecutorService scheduler = new FakeScheduledExecutorService();
        RetryPolicy policy = new RetryPolicy(new long[]{11L, 22L, 33L}, IllegalStateException.class::isInstance);
        AtomicInteger call = new AtomicInteger();

        RetryHandle handle = RetrySchedulerUtils.retryUntilPresent(
                scheduler,
                policy,
                () -> {
                    int current = call.getAndIncrement();
                    if (current == 0) {
                        return Optional.empty();
                    }
                    if (current == 1) {
                        throw new IllegalStateException("retryable");
                    }
                    return Optional.of("done");
                },
                value -> {
                },
                throwable -> {
                },
                () -> {
                }
        );

        assertEquals(List.of(11L), scheduler.scheduledDelaysMs());

        scheduler.runNext();
        assertEquals(List.of(11L, 22L), scheduler.scheduledDelaysMs());

        scheduler.runNext();
        assertEquals(List.of(11L, 22L, 33L), scheduler.scheduledDelaysMs());

        scheduler.runNext();

        assertEquals(3, call.get());
        assertTrue(handle.isCancelled());
    }

    private static final class FakeScheduledExecutorService implements ScheduledExecutorService {
        private final Deque<FakeScheduledFuture> queue = new ArrayDeque<>();
        private final List<Long> scheduledDelaysMs = new ArrayList<>();

        @Override
        public ScheduledFuture<?> schedule(Runnable command, long delay, TimeUnit unit) {
            FakeScheduledFuture future = new FakeScheduledFuture(command, unit.toMillis(delay));
            queue.addLast(future);
            scheduledDelaysMs.add(unit.toMillis(delay));
            return future;
        }

        void runNext() {
            FakeScheduledFuture future = queue.pollFirst();
            if (future == null) {
                return;
            }
            future.run();
        }

        void runAll() {
            while (!queue.isEmpty()) {
                runNext();
            }
        }

        List<Long> scheduledDelaysMs() {
            return List.copyOf(scheduledDelaysMs);
        }

        @Override
        public void shutdown() {
            throw new UnsupportedOperationException();
        }

        @Override
        public List<Runnable> shutdownNow() {
            throw new UnsupportedOperationException();
        }

        @Override
        public boolean isShutdown() {
            return false;
        }

        @Override
        public boolean isTerminated() {
            return false;
        }

        @Override
        public boolean awaitTermination(long timeout, TimeUnit unit) {
            throw new UnsupportedOperationException();
        }

        @Override
        public <V> ScheduledFuture<V> schedule(Callable<V> callable, long delay, TimeUnit unit) {
            throw new UnsupportedOperationException();
        }

        @Override
        public ScheduledFuture<?> scheduleAtFixedRate(Runnable command, long initialDelay, long period, TimeUnit unit) {
            throw new UnsupportedOperationException();
        }

        @Override
        public ScheduledFuture<?> scheduleWithFixedDelay(Runnable command, long initialDelay, long delay, TimeUnit unit) {
            throw new UnsupportedOperationException();
        }

        @Override
        public <T> Future<T> submit(Callable<T> task) {
            throw new UnsupportedOperationException();
        }

        @Override
        public <T> Future<T> submit(Runnable task, T result) {
            throw new UnsupportedOperationException();
        }

        @Override
        public Future<?> submit(Runnable task) {
            throw new UnsupportedOperationException();
        }

        @Override
        public <T> List<Future<T>> invokeAll(Collection<? extends Callable<T>> tasks) {
            throw new UnsupportedOperationException();
        }

        @Override
        public <T> List<Future<T>> invokeAll(Collection<? extends Callable<T>> tasks, long timeout, TimeUnit unit) {
            throw new UnsupportedOperationException();
        }

        @Override
        public <T> T invokeAny(Collection<? extends Callable<T>> tasks) {
            throw new UnsupportedOperationException();
        }

        @Override
        public <T> T invokeAny(Collection<? extends Callable<T>> tasks, long timeout, TimeUnit unit) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void execute(Runnable command) {
            throw new UnsupportedOperationException();
        }
    }

    private static final class FakeScheduledFuture implements ScheduledFuture<Object> {
        private final Runnable command;
        private final long delayMs;
        private boolean cancelled;
        private boolean done;

        private FakeScheduledFuture(Runnable command, long delayMs) {
            this.command = command;
            this.delayMs = delayMs;
        }

        void run() {
            if (cancelled || done) {
                return;
            }
            done = true;
            command.run();
        }

        @Override
        public long getDelay(TimeUnit unit) {
            return unit.convert(delayMs, TimeUnit.MILLISECONDS);
        }

        @Override
        public int compareTo(Delayed other) {
            long diff = getDelay(TimeUnit.MILLISECONDS) - other.getDelay(TimeUnit.MILLISECONDS);
            return Long.compare(diff, 0);
        }

        @Override
        public boolean cancel(boolean mayInterruptIfRunning) {
            cancelled = true;
            done = true;
            return true;
        }

        @Override
        public boolean isCancelled() {
            return cancelled;
        }

        @Override
        public boolean isDone() {
            return done;
        }

        @Override
        public Object get() {
            throw new UnsupportedOperationException();
        }

        @Override
        public Object get(long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
            throw new UnsupportedOperationException();
        }
    }
}
