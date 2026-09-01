package br.angarion.dev.engine.network.protocol;

import br.angarion.dev.engine.communication.DataLayout;
import br.angarion.dev.engine.communication.MBT;
import br.angarion.dev.engine.communication.intention.Intention;
import br.angarion.dev.engine.communication.response.ApprovedResponse;
import br.angarion.dev.engine.communication.response.DeniedResponse;
import br.angarion.dev.engine.communication.response.FailureResponse;
import br.angarion.dev.engine.communication.response.PartiallyApprovedResponse;
import br.angarion.dev.engine.communication.response.ResponsePublisherSingleton;
import br.angarion.dev.engine.communication.validator.Approved;
import br.angarion.dev.engine.communication.validator.Denied;
import br.angarion.dev.engine.communication.validator.Failure;
import br.angarion.dev.engine.communication.validator.PartiallyApproved;
import br.angarion.dev.engine.communication.validator.ValidationResult;
import br.angarion.dev.engine.network.transport.MessageReceivedListener;
import br.angarion.dev.engine.runtime.ComponentResolver;
import br.angarion.dev.engine.runtime.InnerProcessor;
import br.angarion.dev.engine.runtime.MemoryBank;

import java.lang.foreign.MemorySegment;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.Queue;

import org.jctools.queues.SpscArrayQueue;

final class ObedientGateway implements MessageReceivedListener {

    private final ComponentResolver resolver;
    private final MemoryBank memoryBank;
    private final ExecutorService executorService = Executors.newVirtualThreadPerTaskExecutor();

    private final int queueCapacity = 1024;
    private final Queue<MessageReceivedWrapper> queue = new SpscArrayQueue<>(queueCapacity);

    public ObedientGateway(
        ComponentResolver componentResolver,
        MemoryBank memoryBank
    ) {
        this.resolver = componentResolver;
        this.memoryBank = memoryBank;
    }

    public void onMessageReceived(MessageReceivedWrapper messageReceived) {
        while (!queue.offer(messageReceived)) {
            Thread.onSpinWait();
        }
    }

    public void start() {
        while (!Thread.currentThread().isInterrupted()) {
            MessageReceivedWrapper message = queue.poll();

            if (message != null) {
                onMessageReceived(message.clientId(), message.message());
            }
        }
    }

    private void onMessageReceived(int clientId, MemorySegment message) {
        int type = (int) MBT.TYPE.get(message, 0L);

        InnerProcessor<?> innerProcessor = resolver.lookup(type);
        DataLayout dataLayout = innerProcessor.dataLayout();

        if (dataLayout.isNotification() == true) {
            System.out.println("Notification received;");
            innerProcessor.useCase().execute(clientId, message);
            return;
        }

        final int correlationId = Intention.getCorrelationId(message);

        if (dataLayout.isBlocking()) {
            CompletableFuture.supplyAsync(() -> {
                return processIntention(innerProcessor, message, clientId, correlationId);
            }, executorService).thenAccept(response -> {
                ResponsePublisherSingleton.get().publish(response, clientId);
            });
            return;
        }

        ResponsePublisherSingleton.get().publish(processIntention(innerProcessor, message, clientId, correlationId), clientId);
    }

    private MemorySegment processIntention(InnerProcessor<?> innerProcessor, MemorySegment message, int clientId, int correlationId) {
        ValidationResult validationResult = innerProcessor
            .validator()
            .validate(message);

        System.out.println("Intention received; correlationId: " + correlationId + ";");
        final MemorySegment response;

        switch (validationResult) {
            case Approved _ -> {
                response = memoryBank.get(ApprovedResponse.HEADER_SIZE);
                ApprovedResponse.writeHeader(response, correlationId);

                innerProcessor.useCase().execute(clientId, message);
            }
            case Denied denied -> {
                response = memoryBank.get(DeniedResponse.HEADER_SIZE);
                DeniedResponse.writeHeader(
                    response,
                    correlationId,
                    denied.reasonCode()
                );
            }
            case PartiallyApproved partiallyApproved -> {
                response = memoryBank.get(
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

                innerProcessor.useCase().execute(clientId, message); // Execution must serve as validation to ensure the "partially approved"
                                                                     //                               result is consistent across both parts.
            }
            case Failure _ -> {
                response = memoryBank.get(FailureResponse.HEADER_SIZE);
                FailureResponse.writeHeader(response, correlationId);
            }
        }

        return response;
    }
}
