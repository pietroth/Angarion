package br.angarion.dev.engine.communication;

import java.lang.foreign.MemoryLayout;
import java.lang.foreign.StructLayout;
import java.lang.foreign.ValueLayout;

public final class BaseProtocol {
    private BaseProtocol(){}
    
    /*
        The ID is a generic protocol identifier. 
        The protocol ID must be completely generic, and it can have different meanings in different message types.
    */

    public static final StructLayout LAYOUT = MemoryLayout.structLayout(
        ValueLayout.JAVA_INT.withName("totalSize")
    );

    public static final long LAYOUT_SIZE = LAYOUT.byteSize();
}
