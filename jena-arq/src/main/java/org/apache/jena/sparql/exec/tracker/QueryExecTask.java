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

import org.apache.jena.query.Query;
import org.apache.jena.query.QueryType;
import org.apache.jena.sparql.exec.QueryExec;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Wraps a QueryExec and tracks its execution and any error in its machinery.
 * This includes obtained RowSets and Iterators.
 */
public class QueryExecTask
    extends QueryExecTaskBase<QueryExec> // <X>
    implements BasicTaskExec
{
    private static final Logger logger = LoggerFactory.getLogger(QueryExecTask.class);

    protected TaskListener<? super QueryExecTask> listener;
    protected long creationTime;
    protected long startTime = -1;
    protected long abortTime = -1;
    protected long finishTime = -1;
    protected TaskState currentState = TaskState.CREATED;

    /**
     * Note: The constructor does not notify the listener with the creation event.
     * This has to be done externally, such as using {@link #create(QueryExec, TaskListener)}.
     */
    protected QueryExecTask(QueryExec delegate, long creationTime, TaskListener<? super QueryExecTask> listener) {
        super(delegate, new ThrowableTrackerFirst());
        this.listener = listener;
        this.creationTime = creationTime;
    }

    /** Wrap a QueryExec and notify the listener with the creation event using the current time. */
    public static QueryExecTask create(QueryExec delegate, TaskListener<? super QueryExecTask> listener) {
        long creationTime = System.currentTimeMillis();
        return create(delegate, creationTime, listener);
    }

    /** Wrap a QueryExec and notify the listener with the creation event using the given time. */
    public static QueryExecTask create(QueryExec delegate, long creationTime, TaskListener<? super QueryExecTask> listener) {
        Objects.requireNonNull(delegate);
        Objects.requireNonNull(listener);
        QueryExecTask result = new QueryExecTask(delegate, creationTime, listener);
        listener.onStateChange(result);
        return result;
    }

    @Override
    public TaskState getTaskState() {
        return currentState;
    }

    @Override
    public long getCreationTime() {
        return creationTime;
    }

    @Override
    public long getAbortTime() {
        return abortTime;
    }

    @Override
    public long getStartTime() {
        return startTime;
    }

    @Override
    public long getFinishTime() {
        return finishTime;
    }

    @Override
    public Throwable getThrowable() {
        return getThrowableTracker().getFirstThrowable();
    }

    @Override
    public String getLabel() {
        Query query = getQuery();
        String result;
        if (query == null) {
            result = getQueryString();
            if (result == null) {
                result = "Unknown query";
            }
        } else {
            result = query.toString();
        }
        return result;
    }

    @Override
    public void abort() {
        if (!isAborting()) {
            this.abortTime = System.currentTimeMillis();
            super.abort();
        }
    }

    @Override
    public void beforeExec(QueryType queryType) {
        if (!TaskState.CREATED.equals(currentState)) {
            throw new IllegalStateException("Already started.");
        }

        startTime = System.currentTimeMillis();
        transition(TaskState.STARTING, () -> {});
        transition(TaskState.RUNNING, () -> super.beforeExec(queryType));
    }

    @Override
    public void afterExec() {
        try {
            transition(TaskState.TERMINATING, () -> {});
        } finally {
            try {
                super.afterExec();
            } finally {
                updateFinishTime();
                advertiseStateChange(TaskState.TERMINATED);
            }
        }
    }

    protected void updateFinishTime() {
        if (finishTime < 0) {
            finishTime = System.currentTimeMillis();
        }
    }

    /** Update state notifies all listeners of the change. */
    protected void advertiseStateChange(TaskState newState) {
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

    /**
     * Run the given action.
     *
     * On success, transitions to the specified target state.
     *
     * On failure, transitions to {@link TaskState#TERMINATING} and re-throws the encountered exception.
     * This should cause a subsequent call to close() which transitions to {@link TaskState#TERMINATED}.
     */
    protected void transition(TaskState targetState, Runnable action) {
        try {
            action.run();
            advertiseStateChange(targetState);
        } catch (Throwable throwable) {
            throwable.addSuppressed(new RuntimeException("Failure transitioning from " + currentState + " to " + targetState + ".", throwable));
            getThrowableTracker().report(throwable);
            advertiseStateChange(TaskState.TERMINATING);
            throw throwable;
        }
    }

    @Override
    public String getStatusMessage() {
        return "";
    }

    @Override
    public String toString() {
        return "QueryExecTask [startTime=" + getStartTime()
                + ", finishTime=" + getFinishTime() + ", getThrowable=" + getThrowable() + ", queryExecType=" + getQueryExecType()
                // Queries excluded because they make the string less readable.
                // + ", query=" + getQuery() + ", queryString=" + getQueryString()
                + ", delegate=" + getDelegate() + "]";
    }
}
