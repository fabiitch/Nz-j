package com.fabiitch.nzj.event;

/**
 * Functional listener receiving one event object.
 */
@FunctionalInterface
public interface Listener<T> {

    /**
     * Called when an event is fired.
     */
    void onEvent(T event);
}