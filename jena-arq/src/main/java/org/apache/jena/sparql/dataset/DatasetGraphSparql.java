/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 *
 *   SPDX-License-Identifier: Apache-2.0
 */

package org.apache.jena.sparql.dataset;

import java.util.Iterator;
import java.util.Objects;

import org.apache.jena.graph.Graph;
import org.apache.jena.graph.Node;
import org.apache.jena.query.Query;
import org.apache.jena.query.ReadWrite;
import org.apache.jena.query.TxnType;
import org.apache.jena.riot.system.PrefixMap;
import org.apache.jena.riot.system.PrefixMapFactory;
import org.apache.jena.riot.system.Prefixes;
import org.apache.jena.riot.system.StreamRDF;
import org.apache.jena.shared.PrefixMapping;
import org.apache.jena.sparql.JenaTransactionException;
import org.apache.jena.sparql.core.DatasetGraph;
import org.apache.jena.sparql.core.DatasetGraphBase;
import org.apache.jena.sparql.core.GraphView;
import org.apache.jena.sparql.core.Quad;
import org.apache.jena.sparql.core.Transactional;
import org.apache.jena.sparql.core.TransactionalNull;
import org.apache.jena.sparql.exec.ParseCheckUtils;
import org.apache.jena.sparql.exec.QueryExec;
import org.apache.jena.sparql.exec.UpdateExec;
import org.apache.jena.sparql.util.Context;
import org.apache.jena.update.UpdateRequest;

/**
 * Base {@link DatasetGraph} implementation that forwards <i>all</i> SPARQL
 * execution to a {@link DsgSparqlExecutor} instance.
 */
public abstract class DatasetGraphSparql extends DatasetGraphBase {

    private final PrefixMap prefixes = PrefixMapFactory.create();
    private final PrefixMapping prefixMapping = Prefixes.adapt(prefixes);
    private final Transactional transactional = TransactionalNull.create();

    /** Helper that knows how to turn a Query/UpdateRequest into an executable. */
    private final DsgSparqlExecutor executor;

    public DatasetGraphSparql() {
        this(DsgSparqlExecutor.DEFAULT);
    }

    public DatasetGraphSparql(DsgSparqlExecutor executor) {
        super();
        this.executor = Objects.requireNonNull(executor);
        initContext();
    }

    @Override
    public PrefixMap prefixes() {
        return prefixes;
    }

    protected Transactional getTransactional() {
        return transactional;
    }

    protected void initContext() {
        Context cxt = getContext();
        // Advertise that SPARQL statements should not be parsed.
        ParseCheckUtils.setParseCheck(cxt, false);
    }

    protected abstract QueryExec query(Query query);
    protected abstract UpdateExec update(UpdateRequest updateRequest);

    /** Create a bulk‑insert {@link StreamRDF} that writes via the executor. */
    protected StreamRDF newUpdateSink() {
        return new StreamRDFToUpdateRequest(this::execUpdate, prefixMapping, Integer.MAX_VALUE);
    }

    protected void execUpdate(UpdateRequest updateRequest) {
        UpdateExec uExec = update(updateRequest);
        uExec.execute();
    }

    @Override
    public Iterator<Node> listGraphNodes() {
        return executor.listGraphNodes(this::query, prefixMapping);
    }

    @Override
    public Iterator<Quad> find(Node g, Node s, Node p, Node o) {
        return executor.find(this::query, prefixMapping, g, s, p, o);
    }

    @Override
    public Iterator<Quad> findNG(Node g, Node s, Node p, Node o) {
        // Named‑graph only
        return executor.findNG(this::query, prefixMapping, g, s, p, o);
    }

    @Override
    public boolean contains(Node g, Node s, Node p, Node o) {
        return executor.contains(this::query, prefixMapping, g, s, p, o);
    }

    @Override
    public boolean containsGraph(Node graphNode) {
        return executor.containsGraph(this::query, prefixMapping, graphNode);
    }

    @Override
    public Graph getDefaultGraph() {
        DatasetGraphSparql self = this;
        return new GraphView(this, Quad.defaultGraphNodeGenerated) {
            @Override
            protected int graphBaseSize() {
                long sz = sizeLong();
                return (sz < Integer.MAX_VALUE) ? (int)sz : Integer.MAX_VALUE;
            }

            @Override
            public long sizeLong() {
                long r = executor.fetchDefaultGraphSize(self::query, prefixMapping);
                return r;
            }
        };
    }

    @Override
    public Graph getGraph(Node graphNode) {
        DatasetGraphSparql self = this;
        return new GraphView(this, graphNode) {
            @Override
            protected int graphBaseSize() {
                long sz = sizeLong();
                return (sz < Integer.MAX_VALUE) ? (int)sz : Integer.MAX_VALUE;
            }

            @Override
            public long sizeLong() {
                long r = executor.fetchGraphSize(self::query, prefixMapping, graphNode);
                return r;
            }
        };
    }

    @Override
    public void addGraph(Node graphName, Graph graph) {
        // COPY is eligible when adding a graph that is already a view over this dataset.
        if (graph instanceof GraphView view && view.getDataset() == this) {
            Node source = view.getGraphName();
            executor.copy(this::update, prefixMapping, source, graphName);
        } else {
            addGraphViaUpdateSink(graphName, graph);
        }
    }

    protected void addGraphViaUpdateSink(Node graphName, Graph graph) {
        StreamRDF sink = newUpdateSink();
        try {
            sink.start();
            StreamRDFToUpdateRequest.sendGraphTriplesToStream(graph, graphName, sink);
        } finally {
            sink.finish();
        }
    }

    @Override
    public void removeGraph(Node graphName) {
        Objects.requireNonNull(graphName);
        deleteAny(graphName, Node.ANY, Node.ANY, Node.ANY);
    }

    @Override
    public void add(Quad quad) {
        executor.add(this::update, prefixMapping, quad);
    }

    @Override
    public void delete(Quad quad) {
        executor.delete(this::update, prefixMapping, quad);
    }

    @Override
    public void deleteAny(Node g, Node s, Node p, Node o) {
        executor.deleteAny(this::update, prefixMapping, g, s, p, o);
    }

    @Override
    public long size() {
        return executor.fetchGraphCount(this::query, prefixMapping);
    }

    @Override
    public boolean supportsTransactions() {
        return false;
    }

    @Override
    public boolean supportsTransactionAbort() {
        return false;
    }

    @Override
    public void abort() {
        getTransactional().abort();
    }

    @Override
    public void begin(ReadWrite readWrite) {
        getTransactional().begin(readWrite);
    }

    @Override
    public void commit() {
        getTransactional().commit();
    }

    @Override
    public void end() {
        try {
            if (isInTransaction()) {
                if (transactionMode().equals(ReadWrite.WRITE)) {
                    String msg = "end() called for WRITE transaction without commit or abort having been called. This causes a forced abort.";
                    throw new JenaTransactionException(msg);
                }
            }
        } finally {
            getTransactional().end();
        }
    }

    @Override
    public boolean isInTransaction() {
        return getTransactional().isInTransaction();
    }

    @Override
    public void begin(TxnType type) {
        getTransactional().begin(type);
    }

    @Override
    public boolean promote(Promote mode) {
        return getTransactional().promote(mode);
    }

    @Override
    public ReadWrite transactionMode() {
        return getTransactional().transactionMode();
    }

    @Override
    public TxnType transactionType() {
        return getTransactional().transactionType();
    }
}
