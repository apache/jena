package org.apache.jena.sparql.exec.tracker;

import org.apache.jena.sparql.exec.UpdateExec;

public class UpdateExecWrapperBase<X extends UpdateExec>
    implements UpdateExecWrapper<X>
{
    protected X delegate;

    public UpdateExecWrapperBase(X delegate) {
        super();
        this.delegate = delegate;
    }

    @Override
    public X getDelegate() {
        return delegate;
    }
}
