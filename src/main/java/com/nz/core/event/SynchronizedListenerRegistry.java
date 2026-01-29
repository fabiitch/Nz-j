package com.nz.core.event;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/**
 * Listener registry backed by a synchronized {@link ArrayList}.
 * Provides deterministic behavior with simple synchronization.
 *
 * @param <E> event type
 */
public final class SynchronizedListenerRegistry<E> implements ListenerRegistry<E> {
    private final List<Consumer<? super E>> listeners = new ArrayList<>();

    @Override
    public Subscription add(Consumer<? super E> listener) {
        synchronized (listeners) {
            listeners.add(listener);
        }
        return () -> {
            synchronized (listeners) {
                listeners.remove(listener);
            }
        };
    }

    @Override
    public void dispatch(E event) {
        @SuppressWarnings("unchecked")
        Consumer<? super E>[] snapshot;
        synchronized (listeners) {
            snapshot = listeners.toArray(new Consumer[0]);
        }
        for (Consumer<? super E> listener : snapshot) {
            try {
                listener.accept(event);
            } catch (RuntimeException ex) {
                System.err.println("Event listener threw an exception: " + ex.getMessage());
                ex.printStackTrace(System.err);
            }
        }
    }
}
