package br.angarion.dev.engine.network.protocol;

import java.lang.foreign.MemorySegment;

public record MessageReceivedWrapper(
    int clientId, MemorySegment message
) {}
