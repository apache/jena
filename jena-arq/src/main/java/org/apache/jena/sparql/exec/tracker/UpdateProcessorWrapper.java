package org.apache.jena.sparql.exec.tracker;

import org.apache.jena.sparql.util.Context;
import org.apache.jena.update.UpdateProcessor;
import org.apache.jena.update.UpdateRequest;

public interface UpdateProcessorWrapper<T extends UpdateProcessor>
    extends UpdateProcessor
{
    T getDelegate();

    // XXX Expose the DatasetGraph if applicable?
    //default DatasetGraph getDatasetGraph() {
    //    return getDelegate().getDatasetGraph();
    //}

    @Override
    public default void abort() {
        getDelegate().abort();
    }

    @Override
    public default Context getContext() {
        return getDelegate().getContext();
    }

    @Override
    default void execute() {
        getDelegate().execute();
    }

    @Override
    default UpdateRequest getUpdateRequest() {
        return getDelegate().getUpdateRequest();
    }

    @Override
    default String getUpdateRequestString() {
        return getDelegate().getUpdateRequestString();
    }
}
