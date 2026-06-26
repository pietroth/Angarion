package br.angarion.dev.engine.communication;

import java.lang.foreign.MemoryLayout;
import java.lang.foreign.MemoryLayout.PathElement;
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

    public static final VarHandle FAMILY = LAYOUT.varHandle(
        PathElement.groupElement("family")
    );

    public static final VarHandle TYPE = LAYOUT.varHandle(
        PathElement.groupElement("type")
    );
}
