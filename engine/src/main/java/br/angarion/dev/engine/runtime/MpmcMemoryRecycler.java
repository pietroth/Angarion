package br.angarion.dev.engine.runtime;

import java.lang.foreign.MemorySegment;
import org.jctools.queues.MpmcUnboundedXaddArrayQueue;

final class MpmcMemoryRecycler {

    private final MemorySegment rootSegment;

    MpmcMemoryRecycler() {}
}
