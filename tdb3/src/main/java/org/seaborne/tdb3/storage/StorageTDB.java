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

package org.seaborne.tdb3.storage;

import java.util.stream.Stream;

import org.apache.jena.atlas.iterator.Iter;
import org.apache.jena.atlas.lib.InternalErrorException;
import org.apache.jena.atlas.lib.NotImplemented;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.Triple;
import org.apache.jena.query.ReadWrite;
import org.apache.jena.sparql.core.DatasetChanges;
import org.apache.jena.sparql.core.DatasetPrefixStorage;
import org.apache.jena.sparql.core.Quad;
import org.apache.jena.sparql.core.QuadAction;
import org.apache.jena.sparql.engine.optimizer.reorder.ReorderTransformation;
import org.apache.jena.sparql.util.Context;
import org.seaborne.dboe.base.file.Location;
import org.seaborne.dboe.transaction.Transactional;
import org.seaborne.dboe.transaction.TransactionalMonitor;
import org.seaborne.dboe.transaction.txn.Transaction;
import org.seaborne.dboe.transaction.txn.TransactionException;
import org.seaborne.dboe.transaction.txn.TransactionalSystem;
import org.seaborne.tdb2.setup.StoreParams;
import org.seaborne.tdb2.store.QuadTable;
import org.seaborne.tdb2.store.TripleTable;
import projects.dsg2.storage.StorageRDF;

// The transactional stuff could go in DatasetGraphStorage
// Or getTransactional()?
public class StorageTDB implements StorageRDF, Transactional {
    // SWITCHING.
    private TripleTable                 tripleTable;
    private QuadTable                   quadTable;
    private DatasetPrefixStorage        prefixes;
    private Location                    location;
    // SWITCHING.

    private final ReorderTransformation transform;
    private StoreParams                 config;

    // private GraphTDB defaultGraphTDB ;
    private final boolean               checkForChange = false;
    private boolean                     closed         = false;
    private TransactionalSystem         txnSystem;
    private Context                     context        = new Context();

    public StorageTDB(TransactionalSystem txnSystem, TripleTable tripleTable, QuadTable quadTable, DatasetPrefixStorage prefixes,
                      ReorderTransformation transform, Location location, StoreParams params) {
        reset(txnSystem, tripleTable, quadTable, prefixes, location, params);
        this.transform = transform;
        // this.defaultGraphTDB = getDefaultGraphTDB() ;
    }

    public void reset(TransactionalSystem txnSystem, TripleTable tripleTable, QuadTable quadTable, DatasetPrefixStorage prefixes,
                      Location location, StoreParams params) {
        this.txnSystem = txnSystem;
        this.tripleTable = tripleTable;
        this.quadTable = quadTable;
        this.prefixes = prefixes;
        // this.defaultGraphTDB = getDefaultGraphTDB() ;
        this.config = params;
        this.location = location;
    }

    public QuadTable getQuadTable() {
        checkActive();
        return quadTable;
    }

    public TripleTable getTripleTable() {
        checkActive();
        return tripleTable;
    }

    public Context getContext() { return context; }

    private void checkActive() {}

    // Watching changes (add, delete, deleteAny)

    private DatasetChanges monitor = null;

    public void setMonitor(DatasetChanges changes) {
        monitor = changes;
    }

    public void removeMonitor(DatasetChanges changes) {
        if ( monitor != changes )
            throw new InternalErrorException();
        monitor = null;
    }

    // XXX Optimize by integrating with add/delete operations.
    private final void notifyAdd(Node g, Node s, Node p, Node o) {
        if ( monitor == null )
            return;
        QuadAction action = QuadAction.ADD;
        if ( checkForChange ) {
            if ( contains(g, s, p, o) )
                action = QuadAction.NO_ADD;
        }
        monitor.change(action, g, s, p, o);
    }

    private final void notifyDelete(Node g, Node s, Node p, Node o) {
        if ( monitor == null )
            return;
        QuadAction action = QuadAction.DELETE;
        if ( checkForChange ) {
            if ( !contains(g, s, p, o) )
                action = QuadAction.NO_DELETE;
        }
        monitor.change(action, g, s, p, o);
    }

