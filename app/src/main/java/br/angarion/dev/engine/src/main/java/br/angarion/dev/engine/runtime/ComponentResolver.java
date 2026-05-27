package br.angarion.dev.engine.runtime;

import br.angarion.dev.engine.communication.DataLayout;
import br.angarion.dev.engine.communication.validator.Validator;
import br.angarion.dev.engine.usecase.UseCase;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;

public final class ComponentResolver {
    private Int2ObjectOpenHashMap<InnerProcessor<?>> processors = new Int2ObjectOpenHashMap<>();

    public <T extends DataLayout> void register(
        int id,
        Validator<T> validator,
        UseCase<T> useCase
    )
    {
        processors.put(id, new InnerProcessor<T>(validator, useCase));
    }

    public InnerProcessor<?> lookup(int id) {
        return processors.get(id);
    }
}
