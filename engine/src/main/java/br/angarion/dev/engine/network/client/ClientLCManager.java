package br.angarion.dev.engine.network.client;

import java.util.function.Consumer;

// LC means LifeCycle

public interface ClientLCManager extends ClientConnectedListener {
    void disconnectClient(int id);
    void forEachOnline(Consumer<Client> consumer);
    int onlineCount();
    boolean isConnected(int id);
    void subscribe(ClientProcessedListener listener);
}
