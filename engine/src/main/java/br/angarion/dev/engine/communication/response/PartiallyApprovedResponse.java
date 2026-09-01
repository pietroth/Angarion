package br.angarion.dev.engine.communication.response;

import java.lang.foreign.MemoryLayout;
import java.lang.foreign.MemoryLayout.PathElement;
import java.lang.foreign.MemorySegment;
import java.lang.foreign.ValueLayout;
import java.lang.invoke.VarHandle;
import java.nio.ByteOrder;

public final class PartiallyApprovedResponse {

    private PartiallyApprovedResponse() {}

    private static final MemoryLayout LAYOUT = MemoryLayout.structLayout(
        Response.LAYOUT.withName("base"), // 4 bytes (1 short, 1 byte, 1 padding)
        ValueLayout.JAVA_SHORT.withName("reasonCode").withOrder(ByteOrder.BIG_ENDIAN)
    );

    private static final VarHandle REASON_CODE = LAYOUT.varHandle(
        PathElement.groupElement("reasonCode")
    );

    public static final void writeHeader(
        MemorySegment src,
        int correlationId,
        int reasonCode
    ) {
        if (reasonCode < 0 || reasonCode > 0xFFFF) // Unsigned short (0...65535)
            throw new IllegalArgumentException("reasonCode must be between 0 and 65.535");

        if (correlationId < 0 || correlationId > 0xFFFF) // Unsigned short (0...65535)
            throw new IllegalArgumentException("correlationId must be between 0 and 65.535");

        Response.CORRELATION_ID.set(src, 0L, correlationId);
        Response.STATUS.set(src, 0L, Response.PARTIALLY_APPROVED);
        REASON_CODE.set(src, 0L, (short) reasonCode);
    }

    public static final int getStatus(MemorySegment src) {
        byte value = (byte) Response.STATUS.get(src, 0L);
        return (int) Byte.toUnsignedInt(value);
    }

    public static final int getCorrelationId(MemorySegment src) {
        byte value = (byte) Response.CORRELATION_ID.get(src, 0L);
        return (int) Byte.toUnsignedInt(value);
    }

    public static final int getReasonCode(MemorySegment src) {
        short value = (short) REASON_CODE.get(src, 0L);
        return Short.toUnsignedInt(value);
    }

    public static final int HEADER_SIZE = (int) LAYOUT.byteSize();

    public static final MemorySegment payloadSlice(MemorySegment message) {
        return message.asSlice(HEADER_SIZE, message.byteSize() - HEADER_SIZE);
    }
}
