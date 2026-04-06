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

package org.apache.jena.riot.writer.c14n;

import java.util.Iterator;
import java.util.LinkedHashSet;

import org.apache.jena.atlas.iterator.Iter;
import org.apache.jena.graph.Graph;
import org.apache.jena.graph.Node;
import org.apache.jena.query.ReadWrite;
import org.apache.jena.query.TxnType;
import org.apache.jena.riot.system.PrefixMap;
import org.apache.jena.riot.system.PrefixMapFactory;
import org.apache.jena.sparql.core.*;

/**
 * A simple dataset implementation that preserves the order of quads.
 * {@link DatasetGraph#find()} will return in insertion order.
 */
public class DatasetGraphOrdered extends DatasetGraphQuads {

    private LinkedHashSet<Quad> quads = new LinkedHashSet<>();
    private PrefixMap prefixMap = PrefixMapFactory.create();

    @Override
    public PrefixMap prefixes() {
        return prefixMap;
    }

    @Override
    public Iterator<Quad> find() {
        return quads.iterator();
    }

    @Override
    public Iterator<Quad> find(Node g, Node s, Node p, Node o) {
        Iterator<Quad> iterator = Iter.filter(quads.iterator(), quad -> matches(quad, g, s, p, o));
        return iterator;
    }

    @Override
    public Iterator<Quad> findNG(Node g, Node s, Node p, Node o) {
       Iterator<Quad> iterator = Iter.filter(quads.iterator(),
                                             quad -> (matches(quad, g, s, p, o) && ! Quad.isDefaultGraph(g)));
        return iterator;
    }

    @Override
    public void add(Quad quad) {
        quads.add(quad);
    }

    @Override
    public void delete(Quad quad) {
        quads.remove(quad);
    }

    @Override
    public Graph getDefaultGraph() {
        return GraphView.createDefaultGraph(this);
    }

    @Override
    public Graph getGraph(Node graphNode) {
        return GraphView.createNamedGraph(this, graphNode);
    }

    private static boolean matches(Quad quadData, Node g, Node s, Node p, Node o) {
        return Match.match(quadData, g, s, p, o);
    }

    // ----
    private final Transactional txn = TransactionalLock.createMRSW();
    private final Transactional txn() {
        return txn;
    }

    @Override
    public void begin() {
        txn().begin();
    }

    @Override
    public void begin(TxnType txnType) {
        txn().begin(txnType);
    }

    @Override
    public void begin(ReadWrite mode) {
        txn().begin(mode);
    }

    @Override
    public boolean promote(Promote txnType) {
        return txn().promote(txnType);
    }

    @Override
    public void commit() {
        txn().commit();
    }

    @Override
    public void abort() {
        txn().abort();
    }

    @Override
    public boolean isInTransaction() {
        return txn().isInTransaction();
    }

    @Override
    public void end() {
        txn().end();
    }

    @Override
    public ReadWrite transactionMode() {
        return txn().transactionMode();
    }

    @Override
    public TxnType transactionType() {
        return txn().transactionType();
    }

    @Override
    public boolean supportsTransactions() {
        return true;
    }

    @Override
    public boolean supportsTransactionAbort() {
        return false;
    }
    // ----

    // Secondary indexes?
//
// @Override
// public void performAdd( Triple t ) {
// triples.add(t);
// }
//
// @Override
// public void performDelete( Triple t ) {
// triples.remove(t);
// }
//
// @Override
// public TransactionHandler getTransactionHandler() {
// return new TransactionHandlerNull();
// }
//
// @Override
// public ExtendedIterator<Triple> find() {
// return WrappedIterator.create(triples.iterator());
// }
//
// @Override
// protected ExtendedIterator<Triple> graphBaseFind(Triple triplePattern) {
// Iterator<Triple> iterator = Iter.filter(triples.iterator(),
// triple->matches(triple, triplePattern));
// return WrappedIterator.create(iterator);
// }
//
// private static boolean matches(Triple tripleData, Triple triplePattern) {
// return Match.match(tripleData,
// triplePattern.getSubject(),
// triplePattern.getPredicate(),
// triplePattern.getObject());
// }
}
