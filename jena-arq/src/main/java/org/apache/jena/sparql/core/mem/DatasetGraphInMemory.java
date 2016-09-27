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

package org.apache.jena.sparql.core.mem;

import static java.lang.ThreadLocal.withInitial;
import static org.apache.jena.graph.Node.ANY;
import static org.apache.jena.query.ReadWrite.READ;
import static org.apache.jena.query.ReadWrite.WRITE;
import static org.apache.jena.sparql.core.Quad.isUnionGraph;
import static org.apache.jena.sparql.util.graph.GraphUtils.triples2quadsDftGraph ;
import static org.slf4j.LoggerFactory.getLogger;

import java.util.Iterator;
import java.util.concurrent.atomic.AtomicLong ;
import java.util.concurrent.locks.ReentrantLock ;
import java.util.function.Consumer;
import java.util.function.Supplier;

import org.apache.jena.atlas.lib.InternalErrorException ;
import org.apache.jena.graph.Graph;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.Triple;
import org.apache.jena.query.ReadWrite;
import org.apache.jena.shared.Lock;
import org.apache.jena.shared.LockMRPlusSW;
import org.apache.jena.sparql.JenaTransactionException;
import org.apache.jena.sparql.core.* ;
import org.slf4j.Logger;

/**
 * A {@link DatasetGraph} backed by an {@link QuadTable}. By default, this is a
 * {@link HexTable} designed for high-speed in-memory operation.
 */
public class DatasetGraphInMemory extends DatasetGraphTriplesQuads implements Transactional {

    private static final Logger log = getLogger(DatasetGraphInMemory.class);

    private final DatasetPrefixStorage prefixes = new DatasetPrefixStorageInMemory();

    /** This lock imposes the multiple-reader and single-writer policy of transactions */
    private final Lock transactionLock = new LockMRPlusSW();

    /**
     * Transaction lifecycle operations must be atomic, especially
     * {@link Transactional#begin} and {@link Transactional#commit}.
     * <p>
     * There are changes to be made to several datastructures and this 
     * insures that they are made consistently.
     */
    private final ReentrantLock systemLock = new ReentrantLock(true);
    
    /**
     * Dataset version.
     * A write transaction increments this in commit.
     */
    private final AtomicLong generation = new AtomicLong(0) ;
    private final ThreadLocal<Long> version = withInitial(() -> 0L);

    private final ThreadLocal<Boolean> isInTransaction = withInitial(() -> false);

    @Override
    public boolean isInTransaction() {
        return isInTransaction.get();
    }

    protected void isInTransaction(final boolean b) {
        isInTransaction.set(b);
    }

    private final ThreadLocal<ReadWrite> transactionType = withInitial(() -> null);

    /**
     * @return the type of transaction in progress
     */
    public ReadWrite transactionType() {
        return transactionType.get();
    }

    protected void transactionType(final ReadWrite readWrite) {
        transactionType.set(readWrite);
    }

    private final QuadTable quadsIndex;

    private QuadTable quadsIndex() {
        return quadsIndex;
    }

    private final TripleTable defaultGraph;

    private TripleTable defaultGraph() {
        return defaultGraph;
    }

    /**
     * Default constructor.
     */
    public DatasetGraphInMemory() {
        this(new HexTable(), new TriTable());
    }

    /**
     * @param i a table in which to store quads
     * @param t a table in which to store triples
     */
    public DatasetGraphInMemory(final QuadTable i, final TripleTable t) {
        this.quadsIndex = i;
        this.defaultGraph = t;
    }
    
    @Override
    public boolean supportsTransactions()       { return true; }
    @Override
    public boolean supportsTransactionAbort()   { return true; }

    @Override
    public void begin(final ReadWrite readWrite) {
        if (isInTransaction()) 
            throw new JenaTransactionException("Transactions cannot be nested!");
        startTransaction(readWrite) ;
        _begin(readWrite) ;
    }

    private void _begin(ReadWrite readWrite) {
        withLock(systemLock, () ->{
            quadsIndex().begin(readWrite);
            defaultGraph().begin(readWrite);
            version.set(generation.get());
        }) ;
    }
    
