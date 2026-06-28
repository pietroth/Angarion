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
        BaseProtocol.LAYOUT.withName("protocol"), // 4 bytes (1 int)
        MBT.LAYOUT.withName("mbt"), // 4 bytes (1 int)
        ValueLayout.JAVA_INT.withName("originId").withOrder(
            ByteOrder.BIG_ENDIAN
        )
    ).withByteAlignment(8);

    private static final VarHandle ORIGIN_ID = LAYOUT.varHandle(
        PathElement.groupElement("originId")
    );

    public static final void writeHeader(
        MemorySegment dest,
        int familyId,
        int typeId,
        int totalSize,
        int originId
    ) {
        BaseProtocol.TOTAL_SIZE.set(dest, 0L, totalSize);
        MBT.FAMILY.set(dest, 0L, familyId);
        MBT.TYPE.set(dest, 0L, typeId);
        ORIGIN_ID.set(dest, 0L, originId);
    }

    public static final int getOriginId(MemorySegment src) {
        return (int) ORIGIN_ID.get(src, 0L);
    }

    public static final int getTotalSize(MemorySegment src) {
        return (int) BaseProtocol.TOTAL_SIZE.get(src, 0L);
    }

    public static final int getFamily(MemorySegment src) {
        return (int) MBT.FAMILY.get(src, 0L);
    }

    public static final int getType(MemorySegment src) {
        return (int) MBT.TYPE.get(src, 0L);
    }

    public static final long HEADER_SIZE = LAYOUT.byteSize();

    public static final MemorySegment payloadSlice(MemorySegment message) {
        return message.asSlice(HEADER_SIZE, message.byteSize() - HEADER_SIZE);
    }
}
