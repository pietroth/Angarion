package br.angarion.dev.engine.communication;

import java.lang.foreign.MemoryLayout;
import java.lang.foreign.MemorySegment;
import java.lang.foreign.StructLayout;
import java.lang.foreign.ValueLayout;
import java.lang.foreign.MemoryLayout.PathElement;
import java.lang.invoke.VarHandle;

// MI means Message with Identification;

public abstract class MIInConstruction {
    
    public static final StructLayout LAYOUT = MemoryLayout.structLayout(
        ValueLayout.JAVA_INT.withName("id")
    );

    private static final VarHandle ID = 
        LAYOUT.varHandle(PathElement.groupElement("id"));

    public static final void writeMI(MemorySegment dest, int id) {
        ID.set(dest);
    }

    public static final int getId(MemorySegment src) {
        return (int) ID.get(src);
    }

    public static final long LAYOUT_SIZE = LAYOUT.byteSize();
}
