package com.nz.core.event;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.Test;

class EventChannelTest {
    @Test
    void subscribeReceivesEvents() {
        EventChannel<String> channel = EventChannels.copyOnWrite();
        List<String> received = new ArrayList<>();

        channel.source().subscribe(received::add);
        channel.sink().publish("event");

        assertEquals(Arrays.asList("event"), received);
    }

    @Test
    void unsubscribeStopsReception() {
        EventChannel<String> channel = EventChannels.synchronizedRegistry();
        List<String> received = new ArrayList<>();

        Subscription subscription = channel.source().subscribe(received::add);
        subscription.close();
        channel.sink().publish("event");

        assertEquals(Collections.emptyList(), received);
    }

    @Test
    void multipleSubscribersReceiveSameEvent() {
        EventChannel<String> channel = EventChannels.copyOnWrite();
        List<String> first = new ArrayList<>();
        List<String> second = new ArrayList<>();

        channel.source().subscribe(first::add);
        channel.source().subscribe(second::add);
        channel.sink().publish("event");

        assertEquals(Arrays.asList("event"), first);
        assertEquals(Arrays.asList("event"), second);
    }

    @Test
    void publishIsNotExposedOnEventChannel() {
        assertThrows(NoSuchMethodException.class, () -> EventChannel.class.getMethod("publish", Object.class));
    }
}
