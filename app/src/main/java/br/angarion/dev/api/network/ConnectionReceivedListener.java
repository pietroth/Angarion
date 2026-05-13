package br.angarion.dev.api.network;

import java.lang.foreign.MemorySegment;

public interface ConnectionReceivedListener {
    void onConnectionReceived(Connection connection, MemorySegment segment);
}
