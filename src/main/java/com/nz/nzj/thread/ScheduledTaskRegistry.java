package com.nz.nzj.thread;

import lombok.extern.slf4j.Slf4j;

import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledFuture;

@Slf4j
public final class ScheduledTaskRegistry implements AutoCloseable {

    private final Map<String, ScheduledFuture<?>> tasks = new ConcurrentHashMap<>();

    public ScheduledFuture<?> replace(String taskName, ScheduledFuture<?> future) {
        Objects.requireNonNull(taskName, "taskName");
        Objects.requireNonNull(future, "future");

        ScheduledFuture<?> previous = tasks.put(taskName, future);
        if (previous != null) {
            log.debug("Replacing scheduled task [{}]", taskName);
            previous.cancel(true);
        } else {
            log.debug("Registering scheduled task [{}]", taskName);
        }

        return future;
    }

    public void cancel(String taskName) {
        Objects.requireNonNull(taskName, "taskName");

        ScheduledFuture<?> future = tasks.remove(taskName);
        if (future != null) {
            log.debug("Cancelling scheduled task [{}]", taskName);
            future.cancel(true);
        }
    }

    public boolean isRunning(String taskName) {
        Objects.requireNonNull(taskName, "taskName");

        ScheduledFuture<?> future = tasks.get(taskName);
        return future != null && !future.isCancelled() && !future.isDone();
    }

    public void cancelAll() {
        tasks.forEach((taskName, future) -> {
            if (future != null) {
                log.debug("Cancelling scheduled task [{}]", taskName);
                future.cancel(true);
            }
        });
        tasks.clear();
    }

    @Override
    public void close() {
        cancelAll();
    }
}
