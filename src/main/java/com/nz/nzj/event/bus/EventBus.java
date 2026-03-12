package com.nz.nzj.event.bus;

import com.nz.nzj.event.Listener;

import java.util.Arrays;

/**
 * Fast event bus.
 * <p>
 * Not thread-safe.
 * <p>
 * Best choice when:
 * - one thread owns the bus
 * - listeners are modified on the same thread
 * - fire(...) is called frequently
 * <p>
 * This implementation is optimized for low overhead during dispatch.
 *
 * @param <T> event type
 */
public class EventBus<T> extends AbstractEventBus<T> {

    private Listener<T>[] listeners;
    private int size;

    @SuppressWarnings("unchecked")
    public EventBus() {
        this.listeners = (Listener<T>[]) new Listener[8];
    }

    @Override
    public void addListener(Listener<T> listener) {
        if (listener == null) {
            throw new IllegalArgumentException("listener cannot be null");
        }

        // Avoid duplicate registrations.
        for (int i = 0; i < size; i++) {
            if (listeners[i] == listener) {
                return;
            }
        }

        // Grow internal array when needed.
        if (size == listeners.length) {
            listeners = Arrays.copyOf(listeners, listeners.length << 1);
        }

        listeners[size++] = listener;
    }

    @Override
    public void removeListener(Listener<T> listener) {
        for (int i = 0; i < size; i++) {
            if (listeners[i] == listener) {
                int moved = size - i - 1;

                if (moved > 0) {
                    System.arraycopy(listeners, i + 1, listeners, i, moved);
                }

                listeners[--size] = null;
                return;
            }
        }
    }

    @Override
    public void clearListeners() {
        Arrays.fill(listeners, 0, size, null);
        size = 0;
    }

    @Override
    public int size() {
        return size;
    }

    /**
     * Returns a compact snapshot containing only active listeners.
     * <p>
     * We do not return the raw backing array because it may contain nulls
     * after 'size', and AbstractEventBus.fire(...) expects only valid listeners.
     */
    @Override
    protected Listener<T>[] snapshot() {
        return Arrays.copyOf(listeners, size);
    }
}
