package br.angarion.dev.engine.network.client;

import java.util.function.Consumer;

// LC means LifeCycle

public interface ClientLCManager {
    void disconnectClient(int id);
    void forEachOnline(Consumer<Client> consumer);
    int onlineCount();
    boolean isConnected(int id);
    void subscribe(ClientProcessedListener listener);
    void subscribe(ClientConnectedListener listener);
    int connect(Client client);
}
