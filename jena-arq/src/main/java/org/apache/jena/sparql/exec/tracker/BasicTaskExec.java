package org.apache.jena.sparql.exec.tracker;

import org.apache.jena.sparql.engine.iterator.Abortable;

/** Interface to a task that can execute once. Not a periodic one. */
public interface BasicTaskExec
    extends BasicTaskInfo, Abortable // FIXME Abortable is in iterator package - not ideal.
{
    // Alternative design: Expose a runnable for an abort action.
    // Would remove the need for a separate isAbortSupported method.
    // Optional<Runnable> getAbortAction();
}
