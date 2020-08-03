package net.openhft.chronicle.core.io;

import net.openhft.chronicle.core.CoreTestCommon;
import net.openhft.chronicle.core.Jvm;
import net.openhft.chronicle.core.onoes.ExceptionHandler;
import net.openhft.chronicle.core.onoes.ExceptionKey;
import org.jetbrains.annotations.NotNull;
import org.junit.Test;

import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.SynchronousQueue;
import java.util.stream.Collectors;

import static org.junit.Assert.*;
import static org.junit.Assume.assumeTrue;

public class AbstractCloseableTest extends CoreTestCommon {

    @Test
    public void close() throws IllegalStateException {
        MyCloseable mc = new MyCloseable();
        assertFalse(mc.isClosed());
        assertEquals(0, mc.beforeClose);
        assertEquals(0, mc.performClose);

        mc.throwExceptionIfClosed();

        mc.close();
        assertTrue(mc.isClosed());
        assertEquals(1, mc.beforeClose);
        assertEquals(1, mc.performClose);

        mc.close();
        assertTrue(mc.isClosed());
        assertEquals(2, mc.beforeClose);
        assertEquals(1, mc.performClose);

        mc.close();
        assertTrue(mc.isClosed());
        assertEquals(3, mc.beforeClose);
        assertEquals(1, mc.performClose);
    }

    @Test(expected = IllegalStateException.class)
    public void throwExceptionIfClosed() throws IllegalStateException {
        MyCloseable mc = new MyCloseable();
        mc.close();
        mc.throwExceptionIfClosed();
    }

    @Test
    public void warnAndCloseIfNotClosed() {
        assumeTrue(Jvm.isResourceTracing());

        Map<ExceptionKey, Integer> map = Jvm.recordExceptions();
        MyCloseable mc = new MyCloseable();

        // not recorded for now.
        mc.warnAndCloseIfNotClosed();

        assertTrue(mc.isClosed());
        Jvm.resetExceptionHandlers();
        assertEquals("Discarded without closing\n" +
                        "java.lang.IllegalStateException: net.openhft.chronicle.core.StackTrace: class net.openhft.chronicle.core.io.AbstractCloseableTest$MyCloseable - Created Here on main",
                map.keySet().stream()
                        .map(e -> e.message + "\n" + e.throwable)
                        .collect(Collectors.joining(", ")));
    }

    @Test
    public void closeTookTooLong() {
        assumeTrue(Jvm.isResourceTracing());

        Map<ExceptionKey, Integer> map = Jvm.recordExceptions();
        MySlowCloseable mc = new MySlowCloseable();
        mc.close();
        assertTrue(mc.isClosed());
        Jvm.resetExceptionHandlers();
        assertEquals("Took xx ms to performClose on MySlowCloseable",
                map.keySet().stream()
                        .map(e -> e.message)
                        .collect(Collectors.joining(", "))
                        .replaceAll("\\d", "x"));
    }

    @Test
    public void threadSafetyCheck() throws InterruptedException {
        assumeTrue(Jvm.isResourceTracing());

        MyCloseable mc = new MyCloseable();
        BlockingQueue<String> q = new SynchronousQueue<>();
        Thread t = new Thread(() -> {
            try {
                mc.resetUsedByThread();
                q.put("reset");
                Jvm.pause(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        });
        t.start();
        q.take();
        try {
            mc.throwExceptionIfClosed();
            fail();
        } catch (IllegalStateException e) {
            e.printStackTrace();
        } finally {
            mc.close();
        }
        assertTrue(mc.isClosed());
    }

    static class MySlowCloseable extends AbstractCloseable {
        @Override
        protected void performClose() {
            Jvm.pause(20);
        }

        @NotNull
        @Override
        protected ExceptionHandler warn() {
            return Jvm.warn();
        }
    }

    static class MyCloseable extends AbstractCloseable {
        int beforeClose;
        int performClose;

        @Override
        protected void beforeClose() {
            super.beforeClose();
            beforeClose++;
        }

        @Override
        protected void performClose() {
            performClose++;
        }

        @NotNull
        @Override
        protected ExceptionHandler warn() {
            return Jvm.warn();
        }
    }
}