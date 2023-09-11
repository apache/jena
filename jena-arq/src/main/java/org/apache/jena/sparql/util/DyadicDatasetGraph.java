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

package org.apache.jena.sparql.util;

import static java.util.Objects.requireNonNull;
import static org.apache.jena.atlas.iterator.Iter.count;
import static org.apache.jena.atlas.iterator.Iter.map;
import static org.apache.jena.graph.Node.ANY;
import static org.apache.jena.query.TxnType.READ;
import static org.apache.jena.sparql.core.Quad.defaultGraphIRI;

import java.util.Iterator;
import java.util.Objects;

import org.apache.jena.atlas.iterator.Iter;
import org.apache.jena.atlas.lib.PairOfSameType;
import org.apache.jena.graph.Graph;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.compose.MultiUnion;
import org.apache.jena.query.ReadWrite;
import org.apache.jena.query.TxnType;
import org.apache.jena.riot.other.G;
import org.apache.jena.riot.system.PrefixMap;
import org.apache.jena.riot.system.PrefixMapUnmodifiable;
import org.apache.jena.shared.Lock;
import org.apache.jena.sparql.JenaTransactionException;
import org.apache.jena.sparql.core.DatasetGraph;
import org.apache.jena.sparql.core.Quad;

public abstract class DyadicDatasetGraph extends PairOfSameType<DatasetGraph> implements DatasetGraph {

    private Context context;

    private final Lock lock;

    protected DyadicDatasetGraph(DatasetGraph left, DatasetGraph right, Context c) {
        super(requireNonNull(left), requireNonNull(right));
        this.context = requireNonNull(c);
        this.lock = new PairLock(left.getLock(), right.getLock());
    }

    protected static void throwNoMutationAllowed() {
        throw new UnsupportedOperationException("This view does not allow mutation!");
    }


    @Override
    public PrefixMap prefixes() {
        return new PrefixMapUnmodifiable(getLeft().prefixes());
    }

    @Override
    public void commit() {
        forEach(DatasetGraph::commit);
    }

    @Override
    public void begin() {
        begin(TxnType.READ);
    }


    @Override
    public void begin(TxnType type) {
        switch (type) {
        case READ:
            forEach(dsg -> dsg.begin(type));
            break;
        default:
            throw new JenaTransactionException("Only READ transactions supported");
        }
    }

    @Override
    public boolean promote(Promote txnType) {
        // no mutation allowed
        return false;
    }

    @Override
    public ReadWrite transactionMode() {
        if ( ! isInTransaction() )
            return null;
        return TxnType.convert(transactionType());
    }

    @Override
    public TxnType transactionType() {
        if ( ! isInTransaction() )
            return null;
        // no mutation allowed
        return READ ;
    }

    @Override
    public synchronized void begin(ReadWrite readWrite) {
        Objects.requireNonNull(readWrite);
        begin(TxnType.convert(readWrite));
    }

    @Override
    public void abort() {
        forEach(DatasetGraph::abort);
    }

    @Override
    public void end() {
        forEach(DatasetGraph::end);
    }

    @Override
    public boolean isInTransaction() {
        if ( both(DatasetGraph::isInTransaction) )
            return true;
        if ( !either(DatasetGraph::isInTransaction) )
            return false;
        throw new JenaTransactionException("One datset in a transaction and one not");
    }

    @Override
    public void addGraph(Node graphName, Graph graph) {
        throwNoMutationAllowed();
    }

    @Override
    public void removeGraph(Node graphName) {
        throwNoMutationAllowed();
    }

    @Override
    public void add(Quad quad) {
        throwNoMutationAllowed();
    }

    @Override
    public void delete(Quad quad) {
        throwNoMutationAllowed();
    }

    @Override
    public void add(Node g, Node s, Node p, Node o) {
        throwNoMutationAllowed();
    }

    @Override
    public void delete(Node g, Node s, Node p, Node o) {
        throwNoMutationAllowed();
    }

    @Override
    public void deleteAny(Node g, Node s, Node p, Node o) {
        throwNoMutationAllowed();
    }

    @Override
    public void clear() {
        throwNoMutationAllowed();
    }

    @Override
    public Iterator<Quad> find() {
        return find(Quad.ANY);
    }

    @Override
    public Iterator<Quad> find(Quad q) {
        return find(q.getGraph(), q.getSubject(), q.getPredicate(), q.getObject());
    }

    @Override
    public Iterator<Quad> find(Node g, Node s, Node p, Node o) {
        if ( g.isConcrete() )
            return findInOneGraph(g, s, p, o);
        Iterator<Quad> iter = findNG(ANY, s, p, o);
        iter = Iter.concat(iter, findInOneGraph(defaultGraphIRI, s, p, o));
        return iter;
    }

    @Override
    public Iterator<Quad> findNG(Node g, Node s, Node p, Node o) {
        if ( g.isConcrete() )
            return findInOneGraph(g, s, p, o);
        Iterator<Quad> iter = Iter.flatMap(listGraphNodes(), gn -> findInOneGraph(gn, s, p, o));
        return iter;
    }

    protected Iterator<Quad> findInOneGraph(Node g, Node s, Node p, Node o) {
        return G.triples2quads(g, getGraph(g).find(s, p, o));
    }

    @Override
    public Graph getUnionGraph() {
        return new MultiUnion(map(listGraphNodes(), this::getGraph));
    }

    @Override
    public boolean contains(Quad q) {
        return contains(q.getGraph(), q.getSubject(), q.getPredicate(), q.getObject());
    }

    @Override
    public Lock getLock() {
        return lock;
    }

    @Override
    public Context getContext() {
        return context;
    }

    public DatasetGraph setContext(Context c) {
        this.context = c;
        return this;
    }

    @Override
    public void close() {
    }

    @Override
    public boolean supportsTransactions() {
        return both(DatasetGraph::supportsTransactions);
    }

    @Override
    public boolean supportsTransactionAbort() {
        return both(DatasetGraph::supportsTransactionAbort);
    }

    @Override
    public long size() {
        return count(listGraphNodes());
    }

    @Override
    public boolean isEmpty() {
        return listGraphNodes().hasNext();
    }

    private static class PairLock extends PairOfSameType<Lock> implements Lock {

        public PairLock(Lock left, Lock right) {
            super(left, right);
        }

        @Override
        public void enterCriticalSection(boolean readLockRequested) {
            forEach(lock -> lock.enterCriticalSection(readLockRequested));
        }

        @Override
        public void leaveCriticalSection() {
            forEach(Lock::leaveCriticalSection);
        }
    }
}
