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

package org.apache.jena.rdfpatch.system;

import java.util.Iterator;
import java.util.function.Consumer;

import org.apache.jena.graph.Graph;
import org.apache.jena.graph.Node;
import org.apache.jena.query.ReadWrite;
import org.apache.jena.query.TxnType;
import org.apache.jena.rdfpatch.RDFChanges;
import org.apache.jena.sparql.JenaTransactionException;
import org.apache.jena.sparql.core.DatasetGraph;
import org.apache.jena.sparql.core.DatasetGraphWrapper;
import org.apache.jena.sparql.core.Quad;

/**
 * Connect a {@link DatasetGraph} with {@link RDFChanges}. All operations on the
 * {@link DatasetGraph} that cause changes have the change sent to the
 * {@link RDFChanges}.
 * <p>
 * Optionally, a sync handler can be given that is called on {@code sync()} or {@code begin}.
 * This class is stateless so updating the wrapped dataset is possible via the sync handler.
 * <p>
 * Synchronization can also be performed externally on the wrapped dataset.
 * <p>
 * Use {@link DatasetGraphRealChanges} to get a dataset that logs only changes that have a
 * real effect - that makes the changes log reversible (play delete for each add) to undo
 * a sequence of changes.
 *
 * @see DatasetGraphRealChanges
 * @see RDFChanges
 */
public class DatasetGraphChanges extends DatasetGraphWrapper {
    // Break up?
    // inherits DatasetGraphRealChanges < DatasetGraphAddDelete

    protected final Runnable syncHandler;
    protected final Consumer<ReadWrite> txnSyncHandler;
    protected final RDFChanges changesMonitor;
    private static Runnable identityRunnable = ()->{};
    private static <X> Consumer<X> identityConsumer() { return (x)->{}; }

    /** Create a {@code DatasetGraphChanges} which does not have any sync handlers */
    public DatasetGraphChanges(DatasetGraph dsg, RDFChanges monitor) {
        this(dsg, monitor, identityRunnable, identityConsumer());
    }

    /** Create a {@code DatasetGraphChanges} which calls different patch log synchronization
     *  handlers on {@link #sync} and {@link #begin}.
     *  {@code syncHandler} defaults (with null) to "no action".
     *
     *  Transactional usage preferred.
     */
    public DatasetGraphChanges(DatasetGraph dsg, RDFChanges changesMonitor, Runnable syncHandler, Consumer<ReadWrite> txnSyncHandler) {
        super(dsg);
        this.changesMonitor = changesMonitor;
        this.syncHandler = syncHandler == null ? identityRunnable : syncHandler;
        this.txnSyncHandler = txnSyncHandler == null ? identityConsumer() : txnSyncHandler;
    }

    public RDFChanges getMonitor() { return changesMonitor; }

    @Override public void sync() {
        syncHandler.run();
        if ( syncHandler != identityRunnable )
            super.sync();
    }

    @Override
    public void add(Quad quad) {
        add(quad.getGraph(), quad.getSubject(), quad.getPredicate(), quad.getObject());
    }

    @Override
    public void delete(Quad quad) {
        delete(quad.getGraph(), quad.getSubject(), quad.getPredicate(), quad.getObject());
    }

    @Override
    public void add(Node g, Node s, Node p, Node o) {
        requireWriteTxn();
        changesMonitor.add(g, s, p, o);
        super.add(g, s, p, o);
    }

    @Override
    public void delete(Node g, Node s, Node p, Node o) {
        requireWriteTxn();
        changesMonitor.delete(g, s, p, o);
        super.delete(g, s, p, o);
    }

    private void requireWriteTxn() {
        ReadWrite mode = transactionMode();
        if ( mode == ReadWrite.WRITE )
            return;
        boolean b = promote() ;
        if ( !b )
            throw new JenaTransactionException("Can't write") ;
    }

    @Override
    public Graph getDefaultGraph()
    { return new GraphChanges(get().getDefaultGraph(), null, changesMonitor) ; }

    @Override
    public Graph getGraph(Node graphNode)
    { return new GraphChanges(get().getGraph(graphNode), graphNode, changesMonitor) ; }

    @Override
    public void addGraph(Node graphName, Graph data) {
        removeGraph(graphName);
        data.find().forEachRemaining((t) -> add(graphName, t.getSubject(), t.getPredicate(), t.getObject()));
    }

    @Override
    public void removeGraph(Node graphName) {
        deleteAny(graphName, Node.ANY, Node.ANY, Node.ANY);
    }

