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

import java.util.Objects;
import java.util.concurrent.CancellationException;
import java.util.concurrent.atomic.AtomicBoolean;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Thread base class that provides a {@link #abort()} method that can run a custom action
 * (in addition to setting the interrupted flag) as well as a doAfterRun() method
 * for cleaning up after execution (only gets called if there is a prior call to run).
 */
public abstract class TaskThread
    extends Thread
    implements BasicTask
{
    private static final Logger logger = LoggerFactory.getLogger(TaskThread.class);

    private final AtomicBoolean requestingCancel;
    private Object cancelLock = new Object();

    private TaskState state = TaskState.CREATED;
    private TaskListener<BasicTask> taskListener;

    // protected List<BiConsumer<? super T, Throwable>> completionHandlers = new ArrayList<>();

    private boolean isComplete = false;
    private Throwable throwable;

    private String label;
    private String statusMessage;

    /**
     * Time of the first abort request; null if there is none.
     * Cancel time will not be set if cancel is called after a task's completion.
     */
    private long creationTime = -1;
    private long startTime = -1; // When when run() is called.
    private long cancelTime = -1;
    private long endTime = -1;   // Time when runActual returns - but before afterRun() is called.

    // XXX Add native support for CompletableFuture?

    private void updateState(TaskState state) {
        synchronized (cancelLock) {
            this.state = state;
            if (taskListener != null) {
                try {
                    taskListener.onStateChange(this);
                } catch (Throwable t) {
                    logger.warn("Listener raised an exception.", t);
                }
            }
        }
    }

    public TaskThread(String label, TaskListener<BasicTask> taskListener) {
        this(label, taskListener, new AtomicBoolean());
    }

    public TaskThread(String label, TaskListener<BasicTask> taskListener, AtomicBoolean requestingCancel) {
        super();
        this.label = label;
        this.taskListener = taskListener;
        this.requestingCancel = Objects.requireNonNull(requestingCancel);

        this.creationTime = System.currentTimeMillis();
        updateState(TaskState.CREATED);
    }

    @Override
    public String getLabel() {
        return label;
    }

    @Override
    public TaskState getTaskState() {
        return state;
    }

    @Override
    public Throwable getThrowable() {
        return throwable;
    }

    @Override
    public long getCreationTime() {
        return creationTime;
    }

    @Override
    public long getStartTime() {
        return startTime;
    }

    @Override
    public long getEndTime() {
        return endTime;
    }

    @Override
    public long getAbortTime() {
        return cancelTime;
    }

    protected void setStatusMessage(String statusMessage) {
        this.statusMessage = statusMessage;
    }

    @Override
    public String getStatusMessage() {
        return statusMessage;
    }

    protected void beforeRun() {
    }

    @Override
    public final void run() {
        synchronized (cancelLock) {
            if (!TaskState.CREATED.equals(state)) {
                // Task may have already been started or aborted.
                throw new IllegalStateException("Can only start tasks in CREATED state, but this task is in state: " + state);
            }

            startTime = System.currentTimeMillis();
            updateState(TaskState.STARTING);
        }

        runInternal();
    }

    public final void runInternal() {
        try {
            checkCancelled();
            beforeRun();

            checkCancelled();
            runActual();
        } catch (Throwable t) {
            throwable = t;
            updateState(TaskState.TERMINATING);
            throw new RuntimeException(t);
        } finally {
            endTime = System.currentTimeMillis();
            isComplete = true;
            try {
                doAfterRun();
            } finally {
                updateState(TaskState.TERMINATED);
            }
        }
    }

    /** The actual "run" method that must be implemented. */
    public abstract void runActual() throws Exception;

    protected void requestCancel() {
        // If the task has not been started, then directly transition to terminated with a cancellation exception.
        // Otherwise transition to aborting state.
        if (TaskState.CREATED.equals(state)) {
            this.throwable = new CancellationException();
            updateState(TaskState.TERMINATED);
        } else {
            updateState(TaskState.ABORTING);
            this.interrupt();
        }
    }

    protected void doAfterRun() {
    }

    /** Raises a {@link CancellationException} if abort was called. */
    protected void checkCancelled() {
        if (isAborting()) {
            throw new CancellationException();
        }
    }

    @Override
    public void abort() {
        synchronized (cancelLock) {
            if ( ! requestingCancel.get() ) {
                requestingCancel.set(true);
                cancelTime = System.currentTimeMillis();
                if (!isComplete) {
                    this.requestCancel();
                }
            }
        }
    }

    @Override
    public boolean isAborting() {
        return requestingCancel.get();
    }
}
