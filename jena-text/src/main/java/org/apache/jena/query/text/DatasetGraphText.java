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

import org.apache.lucene.queryparser.classic.QueryParserBase ;
import org.slf4j.Logger ;
import org.slf4j.LoggerFactory ;

import com.hp.hpl.jena.graph.Graph ;
import com.hp.hpl.jena.graph.Node ;
import com.hp.hpl.jena.query.ReadWrite ;
import com.hp.hpl.jena.sparql.JenaTransactionException ;
import com.hp.hpl.jena.sparql.core.* ;

public class DatasetGraphText extends DatasetGraphMonitor implements Transactional
{
    private static Logger       log = LoggerFactory.getLogger(DatasetGraphText.class) ;
    private final TextIndex     textIndex ;
    private final Transactional dsgtxn ;
    private final Graph         dftGraph ;

    public DatasetGraphText(DatasetGraph dsg, TextIndex index, TextDocProducer producer)
    {
        super(dsg, producer) ;
        this.textIndex = index ;
        if ( dsg instanceof Transactional )
            dsgtxn = (Transactional)dsg ;
        else
            dsgtxn = new DatasetGraphWithLock(dsg) ;
        dftGraph = GraphView.createDefaultGraph(this) ;
    }

    // ---- Intecept these and force the use of views.
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
    public Iterator<Node> search(String queryString) {
        return search(queryString, null) ;
    }

    /** Search the text index on the text field associated with the predicate */
    public Iterator<Node> search(String queryString, Node predicate) {
        return search(queryString, predicate, -1) ;
    }

    /** Search the text index on the default text field */
    public Iterator<Node> search(String queryString, int limit) {
        return search(queryString, null, limit) ;
    }

    /** Search the text index on the text field associated with the predicate */
    public Iterator<Node> search(String queryString, Node predicate, int limit) {
        queryString = QueryParserBase.escape(queryString) ;
        if ( predicate != null ) {
            String f = textIndex.getDocDef().getField(predicate) ;
            queryString = f + ":" + queryString ;
        }
        List<Node> results = textIndex.query(queryString, limit) ;
        return results.iterator() ;
    }

    // Imperfect.
    private boolean needFinish = false ;

    @Override
    public void begin(ReadWrite readWrite) {
        dsgtxn.begin(readWrite) ;
        // textIndex.begin(readWrite) ;
        if ( readWrite == ReadWrite.WRITE ) {
            // WRONG design
            super.getMonitor().start() ;
            // Right design.
            // textIndex.startIndexing() ;
            needFinish = true ;
        }
    }

    @Override
    public void commit() {
        try {
            if ( needFinish ) {
                super.getMonitor().finish() ;
                // textIndex.finishIndexing() ;
            }
            needFinish = false ;
            // textIndex.commit() ;
            dsgtxn.commit() ;
        }
        catch (Throwable ex) {
            log.warn("Exception in commit: " + ex.getMessage(), ex) ;
            dsgtxn.abort() ;
        }
    }

    @Override
    public void abort() {
        try {
            if ( needFinish )
                textIndex.abortIndexing() ;
            dsgtxn.abort() ;
        }
        catch (JenaTransactionException ex) { throw ex ; }
        catch (RuntimeException ex) { 
            log.warn("Exception in abort: " + ex.getMessage(), ex) ;
            throw ex ;
        }
    }

    @Override
    public boolean isInTransaction() {
        return dsgtxn.isInTransaction() ;
    }

    @Override
    public void end() {
        try {
            // textIndex.end() ;
            dsgtxn.end() ;
        }
        catch (Throwable ex) { log.warn("Exception in end: " + ex.getMessage(), ex) ; }
    }
}
