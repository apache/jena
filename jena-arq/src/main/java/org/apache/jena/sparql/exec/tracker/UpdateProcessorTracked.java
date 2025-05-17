package org.apache.jena.sparql.exec.tracker;

import org.apache.jena.update.UpdateProcessor;

public class UpdateProcessorTracked<X extends UpdateProcessor>
    extends UpdateProcessorWrapperBase<X>
{
    // protected UpdateExecListener listener;

    public UpdateProcessorTracked(X delegate) {
        super(delegate);
        // this.listener = Objects.requireNonNull(listener);
    }
}
