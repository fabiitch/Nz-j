package com.nz.nzj.thread.retry;

import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Consumer;
import java.util.function.Supplier;

public final class OptionalRetryJob<T> implements RetryHandle {

    private final ScheduledExecutorService scheduler;
    private final RetryPlan plan;
    private final Supplier<Optional<T>> supplier;
    private final Consumer<T> onSuccess;
    private final Consumer<Throwable> onFailure;
    private final Runnable onExhausted;

    private final AtomicLong runId = new AtomicLong(0);
    private final AtomicBoolean cancelled = new AtomicBoolean(true);

    public OptionalRetryJob(ScheduledExecutorService scheduler,
                            RetryPlan plan,
                            Supplier<Optional<T>> supplier,
                            Consumer<T> onSuccess) {
        this(scheduler, plan, supplier, onSuccess, throwable -> {}, () -> {});
    }

    public OptionalRetryJob(ScheduledExecutorService scheduler,
                            RetryPlan plan,
                            Supplier<Optional<T>> supplier,
                            Consumer<T> onSuccess,
                            Consumer<Throwable> onFailure,
                            Runnable onExhausted) {
        this.scheduler = Objects.requireNonNull(scheduler, "scheduler");
        this.plan = Objects.requireNonNull(plan, "plan");
        this.supplier = Objects.requireNonNull(supplier, "supplier");
        this.onSuccess = Objects.requireNonNull(onSuccess, "onSuccess");
        this.onFailure = Objects.requireNonNull(onFailure, "onFailure");
        this.onExhausted = Objects.requireNonNull(onExhausted, "onExhausted");
    }

    public synchronized void start() {
        cancelled.set(false);
        long currentRunId = runId.incrementAndGet();

        long cumulativeDelay = 0L;
        long[] delays = plan.delaysMs();

        for (int i = 0; i < delays.length; i++) {
            cumulativeDelay += delays[i];
            boolean lastAttempt = i == delays.length - 1;
            long scheduledDelay = cumulativeDelay;

            scheduler.schedule(
                    () -> attempt(currentRunId, lastAttempt),
                    scheduledDelay,
                    TimeUnit.MILLISECONDS
            );
        }
    }

    private void attempt(long expectedRunId, boolean lastAttempt) {
        if (cancelled.get() || runId.get() != expectedRunId) {
            return;
        }

        try {
            Optional<T> result = supplier.get();

            if (cancelled.get() || runId.get() != expectedRunId) {
                return;
            }

            if (result.isPresent()) {
                cancel();
                onSuccess.accept(result.get());
                return;
            }

            if (lastAttempt) {
                cancel();
                onExhausted.run();
            }

        } catch (Throwable throwable) {
            cancel();
            onFailure.accept(throwable);
        }
    }

    @Override
    public synchronized void cancel() {
        cancelled.set(true);
        runId.incrementAndGet();
    }

    @Override
    public boolean isCancelled() {
        return cancelled.get();
    }
}