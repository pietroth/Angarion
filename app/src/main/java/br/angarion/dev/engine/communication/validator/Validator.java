package br.angarion.dev.engine.communication.validator;

import br.angarion.dev.engine.communication.MIDFData;
import br.angarion.dev.engine.communication.intention.Intention;

@FunctionalInterface
public interface Validator<T extends MIDFData> {
    ValidatorResponse validate(Intention<T> intention);
}
