package br.angarion.dev.infra.network;

import java.security.cert.CertificateException;
import java.util.ArrayList;
import java.util.concurrent.TimeUnit;
import br.angarion.dev.engine.network.client.ClientConnectedListener;
import br.angarion.dev.engine.network.transport.MessageReceivedListener;
import br.angarion.dev.engine.network.transport.Server;
import br.angarion.dev.engine.runtime.MemoryLender;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioDatagramChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import io.netty.handler.ssl.util.SelfSignedCertificate;
import io.netty.incubator.codec.quic.*;

public final class NettyQuicServer implements Server {
    private final ArrayList<ClientConnectedListener> clientConnectedListeners = new ArrayList<>();
    private final ArrayList<MessageReceivedListener> messageReceivedListeners = new ArrayList<>();
    private final int eventLoopGroupNThreads;
    private final NettyQuicClientLCManager clientLCManager;
    private final MemoryLender memoryLender;

    private static final int MAX_MESSAGE_BYTES_SIZE = 1460;

    public NettyQuicServer(
        int eventLoopGroupNThreads,
        NettyQuicClientLCManager clientLCManager,
        MemoryLender memoryLender
    ){
        this.eventLoopGroupNThreads = eventLoopGroupNThreads;
        this.clientLCManager = clientLCManager;
        this.memoryLender = memoryLender;
    }

    @Override
    public final void run() {
        try {
            SelfSignedCertificate cert = new SelfSignedCertificate();
            QuicSslContext sslContext = QuicSslContextBuilder.forServer(
                cert.privateKey(), null, cert.certificate())
            .applicationProtocols("iner").build();

            ChannelHandler channelHandler = new QuicServerCodecBuilder()
                .sslContext(sslContext)
                .maxIdleTimeout(5000, TimeUnit.MILLISECONDS)
                .initialMaxData(10000000)
                .initialMaxStreamDataBidirectionalLocal(1000000)
                .tokenHandler(InsecureQuicTokenHandler.INSTANCE)
                .handler(new ChannelInitializer<QuicChannel>() {
                    @Override
                    protected void initChannel(QuicChannel ch) {
                        ch.pipeline().addLast(new NettyQuicConnectionHandler(clientLCManager));
                    }
                })
                .streamHandler(new ChannelInitializer<QuicStreamChannel>() {
                    @Override
                    protected void initChannel(QuicStreamChannel ch) {
                        ch.pipeline().addLast(
                            new LengthFieldBasedFrameDecoder(
                                MAX_MESSAGE_BYTES_SIZE,
                                0, // totalSize starts at offset 0
                                Integer.BYTES, // totalSize byte size
                                -Integer.BYTES, // totalSize includes the size itself
                                0 // do not remove bytes
                            )
                        ).addLast(new NettyQuicStreamHandler(messageReceivedListeners, memoryLender));
                    }
                })
                .build();

            EventLoopGroup group = new NioEventLoopGroup(eventLoopGroupNThreads);
            new Bootstrap()
                .group(group)
                .channel(NioDatagramChannel.class)
                .handler(channelHandler)
                .bind(8843).sync();

        } catch (CertificateException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override public final void subscribe(ClientConnectedListener listener) { clientConnectedListeners.add(listener); }
    @Override public final void subscribe(MessageReceivedListener listener) { messageReceivedListeners.add(listener); }
}
