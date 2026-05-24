package br.angarion.dev.engine.communication;

import java.lang.foreign.MemoryLayout;
import java.lang.foreign.StructLayout;
import java.lang.foreign.ValueLayout;

// MBT means Message Based on Types (family + specific type)

public final class MBT {
    
    public static final StructLayout LAYOUT = MemoryLayout.structLayout(
        ValueLayout.JAVA_INT.withName("id")
    );

    public static final long LAYOUT_SIZE = LAYOUT.byteSize();

    public static final int packTypeAndFamily(short type, short family) {
        return (type << 16) | (family & 0xFFFF);
    }

    public static final short unpackType(int id) {
        return (short) (id >> 16);
    }

    public static final short unpackFamily(int id) {
        return (short) (id & 0xFFFF);
    }
}
