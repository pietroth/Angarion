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
        delivery.deliveryEvent(event, EventInConstruction.getOriginId(event), targetScope);
        System.out.println("Published event. Family: " + EventInConstruction.getUnpackedFamily(event) + ", Type: " + EventInConstruction.getUnpackedType(event) + ", OriginId: " + EventInConstruction.getOriginId(event) + ", Target Size: " + targetScope.modifier.toArray().length);
    }
}
