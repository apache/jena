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

import org.apache.jena.atlas.json.JsonArray;
import org.apache.jena.atlas.json.JsonObject;
import org.apache.jena.graph.Graph;
import org.apache.jena.graph.Triple;
import org.apache.jena.query.DatasetFactory;
import org.apache.jena.query.Query;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.sparql.core.DatasetGraph;
import org.apache.jena.sparql.core.Quad;
import org.apache.jena.sparql.util.Context;

public class QueryExecAdapter implements QueryExec {

    private QueryExecution queryExecution;

    protected QueryExecAdapter(QueryExecution queryExecution) {
        this.queryExecution = queryExecution;
    }

    protected QueryExecution get() { return queryExecution; }

    public static QueryExec adapt(QueryExecution qExec) {
        if ( qExec instanceof QueryExecutionAdapter) {
            return ((QueryExecutionAdapter)qExec).get();
        }
        return new QueryExecAdapter(qExec);
    }

    @Override
    public DatasetGraph getDataset() {
        return get().getDataset().asDatasetGraph();
    }

    @Override
    public Context getContext() {
        return get().getContext();
    }

    @Override
    public Query getQuery() {
        return get().getQuery();
    }

    @Override
    public RowSet select() {
        return new RowSetAdapter(get().execSelect());
    }

    @Override
    public Graph construct() {
        return get().execConstruct().getGraph();
    }

    @Override
    public Graph construct(Graph graph) {
        get().execConstruct(ModelFactory.createModelForGraph(graph));
        return graph;
    }

    @Override
    public Iterator<Triple> constructTriples() {
        return get().execConstructTriples();
    }

    @Override
    public Iterator<Quad> constructQuads() {
        return get().execConstructQuads();
    }

    @Override
    public DatasetGraph constructDataset(DatasetGraph dataset) {
        get().execConstructDataset(DatasetFactory.wrap(dataset));
        return dataset;
    }

    @Override
    public Graph describe() {
        return get().execDescribe().getGraph();
    }

    @Override
    public Graph describe(Graph graph) {
        get().execConstruct(ModelFactory.createModelForGraph(graph));
        return graph;
    }

    @Override
    public Iterator<Triple> describeTriples() {
        return get().execDescribeTriples();
    }

    @Override
    public boolean ask() {
        return get().execAsk();
    }

    @Override
    public JsonArray execJson() {
        return get().execJson();
    }

    @Override
    public Iterator<JsonObject> execJsonItems() {
        return get().execJsonItems();
    }

    @Override
    public void abort() { get().abort(); }

    @Override
    public void close() { get().close(); }

    @Override
    public boolean isClosed() { return get().isClosed(); }
}
