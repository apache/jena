package org.apache.jena.sparql.exec.tracker;

import org.apache.jena.sparql.exec.QueryExec;
import org.apache.jena.sparql.exec.UpdateExec;

public interface ExecListener<T> {
    void onQueryExecStart(StartRecord<QueryExec> startRecord);
    void onQueryExecFinish(FinishRecord<QueryExec> endRecord);
}
