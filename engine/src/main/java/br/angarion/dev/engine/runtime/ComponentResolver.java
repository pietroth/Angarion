package br.angarion.dev.engine.runtime;

import br.angarion.dev.engine.communication.validator.Validator;
import br.angarion.dev.engine.communication.DataLayout;
import br.angarion.dev.engine.usecase.UseCase;

import java.util.Arrays;

public final class ComponentResolver {
    private InnerProcessor<?>[][] processors;

    public ComponentResolver() {
        this(16);
    }

    public ComponentResolver(int familyCapacity) {
        if (familyCapacity <= 0) {
            throw new IllegalArgumentException("familyCapacity must be positive");
        }
        this.processors = new InnerProcessor<?>[familyCapacity][];
    }

    public <T extends DataLayout> void register(
        int familyId,
        int typeId,
        Validator<T> validator,
        UseCase<T> useCase
    )
    {
        ensureCapacity(familyId, typeId);
        processors[familyId][typeId] = new InnerProcessor<T>(validator, useCase);
    }

    public InnerProcessor<?> lookup(int familyId, int typeId) {
        if (familyId < 0 || familyId >= processors.length) {
            return null;
        }
        InnerProcessor<?>[] family = processors[familyId];
        if (family == null || typeId < 0 || typeId >= family.length) {
            return null;
        }
        return processors[familyId][typeId];
    }

    private void ensureCapacity(int familyId, int typeId) {
        if (familyId >= processors.length) {
            processors = Arrays.copyOf(processors, familyId + 1);
        }

        InnerProcessor<?>[] family = processors[familyId];
        if (family == null) {
            processors[familyId] = new InnerProcessor<?>[typeId + 1];
            return;
        }

        if (typeId >= family.length) {
            processors[familyId] = Arrays.copyOf(family, typeId + 1);
        }
    }
}
