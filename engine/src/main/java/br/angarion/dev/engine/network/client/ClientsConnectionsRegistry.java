package br.angarion.dev.engine.network.client;

import java.util.Set;

import it.unimi.dsi.fastutil.ints.IntObjectBiConsumer;

public interface ClientsConnectionsRegistry<T extends Client> {
    T get(int id);
    void put(int id, T client);
    void remove(int id);
    boolean contains(int id);
    void forEach(IntObjectBiConsumer<T> consumer);
    int lenght();
    Set<Integer> keySet();
}
