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
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

import org.apache.jena.graph.Graph;
import org.apache.jena.graph.Node;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.sparql.core.DatasetGraph;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.engine.binding.Binding;
import org.apache.jena.sparql.engine.binding.BindingLib;
import org.apache.jena.sparql.exec.QueryExec;
import org.apache.jena.sparql.exec.QueryExecBuilder;
import org.apache.jena.sparql.exec.QueryExecutionCompat;
import org.apache.jena.sparql.util.Context;
import org.apache.jena.sparql.util.Symbol;

/**
 * Query Execution for local datasets - builder style.
 */
public class QueryExecutionBuilder {

    /** Create a new builder of {@link QueryExecution} for a local dataset. */
    public static QueryExecutionBuilder newBuilder() { return new QueryExecutionBuilder(); }

    private final QueryExecBuilder builder;

    public QueryExecutionBuilder() {
        builder = QueryExec.newBuilder();
    }

    public QueryExecutionBuilder query(Query query) {
        builder.query(query);
        return this;
    }

    public QueryExecutionBuilder query(String queryString) {
        builder.query(queryString);
        return this;
    }

    public QueryExecutionBuilder query(String queryString, Syntax syntax) {
        builder.query(queryString, syntax);
        return this;
    }

    public QueryExecutionBuilder dataset(DatasetGraph dsg) {
        builder.dataset(dsg);
        return this;
    }

    public QueryExecutionBuilder dataset(Dataset dataset) {
        builder.dataset(dataset.asDatasetGraph());
        return this;
    }

    public QueryExecutionBuilder model(Model model) {
        Graph graph = model.getGraph();
        builder.graph(graph);
        return this;
    }

    public QueryExecutionBuilder set(Symbol symbol, Object value) {
        builder.set(symbol, value);
        return this;
    }

    public QueryExecutionBuilder set(Symbol symbol, boolean value) {
        builder.set(symbol, value);
        return this;
    }

    public QueryExecutionBuilder context(Context context) {
        builder.context(context);
        return this;
    }

    /** Prefer {@link #substitution(Binding)} which substitutes variables for values in the the query before execution. */
    public QueryExecutionBuilder initialBinding(Binding binding) {
        builder.initialBinding(binding);
        return this;
    }

    /** Prefer {@link #substitution(QuerySolution)} which substitutes variables for values in the the query before execution. */
    public QueryExecutionBuilder initialBinding(QuerySolution querySolution) {
        if ( querySolution != null ) {
            Binding binding = BindingLib.toBinding(querySolution);
            initialBinding(binding);
        }
        return this;
    }

    public QueryExecutionBuilder substitution(QuerySolution querySolution) {
        if ( querySolution != null ) {
            Binding binding = BindingLib.toBinding(querySolution);
            builder.substitution(binding);
        }
        return this;
    }

    public QueryExecutionBuilder substitution(Binding binding) {
        builder.substitution(binding);
        return this;
    }

    public QueryExecutionBuilder substitution(String varName, RDFNode value) {
        Var var = Var.alloc(varName);
        Node val = value.asNode();
        builder.substitution(var, val);
        return this;
    }

    public QueryExecutionBuilder timeout(long value, TimeUnit timeUnit) {
        builder.timeout(value, timeUnit);
        return this;
    }

    /** The time-to-first result timeout. */
    public QueryExecutionBuilder initialTimeout(long value, TimeUnit timeUnit) {
        builder.initialTimeout(value, timeUnit);
        return this;
    }

    /** The overall, start-to-finish timeout, to go with an initial timeout. */
    public QueryExecutionBuilder overallTimeout(long value, TimeUnit timeUnit) {
        builder.overallTimeout(value, timeUnit);
        return this;
    }

    public QueryExecution build() {
        // QueryExecutionCompat delays creating the execution (builder.build) until
        // it is required so that setters in QueryExecution
        // (setInitialBinding/setTimeout*) act on the QueryExec builder.
        return new QueryExecutionCompat(builder);
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
        try ( QueryExecution qExec = build() ) {
            Query query = qExec.getQuery();
            if ( !query.isSelectType() )
                throw new QueryExecException("Attempt to execute SELECT for a "+query.queryType()+" query");
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
        try ( QueryExecution qExec = build() ) {
            Query query = qExec.getQuery();
            if ( !query.isConstructType() )
                throw new QueryExecException("Attempt to execute CONSTRUCT for a "+query.queryType()+" query");
            return qExec.execConstruct();
        }
    }

    public Model describe() {
        try ( QueryExecution qExec = build() ) {
            Query query = qExec.getQuery();
            if ( !query.isDescribeType() )
                throw new QueryExecException("Attempt to execute DESCRIBE for a "+query.queryType()+" query");
            return qExec.execDescribe();
        }
    }

    public boolean ask() {
        try ( QueryExecution qExec = build() ) {
            Query query = qExec.getQuery();
            if ( !query.isAskType() )
                throw new QueryExecException("Attempt to execute ASK for a "+query.queryType()+" query");
            return qExec.execAsk();
        }
    }
}

