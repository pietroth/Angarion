package br.angarion.dev.engine.communication.codec;

import java.lang.foreign.MemorySegment;

import br.angarion.dev.engine.communication.MIDFData;

public interface Codec<T extends MIDFData> {
    int size();
    int size(T data);
    void encode(MemorySegment dest, T data);
    T decode(MemorySegment src);
}
