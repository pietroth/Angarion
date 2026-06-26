package br.angarion.dev.engine.runtime;

public final class MemoryBuffer {

    private final int chunkSize;
    private final int capacity;

    public MemoryBuffer(int chunkSize, int capacity) {
        this.chunkSize = chunkSize;
        this.capacity = capacity;
    }

    public int getChunkSize() {
        return this.chunkSize;
    }

    public int getCapacity() {
        return this.capacity;
    }
}
