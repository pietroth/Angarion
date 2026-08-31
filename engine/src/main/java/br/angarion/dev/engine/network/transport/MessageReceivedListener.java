package br.angarion.dev.engine.network.transport;

import br.angarion.dev.engine.network.protocol.MessageReceivedWrapper;

public interface MessageReceivedListener {
    void onMessageReceived(MessageReceivedWrapper messageReceivedWrapper);
}
