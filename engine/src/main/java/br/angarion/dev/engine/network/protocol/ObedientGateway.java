package br.angarion.dev.engine.network.protocol;

import br.angarion.dev.engine.communication.DataLayout;
import br.angarion.dev.engine.communication.MBT;
import br.angarion.dev.engine.network.MessageDeliveryHandler;
import br.angarion.dev.engine.network.transport.MessageReceivedListener;
import br.angarion.dev.engine.runtime.ComponentResolver;
import br.angarion.dev.engine.runtime.InnerProcessor;
import br.angarion.dev.engine.runtime.MemoryLender;
import java.lang.foreign.MemorySegment;

final class ObedientGateway implements MessageReceivedListener {

    private final ComponentResolver resolver;
    private final MemoryLender memoryLender;
    private final MessageDeliveryHandler deliveryHandler;

    public ObedientGateway(
        ComponentResolver componentResolver,
        MemoryLender memoryLender,
        MessageDeliveryHandler deliveryHandler
    ) {
        this.resolver = componentResolver;
        this.memoryLender = memoryLender;
        this.deliveryHandler = deliveryHandler;
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

        ValidatorResponse response = innerProcessor
            .validator()
            .validate(message);

        if (response.getStatus() == IR.SUCCESS) {
            innerProcessor.useCase().execute(clientId, message);
            MemorySegment ir = memoryLender.borrow((int) IR.HEADER_SIZE);
            IR.writeHeader(ir, (int) ir.byteSize(), clientId, IR.SUCCESS, 0);

            deliveryHandler.deliveryIr(ir, clientId);
        }
    }
}
