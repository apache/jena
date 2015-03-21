package com.hp.hpl.jena.sparql.lang.sparql_11_plus;

import org.apache.jena.atlas.logging.Log;

import com.hp.hpl.jena.query.ARQ;
import com.hp.hpl.jena.query.Dataset;
import com.hp.hpl.jena.query.DatasetFactory;
import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.sparql.core.DatasetGraph;
import com.hp.hpl.jena.sparql.engine.QueryEngineFactory;
import com.hp.hpl.jena.sparql.engine.QueryEngineRegistry;
import com.hp.hpl.jena.sparql.engine.QueryExecutionBase;
import com.hp.hpl.jena.sparql.util.Context;

public class QueryExecutionPlusFactory {
    static public QueryExecutionPlus create(Query query, Model model)
    {
        checkArg(query) ;
        checkArg(model) ;
        return make(query, DatasetFactory.create(model) ,null) ;
    }
    
    protected static QueryExecutionPlus make(Query query, Dataset dataset, Context context)
    {
        query.setResultVars() ;
        if ( context == null )
            context = ARQ.getContext();  // .copy done in QueryExecutionBase -> Context.setupContext. 
        DatasetGraph dsg = null ;
        if ( dataset != null )
            dsg = dataset.asDatasetGraph() ;
        QueryEngineFactory f = findFactory(query, dsg, context);
        if ( f == null )
        {
            Log.warn(QueryExecutionFactory.class, "Failed to find a QueryEngineFactory for query: "+query) ;
            return null ;
        }
        return new QueryExecutionPlusBase(query, dataset, context, f) ;
    }
    
    static private QueryEngineFactory findFactory(Query query, DatasetGraph dataset, Context context)
    {
        return QueryEngineRegistry.get().find(query, dataset, context);
    }
    
    static private void checkNotNull(Object obj, String msg)
    {
        if ( obj == null )
            throw new IllegalArgumentException(msg) ;
    }
    
    static private void checkArg(Model model)
    { checkNotNull(model, "Model is a null pointer") ; }

//    static private void checkArg(Dataset dataset)
//    { checkNotNull(dataset, "Dataset is a null pointer") ; }

    static private void checkArg(String queryStr)
    { checkNotNull(queryStr, "Query string is null") ; }

    static private void checkArg(Query query)
    { checkNotNull(query, "Query is null") ; }
}
