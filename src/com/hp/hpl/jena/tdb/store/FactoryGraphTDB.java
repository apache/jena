/*
 * (c) Copyright 2008 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.tdb.store;

import static com.hp.hpl.jena.tdb.TDB.logExec;
import static com.hp.hpl.jena.tdb.TDB.logInfo;
import static com.hp.hpl.jena.tdb.sys.SystemTDB.LenIndexRecord;
import static com.hp.hpl.jena.tdb.sys.SystemTDB.LenNodeHash;
import static com.hp.hpl.jena.tdb.sys.SystemTDB.SizeOfNodeId;
import lib.ColumnMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;

import com.hp.hpl.jena.graph.Graph;

import com.hp.hpl.jena.sparql.sse.SSEParseException;

import com.hp.hpl.jena.tdb.TDBFactory;
import com.hp.hpl.jena.tdb.base.file.Location;
import com.hp.hpl.jena.tdb.base.record.RecordFactory;
import com.hp.hpl.jena.tdb.index.IndexBuilder;
import com.hp.hpl.jena.tdb.index.RangeIndex;
import com.hp.hpl.jena.tdb.index.TupleIndex;
import com.hp.hpl.jena.tdb.solver.reorder.ReorderLib;
import com.hp.hpl.jena.tdb.solver.reorder.ReorderTransformation;
import com.hp.hpl.jena.tdb.sys.Names;
import com.hp.hpl.jena.tdb.sys.SystemTDB;


public class FactoryGraphTDB
{
    // For this class
    private static Logger log = LoggerFactory.getLogger(FactoryGraphTDB.class) ;

    private static String primaryIndex = "SPO" ; 
    private static String[] indexes = { primaryIndex, "POS", "OSP" } ;
    
    // ---- Record factories
    public final static RecordFactory indexRecordFactory = new RecordFactory(LenIndexRecord, 0) ; 
    public final static RecordFactory nodeRecordFactory = new RecordFactory(LenNodeHash, SizeOfNodeId) ;
    
    /** Create a TDB graph at the location.  Any existing persistent files are reconnected.
     * If there no index or node files at the location, an empty graph is setup.
     * 
     * @param location      The location containg the file system resources.
     */
    public static GraphTDB createGraph(Location location)
    {
        return createGraph(IndexBuilder.get(), location) ;
    }  
    
    /** Create a TDB graph using a specifc index builder - mainly for testing */
    public static GraphTDB createGraph(IndexBuilder indexBuilder, Location location)
    {
        TripleTable table = createTripleTable(indexBuilder, location, indexes) ;
        ReorderTransformation transform = chooseOptimizer(location) ;
        return new GraphTDB(table, transform, location) ;
    }  
    
    
    /** Create a TDB graph in-memory - for testing */
    public static GraphTDB createGraphMem()
    {
        return createGraph(IndexBuilder.mem(), null) ;
    }

    /** Create a TDB graph in-memory - for testing */
    public static GraphTDB createGraphMem(IndexBuilder indexBuilder)
    {
        TripleTable table = createTripleTableMem(indexBuilder, indexes) ;
        ReorderTransformation transform = chooseOptimizer(null) ;
        return new GraphTDB(table, transform, null) ;
    }

    /** Create a TDB graph at the location and wrap it up as a Model.  
     * Any existing persistent files are reconnected.
     * If there no index or node files at the location, an empty graph is setup.
     * 
     * @param location      The location containg the file system resources.
     */
    public static Model createModel(Location location)
    {
        return ModelFactory.createModelForGraph(createGraph(location)) ;
    }
    
    /** Create a TDB graph in-memory - for testing */
    static TripleTable createTripleTableMem()
    { 
        return createTripleTableMem(indexes) ;
    }

    static TripleTable createTripleTableMem(String...descs)
    { 
        return createTripleTableMem(IndexBuilder.mem(), descs) ;
    }
    
    private static TripleTable createTripleTableMem(IndexBuilder indexBuilder, String...descs)
    {
        TupleIndex indexes[] = new TupleIndex[descs.length] ;
        int i = 0 ;
        for ( String desc : descs )
        {
            indexes[i] = createTupleIndex(indexBuilder, null, desc) ;
            i++ ;
        }

        NodeTable nodeTable = new NodeTableIndex(indexBuilder) ;
        return new TripleTable(indexes, indexRecordFactory, nodeTable, null) ;
    }
    
    private static TripleTable createTripleTable(IndexBuilder indexBuilder, Location location, String...descs)
    {
        TupleIndex indexes[] = new TupleIndex[descs.length] ;
        int i = 0 ;
        for ( String desc : descs )
        {
            indexes[i] = createTupleIndex(indexBuilder, location, desc) ;
            i++ ;
        }

        NodeTable nodeTable = new NodeTableIndex(indexBuilder, location) ;
        return new TripleTable(indexes, indexRecordFactory, nodeTable, location) ;
    }

    static TupleIndex createTupleIndex(IndexBuilder indexBuilder, Location location, String desc)
    {
        RangeIndex rIdx1 = indexBuilder.newRangeIndex(location, indexRecordFactory, desc) ;
        TupleIndex tupleIndex = new TupleIndex(3, new ColumnMap(primaryIndex, desc), indexRecordFactory, rIdx1) ; 
        return tupleIndex ;
    }
    
    private static ReorderTransformation chooseOptimizer(Location location)
    {
        if ( location == null )
            return ReorderLib.identity() ;
        
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
    
    // ----------
    public static void enable()
    {
        TDBFactory.ImplFactory f = new TDBFactory.ImplFactory()
        {
            @Override
            public Graph createGraph()
            {
                return FactoryGraphTDB.createGraphMem() ;
            }
    
            @Override
            public Graph createGraph(Location loc)
            {
                return FactoryGraphTDB.createGraph(loc) ;
            }
        } ;
        TDBFactory.setImplFactory(f) ;
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