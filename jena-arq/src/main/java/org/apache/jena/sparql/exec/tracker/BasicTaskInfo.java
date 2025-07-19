package org.apache.jena.sparql.exec.tracker;

public interface BasicTaskInfo {
    public static enum State {
        /** Task object has been created. */
        CREATED,

        /** A method semantically akin to beforeRun() or init() has been called. */
        STARTING,

        /** A method semantically akin to run() has been called. This implies that the starting method has completed. */
        RUNNING,

        /** A method semantically akin to afterRun() or close() has been called but not completed yet. */
        TERMINATING,

        /** A method semantically akin to afterRun() or close() has completed. */
        TERMINATED,

        // FIXME Failed should be removed - it is TERMINATED with a non-null throwable.
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
