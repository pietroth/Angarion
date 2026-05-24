package br.angarion.dev.engine.network.protocol;

import br.angarion.dev.engine.communication.intention.IntentionInConstruction;
import br.angarion.dev.engine.communication.response.IR;
import br.angarion.dev.engine.communication.response.IRPublisherSingleton;
import br.angarion.dev.engine.communication.validator.ValidatorResponse;
import br.angarion.dev.engine.communication.validator.ValidatorType;
import br.angarion.dev.engine.runtime.ComponentResolver;
import br.angarion.dev.engine.runtime.InnerProcessor;
import br.angarion.dev.engine.communication.codec.DataLayout;
import br.angarion.dev.engine.network.transport.ConnectionReceivedListener;
import br.angarion.dev.engine.network.transport.Connection;

import java.lang.foreign.MemorySegment;
import java.lang.foreign.ValueLayout;

public class IntentionGateway implements ConnectionReceivedListener {
    private final ComponentResolver processingPipeline;

    public IntentionGateway(ComponentResolver processingPipeline) {
        this.processingPipeline = processingPipeline;
    }

    @Override
    public void onConnectionReceived(Connection connection, MemorySegment segment) {
        System.out.println("Received intention. OriginId: " + connection.getId() + ", Size: " + segment.byteSize());
        int id = IntentionInConstruction.getTypeId(segment);
        InnerProcessor<?> processor = processingPipeline.lookup((id >> 6) & 0x3F, id & 0x3F);

        if (processor == null) 
            throw new IllegalStateException("No processor found for intention id " + id);

        processIntention(processor, connection, segment);
        System.out.println("Processed intention. Id: " + id + ", OriginId: " + connection.getId());
    }

    private <T extends DataLayout> void processIntention(InnerProcessor<T> processor, Connection connection, MemorySegment intention) {
        ValidatorResponse validationResult = processor.validator().validate(intention);
        MemorySegment intentionPayload = IntentionInConstruction.payloadSlice(intention);

        if (validationResult.getType() == ValidatorType.ERROR) // validation failed, publish IR and return
        {
            IRPublisherSingleton.get().publish(new IR.Builder()
                .error(IntentionInConstruction.getCorrelationId(intention), (byte) IR.ERROR, validationResult.getCode())
                .build(),
                IntentionInConstruction.getOriginId(intention)
            );
            System.out.println("IR published (Error): Correlation Id: " + IntentionInConstruction.getCorrelationId(intention));
            return;
        }

        else if (validationResult.getType() == ValidatorType.PARTIAL) // validation partial, publish IR and execute
        {
            IRPublisherSingleton.get().publish(new IR.Builder()
                .partial(IntentionInConstruction.getCorrelationId(intention), (byte) IR.PARTIAL, validationResult.getData())
                .build(),
                IntentionInConstruction.getOriginId(intention)
            );
            System.out.println("IR published (Partial): Correlation Id: " + IntentionInConstruction.getCorrelationId(intention) + "; Data: " + validationResult.getData().toArray(ValueLayout.JAVA_BYTE));

            if (validationResult.getData() != null && validationResult.getData() != MemorySegment.NULL) {
                intentionPayload = validationResult.getData();
            }
        }

        processor.useCase().execute(IntentionInConstruction.getOriginId(intention), intentionPayload);

        IRPublisherSingleton.get().publish(new IR.Builder()
            .success(IntentionInConstruction.getCorrelationId(intention), (byte) 0)
            .build(), 
            IntentionInConstruction.getOriginId(intention));

        System.out.println("IR published (Success): Correlation Id: " + IntentionInConstruction.getCorrelationId(intention));
    }
}
