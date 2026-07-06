package br.angarion.dev.engine.communication.response;

import java.lang.foreign.MemoryLayout;
import java.lang.foreign.MemoryLayout.PathElement;
import java.lang.foreign.MemorySegment;
import java.lang.foreign.ValueLayout;
import java.lang.invoke.VarHandle;

public final class DeniedResponse {

    private DeniedResponse() {}

    private static final MemoryLayout LAYOUT = MemoryLayout.structLayout(
        Response.LAYOUT.withName("base"), // 5 bytes (1 int, 1 byte)
        ValueLayout.JAVA_SHORT.withName("reasonCode")
    ).withByteAlignment(4);

    private static final VarHandle REASON_CODE = LAYOUT.varHandle(
        PathElement.groupElement("reasonCode")
    );

    public static final void writeHeader(
        MemorySegment src,
        int correlationId,
        int errorCode
    ) {
        if (errorCode < 0 || errorCode > 65.535) {
            throw new IllegalArgumentException(
                "errorCode must be between 0 and 65.535"
            );
        }

        Response.CORRELATION_ID.set(src, 0L, correlationId);
        Response.STATUS.set(src, 0L, Response.DENIED);
        REASON_CODE.set(src, 0L, (short) errorCode);
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
}
