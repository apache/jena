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
import org.apache.jena.graph.Triple;
import org.apache.jena.query.*;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.sparql.ARQConstants;
import org.apache.jena.sparql.core.Quad;
import org.apache.jena.sparql.engine.binding.Binding;
import org.apache.jena.sparql.engine.binding.BindingLib;
import org.apache.jena.sparql.util.Context;

/**
 * Query execution that delays making the QueryExecution until needed by exec* This
 * means timeout and initialBinds can still be set.
 *
 * This is temporary - setTimne, setIntialBinding on a QueryExecution are being phased out.
 *
 * @see QueryExecution
 */
public class QueryExecutionCompat extends QueryExecutionAdapter {
    private final QueryExecBuilder qExecBuilder;
    private QueryExec qExecHere = null;
    private Dataset datasetHere = null;

    public static QueryExecution compatibility(QueryExecBuilder qExec) {
        return new QueryExecutionCompat(qExec);
    }

    @SuppressWarnings("deprecation")
    public QueryExecutionCompat(QueryExecBuilder qExecBuilder) {
        super(null);
        this.qExecBuilder = qExecBuilder;
        // [QExec]
        qExecBuilder.setupContext();
    }

    @Override
    protected QueryExec get() { return qExecHere; }

    private QueryExec state() {
        // Build, don't keep.
        return qExecBuilder.build();
    }

    private void execution() {
        // Delay until use so setTimeout,setInitialBindings work.
        // Also - rebuild allowed!
        if ( qExecHere == null ) {
            qExecHere = qExecBuilder.build();
            Object x = qExecHere.getDataset();
            datasetHere = qExecHere.getDataset() != null ? DatasetFactory.wrap(qExecHere.getDataset()) : null;
            // [QExec]
            qExecHere.getContext().set(ARQConstants.sysCurrentDataset, datasetHere);
        }
    }

    @Override
    public void setInitialBinding(Binding binding) {
        qExecBuilder.initialBinding(binding);
    }

    @Override
    public Dataset getDataset() {
        return datasetHere;
    }

    @SuppressWarnings("deprecation")
    @Override
    public Context getContext() {
        if ( qExecHere == null )
            return qExecBuilder.setupContext();
        return qExecHere.getContext();
    }

    @Override
    public Query getQuery() {
        return state().getQuery();
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
    public void setTimeout(long timeout, TimeUnit timeoutUnits) {
        qExecBuilder.timeout(timeout, timeoutUnits);
    }

    @Override
    public void setTimeout(long timeout) {
        qExecBuilder.timeout(timeout, TimeUnit.MILLISECONDS);
    }

    @Override
    public void setTimeout(long timeout1, TimeUnit timeUnit1, long timeout2, TimeUnit timeUnit2) {
        qExecBuilder.initialTimeout(timeout1, timeUnit1);
        qExecBuilder.overallTimeout(timeout2, timeUnit2);
    }

    @Override
    public void setTimeout(long timeout1, long timeout2) {
        qExecBuilder.initialTimeout(timeout1, TimeUnit.MILLISECONDS);
        qExecBuilder.overallTimeout(timeout2, TimeUnit.MILLISECONDS);
    }

    @Override
    public long getTimeout1() {
        return -1L;
    }

    @Override
    public long getTimeout2() {
        return -1L;
    }

    @Override
    public void setInitialBinding(QuerySolution querySolution) {
        qExecBuilder.initialBinding(BindingLib.toBinding(querySolution));
    }
}
