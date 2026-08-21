package br.angarion.dev.infra.network;

import br.angarion.dev.engine.network.client.ClientLCManager;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.incubator.codec.quic.QuicChannel;

public final class NettyQuicConnectionHandler extends ChannelInboundHandlerAdapter {
    private final ClientLCManager clientLCManager;

    public NettyQuicConnectionHandler(NettyQuicClientLCManager clientLCManager) {
        this.clientLCManager = clientLCManager;
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) {
        System.out.println("New Quic Connection: " + ctx.channel().id());
        QuicChannel channel = (QuicChannel) ctx.channel();
        NettyQuicClient client = new NettyQuicClient(channel);

        int clientId = clientLCManager.connect(client);

        channel.attr(ClientIdAttributeKey.CLIENT_ID).set(clientId);

        ctx.fireChannelActive();
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        System.out.println("Quic Connection Closed: " + ctx.channel().id());

        Integer clientId = ctx.channel().attr(ClientIdAttributeKey.CLIENT_ID).get();
        if (clientId != null) {
            clientLCManager.disconnectClient(clientId);
        }

        ctx.fireChannelInactive();
    }
}
