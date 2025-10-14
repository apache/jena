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

package org.apache.jena.sparql.exec.tracker;

import java.util.Iterator;
import java.util.function.Supplier;

import org.apache.jena.atlas.json.JsonArray;
import org.apache.jena.atlas.json.JsonObject;
import org.apache.jena.graph.Graph;
import org.apache.jena.graph.Triple;
import org.apache.jena.query.Query;
import org.apache.jena.query.QueryType;
import org.apache.jena.sparql.core.DatasetGraph;
import org.apache.jena.sparql.core.Quad;
import org.apache.jena.sparql.exec.QueryExec;
import org.apache.jena.sparql.exec.RowSet;
import org.apache.jena.sparql.util.Context;

/**
 * Wrapper for QueryExec that tracks any encountered exception.
 * This is accomplished by wrapping RowSets and Iterators of the underlying QueryExec.
 */
public abstract class QueryExecTaskBase<T extends QueryExec> implements QueryExec
{
    private T delegate;
    private ThrowableTracker throwableTracker;
    private QueryType queryExecType = null;

    public QueryExecTaskBase(T delegate, ThrowableTracker tracker) {
        super();
        this.delegate = delegate;
        this.throwableTracker = tracker;
    }

    protected T getDelegate() {
        return delegate;
    }

    protected ThrowableTracker getThrowableTracker() {
        return throwableTracker;
    }

    /**
     * The query type requested for execution.
     * For example, calling select() sets this type to {@link QueryType#SELECT}.
     */
    public QueryType getQueryExecType() {
        return queryExecType;
    }

    @Override
    public Context getContext() {
        return getDelegate().getContext();
    }

    @Override
    public Query getQuery() {
        return getDelegate().getQuery();
    }

    @Override
    public String getQueryString() {
        return getDelegate().getQueryString();
    }

    @Override
    public void close() {
        try {
            getDelegate().close();
        } finally {
            afterExec();
        }
    }

    @Override
    public boolean isClosed() {
        return getDelegate().isClosed();
    }

    @Override
    public void abort() {
        getDelegate().abort();
    }

    public void beforeExec(QueryType queryType) {
        this.queryExecType = queryType;
    }

    /** Note that afterExec is run only on close. */
    public void afterExec() {
    }

    @Override
    public RowSet select() {
        return compute(QueryType.SELECT, () -> wrapRowSet(getDelegate().select()));
    }

    @Override
    public Graph construct() {
        return compute(QueryType.CONSTRUCT, () -> getDelegate().construct());
    }

    @Override
    public Graph construct(Graph graph) {
        return compute(QueryType.CONSTRUCT, () -> getDelegate().construct(graph));
    }

    @Override
    public Graph describe() {
        return compute(QueryType.DESCRIBE, () -> getDelegate().describe());
    }

    @Override
    public Graph describe(Graph graph) {
        return compute(QueryType.DESCRIBE, () -> getDelegate().describe(graph));
    }

    @Override
    public boolean ask() {
        return compute(QueryType.ASK, () -> getDelegate().ask());
    }

    @Override
    public Iterator<Triple> constructTriples() {
        return compute(QueryType.CONSTRUCT, () -> wrapIterator(getDelegate().constructTriples()));
    }

    @Override
    public Iterator<Triple> describeTriples() {
        return compute(QueryType.CONSTRUCT, () -> wrapIterator(getDelegate().describeTriples()));
    }

    @Override
    public Iterator<Quad> constructQuads() {
        return compute(QueryType.CONSTRUCT, () -> wrapIterator(getDelegate().constructQuads()));
    }

    @Override
    public DatasetGraph constructDataset() {
        return compute(QueryType.CONSTRUCT, () -> getDelegate().constructDataset());
    }

    @Override
    public DatasetGraph constructDataset(DatasetGraph dataset) {
        return compute(QueryType.CONSTRUCT, () -> getDelegate().constructDataset(dataset));
    }

    @Override
    public JsonArray execJson() {
        return compute(QueryType.CONSTRUCT_JSON, () -> getDelegate().execJson());
    }

    @Override
    public Iterator<JsonObject> execJsonItems() {
        return compute(QueryType.CONSTRUCT_JSON, () -> wrapIterator(getDelegate().execJsonItems()));
    }

    @Override
    public DatasetGraph getDataset() {
        return getDelegate().getDataset();
    }

    protected RowSet wrapRowSet(RowSet base) {
        return new RowSetTracked(base, getThrowableTracker());
    }

    protected <X> Iterator<X> wrapIterator(Iterator<X> base) {
        return new IteratorTracked<>(base, getThrowableTracker());
    }

    protected <X> X compute(QueryType queryType, Supplier<X> supplier) {
        beforeExec(queryType);
        try {
            X result = supplier.get();
            return result;
        } catch(Throwable e) {
            e.addSuppressed(new RuntimeException("Error during select()."));
            throwableTracker.report(e);
            throw e;
        }
        // afterExec is called during close()
    }
}
