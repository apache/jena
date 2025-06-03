package org.apache.jena.sparql.exec.tracker;

import org.apache.jena.update.UpdateProcessor;

public class UpdateProcessorWrapperBase<T extends UpdateProcessor>
    implements UpdateProcessorWrapper<T>
{
    protected T delegate;

    public UpdateProcessorWrapperBase(T delegate) {
        super();
        this.delegate = delegate;
    }

    @Override
    public T getDelegate() {
        return delegate;
    }

    protected void beforeExec() {
    }

    protected void afterExec(Throwable throwable) {
    }

    @Override
    public void execute() {
        beforeExec();
        try {
            delegate.execute();
            afterExec(null);
        } catch(Throwable e) {
            e.addSuppressed(new RuntimeException("Update execution failed."));
            afterExec(e);
            throw e;
        }
    }
}
