package br.angarion.dev.engine.usecase;

import java.lang.foreign.MemorySegment;

import br.angarion.dev.engine.communication.DataLayout;

@FunctionalInterface
public interface UseCase<T extends DataLayout> {
    void execute(int originId, MemorySegment data);
}
