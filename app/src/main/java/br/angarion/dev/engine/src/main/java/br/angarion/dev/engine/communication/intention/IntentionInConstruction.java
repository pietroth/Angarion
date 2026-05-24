package br.angarion.dev.engine.communication.intention;

import java.lang.foreign.MemoryLayout;
import java.lang.foreign.MemorySegment;
import java.lang.foreign.StructLayout;
import java.lang.foreign.ValueLayout;
import java.lang.foreign.MemoryLayout.PathElement;
import java.lang.invoke.VarHandle;

import br.angarion.dev.engine.communication.ProtocolInConstruction;

public final class IntentionInConstruction extends ProtocolInConstruction {
    
    private static final StructLayout LAYOUT = MemoryLayout.structLayout(
        ValueLayout.JAVA_INT.withName("originId"),
        ValueLayout.JAVA_INT.withName("correlationId")
    ).withByteAlignment(8);

    private static final VarHandle ORIGIN_ID = 
        LAYOUT.varHandle(PathElement.groupElement("originId"));

    private static final VarHandle CORRELATION_ID = 
        LAYOUT.varHandle(PathElement.groupElement("correlationId"));

    public static final void writeIntention(MemorySegment dest, int originId, int correlationId) {
        ORIGIN_ID.set(dest, 0L, originId);
        CORRELATION_ID.set(dest, 0L, correlationId);
    }

    public static final int getOriginId(MemorySegment src) {
        return (int) ORIGIN_ID.get(src, 0L);
    }

    public static final int getCorrelationId(MemorySegment src) {
        return (int) CORRELATION_ID.get(src, 0L);
    }

    public static final long LAYOUT_SIZE = LAYOUT.byteSize();
}
