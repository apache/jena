package org.apache.jena.sparql.exec;

import java.time.Instant;
import java.util.function.LongSupplier;

import org.apache.jena.sparql.exec.tracker.FinishRecord;
import org.apache.jena.sparql.exec.tracker.StartRecord;

public class QueryExecBase<T> {
    private long                      queryStartTime   = -1; // Unset
    private StartRecord<QueryExecBase<T>>  startRecord = null;
    private FinishRecord<QueryExecBase<T>> finishRecord = null;
    private LongSupplier              idGenerator;
    private long id;


    void startExec() {
        // queryStartTime = System.currentTimeMillis();
        id = idGenerator.getAsLong();
        Instant instant = Instant.ofEpochMilli(queryStartTime);
        startRecord = new StartRecord<>(id, instant, this, null);
    }

    void endExec(Throwable throwable) {
        Instant instant = Instant.ofEpochMilli(queryStartTime);
        finishRecord = new FinishRecord<>(startRecord, instant, throwable);
    }
}
