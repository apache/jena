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

package org.apache.jena.sparql.exec.tracker;

import java.util.Objects;

import org.apache.jena.sparql.exec.UpdateExec;
import org.apache.jena.update.UpdateRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** Wrapper for UpdateExec that tracks {@link BasicTaskInfo} information. */
public class UpdateExecTask
    extends UpdateProcessorWrapper<UpdateExec>
    implements UpdateExec, BasicTaskExec
{
    private static final Logger logger = LoggerFactory.getLogger(UpdateExecTask.class);

    protected TaskListener<? super UpdateExecTask> listener;

    protected long startTime = -1;
    protected long abortTime = -1;
    protected long finishTime = -1;
    protected Throwable throwable = null;
    protected TaskState currentState = TaskState.CREATED;

    protected UpdateExecTask(UpdateExec delegate, long creationTime, TaskListener<? super UpdateExecTask> listener) {
        super(delegate);
        this.listener = listener;
        this.startTime = creationTime;
    }

    /** Wrap a UpdateExec and notify the listener with the creation event using the current time. */
    public static UpdateExecTask create(UpdateExec delegate, TaskListener<? super UpdateExecTask> listener) {
        long creationTime = System.currentTimeMillis();
        return create(delegate, creationTime, listener);
    }

    /** Wrap a QueryExec and notify the listener with the creation event using the given time. */
    public static UpdateExecTask create(UpdateExec delegate, long creationTime, TaskListener<? super UpdateExecTask> listener) {
        Objects.requireNonNull(delegate);
        Objects.requireNonNull(listener);
        UpdateExecTask result = new UpdateExecTask(delegate, creationTime, listener);
        listener.onStateChange(result);
        return result;
    }

    @Override
    public TaskState getTaskState() {
        return currentState;
    }

    @Override
    public long getCreationTime() {
        return getCreationTime();
    }

    @Override
    public long getStartTime() {
        return startTime;
    }

    @Override
    public long getAbortTime() {
        return abortTime;
    }

    @Override
    public long getFinishTime() {
        return finishTime;
    }

    @Override
    public Throwable getThrowable() {
        return throwable;
    }

    @Override
    public String getLabel() {
        UpdateRequest updateRequest = getUpdateRequest();
        String result;
        if (updateRequest == null) {
            result = getUpdateRequestString();
            if (result == null) {
                result = "Unknown query";
            }
        } else {
            result = updateRequest.toString();
        }
        return result;
    }

    @Override
    public void abort() { // This method body is needed because of noop and delegating default methods.
        if (!isAborting()) {
            this.abortTime = System.currentTimeMillis();
            getDelegate().abort();
        }
    }

    protected void updateThrowable(Throwable throwable) {
        if (this.throwable == null) {
            this.throwable = throwable;
        }
    }

    @Override
    public void execute() {
        Throwable throwable = null;
        beforeExec();
        try {
            super.execute();
        } catch (Throwable t) {
            t.addSuppressed(new RuntimeException("Error during update execution"));
            throwable = t;
        } finally {
            afterExec(throwable);
        }
    }

    protected void beforeExec() {
        this.startTime = System.currentTimeMillis();
        updateState(TaskState.STARTING);
        transition(TaskState.RUNNING, () -> {});
    }

    protected void afterExec(Throwable throwable) {
        this.throwable = throwable;
        try {
            updateState(TaskState.TERMINATING);
        } finally {
            this.finishTime = System.currentTimeMillis();
            updateState(TaskState.TERMINATED);
        }
    }

    protected void updateState(TaskState newState) {
        Objects.requireNonNull(newState);
        if (currentState == null || newState.ordinal() > currentState.ordinal()) {
            // State oldState = currentState;
            currentState = newState;
            if (listener != null) {
                try {
                    listener.onStateChange(this);
                } catch (Throwable e) {
                    logger.warn("Exception raised in listener.", e);
                }
            }
        }
    }

    protected void transition(TaskState targetState, Runnable action) {
        try {
            action.run();
            updateState(targetState);
        } catch (Throwable throwable) {
            throwable.addSuppressed(new RuntimeException("Failure transitioning from " + currentState + " to " + targetState + ".", throwable));
            updateThrowable(throwable);
            updateState(TaskState.TERMINATING);
            throw throwable;
        }
    }

    @Override
    public String getStatusMessage() {
        return "";
    }

    @Override
    public String toString() {
        return "UpdateExecTracked [startTime=" + getStartTime()
                + ", finishTime=" + getFinishTime() + ", throwable=" + throwable
                + ", delegate=" + getDelegate() + "]";
    }
}
