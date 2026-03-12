package com.nz.nzj.event.bus;

import com.nz.nzj.event.Listener;

import java.util.Arrays;

/**
 * Thread-safe event bus.
 * <p>
 * Uses immutable listener snapshots:
 * - fire(...) is lock-free
 * - add/remove/clear are synchronized
 * <p>
 * Best choice when:
 * - listeners may be added/removed from multiple threads
 * - fire(...) happens often
 * - listener registration changes less often than dispatch
 *
 * @param <T> event type
 */
public  class EventBusTS<T> extends AbstractEventBus<T> {

    private volatile Listener<T>[] listeners;

    @SuppressWarnings("unchecked")
    public EventBusTS() {
        this.listeners = (Listener<T>[]) new Listener[0];
    }

    @Override
    public synchronized void addListener(Listener<T> listener) {
        if (listener == null) {
            throw new IllegalArgumentException("listener cannot be null");
        }

        Listener<T>[] current = listeners;

        // Avoid duplicate registrations.
        for (int i = 0; i < current.length; i++) {
            if (current[i] == listener) {
                return;
            }
        }

        Listener<T>[] next = Arrays.copyOf(current, current.length + 1);
        next[current.length] = listener;
        listeners = next;
    }

    @Override
    public synchronized void removeListener(Listener<T> listener) {
        Listener<T>[] current = listeners;

        for (int i = 0; i < current.length; i++) {
            if (current[i] == listener) {
                @SuppressWarnings("unchecked")
                Listener<T>[] next = (Listener<T>[]) new Listener[current.length - 1];

                if (i > 0) {
                    System.arraycopy(current, 0, next, 0, i);
                }

                int rightCount = current.length - i - 1;
                if (rightCount > 0) {
                    System.arraycopy(current, i + 1, next, i, rightCount);
                }

                listeners = next;
                return;
            }
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public synchronized void clearListeners() {
        listeners = (Listener<T>[]) new Listener[0];
    }

    @Override
    public int size() {
        return listeners.length;
    }

    @Override
    protected Listener<T>[] snapshot() {
        return listeners;
    }
}
