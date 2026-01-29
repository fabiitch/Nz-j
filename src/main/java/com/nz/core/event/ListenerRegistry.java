package com.nz.core.event;

import java.util.function.Consumer;

/**
 * Internal abstraction used to manage listeners and dispatch events.
 *
 * @param <E> event type
 */
public interface ListenerRegistry<E> {
    /**
     * Adds a listener.
     *
     * @param listener event listener
     * @return subscription used to remove the listener
     */
    Subscription add(Consumer<? super E> listener);

    /**
     * Dispatches an event to all listeners.
     * Implementations should isolate listener failures so one exception does not
     * prevent other listeners from being notified.
     *
     * @param event event to dispatch
     */
    void dispatch(E event);
}
