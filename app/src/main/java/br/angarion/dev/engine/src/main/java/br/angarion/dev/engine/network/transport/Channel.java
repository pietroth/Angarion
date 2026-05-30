package br.angarion.dev.engine.network.transport;

import java.io.IOException;
import java.lang.foreign.Arena;
import java.lang.foreign.MemorySegment;

public interface Channel {
    MemorySegment read(Arena arena) throws IOException;
    void send(MemorySegment segment) throws IOException;
    void close() throws IOException;
    void subscribe(MessageReceivedListener listener);
    void unsubscribe(MessageReceivedListener listener);
    void setId(int id);
    int getId();
}