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
import org.apache.jena.sparql.core.* ;
import org.apache.lucene.queryparser.classic.QueryParserBase ;
import org.slf4j.Logger ;
import org.slf4j.LoggerFactory ;

public class DatasetGraphText extends DatasetGraphMonitor implements Transactional
{
    private static Logger       log = LoggerFactory.getLogger(DatasetGraphText.class) ;
    private final TextIndex     textIndex ;
    private final Transactional dsgtxn ;
    private final Graph         dftGraph ;
    private final boolean       closeIndexOnClose;
    
    
    // If we are going to implement Transactional, then we are going to have to do as DatasetGraphWithLock and
    // TDB's DatasetGraphTransaction do and track transaction state in a ThreadLocal
    private final ThreadLocal<ReadWrite> readWriteMode = new ThreadLocal<ReadWrite>();
    
    
    public DatasetGraphText(DatasetGraph dsg, TextIndex index, TextDocProducer producer)
    { 
        this(dsg, index, producer, false);
    }
    
    public DatasetGraphText(DatasetGraph dsg, TextIndex index, TextDocProducer producer, boolean closeIndexOnClose)
    {
        super(dsg, producer) ;
        this.textIndex = index ;
        if ( dsg instanceof Transactional )
            dsgtxn = (Transactional)dsg ;
        else
            dsgtxn = new DatasetGraphWithLock(dsg) ;
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
        queryString = QueryParserBase.escape(queryString) ;
        if ( predicate != null ) {
            String f = textIndex.getDocDef().getField(predicate) ;
            queryString = f + ":" + queryString ;
        }
        List<TextHit> results = textIndex.query(predicate, queryString, limit) ;
        return results.iterator() ;
    }

    @Override
    public void begin(ReadWrite readWrite) {
        readWriteMode.set(readWrite);
        dsgtxn.begin(readWrite) ;
        super.getMonitor().start() ;
    }
    
    /**
     * Rollback all changes, discarding any exceptions that occur.
     */
    @Override
    public void abort() {
        // Roll back all both objects, discarding any exceptions that occur
        try { dsgtxn.abort(); } catch (Throwable t) { log.warn("Exception in abort: " + t.getMessage(), t); }
        try { textIndex.rollback(); } catch (Throwable t) { log.warn("Exception in abort: " + t.getMessage(), t); }
        
        readWriteMode.set(null) ;
        super.getMonitor().finish() ;
    }
    
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
        super.getMonitor().finish() ;
        // Phase 1
        if (readWriteMode.get() == ReadWrite.WRITE) {
            try {
                textIndex.prepareCommit();
            }
            catch (Throwable t) {
                log.error("Exception in prepareCommit: " + t.getMessage(), t) ;
                abort();
                throw new TextIndexException(t);
            }
        }
        
        // Phase 2
        try {
            dsgtxn.commit();
            if (readWriteMode.get() == ReadWrite.WRITE) {
                textIndex.commit();
            }
        }
        catch (Throwable t) {
            log.error("Exception in commit: " + t.getMessage(), t) ;
            abort();
            throw new TextIndexException(t);
        }
        readWriteMode.set(null);
    }

    @Override
    public boolean isInTransaction() {
        return readWriteMode.get() != null;
    }

    @Override
    public void end() {
        // If we are still in a write transaction at this point, then commit was never called, so rollback the TextIndex
        if (readWriteMode.get() == ReadWrite.WRITE) {
            try {
                textIndex.rollback();
            }
            catch (Throwable t) {
                log.warn("Exception in end: " + t.getMessage(), t) ;
            }
        }
        
        try {
            dsgtxn.end() ;
        }
        catch (Throwable t) {
            log.warn("Exception in end: " + t.getMessage(), t) ;
        }
        
        readWriteMode.set(null) ;
        super.getMonitor().finish() ;
    }
    
    @Override
    public void close() {
        super.close();
        if (closeIndexOnClose) {
            textIndex.close();
        }
    }
    
}
