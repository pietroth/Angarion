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
        BaseProtocol.LAYOUT.withName("protocol"), // 2 bytes (1 short)
        MBT.LAYOUT.withName("mbt"), // 2 bytes (1 short),
        ValueLayout.JAVA_SHORT.withName("correlationId").withOrder(
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
        if (typeId < 0 || typeId > 0xFFFF) // Unsigned short (0...65535)
            throw new IllegalArgumentException("typeId must be between 0 and 65.535");

        if (correlationId < 0 || correlationId > 0xFFFF) // Unsigned short (0...65535)
            throw new IllegalArgumentException("correlationId must be between 0 and 65.535");

        if (totalSize > 1460)
            throw new IllegalArgumentException("totalSize max: 1460");

        BaseProtocol.TOTAL_SIZE.set(dest, 0L, totalSize);
        MBT.TYPE.set(dest, 0L, typeId);
        CORRELATION_ID.set(dest, 0L, correlationId);
    }

    public static final int getCorrelationId(MemorySegment src) {
        short value = (short) CORRELATION_ID.get(src, 0L);
        return Short.toUnsignedInt(value);
    }

    public static final int getTotalSize(MemorySegment src) {
        short value = (short) BaseProtocol.TOTAL_SIZE.get(src, 0L);
        return Short.toUnsignedInt(value);
    }

    public static final int getType(MemorySegment src) {
        short value = (short) MBT.TYPE.get(src, 0L);
        return Short.toUnsignedInt(value);
    }

    public static final long HEADER_SIZE = LAYOUT.byteSize();

    public static final MemorySegment payloadSlice(MemorySegment message) {
        return message.asSlice(HEADER_SIZE, message.byteSize() - HEADER_SIZE);
    }
}
