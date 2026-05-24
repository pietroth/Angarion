package br.angarion.dev.engine.communication.validator;

import br.angarion.dev.engine.communication.codec.DataLayout;
import java.lang.foreign.MemorySegment;

@FunctionalInterface
public interface Validator<T extends DataLayout> {
    ValidatorResponse validate(MemorySegment intention);
}
