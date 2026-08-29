package br.angarion.dev.engine.communication.intention;

import br.angarion.dev.engine.communication.BaseProtocol;
import br.angarion.dev.engine.communication.MBT;
import java.lang.foreign.MemoryLayout;
import java.lang.foreign.MemoryLayout.PathElement;
import java.lang.foreign.MemorySegment;
import java.lang.foreign.StructLayout;
import java.lang.foreign.ValueLayout;
import java.lang.invoke.VarHandle;
import java.nio.ByteOrder;

public final class Intention {

    private Intention() {}

    /*
        What's correlation Id?
        It's just the id that links the intentions with the IRs.
    */

    private static final StructLayout LAYOUT = MemoryLayout.structLayout(
        BaseProtocol.LAYOUT.withName("protocol"), // 4 bytes (1 int)
        MBT.LAYOUT.withName("mbt"), // 4 bytes (1 int),
        ValueLayout.JAVA_INT.withName("correlationId").withOrder(
            ByteOrder.BIG_ENDIAN
        )
    );

    private static final VarHandle CORRELATION_ID = LAYOUT.varHandle(
        PathElement.groupElement("correlationId")
    );

    public static final void writeHeader(
        MemorySegment dest,
        int typeId,
        int totalSize,
        int correlationId
    ) {
        if (typeId < Short.MIN_VALUE || typeId > Short.MAX_VALUE)
            throw new IllegalArgumentException("typeId must be between " + Short.MIN_VALUE + " and " + Short.MAX_VALUE);

        BaseProtocol.TOTAL_SIZE.set(dest, 0L, totalSize);
        MBT.TYPE.set(dest, 0L, typeId);
        CORRELATION_ID.set(dest, 0L, correlationId);
    }

    public static final int getCorrelationId(MemorySegment src) {
        return (int) CORRELATION_ID.get(src, 0L);
    }

    public static final int getTotalSize(MemorySegment src) {
        return (int) BaseProtocol.TOTAL_SIZE.get(src, 0L);
    }

    public static final int getType(MemorySegment src) {
        return (int) MBT.TYPE.get(src, 0L);
    }

    public static final long HEADER_SIZE = LAYOUT.byteSize();

    public static final MemorySegment payloadSlice(MemorySegment message) {
        return message.asSlice(HEADER_SIZE, message.byteSize() - HEADER_SIZE);
    }
}
