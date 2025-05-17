package org.apache.jena.sparql.exec.tracker;

public interface TaskListenerByState<T extends BasicTaskExec>
    extends TaskListener<T>
{
    @Override
    public default void onStateChange(T task) {
        switch (task.getState()) {
        case CREATED: onCreated(task); break;
        case TERMINATED: onTerminated(task); break;
        default:
            // Log warning?
            break;
        }
    }

    public void onCreated(T task);
    public void onTerminated(T task);
}
