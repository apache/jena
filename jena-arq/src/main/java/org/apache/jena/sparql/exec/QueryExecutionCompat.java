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
import org.apache.jena.graph.Triple;
import org.apache.jena.query.Dataset;
import org.apache.jena.query.Query;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.ResultSet;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.sparql.core.Quad;
import org.apache.jena.sparql.util.Context;

/**
 * Query execution that delays making the QueryExecution until needed by exec
 *
 * @see QueryExecution
 */
public class QueryExecutionCompat extends QueryExecutionAdapter {
    private final QueryExecMod qExecBuilder;
    private QueryExec qExecHere = null;
    private final Dataset datasetHere;
    private final Query queryHere;

    public static QueryExecution compatibility(QueryExecMod qExec, Dataset dataset, Query query, String queryString) {
        return new QueryExecutionCompat(qExec, dataset, query);
    }

    private QueryExecutionCompat(QueryExecMod qExecBuilder, Dataset dataset, Query query) {
        super(null);
        this.qExecBuilder = qExecBuilder;
        this.datasetHere = dataset;
        this.queryHere = query;
    }

    @Override
    protected QueryExec get() {
        execution();
        return qExecHere;
    }

    private void execution() {
        // Delay until used so setTimeout,setInitialBindings work.
        if ( qExecHere == null )
            qExecHere = qExecBuilder.build();
    }

    @Override
    public Dataset getDataset() {
        return datasetHere;
    }

    @Override
    public Context getContext() {
        return qExecBuilder.getContext();
    }

    @Override
    public Query getQuery() {
        if ( queryHere != null )
            return queryHere;
        // Have to build (and hope! It may be a queryString with non-jena extensions).
        execution();
        return qExecHere.getQuery();
    }

    @Override
    public ResultSet execSelect() {
        execution();
        return super.execSelect();
    }

    @Override
    public Model execConstruct() {
        execution();
        return super.execConstruct();
    }

    @Override
    public Model execConstruct(Model model) {
        execution();
        return super.execConstruct(model);
    }

    @Override
    public Iterator<Triple> execConstructTriples() {
        execution();
        return super.execConstructTriples();
    }

    @Override
    public Iterator<Quad> execConstructQuads() {
        execution();
        return super.execConstructQuads();
    }

    @Override
    public Dataset execConstructDataset() {
        execution();
        return super.execConstructDataset();
    }

    @Override
    public Dataset execConstructDataset(Dataset dataset) {
        execution();
        return super.execConstructDataset(dataset);
    }

    @Override
    public Model execDescribe() {
        execution();
        return super.execDescribe();
    }

    @Override
    public Model execDescribe(Model model) {
        execution();
        return super.execDescribe(model);
    }

    @Override
    public Iterator<Triple> execDescribeTriples() {
        execution();
        return super.execDescribeTriples();
    }

    @Override
    public boolean execAsk() {
        execution();
        return super.execAsk();
    }

    @Override
    public JsonArray execJson() {
        execution();
        return super.execJson();
    }

    @Override
    public Iterator<JsonObject> execJsonItems() {
        execution();
        return super.execJsonItems();
    }

    @Override
    public void abort() {
        execution();
        super.abort();
    }

    @Override
    public void close() {
        execution();
        super.close();
    }

    @Override
    public boolean isClosed() {
        if ( qExecHere == null )
            return false;
        return qExecHere.isClosed();
    }

    @Override
    public long getTimeout1() {
        return -1L;
    }

    @Override
    public long getTimeout2() {
        return -1L;
    }
}
