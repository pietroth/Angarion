package br.angarion.dev.infra.network;

import java.lang.foreign.MemorySegment;
import java.nio.ByteBuffer;

import io.netty.buffer.ByteBuf;

public final class ByteBuf2MemorySegment {
    public static final MemorySegment ToSegment(ByteBuf byteBuf) {
        ByteBuffer nioBuffer = byteBuf.nioBuffer(
            byteBuf.readerIndex(),
            byteBuf.readableBytes()
        );

        return MemorySegment.ofBuffer(nioBuffer);
    }
}
