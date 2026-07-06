package br.angarion.dev.engine.communication.response;

import java.lang.foreign.MemoryLayout;
import java.lang.foreign.MemorySegment;

public final class ApprovedResponse {

    private ApprovedResponse() {}

    private static final MemoryLayout LAYOUT = MemoryLayout.structLayout(
        Response.LAYOUT.withName("base")
    );

    public static final void writeHeader(MemorySegment src, int correlationId) {
        Response.CORRELATION_ID.set(src, 0L, correlationId);
        Response.STATUS.set(src, 0L, Response.APPROVED);
    }

    public static final int getStatus(MemorySegment src) {
        return (int) Response.STATUS.get(src, 0L);
    }

    public static final int getCorrelationId(MemorySegment src) {
        return (int) Response.CORRELATION_ID.get(src, 0L);
    }

    public static final int HEADER_SIZE = (int) LAYOUT.byteSize();
}
