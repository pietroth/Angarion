package br.angarion.dev.engine.communication.response;

import java.lang.foreign.MemoryLayout;
import java.lang.foreign.MemoryLayout.PathElement;
import java.lang.foreign.MemorySegment;
import java.lang.foreign.ValueLayout;
import java.lang.invoke.VarHandle;

public final class PartiallyApprovedResponse {

    private PartiallyApprovedResponse() {}

    private static final MemoryLayout LAYOUT = MemoryLayout.structLayout(
        Response.LAYOUT.withName("base"), // 5 bytes (1 int, 1 byte)
        MemoryLayout.paddingLayout(1),
        ValueLayout.JAVA_SHORT.withName("reasonCode")
    );

    private static final VarHandle REASON_CODE = LAYOUT.varHandle(
        PathElement.groupElement("reasonCode")
    );

    public static final void writeHeader(
        MemorySegment src,
        int correlationId,
        int reasonCode
    ) {
        if (reasonCode < 0 || reasonCode > 65535) {
            throw new IllegalArgumentException(
                "reasonCode must be between 0 and 65535"
            );
        }

        Response.CORRELATION_ID.set(src, 0L, correlationId);
        Response.STATUS.set(src, 0L, Response.PARTIALLY_APPROVED);
        REASON_CODE.set(src, 0L, (short) reasonCode);
    }

    public static final int getStatus(MemorySegment src) {
        return (int) Response.STATUS.get(src, 0L);
    }

    public static final int getCorrelationId(MemorySegment src) {
        return (int) Response.CORRELATION_ID.get(src, 0L);
    }

    public static final int getReasonCode(MemorySegment src) {
        return (short) REASON_CODE.get(src, 0L) & 0xFFFF;
    }

    public static final int HEADER_SIZE = (int) LAYOUT.byteSize();

    public static final MemorySegment payloadSlice(MemorySegment message) {
        return message.asSlice(HEADER_SIZE, message.byteSize() - HEADER_SIZE);
    }
}
