package com.nz.core.event;

import java.util.function.Consumer;

/**
 * Public-facing event subscription API.
 * <p>
 * Separating the source (subscription) from the sink (publication) ensures
 * consumers cannot publish events they should only observe.
 * </p>
 *
 * @param <E> event type
 */
public interface EventSource<E> {
    /**
     * Subscribes a listener.
     *
     * @param listener event listener
     * @return subscription used to unsubscribe
     */
    Subscription subscribe(Consumer<? super E> listener);
}
