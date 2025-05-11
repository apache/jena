package org.apache.jena.sparql.engine.dispach;

import org.apache.jena.query.Query;
import org.apache.jena.query.Syntax;
import org.apache.jena.sparql.core.DatasetGraph;
import org.apache.jena.sparql.engine.binding.Binding;
import org.apache.jena.sparql.exec.QueryExec;
import org.apache.jena.sparql.util.Context;

public interface QueryDispatcher {
    QueryExec create(String queryString, Syntax syntax, DatasetGraph dsg, Binding initialBinding, Context context);
    QueryExec create(Query query, DatasetGraph dsg, Binding initialBinding, Context context);
}
