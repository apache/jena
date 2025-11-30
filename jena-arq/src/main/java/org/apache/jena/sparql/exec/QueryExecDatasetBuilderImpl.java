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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
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
import org.apache.jena.sparql.engine.Timeouts;
import org.apache.jena.sparql.engine.Timeouts.Timeout;
import org.apache.jena.sparql.engine.Timeouts.TimeoutBuilderImpl;
import org.apache.jena.sparql.engine.binding.Binding;
import org.apache.jena.sparql.exec.tracker.QueryExecTransform;
import org.apache.jena.sparql.syntax.syntaxtransform.QueryTransformOps;
import org.apache.jena.sparql.util.Context;
import org.apache.jena.sparql.util.ContextAccumulator;
import org.apache.jena.sparql.util.Symbol;
import org.apache.jena.sys.JenaSystem;

/**
 * Query execution for local datasets - builder style.
 */
public class QueryExecDatasetBuilderImpl implements QueryExecMod, QueryExecDatasetBuilder {

    static { JenaSystem.init(); }

    /** Create a new builder of {@link QueryExec} for a local dataset. */
    public static QueryExecDatasetBuilder create() {
        return new QueryExecDatasetBuilderImpl();
    }

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

    private List<QueryExecTransform> queryExecTransforms = new ArrayList<>();

    private QueryExecDatasetBuilderImpl() { }

    @Override
    public Query getQuery()         { return query; }

    @Override
    public String getQueryString()  { return queryString; }

    @Override
    public QueryExecDatasetBuilderImpl query(Query query) {
        this.query = query;
        this.queryString = null;
        return this;
    }

    @Override
    public QueryExecDatasetBuilderImpl query(String queryString) {
        query(queryString, Syntax.syntaxARQ);
        return this;
    }

    @Override
    public QueryExecDatasetBuilderImpl query(String queryString, Syntax syntax) {
        this.query = QueryFactory.create(queryString, syntax);
        return this;
    }

    @Override
    public QueryExecDatasetBuilderImpl parseCheck(boolean parseCheck) {
        return this;
    }

    @Override
    public QueryExecDatasetBuilderImpl dataset(DatasetGraph dsg) {
        this.dataset = dsg;
        return this;
    }

    public QueryExecDatasetBuilderImpl graph(Graph graph) {
        DatasetGraph dsg = DatasetGraphFactory.wrap(graph);
        dataset(dsg);
        return this;
    }

    @Override
    public QueryExecDatasetBuilderImpl set(Symbol symbol, Object value) {
        contextAcc.set(symbol, value);
        return this;
    }

    @Override
    public QueryExecDatasetBuilderImpl set(Symbol symbol, boolean value) {
        contextAcc.set(symbol, value);
        return this;
    }

    @Override
    public QueryExecDatasetBuilderImpl context(Context cxt) {
        contextAcc.context(cxt);
        return this;
    }

    @Override
    public Context getContext() {
        return contextAcc.context();
    }

    @Override
    public QueryExecDatasetBuilderImpl substitution(Binding binding) {
        ensureSubstitutionMap();
        binding.forEach(this.substitutionMap::put);
        return this;
    }

    @Override
    public QueryExecDatasetBuilderImpl substitution(Var var, Node value) {
        ensureSubstitutionMap();
        this.substitutionMap.put(var, value);
        return this;
    }

    private void ensureSubstitutionMap() {
        if ( substitutionMap == null )
            substitutionMap = new HashMap<>();
    }

    @Override
    public QueryExecDatasetBuilderImpl transformExec(QueryExecTransform queryExecTransform) {
        Objects.requireNonNull(queryExecTransform);
        queryExecTransforms.add(queryExecTransform);
        return this;
    }

    @Override
    public QueryExecDatasetBuilderImpl timeout(long timeout) {
        return timeout(timeout, TimeUnit.MILLISECONDS);
    }

    @Override
    public QueryExecDatasetBuilderImpl timeout(long timeout, TimeUnit timeUnit) {
        timeoutBuilder.timeout(timeout, timeUnit);
        return this;
    }

    @Override
    public QueryExecDatasetBuilderImpl initialTimeout(long timeout, TimeUnit timeUnit) {
        timeoutBuilder.initialTimeout(timeout, timeUnit);
        return this;
    }

    @Override
    public QueryExecDatasetBuilderImpl overallTimeout(long timeout, TimeUnit timeUnit) {
        timeoutBuilder.overallTimeout(timeout, timeUnit);
        return this;
    }

    @Override
    public QueryExec build() {
        Objects.requireNonNull(query, "No query for QueryExec");
        // Queries can have FROM/FROM NAMED or VALUES to get data.
        //Objects.requireNonNull(dataset, "No dataset for QueryExec");
        query.ensureResultVars();
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
            queryActual = QueryTransformOps.replaceVars(query, substitutionMap);
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

