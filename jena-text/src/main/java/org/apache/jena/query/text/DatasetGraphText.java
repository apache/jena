/**
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

package org.apache.jena.query.text ;

import java.util.Iterator ;
import java.util.List ;

import org.apache.jena.graph.Graph ;
import org.apache.jena.graph.Node ;
import org.apache.jena.query.ReadWrite ;
import org.apache.jena.query.TxnType;
import org.apache.jena.sparql.core.* ;
import org.apache.lucene.queryparser.classic.QueryParserBase ;
import org.slf4j.Logger ;
import org.slf4j.LoggerFactory ;

public class DatasetGraphText extends DatasetGraphMonitor implements Transactional
{
    private static Logger       log = LoggerFactory.getLogger(DatasetGraphText.class) ;
    private final TextIndex     textIndex ;
    private final Graph         dftGraph ;
    private final boolean       closeIndexOnClose;
    // Lock needed for commit/abort that perform an index operation and a dataset operation
    // which need it happen without a W thread coming in between them.
    // JENA-1302.
    private final Object        txnExitLock = new Object();
    
    // If we are going to implement Transactional, then we are going to have to do as DatasetGraphWithLock and
    // TDB's DatasetGraphTransaction do and track transaction state in a ThreadLocal
    private final ThreadLocal<ReadWrite> readWriteMode = new ThreadLocal<>();
    
    public DatasetGraphText(DatasetGraph dsg, TextIndex index, TextDocProducer producer)
    { 
        this(dsg, index, producer, false);
    }
    
    public DatasetGraphText(DatasetGraph dsg, TextIndex index, TextDocProducer producer, boolean closeIndexOnClose)
    {
        super(dsg, producer) ;
        this.textIndex = index ;
        dftGraph = GraphView.createDefaultGraph(this) ;
        this.closeIndexOnClose = closeIndexOnClose;
    }

    // ---- Intercept these and force the use of views.
    @Override
    public Graph getDefaultGraph() {
        return dftGraph ;
    }

    @Override
    public Graph getGraph(Node graphNode) {
        return GraphView.createNamedGraph(this, graphNode) ;
    }

    // ----

    public TextIndex getTextIndex() {
        return textIndex ;
    }

    /** Search the text index on the default text field */
    public Iterator<TextHit> search(String queryString) {
        return search(queryString, null) ;
    }

    /** Search the text index on the text field associated with the predicate */
    public Iterator<TextHit> search(String queryString, Node predicate) {
        return search(queryString, predicate, -1) ;
    }

    /** Search the text index on the default text field */
    public Iterator<TextHit> search(String queryString, int limit) {
        return search(queryString, null, limit) ;
    }

    /** Search the text index on the text field associated with the predicate */
    public Iterator<TextHit> search(String queryString, Node predicate, int limit) {
        return search(queryString, predicate, null, null, limit) ;
    }

    /** Search the text index on the text field associated with the predicate within graph */
    public Iterator<TextHit> search(String queryString, Node predicate, String graphURI, String lang, int limit) {
        queryString = QueryParserBase.escape(queryString) ;
        if ( predicate != null ) {
            String f = textIndex.getDocDef().getField(predicate) ;
            queryString = f + ":" + queryString ;
        }
        List<TextHit> results = textIndex.query(predicate, queryString, graphURI, lang, limit) ;
        return results.iterator() ;
    }

    @Override
    public void begin(TxnType txnType) {
        switch(txnType) {
            case READ_PROMOTE:
            case READ_COMMITTED_PROMOTE:
                throw new UnsupportedOperationException("begin("+txnType+")");
            default:
        }
        begin(TxnType.convert(txnType));
    }
    
    @Override
    public void begin(ReadWrite readWrite) {
        // Do not synchronized(txnLock) here. It will deadlock because if there
        // is an writer in commit, it can't 
        
        // The "super.begin" is enough.
        readWriteMode.set(readWrite);
        super.begin(readWrite) ;
        super.getMonitor().start() ;
    }
    
    // JENA-1302 :: txnExitLock
    // We need to 
    //   textIndex.prepareCommit();
    //   super.commit();
    //   textIndex.commit();
    // without another thread getting in.
    
    // Concurrency control most of the time is because we use the transaction
    // capability of the wrapped dataset but here we need to do an action before
    // wrapped dataset commit and also an action after.
    // 
    // At the point of super.commit, it let in a new writer in begin() which
    // races to commit before text index commit.
    //
    // txnExitLock extends the time of exclusive access.    
    
    /**
     * Perform a 2-phase commit by first calling prepareCommit() on the TextIndex
     * followed by committing the Transaction object, and then calling commit()
     * on the TextIndex().
     * <p> 
     * If either of the objects fail on either the preparation or actual commit,
     * it terminates and calls {@link #abort()} on both of them.
     * <p>
     * <b>NOTE:</b> it may happen that the TextIndex fails to commit, after the
     * Transactional has already successfully committed.  A rollback instruction will
     * still be issued, but depending on the implementation, it may not have any effect.
     */
    @Override
    public void commit() {
        if (readWriteMode.get() == ReadWrite.WRITE)
            commit_W();
        else
            commit_R();
    }
    
    
    private void commit_R() {
        // No index action needed.
        super.getMonitor().finish() ;
        super.commit();
        readWriteMode.set(null);
    }

    private void commit_W() {
        synchronized(txnExitLock) {
            super.getMonitor().finish() ;
            // Phase 1
            try { textIndex.prepareCommit(); }
            catch (Throwable t) {
                log.error("Exception in prepareCommit: " + t.getMessage(), t) ;
                abort();
                throw new TextIndexException(t);
            }
            
            // Phase 2
            try {
                super.commit();
                textIndex.commit();
            }
            catch (Throwable t) {
                log.error("Exception in commit: " + t.getMessage(), t) ;
                abort();
                throw new TextIndexException(t);
            }
            readWriteMode.set(null);
        }
    }

    /**
     * Rollback all changes, discarding any exceptions that occur.
     */
    @Override
    public void abort() {
        if (readWriteMode.get() == ReadWrite.WRITE)
            abort_W();
        else
            abort_R();
    }
    
    private void abort_R() {
        super.getMonitor().finish() ;
        try { super.abort() ; }
        catch (Throwable t) { log.warn("Exception in abort: " + t.getMessage(), t); }
        readWriteMode.set(null) ;
    }
    
    private void abort_W() {
        synchronized(txnExitLock) {
            super.getMonitor().finish() ;
            // Roll back on both objects, discarding any exceptions that occur
            try { super.abort(); } catch (Throwable t) { log.warn("Exception in abort: " + t.getMessage(), t); }
            try { textIndex.rollback(); } catch (Throwable t) { log.warn("Exception in abort: " + t.getMessage(), t); }
            readWriteMode.set(null) ;
        }
    }

    @Override
    public boolean isInTransaction() {
        return readWriteMode.get() != null;
    }

    @Override
    public void end() {
        if ( ! isInTransaction() ) {
            super.end() ;
            return;
        }
        if (readWriteMode.get() == ReadWrite.WRITE) {
            // If we are still in a write transaction at this point, then commit
            // was never called, so rollback the TextIndex and the dataset.
            abort();
        }
        super.end() ;
        super.getMonitor().finish() ;
        readWriteMode.set(null) ;
    }
    
    @Override
    public boolean supportsTransactions() {
        return super.supportsTransactions() ;
    }
    
    /** Declare whether {@link #abort} is supported.
     *  This goes along with clearing up after exceptions inside application transaction code.
     */
    @Override
    public boolean supportsTransactionAbort() {
        return super.supportsTransactionAbort() ;
    }
    
    @Override
    public void close() {
        super.close();
        if (closeIndexOnClose) {
            textIndex.close();
        }
    }
}
