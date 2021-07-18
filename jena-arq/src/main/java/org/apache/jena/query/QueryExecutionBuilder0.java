/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.jena.query;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

import org.apache.jena.atlas.logging.Log;
import org.apache.jena.graph.Node;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.sparql.core.DatasetGraph;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.engine.QueryEngineFactory;
import org.apache.jena.sparql.engine.QueryEngineRegistry;
import org.apache.jena.sparql.engine.QueryExecutionBase;
import org.apache.jena.sparql.engine.binding.Binding;
import org.apache.jena.sparql.syntax.syntaxtransform.QueryTransformOps;
import org.apache.jena.sparql.util.Context;

/**
 * Query Execution for local datasets - builder style.
 */
public class QueryExecutionBuilder0 {

    // OLD - pre QueryExec.

    /** Create a new builder of {@link QueryExecution} for a local dataset. */
    public static QueryExecutionBuilder0 newBuilder(int DUMMY) {
        QueryExecutionBuilder0 builder = new QueryExecutionBuilder0();
        return builder;
    }

    private DatasetGraph dataset            = null;
    private Query        query              = null;
    private Context      context            = null;
    private Binding      initialBinding     = null;
    private long         timeout1           = -1;
    private TimeUnit     timeoutTimeUnit1   = null;
    private long         timeout2           = -1;
    private TimeUnit     timeoutTimeUnit2   = null;

    private QueryExecutionBuilder0() {}

    public QueryExecutionBuilder0 query(Query query) {
        this.query = query;
        return this;
    }

    public QueryExecutionBuilder0 query(String queryString) {
        query(queryString, Syntax.syntaxARQ);
        return this;
    }

    public QueryExecutionBuilder0 query(String queryString, Syntax syntax) {
        this.query = QueryFactory.create(queryString, syntax);
        return this;
    }

    public QueryExecutionBuilder0 dataset(DatasetGraph dsg) {
        this.dataset = dsg;
        return this;
    }

    public QueryExecutionBuilder0 dataset(Dataset dataset) {
        this.dataset = dataset.asDatasetGraph();
        return this;
    }

    public QueryExecutionBuilder0 context(Context context) {
        if ( context == null )
            return this;
        ensureContext();
        this.context.putAll(context);
        return this;
    }

    private void ensureContext() {
        if ( context == null )
            context = new Context();
    }

    public QueryExecutionBuilder0 initialBinding(Binding binding) {
        this.initialBinding = binding;
        return this;
    }

    public QueryExecutionBuilder0 timeout(long value, TimeUnit timeUnit) {
        this.timeout1 = value;
        this.timeoutTimeUnit1 = timeUnit;
        this.timeout2 = value;
        this.timeoutTimeUnit2 = timeUnit;
        return this;
    }

    public QueryExecutionBuilder0 initialTimeout(long value, TimeUnit timeUnit) {
        this.timeout1 = value;
        this.timeoutTimeUnit1 = timeUnit;
        return this;
    }

    public QueryExecutionBuilder0 overallTimeout(long value, TimeUnit timeUnit) {
        this.timeout2 = value;
        this.timeoutTimeUnit2 = timeUnit;
        return this;
    }

    public QueryExecution build() {
        Objects.requireNonNull(query, "Query for QueryExecution");

        query.setResultVars();
        Context cxt;

        if ( context == null ) {
            // Default is to take the global context, the copy it and merge in the dataset context.
            // If a context is specified by context(Context), use that as given.
            // The query context is modified to insert the current time.
            cxt = ARQ.getContext();
            cxt = Context.setupContextForDataset(cxt, dataset) ;
        } else {
            // Isolate to snapshot it and to allow it to be  modified.
            cxt = context.copy();
        }

        QueryEngineFactory f = QueryEngineRegistry.get().find(query, dataset, cxt);
        if ( f == null ) {
            Log.warn(QueryExecutionBuilder0.class, "Failed to find a QueryEngineFactory");
            return null;
        }

        Query queryActual = query;
        if ( initialBinding != null ) {
            Map<Var, Node> substitutions = bindingToMap(initialBinding);
            queryActual = QueryTransformOps.transform(query, substitutions);
        }

        // QueryExecutionBase set up the final context, merging in the dataset context and setting the current time.
        QueryExecution qExec = new QueryExecutionBase(queryActual, dataset, cxt, f);
        if ( false ) {
            if ( initialBinding != null )
                qExec.setInitialBinding(initialBinding);
        }
        if ( timeoutTimeUnit1 != null && timeout1 > 0 ) {
            if ( timeoutTimeUnit2 != null  && timeout2 > 0 )
                qExec.setTimeout(timeout1, timeoutTimeUnit1, timeout2, timeoutTimeUnit2);
            else
                qExec.setTimeout(timeout1, timeoutTimeUnit1);
        }
        return qExec;
    }

    // ==> BindingUtils
    /** Binding as a Map */
    public static Map<Var, Node> bindingToMap(Binding binding) {
        Map<Var, Node> substitutions = new HashMap<>();
        Iterator<Var> iter = binding.vars();
        while(iter.hasNext()) {
            Var v = iter.next();
            Node n = binding.get(v);
            substitutions.put(v, n);
        }
        return substitutions;
    }

    // (Slightly shorter) abbreviated forms - build-execute now.

    public void select(Consumer<QuerySolution> rowAction) {
        if ( !query.isSelectType() )
            throw new QueryExecException("Attempt to execute SELECT for a "+query.queryType()+" query");
        try ( QueryExecution qExec = build() ) {
            forEachRow(qExec.execSelect(), rowAction);
        }
    }

    // Also in RDFLink
    private static void forEachRow(ResultSet resultSet, Consumer<QuerySolution> rowAction) {
        while(resultSet.hasNext()) {
            rowAction.accept(resultSet.next());
        }
    }

    public Model construct() {
        if ( !query.isConstructType() )
            throw new QueryExecException("Attempt to execute CONSTRUCT for a "+query.queryType()+" query");
        try ( QueryExecution qExec = build() ) {
            return qExec.execConstruct();
        }
    }

    public Model describe() {
        if ( !query.isDescribeType() )
            throw new QueryExecException("Attempt to execute DESCRIBE for a "+query.queryType()+" query");
        try ( QueryExecution qExec = build() ) {
            return qExec.execDescribe();
        }
    }

    public boolean ask() {
        if ( !query.isAskType() )
            throw new QueryExecException("Attempt to execute ASK for a "+query.queryType()+" query");
        try ( QueryExecution qExec = build() ) {
            return qExec.execAsk();
        }
    }
}

