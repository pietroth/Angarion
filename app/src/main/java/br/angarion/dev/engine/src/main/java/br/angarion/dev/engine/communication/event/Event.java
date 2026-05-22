package br.angarion.dev.engine.communication.event;

import br.angarion.dev.engine.communication.MIDFData;
import br.angarion.dev.api.communication.TargetScope;
import br.angarion.dev.engine.communication.MIDF;

public class Event<T extends MIDFData> extends MIDF<T> {
    private final int originId;
    private final TargetScope target;

    public Event(T data, int originId, TargetScope target) {
        super(data);
        this.originId = originId;
        this.target = target;
    }

    public int getOriginId() {
        return originId;
    }

    public TargetScope getTarget() {
        return target;
    }
    
}