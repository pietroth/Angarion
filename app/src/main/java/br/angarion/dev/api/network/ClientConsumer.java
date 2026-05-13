package br.angarion.dev.api.network;

@FunctionalInterface
public interface ClientConsumer {
    void accept(int clientId);
}
