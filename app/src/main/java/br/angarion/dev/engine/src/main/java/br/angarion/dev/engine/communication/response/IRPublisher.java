package br.angarion.dev.engine.communication.response;

import java.lang.foreign.MemorySegment;

import br.angarion.dev.engine.network.MessageDeliveryHandler;

public class IRPublisher {
    private final MessageDeliveryHandler delivery;

    public IRPublisher(MessageDeliveryHandler delivery) {
        this.delivery = delivery;
    }

    public void publish(MemorySegment ir, int originId) {
        delivery.deliveryIr(ir, originId);
        System.out.println("Published IR. CorrelationId: " + IRInConstruction.getCorrelationId(ir) + ", Status: " + IRInConstruction.getStatus(ir) + ", OriginId: " + originId);
    }
}
