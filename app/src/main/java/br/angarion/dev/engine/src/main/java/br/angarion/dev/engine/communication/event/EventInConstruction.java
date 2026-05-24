package br.angarion.dev.engine.communication.event;

import java.lang.foreign.MemoryLayout;
import java.lang.foreign.MemorySegment;
import java.lang.foreign.StructLayout;
import java.lang.foreign.MemoryLayout.PathElement;
import java.lang.invoke.VarHandle;

import br.angarion.dev.engine.communication.ProtocolInConstruction;

public final class EventInConstruction {
    private EventInConstruction(){}

    /*
        What is the origin ID? 
        It simply indicates the Client ID of the client who made the intention that triggered this event."
    */

    /*
        The origin ID does not exist because the protocol ID serves as the origin ID. 
        To save memory and avoid redundancy, we define the origin ID within the protocol ID space. 
    */

    private static final StructLayout LAYOUT = MemoryLayout.structLayout(
        ProtocolInConstruction.LAYOUT.withName("protocol")
    );

    private static final VarHandle PROTOCOL_TOTAL_SIZE = 
        LAYOUT.varHandle(
            PathElement.groupElement("protocol"),
            PathElement.groupElement("totalSize")
        );

    // Here it serves as the originId
    
    private static final VarHandle PROTOCOL_ID = 
        LAYOUT.varHandle(
            PathElement.groupElement("protocol"),
            PathElement.groupElement("id")
        );

    public static final void writeHeader(MemorySegment dest, int totalSize, int originId) {
        PROTOCOL_TOTAL_SIZE.set(dest, 0L, totalSize);
        PROTOCOL_ID.set(dest, 0L, originId);
    }

    public static final int getOriginId(MemorySegment src) {
        return (int) PROTOCOL_ID.get(src, 0L);
    }

    public static final int getTotalSize(MemorySegment src) {
        return (int) PROTOCOL_TOTAL_SIZE.get(src, 0L);
    }

    public static final long HEADER_SIZE = LAYOUT.byteSize();
}
