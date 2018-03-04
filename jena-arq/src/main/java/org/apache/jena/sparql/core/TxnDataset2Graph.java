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

package org.apache.jena.sparql.core;

import java.util.*;
import java.util.function.Consumer;

import org.apache.jena.graph.Graph;
import org.apache.jena.graph.TransactionHandler;
import org.apache.jena.query.ReadWrite;
import org.apache.jena.query.TxnType;
import org.apache.jena.reasoner.InfGraph;
import org.apache.jena.shared.LockMRSW;
import org.apache.jena.sparql.JenaTransactionException;

/**
 * A {@link Transactional} that passes the transaction operations down to transactions on
 * independent graphs.
 * <p>
 * There are limitations:
 * <ul>
 * <li>we can't atomically do all the commits together in the crash situation.
 * <li>This {@code Transactional} maintains a MRSW policy because that is all that is
 * required of graphs in general.
 * </ul>
 * It does cover the important case of one graph ({@link DatasetGraphOne}) where the one
 * graph is an InfGraph and should work when the graphs in the dataset is not changing or
 * when a new memory graph is added mid-transaction.
 * <p>
 * This is not "nested transactions" - theer is no overall "commit" or "abort". If
 * failure/restart occurs, some graphs may have commited and others not. It is the best
 * that can be done given for an arbitrary collection of graphs, backed by different
 * storage and having different capabilities.
 * <p>
 * Best practice is to change the graph membership outside of any transaction,
 * ideally at setup time of the object using this class. (Caution: SPARQL Update
 * can create graphs.   
 * @See {@link DatasetGraphMapLink}
 * @See {@link DatasetGraphOne}
 */
public class TxnDataset2Graph extends TransactionalLock {
    /**
     * Control whether to pass down transactions from the dataset to the graph in the
     * dataset. This should be set to "true"; setting it "false" causes the onld,
     * no-transaction passing behaviour.
     * <p>
     * This is temporary flag during the transition because the change at Jena 3.7.0 needs
     * to be proven in real deployments as well as testing. "false" restores the Jena
     * 3.6.0 and before behaviour (transactions not passed down). See JENA-1492.
     * 
     * @deprecated This flag will be removed.
     */
    @Deprecated
    public static boolean TXN_DSG_GRAPH = true;
    
    private Graph primary;
    // Object key may be a graph or a DSG is the graph is a GraphView.
    // This avoids starting a tranasction on the same storage unit twice. 
    private Map<Object, TransactionHandler> handlers = new HashMap<>();
        
    private Object lock = new Object();
    
    public TxnDataset2Graph(Graph primaryGraph, Graph ... otherGraphs) {
        super(new LockMRSW());
        primary = primaryGraph;
        handlers = buildHandlerSet(primary, Arrays.asList(otherGraphs));
    }
    
    private static Map<Object, TransactionHandler> buildHandlerSet(Graph primary, Collection<Graph> graphs) {
        Map<Object, TransactionHandler> handlers = new HashMap<>();
        addHandler(handlers, primary);
        graphs.forEach(g->addHandler(handlers,g));
        return handlers;
    }
    
    private static void addHandler(Map<Object, TransactionHandler> handlers, Graph graph) {
        TransactionHandler th = graph.getTransactionHandler();
        if ( ! th.transactionsSupported() )
            return;
        Object key = calcKey(graph);
        if ( th.transactionsSupported() )
            handlers.put(key, th) ;
    }

    // Determine the key - an object that is the unit of transactions.
    // For two graphs form the same DatasetGraph, i.e. GraphView, there should be one transaction.
    private static Object calcKey(Graph graph) {
        if ( graph instanceof GraphView )
            // Use the database as the key so that transactions are started once-per-storage.
            // This the case of a graph from some storage being plavced in a general dataset.  
            return ((GraphView)graph).getDataset();
        if ( graph instanceof InfGraph )
            // InfGraph does actual pass done in its TransactionHandler.
            // This allows the base graph to be included in the dataset as well as the InfGraph. 
            return calcKey(((InfGraph)graph).getRawGraph());
        
//        if ( graph instanceof GraphWrapper )
//            return calcKey(((GraphWrapper)graph).get());
//        if ( graph instanceof WrappedGraph )
//            return calcKey(((WrappedGraph)graph).getWrapped());
        return graph;
    }

    private static void removeHandler(Map<Object, TransactionHandler> handlers, Graph graph) {
        Object key = calcKey(graph);
        handlers.remove(graph);
    }

    // Attempt to manage the graph transactions during a transaction.
    // Imperfect for removal, we don't know whether to call commit() or abort().
    // Works for adding.
    // Generally better not to change the graphs during a transaction, just set them once
    // on creation.
    
    public void addGraph(Graph graph) {
        checkNotReadMode();
        if ( graph == null )
            return;
        if ( ! handlers.containsKey(graph) ) {
            // Add if new.
            addHandler(handlers, graph) ;
            if ( super.isInTransaction() ) {
                // If we are in a transaction, start the subtransaction. 
                TransactionHandler th = handlers.get(graph);
                if ( th != null )
                    th.begin();
            }
        }
    }
    
    public void removeGraph(Graph graph) {
        checkNotReadMode();
        if ( graph == null )
            return;
        if ( ! super.isInTransaction() ) {
            // Not in transaction, do now. 
            removeHandler(handlers, graph);
            return;
        }
        // Queue to be removed at the end.
        Set<Graph> toBeRemoved = removedGraphs.get();
        if ( toBeRemoved == null ) {
            // Lazy set of the HashSet. 
            toBeRemoved = new HashSet<>();
            removedGraphs.set(toBeRemoved);
        }
        removedGraphs.get().add(graph);
    }

    public void setPrimaryGraph(Graph graph) {
        checkNotReadMode();
        if ( graph == null )
            return;
        removeGraph(graph);
        addGraph(graph);
    }
    
    private void handlers(Consumer<TransactionHandler> action) {
        synchronized (lock) {
            handlers.forEach((g,th)->action.accept(th));
        }
    }

    private void checkNotReadMode() {
        if ( !super.isInTransaction() )
            return;
        if ( super.isTransactionMode(ReadWrite.READ) )
            throw new JenaTransactionException("In READ mode in transaction");
    }

    private ThreadLocal<Set<Graph>> removedGraphs = ThreadLocal.withInitial(()->null);
    private void start() {}
    private void finish() {
        if ( ! super.isTransactionMode(ReadWrite.WRITE) )
            return;
        // This is called inside the lock of super.
        Set<Graph> toBeRemoved = removedGraphs.get();
        removedGraphs.remove();
        if ( toBeRemoved == null )
            return ;
        toBeRemoved.forEach(g->removeHandler(handlers, g));
    }
    
    // TransactionalLock.begin(ReadWrite) calls begin(TxnType)
    @Override
    public void begin(TxnType type) {
        super.begin(type);
        // Whatever the type. Graph Transactions do not allow for "read-only".
        start();
        handlers(h->h.begin());
    }

    // The MRSW lock means this isn't possible. See super.promote.
//    @Override
//    public boolean promote(Promote mode)

    @Override
    public void commit() {
        handlers(h->h.commit());
        // Before super.commit - we stil hold the lock.
        finish();
        super.commit();
    }

    @Override
    public void abort() {
        handlers(h -> h.abort());
        finish();
        super.abort();
    }
}
