/*
 * (c) Copyright 2008 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.tdb.pgraph;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hp.hpl.jena.sparql.sse.SSEParseException;

import com.hp.hpl.jena.tdb.TDB;
import com.hp.hpl.jena.tdb.TDBException;
import com.hp.hpl.jena.tdb.base.file.Location;
import com.hp.hpl.jena.tdb.index.Index;
import com.hp.hpl.jena.tdb.index.IndexBuilder;
import com.hp.hpl.jena.tdb.index.RangeIndex;
import com.hp.hpl.jena.tdb.index.TripleIndex;
import com.hp.hpl.jena.tdb.solver.reorder.ReorderLib;
import com.hp.hpl.jena.tdb.solver.reorder.ReorderPattern;
import com.hp.hpl.jena.tdb.solver.reorder.ReorderVarCount;
import com.hp.hpl.jena.tdb.sys.Names;

/** Place to put various "making" things. */

public class GraphTDBFactory
{
    // For this class
    private static Logger log = LoggerFactory.getLogger(GraphTDBFactory.class) ;
    // Generally informative
    private static Logger logTDB = LoggerFactory.getLogger(TDB.class) ;

    /** Create a graph backed with storage at a particular location */
    public static GraphTDB create(Location location)
    { 
        if ( location == null )
            throw new TDBException("Location is null") ;
        
        if ( location.exists(Names.indexSPO, Names.btExt) )
        {
            log.info("Existing BTree index found - using BTree indexing") ;
            return create(IndexBuilder.getBTree(), location) ;
        }
        
        if ( location.exists(Names.indexSPO, Names.bptExt1) )
        {
            log.debug("Existing B+Tree index found - using B+Tree indexing") ;
            return create(IndexBuilder.getBPlusTree(), location) ;
        }   
        
        return create(IndexBuilder.get(), location) ;
    }
    
    /** Create a graph backed with storage at a particular location using a system of indexes */
    public static GraphTDB create(IndexBuilder factory, Location location)
    {
        if ( location == null )
            log.warn("Null location") ;
        
        RangeIndex idxSPO = factory.newRangeIndex(location, GraphTDB.indexRecordFactory, Names.indexSPO) ;
        TripleIndex triplesSPO = new TripleIndex(Names.indexSPO, idxSPO) ;

        RangeIndex idxPOS = factory.newRangeIndex(location, GraphTDB.indexRecordFactory, Names.indexPOS) ;
        TripleIndex triplesPOS = new TripleIndex(Names.indexPOS, idxPOS) ;

        RangeIndex idxOSP = factory.newRangeIndex(location, GraphTDB.indexRecordFactory, Names.indexOSP) ;
        TripleIndex triplesOSP = new TripleIndex(Names.indexOSP, idxOSP) ;
     
        // Creates the object file as a file-backed one. 
        NodeTable nodeTable = new NodeTableIndex(factory, location) ;
        
        ReorderPattern reorder = null ;
        if ( location.exists(Names.optStats) )
        {
            try {
                reorder = ReorderLib.weighted(location.getPath(Names.optStats)) ;
                logTDB.info("Statistics-based BGP optimizer") ;  
            } catch (SSEParseException ex) { 
                throw new TDBException("Error in stats file: "+ex.getMessage()) ;
            }
        }
        if ( reorder == null && location.exists(Names.optCountVar) )
        {
            // Not as good but better than nothering.
            reorder = new ReorderVarCount() ;
            logTDB.info("Variable counting BGP optimizer") ;  
        }
        
        if ( location.exists(Names.optNone) )
        {
            reorder = ReorderLib.identity() ;
            logTDB.info("Optimizer explicitly turned off") ;
        }

        if ( reorder == null )
            logTDB.info("No BGP optimizer") ;
        
        return new GraphTDB(triplesSPO, triplesPOS, triplesOSP, nodeTable, reorder) ;
    }
    
    // ----
    
    /** Create a graph backed with storage in-memory (maily for testing) */
    public static GraphTDB createMem()
    {
        return createMem(IndexBuilder.mem()) ;
    }
    
    /** Create a graph backed with storage in-memory (maily for testing) */
    public static GraphTDB createMem(IndexBuilder factory)
    { 
        RangeIndex idxSPO = factory.newRangeIndex(null, GraphTDB.indexRecordFactory, Names.indexSPO) ;
        TripleIndex triplesSPO = new TripleIndex(Names.indexSPO, idxSPO) ;

        RangeIndex idxPOS = factory.newRangeIndex(null, GraphTDB.indexRecordFactory, Names.indexPOS) ;
        TripleIndex triplesPOS = new TripleIndex(Names.indexPOS, idxPOS) ;

        RangeIndex idxOSP = factory.newRangeIndex(null, GraphTDB.indexRecordFactory, Names.indexOSP) ;
        TripleIndex triplesOSP = new TripleIndex(Names.indexOSP, idxOSP) ;
     
        Index nodeIndex = factory.newIndex(null, GraphTDB.nodeRecordFactory, Names.indexNode2Id) ;
        
        // Implicitly creates the object file as a memory one. 
        NodeTable nodeTable = new NodeTableIndex(factory) ;
        
        return new GraphTDB(triplesSPO, triplesPOS, triplesOSP, nodeTable, null) ;
    }
}

/*
 * (c) Copyright 2008 Hewlett-Packard Development Company, LP
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