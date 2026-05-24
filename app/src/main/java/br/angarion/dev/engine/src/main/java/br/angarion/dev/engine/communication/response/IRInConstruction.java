package br.angarion.dev.engine.communication.response;

import java.lang.foreign.MemoryLayout;
import java.lang.foreign.MemorySegment;
import java.lang.foreign.StructLayout;
import java.lang.foreign.ValueLayout;
import java.lang.foreign.MemoryLayout.PathElement;
import java.lang.invoke.VarHandle;

import br.angarion.dev.engine.communication.ProtocolInConstruction;

public final class IRInConstruction {
    private IRInConstruction(){}
    
    /*
        What's correlation Id? 
        It's just the id that links the intentions with the IRs.
    */

    /*
        The correlation Id does not exist directly because the protocol Id serves as correlation Id.
        To save memory and avoid redundancy, we define the correlation ID within the protocol ID space. 
    */

    private static final StructLayout LAYOUT = MemoryLayout.structLayout(
        ProtocolInConstruction.LAYOUT.withName("protocol"),
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

    // Here it serves as the correlation Id.    

    private static final VarHandle PROTOCOL_ID = 
        LAYOUT.varHandle(
            PathElement.groupElement("protocol"),
            PathElement.groupElement("id")
        );

    public static final void writeHeader(MemorySegment dest, int totalSize, int correlationId, int status, int errorCode) {
        if (status > 2 || status < 0) {
            throw new IllegalArgumentException("Status must be between 0 and 2");
        }

        PROTOCOL_TOTAL_SIZE.set(dest, 0L, status);
        PROTOCOL_ID.set(dest, 0L, correlationId);
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
        return (int) PROTOCOL_ID.get(src);
    }

    public static final int getTotalSize(MemorySegment src) {
        return (int) PROTOCOL_TOTAL_SIZE.get(src);
    }

    public static final long LAYOUT_SIZE = LAYOUT.byteSize();

}
