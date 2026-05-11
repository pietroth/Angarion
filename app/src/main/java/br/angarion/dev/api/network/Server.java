package br.angarion.dev.api.network;

public interface Server extends Runnable {
    void subscribe(ConnectionCreatedListener listener);
    void unsubscribe(ConnectionCreatedListener listener);
    void notifyConnectionCreated(Connection connection);
}
