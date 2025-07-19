package org.apache.jena.sparql.engine.dispach;

import java.util.List;

import org.apache.jena.query.Query;
import org.apache.jena.query.QueryException;
import org.apache.jena.query.Syntax;
import org.apache.jena.sparql.core.DatasetGraph;
import org.apache.jena.sparql.engine.binding.Binding;
import org.apache.jena.sparql.exec.QueryExec;
import org.apache.jena.sparql.util.Context;

/** Abstraction of a registry's single chain as a service executor */
public class QueryDispatcherOverRegistry
    implements QueryDispatcher
{
    protected QueryDispatcherRegistry registry;

    /** Position in the chain */
    protected int pos;

    public QueryDispatcherOverRegistry(QueryDispatcherRegistry registry) {
        this(registry, 0);
    }

    public QueryDispatcherOverRegistry(QueryDispatcherRegistry registry, int pos) {
        super();
        this.registry = registry;
        this.pos = pos;
    }

    protected ChainingQueryDispatcher getDispatcher() {
        List<ChainingQueryDispatcher> factories = registry.getFactories();
        int n = factories.size();
        if (pos >= n) {
            throw new QueryException("No more elements in query dispatcher chain (pos=" + pos + ", chain size=" + n + ")");
        }
        ChainingQueryDispatcher dispatcher = factories.get(pos);
        return dispatcher;
    }

    @Override
    public QueryExec create(Query query, DatasetGraph dsg, Binding initialBinding, Context context) {
        ChainingQueryDispatcher dispatcher = getDispatcher();
        QueryDispatcher next = new QueryDispatcherOverRegistry(registry, pos + 1);
        QueryExec result = dispatcher.create(query, dsg, initialBinding, context, next);
        return result;
    }

    @Override
    public QueryExec create(String queryString, Syntax syntax, DatasetGraph dsg, Binding initialBinding, Context context) {
        ChainingQueryDispatcher dispatcher = getDispatcher();
        QueryDispatcher next = new QueryDispatcherOverRegistry(registry, pos + 1);
        QueryExec result = dispatcher.create(queryString, syntax, dsg, initialBinding, context, next);
        return result;
    }
}
