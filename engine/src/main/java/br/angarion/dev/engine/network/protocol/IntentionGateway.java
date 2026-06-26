package br.angarion.dev.engine.network.protocol;

import br.angarion.dev.engine.communication.DataLayout;
import br.angarion.dev.engine.communication.intention.Intention;
import br.angarion.dev.engine.communication.response.IR;
import br.angarion.dev.engine.communication.response.IRPublisherSingleton;
import br.angarion.dev.engine.communication.validator.ValidatorResponse;
import br.angarion.dev.engine.network.transport.MessageReceivedListener;
import br.angarion.dev.engine.runtime.ComponentResolver;
import br.angarion.dev.engine.runtime.InnerProcessor;
import java.lang.foreign.Arena;
import java.lang.foreign.MemorySegment;

public class IntentionGateway implements MessageReceivedListener {

    private final ComponentResolver processingPipeline;

    public IntentionGateway(ComponentResolver processingPipeline) {
        this.processingPipeline = processingPipeline;
    }

    @Override
    public void onMessageReceived(int clientId, MemorySegment segment) {
        System.out.println(
            "Received intention. OriginId: " +
                clientId +
                ", Size: " +
                segment.byteSize()
        );

        if (segment.byteSize() <= Intention.HEADER_SIZE) return;

        // int id = Intention.getTypeId(segment);
        InnerProcessor<?> processor = processingPipeline.lookup(323, 8543);

        if (processor == null) {
            System.out.println(
                "No processor found for intention with TypeId: "
            );
            return;
        }

        processIntention(processor, segment);
    }

    private <T extends DataLayout> void processIntention(
        InnerProcessor<T> processor,
        MemorySegment intention
    ) {
        try (Arena arena = Arena.ofConfined()) {
            ValidatorResponse validationResult = processor
                .validator()
                .validate(intention);
            int validationStatus = validationResult.getStatus();
            int errorCode = validationResult.getErrorCode();

            MemorySegment validationData = validationResult.getData();
            boolean hasData =
                validationData != null && validationData != MemorySegment.NULL;
            int payloadSize = hasData ? (int) validationData.byteSize() : 0;
            int totalSize = (int) IR.HEADER_SIZE + payloadSize;

            int correlationId = Intention.getCorrelationId(intention);
            int originId = Intention.getOriginId(intention);

            MemorySegment ir = arena.allocate(totalSize);
            IR.writeHeader(
                ir,
                totalSize,
                correlationId,
                validationStatus,
                errorCode
            );

            if (payloadSize > 0) {
                ir.asSlice(IR.HEADER_SIZE, payloadSize).copyFrom(
                    validationData
                );
            }

            IRPublisherSingleton.get().publish(ir, originId);

            if (validationStatus == IR.INVALID) {
                System.out.println(
                    "IR published (Error): Correlation Id: " + correlationId
                );
                return;
            }

            MemorySegment intentionPayload =
                validationStatus == IR.PARTIAL && hasData
                    ? validationData
                    : Intention.payloadSlice(intention);

            if (validationStatus == IR.PARTIAL) {
                System.out.println(
                    "IR published (Partial): Correlation Id: " + correlationId
                );
            } else if (validationStatus == IR.SUCCESS) {
                System.out.println(
                    "IR published (Success): Correlation Id: " + correlationId
                );
            }

            processor.useCase().execute(originId, intentionPayload);
        }
    }
}
