package br.angarion.dev.infra.network;

import br.angarion.dev.engine.network.client.Client;
import io.netty.incubator.codec.quic.QuicChannel;

public final class NettyQuicClient implements Client {
    private QuicChannel channel;

    public NettyQuicClient(QuicChannel channel) {
        this.channel = channel;
    }

    public QuicChannel getChannel() {
        return channel;
    }

    public void setChannel(QuicChannel channel) {
        this.channel = channel;
    }
}
