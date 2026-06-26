package br.angarion.dev.engine.network.protocol;

import br.angarion.dev.engine.network.transport.MessageReceivedListener;
import java.lang.foreign.MemorySegment;

final class ObedientGateway implements MessageReceivedListener {

    public ObedientGateway() {}

    public void onMessageReceived(int clientId, MemorySegment message) {}
}
