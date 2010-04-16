/*
 * (c) Copyright 2010 Talis Systems Ltd.
 * All rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.tdb.store.bulkloader;

import java.util.Date ;

import org.openjena.atlas.event.Event ;
import org.openjena.atlas.event.EventListener ;
import org.openjena.atlas.event.EventManager ;
import org.openjena.atlas.event.EventType ;

import com.hp.hpl.jena.graph.Node ;
import com.hp.hpl.jena.graph.Triple ;
import com.hp.hpl.jena.sparql.core.Quad ;
import com.hp.hpl.jena.sparql.util.StringUtils ;
import com.hp.hpl.jena.tdb.nodetable.NodeTupleTable ;
import com.hp.hpl.jena.tdb.nodetable.NodeTupleTableView ;
import com.hp.hpl.jena.tdb.store.DatasetGraphTDB ;

/** Overall framework for bulk loading */
public class BulkLoader
{
    /*
     * Process model
     *   load start
     *   data load start
     *   data load finish
     *   index building start
     *   index building finish
     *   load finish
     * 
     */
    
    // Event callbacks for the load stages?
    // On what object?  The dataset.
    
    public static Destination<Triple> loadTriples(DatasetGraphTDB dsg, boolean showProgress)
    {
        return loadTriples(dsg, dsg.getTripleTable().getNodeTupleTable(), showProgress) ;
    }
    
    public static Destination<Triple> loadTriples(DatasetGraphTDB dsg, Node graphName, boolean showProgress)
    {
        NodeTupleTable ntt = dsg.getQuadTable().getNodeTupleTable() ;
        NodeTupleTable ntt2 = new NodeTupleTableView(ntt, graphName) ;
        return loadTriples(dsg, ntt2, showProgress) ;
    }

    /** Tick point for messages during loading of data */
    public static int LoadTickPoint = 10 ;
    /** Tick point for messages during secondary index creation */
    public static long IndexTickPoint = 20 ;
    
    // Events.
    // Either one "bulkload" event with args for status or
    // better hierarchies of event types.  Event type = list
    
    private static String baseNameGeneral = "http://openjena.org/TDB/event#" ;

    private static String baseName = "http://openjena.org/TDB/bulkload/event#" ;
    
    
    static EventType evBulkload = new EventType(baseName+"bulkload") ;
    static EventType evTick = new EventType(baseNameGeneral+"tick") ;
    
    static EventType evStartBulkload = new EventType(baseName+"start-bulkload") ;
    static EventType evFinishBulkload = new EventType(baseName+"finish-bulkload") ;

    static EventType evStartDataBulkload = new EventType(baseName+"start-bulkload-data") ;
    static EventType evFinishDataBulkload = new EventType(baseName+"finish-bulkload-data") ;
    
    static EventType evStartIndexBulkload = new EventType(baseName+"start-bulkload-data") ;
    static EventType evFinishIndexBulkload = new EventType(baseName+"finish-bulkload-data") ;
    
    static EventListener listener = new EventListener() {
        public void event(Object dest, Event event)
        {
            System.out.printf("%s\n", event.getType()) ;
        }
    } ;
        
//    static class LoadEvent extends Event
//    {
//        public LoadEvent(Object argument)
//        {
//            super(evBulkload, argument) ;
//        }
//    }
//        
//    private void addListeners(DatasetGraph dsg)
//    {
//        EventManager.register(dsg, eventType, listener) ;
//    }
    
    private static Destination<Triple> loadTriples(final DatasetGraphTDB dsg, NodeTupleTable nodeTupleTable, final boolean showProgress)
    {
        // Testing.
        EventManager.register(dsg, evStartBulkload, listener) ;
        EventManager.register(dsg, evFinishBulkload, listener) ;
        
        final LoaderNodeTupleTable x = new LoaderNodeTupleTable( 
                                                                dsg.getTripleTable().getNodeTupleTable(),
                                                                true) ;
        Destination<Triple> sink = new Destination<Triple>() {
            Ticker ticker = (showProgress? new TickEvent(LoadTickPoint) : null ) ;
            
            public void start()
            {
                EventManager.send(dsg, new Event(evStartBulkload, null)) ;
                if ( ticker != null )
                    ticker.start() ;
                x.loadStart() ;
            }
            public void send(Triple triple)
            {
                x.load(triple.getSubject(), triple.getPredicate(),  triple.getObject()) ;
                if ( ticker != null )
                    ticker.tick() ;
            }

            public void flush() { }
            public void close() { }

            public void finish()
            {
                x.loadFinish() ;
                if ( ticker != null )
                    ticker.finish() ;
                EventManager.send(dsg, new Event(evFinishBulkload, null)) ;
            }
        } ;
        return sink ;
    }

    public Destination<Quad> loadQuads(DatasetGraphTDB dsg)
    {
        final LoaderNodeTupleTable loaderTriples = new LoaderNodeTupleTable( 
                                                                dsg.getTripleTable().getNodeTupleTable(),
                                                                true) ;
        final LoaderNodeTupleTable loaderQuads = new LoaderNodeTupleTable( 
                                                                 dsg.getQuadTable().getNodeTupleTable(),
                                                                 true) ;
        Destination<Quad> sink = new Destination<Quad>() {
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
    
    // Start
    // data
    // finish
    
    boolean showProgress = true ;
    
    // ---- Misc utilities
    synchronized void printf(String fmt, Object... args)
    {
        if (!showProgress) return ;
        System.out.printf(fmt, args) ;
    }

    synchronized void println()
    {
        if (!showProgress) return ;
        System.out.println() ;
    }

    synchronized void println(String str)
    {
        if (!showProgress) return ;
        System.out.println(str) ;
    }

    synchronized void now(String str)
    {
        if (!showProgress) return ;

        if (str != null)
        {
            System.out.print(str) ;
            System.out.print(" : ") ;
        }
        System.out.println(StringUtils.str(new Date())) ;
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