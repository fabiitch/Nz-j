package com.nz.core.event;

import java.util.Objects;

/**
 * Generic event channel that exposes a read-only source and a write-only sink.
 * <p>
 * This separation ensures external consumers can only subscribe, while internal
 * components are responsible for publishing events.
 * </p>
 *
 * <pre>
 * EventChannel<String> channel = EventChannels.copyOnWrite();
 * EventSource<String> source = channel.source();
 * EventSink<String> sink = channel.sink();
 *
 * Subscription subscription = source.subscribe(System.out::println);
 * sink.publish("hello");
 * subscription.close();
 * </pre>
 *
 * @param <E> event type
 */
public final class EventChannel<E> {
    private final ListenerRegistry<E> registry;
    private final EventSource<E> source;
    private final EventSink<E> sink;

    public EventChannel(ListenerRegistry<E> registry) {
        this.registry = Objects.requireNonNull(registry, "registry");
        this.source = this.registry::add;
        this.sink = this.registry::dispatch;
    }

    /**
     * Returns the event source (subscription API).
     */
    public EventSource<E> source() {
        return source;
    }

    /**
     * Returns the event sink (publication API).
     */
    public EventSink<E> sink() {
        return sink;
    }
}
