package com.nz.nzj.thread;

import lombok.experimental.UtilityClass;

import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

@UtilityClass
public class ExecutorsUtils {

    private static final RejectedExecutionHandler DEFAULT_REJECTION_POLICY =
            new ThreadPoolExecutor.CallerRunsPolicy();

    public static ExecutorService newSingle(String name) {
        return newSingle(name, Thread.NORM_PRIORITY, true);
    }

    public static ExecutorService newSingle(String name, int priority, boolean daemon) {
        validateThreadName(name);
        validatePriority(priority);

        return new ThreadPoolExecutor(
                1,
                1,
                0L,
                TimeUnit.MILLISECONDS,
                new LinkedBlockingQueue<>(),
                threadFactory(name, priority, daemon),
                DEFAULT_REJECTION_POLICY
        );
    }

    public static ExecutorService newSingle(
            String name,
            int queueCapacity,
            RejectedExecutionHandler rejectedExecutionHandler,
            int priority,
            boolean daemon
    ) {
        validateThreadName(name);
        validateQueueCapacity(queueCapacity);
        validatePriority(priority);
        validateRejectedExecutionHandler(rejectedExecutionHandler);

        return new ThreadPoolExecutor(
                1,
                1,
                0L,
                TimeUnit.MILLISECONDS,
                new ArrayBlockingQueue<>(queueCapacity),
                threadFactory(name, priority, daemon),
                rejectedExecutionHandler
        );
    }

    public static ExecutorService newFixed(int nThreads, String name) {
        return newFixed(nThreads, name, 1000, Thread.NORM_PRIORITY, true);
    }

    public static ExecutorService newFixed(int nThreads, String name, int queueCapacity) {
        return newFixed(nThreads, name, queueCapacity, Thread.NORM_PRIORITY, true);
    }

    public static ExecutorService newFixed(int nThreads, String name, int queueCapacity, int priority, boolean daemon) {
        return newFixed(nThreads, name, queueCapacity, DEFAULT_REJECTION_POLICY, priority, daemon);
    }

    public static ExecutorService newFixed(
            int nThreads,
            String name,
            int queueCapacity,
            RejectedExecutionHandler rejectedExecutionHandler,
            int priority,
            boolean daemon
    ) {
        validateThreadCount(nThreads);
        validateThreadName(name);
        validateQueueCapacity(queueCapacity);
        validatePriority(priority);
        validateRejectedExecutionHandler(rejectedExecutionHandler);

        return new ThreadPoolExecutor(
                nThreads,
                nThreads,
                0L,
                TimeUnit.MILLISECONDS,
                new ArrayBlockingQueue<>(queueCapacity),
                threadFactory(name, priority, daemon),
                rejectedExecutionHandler
        );
    }

    public static ExecutorService newCached(String name) {
        return newCached(name, 32, 60L, Thread.NORM_PRIORITY, true);
    }

    public static ExecutorService newCached(String name, int maxThreads, long keepAliveSeconds, int priority, boolean daemon) {
        validateThreadName(name);
        validateThreadCount(maxThreads);
        validatePriority(priority);

        return new ThreadPoolExecutor(
                0,
                maxThreads,
                keepAliveSeconds,
                TimeUnit.SECONDS,
                new SynchronousQueue<>(),
                threadFactory(name, priority, daemon),
                DEFAULT_REJECTION_POLICY
        );
    }

    public static ScheduledExecutorService newScheduledSingle(String name) {
        return newScheduledSingle(name, Thread.NORM_PRIORITY, true);
    }

    public static ScheduledExecutorService newScheduledSingle(String name, int priority, boolean daemon) {
        validateThreadName(name);
        validatePriority(priority);

        return new ScheduledThreadPoolExecutor(
                1,
                threadFactory(name, priority, daemon),
                DEFAULT_REJECTION_POLICY
        );
    }

    public static ScheduledExecutorService newScheduled(int nThreads, String name) {
        return newScheduled(nThreads, name, Thread.NORM_PRIORITY, true);
    }

    public static ScheduledExecutorService newScheduled(int nThreads, String name, int priority, boolean daemon) {
        validateThreadCount(nThreads);
        validateThreadName(name);
        validatePriority(priority);

        return new ScheduledThreadPoolExecutor(
                nThreads,
                threadFactory(name, priority, daemon),
                DEFAULT_REJECTION_POLICY
        );
    }

    public static ThreadFactory threadFactory(String prefix) {
        return threadFactory(prefix, Thread.NORM_PRIORITY, true);
    }

    public static ThreadFactory threadFactory(String prefix, int priority, boolean daemon) {
        validateThreadName(prefix);
        validatePriority(priority);

        AtomicInteger counter = new AtomicInteger(1);

        return runnable -> {
            Thread thread = new Thread(runnable, prefix + "-" + counter.getAndIncrement());
            thread.setDaemon(daemon);
            thread.setPriority(priority);
            thread.setUncaughtExceptionHandler((t, e) ->
                    System.err.println("[ExecutorsUtils] Uncaught exception in thread " + t.getName() + ": " + e)
            );
            return thread;
        };
    }

    private static void validateThreadCount(int nThreads) {
        if (nThreads <= 0) {
            throw new IllegalArgumentException("nThreads must be > 0");
        }
    }

    private static void validateQueueCapacity(int queueCapacity) {
        if (queueCapacity <= 0) {
            throw new IllegalArgumentException("queueCapacity must be > 0");
        }
    }

    private static void validateThreadName(String name) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("thread name must not be blank");
        }
    }

    private static void validatePriority(int priority) {
        if (priority < Thread.MIN_PRIORITY || priority > Thread.MAX_PRIORITY) {
            throw new IllegalArgumentException(
                    "priority must be between " + Thread.MIN_PRIORITY + " and " + Thread.MAX_PRIORITY
            );
        }
    }

    private static void validateRejectedExecutionHandler(RejectedExecutionHandler rejectedExecutionHandler) {
        if (rejectedExecutionHandler == null) {
            throw new IllegalArgumentException("rejectedExecutionHandler must not be null");
        }
    }
}