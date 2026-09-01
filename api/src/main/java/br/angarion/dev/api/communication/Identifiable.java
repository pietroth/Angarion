package br.angarion.dev.api.communication;

import java.util.concurrent.atomic.AtomicInteger;

public interface Identifiable {
    AtomicInteger ID_GENERATOR = new AtomicInteger(0);

    int id();

    static int getAndIncrement() {
        return ID_GENERATOR.getAndIncrement();
    }
}
