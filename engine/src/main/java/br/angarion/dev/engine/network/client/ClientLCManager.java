package br.angarion.dev.engine.network.client;

import it.unimi.dsi.fastutil.ints.IntObjectBiConsumer;

// LC means LifeCycle

public interface ClientLCManager<T extends Client> {
    void disconnectClient(int id);
    void forEachOnline(IntObjectBiConsumer<T> consumer);
    int onlineCount();
    boolean isConnected(int id);
    void subscribe(ClientProcessedListener listener);
    void subscribe(ClientConnectedListener listener);
    int connect(T client);
}
