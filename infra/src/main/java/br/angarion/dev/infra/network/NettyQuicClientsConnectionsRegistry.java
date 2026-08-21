package br.angarion.dev.infra.network;

import java.util.Set;

import br.angarion.dev.engine.network.client.ClientsConnectionsRegistry;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.ints.IntObjectBiConsumer;

class NettyQuicClientsConnectionsRegistry implements ClientsConnectionsRegistry<NettyQuicClient> {
    private final Int2ObjectOpenHashMap<NettyQuicClient> clientsIds;

    public NettyQuicClientsConnectionsRegistry() {
        clientsIds = new Int2ObjectOpenHashMap<NettyQuicClient>();
    }

    public NettyQuicClient get(int id) {
        return clientsIds.get(id);
    }

    public void put(int id, NettyQuicClient client) {
        clientsIds.put(id, client);
    }

    public void remove(int id) {
        clientsIds.remove(id);
    }

    public boolean contains(int id) {
        return clientsIds.containsKey(id);
    }

    public void forEach(IntObjectBiConsumer<NettyQuicClient> consumer) {
        clientsIds.forEach(consumer);
    }

    public int lenght() {
        return clientsIds.size();
    }

    public Set<Integer> keySet() {
        return clientsIds.keySet();
    }
}
