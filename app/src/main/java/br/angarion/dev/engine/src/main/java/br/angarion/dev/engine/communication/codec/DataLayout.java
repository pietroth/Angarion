package br.angarion.dev.engine.communication.codec;

import java.lang.foreign.MemorySegment;

public interface DataLayout {
    long size();
    void write(MemorySegment dest);
}
