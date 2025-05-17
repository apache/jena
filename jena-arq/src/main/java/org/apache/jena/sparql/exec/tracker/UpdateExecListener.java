package org.apache.jena.sparql.exec.tracker;

import org.apache.jena.sparql.exec.UpdateExec;

public interface UpdateExecListener<X extends UpdateExec> {
    void onUpdateExecStart(X updateExec);
    void onUpdateExecFinish(X updateExec);
}
