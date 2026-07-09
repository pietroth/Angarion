package br.angarion.dev.engine.network.client;

import java.lang.foreign.MemorySegment;
import java.util.function.Consumer;

// LC means LifeCycle

public interface ClientLCManager<T extends Client> extends
    ClientConnectedListener<T> {
    void disconnectClient(int id);
    void forEachOnline(Consumer<T> consumer);
    int onlineCount();
    boolean isConnected(int id);
    void subscribe(ClientProcessedListener<T> listener);
}
