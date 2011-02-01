/*
 * (c) Copyright 2010 Talis Systems Ltd.
 * All rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.tdb.store.bulkloader;

import static org.openjena.riot.Lang.NQUADS ;
import static org.openjena.riot.Lang.NTRIPLES ;

import java.io.InputStream ;
import java.util.List ;

import org.openjena.atlas.event.EventType ;
import org.openjena.riot.RiotReader ;
import org.slf4j.Logger ;

import com.hp.hpl.jena.graph.Node ;
import com.hp.hpl.jena.graph.Triple ;
import com.hp.hpl.jena.sparql.core.Quad ;
import com.hp.hpl.jena.sparql.util.Utils ;
import com.hp.hpl.jena.tdb.TDB ;
import com.hp.hpl.jena.tdb.nodetable.NodeTupleTable ;
import com.hp.hpl.jena.tdb.nodetable.NodeTupleTableView ;
import com.hp.hpl.jena.tdb.store.DatasetGraphTDB ;

/** Overall framework for bulk loading */
public class BulkLoader
{
    // Coordinate the NodeTupleTable loading.

    /** Tick point for messages during loading of data */
    public static int DataTickPoint = 50*1000 ;
    /** Tick point for messages during secondary index creation */
    public static long IndexTickPoint = 100*1000 ;
    
    /** Number of ticks per super tick */
    public static int superTick = 10 ;
    
    // Events.
    //private static String baseNameGeneral = "http://openjena.org/TDB/event#" ;

    private static String baseName = "http://openjena.org/TDB/bulkload/event#" ;
    
    
    public static EventType evStartBulkload = new EventType(baseName+"start-bulkload") ;
    public static EventType evFinishBulkload = new EventType(baseName+"finish-bulkload") ;

    public static EventType evStartDataBulkload = new EventType(baseName+"start-bulkload-data") ;
    public static EventType evFinishDataBulkload = new EventType(baseName+"finish-bulkload-data") ;
    
    public static EventType evStartIndexBulkload = new EventType(baseName+"start-bulkload-index") ;
    public static EventType evFinishIndexBulkload = new EventType(baseName+"finish-bulkload-index") ;
    
    
    static private Logger loadLogger = TDB.logLoader ;
    
    // Event callbacks for the load stages?
    // On what object?  The dataset.

//    /** Load into default graph */
//    public static void loadTriples(DatasetGraphTDB dsg, String url, boolean showProgress)
//    {
//        loadTriples(dsg, asList(url) , showProgress) ;
//    }
    
    /** Load into default graph */
    public static void loadDefaultGraph(DatasetGraphTDB dsg, List<String> urls, boolean showProgress)
    {
        Destination<Triple> dest = destinationDefaultGraph(dsg, showProgress) ;
        loadTriples$(dest, urls) ;
    }

    /** Load into default graph */
    public static void loadDefaultGraph(DatasetGraphTDB dsg, InputStream input, boolean showProgress)
    {
        Destination<Triple> dest = destinationDefaultGraph(dsg, showProgress) ;
        loadTriples$(dest, input) ;
    }

    private static Destination<Triple> destinationDefaultGraph(DatasetGraphTDB dsg, boolean showProgress)
    {
        NodeTupleTable ntt = dsg.getTripleTable().getNodeTupleTable() ;
        return destination(dsg, ntt, showProgress) ;
    }

    /** Load into named graph */
    public static void loadNamedGraph(DatasetGraphTDB dsg, Node graphNode, List<String> urls, boolean showProgress)
    {
        Destination<Triple> dest = destinationNamedGraph(dsg, graphNode, showProgress) ;
        loadTriples$(dest, urls) ;
    }
    
    /** Load into named graph */
    public static void loadNamedGraph(DatasetGraphTDB dsg, Node graphNode, InputStream input, boolean showProgress)
    {
        Destination<Triple> dest = destinationNamedGraph(dsg, graphNode, showProgress) ;
        loadTriples$(dest, input) ;
    }

    /** Load into a dataset */
    public static void loadDataset(DatasetGraphTDB dsg, List<String> urls, boolean showProgress)
    {
        Destination<Quad> dest = destinationDataset(dsg, showProgress) ;
        loadQuads$(dest, urls) ;
    }
    
    /** Load into a dataset */
    public static void loadDataset(DatasetGraphTDB dsg, InputStream input, boolean showProgress)
    {
        Destination<Quad> dest = destinationDataset(dsg, showProgress) ;
        loadQuads$(dest, input) ;
    }
    

    /** Load into a graph */
    private static void loadTriples$(Destination<Triple> dest, List<String> urls)
    {
        dest.start() ;
        for ( String url : urls )
        {
            loadLogger.info("Load: "+url+" -- "+Utils.nowAsString()) ;
            RiotReader.parseTriples(url, dest) ;
        }            
        dest.finish() ;
    }

