package com.nz.core.event;

/**
 * Represents a subscription that can be closed to unsubscribe.
 */
public interface Subscription extends AutoCloseable {
    /**
     * Unsubscribes the listener.
     */
    @Override
    void close();
}
