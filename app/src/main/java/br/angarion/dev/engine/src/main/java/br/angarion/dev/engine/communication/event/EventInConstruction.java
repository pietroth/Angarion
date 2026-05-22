package br.angarion.dev.engine.communication.event;

import java.lang.foreign.MemoryLayout;
import java.lang.foreign.MemorySegment;
import java.lang.foreign.StructLayout;
import java.lang.foreign.ValueLayout;
import java.lang.invoke.VarHandle;

public final class EventInConstruction {

    private static final StructLayout LAYOUT = MemoryLayout.structLayout(
        ValueLayout.JAVA_INT.withName("originId")
    );

    private static final VarHandle ORIGIN_ID = 
        LAYOUT.varHandle(MemoryLayout.PathElement.groupElement("originId"));

    public static final void writeEvent(MemorySegment dest, int originId) {
        ORIGIN_ID.set(dest, 0L, originId);
    }

    public static final int getOriginId(MemorySegment src) {
        return (int) ORIGIN_ID.get(src);
    }

    public static final long LAYOUT_SIZE = LAYOUT.byteSize();
}
