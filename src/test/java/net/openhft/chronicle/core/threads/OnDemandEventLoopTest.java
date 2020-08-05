package net.openhft.chronicle.core.threads;

import net.openhft.chronicle.core.io.Closeable;
import org.jetbrains.annotations.NotNull;
import org.junit.Test;

import java.util.function.BiFunction;

import static org.junit.Assert.*;

public class OnDemandEventLoopTest {
    @Test
    public void onDemand() {
        OnDemandEventLoop el = new OnDemandEventLoop(() -> new EventLoop() {
            @Override
            public String name() {
                return "dummy";
            }

            @Override
            public Closeable addHandler(EventHandler handler) {
                throw new UnsupportedOperationException();
            }

            @Override
            public Closeable addHandler(@NotNull BiFunction<Thread, EventLoop, EventHandler> constructor, @NotNull HandlerPriority priority) {
                throw new UnsupportedOperationException();
            }

            @Override
            public void start() {
                throw new UnsupportedOperationException();
            }

            @Override
            public void unpause() {
                throw new UnsupportedOperationException();
            }

            @Override
            public void stop() {
                throw new UnsupportedOperationException();
            }

            @Override
            public boolean isClosed() {
                throw new UnsupportedOperationException();
            }

            @Override
            public boolean isAlive() {
                throw new UnsupportedOperationException();
            }

            @Override
            public void awaitTermination() {
                throw new UnsupportedOperationException();
            }

            @Override
            public void close() {
            }
        });
        assertFalse(el.hasEventLoop());
        assertEquals("dummy", el.name());
        assertTrue(el.hasEventLoop());
        el.close();
    }

}