package br.angarion.dev.infra.network;

import br.angarion.dev.api.network.NetworkThreadingMode;
import br.angarion.dev.engine.network.client.ClientConnectedListener;
import br.angarion.dev.engine.network.transport.Server;
import io.aeron.Aeron;
import io.aeron.driver.MediaDriver;
import io.aeron.driver.ThreadingMode;
import org.agrona.concurrent.AgentRunner;
import org.agrona.concurrent.IdleStrategy;

public final class AeronServer implements Server<AeronClient> {

    private final ClientConnectedListener<AeronClient>[] listeners;
    private int subscriberCursor = 0;

    private final ThreadingMode aeronThreadingMode;
    private final IdleStrategy idleStrategy;
    private final AeronClientLCManager clientLCManager;
    private final AeronSubscription subscription;

    @SuppressWarnings("unchecked")
    public AeronServer(
        NetworkThreadingMode threadingMode,
        ThreadingMode aeronThreadingMode,
        IdleStrategy idleStrategy,
        AeronClientLCManager clientLCManager,
        AeronSubscription subscription
    ) {
        this.listeners =
            (ClientConnectedListener<AeronClient>[]) new ClientConnectedListener[4];
        this.aeronThreadingMode = aeronThreadingMode;
        this.idleStrategy = idleStrategy;
        this.clientLCManager = clientLCManager;
        this.subscription = subscription;
    }

    @Override
    public void run() {
        MediaDriver.Context context = new MediaDriver.Context()
            .threadingMode(aeronThreadingMode)
            .sharedIdleStrategy(idleStrategy)
            .dirDeleteOnStart(true)
            .dirDeleteOnShutdown(true);

        try (MediaDriver driver = MediaDriver.launchEmbedded(context)) {
            System.out.println(
                "MediaDriver initialized in: " + driver.aeronDirectoryName()
            );

            Aeron.Context aeronContext = new Aeron.Context().aeronDirectoryName(
                driver.aeronDirectoryName()
            );

            AgentRunner runner = new AgentRunner(
                idleStrategy,
                Throwable::printStackTrace,
                null,
                subscription
            );

            Aeron.connect(aeronContext);

            try (AgentRunner autoCloseableRunner = runner) {
                Thread runnerThread = AgentRunner.startOnThread(
                    autoCloseableRunner
                );

                while (
                    !runnerThread.isInterrupted() &&
                    !Thread.currentThread().isInterrupted()
                ) {
                    Thread.sleep(1000);
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                System.out.println("Main thread interrupted");
            }
        }
    }

    @Override
    public void subscribe(ClientConnectedListener<AeronClient> listener) {
        if (subscriberCursor >= listeners.length) {
            throw new IllegalStateException(
                "Max listeners reached: " + listeners.length
            );
        }
        listeners[subscriberCursor] = listener;
        subscriberCursor += 1;
    }
}
