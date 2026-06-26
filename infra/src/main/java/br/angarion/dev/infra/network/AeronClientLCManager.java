package br.angarion.dev.infra.network;

import java.lang.foreign.MemorySegment;
import java.util.function.Consumer;

import br.angarion.dev.engine.network.client.ClientLCManager;
import br.angarion.dev.engine.network.client.ClientProcessedListener;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntStack;

// LC = LifeCycle

public class AeronClientLCManager implements ClientLCManager<AeronClient>, AutoCloseable {
    private final IntStack freeIds;
    private final AeronPublication[] publications;
    private final ClientProcessedListener<AeronClient>[] listeners;
    private int listenersCursor = 0;
    private int onlineCount = 0;
    private AeronClient reusableClient; 

    @SuppressWarnings("unchecked")
    public AeronClientLCManager(int maxPublications) {
        freeIds = new IntArrayList(maxPublications);
        publications = new AeronPublication[maxPublications];
        listeners = (ClientProcessedListener<AeronClient>[]) new ClientProcessedListener[4];
        reusableClient = new AeronClient(null);

        for (int i = maxPublications - 1; i >= 0; i--) {
            freeIds.push(i);
        }
    }

    @Override
    public void onClientConnected(AeronClient client) {
        if (freeIds.isEmpty()) {
            throw new IllegalStateException("Server full!");
        }

        int id = freeIds.popInt();
        client.setId(id);
        publications[id] = client.getPublication();
        onlineCount++;
    }  

    @Override
    public void sendTo(int[] ids, MemorySegment message) {
        for (int i = 0; i < ids.length; i++) {
            int publicationId = ids[i];
            sendTo(publicationId, message);
        }
    }

    @Override
    public void sendTo(int id, MemorySegment message) {
        publications[id].tryClaim(message, 0, (int) message.byteSize());
    }

    @Override
    public void broadcast(MemorySegment message) {
        final int currentSize = publications.length;
        for (int i = 0; i < currentSize; i++) {
            final AeronPublication publication = publications[i];
            if (publication != null) publication.tryClaim(message, 0, (int) message.byteSize());
        }
    }

    @Override
    public void disconnectClient(int id) {
        if (publications[id] != null) {
            publications[id] = null;
            freeIds.push(id);
            onlineCount--;
        }
    }

    @Override
    public void forEachOnline(Consumer<AeronClient> consumer) {
        final int currentSize = publications.length;
        final AeronClient reusableClient = this.reusableClient;
        for (int i = 0; i < currentSize; i++) {
            final AeronPublication publication = publications[i];
            reusableClient.setId(i);
            reusableClient.setPublication(publication);
            if (publication != null) consumer.accept(reusableClient);
        }
    }

    @Override
    public int onlineCount() {
        return onlineCount;
    }

    @Override
    public boolean isConnected(int id) {
        if (publications[id] != null) return true;
        return false;
    }

    @Override
    public void subscribe(ClientProcessedListener<AeronClient> listener) {
        if (listenersCursor >= 4) 
            throw new IllegalStateException("Listeners limit reached! Max: 4");

        listeners[listenersCursor] = listener;
        listenersCursor += 1;
    }

    @Override
    public void close() throws Exception {
    }
}