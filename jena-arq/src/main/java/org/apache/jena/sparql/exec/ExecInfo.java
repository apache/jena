package org.apache.jena.sparql.exec;

public interface ExecInfo {
    long getExecStartTime();
    long getExecFinishTime();
    Throwable getThrowable();
}
