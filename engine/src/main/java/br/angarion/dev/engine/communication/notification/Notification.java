package br.angarion.dev.engine.communication.notification;

import br.angarion.dev.engine.communication.BaseProtocol;
import br.angarion.dev.engine.communication.MBT;
import java.lang.foreign.MemoryLayout;
import java.lang.foreign.MemorySegment;
import java.lang.foreign.StructLayout;

public final class Notification {

    private Notification() {}

    private static final StructLayout LAYOUT = MemoryLayout.structLayout(
        BaseProtocol.LAYOUT.withName("protocol"), // 2 bytes (1 short)
        MBT.LAYOUT.withName("mbt") // 2 bytes (1 short)
    ).withByteAlignment(4);

    public static final void writeHeader(
        MemorySegment dest,
        int typeId,
        int totalSize
    ) {
        if (typeId < 0 || typeId > 0xFFFF) // Unsigned short (0..65535)
            throw new IllegalArgumentException("typeId must be between 0 and 65.535");

        if (totalSize > 1460)
            throw new IllegalArgumentException("totalSize max: 1460");

        BaseProtocol.TOTAL_SIZE.set(dest, 0L, totalSize);
        MBT.TYPE.set(dest, 0L, typeId);
    }

    public static final int getType(MemorySegment src) {
        short value = (short) MBT.TYPE.get(src, 0L);
        return Short.toUnsignedInt(value);
    }

    public static final int getTotalSize(MemorySegment src) {
        short value = (short) BaseProtocol.TOTAL_SIZE.get(src, 0L);
        return Short.toUnsignedInt(value);
    }

    public static final long HEADER_SIZE = LAYOUT.byteSize();

    public static final MemorySegment payloadSlice(MemorySegment message) {
        return message.asSlice(HEADER_SIZE, message.byteSize() - HEADER_SIZE);
    }
}
