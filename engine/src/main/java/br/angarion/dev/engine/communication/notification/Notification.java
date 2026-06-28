package br.angarion.dev.engine.communication.notification;

import br.angarion.dev.engine.communication.BaseProtocol;
import br.angarion.dev.engine.communication.MBT;
import java.lang.foreign.MemoryLayout;
import java.lang.foreign.MemorySegment;
import java.lang.foreign.StructLayout;

public final class Notification {

    private Notification() {}

    private static final StructLayout LAYOUT = MemoryLayout.structLayout(
        BaseProtocol.LAYOUT.withName("protocol"), // 4 bytes (1 int)
        MBT.LAYOUT.withName("mbt") // 4 bytes (2 shorts)
    ).withByteAlignment(4);

    public static final void writeHeader(
        MemorySegment dest,
        int familyId,
        int typeId,
        int totalSize
    ) {
        BaseProtocol.TOTAL_SIZE.set(dest, 0L, totalSize);
        MBT.FAMILY.set(dest, 0L, familyId);
        MBT.TYPE.set(dest, 0L, typeId);
    }

    public static final int getFamily(MemorySegment src) {
        return (int) MBT.FAMILY.get(src, 0L);
    }

    public static final int getType(MemorySegment src) {
        return (int) MBT.TYPE.get(src, 0L);
    }

    public static final int getTotalSize(MemorySegment src) {
        return (int) BaseProtocol.TOTAL_SIZE.get(src, 0L);
    }

    public static final long HEADER_SIZE = LAYOUT.byteSize();

    public static final MemorySegment payloadSlice(MemorySegment message) {
        return message.asSlice(HEADER_SIZE, message.byteSize() - HEADER_SIZE);
    }
}
