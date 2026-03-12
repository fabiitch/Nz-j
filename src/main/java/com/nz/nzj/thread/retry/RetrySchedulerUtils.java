package com.nz.nzj.thread.retry;


import lombok.experimental.UtilityClass;

import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;
import java.util.function.Supplier;

@UtilityClass
public final class RetrySchedulerUtils {

    public static <T> RetryHandle retryUntilPresent(
            ScheduledExecutorService scheduler,
            RetryPolicy policy,
            Supplier<Optional<T>> supplier,
            Consumer<T> onSuccess,
            Consumer<Throwable> onFailure,
            Runnable onExhausted
    ) {
        Objects.requireNonNull(scheduler, "scheduler");
        Objects.requireNonNull(policy, "policy");
        Objects.requireNonNull(supplier, "supplier");
        Objects.requireNonNull(onSuccess, "onSuccess");
        Objects.requireNonNull(onFailure, "onFailure");
        Objects.requireNonNull(onExhausted, "onExhausted");

        State state = new State();
        scheduleAttempt(scheduler, policy, supplier, onSuccess, onFailure, onExhausted, state, 0);
        return state;
    }

    private static <T> void scheduleAttempt(
            ScheduledExecutorService scheduler,
            RetryPolicy policy,
            Supplier<Optional<T>> supplier,
            Consumer<T> onSuccess,
            Consumer<Throwable> onFailure,
            Runnable onExhausted,
            State state,
            int attemptIndex
    ) {
        if (state.cancelled.get()) {
            return;
        }

        if (attemptIndex >= policy.maxAttempts()) {
            state.cancel();
            onExhausted.run();
            return;
        }

        long delayMs = policy.delayMs(attemptIndex);

        ScheduledFuture<?> future = scheduler.schedule(() -> {
            if (state.cancelled.get()) {
                return;
            }

            try {
                Optional<T> result = supplier.get();

                if (state.cancelled.get()) {
                    return;
                }

                if (result.isPresent()) {
                    state.cancel();
                    onSuccess.accept(result.get());
                    return;
                }

                scheduleAttempt(
                        scheduler,
                        policy,
                        supplier,
                        onSuccess,
                        onFailure,
                        onExhausted,
                        state,
                        attemptIndex + 1
                );

            } catch (Throwable throwable) {
                if (!policy.shouldRetryOnException(throwable)) {
                    state.cancel();
                    onFailure.accept(throwable);
                    return;
                }

                scheduleAttempt(
                        scheduler,
                        policy,
                        supplier,
                        onSuccess,
                        onFailure,
                        onExhausted,
                        state,
                        attemptIndex + 1
                );
            }
        }, delayMs, TimeUnit.MILLISECONDS);

        ScheduledFuture<?> previous = state.currentFuture.getAndSet(future);
        if (previous != null && !previous.isDone()) {
            previous.cancel(true);
        }
    }

    private static final class State implements RetryHandle {
        private final AtomicBoolean cancelled = new AtomicBoolean(false);
        private final AtomicReference<ScheduledFuture<?>> currentFuture = new AtomicReference<>();

        @Override
        public void cancel() {
            if (!cancelled.compareAndSet(false, true)) {
                return;
            }

            ScheduledFuture<?> future = currentFuture.getAndSet(null);
            if (future != null) {
                future.cancel(true);
            }
        }

        @Override
        public boolean isCancelled() {
            return cancelled.get();
        }
    }
}
