package br.angarion.dev.engine.network;

import java.lang.foreign.MemorySegment;

import br.angarion.dev.api.communication.TargetModifier;
import br.angarion.dev.api.communication.TargetScope;
import br.angarion.dev.engine.network.client.ClientLCManager;

public class MessageDeliveryHandler {
    private final ClientLCManager clientLCManager;

    public MessageDeliveryHandler(ClientLCManager clientLCManager) {
        this.clientLCManager = clientLCManager;
    }

    public void deliveryEvent(MemorySegment segment, int removedId, TargetScope target) {
        if (target.forAllClients) {
            clientLCManager.sendToAll(segment);
            return;
        }

        TargetModifier modifier = target.modifier;
        clientLCManager.sendTo(modifier.exclude(removedId).toArray(), segment);
    }

    public void deliveryIr(MemorySegment segment, int targetId) {
        clientLCManager.sendTo(new int[] { targetId }, segment);
    }
}
