package com.nz.nzj.thread;

import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;

import java.util.Objects;

@Slf4j
@UtilityClass
public final class SchedulerUtils {

    public static Runnable safe(String taskName, Runnable task) {
        Objects.requireNonNull(taskName, "taskName");
        Objects.requireNonNull(task, "task");

        return () -> {
            try {
                task.run();
            } catch (Throwable t) {
                log.error("Scheduled task [{}] failed", taskName, t);
            }
        };
    }
}
