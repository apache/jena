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

import org.apache.jena.atlas.lib.Pair;
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
import org.apache.jena.sparql.engine.binding.Binding;
import org.apache.jena.sparql.syntax.syntaxtransform.QueryTransformOps;
import org.apache.jena.sparql.util.Context;
import org.apache.jena.sparql.util.Symbol;
import org.apache.jena.sys.JenaSystem;

/**
 * Query execution for local datasets - builder style.
 */
public class QueryExecDatasetBuilder implements QueryExecMod, QueryExecBuilder {

    static { JenaSystem.init(); }

    /** Create a new builder of {@link QueryExec} for a local dataset. */
    public static QueryExecDatasetBuilder newBuilder() {
        QueryExecDatasetBuilder builder = new QueryExecDatasetBuilder();
        return builder;
    }

    private static final long UNSET         = -1;

    private DatasetGraph dataset            = null;
    private Query        query              = null;
    private String       queryString        = null;
    // Items added with "set(,)"
    private Context      addedContext       = new Context();
    // Explicitly given base context (defaults to a copy of ARQ.getContext() merged with dataset context.
    private Context      baseContext        = null;
    // Migration - context when built, but available early to QueryExecution
    private Context      builtContext        = null;

    // Uses query rewrite to replace variables by values.
    private Map<Var, Node>  substitutionMap  = null;

    // Uses initial binding to execution (old, original) feature
    private Binding      initialBinding      = null;
    private long         initialTimeout      = UNSET;
    private TimeUnit     initialTimeoutUnit  = null;
    private long         overallTimeout      = UNSET;
    private TimeUnit     overallTimeoutUnit  = null;

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
        addedContext.set(symbol, value);
        return this;
    }

    @Override
    public QueryExecDatasetBuilder set(Symbol symbol, boolean value) {
        addedContext.set(symbol, value);
        return this;
    }

    @Override
    public QueryExecDatasetBuilder context(Context context) {
        this.baseContext = context;
        this.addedContext.clear();
        return this;
    }

    @Override
    public Context getContext() {
        // QueryExecution may modify this.
        // [QExec] A Context which also modifies addedContext??
        if ( builtContext == null )
            builtContext = buildContext(baseContext, dataset, addedContext);
        return builtContext;
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

    public QueryExecDatasetBuilder initialBinding(Binding binding) {
        this.initialBinding = binding;
        return this;
    }

    @Override
    public QueryExecDatasetBuilder timeout(long value, TimeUnit timeUnit) {
        this.initialTimeout = UNSET;
        this.initialTimeoutUnit = null;
        this.overallTimeout = value;
        this.overallTimeoutUnit = timeUnit;
        return this;
    }

    @Override
    public QueryExecDatasetBuilder initialTimeout(long value, TimeUnit timeUnit) {
        this.initialTimeout = value < 0 ? -1L : value ;
        this.initialTimeoutUnit = timeUnit;
        return this;
    }

    @Override
    public QueryExecDatasetBuilder overallTimeout(long value, TimeUnit timeUnit) {
        this.overallTimeout = value;
        this.overallTimeoutUnit = timeUnit;
        return this;
    }

    // Set times from context if not set directly. e..g Context provides default values.
    // Contrast with SPARQLQueryProcessor where the context is limiting values of the protocol parameter.
    private static void defaultTimeoutsFromContext(QueryExecDatasetBuilder builder, Context cxt) {
        applyTimeouts(builder, cxt.get(ARQ.queryTimeout));
    }

    /** Take obj, find the timeout(s) and apply to the builder */
    private static void applyTimeouts(QueryExecDatasetBuilder builder, Object obj) {
        if ( obj == null )
            return ;
        try {
            if ( obj instanceof Number ) {
                long x = ((Number)obj).longValue();
                if ( builder.overallTimeout < 0 )
                    builder.overallTimeout(x, TimeUnit.MILLISECONDS);
            } else if ( obj instanceof String ) {
                String str = obj.toString();
                Pair<Long, Long> pair = Timeouts.parseTimeoutStr(str, TimeUnit.MILLISECONDS);
                if ( pair == null ) {
                    Log.warn(builder, "Bad timeout string: "+str);
                    return ;
                }
                if ( builder.initialTimeout < 0 )
                    builder.initialTimeout(pair.getLeft(), TimeUnit.MILLISECONDS);
                if ( builder.overallTimeout < 0 )
                    builder.overallTimeout(pair.getRight(), TimeUnit.MILLISECONDS);
            } else
                Log.warn(builder, "Can't interpret timeout: " + obj);
        } catch (Exception ex) {
            Log.warn(builder, "Exception setting timeouts (context) from: "+obj);
        }
    }

    @Override
    public QueryExec build() {
        Objects.requireNonNull(query, "No query for QueryExecution");
        query.setResultVars();
        Context cxt = getContext();

        QueryEngineFactory qeFactory = QueryEngineRegistry.get().find(query, dataset, cxt);
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

        defaultTimeoutsFromContext(this, cxt);

        if ( dataset != null )
            cxt.set(ARQConstants.sysCurrentDataset, DatasetFactory.wrap(dataset));
        if ( queryActual != null )
            cxt.set(ARQConstants.sysCurrentQuery, queryActual);

        QueryExec qExec = new QueryExecDataset(queryActual, queryStringActual, dataset, cxt, qeFactory,
                                               initialTimeout, initialTimeoutUnit,
                                               overallTimeout, overallTimeoutUnit,
                                               initialBinding);
        // Unset cached value.
        builtContext = null;
        return qExec;
    }

    private Context dftContext() {
        return Context.setupContextForDataset(ARQ.getContext(), dataset) ;
    }

    private static Context buildContext(Context baseContext, DatasetGraph dataset, Context addedContext) {
        // Default is to take the global context, the copy it and merge in the dataset context.
        // If a context is specified by context(Context), use that as given.
        // The query context is modified to insert the current time.
        // This copy-isolates.

        Context cxt;
        if ( baseContext == null )
            cxt = Context.setupContextForDataset(ARQ.getContext(), dataset) ;
        else
            cxt = baseContext;
        if ( addedContext != null )
            cxt.putAll(addedContext);
        return cxt;
    }
}
