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

package org.apache.jena.sparql.graph;

import java.util.List;

import org.apache.jena.graph.Graph;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.Triple;
import org.apache.jena.query.ReadWrite;
import org.apache.jena.query.TxnType;
import org.apache.jena.sparql.core.DatasetGraph;
import org.apache.jena.sparql.core.DatasetGraphFactory;
import org.apache.jena.sparql.core.Transactional;
import org.apache.jena.sparql.core.mem.DatasetGraphInMemory;
import org.apache.jena.util.iterator.ExtendedIterator;
import org.apache.jena.util.iterator.WrappedIterator;

/**
 * In-memory, transactional graph.
 *
 * @implNote
 * The implementation uses the default graph of {@link DatasetGraphInMemory}.
 * The graph transaction handler continues to work.
 * This class adds the {@link Transactional} to the graph itself
 * and provides {@link ExtendedIterator ExtendedIterators} that provide
 * read access to the data if used outside a transaction.
 */
public class GraphTxn extends GraphWrapper implements Transactional {

    private DatasetGraph dsg;
    private Graph graph;

    public GraphTxn() {
        super(null);
        this.dsg = DatasetGraphFactory.createTxnMem();
        this.graph = dsg.getDefaultGraph();
    }

    // TransactionHandler will go via dsg.getDefaultGraph.

    @Override
    public Graph get() { return graph; }

    private Transactional getT() { return dsg; }

    // Map Transactional to the DSG.
    // Like TransactionalWrapper, but we want to subclass GraphWrapper.
    @Override
    public void begin(TxnType type) {
        getT().begin(type);
    }

    @Override
    public void begin(ReadWrite readWrite) {
        getT().begin(readWrite);
    }

    @Override
    public boolean promote(Promote mode) {
        return getT().promote(mode);
    }

    @Override
    public void commit() {
        getT().commit();
    }

    @Override
    public void abort() {
        getT().abort();
    }

    @Override
    public void end() {
        getT().end();
    }

    @Override
    public ReadWrite transactionMode() {
        return getT().transactionMode();
    }

    @Override
    public TxnType transactionType() {
        return getT().transactionType();
    }

    @Override
    public boolean isInTransaction() {
        return getT().isInTransaction();
    }

    private static class IteratorTxn<T> extends WrappedIterator<T> {

        private final GraphTxn graph;
        private final boolean needIterTxn;

        IteratorTxn(GraphTxn graph, ExtendedIterator<T> base) {
            super(base, true);  // removeDenied.
            this.graph = graph;
            needIterTxn = ! graph.getT().isInTransaction();
            if ( needIterTxn )
                graph.begin(TxnType.READ);
        }

        @Override
        public void close() {
            if ( needIterTxn ) {
                graph.commit();
                graph.end();
            }
        }
    }

    /**
     * Whether to read the base graph completely at the point that "find" is called
     * or to have an iterator over results. Both setting should work in normal situations
     * Using a {@link IteratorTxn}, which is an {@link ExtendedIterator},
     * starts a read transaction and that assumes the extended iterator is closed.
     */
    private static boolean ISOLATE = false;

    @Override
    public ExtendedIterator<Triple> find(Triple triple) {
        if ( ISOLATE )
            return isolate(get().find(triple));
        return new IteratorTxn<Triple>(this, get().find(triple));
    }

    @Override
    public ExtendedIterator<Triple> find(Node s, Node p, Node o) {
        if ( ISOLATE )
            return isolate(get().find(s, p, o));
        return new IteratorTxn<Triple>(this, get().find(s, p, o));
    }

    /** Isolate by materializing the iterator. */
    private ExtendedIterator<Triple> isolate(ExtendedIterator<Triple> iter) {
        List<Triple> list = iter.toList();
        return WrappedIterator.create(list.iterator());
    }
}
