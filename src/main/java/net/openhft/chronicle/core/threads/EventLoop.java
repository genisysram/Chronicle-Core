/*
 * Copyright 2016-2020 Chronicle Software
 *
 * https://chronicle.software
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package net.openhft.chronicle.core.threads;

import net.openhft.chronicle.core.Jvm;
import net.openhft.chronicle.core.io.Closeable;
import org.jetbrains.annotations.NotNull;

import java.util.function.BiFunction;

public interface EventLoop extends Closeable {
    boolean DEBUG_ADDING_HANDLERS = Jvm.getBoolean("debug.adding.handlers");

    String name();

    @Deprecated
    default void addHandler(boolean dontAttemptToRunImmediatelyInCurrentThread, @NotNull EventHandler handler) {
        throw new UnsupportedOperationException("dontAttemptToRunImmediatelyInCurrentThread is always true now");
    }

    /**
     * Add handler to event loop to be executed. Event loops should execute handlers in order of priority.
     * Handlers with same priority have no guarantee of execution order.
     *
     * @param handler handler
     * @return closeable to use for cleaning up.
     */
    Closeable addHandler(EventHandler handler);

    /**
     * Adds a handler to this EventLoop by first invoking the provided EventHandler
     * constructor and then taking the constructed instance and providing it to
     * the {@link #addHandler(EventHandler)} method.
     * <p>
     * The BiFunction's Thread is the event thread for this EventLoop
     * The BiFunction's EventLoop is the same parameter later provided to EventHandler::eventLoop
     * <p>
     * This method improves the ability to write immutable or partly immutable EventHandler classes
     * compared with the {@link #addHandler(EventHandler)} method.
     *
     * @param constructor to invoke when creating an EventHandler
     * @param priority for the EventHandler that must match the created EventHandler's priority
     * @return closeable to use for cleaning up.
     * @throws IllegalStateException if the provided {@code priority} does not match the
     * constructed EventHandler's priority.
     */
    Closeable addHandler(@NotNull BiFunction<Thread, EventLoop, EventHandler> constructor, @NotNull HandlerPriority priority);

    void start();

    void unpause();

    void stop();

    /**
     * @return {@code true} close has been called
     */
    @Override
    boolean isClosed();

    /**
     * @return {@code true} if the main thread is running
     */
    boolean isAlive();

    /**
     * Wait until the event loop has terminated (after close has been called)
     */
    void awaitTermination();
}
