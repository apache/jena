/*
 * (c) Copyright 2010 Talis Systems Ltd.
 * All rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.tdb.store.bulkloader;

import org.openjena.atlas.event.EventType ;
import org.slf4j.Logger ;
import org.slf4j.LoggerFactory ;

import com.hp.hpl.jena.graph.Node ;
import com.hp.hpl.jena.graph.Triple ;
import com.hp.hpl.jena.sparql.core.Quad ;
import com.hp.hpl.jena.tdb.nodetable.NodeTupleTable ;
import com.hp.hpl.jena.tdb.nodetable.NodeTupleTableView ;
import com.hp.hpl.jena.tdb.store.DatasetGraphTDB ;

/** Overall framework for bulk loading */
public class BulkLoader
{
    // Coordinate the NodeTupleTable loading.

    /** Tick point for messages during loading of data */
    public static final int LoadTickPoint = 1000 ;
    /** Tick point for messages during secondary index creation */
    public static final long IndexTickPoint = 5000 ;
    
    /** Number of ticks per super tick */
    public static int superTick = 2 ;
    
    // Events.
    //private static String baseNameGeneral = "http://openjena.org/TDB/event#" ;

    private static String baseName = "http://openjena.org/TDB/bulkload/event#" ;
    
    
    public static EventType evStartBulkload = new EventType(baseName+"start-bulkload") ;
    public static EventType evFinishBulkload = new EventType(baseName+"finish-bulkload") ;

    public static EventType evStartDataBulkload = new EventType(baseName+"start-bulkload-data") ;
    public static EventType evFinishDataBulkload = new EventType(baseName+"finish-bulkload-data") ;
    
    public static EventType evStartIndexBulkload = new EventType(baseName+"start-bulkload-index") ;
    public static EventType evFinishIndexBulkload = new EventType(baseName+"finish-bulkload-index") ;
    
    static private Logger loadLogger = LoggerFactory.getLogger("com.hp.hpl.jena.tdb.loader") ;
    
    // Event callbacks for the load stages?
    // On what object?  The dataset.
    
    /** Load into default graph */
    public static Destination<Triple> loadTriples(DatasetGraphTDB dsg, boolean showProgress)
    {
        return loadTriples(dsg, dsg.getTripleTable().getNodeTupleTable(), showProgress) ;
    }

    /** Load into named graph */
    public static Destination<Triple> loadTriples(DatasetGraphTDB dsg, Node graphName, boolean showProgress)
    {
        NodeTupleTable ntt = dsg.getQuadTable().getNodeTupleTable() ;
        NodeTupleTable ntt2 = new NodeTupleTableView(ntt, graphName) ;
        return loadTriples(dsg, ntt2, showProgress) ;
    }

    private static Destination<Triple> loadTriples(final DatasetGraphTDB dsg, NodeTupleTable nodeTupleTable, final boolean showProgress)
    {
        final LoadMonitor monitor = (showProgress ? 
                                       new LoadMonitor(dsg, loadLogger, LoadTickPoint, IndexTickPoint) :
                                       new LoadMonitor(dsg, null, LoadTickPoint, IndexTickPoint) ) ;

        final LoaderNodeTupleTable x = new LoaderNodeTupleTable(dsg.getTripleTable().getNodeTupleTable(),
                                                                "triples",
                                                                monitor) ;
        
        Destination<Triple> sink = new Destination<Triple>() {
            long count = 0 ;
            public void start()
            {
                x.loadStart() ;
            }
            public void send(Triple triple)
            {
                x.load(triple.getSubject(), triple.getPredicate(),  triple.getObject()) ;
                count++ ;
            }

            public void flush() { }
            public void close() { }

            public void finish()
            {
                x.loadFinish() ;
            }
        } ;
        return sink ;
    }

    public static Destination<Quad> loadQuads(final DatasetGraphTDB dsg, final boolean showProgress)
    {
        final LoadMonitor monitor = (showProgress ? 
                                       new LoadMonitor(dsg, loadLogger, LoadTickPoint, IndexTickPoint) :
                                       new LoadMonitor(dsg, null, LoadTickPoint, IndexTickPoint) ) ;

        
        final LoaderNodeTupleTable loaderTriples = new LoaderNodeTupleTable(
                                                                dsg.getTripleTable().getNodeTupleTable(),
                                                                "triples",
                                                                monitor) ;
        final LoaderNodeTupleTable loaderQuads = new LoaderNodeTupleTable( 
                                                                 dsg.getQuadTable().getNodeTupleTable(),
                                                                 "quads",
                                                                 monitor) ;
        Destination<Quad> sink = new Destination<Quad>() {
            long count = 0 ;
            public void start()
            {
                loaderTriples.loadStart() ;
                loaderQuads.loadStart() ;
            }
            
            public void send(Quad quad)
            {
                if ( quad.isTriple() || quad.isDefaultGraph() )
                    loaderTriples.load(quad.getSubject(), quad.getPredicate(),  quad.getObject()) ;
                else
                    loaderQuads.load(quad.getGraph(), quad.getSubject(), quad.getPredicate(),  quad.getObject()) ;
                count++ ;
            }

            public void finish()
            {
                loaderTriples.loadFinish() ;
                loaderQuads.loadFinish() ;
            }
            
            public void flush() { }
            public void close() { }
        } ;
        return sink ;
    }
}

/*
 * (c) Copyright 2010 Talis Systems Ltd.
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in the
 *    documentation and/or other materials provided with the distribution.
 * 3. The name of the author may not be used to endorse or promote products
 *    derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE AUTHOR ``AS IS'' AND ANY EXPRESS OR
 * IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.
 * IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR ANY DIRECT, INDIRECT,
 * INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT
 * NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 * THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF
 * THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */