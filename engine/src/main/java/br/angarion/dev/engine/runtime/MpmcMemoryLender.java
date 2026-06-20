package br.angarion.dev.engine.runtime;

import java.lang.foreign.MemorySegment;
import org.jctools.queues.MpmcUnboundedXaddArrayQueue;

final class MpmcMemoryLender implements MemoryLender {

    private final MemorySegment rootSegment;

    private final MpmcMemoryLender() {
        rootSegment = MemorySegment.NULL;
    }

    public MemorySegment borrow(int size) {
        return MemorySegment.NULL;
    }

    public void giveBack(MemorySegment segment) {}
}
