package br.angarion.dev.engine.network.client;

import java.lang.foreign.MemorySegment;
import java.lang.foreign.ValueLayout;

import br.angarion.dev.api.network.ClientConsumer;
import br.angarion.dev.engine.network.transport.Channel;
import br.angarion.dev.engine.network.transport.ChannelCreatedListener;
import br.angarion.dev.engine.network.protocol.ChannelProcessedListener;
import br.angarion.dev.engine.network.NetworkAggregatorSingleton;
import br.angarion.dev.engine.network.protocol.IntentionGateway;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntStack;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectList;

// LC = LifeCycle

public class ClientLCManager implements ChannelCreatedListener {
    private final ObjectList<Client> clients;
    private final IntStack freeIds; 
    private final IntentionGateway intentionGateway;
    public final int maxClients;

    private final ChannelProcessedListener[] listeners;
    private int subscriberCursor = 0;

    public ClientLCManager(int maxClients, IntentionGateway intentionGateway) {
        this.intentionGateway = intentionGateway;
        this.maxClients = maxClients;
        this.clients = new ObjectArrayList<>(maxClients);
        this.listeners = new ChannelProcessedListener[4];

        this.freeIds = new IntArrayList(maxClients);
        for (int i = maxClients - 1; i >= 0; i--) {
            freeIds.push(i);
        }
    }

    @Override
    public void onChannelCreated(Channel channel) {
        if (freeIds.isEmpty()) {
            System.out.println("Channel from " + channel.getId() + " rejected: server is full.");
            return;
        }

        int id = freeIds.popInt();

        System.out.println(channel.getId() + " connected. Assigned client ID: " + id);
        System.out.println("New channel created with ID: " + id);
        
        channel.setId(id); // This links created Id with channel Id; Don't remove it!

        System.out.println("New Id linked to channel: " + channel.getId());

        Client client = new Client.Builder()
                .id(id)
                .channel(channel)
                .build();

        client.getChannel().subscribe(intentionGateway);

        while (clients.size() <= id) {
            clients.add(null);
        }
        clients.set(id, client);

        notifyChannelProcessed(channel);
    }

    public boolean isConnected(int id) {
        final int currentSize = this.clients.size();
        return id >= 0 && id < currentSize && clients.get(id) != null;
    }

    public ObjectList<Client> getClients() {
        return clients;
    }

    public Client getClientById(int id) {
        final int currentSize = this.clients.size();
        if (id < 0 || id >= currentSize) return null;
        return clients.get(id);
    }

    public int onlineCount() {
        int count = 0;
        final int currentSize = this.clients.size();
        for (int i = 0; i < currentSize; i++) {
            if (clients.get(i) != null) {
                count++;
            }
        }
        return count;
    }

    public void forEachOnline(ClientConsumer consumer) {
        final int currentSize = this.clients.size();
        for (int i = 0; i < currentSize; i++) {
            final Client client = clients.get(i);
            if (client != null) {
                consumer.accept(client.getId());
            }
        }
    }

    public void disconnectClient(int id) {
        final int currentSize = this.clients.size();
        if (id >= 0 && id < currentSize && clients.get(id) != null) {
            clients.set(id, null);
            freeIds.push(id);
        }
    }

    public void sendTo(int id, MemorySegment segment) {
        final Client client = getClientById(id);
        if (client == null) {
            return;
        }

        sendTo(client, segment);
    }

    public void sendTo(int[] ids, MemorySegment segment) {
        final int len = ids.length;
        for (int i = 0; i < len; i++) {
            sendTo(ids[i], segment);
        }
    }

    public void sendToAll(MemorySegment segment) {
        final int currentSize = clients.size();
        for (int i = 0; i < currentSize; i++) {
            Client client = clients.get(i);
            if (client != null) {
                sendTo(client, segment);
            }
        }
    }

    private void sendTo(Client client, MemorySegment segment) {
        NetworkAggregatorSingleton.get().append(client.getId(), segment.toArray(ValueLayout.JAVA_BYTE));
    }

    public void subscribe(ChannelProcessedListener listener) {
        if (subscriberCursor >= listeners.length) {
            throw new IllegalStateException("Max listeners reached: " + listeners.length);
        }
        listeners[subscriberCursor] = listener;
        subscriberCursor += 1;
    }

    public void notifyChannelProcessed(Channel channel) {
        final ChannelProcessedListener[] listeners = this.listeners;
        final int limit = this.subscriberCursor;

        for (int i = 0; i < limit; i++) {
            listeners[i].onChannelProcessed(channel);
        }
    }
}