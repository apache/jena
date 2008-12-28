/*
 * (c) Copyright 2008, 2009 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.tdb.pgraph;

//import static com.hp.hpl.jena.tdb.TDB.logExec;
//import static com.hp.hpl.jena.tdb.TDB.logInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hp.hpl.jena.sparql.sse.SSEParseException;

import com.hp.hpl.jena.tdb.TDBException;
import com.hp.hpl.jena.tdb.base.file.Location;
import com.hp.hpl.jena.tdb.index.IndexBuilder;
import com.hp.hpl.jena.tdb.index.RangeIndex;
import com.hp.hpl.jena.tdb.nodetable.NodeTable;
import com.hp.hpl.jena.tdb.nodetable.NodeTableIndex;
import com.hp.hpl.jena.tdb.solver.reorder.ReorderLib;
import com.hp.hpl.jena.tdb.solver.reorder.ReorderTransformation;
import com.hp.hpl.jena.tdb.sys.Names;
import com.hp.hpl.jena.tdb.sys.SystemTDB;

/** Place to put various "making" things. */

public class PGraphFactory
{
    public static final String indexSPO         = "SPO" ;
    public static final String indexPOS         = "POS" ;
    public static final String indexOSP         = "OSP" ;
    
    // For this class
    private static Logger log = LoggerFactory.getLogger(PGraphFactory.class) ;
    
    // Replacements.
    private static Logger logExec = LoggerFactory.getLogger(PGraphFactory.class) ;
    private static Logger logInfo = LoggerFactory.getLogger(PGraphFactory.class) ;

    /** Create a graph backed with storage at a particular location */
    public static PGraph create(Location location)
    { 
        if ( location == null )
            throw new TDBException("Location is null") ;

        if ( location.exists(Names.indexNode2Id, Names.extHashExt) )
        {
            log.info("Existing extendible hash index for nodes found - using ExtHash/B+Tree indexing") ;
            return create(IndexBuilder.getExtHash(), location) ;
        }
        
        if ( location.exists(indexSPO, Names.btExt) )
        {
            log.info("Existing BTree index found - using BTree indexing") ;
            return create(IndexBuilder.getBTree(), location) ;
        }
        
        if ( location.exists(indexSPO, Names.bptExt1) )
        {
            log.debug("Existing B+Tree index found - using B+Tree indexing") ;
            return create(IndexBuilder.getBPlusTree(), location) ;
        }   
        
        return create(IndexBuilder.get(), location) ;
    }
    
    /** Create a graph backed with storage at a particular location using a system of indexes */
    public static PGraph create(IndexBuilder indexBuilder, Location location)
    {
        if ( location == null )
            log.warn("Null location") ;
        
        RangeIndex idxSPO = indexBuilder.newRangeIndex(location, PGraph.indexRecordFactory, indexSPO) ;
        TripleIndex triplesSPO = new TripleIndex(indexSPO, idxSPO) ;

        RangeIndex idxPOS = indexBuilder.newRangeIndex(location, PGraph.indexRecordFactory, indexPOS) ;
        TripleIndex triplesPOS = new TripleIndex(indexPOS, idxPOS) ;

        RangeIndex idxOSP = indexBuilder.newRangeIndex(location, PGraph.indexRecordFactory, indexOSP) ;
        TripleIndex triplesOSP = new TripleIndex(indexOSP, idxOSP) ;
     
        // Creates the object file as a file-backed one. 
        NodeTable nodeTable = new NodeTableIndex(indexBuilder, location, 
                                                 Names.nodesData, Names.indexNode2Id,
                                                 SystemTDB.Node2NodeIdCacheSize,
                                                 SystemTDB.NodeId2NodeCacheSize) ;
        
        ReorderTransformation reorder = chooseOptimizer(location) ;

        return new PGraph(triplesSPO, triplesPOS, triplesOSP, nodeTable, reorder, location) ;
    }
    
    private static ReorderTransformation chooseOptimizer(Location location)
    {
        ReorderTransformation reorder = null ;
        if ( location.exists(Names.optStats) )
        {
            try {
                reorder = ReorderLib.weighted(location.getPath(Names.optStats)) ;
                logInfo.info("Statistics-based BGP optimizer") ;  
            } catch (SSEParseException ex) { 
                log.warn("Error in stats file: "+ex.getMessage()) ;
                reorder = null ;
            }
        }
        
        if ( reorder == null && location.exists(Names.optDefault) )
        {
            // Not as good but better than nothing.
            reorder = ReorderLib.fixed() ;
            logInfo.info("Fixed pattern BGP optimizer") ;  
        }
        
        if ( location.exists(Names.optNone) )
        {
            reorder = ReorderLib.identity() ;
            logInfo.info("Optimizer explicitly turned off") ;
        }

        if ( reorder == null )
            reorder = SystemTDB.defaultOptimizer ;
        
        if ( reorder == null )
            logExec.warn("No BGP optimizer") ;
        
        return reorder ; 
    }
    
    // ----
    
    /** Create a graph backed with storage in-memory (maily for testing) */
    public static PGraph createMem()
    {
        return createMem(IndexBuilder.mem()) ;
    }
    
    /** Create a graph backed with storage in-memory (maily for testing) */
    public static PGraph createMem(IndexBuilder factory)
    { 
        RangeIndex idxSPO = factory.newRangeIndex(null, PGraph.indexRecordFactory, indexSPO) ;
        TripleIndex triplesSPO = new TripleIndex(indexSPO, idxSPO) ;

        RangeIndex idxPOS = factory.newRangeIndex(null, PGraph.indexRecordFactory, indexPOS) ;
        TripleIndex triplesPOS = new TripleIndex(indexPOS, idxPOS) ;

        RangeIndex idxOSP = factory.newRangeIndex(null, PGraph.indexRecordFactory, indexOSP) ;
        TripleIndex triplesOSP = new TripleIndex(indexOSP, idxOSP) ;
     
        NodeTable nodeTable = new NodeTableIndex(factory) ;
        
        return new PGraph(triplesSPO, triplesPOS, triplesOSP, nodeTable, null, null) ;
    }
}

/*
 * (c) Copyright 2008, 2009 Hewlett-Packard Development Company, LP
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