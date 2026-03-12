package com.nz.nzj.thread.retry;

public interface RetryHandle extends AutoCloseable {

    void cancel();

    boolean isCancelled();

    @Override
    default void close() {
        cancel();
    }
}
