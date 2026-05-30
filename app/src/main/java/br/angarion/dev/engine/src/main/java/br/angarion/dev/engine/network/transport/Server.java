package br.angarion.dev.engine.network.transport;

public interface Server extends Runnable {
    void subscribe(ChannelCreatedListener listener);
    void unsubscribe(ChannelCreatedListener listener);
    void subscribe(MessageReceivedListener listener);
    void unsubscribe(MessageReceivedListener listener);
}
