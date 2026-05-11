package br.angarion.dev.engine.network.protocol;

import br.angarion.dev.api.network.Connection;

public interface ConnectionProcessedListener {
    void onConnectionProcessed(Connection connection);
}
