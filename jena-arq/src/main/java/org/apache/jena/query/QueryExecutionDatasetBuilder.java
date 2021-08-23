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

import org.apache.jena.graph.Node;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.sparql.core.DatasetGraph;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.engine.binding.Binding;
import org.apache.jena.sparql.engine.binding.BindingLib;
import org.apache.jena.sparql.exec.QueryExec;
import org.apache.jena.sparql.exec.QueryExecDatasetBuilder;
import org.apache.jena.sparql.exec.QueryExecutionCompat;
import org.apache.jena.sparql.util.Context;
import org.apache.jena.sparql.util.Symbol;

/**
 * Query Execution for local datasets - builder style.
 */
public class QueryExecutionDatasetBuilder implements QueryExecutionBuilder {
    // implements but overrides everything to get the right return type.

    /** Create a new builder of {@link QueryExecution} for a local dataset. */
    public static QueryExecutionDatasetBuilder create() { return new QueryExecutionDatasetBuilder(); }

    private final QueryExecDatasetBuilder builder;
    private Dataset dataset = null;

    public QueryExecutionDatasetBuilder() {
        builder = QueryExecDatasetBuilder.create();
    }

    @Override
    public QueryExecutionDatasetBuilder query(Query query) {
        builder.query(query);
        return this;
    }

    @Override
    public QueryExecutionDatasetBuilder query(String queryString) {
        builder.query(queryString);
        return this;
    }

    @Override
    public QueryExecutionDatasetBuilder query(String queryString, Syntax syntax) {
        builder.query(queryString, syntax);
        return this;
    }

    /** @deprecated Use {@link QueryExec#dataset} */
    @Deprecated
    public QueryExecutionDatasetBuilder dataset(DatasetGraph dsg) {
        this.dataset = DatasetFactory.wrap(dsg);
        builder.dataset(dsg);
        return this;
    }

    public QueryExecutionDatasetBuilder dataset(Dataset dataset) {
        this.dataset = dataset;
        builder.dataset(dataset.asDatasetGraph());
        return this;
    }

    public QueryExecutionDatasetBuilder model(Model model) {
        Dataset ds = DatasetFactory.create(model);
        dataset(ds);
        return this;
    }

    @Override
    public QueryExecutionDatasetBuilder set(Symbol symbol, Object value) {
        builder.set(symbol, value);
        return this;
    }

    @Override
    public QueryExecutionDatasetBuilder set(Symbol symbol, boolean value) {
        builder.set(symbol, value);
        return this;
    }

    @Override
    public QueryExecutionDatasetBuilder context(Context context) {
        builder.context(context);
        return this;
    }

    /** Prefer {@link #substitution(Binding)} which substitutes variables for values in the the query before execution. */
    public QueryExecutionDatasetBuilder initialBinding(Binding binding) {
        builder.initialBinding(binding);
        return this;
    }

    /**
     * Prefer {@link #substitution(QuerySolution)} which substitutes variables for values in the the query before execution.
     */
    public QueryExecutionDatasetBuilder initialBinding(QuerySolution querySolution) {
        if ( querySolution != null ) {
            Binding binding = BindingLib.toBinding(querySolution);
            initialBinding(binding);
        }
        return this;
    }

    @Override
    public QueryExecutionDatasetBuilder substitution(QuerySolution querySolution) {
        if ( querySolution != null ) {
            Binding binding = BindingLib.toBinding(querySolution);
            builder.substitution(binding);
        }
        return this;
    }

    public QueryExecutionDatasetBuilder substitution(Binding binding) {
        builder.substitution(binding);
        return this;
    }

    @Override
    public QueryExecutionDatasetBuilder substitution(String varName, RDFNode value) {
        Var var = Var.alloc(varName);
        Node val = value.asNode();
        builder.substitution(var, val);
        return this;
    }

    @Override
    public QueryExecutionDatasetBuilder timeout(long value, TimeUnit timeUnit) {
        builder.timeout(value, timeUnit);
        return this;
    }

    /** The time-to-first result timeout. */
    public QueryExecutionDatasetBuilder initialTimeout(long value, TimeUnit timeUnit) {
        builder.initialTimeout(value, timeUnit);
        return this;
    }

    /** The overall, start-to-finish timeout, to go with an initial timeout. */
    public QueryExecutionDatasetBuilder overallTimeout(long value, TimeUnit timeUnit) {
        builder.overallTimeout(value, timeUnit);
        return this;
    }

    @Override
    public QueryExecution build() {
        // QueryExecutionCompat delays creating the execution (builder.build) until
        // it is required so that setters in QueryExecution
        // (setInitialBinding/setTimeout*) act on the QueryExec builder.
        return QueryExecutionCompat.compatibility(builder, dataset, builder.getQuery(), builder.getQueryString());
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
}
