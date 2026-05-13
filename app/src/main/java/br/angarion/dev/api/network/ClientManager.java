package br.angarion.dev.api.communication;

public interface ClientManager {
    boolean isConnected(int clientId);
    int maxClients();
    int onlineCount();
    void disconnect(int clientId);
    void forEachOnline();
}
