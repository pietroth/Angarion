package br.angarion.dev.engine.network.transport;

import br.angarion.dev.engine.network.client.ClientConnectedListener;

public interface Server extends Runnable {
    void subscribe(ClientConnectedListener listener);
}