    @Override
    public void add(Node s, Node p, Node o) {
        checkActive();
        requireWriteTxn();
        notifyAdd(null, s, p, o);
        getTripleTable().add(s, p, o);
    }

    @Override
    public void add(Node g, Node s, Node p, Node o) {
        checkActive();
        requireWriteTxn();
        notifyAdd(g, s, p, o);
        getQuadTable().add(g, s, p, o);
    }

    @Override
    public void delete(Node s, Node p, Node o) {
        checkActive();
        requireWriteTxn();
        notifyDelete(null, s, p, o);
        getTripleTable().delete(s, p, o);
        
    }

    @Override
    public void delete(Node g, Node s, Node p, Node o) {
        checkActive();
        requireWriteTxn();
        notifyDelete(g, s, p, o);
        getQuadTable().delete(g, s, p, o);
    }

    @Override
    public void removeAll(Node s, Node p, Node o) {
        checkActive();
        requireWriteTxn();
        throw new NotImplemented();
    }

    @Override
    public void removeAll(Node g, Node s, Node p, Node o) {
        checkActive();
        requireWriteTxn();
        throw new NotImplemented();
    }

    @Override
    public Stream<Quad> find(Node g, Node s, Node p, Node o) {
        checkActive();
        requireTxn();
        return Iter.asStream(getQuadTable().find(g, s, p, o));
    }

    @Override
    public Stream<Triple> find(Node s, Node p, Node o) {
        checkActive();
        requireTxn();
        return Iter.asStream(getTripleTable().find(s, p, o));
    }

    @Override
    public boolean contains(Node s, Node p, Node o) {
        checkActive();
        requireTxn();
        // XXX Add contains to TripleTable
        return getTripleTable().find(s, p, o).hasNext();
    }

    @Override
    public boolean contains(Node g, Node s, Node p, Node o) {
        checkActive();
        requireTxn();
        // XXX Add contains to QuadTable
        return getQuadTable().find(g, s, p, o).hasNext();
    }

    
    // Promotion
    private void requireTxn() {
        if ( ! txnSystem.isInTransaction() )
            throw new TransactionException("Not on a transaction");
    }
    
    // Promotion
    private void requireWriteTxn() {
        Transaction txn = txnSystem.getThreadTransaction();
        if ( txn.isWriteTxn() )
            return;
        // Transaction.promoteOrException
        boolean b = txn.promote();
        if ( !b )
            throw new TransactionException("Can't write");
    }

    @Override
    public boolean isInTransaction() {
        return txnSystem.isInTransaction() ;
    }
    
    // txnSystem with monitor?
    @Override
    public void begin(ReadWrite readWrite) {
        if ( txnMonitor != null ) txnMonitor.startBegin(readWrite); 
        txnSystem.begin(readWrite) ;
        if ( txnMonitor != null ) txnMonitor.finishBegin(readWrite); 
    }

    @Override
    public boolean promote() {
        if ( txnMonitor != null ) txnMonitor.startPromote();
        try { 
            return txnSystem.promote() ;
        } finally { if ( txnMonitor != null ) txnMonitor.finishPromote(); }
    }

    @Override
    public void commit() {
        if ( txnMonitor != null ) txnMonitor.startCommit();
        txnSystem.commit() ;
        if ( txnMonitor != null ) txnMonitor.finishCommit();  
    }

    @Override
    public void abort() {
        if ( txnMonitor != null ) txnMonitor.startAbort() ; 
        txnSystem.abort() ;
        if ( txnMonitor != null ) txnMonitor.finishAbort() ;  
    }

    @Override
    public void end() {
        if ( txnMonitor != null ) txnMonitor.startEnd(); 
        txnSystem.end() ;
        if ( txnMonitor != null ) txnMonitor.finishEnd(); 
    }

    public TransactionalSystem getTxnSystem() {
        return txnSystem ;
    }

   // Watching Transactional
    
    private TransactionalMonitor txnMonitor = null ;
    public void setTransactionalMonitor(TransactionalMonitor changes) {
        txnMonitor = changes ;
    }

    public void removeTransactionalMonitor(TransactionalMonitor changes) {
        if ( txnMonitor != changes )
            throw new InternalErrorException() ;
        txnMonitor = null ;
    }

}
