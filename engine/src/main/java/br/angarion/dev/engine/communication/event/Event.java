package br.angarion.dev.engine.communication.event;

import br.angarion.dev.engine.communication.BaseProtocol;
import br.angarion.dev.engine.communication.MBT;
import java.lang.foreign.MemoryLayout;
import java.lang.foreign.MemoryLayout.PathElement;
import java.lang.foreign.MemorySegment;
import java.lang.foreign.StructLayout;
import java.lang.foreign.ValueLayout;
import java.lang.invoke.VarHandle;
import java.nio.ByteOrder;

public final class Event {

    private Event() {}

    /*
        What is the origin ID?
        It simply indicates the Client ID of the client who made the intention that triggered this event."
    */

    private static final StructLayout LAYOUT = MemoryLayout.structLayout(
        BaseProtocol.LAYOUT.withName("protocol"), // 2 bytes (1 short)
        MBT.LAYOUT.withName("mbt"), // 2 bytes (1 short)
        ValueLayout.JAVA_SHORT.withName("originId").withOrder(
            ByteOrder.BIG_ENDIAN
        )
    );

    private static final VarHandle ORIGIN_ID = LAYOUT.varHandle(
        PathElement.groupElement("originId")
    );

    public static final void writeHeader(
        MemorySegment dest,
        int typeId,
        int totalSize,
        int originId
    ) {
        if (typeId < 0 || typeId > 0xFFFF) // Unsigned short (0..65535)
            throw new IllegalArgumentException("typeId must be between 0 and 65.535");

        if (totalSize > 1460)
            throw new IllegalArgumentException("totalSize max: 1460");

        if (originId < 0 || originId > 0xFFFF) // Unsigned short (0..65535)
        throw new IllegalArgumentException("originId must be between 0 and 65.535");

        BaseProtocol.TOTAL_SIZE.set(dest, 0L, totalSize);
        MBT.TYPE.set(dest, 0L, typeId);
        ORIGIN_ID.set(dest, 0L, originId);
    }

    public static final int getOriginId(MemorySegment src) {
        short value = (short) ORIGIN_ID.get(src, 0L);
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
