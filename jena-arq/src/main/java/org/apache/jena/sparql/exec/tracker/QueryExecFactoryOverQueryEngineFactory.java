package org.apache.jena.sparql.exec.tracker;

import org.apache.jena.query.Query;
import org.apache.jena.sparql.algebra.Op;
import org.apache.jena.sparql.core.DatasetGraph;
import org.apache.jena.sparql.engine.QueryEngineFactory;
import org.apache.jena.sparql.engine.QueryEngineRegistry;
import org.apache.jena.sparql.engine.binding.Binding;
import org.apache.jena.sparql.exec.QueryExec;
import org.apache.jena.sparql.util.Context;

public class QueryExecFactoryOverQueryEngineFactory
    implements QueryExecFactory
{
    @Override
    public boolean accept(Query query, DatasetGraph dataset, Context context) {
        QueryExec.dataset(dataset).query(query).context(context).build();
        QueryEngineRegistry registry = QueryEngineRegistry.chooseRegistry(context);
        QueryEngineFactory f = registry.find(query, dataset, context);
        return f.accept(query, dataset, context);
    }

    @Override
    public QueryExec create(Query query, DatasetGraph dataset, Binding inputBinding, Context context) {
        // TODO Auto-generated method stub
        return null;
    }

//    @Override
//    public boolean accept(Op op, DatasetGraph dataset, Context context) {
//        // TODO Auto-generated method stub
//        return false;
//    }
//
//    @Override
//    public QueryExec create(Op op, DatasetGraph dataset, Binding inputBinding, Context context) {
//        // TODO Auto-generated method stub
//        return null;
//    }
}
