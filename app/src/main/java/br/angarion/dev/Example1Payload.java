package br.angarion.dev;

import br.angarion.dev.api.communication.Payload;

public record Example1Payload(
    byte age
) implements Payload {}
