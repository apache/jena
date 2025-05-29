package org.apache.jena.sparql.exec.tracker;

import java.util.Objects;

import org.apache.jena.sparql.exec.UpdateExec;
import org.apache.jena.update.UpdateRequest;

public class UpdateExecTask
    extends UpdateProcessorTracked<UpdateExec>
    implements UpdateExec, BasicTaskExec
{
    protected TaskListener<? super UpdateExecTask> listener;

    protected long startTime = -1;
    protected long finishTime = -1;
    protected Throwable throwable = null;
    protected State currentState;
//    protected StartRecord<UpdateExec> startRecord = null;
//    protected FinishRecord<UpdateExec> finishRecord = null;

    public UpdateExecTask(UpdateExec delegate, TaskListener<? super UpdateExecTask> listener) {
        super(delegate);
        this.listener = listener;
        this.currentState = State.CREATED;
        this.startTime = System.currentTimeMillis();
    }

    @Override
    public State getState() {
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
    public long getFinishTime() {
        return finishTime;
    }

    @Override
    public Throwable getThrowable() {
        return throwable;
    }

    @Override
    public String getDescription() {
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
        getDelegate().abort();
    }

    protected void updateThrowable(Throwable throwable) {
        if (this.throwable == null) {
            this.throwable = throwable;
        }
    }

    @Override
    protected void beforeExec() {
        this.startTime = System.currentTimeMillis();
        updateState(State.STARTING);
        transition(State.RUNNING, () -> super.beforeExec());
    }

    @Override
    protected void afterExec(Throwable throwable) {
        this.throwable = throwable;
        this.finishTime = System.currentTimeMillis();
        updateState(State.TERMINATING);
        transition(State.TERMINATED, () -> super.afterExec(throwable));
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
            updateThrowable(throwable);
            updateState(State.FAILED);
            throw throwable;
        }
    }

    @Override
    public String toString() {
        return "UpdateExecTracked [startTime=" + getStartTime()
                + ", finishTime=" + getFinishTime() + ", getThrowable=" + getThrowable()
                // + ", getQuery()=" + getQuery() + ", getQueryString()=" + getQueryString()
                + ", delegate=" + getDelegate() + "]";
    }
}
