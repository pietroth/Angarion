package br.angarion.dev.engine.communication.validator;

import java.lang.foreign.MemorySegment;

import br.angarion.dev.engine.communication.response.IR;

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
        return new ValidatorResponse(IR.SUCCESS, 0, MemorySegment.NULL);
    }

    public static ValidatorResponse error(int errorCode) {
        return new ValidatorResponse(IR.INVALID, errorCode, MemorySegment.NULL);
    }

    public static ValidatorResponse partial(int errorCode, MemorySegment segment) {
        return new ValidatorResponse(IR.PARTIAL, errorCode, segment);
    }

    public int getStatus() {
        return status;
    }

    public int getErrorCode() {
        return errorCode;
    }

    public MemorySegment getData() {
        return data;
    }
}
