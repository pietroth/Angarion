package br.angarion.dev.engine.network;

import java.lang.foreign.MemorySegment;

public interface MessageSender {
    void sendTo(int[] ids, MemorySegment message);
    void sendTo(int id, MemorySegment message);
    void broadcast(MemorySegment message);
}
