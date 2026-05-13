package br.angarion.dev.api.communication;

public interface Type {
    void onSuccess();
    void onInvalid();
    void onPartial();
}
