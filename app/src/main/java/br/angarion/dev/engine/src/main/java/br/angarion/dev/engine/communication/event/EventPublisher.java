package br.angarion.dev.engine.communication.event;

import java.lang.foreign.MemorySegment;

import br.angarion.dev.api.communication.TargetScope;
import br.angarion.dev.engine.network.MessageDeliveryHandler;

public class EventPublisher {
    private final MessageDeliveryHandler delivery;

    public EventPublisher(MessageDeliveryHandler delivery) {
        this.delivery = delivery;

    }

    public void publish(MemorySegment event, TargetScope targetScope) {
        delivery.deliveryEvent(event, Event.getOriginId(event), targetScope);
        System.out.println("Published event. Family: " + Event.getUnpackedFamily(event) + ", Type: " + Event.getUnpackedType(event) + ", OriginId: " + Event.getOriginId(event) + ", Target Size: " + targetScope.modifier.toArray().length);
    }
}
