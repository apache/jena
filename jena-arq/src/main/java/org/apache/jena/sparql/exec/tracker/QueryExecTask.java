package org.apache.jena.sparql.exec.tracker;

import java.util.Objects;

import org.apache.jena.query.Query;
import org.apache.jena.query.QueryType;
import org.apache.jena.sparql.exec.QueryExec;

/**
 * Wraps a QueryExec and tracks its execution and any error in its machinery.
 * This includes obtained RowSets and Iterators.
 */
public class QueryExecTask
    extends QueryExecTaskBase<QueryExec> // <X>
    implements BasicTaskExec
{
    protected TaskListener<? super QueryExecTask> listener;
    protected long creationTime;
    protected long startTime = -1;
    protected long finishTime = -1;
    protected State currentState = State.CREATED;

    public QueryExecTask(QueryExec delegate, TaskListener<? super QueryExecTask> listener) {
        super(delegate, new ThrowableTrackerFirst());
        this.listener = listener;
        this.creationTime = System.currentTimeMillis();
    }

    @Override
    public State getState() {
        return currentState;
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
    public long getFinishTime() {
        return finishTime;
    }

    @Override
    public Throwable getThrowable() {
        return throwableTracker.getFirstThrowable();
    }

    @Override
    public String getDescription() {
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
    public void beforeExec(QueryType queryType) {
        if (!State.CREATED.equals(currentState)) {
            throw new IllegalStateException("Already started.");
        }

        startTime = System.currentTimeMillis();
        updateState(State.STARTING);
        transition(State.RUNNING, () -> super.beforeExec(queryType));
    }

    @Override
    public void afterExec() {
        updateState(State.TERMINATING);
        // Only set finishTime for State.TERMINATED.
        updateFinishTime();
        transition(State.TERMINATED, () -> super.afterExec());
    }

    protected void updateFinishTime() {
        if (finishTime < 0) {
            finishTime = System.currentTimeMillis();
        }
    }

    protected void updateState(State newState) {
        Objects.requireNonNull(newState);
        if (currentState == null || newState.ordinal() > currentState.ordinal()) {
            // State oldState = currentState;
            currentState = newState;
            if (listener != null) {
                listener.onStateChange(this);
            }
        }
    }

    protected void transition(State targetState, Runnable action) {
        try {
            action.run();
            updateState(targetState);
        } catch (Throwable throwable) {
            throwable.addSuppressed(new RuntimeException("Failure transitioning from " + currentState + " to " + targetState + ".", throwable));
            throwableTracker.report(throwable);
            updateFinishTime();
            updateState(State.FAILED);
            throw throwable;
        }
    }

    @Override
    public String toString() {
        return "QueryExecTracked [startTime=" + getStartTime()
                + ", finishTime=" + getFinishTime() + ", getThrowable=" + getThrowable() + ", queryExecType=" + getQueryExecType()
                // + ", getQuery()=" + getQuery() + ", getQueryString()=" + getQueryString()
                + ", delegate=" + getDelegate() + "]";
    }
}
