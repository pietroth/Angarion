package br.angarion.dev.engine.communication.response;

import java.lang.foreign.MemoryLayout;
import java.lang.foreign.MemorySegment;
import java.lang.foreign.StructLayout;
import java.lang.foreign.ValueLayout;
import java.lang.foreign.MemoryLayout.PathElement;
import java.lang.invoke.VarHandle;

import br.angarion.dev.engine.communication.ProtocolInConstruction;

public final class IRInConstruction extends ProtocolInConstruction {
    
    private static final StructLayout LAYOUT = MemoryLayout.structLayout(
        ValueLayout.JAVA_BYTE.withName("status"),
        ValueLayout.JAVA_SHORT.withName("errorCode")
    ).withByteAlignment(4);

    private static final VarHandle STATUS = 
        LAYOUT.varHandle(PathElement.groupElement("status"));

    private static final VarHandle ERROR_CODE = 
        LAYOUT.varHandle(PathElement.groupElement("errorCode"));

    public static final void writeIR(MemorySegment dest, int status, int errorCode) {
        if (status > 2 || status < 0) {
            throw new IllegalArgumentException("Status must be between 0 and 2");
        }

        STATUS.set(dest, 0L, status);
        ERROR_CODE.set(dest, 0L, errorCode);
    }

    public static final int getStatus(MemorySegment src) {
        return (int) STATUS.get(src, 0L);
    }

    public static final int getErrorCode(MemorySegment src) {
        return (int) ERROR_CODE.get(src, 0L);
    }

    public static final long LAYOUT_SIZE = LAYOUT.byteSize();
}
