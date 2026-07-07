package br.angarion.dev.engine.communication.response;

import br.angarion.dev.engine.network.MessageDeliveryHandler;
import java.lang.foreign.MemorySegment;

public final class ResponsePublisher {

    private final MessageDeliveryHandler deliveryHandler;

    public ResponsePublisher(MessageDeliveryHandler deliveryHandler) {
        this.deliveryHandler = deliveryHandler;
    }

    public final void publish(MemorySegment response, int targetId) {
        deliveryHandler.deliveryIr(response, targetId);
        System.out.println("Response published.");
    }
}
