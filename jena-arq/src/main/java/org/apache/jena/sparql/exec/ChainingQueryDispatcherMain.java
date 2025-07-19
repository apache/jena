package org.apache.jena.sparql.exec;

import org.apache.jena.atlas.logging.Log;
import org.apache.jena.query.Query;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.query.Syntax;
import org.apache.jena.sparql.core.DatasetGraph;
import org.apache.jena.sparql.engine.QueryEngineFactory;
import org.apache.jena.sparql.engine.QueryEngineRegistry;
import org.apache.jena.sparql.engine.Timeouts;
import org.apache.jena.sparql.engine.Timeouts.Timeout;
import org.apache.jena.sparql.engine.Timeouts.TimeoutBuilderImpl;
import org.apache.jena.sparql.engine.binding.Binding;
import org.apache.jena.sparql.engine.dispach.ChainingQueryDispatcher;
import org.apache.jena.sparql.engine.dispach.QueryDispatcher;
import org.apache.jena.sparql.util.Context;

public class ChainingQueryDispatcherMain
    implements ChainingQueryDispatcher
{
    @Override
    public QueryExec create(String queryString, Syntax syntax, DatasetGraph dsg, Binding initialBinding, Context context, QueryDispatcher chain) {
        Query query = QueryFactory.create(queryString, syntax);
        return create(query, dsg, initialBinding, context, chain);
    }

    @Override
    public QueryExec create(Query queryActual, DatasetGraph dataset, Binding initialBinding, Context cxt, QueryDispatcher chain) {
        QueryEngineFactory qeFactory = QueryEngineRegistry.findFactory(queryActual, dataset, cxt);
        if ( qeFactory == null ) {
            Log.warn(QueryExecDatasetBuilder.class, "Failed to find a QueryEngineFactory");
            return null;
        }

        TimeoutBuilderImpl timeoutBuilder = new TimeoutBuilderImpl();
        Timeouts.applyDefaultQueryTimeoutFromContext(timeoutBuilder, cxt);
        Timeout timeout = timeoutBuilder.build();

        String queryStringActual = null;
        QueryExec qExec = new QueryExecDataset(queryActual, queryStringActual, dataset, cxt, qeFactory,
                                               timeout, initialBinding);
        return qExec;
    }
}
