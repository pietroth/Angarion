package br.angarion.dev.infra.network;

import io.aeron.Subscription;
import io.aeron.logbuffer.FragmentHandler;
import io.aeron.logbuffer.Header;
import org.agrona.DirectBuffer;
import org.agrona.concurrent.Agent;

import java.lang.foreign.Arena;
import java.lang.foreign.MemorySegment;
import java.util.Objects;
import java.util.function.Consumer;

final class AeronSubscription implements Agent {
    private final Subscription subscription;
    private final FragmentHandler fragmentHandler;
    private final Consumer<MemorySegment> currentConsumer;
    private final static int FRAGMENT_LIMIT = 32;

    public AeronSubscription(final Subscription subscription, final Consumer<MemorySegment> messageConsumer) {
        this.subscription = Objects.requireNonNull(subscription, "subscription");
        this.fragmentHandler = this::onFragment;
        this.currentConsumer = messageConsumer;
    }

    @Override
    public int doWork() {
        if (subscription.isClosed()) {
            return 0;
        }
        return subscription.poll(fragmentHandler, FRAGMENT_LIMIT);
    }

    private void onFragment(
        final DirectBuffer buffer,
        final int offset,
        final int length,
        final Header ignoredHeader) {
        
        long address = buffer.addressOffset() + offset;
        
        MemorySegment segment = MemorySegment.ofAddress(address)
                                            .reinterpret(length, Arena.global(), null);

        final Consumer<MemorySegment> consumer = currentConsumer;
        if (consumer == null) {
            throw new IllegalStateException("Fragment received without an active consumer");
        }
        
        consumer.accept(segment);
    }

    public boolean isConnected() {
        return subscription.isConnected();
    }

    public boolean hasNoImages() {
        return subscription.hasNoImages();
    }

    public int imageCount() {
        return subscription.imageCount();
    }

    public boolean isClosed() {
        return subscription.isClosed();
    }

    @Override
    public String roleName() {
        return "aeron-subscription";
    }
}
