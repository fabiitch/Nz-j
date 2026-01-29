package com.nz.core.event;

/**
 * Internal-facing event publication API.
 *
 * @param <E> event type
 */
public interface EventSink<E> {
    /**
     * Publishes an event to all subscribed listeners.
     *
     * @param event event to publish
     */
    void publish(E event);
}
