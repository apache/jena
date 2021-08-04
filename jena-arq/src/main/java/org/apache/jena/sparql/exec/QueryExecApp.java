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
import java.util.concurrent.TimeUnit;

import org.apache.jena.atlas.json.JsonArray;
import org.apache.jena.atlas.json.JsonObject;
import org.apache.jena.graph.Graph;
import org.apache.jena.graph.Triple;
import org.apache.jena.query.Query;
import org.apache.jena.sparql.core.DatasetGraph;
import org.apache.jena.sparql.core.Quad;
import org.apache.jena.sparql.util.Context;

/**
 * {@link QueryExec} that delays making the QueryExec until needed by a query operation
 * This means timeouts and initialBinding can still be set via the {@link QueryExecMod}.
 *
 * @see QueryExec
 */
public class QueryExecApp implements QueryExec {
    private final QueryExecMod qExecBuilder;
    private QueryExec qExecHere = null;
    // Frozen elements of the build.
    private final DatasetGraph datasetHere;
    private final Query queryHere;
    private final String queryStringHere;

    public static QueryExec create(QueryExecMod qExec, DatasetGraph dataset, Query query, String queryString) {
        return new QueryExecApp(qExec, dataset, query, queryString);
    }

    private QueryExecApp(QueryExecMod qExecBuilder, DatasetGraph dataset, Query query, String queryString) {
        // In normal use, one of query and queryString should be non-null
        // (If being used as a carrier for QueryExecMod

        this.qExecBuilder = qExecBuilder;
        // Have the QueryExecBuilder build the context now, even though it is not finished.
        this.datasetHere = dataset;
        this.queryHere = query;
        this.queryStringHere = queryString;
    }

    protected QueryExec get() { return qExecHere; }

    public QueryExecMod getBuilder() {
        return qExecBuilder;
    }

    private void execution() {
        if ( qExecHere == null )
            qExecHere = qExecBuilder.build();
    }

    @Override
    public DatasetGraph getDataset() {
        return datasetHere;
    }

    @Override
    public Context getContext() {
        return qExecBuilder.getContext();
    }

    @Override
    public Query getQuery() {
        return queryHere;
    }

    @Override
    public String getQueryString() {
        return queryStringHere;
    }

    @Override
    public RowSet select() {
        execution();
        return qExecHere.select();
    }

    @Override
    public Graph construct(Graph graph) {
        execution();
        return qExecHere.construct(graph);
    }

    @Override
    public Iterator<Triple> constructTriples() {
        execution();
        return qExecHere.constructTriples();
    }

    @Override
    public Iterator<Quad> constructQuads() {
        execution();
        return qExecHere.constructQuads();
    }

    @Override
    public DatasetGraph constructDataset(DatasetGraph dataset) {
        execution();
        return qExecHere.constructDataset(dataset);
    }

    @Override
    public Graph describe(Graph graph) {
        execution();
        return qExecHere.describe(graph);
    }

    @Override
    public Iterator<Triple> describeTriples() {
        execution();
        return qExecHere.describeTriples();
    }

    @Override
    public boolean ask() {
        execution();
        return qExecHere.ask();
    }

    @Override
    public JsonArray execJson() {
        execution();
        return qExecHere.execJson();
    }

    @Override
    public Iterator<JsonObject> execJsonItems() {
        execution();
        return qExecHere.execJsonItems();
    }

    @Override
    public void abort() {
        execution();
        qExecHere.abort();
    }

    @Override
    public void close() {
        execution();
        qExecHere.close();
    }

    @Override
    public boolean isClosed() {
        if ( qExecHere == null )
            return false;
        return qExecHere.isClosed();
    }

    public void timeout(long timeout, TimeUnit timeoutUnits) {
        qExecBuilder.timeout(timeout, timeoutUnits);
    }

    public void timeout(long timeout) {
        qExecBuilder.timeout(timeout, TimeUnit.MILLISECONDS);
    }

    public void timeout(long timeout1, TimeUnit timeUnit1, long timeout2, TimeUnit timeUnit2) {
        qExecBuilder.initialTimeout(timeout1, timeUnit1);
        qExecBuilder.overallTimeout(timeout2, timeUnit2);
    }

    public void timeout(long timeout1, long timeout2) {
        qExecBuilder.initialTimeout(timeout1, TimeUnit.MILLISECONDS);
        qExecBuilder.overallTimeout(timeout2, TimeUnit.MILLISECONDS);
    }
}
