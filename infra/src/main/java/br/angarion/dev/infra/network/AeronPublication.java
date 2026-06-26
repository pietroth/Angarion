// I need to transform this into non-blocking without while loops.

package br.angarion.dev.infra.network;

import java.lang.foreign.Arena;
import java.lang.foreign.MemorySegment;

import org.agrona.concurrent.IdleStrategy;
import io.aeron.ExclusivePublication;
import io.aeron.logbuffer.BufferClaim;

final class AeronPublication implements AutoCloseable {
    private final ExclusivePublication exclusivePublication;
    private final IdleStrategy idleStrategy;
    private final BufferClaim bufferClaim;

    public AeronPublication(ExclusivePublication exclusivePublication, IdleStrategy idleStrategy) {
        this.exclusivePublication = exclusivePublication;
        this.idleStrategy = idleStrategy;
        this.bufferClaim = new BufferClaim();
    }

    public void tryClaim(final MemorySegment payload, final int offset, final int lenght) {
        idleStrategy.reset();

        while (true) {
            final long result = exclusivePublication.tryClaim(lenght, bufferClaim);

            if (result > 0) {
                try {
                    final long addressDestination = bufferClaim.buffer().addressOffset() + bufferClaim.offset();

                    final MemorySegment segmentDestination = MemorySegment.ofAddress(addressDestination)
                        .reinterpret(lenght, Arena.global(), null);
                    
                    segmentDestination.copyFrom(payload.asSlice(offset, lenght));
                    
                } finally {
                    bufferClaim.commit();
                }
                return;
            }

            if (result == ExclusivePublication.BACK_PRESSURED || result == ExclusivePublication.NOT_CONNECTED) {
                idleStrategy.idle();
            } else if (result == ExclusivePublication.ADMIN_ACTION) {
                idleStrategy.idle();
            } else if (result == ExclusivePublication.CLOSED) {
                throw new IllegalStateException("Publication is closed");
            } else if (result == ExclusivePublication.MAX_POSITION_EXCEEDED) {
                throw new IllegalStateException("Publication max position exceeded");
            }
        }
        
    }

    @Override
    public void close(){
        if (exclusivePublication != null) {
            exclusivePublication.close();
        }
    }
}
