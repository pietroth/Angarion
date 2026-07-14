package br.angarion.dev.engine.communication.event;

import br.angarion.dev.api.communication.TargetModifier;
import br.angarion.dev.api.communication.TargetScope;
import br.angarion.dev.engine.network.MessageSender;
import java.lang.foreign.MemorySegment;

public final class EventPublisher {

    private final MessageSender messageSender;

    public EventPublisher(MessageSender messageSender) {
        this.messageSender = messageSender;
    }

    public final void publish(MemorySegment event, TargetScope targetScope) {
        if (targetScope.broadcast) {
            messageSender.broadcast(event);
            return;
        }

        TargetModifier modifier = targetScope.modifier;
        messageSender.sendTo(modifier.toArray(), event);
        System.out.println(
            "Published event. Type: " +
                Event.getType(event) +
                ", OriginId: " +
                Event.getOriginId(event)
        );
    }
}
