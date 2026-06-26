package br.angarion.dev.infra.network;

import br.angarion.dev.engine.network.client.Client;

final class AeronClient implements Client {
    private AeronPublication publication;
    private int id;

    public AeronClient(AeronPublication publication) {
        this.publication = publication;
        this.id = 0;
    }

    @Override
    public int getId() {
        return id;
    }

    @Override
    public void setId(int id) {
        this.id = id;
    }

    public AeronPublication getPublication() {
        return publication;
    }

    public void setPublication(AeronPublication publication) {
        this.publication = publication;
    }
}
