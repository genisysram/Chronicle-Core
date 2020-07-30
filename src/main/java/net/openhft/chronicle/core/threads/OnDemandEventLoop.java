package net.openhft.chronicle.core.threads;

import net.openhft.chronicle.core.io.Closeable;

import java.util.function.Supplier;

public class OnDemandEventLoop implements EventLoop {
    private final Supplier<EventLoop> eventLoopSupplier;
    private volatile EventLoop eventLoop;

    public OnDemandEventLoop(Supplier<EventLoop> eventLoopSupplier) {
        this.eventLoopSupplier = eventLoopSupplier;
    }

    EventLoop eventLoop() {
        EventLoop el = this.eventLoop;
        if (el != null)
            return el;
        synchronized (this) {
            el = this.eventLoop;
            if (el != null)
                return el;
            return eventLoop = eventLoopSupplier.get();
        }
    }

    public boolean hasEventLoop() {
        return eventLoop != null;
    }

    @Override
    public String name() {
        return eventLoop().name();
    }

    @Override
    public Closeable addHandler(EventHandler handler) {
        return eventLoop().addHandler(handler);
    }

    @Override
    public void start() {
        eventLoop().start();
    }

    @Override
    public void unpause() {
        if (hasEventLoop())
            eventLoop().unpause();
    }

    @Override
    public void stop() {
        if (hasEventLoop())
            eventLoop().stop();
    }

    @Override
    public boolean isClosed() {
        return !hasEventLoop() || eventLoop().isClosed();
    }

    @Override
    public boolean isAlive() {
        return hasEventLoop() && eventLoop().isAlive();
    }

    @Override
    public void awaitTermination() {
        if (hasEventLoop())
            eventLoop().awaitTermination();
    }

    @Override
    public void close() {
        if (hasEventLoop())
            eventLoop().close();
    }
}