    /** Called transaction start code at most once per transaction. */ 
    private void startTransaction(ReadWrite mode) {
        transactionLock.enterCriticalSection(mode.equals(READ)); // get the dataset write lock, if needed.
        transactionType(mode);
        isInTransaction(true);
    }

    /** Called transaction ending code at most once per transaction. */ 
    private void finishTransaction() {
        isInTransaction.remove();
        transactionType.remove();
        version.remove();
        transactionLock.leaveCriticalSection();
    }
     
    @Override
    public void commit() {
        if (!isInTransaction())
            throw new JenaTransactionException("Tried to commit outside a transaction!");
        if (transactionType().equals(WRITE))
            _commit();
        finishTransaction();
    }

    private void _commit() {
        withLock(systemLock, () -> {
            quadsIndex().commit();
            defaultGraph().commit();
            quadsIndex().end();
            defaultGraph().end();

            if ( transactionType().equals(WRITE) ) {
                if ( version.get() != generation.get() )
                    throw new InternalErrorException(String.format("Version=%d, Generation=%d",version.get(),generation.get())) ;
                generation.incrementAndGet() ;
            }
        } ) ;
    }
    
    @Override
    public void abort() {
        if (!isInTransaction()) 
            throw new JenaTransactionException("Tried to abort outside a transaction!");
        if (transactionType().equals(WRITE))
            _abort();
        finishTransaction();
    }

    private void _abort() {
        withLock(systemLock, () -> {
            quadsIndex().abort();
            defaultGraph().abort();
            quadsIndex().end();
            defaultGraph().end();
        } ) ;
    }
    
    @Override
    public void close() {
        if (isInTransaction())
            abort();
    }

    @Override
    public void end() {
        if (isInTransaction()) {
            if (transactionType().equals(WRITE)) {
                log.warn("end() called for WRITE transaction without commit or abort having been called. This causes a forced abort.");
                // _abort does _end actions inside the lock. 
                _abort() ;
            } else {
                _end() ;
            }
            finishTransaction();
        }
    }
    
    private void _end() {
        withLock(systemLock, () -> {
            quadsIndex().end();
            defaultGraph().end();
        } ) ;
    }
    
    private static void withLock(java.util.concurrent.locks.Lock lock, Runnable action) {
        lock.lock();
        try { action.run(); }
        finally {
            lock.unlock();
        }
    }
    
    private <T> Iterator<T> access(final Supplier<Iterator<T>> source) {
        if (!isInTransaction()) {
            begin(READ);
            try {
                return source.get();
            } finally {
                end();
            }
        }
        return source.get();
    }

    @Override
    public Iterator<Node> listGraphNodes() {
        return access(() -> quadsIndex().listGraphNodes().iterator());
    }

    private Iterator<Quad> quadsFinder(final Node g, final Node s, final Node p, final Node o) {
        if (isUnionGraph(g)) return findInUnionGraph$(s, p, o);
        return quadsIndex().find(g, s, p, o).iterator();
    }

    /**
     * Union graph is the merge of named graphs.
     */
    // Temp - Should this be replaced by DatasetGraphBaseFind code?
    private Iterator<Quad> findInUnionGraph$(final Node s, final Node p, final Node o) {
        return access(() -> quadsIndex().findInUnionGraph(s, p, o).iterator());
    }

    private Iterator<Quad> triplesFinder(final Node s, final Node p, final Node o) {
        return triples2quadsDftGraph(defaultGraph().find(s, p, o).iterator());
    }

    @Override
    public void setDefaultGraph(final Graph g) {
        mutate(graph -> {
            defaultGraph().clear();
            graph.find(ANY, ANY, ANY)
            .forEachRemaining(t -> addToDftGraph(t.getSubject(), t.getPredicate(), t.getObject()));
        } , g);
    }

    @Override
    public Graph getGraph(final Node graphNode) {
        return new GraphInMemory(this, graphNode);
    }

    @Override
    public Graph getDefaultGraph() {
        return getGraph(Quad.defaultGraphNodeGenerated);
    }

