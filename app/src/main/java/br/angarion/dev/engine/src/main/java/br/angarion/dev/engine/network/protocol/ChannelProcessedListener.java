package br.angarion.dev.engine.network.protocol;

import br.angarion.dev.engine.network.transport.Channel;

public interface ChannelProcessedListener {
    void onChannelProcessed(Channel channel);
}
