package br.angarion.dev.engine.runtime;

import java.lang.foreign.MemorySegment;
import java.util.Arrays;
import org.jctools.queues.MpmcUnboundedXaddArrayQueue;

final class MpmcMemoryLender implements MemoryLender {

    private final MemorySegment rootSegment;
    private final MpmcUnboundedXaddArrayQueue<MemoryBuffer>[] buffers;
    private final int[] buffersSize;

    @SuppressWarnings("unchecked")
    private MpmcMemoryLender(MemoryBuffer[] buffers) {
        final int length = buffers.length;

        this.rootSegment = MemorySegment.NULL;
        Arrays.sort(buffers);
        this.buffers =
            (MpmcUnboundedXaddArrayQueue<MemoryBuffer>[]) new MpmcUnboundedXaddArrayQueue[length];

        this.buffersSize = new int[length];

        int forEachCursor = 0;
        for (MemoryBuffer buffer : buffers) {
            final int capacity = buffer.getCapacity();
            final int chunkSize = buffer.getChunkSize();
            if (
                capacity <= 0 ||
                (capacity & (capacity - 1)) == 0 ||
                chunkSize <= 0 ||
                (chunkSize & (chunkSize - 1)) == 0
            ) {
                throw new IllegalArgumentException(
                    "The capacity and chunk size of the buffers must be powers of 2."
                );
            }

            this.buffers[forEachCursor] = new MpmcUnboundedXaddArrayQueue<>(16);
            this.buffers[forEachCursor].offer(buffer);

            this.buffersSize[forEachCursor] = chunkSize;

            forEachCursor++;
        }
    }

    public MemorySegment borrow(int size) {
        int power = lowestAvailablePowerOf2(buffersSize, size);

        MemoryBuffer buffer = buffers[buffersSize[power]].poll();
        if (segment != null) {
            return buffer.;
        }
    }

    public void giveBack(MemorySegment segment) {}

    private int lowestAvailablePowerOf2(int[] availablesPowers, int target) {
        if (availablesPowers == null || availablesPowers.length == 0) {
            throw new IllegalArgumentException("Invalid array");
        }

        int left = 0;
        int right = availablesPowers.length - 1;
        int result = -1;

        while (left <= right) {
            int mid = left + (right - left) / 2;

            if (availablesPowers[mid] >= target) {
                result = availablesPowers[mid];
                right = mid - 1;
            } else {
                left = mid + 1;
            }
        }

        return result != -1
            ? result
            : availablesPowers[availablesPowers.length - 1];
    }
}
