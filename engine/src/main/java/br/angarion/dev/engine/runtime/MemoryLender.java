package br.angarion.dev.engine.runtime;

import java.lang.foreign.MemorySegment;

interface MemoryLender {
    MemorySegment borrow(int size);
    void giveBack(MemorySegment segment);
}
