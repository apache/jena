package org.apache.jena.sparql.exec.tracker;

public interface BasicTaskInfo {
    public static enum State {
        CREATED,
        STARTING, // start() called but not completed yet.
        RUNNING,
        TERMINATING, // in close() but not completed
        TERMINATED,
        FAILED
    }

    State getState();

    long getCreationTime();
    long getStartTime();
    long getFinishTime();

    /**
     * Return a description suitable for presentation to users.
     * This might be a less technical description than {@link #toString()}.
     */
    String getDescription();
    Throwable getThrowable();
}
