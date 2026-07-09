package br.angarion.dev.engine.network.protocol;

import br.angarion.dev.engine.communication.DataLayout;
import br.angarion.dev.engine.communication.MBT;
import br.angarion.dev.engine.communication.intention.Intention;
import br.angarion.dev.engine.communication.response.ApprovedResponse;
import br.angarion.dev.engine.communication.response.DeniedResponse;
import br.angarion.dev.engine.communication.response.PartiallyApprovedResponse;
import br.angarion.dev.engine.communication.response.ResponsePublisherSingleton;
import br.angarion.dev.engine.communication.validator.Approved;
import br.angarion.dev.engine.communication.validator.Denied;
import br.angarion.dev.engine.communication.validator.PartiallyApproved;
import br.angarion.dev.engine.communication.validator.ValidationResult;
import br.angarion.dev.engine.network.transport.MessageReceivedListener;
import br.angarion.dev.engine.runtime.ComponentResolver;
import br.angarion.dev.engine.runtime.InnerProcessor;
import br.angarion.dev.engine.runtime.MemoryLender;
import java.lang.foreign.MemorySegment;

final class ObedientGateway implements MessageReceivedListener {

    private final ComponentResolver resolver;
    private final MemoryLender memoryLender;

    public ObedientGateway(
        ComponentResolver componentResolver,
        MemoryLender memoryLender
    ) {
        this.resolver = componentResolver;
        this.memoryLender = memoryLender;
    }

    public void onMessageReceived(int clientId, MemorySegment message) {
        int family = (int) MBT.FAMILY.get(message, 0L);
        int type = (int) MBT.TYPE.get(message, 0L);

        InnerProcessor<?> innerProcessor = resolver.lookup(family, type);
        DataLayout dataLayout = innerProcessor.dataLayout();

        if (dataLayout.isNotification() == true) {
            innerProcessor.useCase().execute(clientId, message);
            return;
        }

        ValidationResult validationResult = innerProcessor
            .validator()
            .validate(message);

        final int correlationId = Intention.getCorrelationId(message);

        switch (validationResult) {
            case Approved _ -> {
                final MemorySegment response = memoryLender.borrow(
                    ApprovedResponse.HEADER_SIZE
                );
                ApprovedResponse.writeHeader(response, correlationId);
                ResponsePublisherSingleton.get().publish(response, clientId);
            }
            case Denied denied -> {
                final MemorySegment response = memoryLender.borrow(
                    DeniedResponse.HEADER_SIZE
                );
                DeniedResponse.writeHeader(
                    response,
                    correlationId,
                    denied.reasonCode()
                );
                ResponsePublisherSingleton.get().publish(response, clientId);
            }
            case PartiallyApproved partiallyApproved -> {
                final MemorySegment response = memoryLender.borrow(
                    PartiallyApprovedResponse.HEADER_SIZE +
                        partiallyApproved.payload().size()
                );
                PartiallyApprovedResponse.writeHeader(
                    response,
                    correlationId,
                    partiallyApproved.reasonCode()
                );
                partiallyApproved
                    .payload()
                    .write(response, PartiallyApprovedResponse.HEADER_SIZE);

                ResponsePublisherSingleton.get().publish(response, clientId);
            }
        }
    }
}
