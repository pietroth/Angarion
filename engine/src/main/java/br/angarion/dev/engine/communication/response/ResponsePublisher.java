package br.angarion.dev.engine.communication.response;

import br.angarion.dev.engine.network.MessageSender;
import java.lang.foreign.MemorySegment;

public final class ResponsePublisher {

    private final MessageSender messageSender;

    public ResponsePublisher(MessageSender messageSender) {
        this.messageSender = messageSender;
    }

    public final void publish(MemorySegment response, int targetId) {
        messageSender.sendTo(targetId, response);
        System.out.println("Response published.");
    }
}