    private Consumer<Graph> addGraph(final Node name) {
        return g -> g.find(ANY, ANY, ANY).forEachRemaining(t -> add(new Quad(name, t)));
    }

    private final Consumer<Graph> removeGraph = g -> g.find(ANY, ANY, ANY).forEachRemaining(g::delete);

    @Override
    public void addGraph(final Node graphName, final Graph graph) {
        mutate(addGraph(graphName), graph);
    }

    @Override
    public void removeGraph(final Node graphName) {
        mutate(removeGraph, getGraph(graphName));
        prefixes().removeAllFromPrefixMap(graphName.getURI()) ;
    }

    /**
     * Wrap a mutation in a WRITE transaction iff necessary.
     *
     * @param mutator
     * @param payload
     */
    private <T> void mutate(final Consumer<T> mutator, final T payload) {
        if (!isInTransaction()) {
            begin(WRITE);
            try {
                mutator.accept(payload);
                commit();
            } finally {
                end();
            }
            return ;
        }
        if ( !transactionType().equals(WRITE) ) {
            if ( ! promotion )
                throw new JenaTransactionException("Tried to write inside a READ transaction!");
            promote(readCommittedPromotion) ;
        }
        mutator.accept(payload);
    }

    /*private*/public/*for development*/ static boolean promotion               = false ;
    /*private*/public/*for development*/ static boolean readCommittedPromotion  = true ;
    
    private void promote(boolean readCommited) {
        //System.err.printf("Promote: version=%d generation=%d\n", version.get() , generation.get()) ;
        
        // Outside lock.
        if ( ! readCommited && version.get() != generation.get() )  {
            // This tests for any commited writers since this transaction started.
            // This does not catch the case of a currently active writer
            // that has not gone to commit or abort yet.
            // The final test is after we obtain the transactionLock.
            throw new JenaTransactionException("Dataset changed - can't promote") ;
        }
       
        // Blocking on other writers.
        transactionLock.enterCriticalSection(false);
        // Check again now we are inside the lock. 
        if ( ! readCommited && version.get() != generation.get() )  {
                // Can't promote - release the lock.
                transactionLock.leaveCriticalSection();
                throw new JenaTransactionException("Concurrent writer changed the dataset : can't promote") ;
            }
        // We have the lock and we have promoted!
        transactionType(WRITE);
        _begin(WRITE) ;
    }

    /**
     * @return the prefixes in use in this dataset
     */
    public DatasetPrefixStorage prefixes() {
        return prefixes;
    }

    @Override
    public long size() {
        return quadsIndex().listGraphNodes().count() ;
    }

    @Override
    public void clear() {
        mutate(x -> {
            defaultGraph().clear();
            quadsIndex().clear();
        } , null);
    }

    @Override
    protected void addToDftGraph(final Node s, final Node p, final Node o) {
        mutate(defaultGraph()::add, Triple.create(s, p, o));
    }

    @Override
    protected void addToNamedGraph(final Node g, final Node s, final Node p, final Node o) {
        mutate(quadsIndex()::add, Quad.create(g, s, p, o));
    }

    @Override
    protected void deleteFromDftGraph(final Node s, final Node p, final Node o) {
        mutate(defaultGraph()::delete, Triple.create(s, p, o));
    }

    @Override
    protected void deleteFromNamedGraph(final Node g, final Node s, final Node p, final Node o) {
        mutate(quadsIndex()::delete, Quad.create(g, s, p, o));
    }

    @Override
    protected Iterator<Quad> findInDftGraph(final Node s, final Node p, final Node o) {
        return access(() -> triplesFinder(s, p, o));
    }

    @Override
    protected Iterator<Quad> findInSpecificNamedGraph(final Node g, final Node s, final Node p, final Node o) {
        return access(() -> quadsFinder(g, s, p, o));
    }

    @Override
    protected Iterator<Quad> findInAnyNamedGraphs(final Node s, final Node p, final Node o) {
        return findInSpecificNamedGraph(ANY, s, p, o);
    }
}
