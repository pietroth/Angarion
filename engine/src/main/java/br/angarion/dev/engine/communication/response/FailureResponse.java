package br.angarion.dev.engine.communication.response;

import java.lang.foreign.MemoryLayout;
import java.lang.foreign.MemorySegment;

public final class FailureResponse {

    private FailureResponse() {}

    private static final MemoryLayout LAYOUT = MemoryLayout.structLayout(
        Response.LAYOUT.withName("base")
    );

    public static final void writeHeader(MemorySegment src, int correlationId) {
        if (correlationId < 0 || correlationId > 0xFFFF) // Unsigned short (0..65535)
            throw new IllegalArgumentException("correlationId must be between 0 and 65535");

        Response.CORRELATION_ID.set(src, 0L, correlationId);
        Response.STATUS.set(src, 0L, Response.FAILURE);
    }

    public static final int getStatus(MemorySegment src) {
        short value = (short) Response.STATUS.get(src, 0L);
        return Short.toUnsignedInt(value);
    }

    public static final int getCorrelationId(MemorySegment src) {
        short value = (short) Response.CORRELATION_ID.get(src, 0L);
        return Short.toUnsignedInt(value);
    }

    public static final int HEADER_SIZE = (int) LAYOUT.byteSize();
}
