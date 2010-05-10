/*
 * (c) Copyright 2010 Talis Systems Ltd.
 * All rights reserved.
 * [See end of file]
 */

package dev;

import java.io.InputStream ;

import org.openjena.atlas.io.IO ;
import org.openjena.atlas.lib.Sink ;
import org.openjena.atlas.logging.Log ;
import org.slf4j.Logger ;
import org.slf4j.LoggerFactory ;

import com.hp.hpl.jena.graph.Triple ;
import com.hp.hpl.jena.riot.ParserFactory ;
import com.hp.hpl.jena.riot.lang.LangRIOT ;
import com.hp.hpl.jena.tdb.base.file.Location ;
import com.hp.hpl.jena.tdb.nodetable.NodeTable ;
import com.hp.hpl.jena.tdb.store.bulkloader.BulkLoader ;
import com.hp.hpl.jena.tdb.store.bulkloader.LoadMonitor ;
import com.hp.hpl.jena.tdb.sys.Names ;
import com.hp.hpl.jena.tdb.sys.SetupTDB ;
import com.hp.hpl.jena.tdb.sys.SystemTDB ;

public class RunLoaderExperiment
{
    static { Log.setLog4j() ; }
    static String divider = "----------" ;
    static String nextDivider = null ;
    static void divider()
    {
        if ( nextDivider != null )
            System.out.println(nextDivider) ;
        nextDivider = divider ;
    }
    
    public static void main(String...argv)
    {
        String loggerName = "LOADER" ; 
        Logger log = LoggerFactory.getLogger(loggerName) ;
        Log.enable(loggerName) ;

        final NodeTable nodeTable = SetupTDB.makeNodeTable(new Location("XDB"), 
                                            Names.indexNode2Id, SystemTDB.Node2NodeIdCacheSize,
                                            Names.indexId2Node, SystemTDB.NodeId2NodeCacheSize) ;
        
        LoadMonitor loadMonitor = new LoadMonitor(null, log, "triples", BulkLoader.DataTickPoint, BulkLoader.IndexTickPoint) ;
        Sink<Triple> nodesOnly = new StreamTriples(nodeTable, loadMonitor) ;
        
         
        if ( argv.length == 0 )
            argv = new String[]{"-"} ;
        
        
        
        loadMonitor.startLoad() ;
        loadMonitor.startDataPhase() ;
        for ( String s : argv )
        {
            InputStream in = IO.openFile(s) ;
            LangRIOT parser = ParserFactory.createParserNTriples(in, nodesOnly) ;
            parser.parse() ;
        }
        nodesOnly.close() ;
        loadMonitor.finishDataPhase() ;
        loadMonitor.finishLoad() ;
    }
    
    static class StreamTriples implements Sink<Triple>
    {
        private NodeTable nodeTable ;
        private long count = 0 ;
        private LoadMonitor loadMonitor ; 

        public StreamTriples(NodeTable nodeTable, LoadMonitor loadMonitor)
        { this.nodeTable = nodeTable ; this.loadMonitor = loadMonitor ; }

        public void send(Triple triple)
        {
            count++ ;
            loadMonitor.dataItem() ;
            nodeTable.getAllocateNodeId(triple.getSubject()) ;
            nodeTable.getAllocateNodeId(triple.getPredicate()) ;
            nodeTable.getAllocateNodeId(triple.getObject()) ;
        }

        public void flush()
        {}

        public void close()
        {}
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