package br.angarion.dev.api.communication;

public interface Validator<T extends Payload> {
    void validate(T payload);
}
