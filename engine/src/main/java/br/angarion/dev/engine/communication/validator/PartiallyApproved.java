package br.angarion.dev.engine.communication.validator;

import java.lang.foreign.MemorySegment;

public record PartiallyApproved(
    int reasonCode,
    short payloadType,
    MemorySegment payload
) implements ValidationResult {}
