package br.angarion.dev.engine.communication;

import java.lang.foreign.MemoryLayout;
import java.lang.foreign.MemoryLayout.PathElement;
import java.lang.foreign.MemorySegment;
import java.lang.foreign.StructLayout;
import java.lang.foreign.ValueLayout;
import java.lang.invoke.VarHandle;
import java.nio.ByteOrder;

public final class BaseProtocol {

    private BaseProtocol() {}

    public static final StructLayout LAYOUT = MemoryLayout.structLayout(
        ValueLayout.JAVA_INT.withName("totalSize").withOrder(
            ByteOrder.BIG_ENDIAN
        )
    );

    public static final long LAYOUT_SIZE = LAYOUT.byteSize();

    public static final VarHandle TOTAL_SIZE = LAYOUT.varHandle(
        PathElement.groupElement("totalSize")
    );

    public static final int getTotalSize(MemorySegment src) {
        return (int) TOTAL_SIZE.get(src, 0L);
    }
}
