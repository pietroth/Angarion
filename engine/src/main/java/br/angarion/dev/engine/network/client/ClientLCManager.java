package br.angarion.dev.engine.network.client;

import java.lang.foreign.MemorySegment;
import java.util.function.Consumer;

// LC means LifeCycle

public interface ClientLCManager<T extends Client> extends
    ClientConnectedListener<T> {
    void sendTo(int[] ids, MemorySegment message);
    void sendTo(int id, MemorySegment message);
    void broadcast(MemorySegment message);
    void disconnectClient(int id);
    void forEachOnline(Consumer<T> consumer);
    int onlineCount();
    boolean isConnected(int id);
    void subscribe(ClientProcessedListener<T> listener);
}
