/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.jena.geosparql.spatial.task;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.BiConsumer;

import org.apache.jena.geosparql.spatial.task.TaskControl.Registration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Thread base class that provides a {@link #cancel()} method that can run a custom action
 * (in addition to setting the interrupted flag) as well as a doAfterRun() method
 * for cleaning up after execution (only gets called if there is a prior call to run).
 */
public abstract class AbortableThread<T>
    extends Thread
{
    private static final Logger logger = LoggerFactory.getLogger(AbortableThread.class);

    private final AtomicBoolean requestingCancel;
    private volatile boolean cancelOnce = false;
    private Object cancelLock = new Object();

    protected List<BiConsumer<? super T, Throwable>> completionHandlers = new ArrayList<>();

    private boolean isComplete = false;
    private T value = null;
    private Throwable throwable;

    private Instant startTime;
    private String statusMessage;

    /**
     * Time of the first abort request; null if there is none.
     * Cancel time will not be set if cancel is called after a task's completion.
     */
    private Instant cancelTime;
    private Instant endTime;

    // XXX Add native support for CompletableFuture?

    public AbortableThread() {
        this(new AtomicBoolean());
    }

    public AbortableThread(AtomicBoolean requestingCancel) {
        super();
        this.requestingCancel = Objects.requireNonNull(requestingCancel);
    }

    public final boolean isComplete() {
        return isComplete;
    }

    public T getValue() {
        return value;
    }

    public Throwable getThrowable() {
        return throwable;
    }

    public Instant getStartTime() {
        return startTime;
    }

    public Instant getEndTime() {
        return endTime;
    }

    public Instant getCancelTime() {
        return cancelTime;
    }

    protected void setStatusMessage(String statusMessage) {
        this.statusMessage = statusMessage;
    }

    public String getStatusMessage() {
        return statusMessage;
    }

    @Override
    public final void run() {
        try {
            startTime = Instant.now();
            runInternal();
        } finally {
            synchronized (cancelLock) {
                fireEvents();
                // future.complete(null);
            }
        }
    }

    public final void runInternal() {
        try {
            runActual();
        } catch (Throwable t) {
            // t.addSuppressed(new RuntimeException("An error occurred."));
            throwable = t;
            throw new RuntimeException(t);
        } finally {
            endTime = Instant.now();
            isComplete = true;
            doAfterRun();
        }
    }

    public abstract void runActual() throws Exception;

    /** Returns true iff {@link #cancel()} was called. */
    public final boolean isCancelled() {
        return cancelOnce;
    }

    public final void cancel() {
        synchronized (cancelLock) {
            if ( ! cancelOnce && !isComplete ) {
                // Need to set the flags before allowing subclasses to handle requestCancel() in order
                // to prevent a race condition. We want to be sure that calls to have hasNext()/nextBinding()
                // will definitely throw a QueryCancelledException in this class and
                // not allow a situation in which a subclass component thinks it is cancelled,
                // while this class does not.
                if ( requestingCancel != null )
                    // Signalling from timeouts
                    requestingCancel.set(true);
                cancelOnce = true;
                cancelTime = Instant.now();
                this.requestCancel();
            }
        }
    }

    protected void requestCancel() {
        this.interrupt();
    }

    protected void doAfterRun() {
    }

    protected void fireEvents() {
        for (BiConsumer<? super T, Throwable> handler : completionHandlers) {
            try {
                handler.accept(null, throwable);
            } catch (Throwable t) {
                if (logger.isWarnEnabled()) {
                    logger.warn("A task completion handler raised an exception.", t);
                }
            }
        }
    }

    /**
     * Registered an action to run on completion of the thread. An action is run at most once.
     *
     * @param action The action is invoked with any thrown exception - null if there was none.
     * @return A registration that can be used to unregister the listener early.
     */
    protected Registration whenComplete(BiConsumer<? super T, Throwable> action) {
        Objects.requireNonNull(action);
        synchronized (cancelLock) {
            boolean isAdded = completionHandlers.add(action);
            if (isAdded) {
                // Immediately resolve if already complete.
                if (isComplete()) {
                    action.accept(value, throwable);
                }
            }
            return () -> {
                synchronized (cancelLock) {
                    completionHandlers.remove(action);
                }
            };
        }
    }
}
