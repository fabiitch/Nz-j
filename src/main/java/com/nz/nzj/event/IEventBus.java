package com.nz.nzj.event;

/**
 * Basic event bus contract.
 *
 * @param <T> event type
 */
public interface IEventBus<T> {

    /**
     * Fires an event to all registered listeners.
     */
    void fire(T event);

    /**
     * Adds a listener if not already registered.
     */
    void addListener(Listener<T> listener);

    /**
     * Removes a listener if present.
     */
    void removeListener(Listener<T> listener);

    /**
     * Removes all listeners.
     */
    void clearListeners();

    /**
     * Returns the current number of listeners.
     */
    int size();

    /**
     * Returns true if no listener is registered.
     */
    default boolean isEmpty() {
        return size() == 0;
    }
}