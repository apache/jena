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

package org.apache.jena.tdb2.store;

import java.util.Iterator;
import java.util.function.Consumer;
import java.util.function.Supplier;

import org.apache.jena.atlas.lib.tuple.Tuple;
import org.apache.jena.dboe.storage.StorageRDF;
import org.apache.jena.dboe.transaction.txn.Transaction;
import org.apache.jena.dboe.transaction.txn.TransactionException;
import org.apache.jena.dboe.transaction.txn.TransactionalSystem;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.Triple;
import org.apache.jena.sparql.core.Quad;

/** {@link StorageRDF} for TDB2 */
public class StorageTDB implements StorageRDF {
    // SWITCHING. This could be the switch point, not the DatasetGraph. Probably makes little difference.
    private TripleTable                 tripleTable;
    private QuadTable                   quadTable;
    private TransactionalSystem         txnSystem;
    // SWITCHING.

    // In notifyAdd and notifyDelete,  check whether the change is a real change or not.
    // e.g. Adding a quad already present is not a real change.
    // However, that requires looking in the data so incurs a cost.
    // Normally, "false". "QuadAction.NO_*" are not used.
    private final boolean               checkForChange = false;
    private boolean                     closed         = false;

    public StorageTDB(TransactionalSystem txnSystem, TripleTable tripleTable, QuadTable quadTable) {
        this.txnSystem = txnSystem;
        this.tripleTable = tripleTable;
        this.quadTable = quadTable;
    }

    public QuadTable getQuadTable() {
        checkActive();
        return quadTable;
    }

    public TripleTable getTripleTable() {
        checkActive();
        return tripleTable;
    }

    private void checkActive() {}

    private final void notifyAdd(Node g, Node s, Node p, Node o) { }

    private final void notifyDelete(Node g, Node s, Node p, Node o) { }

    @Override
    public void add(Node s, Node p, Node o) {
        checkActive();
        ensureWriteTxn();
        notifyAdd(null, s, p, o);
        getTripleTable().add(s, p, o);
    }

    @Override
    public void add(Node g, Node s, Node p, Node o) {
        checkActive();
        ensureWriteTxn();
        notifyAdd(g, s, p, o);
        getQuadTable().add(g, s, p, o);
    }

    @Override
    public void delete(Node s, Node p, Node o) {
        checkActive();
        ensureWriteTxn();
        notifyDelete(null, s, p, o);
        getTripleTable().delete(s, p, o);
    }

    @Override
    public void delete(Node g, Node s, Node p, Node o) {
        checkActive();
        ensureWriteTxn();
        notifyDelete(g, s, p, o);
        getQuadTable().delete(g, s, p, o);
    }

    @Override
    public void removeAll(Node s, Node p, Node o) {
        checkActive();
        ensureWriteTxn();
        removeWorker(() -> tripleTable.getNodeTupleTable().findAsNodeIds(s,p,o),
                     x  -> tripleTable.getNodeTupleTable().getTupleTable().delete(x) );
    }

    @Override
    public void removeAll(Node g, Node s, Node p, Node o) {
        checkActive();
        ensureWriteTxn();
        removeWorker(() -> quadTable.getNodeTupleTable().findAsNodeIds(g,s,p,o),
                     x  -> quadTable.getNodeTupleTable().getTupleTable().delete(x) );
    }

    private static final int DeleteBufferSize = 1000;

    /** General purpose "remove by pattern" code */
    private void removeWorker(Supplier<Iterator<Tuple<NodeId>>> finder, Consumer<Tuple<NodeId>> deleter) {
        // Allocate buffer once.
        // Not Java11 @SuppressWarnings("unchecked")
        //Tuple<NodeId>[] buffer = (Tuple<NodeId>[])new Object[DeleteBufferSize];
        Object[] buffer = new Object[DeleteBufferSize];
        while (true) {
            Iterator<Tuple<NodeId>> iter = finder.get();
            // Get a slice
            int idx = 0;
            for (; idx < DeleteBufferSize; idx++ ) {
                if ( !iter.hasNext() )
                    break;
                buffer[idx] = iter.next();
            }
            // Delete them.
            for ( int i = 0; i < idx; i++ ) {
                @SuppressWarnings("unchecked")
                Tuple<NodeId> x = (Tuple<NodeId>)buffer[i];
                deleter.accept(x);
                buffer[i] = null;
            }
            // Finished?
            if ( idx < DeleteBufferSize )
                break;
        }
    }

    @Override
    public Iterator<Quad> find(Node g, Node s, Node p, Node o) {
        checkActive();
        requireTxn();
        return getQuadTable().find(g, s, p, o);
    }

    @Override
    public Iterator<Triple> find(Node s, Node p, Node o) {
        checkActive();
        requireTxn();
        return getTripleTable().find(s, p, o);
    }

//    @Override
//    public Stream<Quad> stream(Node g, Node s, Node p, Node o) {
//        checkActive();
//        requireTxn();
//        return Iter.asStream(getQuadTable().find(g, s, p, o));
//    }
//
//    @Override
//    public Stream<Triple> stream(Node s, Node p, Node o) {
//        checkActive();
//        requireTxn();
//        return Iter.asStream(getTripleTable().find(s, p, o));
//    }

    @Override
    public boolean contains(Node s, Node p, Node o) {
        checkActive();
        requireTxn();
        return getTripleTable().find(s, p, o).hasNext();
    }

    @Override
    public boolean contains(Node g, Node s, Node p, Node o) {
        checkActive();
        requireTxn();
        return getQuadTable().find(g, s, p, o).hasNext();
    }

    // This test is also done by the transactional components so no need to test here.
    private void requireTxn() {}

//    private void requireTxn() {
//        if ( ! txnSystem.isInTransaction() )
//            throw new TransactionException("Not on a transaction");
//    }

    private void ensureWriteTxn() {
        Transaction txn = txnSystem.getThreadTransaction();
        if ( txn == null )
            throw new TransactionException("Not on a write transaction");
        txn.ensureWriteTxn();
    }
}
