package org.apache.jena.sparql.exec.tracker;

import org.apache.jena.sparql.core.DatasetGraph;
import org.apache.jena.sparql.engine.binding.Binding;
import org.apache.jena.sparql.engine.dispach.ChainingUpdateDispatcher;
import org.apache.jena.sparql.engine.dispach.UpdateDispatcher;
import org.apache.jena.sparql.exec.UpdateExec;
import org.apache.jena.sparql.util.Context;
import org.apache.jena.update.UpdateRequest;

public class ChainingUpdateDispatcherExecTracker
    implements ChainingUpdateDispatcher
{
    @Override
    public UpdateExec create(String updateRequestString, DatasetGraph dsg, Binding initialBinding, Context context,
            UpdateDispatcher chain) {
        UpdateExec delegate = chain.create(updateRequestString, dsg, initialBinding, context);
        UpdateExec result = TaskTrackerRegistry.track(context, delegate);
        TaskTrackerRegistry.remove(context);
        return result;
    }

    @Override
    public UpdateExec create(UpdateRequest updateRequest, DatasetGraph dsg, Binding initialBinding, Context context,
            UpdateDispatcher chain) {
        UpdateExec delegate = chain.create(updateRequest, dsg, initialBinding, context);
        UpdateExec result = TaskTrackerRegistry.track(context, delegate);
        TaskTrackerRegistry.remove(context);
        return result;
    }
}
