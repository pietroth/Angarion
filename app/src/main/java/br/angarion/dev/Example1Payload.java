package br.angarion.dev;

import br.angarion.dev.api.communication.Identifiable;
import br.angarion.dev.api.communication.Payload;

public record Example1Payload(
    int id,
    String name,
    byte age
) implements Payload {
    public Example1Payload(String name, byte age) {
        this(Identifiable.getAndIncrement(), name, age);
    }
}
