package br.angarion.dev.engine.communication.validator;

import br.angarion.dev.engine.communication.DataLayout;

public record PartiallyApproved(
    int reasonCode,
    short payloadType,
    DataLayout payload
) implements ValidationResult {}
