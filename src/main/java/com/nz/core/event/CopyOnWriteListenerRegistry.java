package com.nz.core.event;

import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;

/**
 * Listener registry based on {@link CopyOnWriteArrayList}, suited for read-heavy scenarios.
 *
 * @param <E> event type
 */
public final class CopyOnWriteListenerRegistry<E> implements ListenerRegistry<E> {
    private final CopyOnWriteArrayList<Consumer<? super E>> listeners = new CopyOnWriteArrayList<>();

    @Override
    public Subscription add(Consumer<? super E> listener) {
        listeners.add(listener);
        return () -> listeners.remove(listener);
    }

    @Override
    public void dispatch(E event) {
        for (Consumer<? super E> listener : listeners) {
            try {
                listener.accept(event);
            } catch (RuntimeException ex) {
                System.err.println("Event listener threw an exception: " + ex.getMessage());
                ex.printStackTrace(System.err);
            }
        }
    }
}
