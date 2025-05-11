package org.apache.jena.sparql.exec;

import org.apache.jena.sparql.core.DatasetGraph;
import org.apache.jena.sparql.engine.Timeouts;
import org.apache.jena.sparql.engine.Timeouts.Timeout;
import org.apache.jena.sparql.engine.binding.Binding;
import org.apache.jena.sparql.engine.dispach.ChainingUpdateDispatcher;
import org.apache.jena.sparql.engine.dispach.UpdateDispatcher;
import org.apache.jena.sparql.modify.UpdateEngineFactory;
import org.apache.jena.sparql.modify.UpdateEngineRegistry;
import org.apache.jena.sparql.util.Context;
import org.apache.jena.update.UpdateException;
import org.apache.jena.update.UpdateFactory;
import org.apache.jena.update.UpdateRequest;

public class ChainingUpdateDispatcherMain
    implements ChainingUpdateDispatcher
{
    @Override
    public UpdateExec create(String updateRequestString, DatasetGraph dsg, Binding initialBinding, Context context, UpdateDispatcher chain) {
        UpdateRequest updateRequest = UpdateFactory.create(updateRequestString);
        return create(updateRequest, dsg, initialBinding, context, chain);
    }

    @Override
    public UpdateExec create(UpdateRequest updateRequest, DatasetGraph dataset, Binding initialBinding, Context cxt, UpdateDispatcher chain) {
        UpdateRequest actualUpdate = updateRequest;

        UpdateEngineFactory f = UpdateEngineRegistry.get().find(dataset, cxt);
        if ( f == null )
            throw new UpdateException("Failed to find an UpdateEngine");

        Timeout timeout = Timeouts.extractUpdateTimeout(cxt);
        UpdateExec uExec = new UpdateExecDataset(actualUpdate, dataset, initialBinding, cxt, f, timeout);
        return uExec;
    }
}
