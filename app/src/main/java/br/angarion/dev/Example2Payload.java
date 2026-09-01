package br.angarion.dev;

import br.angarion.dev.api.communication.Identifiable;
import br.angarion.dev.api.communication.Payload;

public record Example2Payload(
    int id, long statusCode, byte code
) implements Payload {
    public Example2Payload(long statusCode, byte code) {
        this(Identifiable.getAndIncrement(), statusCode, code);
    }
}
