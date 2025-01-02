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

package org.apache.jena.sparql.exec;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

import org.apache.jena.atlas.logging.Log;
import org.apache.jena.graph.Graph;
import org.apache.jena.graph.Node;
import org.apache.jena.query.*;
import org.apache.jena.sparql.ARQConstants;
import org.apache.jena.sparql.core.DatasetGraph;
import org.apache.jena.sparql.core.DatasetGraphFactory;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.engine.QueryEngineFactory;
import org.apache.jena.sparql.engine.QueryEngineRegistry;
import org.apache.jena.sparql.engine.binding.Binding;
import org.apache.jena.sparql.engine.Timeouts;
import org.apache.jena.sparql.engine.Timeouts.Timeout;
import org.apache.jena.sparql.engine.Timeouts.TimeoutBuilderImpl;
import org.apache.jena.sparql.syntax.syntaxtransform.QueryTransformOps;
import org.apache.jena.sparql.util.Context;
import org.apache.jena.sparql.util.ContextAccumulator;
import org.apache.jena.sparql.util.Symbol;
import org.apache.jena.sys.JenaSystem;

/**
 * Query execution for local datasets - builder style.
 */
public class QueryExecDatasetBuilder implements QueryExecMod, QueryExecBuilder {

    static { JenaSystem.init(); }

    /** Create a new builder of {@link QueryExec} for a local dataset. */
    public static QueryExecDatasetBuilder create() {
        QueryExecDatasetBuilder builder = new QueryExecDatasetBuilder();
        return builder;
    }

    private static final long UNSET         = -1;

    private DatasetGraph dataset            = null;
    private Query        query              = null;
    private String       queryString        = null;

    private ContextAccumulator contextAcc =
            ContextAccumulator.newBuilder(()->ARQ.getContext(), ()->Context.fromDataset(dataset));

    // Uses query rewrite to replace variables by values.
    private Map<Var, Node>  substitutionMap  = null;

    // Uses initial binding to execution (old, original) feature
    private Binding      initialBinding      = null;
    private TimeoutBuilderImpl timeoutBuilder  = new TimeoutBuilderImpl();

    private QueryExecDatasetBuilder() { }

    public Query getQuery()         { return query; }
    public String getQueryString()  { return queryString; }

    @Override
    public QueryExecDatasetBuilder query(Query query) {
        this.query = query;
        return this;
    }

    @Override
    public QueryExecDatasetBuilder query(String queryString) {
        query(queryString, Syntax.syntaxARQ);
        return this;
    }

    /** The parse-check flag has no effect for query execs over datasets. */
    @Override
    public QueryExecDatasetBuilder parseCheck(boolean parseCheck) {
        return this;
    }

    @Override
    public QueryExecDatasetBuilder query(String queryString, Syntax syntax) {
        this.queryString = queryString;
        this.query = QueryFactory.create(queryString, syntax);
        return this;
    }

    public QueryExecDatasetBuilder dataset(DatasetGraph dsg) {
        this.dataset = dsg;
        return this;
    }

    public QueryExecDatasetBuilder graph(Graph graph) {
        DatasetGraph dsg = DatasetGraphFactory.wrap(graph);
        dataset(dsg);
        return this;
    }

    @Override
    public QueryExecDatasetBuilder set(Symbol symbol, Object value) {
        contextAcc.set(symbol, value);
        return this;
    }

    @Override
    public QueryExecDatasetBuilder set(Symbol symbol, boolean value) {
        contextAcc.set(symbol, value);
        return this;
    }

    @Override
    public QueryExecDatasetBuilder context(Context cxt) {
        contextAcc.context(cxt);
        return this;
    }

    @Override
    public Context getContext() {
        return contextAcc.context();
    }

    @Override
    public QueryExecDatasetBuilder substitution(Binding binding) {
        ensureSubstitutionMap();
        binding.forEach(this.substitutionMap::put);
        return this;
    }

    @Override
    public QueryExecDatasetBuilder substitution(Var var, Node value) {
        ensureSubstitutionMap();
        this.substitutionMap.put(var, value);
        return this;
    }

    private void ensureSubstitutionMap() {
        if ( substitutionMap == null )
            substitutionMap = new HashMap<>();
    }

    /** @deprecated Use {@link #substitution(Binding)} */
    @Deprecated(forRemoval = true)
    public QueryExecDatasetBuilder initialBinding(Binding binding) {
        this.initialBinding = binding;
        return this;
    }

    @Override
    public QueryExecDatasetBuilder timeout(long value, TimeUnit timeUnit) {
        timeoutBuilder.timeout(value, timeUnit);
        return this;
    }

    @Override
    public QueryExecDatasetBuilder initialTimeout(long value, TimeUnit timeUnit) {
        timeoutBuilder.initialTimeout(value, timeUnit);
        return this;
    }

    @Override
    public QueryExecDatasetBuilder overallTimeout(long value, TimeUnit timeUnit) {
        timeoutBuilder.overallTimeout(value, timeUnit);
        return this;
    }

    @Override
    public QueryExec build() {
        Objects.requireNonNull(query, "No query for QueryExec");
        // Queries can have FROM/FROM NAMED or VALUES to get data.
        //Objects.requireNonNull(dataset, "No dataset for QueryExec");
        query.setResultVars();
        Context cxt = getContext();

        QueryEngineFactory qeFactory = QueryEngineRegistry.findFactory(query, dataset, cxt);
        if ( qeFactory == null ) {
            Log.warn(QueryExecDatasetBuilder.class, "Failed to find a QueryEngineFactory");
            return null;
        }

        // Initial bindings / parameterized query
        Query queryActual = query;
        String queryStringActual = queryString;

        if ( substitutionMap != null && ! substitutionMap.isEmpty() ) {
            queryActual = QueryTransformOps.transform(query, substitutionMap);
            queryStringActual = null;
        }

        Timeouts.applyDefaultQueryTimeoutFromContext(this.timeoutBuilder, cxt);

        if ( dataset != null )
            cxt.set(ARQConstants.sysCurrentDataset, DatasetFactory.wrap(dataset));
        if ( queryActual != null )
            cxt.set(ARQConstants.sysCurrentQuery, queryActual);

        Timeout timeout = timeoutBuilder.build();

        QueryExec qExec = new QueryExecDataset(queryActual, queryStringActual, dataset, cxt, qeFactory,
                                               timeout, initialBinding);
        return qExec;
    }
}
