package com.nz.nzj.event.bus;

import com.nz.nzj.event.IEventBus;
import com.nz.nzj.event.Listener;

/**
 * Shared base for event bus implementations.
 *
 * This class contains the common fire(...) logic.
 * Concrete implementations only need to provide a listener snapshot.
 *
 * @param <T> event type
 */
public abstract class AbstractEventBus<T> implements IEventBus<T> {

    @Override
    public final void fire(T event) {
        Listener<T>[] snapshot = snapshot();

        for (int i = 0; i < snapshot.length; i++) {
            snapshot[i].onEvent(event);
        }
    }

    /**
     * Returns a stable listener array used during dispatch.
     *
     * For the fast non-thread-safe version, this may be the internal array
     * trimmed to current size.
     *
     * For the thread-safe version, this is usually the current immutable snapshot.
     */
    protected abstract Listener<T>[] snapshot();
}