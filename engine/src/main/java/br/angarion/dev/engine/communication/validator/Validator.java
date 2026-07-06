package br.angarion.dev.engine.communication.validator;

import br.angarion.dev.engine.communication.DataLayout;
import java.lang.foreign.MemorySegment;

@FunctionalInterface
public interface Validator<T extends DataLayout> {
    void validate(MemorySegment intention);
}
