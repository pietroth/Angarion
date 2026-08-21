package br.angarion.dev.infra.network;

import java.util.ArrayList;
import java.util.function.Consumer;

import br.angarion.dev.engine.network.client.Client;
import br.angarion.dev.engine.network.client.ClientConnectedListener;
import br.angarion.dev.engine.network.client.ClientLCManager;
import br.angarion.dev.engine.network.client.ClientProcessedListener;
import it.unimi.dsi.fastutil.ints.IntArrayList;

public class NettyQuicClientLCManager implements ClientLCManager {
    private final NettyQuicClientsConnectionsRegistry clientsConnectionsRegistry = new NettyQuicClientsConnectionsRegistry();
    private final ArrayList<ClientProcessedListener> clientProcessedListeners = new ArrayList<ClientProcessedListener>();
    private final ArrayList<ClientConnectedListener> clientConnectedListeners = new ArrayList<ClientConnectedListener>();
    private final IntArrayList freeIds = new IntArrayList();

    private final int maxClients;
    private int nextId = 0;

    public NettyQuicClientLCManager(int maxClients) {
       this.maxClients = maxClients;
    }

    public NettyQuicClientLCManager() {
        this.maxClients = 0;
    }

    @Override
    public int connect(Client client) {
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
    public void forEachOnline(Consumer<Client> consumer) {

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