    private static final int DeleteBufferSize = 10000;
    @Override
    /** Simple implementation but done without assuming iterator.remove() */
    public void deleteAny(Node g, Node s, Node p, Node o) {
        requireWriteTxn();
        Quad[] buffer = new Quad[DeleteBufferSize];
        while (true) {
            Iterator<Quad> iter = find(g, s, p, o);
            // Get a slice
            int len = 0;
            for ( ; len < DeleteBufferSize ; len++ ) {
                if ( !iter.hasNext() )
                    break;
                buffer[len] = iter.next();
            }
            // Delete them.
            for ( int i = 0 ; i < len ; i++ ) {
                delete(buffer[i]);
                buffer[i] = null;
            }
            // Finished?
            if ( len < DeleteBufferSize )
                break;
        }
    }

    // In case an implementation has one "begin" calling another.
    private ThreadLocal<Boolean> insideBegin = ThreadLocal.withInitial(()->false);

    @Override
    public void begin() {
        if ( insideBegin.get() ) {
            super.begin();
            return;
        }
        insideBegin.set(true);
        try {
            ReadWrite readWrite = transactionMode();
            // If a write, start the changedMonitor including get the patch log lock.
            if ( readWrite == ReadWrite.WRITE )
                changesMonitor.txnBegin();
            // For the sync, we have to assume it will write.
            // Any transaction causes a write-sync to be done in "begin".
            txnSyncHandler.accept(readWrite);
            super.begin();
        } finally {
            insideBegin.set(false);
        }
        internalBegin();
    }

    /** Called after begin and sync has occurred. */
    protected void internalBegin() {}

    @Override
    public void begin(TxnType txnType) {
        if ( insideBegin.get() ) {
            super.begin(txnType);
            return;
        }
        insideBegin.set(true);
        try {
          if ( txnType == TxnType.WRITE)
              changesMonitor.txnBegin();
          // For the sync, we have to assume it will write.
          // Sync: do as if a WRITE if the transaction is a "promote"
          // There isn't a sync during promotion.
          if ( txnType != TxnType.READ )
              txnSyncHandler.accept(ReadWrite.WRITE);
          else
              txnSyncHandler.accept(ReadWrite.READ);
          super.begin(txnType);
        } finally {
            insideBegin.set(false);
        }
        internalBegin();
    }

    @Override
    public void begin(ReadWrite readWrite) {
        TxnType txnType = TxnType.convert(readWrite);
        begin(txnType);
    }

    @Override
    public boolean promote() {
        // Do not use the wrapper code which will redirect to the wrapped DSG
        // bypassing promote(Promote) below.
        // This is copied :-( from "Transactional"
        TxnType txnType = transactionType();
        if ( txnType == null )
            throw new JenaTransactionException("Not in a transaction") ;
        switch(txnType) {
            case WRITE :                  return true;
            case READ :                   return false;
            case READ_PROMOTE :           return promote(Promote.ISOLATED);
            case READ_COMMITTED_PROMOTE : return promote(Promote.READ_COMMITTED);
        }
        throw new JenaTransactionException("Can't determine promote '"+txnType+"'transaction");
    }

    @Override
    public boolean promote(Promote type) {
        // Any potential write causes a write-sync to be done in "begin".
        // Here we are inside the transaction so calling the sync handler is not possible (nested transaction risk).
        if ( super.transactionMode() == ReadWrite.READ ) {
            boolean b = super.promote(type);
            if ( super.transactionMode() == ReadWrite.WRITE ) {
                // Promotion happened.
                // READ_PROMOTE would not reveal any new triples.
                // READ_COMMITTED_PROMOTE : can't atomically do local and remote "begin(write)"
                // Nested transaction. See above.
//                if ( transactionType() == TxnType.READ_COMMITTED_PROMOTE )
//                    txnSyncHandler.accept(ReadWrite.WRITE);
                // We have gone ReadWrite.READ -> ReadWrite.WRITE
                changesMonitor.txnBegin();
            }
            return b;
        }
        //Already WRITE.
        return super.promote(type);
    }

    @Override
    public void commit() {
        // Assume local commit will work - so signal first.
        // If the monitor.txnCommit fails, the commit should not happen
        if ( isWriteMode() ) {
            try {
                changesMonitor.txnCommit();
            } catch (Exception ex) {
                //Don't signal.  monitor.txnAbort() is a client-caused abort.
                super.abort();
                throw ex;
                //return;
            }
        }
        super.commit();
    }

    @Override
    public void abort() {
        // Assume abort will work - signal first.
        if ( isWriteMode() )
            changesMonitor.txnAbort();
        super.abort();
    }

    private boolean isWriteMode() {
        return super.transactionMode() == ReadWrite.WRITE;
    }

//    @Override
//    public void end() {
//        super.end();
//    }

}
