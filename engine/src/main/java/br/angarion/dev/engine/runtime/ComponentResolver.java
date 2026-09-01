package br.angarion.dev.engine.runtime;

import br.angarion.dev.api.communication.Payload;
import br.angarion.dev.engine.communication.DataLayout;

public interface ComponentResolver {
    InnerProcessor<?> lookup(int type);
    Payload getPayload(DataLayout dataLayout); // Don't forget to implement the EventPublisher
                                               //  that receives the payload from the API side.
}
