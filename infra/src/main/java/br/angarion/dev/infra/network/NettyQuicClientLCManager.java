package br.angarion.dev.infra.network;

import java.util.ArrayList;

import br.angarion.dev.engine.network.client.ClientConnectedListener;
import br.angarion.dev.engine.network.client.ClientLCManager;
import br.angarion.dev.engine.network.client.ClientProcessedListener;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntObjectBiConsumer;

public class NettyQuicClientLCManager implements ClientLCManager<NettyQuicClient> {
    private final NettyQuicClientsConnectionsRegistry clientsConnectionsRegistry;
    private final ArrayList<ClientProcessedListener> clientProcessedListeners = new ArrayList<ClientProcessedListener>();
    private final ArrayList<ClientConnectedListener> clientConnectedListeners = new ArrayList<ClientConnectedListener>();
    private final IntArrayList freeIds = new IntArrayList();

    private final int maxClients;
    private int nextId = 0;

    public NettyQuicClientLCManager(NettyQuicClientsConnectionsRegistry connectionsRegistry, int maxClients) {
        this.clientsConnectionsRegistry = connectionsRegistry;
        this.maxClients = maxClients;
    }

    public NettyQuicClientLCManager(NettyQuicClientsConnectionsRegistry connectionsRegistry) {
        this.clientsConnectionsRegistry = connectionsRegistry;
        this.maxClients = 0;
    }

    @Override
    public int connect(NettyQuicClient client) {
        if (clientsConnectionsRegistry.lenght() >= maxClients) {
            throw new IllegalArgumentException("Max clients reached!");
        }

        int id = acquireId();
        clientsConnectionsRegistry.put(id, (NettyQuicClient) client);
        return id;
    }

    @Override
    public void disconnectClient(int id) {
        clientsConnectionsRegistry.remove(id);
        releaseId(id);
    }

    @Override
    public void forEachOnline(IntObjectBiConsumer<NettyQuicClient> consumer) {
        clientsConnectionsRegistry.forEach(consumer);
    }

    @Override
    public int onlineCount() {
        return clientsConnectionsRegistry.lenght();
    }

    @Override
    public boolean isConnected(int id) {
        return clientsConnectionsRegistry.contains(id);
    }

    @Override
    public void subscribe(ClientProcessedListener listener) {
        clientProcessedListeners.add(listener);
    }

    @Override
    public void subscribe(ClientConnectedListener listener) {
        clientConnectedListeners.add(listener);
    }

    public int acquireId() {
        if (!freeIds.isEmpty()) {
            return freeIds.popInt();
        }
        return nextId++;
    }

    public void releaseId(int id) {
        freeIds.push(id);
    }
}
