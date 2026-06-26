package br.angarion.dev.engine.network.transport;

import java.lang.foreign.MemorySegment;

public interface MessageReceivedListener {
    void onMessageReceived(int clientId, MemorySegment segment);
}
