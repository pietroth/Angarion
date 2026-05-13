package br.angarion.dev.api.communication;

public interface EventPublisher {
    <T extends Type> void publish(T type)
}