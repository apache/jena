package org.apache.jena.sparql.engine.dispach;

import java.util.List;

import org.apache.jena.query.QueryException;
import org.apache.jena.sparql.core.DatasetGraph;
import org.apache.jena.sparql.engine.binding.Binding;
import org.apache.jena.sparql.exec.UpdateExec;
import org.apache.jena.sparql.util.Context;
import org.apache.jena.update.UpdateRequest;

/** Abstraction of a registry's single chain as a service executor */
public class UpdateDispatcherOverRegistry
    implements UpdateDispatcher
{
    protected UpdateDispatcherRegistry registry;

    /** Position in the chain */
    protected int pos;

    public UpdateDispatcherOverRegistry(UpdateDispatcherRegistry registry) {
        this(registry, 0);
    }

    public UpdateDispatcherOverRegistry(UpdateDispatcherRegistry registry, int pos) {
        super();
        this.registry = registry;
        this.pos = pos;
    }

    protected ChainingUpdateDispatcher getDispatcher() {
        List<ChainingUpdateDispatcher> factories = registry.getFactories();
        int n = factories.size();
        if (pos >= n) {
            throw new QueryException("No more elements in query dispatcher chain (pos=" + pos + ", chain size=" + n + ")");
        }
        ChainingUpdateDispatcher dispatcher = factories.get(pos);
        return dispatcher;
    }

    @Override
    public UpdateExec create(UpdateRequest updateRequest, DatasetGraph dsg, Binding initialBinding, Context context) {
        ChainingUpdateDispatcher dispatcher = getDispatcher();
        UpdateDispatcher next = new UpdateDispatcherOverRegistry(registry, pos + 1);
        UpdateExec result = dispatcher.create(updateRequest, dsg, initialBinding, context, next);
        return result;
    }

    @Override
    public UpdateExec create(String queryString, DatasetGraph dsg, Binding initialBinding, Context context) {
        ChainingUpdateDispatcher dispatcher = getDispatcher();
        UpdateDispatcher next = new UpdateDispatcherOverRegistry(registry, pos + 1);
        UpdateExec result = dispatcher.create(queryString, dsg, initialBinding, context, next);
        return result;
    }
}
