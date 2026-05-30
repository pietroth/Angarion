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

    private final ObjectList<ChannelProcessedListener> listeners;

    public ClientLCManager(int maxClients, IntentionGateway intentionGateway) {
        this.intentionGateway = intentionGateway;
        this.maxClients = maxClients;
        this.clients = new ObjectArrayList<>();
        this.listeners = new ObjectArrayList<>();

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
        return id >= 0 && id < clients.size() && clients.get(id) != null;
    }

    public ObjectList<Client> getClients() {
        return clients;
    }

    public Client getClientById(int id) {
        if (id < 0 || id >= clients.size()) return null;
        return clients.get(id);
    }

    public int onlineCount() {
        int count = 0;
        for (Client client : clients) {
            if (client != null) {
                count++;
            }
        }
        return count;
    }

    public void forEachOnline(ClientConsumer consumer) {
        for (Client client : clients) {
            if (client != null) {
                consumer.accept(client.getId());
            }
        }
    }

    public void disconnectClient(int id) {
        if (id >= 0 && id < clients.size() && clients.get(id) != null) {
            clients.set(id, null);
            freeIds.push(id);
        }
    }

    public void sendTo(int id, MemorySegment segment) {
        Client client = getClientById(id);
        if (client == null) {
            return;
        }

        sendTo(client, segment);
    }

    public void sendTo(int[] ids, MemorySegment segment) {
        for (int id : ids) {
            sendTo(id, segment);
        }
    }

    public void sendToAll(MemorySegment segment) {
        int size = clients.size();
        for (int i = 0; i < size; i++) {
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
        listeners.add(listener);
    }

    public void unsubscribe(ChannelProcessedListener listener) {
        listeners.remove(listener);
    }

    public void notifyChannelProcessed(Channel channel) {
        for (ChannelProcessedListener listener : listeners) {
            listener.onChannelProcessed(channel);
        }
    }
}
