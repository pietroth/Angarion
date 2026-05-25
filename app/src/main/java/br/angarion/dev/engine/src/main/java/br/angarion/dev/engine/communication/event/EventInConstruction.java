package br.angarion.dev.engine.communication.event;

import java.lang.foreign.MemoryLayout;
import java.lang.foreign.MemorySegment;
import java.lang.foreign.StructLayout;
import java.lang.foreign.ValueLayout;
import java.lang.foreign.MemoryLayout.PathElement;
import java.lang.invoke.VarHandle;

import br.angarion.dev.engine.communication.BaseProtocol;
import br.angarion.dev.engine.communication.MBT;

public final class EventInConstruction {
    private EventInConstruction(){}

    /*
        What is the origin ID? 
        It simply indicates the Client ID of the client who made the intention that triggered this event."
    */

    private static final StructLayout LAYOUT = MemoryLayout.structLayout(
        BaseProtocol.LAYOUT.withName("protocol"), // 4 bytes (1 int)
        MBT.LAYOUT.withName("mbt"), // 4 bytes (1 int)
        ValueLayout.JAVA_INT.withName("originId")
    );

    private static final VarHandle PROTOCOL_TOTAL_SIZE = 
        LAYOUT.varHandle(
            PathElement.groupElement("protocol"),
            PathElement.groupElement("totalSize")
        );
    
    private static final VarHandle ORIGIN_ID = 
        LAYOUT.varHandle(PathElement.groupElement("originId"));

    private static final VarHandle MBT_ID = 
        LAYOUT.varHandle(
            PathElement.groupElement("mbt"),
            PathElement.groupElement("id")
        );

    public static final void writeHeader(MemorySegment dest, int typeId, int totalSize, int originId) {
        PROTOCOL_TOTAL_SIZE.set(dest, 0L, totalSize);
        MBT_ID.set(dest, 0L, typeId);
        ORIGIN_ID.set(dest, 0L, originId);
    }

    public static final int getOriginId(MemorySegment src) {
        return (int) ORIGIN_ID.get(src, 0L);
    }

    public static final int getTotalSize(MemorySegment src) {
        return (int) PROTOCOL_TOTAL_SIZE.get(src, 0L);
    }

    public static final int getTypeId(MemorySegment src) {
        return (int) MBT_ID.get(src, 0L);
    }

    public static final short getFamily(MemorySegment src) {
        return MBT.unpackFamily(getTypeId(src));
    }

    public static final short getUnpackedType(MemorySegment src) {
        return MBT.unpackType(getTypeId(src));
    }

    public static final long HEADER_SIZE = LAYOUT.byteSize();

    public static final MemorySegment payloadSlice(MemorySegment message) {
        return message.asSlice(HEADER_SIZE, message.byteSize() - HEADER_SIZE);
    }
}
