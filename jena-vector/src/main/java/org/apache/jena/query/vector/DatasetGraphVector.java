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

package org.apache.jena.query.vector;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.jena.graph.Graph;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.Triple;
import org.apache.jena.query.ReadWrite;
import org.apache.jena.sparql.core.DatasetGraph;
import org.apache.jena.sparql.core.DatasetGraphWrapper;
import org.apache.jena.sparql.core.GraphView;
import org.apache.jena.sparql.core.Quad;
import org.apache.jena.util.iterator.ExtendedIterator;

public class DatasetGraphVector extends DatasetGraphWrapper {
    private final VectorIndex vectorIndex;
    private final Graph dftGraph;
    private final boolean closeIndexOnClose;
    private final ThreadLocal<ReadWrite> readWriteMode = new ThreadLocal<>();

    public DatasetGraphVector(DatasetGraph dsg, VectorIndex vectorIndex, boolean closeIndexOnClose) {
        super(dsg);
        this.vectorIndex = vectorIndex;
        this.closeIndexOnClose = closeIndexOnClose;
        this.dftGraph = GraphView.createDefaultGraph(this);
    }

    public VectorIndex getVectorIndex() {
        return vectorIndex;
    }

    @Override
    public Graph getDefaultGraph() {
        return dftGraph;
    }

    @Override
    public Graph getGraph(Node graphNode) {
        return GraphView.createNamedGraph(this, graphNode);
    }

    @Override
    public void add(Node g, Node s, Node p, Node o) {
        if (contains(g, s, p, o))
            return;
        super.add(g, s, p, o);
        if (p.equals(vectorIndex.getTextPredicate()) && o.isLiteral()) {
            vectorIndex.add(g, s, p, o.getLiteralLexicalForm());
            autocommitVectorIndex();
        }
    }

    @Override
    public void add(Quad quad) {
        add(quad.getGraph(), quad.getSubject(), quad.getPredicate(), quad.getObject());
    }

    @Override
    public void delete(Node g, Node s, Node p, Node o) {
        if (!contains(g, s, p, o))
            return;
        super.delete(g, s, p, o);
        if (p.equals(vectorIndex.getTextPredicate())) {
            vectorIndex.delete(g, s, p);
            autocommitVectorIndex();
        }
    }

    @Override
    public void delete(Quad quad) {
        delete(quad.getGraph(), quad.getSubject(), quad.getPredicate(), quad.getObject());
    }

    @Override
    public void deleteAny(Node g, Node s, Node p, Node o) {
        Node graph = g == null ? Quad.defaultGraphNodeGenerated : g;
        List<Quad> quads = new ArrayList<>();
        Iterator<Quad> iterator = find(graph, s, p, o);
        while (iterator.hasNext())
            quads.add(iterator.next());
        quads.forEach(this::delete);
    }

    @Override
    public void addGraph(Node graphName, Graph graph) {
        ExtendedIterator<Triple> iterator = graph.find(Node.ANY, Node.ANY, Node.ANY);
        try {
            while (iterator.hasNext()) {
                Triple triple = iterator.next();
                add(graphName, triple.getSubject(), triple.getPredicate(), triple.getObject());
            }
        } finally {
            iterator.close();
        }
    }

    @Override
    public void removeGraph(Node graphName) {
        deleteAny(graphName, Node.ANY, Node.ANY, Node.ANY);
    }

    @Override
    public void begin(ReadWrite readWrite) {
        readWriteMode.set(readWrite);
        super.begin(readWrite);
    }

    @Override
    public void commit() {
        if (readWriteMode.get() == ReadWrite.WRITE)
            vectorIndex.prepareCommit();
        super.commit();
        if (readWriteMode.get() == ReadWrite.WRITE)
            vectorIndex.commit();
        readWriteMode.remove();
    }

    @Override
    public void abort() {
        try {
            super.abort();
        } finally {
            if (readWriteMode.get() == ReadWrite.WRITE)
                vectorIndex.rollback();
            readWriteMode.remove();
        }
    }

    @Override
    public void end() {
        if (readWriteMode.get() == ReadWrite.WRITE) {
            try {
                vectorIndex.rollback();
            } finally {
                readWriteMode.remove();
            }
        } else {
            readWriteMode.remove();
        }
        super.end();
    }

    @Override
    public void close() {
        super.close();
        if (closeIndexOnClose)
            vectorIndex.close();
    }

    private void autocommitVectorIndex() {
        if (readWriteMode.get() == null)
            vectorIndex.commit();
    }
}
