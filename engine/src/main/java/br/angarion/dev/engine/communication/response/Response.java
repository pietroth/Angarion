package br.angarion.dev.engine.communication.response;

import java.lang.foreign.MemoryLayout;
import java.lang.foreign.MemoryLayout.PathElement;
import java.lang.foreign.ValueLayout;
import java.lang.invoke.VarHandle;
import java.nio.ByteOrder;

public final class Response {

    private Response() {}

    public static final int APPROVED = 0;
    public static final int DENIED = 1;
    public static final int PARTIALLY_APPROVED = 2;
    public static final int FAILURE = 3;

    public static final MemoryLayout LAYOUT = MemoryLayout.structLayout(
        ValueLayout.JAVA_SHORT.withName("correlationId").withOrder(
            ByteOrder.BIG_ENDIAN
        ),
        ValueLayout.JAVA_BYTE.withName("status").withOrder(ByteOrder.BIG_ENDIAN),
        MemoryLayout.paddingLayout(1)
    );

    public static final VarHandle STATUS = LAYOUT.varHandle(
        PathElement.groupElement("status")
    );

    public static final VarHandle CORRELATION_ID = LAYOUT.varHandle(
        PathElement.groupElement("correlationId")
    );

    public static final int HEADER_SIZE = (int) LAYOUT.byteSize();
}
