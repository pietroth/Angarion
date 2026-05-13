package br.angarion.dev.api.communication;

public class ValidatorResponse {
    private final ValidatorType type;
    private final byte errorCode;
    private final MIDFData data;

    private ValidatorResponse(ValidatorType type, byte errorCode, MIDFData data) {
        this.type = type;
        this.errorCode = code;
        this.data = data;
    }

    public static ValidatorResponse success() {
        return new ValidatorResponse(ValidatorType.SUCCESS, (byte) 0, null);
    }

    public static ValidatorResponse error(byte errorCode) {
        return new ValidatorResponse(ValidatorType.ERROR, errorCode, null);
    }

    public static ValidatorResponse partial(byte errorCode, MIDFData data) {
        return new ValidatorResponse(ValidatorType.PARTIAL, errorCode, data);
    }

    public ValidatorType getType() {
        return type;
    }

    public byte getErrorCode() {
        return errorCode;
    }

    public MemorySegment getData() {
        return data;
    }
}
