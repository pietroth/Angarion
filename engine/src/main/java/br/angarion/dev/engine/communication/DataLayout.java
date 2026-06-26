package br.angarion.dev.engine.communication;

import java.lang.foreign.MemorySegment;

public interface DataLayout {
    long size();
    void write(MemorySegment dest);
    boolean isNotification();
}
