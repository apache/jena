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
import java.util.List;

import org.apache.jena.atlas.iterator.Iter;
import org.apache.jena.atlas.lib.tuple.Tuple;
import org.apache.jena.dboe.base.record.RecordFactory;
import org.apache.jena.dboe.storage.StoragePrefixes;
import org.apache.jena.dboe.storage.prefixes.PrefixEntry;
import org.apache.jena.dboe.storage.prefixes.PrefixLib;
import org.apache.jena.dboe.transaction.txn.Transaction;
import org.apache.jena.dboe.transaction.txn.TransactionException;
import org.apache.jena.dboe.transaction.txn.TransactionalSystem;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.tdb2.store.nodetupletable.NodeTupleTable;

public class StoragePrefixesTDB implements StoragePrefixes {

    static final RecordFactory factory = new RecordFactory(3*NodeId.SIZE, 0);
    private TransactionalSystem txnSystem;
    private NodeTupleTable prefixTable;

    public StoragePrefixesTDB(TransactionalSystem txnSystem, NodeTupleTable prefixTable) {
        this.txnSystem = txnSystem;
        this.prefixTable = prefixTable;
    }

    public NodeTupleTable getNodeTupleTable() {
        return prefixTable;
    }

    @Override
    public String get(Node graphNode, String prefix) {
        requireTxn();
        graphNode = PrefixLib.canonicalGraphName(graphNode);
        Node p = NodeFactory.createLiteral(prefix);
        Iterator<Tuple<Node>> iter = prefixTable.find(graphNode, p, null);
        if ( ! iter.hasNext() )
            return null;
        Node x = iter.next().get(2);
        Iter.close(iter);
        return x.getURI();
    }

    @Override
    public Iterator<PrefixEntry> get(Node graphNode) {
        requireTxn();
        graphNode = PrefixLib.canonicalGraphName(graphNode);
        Iterator<Tuple<Node>> iter = prefixTable.find(graphNode, null, null);
        return Iter.iter(iter).map(t->PrefixEntry.create(t.get(1).getLiteralLexicalForm(), t.get(2).getURI()));
    }

    @Override
    public Iterator<Node> listGraphNodes() {
        requireTxn();
        Iterator<Tuple<Node>> iter = prefixTable.find((Node)null, null, null);
        return Iter.iter(iter).map(t->t.get(0)).distinct();
    }

    @Override
    public void add(Node graphNode, String prefix, String iriStr) {
        ensureWriteTxn();
        add_ext(graphNode, prefix, iriStr);
    }

    /** Add without checks - used by the bulkloader when it takes control of the transaction. */  
    public void add_ext(Node graphNode, String prefix, String iriStr) {
        // By exposing the operation here, we use the rules (e.g. canonicalGraphName) on
        // added prefixes. Going to the NodeTupleTable prefixTable would skip those and
        // require node creation in the caller as well.
        graphNode = PrefixLib.canonicalGraphName(graphNode);
        Node p = NodeFactory.createLiteral(prefix);
        Node u = NodeFactory.createURI(iriStr);
        // Delete any existing old mapping of prefix.
        remove_ext(graphNode, p, Node.ANY);
        prefixTable.addRow(graphNode,p,u);
    }

    @Override
    public void delete(Node graphNode, String prefix) {
        Node p = NodeFactory.createLiteral(prefix);
        remove(graphNode, p, null);
    }

    @Override
    public void deleteAll(Node graphNode) {
        remove(graphNode, null, null);
    }

    private void remove(Node g, Node p, Node u) {
        ensureWriteTxn();
        remove_ext(g, p, u);
    }

    /** Remove without checks - used by the bulkloader when it takes control of the transaction. */  
    private void remove_ext(Node g, Node p, Node u) {
        // See add_ext
        g = PrefixLib.canonicalGraphName(g);
        Iterator<Tuple<Node>> iter = prefixTable.find(g, p, u);
        List<Tuple<Node>> list = Iter.toList(iter);    // Materialize.
        for ( Tuple<Node> tuple : list )
            prefixTable.deleteRow(tuple.get(0), tuple.get(1), tuple.get(2));
    }

    @Override
    public boolean isEmpty() {
        requireTxn();
        return prefixTable.isEmpty();
    }

    @Override
    public int size() {
        requireTxn();
        return (int)prefixTable.size();
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
            throw new TransactionException("Not in a transaction");
        txn.ensureWriteTxn();
    }
}
