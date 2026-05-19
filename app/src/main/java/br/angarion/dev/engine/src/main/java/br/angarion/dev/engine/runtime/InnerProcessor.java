package br.angarion.dev.engine.runtime;

import br.angarion.dev.engine.communication.MIDFData;
import br.angarion.dev.engine.communication.codec.Codec;
import br.angarion.dev.engine.communication.validator.Validator;
import br.angarion.dev.engine.usecase.UseCase;

public record InnerProcessor<T extends MIDFData>(
    Validator<T> validator,
    UseCase<T> useCase,
    Codec<T> codec
) {}
