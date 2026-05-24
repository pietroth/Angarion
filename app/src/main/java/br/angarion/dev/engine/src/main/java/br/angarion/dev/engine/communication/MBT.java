package br.angarion.dev.engine.communication;

import java.lang.foreign.MemoryLayout;
import java.lang.foreign.StructLayout;
import java.lang.foreign.ValueLayout;

// MBT means Message Based on Types (family + specific type)

public final class MBT {
    
    public static final StructLayout LAYOUT = MemoryLayout.structLayout(
        ValueLayout.JAVA_INT.withName("typeId")
    );

    public static final long LAYOUT_SIZE = LAYOUT.byteSize();
}
