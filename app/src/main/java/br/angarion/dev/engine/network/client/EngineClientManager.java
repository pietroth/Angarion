package br.angarion.dev.engine.network.client;

import br.angarion.dev.api.network.ClientConsumer;
import br.angarion.dev.api.network.ClientManager;

public class EngineClientManager implements ClientManager{
    private final ClientLCManager clientLCManager;

    public EngineClientManager(ClientLCManager clientLCManager) {
        this.clientLCManager = clientLCManager;
    }

    @Override
    public boolean isConnected(int clientId) {
        return clientLCManager.isConnected(clientId);
    }

    @Override
    public int maxClients() {
        return clientLCManager.maxClients;
    }

    @Override
    public int onlineCount() {
        return clientLCManager.onlineCount();
    }

    @Override
    public void disconnect(int clientId) {
        clientLCManager.disconnectClient(clientId);
    }

    @Override
    public void forEachOnline(ClientConsumer consumer) {
        clientLCManager.forEachOnline(consumer);
    }
    
}
