package br.angarion.dev.infra.network;

import java.util.ArrayList;

import br.angarion.dev.engine.network.client.ClientConnectedListener;
import br.angarion.dev.engine.network.transport.Server;

public final class NettyQuicServer implements Server {
    private final ArrayList<ClientConnectedListener> listeners = new ArrayList<ClientConnectedListener>();

    @Override
    public final void run() {

    }

    @Override
    public final void subscribe(ClientConnectedListener listener) {
        listeners.add(listener);
    }
}
