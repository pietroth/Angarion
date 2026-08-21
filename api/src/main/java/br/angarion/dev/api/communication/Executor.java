package br.angarion.dev.api.communication;

public interface Executor<T extends Payload> {
    void onApproved(T payload);
    void onPartiallyApproved(T payload);
    void onDenied(T payload);
    void onFailure(T payload);
}
