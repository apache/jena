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

import java.util.Iterator;
import java.util.function.Supplier;

import org.apache.jena.atlas.json.JsonArray;
import org.apache.jena.atlas.json.JsonObject;
import org.apache.jena.graph.Graph;
import org.apache.jena.graph.Triple;
import org.apache.jena.query.Query;
import org.apache.jena.sparql.core.DatasetGraph;
import org.apache.jena.sparql.core.Quad;
import org.apache.jena.sparql.util.Context;

public abstract class QueryExecWrapper<X extends QueryExec>
    implements QueryExec
{
    private X delegate;

    public QueryExecWrapper(X delegate) {
        super();
        this.delegate = delegate;
    }

    protected X getDelegate() {
        return delegate;
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
        getDelegate().close();
    }

    @Override
    public boolean isClosed() {
        return getDelegate().isClosed();
    }

    @Override
    public void abort() {
        getDelegate().abort();
    }

    @Override
    public RowSet select() {
        return exec(() -> getDelegate().select());
    }

    @Override
    public Graph construct() {
        return exec(() -> getDelegate().construct());
    }

    @Override
    public Graph construct(Graph graph) {
        return exec(() -> getDelegate().construct(graph));
    }

    @Override
    public Graph describe() {
        return exec(() -> getDelegate().describe());
    }

    @Override
    public Graph describe(Graph graph) {
        return exec(() -> getDelegate().describe(graph));
    }

    @Override
    public boolean ask() {
        return exec(() -> getDelegate().ask());
    }

    @Override
    public Iterator<Triple> constructTriples() {
        return exec(() -> getDelegate().constructTriples());
    }

    @Override
    public Iterator<Triple> describeTriples() {
        return exec(() -> getDelegate().describeTriples());
    }

    @Override
    public Iterator<Quad> constructQuads() {
        return exec(() -> getDelegate().constructQuads());
    }

    @Override
    public DatasetGraph constructDataset() {
        return exec(() -> getDelegate().constructDataset());
    }

    @Override
    public DatasetGraph constructDataset(DatasetGraph dataset) {
        return exec(() -> getDelegate().constructDataset(dataset));
    }

    @Override
    public JsonArray execJson() {
        return exec(() -> getDelegate().execJson());
    }

    @Override
    public Iterator<JsonObject> execJsonItems() {
        return exec(() -> getDelegate().execJsonItems());
    }

    @Override
    public DatasetGraph getDataset() {
        return getDelegate().getDataset();
    }

    protected <T> T exec(Supplier<T> supplier) {
        T result = supplier.get();
        return result;
    }
}
