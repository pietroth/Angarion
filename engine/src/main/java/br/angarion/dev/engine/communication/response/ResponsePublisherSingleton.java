package br.angarion.dev.engine.communication.response;

public class ResponsePublisherSingleton {

    private static ResponsePublisher instance;

    public static void init(ResponsePublisher publisher) {
        if (instance != null) {
            throw new IllegalStateException(
                "ResponsePublisherSingleton is already initialized"
            );
        }
        instance = publisher;
    }

    public static ResponsePublisher get() {
        if (instance == null) {
            throw new IllegalStateException(
                "ResponsePublisherSingleton is not initialized"
            );
        }
        return instance;
    }
}
