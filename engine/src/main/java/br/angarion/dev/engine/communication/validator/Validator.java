package br.angarion.dev.engine.communication.validator;

import java.lang.foreign.MemorySegment;

import br.angarion.dev.engine.communication.DataLayout;

@FunctionalInterface
public interface Validator<T extends DataLayout> {
    ValidatorResponse validate(MemorySegment intention);
}
