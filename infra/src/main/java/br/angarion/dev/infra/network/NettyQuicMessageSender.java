package br.angarion.dev.infra.network;

import java.lang.foreign.MemorySegment;
import java.nio.ByteBuffer;

import br.angarion.dev.engine.network.MessageSender;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerAdapter;
import io.netty.incubator.codec.quic.QuicChannel;
import io.netty.incubator.codec.quic.QuicStreamChannel;
import io.netty.incubator.codec.quic.QuicStreamType;

public final class NettyQuicMessageSender implements MessageSender {
    private final NettyQuicClientsConnectionsRegistry clientsConnectionsRegistry = new NettyQuicClientsConnectionsRegistry();

    public void sendTo(int[] ids, MemorySegment message) {
        for (int id : ids) {
            var client = clientsConnectionsRegistry.get(id);
            if (client != null) {
                sendMessage(client.getChannel(), message);
            }
        }
    }

    public void sendTo(int id, MemorySegment message) {
        var client = clientsConnectionsRegistry.get(id);
        if (client != null) {
            sendMessage(client.getChannel(), message);
        }
    }

    public void broadcast(MemorySegment message) {
        clientsConnectionsRegistry.forEach((id, client) -> {
            if (client != null) {
                sendMessage(client.getChannel(), message);
            }
        });
    }

    private void sendMessage(QuicChannel channel, MemorySegment message) {
        ByteBuffer byteBuffer = message.asByteBuffer();
        ByteBuf buf = Unpooled.wrappedBuffer(byteBuffer);

        channel.createStream(QuicStreamType.UNIDIRECTIONAL, new ChannelHandlerAdapter() {})
            .addListener(future -> {
                if (future.isSuccess()) {
                    QuicStreamChannel streamChannel = (QuicStreamChannel) future.getNow();
                    streamChannel.writeAndFlush(buf).addListener(QuicStreamChannel.SHUTDOWN_OUTPUT);
                } else {
                    buf.release();
                }
            });
    }
}
