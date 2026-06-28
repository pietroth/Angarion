package br.angarion.dev.engine.communication;

import java.lang.foreign.MemoryLayout;
import java.lang.foreign.MemoryLayout.PathElement;
import java.lang.foreign.MemorySegment;
import java.lang.foreign.StructLayout;
import java.lang.foreign.ValueLayout;
import java.lang.invoke.VarHandle;
import java.nio.ByteOrder;

// MBT means Message Based on Types (family + specific type)

public final class MBT {

    public static final StructLayout LAYOUT = MemoryLayout.structLayout(
        ValueLayout.JAVA_SHORT.withName("family").withOrder(
            ByteOrder.BIG_ENDIAN
        ),
        ValueLayout.JAVA_SHORT.withName("type").withOrder(ByteOrder.BIG_ENDIAN)
    );

    public static final long LAYOUT_SIZE = LAYOUT.byteSize();

    private static final VarHandle FAMILY = LAYOUT.varHandle(
        PathElement.groupElement("family")
    );

    private static final VarHandle TYPE = LAYOUT.varHandle(
        PathElement.groupElement("type")
    );

    public static final int getType(MemorySegment src) {
        return (int) TYPE.get(src, 0L);
    }

    public static final int getFamily(MemorySegment src) {
        return (int) FAMILY.get(src, 0L);
    }
}
