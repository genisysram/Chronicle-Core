package net.openhft.chronicle.core.threads;


import net.openhft.chronicle.core.io.Closeable;
import org.jetbrains.annotations.NotNull;

/**
 * Manage the lifecycle of an EventHandler
 */
public class EventHandlerManager implements EventHandler, Closeable {
    private transient volatile boolean closed, performedClose;
    private final EventHandler eventHandler;
    private EventLoop eventLoop;

    protected EventHandlerManager(EventHandler eventHandler) {
        this.eventHandler = eventHandler;
    }

    public static EventHandlerManager wrap(EventHandler handler) {
        return handler instanceof EventHandlerManager
                ? (EventHandlerManager) handler
                : new EventHandlerManager(handler);
    }

    @Override
    public boolean action() throws InvalidEventHandlerException, InterruptedException {
        if (isClosed()) {
            callPerformCloseOnce();
            throw new InvalidEventHandlerException();
        }
        try {
            return eventHandler.action();
        } catch (InvalidEventHandlerException toBeRemoved) {
            close();
            callPerformCloseOnce();
            throw toBeRemoved;
        }
    }

    public void callPerformCloseOnce() {
        if (!performedClose) {
            performClose();
            performedClose = true;
        }
    }

    @Override
    public boolean isClosed() {
        return closed;
    }

    @Override
    public void eventLoop(EventLoop eventLoop) {
        this.eventLoop = eventLoop;
        eventHandler.eventLoop(eventLoop);
    }

    protected void performClose() {
        eventHandler.close();
    }

    @Override
    public void close() {
        closed = true;
        if (eventLoop == null || eventLoop.isClosed())
            callPerformCloseOnce();
    }

    @NotNull
    @Override
    public HandlerPriority priority() {
        return eventHandler.priority();
    }
}
