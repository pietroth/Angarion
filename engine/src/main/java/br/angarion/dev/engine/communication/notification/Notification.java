package br.angarion.dev.engine.communication.notification;

import br.angarion.dev.engine.communication.BaseProtocol;
import br.angarion.dev.engine.communication.MBT;
import java.lang.foreign.MemoryLayout;
import java.lang.foreign.MemoryLayout.PathElement;
import java.lang.foreign.MemorySegment;
import java.lang.foreign.StructLayout;
import java.lang.foreign.ValueLayout;
import java.lang.invoke.VarHandle;

public final class Notification {

    private Notification() {}

    /*
        What is the origin ID?
        It simply indicates the ID of the client who made this notification"
    */

    private static final StructLayout LAYOUT = MemoryLayout.structLayout(
        BaseProtocol.LAYOUT.withName("protocol"), // 4 bytes (1 int)
        MBT.LAYOUT.withName("mbt"), // 4 bytes (2 shorts)
        ValueLayout.JAVA_INT.withName("originId") // 4 bytes
    ).withByteAlignment(4);

    private static final VarHandle PROTOCOL_TOTAL_SIZE = LAYOUT.varHandle(
        PathElement.groupElement("protocol"),
        PathElement.groupElement("totalSize")
    );

    private static final VarHandle MBT_FAMILY = LAYOUT.varHandle(
        PathElement.groupElement("mbt"),
        PathElement.groupElement("family")
    );

    private static final VarHandle MBT_TYPE = LAYOUT.varHandle(
        PathElement.groupElement("mbt"),
        PathElement.groupElement("family")
    );

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
        PROTOCOL_TOTAL_SIZE.set(dest, 0L, totalSize);
        MBT_FAMILY.set(dest, 0L, familyId);
        MBT_TYPE.set(dest, 0L, typeId);
        ORIGIN_ID.set(dest, 0L, originId);
    }

    public static final long HEADER_SIZE = LAYOUT.byteSize();

    public static final MemorySegment payloadSlice(MemorySegment message) {
        return message.asSlice(HEADER_SIZE, message.byteSize() - HEADER_SIZE);
    }
}
