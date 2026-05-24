package br.angarion.dev.engine.usecase;
import br.angarion.dev.engine.communication.codec.DataLayout;

@FunctionalInterface
public interface UseCase<T extends DataLayout> {
    void execute(int entityId, T data);
}
