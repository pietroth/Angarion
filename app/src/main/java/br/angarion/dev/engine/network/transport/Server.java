package br.angarion.dev.engine.network.transport;

public interface Server extends Runnable {
    void subscribe(ConnectionCreatedListener listener);
    void unsubscribe(ConnectionCreatedListener listener);
    void notifyConnectionCreated(Connection connection);
}
