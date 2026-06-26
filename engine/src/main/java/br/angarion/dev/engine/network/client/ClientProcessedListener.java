package br.angarion.dev.engine.network.client;

public interface ClientProcessedListener<T extends Client> {
    void onClientProcessed(T client);
}
