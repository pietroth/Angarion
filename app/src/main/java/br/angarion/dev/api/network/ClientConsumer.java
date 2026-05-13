package br.angarion.dev.api.communication;

@FunctionalInterface
public interface ClientConsumer {
    void accept(int clientId);
}
