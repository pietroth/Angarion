package br.angarion.dev.engine.network.protocol;

import br.angarion.dev.engine.network.transport.Connection;

public interface ConnectionProcessedListener {
    void onConnectionProcessed(Connection connection);
}
