package org.apache.jena.sparql.exec.tracker;

public interface TaskListener<T extends BasicTaskExec> {
    void onStateChange(T task);
}
