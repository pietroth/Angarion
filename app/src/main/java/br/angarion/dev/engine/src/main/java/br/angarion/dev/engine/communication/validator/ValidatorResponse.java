package br.angarion.dev.engine.communication.validator;

import java.lang.foreign.MemorySegment;

import br.angarion.dev.engine.communication.response.IRInConstruction;

public class ValidatorResponse {
    private final int status;
    private final int errorCode;
    private final MemorySegment data;

    private ValidatorResponse(int status, int errorCode, MemorySegment data) {
        this.status = status;
        this.errorCode = errorCode;
        this.data = data;
    }

    public static ValidatorResponse success() {
        return new ValidatorResponse(IRInConstruction.SUCCESS, 0, MemorySegment.NULL);
    }

    public static ValidatorResponse error(int errorCode) {
        return new ValidatorResponse(IRInConstruction.INVALID, errorCode, MemorySegment.NULL);
    }

    public static ValidatorResponse partial(int errorCode, MemorySegment segment) {
        return new ValidatorResponse(IRInConstruction.PARTIAL, errorCode, segment);
    }

    public int getType() {
        return status;
    }

    public int getErrorCode() {
        return errorCode;
    }

    public MemorySegment getData() {
        return data;
    }
}