    /** Load into a graph */
    private static void loadTriples$(Destination<Triple> dest, InputStream input)
    {
        loadLogger.info("Load: from input stream -- "+Utils.nowAsString()) ;
        dest.start() ;
        RiotReader.parseTriples(input, NTRIPLES, null, dest) ;
        dest.finish() ;
    }
    
    /** Load quads into a dataset */
    private static void loadQuads$(Destination<Quad> dest, List<String> urls)
    {
        dest.start() ;
        for ( String url : urls )
        {
            loadLogger.info("Load: "+url+" -- "+Utils.nowAsString()) ;
            RiotReader.parseQuads(url, dest) ;
        }
        dest.finish() ;
    }

    /** Load quads into a dataset */
    private static void loadQuads$(Destination<Quad> dest, InputStream input)
    {
        loadLogger.info("Load: from input stream -- "+Utils.nowAsString()) ;
        dest.start() ;
        RiotReader.parseQuads(input, NQUADS, null, dest) ;
        dest.finish() ;
    }
    
    private static Destination<Triple> destinationNamedGraph(DatasetGraphTDB dsg, Node graphName, boolean showProgress)
    {
        if ( graphName == null )
            return destinationDefaultGraph(dsg,showProgress) ;
        
        NodeTupleTable ntt = dsg.getQuadTable().getNodeTupleTable() ;
        NodeTupleTable ntt2 = new NodeTupleTableView(ntt, graphName) ;
        return destination(dsg, ntt2, showProgress) ;
    }

    private static LoadMonitor createLoadMonitor(DatasetGraphTDB dsg, String itemName, boolean showProgress)
    {
        if ( showProgress ) 
            return new LoadMonitor(dsg, loadLogger, itemName, DataTickPoint, IndexTickPoint) ;
        else
            return new LoadMonitor(dsg, null, itemName, DataTickPoint, IndexTickPoint) ; 
    }
    
    private static Destination<Triple> destination(final DatasetGraphTDB dsg, NodeTupleTable nodeTupleTable, final boolean showProgress)
    {
        LoadMonitor monitor = createLoadMonitor(dsg, "triples", showProgress) ;
        final LoaderNodeTupleTable loaderTriples = new LoaderNodeTupleTable(nodeTupleTable, "triples", monitor) ;
        
        Destination<Triple> sink = new Destination<Triple>() {
            long count = 0 ;
            final public void start()
            {
                loaderTriples.loadStart() ;
                loaderTriples.loadDataStart() ;
            }
            final public void send(Triple triple)
            {
                loaderTriples.load(triple.getSubject(), triple.getPredicate(),  triple.getObject()) ;
                count++ ;
            }

            final public void flush() { }
            public void close() { }

            final public void finish()
            {
                loaderTriples.loadDataFinish() ;
                loaderTriples.loadIndexStart() ;
                loaderTriples.loadIndexFinish() ;
                loaderTriples.loadFinish() ;
                dsg.sync();
            }
        } ;
        return sink ;
    }

    private static Destination<Quad> destinationDataset(final DatasetGraphTDB dsg, boolean showProgress)
    {
        LoadMonitor monitor1 = createLoadMonitor(dsg, "triples", showProgress) ;
        LoadMonitor monitor2 = createLoadMonitor(dsg, "quads", showProgress) ;
        final LoaderNodeTupleTable loaderTriples = new LoaderNodeTupleTable(
                                                                dsg.getTripleTable().getNodeTupleTable(),
                                                                "triples",
                                                                monitor1) ;
        final LoaderNodeTupleTable loaderQuads = new LoaderNodeTupleTable( 
                                                                 dsg.getQuadTable().getNodeTupleTable(),
                                                                 "quads",
                                                                 monitor2) ;
        Destination<Quad> sink = new Destination<Quad>() {
            long count = 0 ;
            final public void start()
            {
                loaderTriples.loadStart() ;
                loaderQuads.loadStart() ;

                loaderTriples.loadDataStart() ;
                loaderQuads.loadDataStart() ;
            }
            
            final public void send(Quad quad)
            {
                if ( quad.isTriple() || quad.isDefaultGraph() )
                    loaderTriples.load(quad.getSubject(), quad.getPredicate(),  quad.getObject()) ;
                else
                    loaderQuads.load(quad.getGraph(), quad.getSubject(), quad.getPredicate(),  quad.getObject()) ;
                count++ ;
            }

            final public void finish()
            {
                loaderTriples.loadDataFinish() ;
                loaderQuads.loadDataFinish() ;
                
                loaderTriples.loadIndexStart() ;
                loaderQuads.loadIndexStart() ;

                loaderTriples.loadIndexFinish() ;
                loaderQuads.loadIndexFinish() ;

                loaderTriples.loadFinish() ;
                loaderQuads.loadFinish() ;
                
                dsg.sync() ;
            }
            
            final public void flush() { }
            final public void close() { }
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