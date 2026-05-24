package br.angarion.dev.engine.communication;

import java.lang.foreign.MemoryLayout;
import java.lang.foreign.MemorySegment;
import java.lang.foreign.StructLayout;
import java.lang.foreign.ValueLayout;
import java.lang.foreign.MemoryLayout.PathElement;
import java.lang.invoke.VarHandle;

public abstract class ProtocolInConstruction {
    
    public static final StructLayout LAYOUT = MemoryLayout.structLayout(
        ValueLayout.JAVA_INT.withName("totalSize"),
        ValueLayout.JAVA_INT.withName("id")
    ).withByteAlignment(8);

    private static final VarHandle TOTAL_SIZE = 
        LAYOUT.varHandle(PathElement.groupElement("totalSize"));

    private static final VarHandle ID = 
        LAYOUT.varHandle(PathElement.groupElement("id"));

    public static final void writeProtcol(MemorySegment dest, int id, int totalSize) {
        TOTAL_SIZE.set(dest, 0L, totalSize);
        ID.set(dest, 0L, id);
    }

    public static final int getId(MemorySegment src) {
        return (int) ID.get(src, 0L);
    }

    public static final int getTotalSize(MemorySegment src) {
        return (int) TOTAL_SIZE.get(src, 0L);
    }

    public static final long LAYOUT_SIZE = LAYOUT.byteSize();
}
