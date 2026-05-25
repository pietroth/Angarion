package br.angarion.dev.engine.network.protocol;

import br.angarion.dev.engine.communication.DataLayout;
import br.angarion.dev.engine.communication.intention.IntentionInConstruction;
import br.angarion.dev.engine.communication.response.IRInConstruction;
import br.angarion.dev.engine.communication.response.IRPublisherSingleton;
import br.angarion.dev.engine.communication.validator.ValidatorResponse;
import br.angarion.dev.engine.runtime.ComponentResolver;
import br.angarion.dev.engine.runtime.InnerProcessor;
import br.angarion.dev.engine.network.transport.ConnectionReceivedListener;
import br.angarion.dev.engine.network.transport.Connection;

import java.lang.foreign.Arena;
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
        InnerProcessor<?> processor = processingPipeline.lookup(id);

        if (processor == null) 
            throw new IllegalStateException("No processor found for intention id " + id);

        processIntention(processor, connection, segment);
    }

    private <T extends DataLayout> void processIntention(InnerProcessor<T> processor, Connection connection, MemorySegment intention) {
        try (Arena arena = Arena.ofConfined()) {
            ValidatorResponse validationResult = processor.validator().validate(intention);
            MemorySegment intentionPayload = IntentionInConstruction.payloadSlice(intention);

            MemorySegment data = validationResult.getData();
            int payloadSize = (data == null || data == MemorySegment.NULL) ? 0 : (int) data.byteSize();
            int totalSize = (int) IRInConstruction.HEADER_SIZE + payloadSize;

            int correlationId = IntentionInConstruction.getCorrelationId(intention);
            int originId = IntentionInConstruction.getOriginId(intention);

            MemorySegment ir = arena.allocate(totalSize);

            if (validationResult.getType() == IRInConstruction.INVALID) // validation invalid, publish IR and return
            {
                IRInConstruction.writeHeader(ir, totalSize, correlationId, IRInConstruction.INVALID, validationResult.getErrorCode());

                IRPublisherSingleton.get().publish(
                    ir,
                    originId
                );
                System.out.println("IR published (Error): Correlation Id: " + correlationId);
                return;
            }

            else if (validationResult.getType() == IRInConstruction.PARTIAL) // validation partial, publish IR and execute
            {
                IRInConstruction.writeHeader(ir, totalSize, correlationId, IRInConstruction.PARTIAL, validationResult.getErrorCode());

                IRPublisherSingleton.get().publish(
                    ir,
                    originId
                );
                System.out.println("IR published (Partial): Correlation Id: " + correlationId + "; Data: " + data.toArray(ValueLayout.JAVA_BYTE));

                if (data != null && data != MemorySegment.NULL) {
                    intentionPayload = data;
                }
            }

            else if (validationResult.getType() == IRInConstruction.SUCCESS) 
            {
                IRInConstruction.writeHeader(ir, totalSize, correlationId, IRInConstruction.SUCCESS, validationResult.getErrorCode());

                IRPublisherSingleton.get().publish(
                    ir,
                    originId
                );

                System.out.println("IR published (Success): Correlation Id: " + correlationId);
            }

            processor.useCase().execute(originId, intentionPayload);
        }
    }
}
