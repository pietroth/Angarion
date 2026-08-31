package br.angarion.dev.engine.runtime;

import java.lang.foreign.Arena;
import java.lang.foreign.MemorySegment;

public class MemoryBank {
    private final Arena arena;

    public MemoryBank() {
        arena = Arena.ofShared();
    }

    public MemorySegment get(int byteSize) {
        return arena.allocate(byteSize);
    }

    public void close() {
        arena.close();
    }
}
