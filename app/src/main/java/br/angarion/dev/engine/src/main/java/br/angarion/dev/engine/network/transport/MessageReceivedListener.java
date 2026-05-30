package br.angarion.dev.engine.network.transport;

import java.lang.foreign.MemorySegment;

public interface MessageReceivedListener {
    void onMessageReceived(Channel channel, MemorySegment segment);
}
