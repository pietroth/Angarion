package br.angarion.dev.api.communication;

public interface Type {
    ValidatorResponse validate();
    void onSuccess();
    void onInvalid();
    void onPartial();
}
