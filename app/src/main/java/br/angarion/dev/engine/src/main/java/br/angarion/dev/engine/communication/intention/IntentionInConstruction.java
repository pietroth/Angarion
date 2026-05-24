package br.angarion.dev.engine.communication.intention;

import java.lang.foreign.MemoryLayout;
import java.lang.foreign.MemorySegment;
import java.lang.foreign.StructLayout;
import java.lang.foreign.ValueLayout;
import java.lang.foreign.MemoryLayout.PathElement;
import java.lang.invoke.VarHandle;

import br.angarion.dev.engine.communication.ProtocolInConstruction;

public final class IntentionInConstruction {
    private IntentionInConstruction(){}
    
    /*
        What's correlation Id? 
        It's just the id that links the intentions with the IRs.
    */

    /*
        What is the origin ID? 
        It simply indicates the ID of the client who made this intention"
    */

    /*
        The origin ID does not exist because the protocol ID serves as the origin ID. 
        To save memory and avoid redundancy, we define the origin ID within the protocol ID space. 
    */

    private static final StructLayout LAYOUT = MemoryLayout.structLayout(
        ProtocolInConstruction.LAYOUT.withName("protocol"), 
        ValueLayout.JAVA_INT.withName("correlationId")
    ).withByteAlignment(8);

    private static final VarHandle CORRELATION_ID = 
        LAYOUT.varHandle(PathElement.groupElement("correlationId"));

    private static final VarHandle PROTOCOL_TOTAL_SIZE = 
        LAYOUT.varHandle(
            PathElement.groupElement("protocol"),
            PathElement.groupElement("totalSize")
        );

    // Here, it serves as the origin Id  

    private static final VarHandle PROTOCOL_ID = 
        LAYOUT.varHandle(
            PathElement.groupElement("protocol"),
            PathElement.groupElement("id")
        );

    public static final void writeIntention(MemorySegment dest, int totalSize, int originId, int correlationId) {
        PROTOCOL_TOTAL_SIZE.set(dest, 0L, totalSize);
        PROTOCOL_ID.set(dest, 0L, originId);
        CORRELATION_ID.set(dest, 0L, correlationId);
    }

    public static final int getOriginId(MemorySegment src) {
        return (int) PROTOCOL_ID.get(src, 0L);
    }

    public static final int getCorrelationId(MemorySegment src) {
        return (int) CORRELATION_ID.get(src, 0L);
    }

    public static final int getTotalSize(MemorySegment src) {
        return (int) PROTOCOL_TOTAL_SIZE.get(src, 0L);
    }

    public static final long LAYOUT_SIZE = LAYOUT.byteSize();
}
