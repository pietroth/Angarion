package br.angarion.dev.engine.runtime;

import br.angarion.dev.engine.communication.codec.DataLayout;
import br.angarion.dev.engine.communication.validator.Validator;
import br.angarion.dev.engine.usecase.UseCase;

@SuppressWarnings("rawtypes")
public final class ComponentResolver {
    private final InnerProcessor[] processors = new InnerProcessor[4096];

    public <T extends DataLayout> void register(
        int id,
        Validator<T> validator,
        UseCase<T> useCase
    )
    {
        processors[id] = new InnerProcessor<>(validator, useCase);
    }

    public InnerProcessor<?> lookup(int id) {
        return processors[id];
    }
}
