package com.nz.core.event;

/**
 * Factory for creating {@link EventChannel} instances with common listener registries.
 */
public final class EventChannels {
    private EventChannels() {
    }

    /**
     * Creates an event channel backed by a {@link CopyOnWriteListenerRegistry}.
     * Suitable for read-heavy workloads where dispatch is frequent and mutations are rare.
     */
    public static <E> EventChannel<E> copyOnWrite() {
        return new EventChannel<>(new CopyOnWriteListenerRegistry<>());
    }

    /**
     * Creates an event channel backed by a {@link SynchronizedListenerRegistry}.
     * Suitable for predictable synchronization with fewer listeners or balanced read/write.
     */
    public static <E> EventChannel<E> synchronizedRegistry() {
        return new EventChannel<>(new SynchronizedListenerRegistry<>());
    }
}
