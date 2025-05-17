package org.apache.jena.sparql.exec.tracker;

import org.apache.jena.sparql.exec.UpdateExec;

public interface UpdateExecWrapper<X extends UpdateExec>
    extends UpdateExec, UpdateProcessorWrapper<X>
{
}
