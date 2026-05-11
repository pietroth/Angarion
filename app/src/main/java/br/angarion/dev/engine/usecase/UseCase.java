package br.angarion.dev.engine.usecase;

import br.angarion.dev.engine.communication.MIDFData;

@FunctionalInterface
public interface UseCase<T extends MIDFData> {
    void execute(int entityId, T data);
}
