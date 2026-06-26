package br.angarion.dev.engine.network.transport;

import br.angarion.dev.engine.network.client.Client;
import br.angarion.dev.engine.network.client.ClientConnectedListener;

public interface Server<T extends Client> extends Runnable {
    void subscribe(ClientConnectedListener<T> listener);
}
