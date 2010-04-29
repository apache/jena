/*
 * (c) Copyright 2010 Talis Systems Ltd.
 * All rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.tdb.store.bulkloader;

import java.io.InputStream ;
import java.util.ArrayList ;
import java.util.List ;

import org.openjena.atlas.event.EventType ;
import org.openjena.atlas.io.IO ;
import org.slf4j.Logger ;
import org.slf4j.LoggerFactory ;

import com.hp.hpl.jena.graph.Node ;
import com.hp.hpl.jena.graph.Triple ;
import com.hp.hpl.jena.riot.Lang ;
import com.hp.hpl.jena.riot.ParserFactory ;
import com.hp.hpl.jena.riot.lang.LangRIOT ;
import com.hp.hpl.jena.sparql.core.Quad ;
import com.hp.hpl.jena.sparql.util.Utils ;
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

//    /** Load into default graph */
//    public static void loadTriples(DatasetGraphTDB dsg, String url, boolean showProgress)
//    {
//        loadTriples(dsg, asList(url) , showProgress) ;
//    }
    
    /** Load into default graph */
    public static void loadDefaultGraph(DatasetGraphTDB dsg, List<String> urls, boolean showProgress)
    {
        Destination<Triple> dest = destinationDefaultGraph(dsg, showProgress) ;
        loadTriples$(dest, urls, showProgress) ;
    }

    /** Load into default graph */
    public static void loadDefaultGraph(DatasetGraphTDB dsg, LangRIOT parser, boolean showProgress)
    {
        Destination<Triple> dest = destinationDefaultGraph(dsg, showProgress) ;
        dest.start() ;
        parser.parse() ;
        dest.finish() ;
    }

    private static String nameForURL(String url)
    {
        if ( url == null || url.equals("-") )
            return "stdin" ;
        return url ;
    }

    private static List<String> asList(String string)
    {
        List<String> list = new ArrayList<String>() ;
        list.add(string) ;
        return list ;
    }
    
    private static Destination<Triple> destinationDefaultGraph(DatasetGraphTDB dsg, boolean showProgress)
    {
        NodeTupleTable ntt = dsg.getQuadTable().getNodeTupleTable() ;
        return destination(dsg, ntt, showProgress) ;
    }

    
    
    /** Load into named graph */
    public static void loadNamedGraph(DatasetGraphTDB dsg, Node graphNode, LangRIOT parser, boolean showProgress)
    {
        Destination<Triple> dest = destinationNamedGraph(dsg, graphNode, showProgress) ;
        dest.start() ;
        parser.parse() ;
        dest.finish() ;
    }

    /** Load into named graph */
    public static void loadNamedGraph(DatasetGraphTDB dsg, Node graphNode, List<String> urls, boolean showProgress)
    {
        Destination<Triple> dest = destinationNamedGraph(dsg, graphNode, showProgress) ;
        loadTriples$(dest, urls, showProgress) ;
    }
    
    /** Load into a dataset */
    public static void loadDataset(DatasetGraphTDB dsg, LangRIOT parser, boolean showProgress)
    {
        Destination<Quad> dest = destinationDataset(dsg, showProgress) ;
        dest.start() ;
        parser.parse() ;
        dest.finish() ;
    }
    
    /** Load into a dataset */
    public static void loadDataset(DatasetGraphTDB dsg, List<String> urls, boolean showProgress)
    {
        Destination<Quad> dest = destinationDataset(dsg, showProgress) ;
        loadQuads$(dest, urls, showProgress) ;
    }
    
    /** Load into default graph */
    private static void loadTriples$(Destination<Triple> dest, List<String> urls, boolean showProgress)
    {
        dest.start() ;
        for ( String url : urls )
        {
            String printName = nameForURL(url) ;  
            InputStream in = IO.openFile(url) ; 
            loadLogger.info("Load: "+printName+" -- "+Utils.nowAsString()) ;
            String base = url ;
            if ( base.equals("-") )
                base = null ;
            Lang lang = Lang.get(url, Lang.NTRIPLES) ;
            LangRIOT parser = ParserFactory.createParserTriples(in, lang, base, dest) ;
            parser.parse() ;
        }            
        dest.finish() ;
    }

    private static void loadQuads$(Destination<Quad> dest, List<String> urls, boolean showProgress)
    {
        // REFACTOR (but how?)
        // This occurs for triples
        dest.start() ;
        for ( String url : urls )
        {
            String printName = nameForURL(url) ;  
            InputStream in = IO.openFile(url) ; 
            loadLogger.info("Load: "+printName+" -- "+Utils.nowAsString()) ;
            String base = url ;
            if ( base.equals("-") )
                base = null ;
            Lang lang = Lang.get(url, Lang.NQUADS) ;
            LangRIOT parser = ParserFactory.createParserQuads(in, lang, base, dest) ;
            parser.parse() ;
        }            
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
        final LoaderNodeTupleTable loaderTriples = new LoaderNodeTupleTable(dsg.getTripleTable().getNodeTupleTable(),
                                                                            "triples",
                                                                            monitor) ;
        
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
            }
        } ;
        return sink ;
    }

    private static Destination<Quad> destinationDataset(DatasetGraphTDB dsg, boolean showProgress)
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