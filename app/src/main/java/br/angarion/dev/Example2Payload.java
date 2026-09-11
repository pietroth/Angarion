package br.angarion.dev;

import br.angarion.dev.api.communication.Payload;

public record Example2Payload(
    long statusCode, byte code
) implements Payload {}
