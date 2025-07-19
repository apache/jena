package org.apache.jena.sparql.exec.tracker;

import org.apache.jena.query.Query;
import org.apache.jena.query.Syntax;
import org.apache.jena.sparql.core.DatasetGraph;
import org.apache.jena.sparql.engine.binding.Binding;
import org.apache.jena.sparql.engine.dispach.ChainingQueryDispatcher;
import org.apache.jena.sparql.engine.dispach.QueryDispatcher;
import org.apache.jena.sparql.exec.QueryExec;
import org.apache.jena.sparql.util.Context;

public class ChainingQueryDispatcherExecTracker
    implements ChainingQueryDispatcher
{
    @Override
    public QueryExec create(Query query, DatasetGraph dsg, Binding initialBinding, Context context,
            QueryDispatcher chain) {
        QueryExec delegate = chain.create(query, dsg, initialBinding, context);
        QueryExec result = TaskTrackerRegistry.track(context, delegate);
        TaskTrackerRegistry.remove(context);
        return result;
    }

    @Override
    public QueryExec create(String queryString, Syntax syntax, DatasetGraph dsg, Binding initialBinding,
            Context context, QueryDispatcher chain) {
        QueryExec delegate = chain.create(queryString, syntax, dsg, initialBinding, context);
        QueryExec result = TaskTrackerRegistry.track(context, delegate);
        TaskTrackerRegistry.remove(context);
        return result;
    }
}
