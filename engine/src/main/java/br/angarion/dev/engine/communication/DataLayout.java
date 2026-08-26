package br.angarion.dev.engine.communication;

import java.lang.foreign.MemorySegment;

public interface DataLayout {
    int size();
    void write(MemorySegment dest, int offset);
    boolean isNotification();
    boolean isCpuIntensive();
    boolean isBlocking();
    String family();
}
