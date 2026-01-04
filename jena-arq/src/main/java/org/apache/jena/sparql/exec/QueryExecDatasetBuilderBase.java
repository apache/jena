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

import org.apache.jena.graph.Node;
import org.apache.jena.query.ARQ;
import org.apache.jena.query.Query;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.query.Syntax;
import org.apache.jena.sparql.adapter.ParseCheckUtils;
import org.apache.jena.sparql.core.DatasetGraph;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.engine.Timeouts.TimeoutBuilderImpl;
import org.apache.jena.sparql.engine.binding.Binding;
import org.apache.jena.sparql.exec.tracker.QueryExecTransform;
import org.apache.jena.sparql.util.Context;
import org.apache.jena.sparql.util.ContextAccumulator;
import org.apache.jena.sparql.util.Symbol;
import org.apache.jena.sys.JenaSystem;

/**
 * Query execution for local datasets - builder style.
 */
public abstract class QueryExecDatasetBuilderBase<X extends QueryExecDatasetBuilderBase<X>>
    implements QueryExecDatasetBuilder
{

    static { JenaSystem.init(); }

    protected DatasetGraph dataset            = null;
    protected Query        query              = null;
    protected String       queryString        = null;
    protected Syntax       syntax             = null;
    protected Boolean parseCheck              = null;

    protected ContextAccumulator contextAcc =
              ContextAccumulator.newBuilder(()->ARQ.getContext(), ()->Context.fromDataset(dataset));

    // Uses query rewrite to replace variables by values.
    protected Map<Var, Node>  substitutionMap  = null;

    protected TimeoutBuilderImpl timeoutBuilder  = new TimeoutBuilderImpl();

    protected List<QueryExecTransform> queryExecTransforms = new ArrayList<>();

    protected QueryExecDatasetBuilderBase() { }

    @Override public Query getQuery()         { return query; }
    @Override public String getQueryString()  { return queryString; }

    @Override
    public X dataset(DatasetGraph dsg) {
        this.dataset = dsg;
        return thisBuilder();
    }

    @SuppressWarnings("unchecked")
    protected X thisBuilder() {
        return (X)this;
    }

    @Override
    public X query(Query query) {
        this.query = query;
        this.queryString = null;
        this.syntax = null;
        return thisBuilder();
    }

    @Override
    public X query(String queryString) {
        query(queryString, Syntax.syntaxARQ);
        return thisBuilder();
    }

    @Override
    public X query(String queryString, Syntax syntax) {
        boolean parseCheck = effectiveParseCheck();
        this.query = parseCheck ? QueryFactory.create(queryString, syntax) : null;
        this.queryString = queryString;
        this.syntax = syntax;
        return thisBuilder();
    }

    @Override
    public X parseCheck(boolean parseCheck) {
        this.parseCheck = parseCheck;
        return thisBuilder();
    }

    protected boolean effectiveParseCheck() {
        return ParseCheckUtils.effectiveParseCheck(parseCheck, contextAcc);
    }

    @Override
    public X set(Symbol symbol, Object value) {
        contextAcc.set(symbol, value);
        return thisBuilder();
    }

    @Override
    public X set(Symbol symbol, boolean value) {
        contextAcc.set(symbol, value);
        return thisBuilder();
    }

    @Override
    public X context(Context cxt) {
        contextAcc.context(cxt);
        return thisBuilder();
    }

    @Override
    public Context getContext() {
        return contextAcc.context();
    }

    @Override
    public X substitution(Binding binding) {
        ensureSubstitutionMap();
        binding.forEach(this.substitutionMap::put);
        return thisBuilder();
    }

    @Override
    public X substitution(Var var, Node value) {
        ensureSubstitutionMap();
        this.substitutionMap.put(var, value);
        return thisBuilder();
    }

    private void ensureSubstitutionMap() {
        if ( substitutionMap == null )
            substitutionMap = new HashMap<>();
    }

    @Override
    public X timeout(long timeout) {
        return timeout(timeout, TimeUnit.MILLISECONDS);
    }

    @Override
    public X timeout(long timeout, TimeUnit timeUnit) {
        timeoutBuilder.timeout(timeout, timeUnit);
        return thisBuilder();
    }

    @Override
    public X initialTimeout(long timeout, TimeUnit timeUnit) {
        timeoutBuilder.initialTimeout(timeout, timeUnit);
        return thisBuilder();
    }

    @Override
    public X overallTimeout(long timeout, TimeUnit timeUnit) {
        timeoutBuilder.overallTimeout(timeout, timeUnit);
        return thisBuilder();
    }

    @Override
    public X transformExec(QueryExecTransform queryExecTransform) {
        Objects.requireNonNull(queryExecTransform);
        queryExecTransforms.add(queryExecTransform);
        return thisBuilder();
    }
}
