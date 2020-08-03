package net.openhft.chronicle.core.io;

import net.openhft.chronicle.core.CoreTestCommon;
import net.openhft.chronicle.core.Jvm;
import net.openhft.chronicle.core.onoes.ExceptionHandler;
import net.openhft.chronicle.core.onoes.ExceptionKey;
import org.jetbrains.annotations.NotNull;
import org.junit.Test;

import java.util.Map;
import java.util.stream.Collectors;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import static org.junit.Assume.assumeTrue;

public class AbstractReferenceCountedTest extends CoreTestCommon {

    @Test
    public void reserve() throws IllegalStateException {
        assumeTrue(Jvm.isResourceTracing());

        MyReferenceCounted rc = new MyReferenceCounted();
        assertEquals(1, rc.refCount());

        ReferenceOwner a = ReferenceOwner.temporary("a");
        rc.reserve(a);
        assertEquals(2, rc.refCount());

        ReferenceOwner b = ReferenceOwner.temporary("b");
        rc.reserve(b);
        assertEquals(3, rc.refCount());

        try {
            rc.reserve(a);
            fail();
        } catch (IllegalStateException ignored) {
        }
        assertEquals(3, rc.refCount());

        rc.release(b);
        assertEquals(2, rc.refCount());

        rc.release(a);
        assertEquals(1, rc.refCount());
        assertEquals(0, rc.performRelease);

        rc.releaseLast();
        assertEquals(0, rc.refCount());
        assertEquals(1, rc.performRelease);
    }

    @Test
    public void releaseTookTooLong() {
        assumeTrue(Jvm.isResourceTracing());

        Map<ExceptionKey, Integer> map = Jvm.recordExceptions();
        MySlowReleased mc = new MySlowReleased();
        mc.releaseLast();
        assertEquals(0, mc.refCount());
        Jvm.resetExceptionHandlers();
        assertEquals("Took xx.x ms to performRelease on MySlowReleased",
                map.keySet().stream()
                        .map(e -> e.message)
                        .collect(Collectors.joining(", "))
                        .replaceAll("\\d", "x"));
    }

    static class MySlowReleased extends AbstractReferenceCounted {
        public MySlowReleased() {
        }

        @Override
        protected void performRelease() {
            Jvm.pause(20);
        }

        @NotNull
        @Override
        protected ExceptionHandler warn() {
            return Jvm.warn();
        }
    }

    static class MyReferenceCounted extends AbstractReferenceCounted {
        int performRelease;

        public MyReferenceCounted() {
        }

        @Override
        protected void performRelease() {
            performRelease++;
        }

        @NotNull
        @Override
        protected ExceptionHandler warn() {
            return Jvm.warn();
        }
    }
}