package br.angarion.dev.infra.network;

import java.lang.foreign.MemorySegment;
import java.util.ArrayList;

import br.angarion.dev.engine.network.transport.MessageReceivedListener;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.incubator.codec.quic.QuicChannel;

public final class NettyQuicStreamHandler extends ChannelInboundHandlerAdapter {
    private final ArrayList<MessageReceivedListener> listeners;

    public NettyQuicStreamHandler(
        ArrayList<MessageReceivedListener> listeners)
    {
        this.listeners = listeners;
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) {
        ByteBuf in = (ByteBuf) msg;
        try {
            MemorySegment segment = ByteBuf2MemorySegment.ToSegment(in);

            QuicChannel connection = (QuicChannel) ctx.channel().parent();
            int clientId = connection.attr(ClientIdAttributeKey.CLIENT_ID).get();

            System.out.println("Message received from client " + clientId + "; totalSize: " + segment.byteSize() + ";");

            for (MessageReceivedListener listener : listeners) {
                listener.onMessageReceived(clientId, segment);
            }

        } finally {
            in.release();
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        cause.printStackTrace();
        ctx.close();
    }

    public void subscribe(MessageReceivedListener listener) {
        listeners.add(listener);
    }
}
