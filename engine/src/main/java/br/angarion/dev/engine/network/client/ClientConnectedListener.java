package br.angarion.dev.engine.network.client;

public interface ClientConnectedListener<T extends Client> {
    void onClientConnected(T client);
}
