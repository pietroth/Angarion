package br.angarion.dev.engine.communication.response;

import java.lang.foreign.MemoryLayout;
import java.lang.foreign.MemorySegment;
import java.lang.foreign.StructLayout;
import java.lang.foreign.ValueLayout;
import java.lang.foreign.MemoryLayout.PathElement;
import java.lang.invoke.VarHandle;

import br.angarion.dev.engine.communication.BaseProtocol;

public final class IRInConstruction {
    private IRInConstruction(){}
    
    /*
        What's correlation Id? 
        It's just the id that links the intentions with the IRs.
    */

    private static final StructLayout LAYOUT = MemoryLayout.structLayout(
        BaseProtocol.LAYOUT.withName("protocol"), // 4 bytes (1 int)
        ValueLayout.JAVA_INT.withName("correlationId"),
        ValueLayout.JAVA_SHORT.withName("errorCode"),
        ValueLayout.JAVA_BYTE.withName("status")
    ).withByteAlignment(8);

    private static final VarHandle STATUS = 
        LAYOUT.varHandle(PathElement.groupElement("status"));

    private static final VarHandle ERROR_CODE = 
        LAYOUT.varHandle(PathElement.groupElement("errorCode"));

    private static final VarHandle PROTOCOL_TOTAL_SIZE = 
        LAYOUT.varHandle(
            PathElement.groupElement("protocol"),
            PathElement.groupElement("totalSize")
        );  

    private static final VarHandle CORRELATION_ID = 
        LAYOUT.varHandle(PathElement.groupElement("correlationId"));

    public static final void writeHeader(MemorySegment dest, int totalSize, int correlationId, int status, int errorCode) {
        if (status > 2 || status < 0) {
            throw new IllegalArgumentException("Status must be between 0 and 2");
        }

        PROTOCOL_TOTAL_SIZE.set(dest, 0L, status);
        CORRELATION_ID.set(dest, 0L, correlationId);
        STATUS.set(dest, 0L, status);
        ERROR_CODE.set(dest, 0L, errorCode);
    }

    public static final int getStatus(MemorySegment src) {
        return (int) STATUS.get(src, 0L);
    }

    public static final int getErrorCode(MemorySegment src) {
        return (int) ERROR_CODE.get(src, 0L);
    }

    public static final int getCorrelationId(MemorySegment src) {
        return (int) CORRELATION_ID.get(src);
    }

    public static final int getTotalSize(MemorySegment src) {
        return (int) PROTOCOL_TOTAL_SIZE.get(src);
    }

    public static final long HEADER_SIZE = LAYOUT.byteSize();

    public static final MemorySegment payloadSlice(MemorySegment message) {
        return message.asSlice(HEADER_SIZE, message.byteSize() - HEADER_SIZE);
    }
}
