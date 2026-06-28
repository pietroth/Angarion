package br.angarion.dev.engine.runtime;

import br.angarion.dev.engine.communication.DataLayout;
import br.angarion.dev.engine.communication.validator.Validator;
import br.angarion.dev.engine.usecase.UseCase;

public record InnerProcessor<T extends DataLayout>(
    T dataLayout,
    Validator<T> validator,
    UseCase<T> useCase
) {}
