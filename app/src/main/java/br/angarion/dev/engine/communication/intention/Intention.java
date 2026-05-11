package br.angarion.dev.engine.communication.intention;

import br.angarion.dev.engine.communication.MIDF;
import br.angarion.dev.engine.communication.MIDFData;

public class Intention<T extends MIDFData> extends MIDF<T> {
    private final int originId;
    private final int correlationId; // Correlation ID with Response

    public Intention(T data, int correlationId, int originId) {
        super(data);
        this.correlationId = correlationId;
        this.originId = originId;
    }

    public int getCorrelationId() {
        return correlationId;
    }

    public int getOriginId() {
        return originId;
    }
}
