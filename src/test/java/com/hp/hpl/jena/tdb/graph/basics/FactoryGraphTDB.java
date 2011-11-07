/*
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

package com.hp.hpl.jena.tdb.graph.basics;

import static com.hp.hpl.jena.tdb.TDB.logInfo ;
import static com.hp.hpl.jena.tdb.sys.Names.primaryIndexQuads ;
import static com.hp.hpl.jena.tdb.sys.Names.primaryIndexTriples ;
import static com.hp.hpl.jena.tdb.sys.Names.quadIndexes ;
import static com.hp.hpl.jena.tdb.sys.Names.tripleIndexes ;
import static com.hp.hpl.jena.tdb.sys.SystemTDB.LenIndexQuadRecord ;
import static com.hp.hpl.jena.tdb.sys.SystemTDB.LenIndexTripleRecord ;
import static com.hp.hpl.jena.tdb.sys.SystemTDB.LenNodeHash ;
import static com.hp.hpl.jena.tdb.sys.SystemTDB.SizeOfNodeId ;
import org.openjena.atlas.lib.ColumnMap ;
import org.slf4j.Logger ;
import org.slf4j.LoggerFactory ;

import com.hp.hpl.jena.sparql.engine.optimizer.reorder.ReorderLib ;
import com.hp.hpl.jena.sparql.engine.optimizer.reorder.ReorderTransformation ;
import com.hp.hpl.jena.sparql.sse.SSEParseException ;
import com.hp.hpl.jena.tdb.base.file.FileSet ;
import com.hp.hpl.jena.tdb.base.file.Location ;
import com.hp.hpl.jena.tdb.base.record.RecordFactory ;
import com.hp.hpl.jena.tdb.index.IndexBuilder ;
import com.hp.hpl.jena.tdb.index.RangeIndex ;
import com.hp.hpl.jena.tdb.index.TupleIndex ;
import com.hp.hpl.jena.tdb.index.TupleIndexRecord ;
import com.hp.hpl.jena.tdb.nodetable.NodeTable ;
import com.hp.hpl.jena.tdb.nodetable.NodeTableFactory ;
import com.hp.hpl.jena.tdb.store.DatasetGraphTDB ;
import com.hp.hpl.jena.tdb.store.DatasetPrefixesTDB ;
import com.hp.hpl.jena.tdb.store.GraphTriplesTDB ;
import com.hp.hpl.jena.tdb.store.QuadTable ;
import com.hp.hpl.jena.tdb.store.TripleTable ;
import com.hp.hpl.jena.tdb.sys.DatasetControl ;
import com.hp.hpl.jena.tdb.sys.DatasetControlMRSW ;
import com.hp.hpl.jena.tdb.sys.Names ;
import com.hp.hpl.jena.tdb.sys.SystemTDB ;

/** Low-level factory for things TDB. See {@link com.hp.hpl.jena.tdb.TDBFactory} for the usual application API */
class FactoryGraphTDB
{
    // This class and GraphTDBFactoryTesting are old ways of making graphs and datasets 

    
    // **** Much of this is superceeded by the newer factory for making datasets that manages metafiles. 
    // Remains for testing using IndexBuilders
    
    // For this class
    private static Logger log = LoggerFactory.getLogger(FactoryGraphTDB.class) ;

    // ---- Record factories
    public final static RecordFactory indexRecordTripleFactory = new RecordFactory(LenIndexTripleRecord, 0) ; 
    public final static RecordFactory indexRecordQuadFactory = new RecordFactory(LenIndexQuadRecord, 0) ; 
    public final static RecordFactory nodeRecordFactory = new RecordFactory(LenNodeHash, SizeOfNodeId) ;
    
    /** Create a TDB graph using a specifc index builder - mainly for testing */
    public static GraphTriplesTDB createGraph(IndexBuilder indexBuilder, Location location)
    {
        DatasetGraphTDB ds = _createDatasetGraph(indexBuilder, location, tripleIndexes, quadIndexes) ;
        return (GraphTriplesTDB)ds.getDefaultGraph() ;
    }  

    /** Create a TDB graph in-memory - for testing */
    public static GraphTriplesTDB createGraphMem(IndexBuilder indexBuilder)
    {
        return createGraph(indexBuilder, Location.mem()) ;
    }
    
//    /** Create or connect a TDB dataset */ 
//    public static DatasetGraphTDB createDatasetGraph(Location location)
//    {
//        if ( location.isMem() )
//            return createDatasetGraphMem() ;
//        return createDatasetGraph(IndexBuilder.get(), location) ;
//    }
//
//    /** Create or connect a TDB dataset in-memory - for testing */ 
//    public static DatasetGraphTDB createDatasetGraphMem()
//    {
//        return createDatasetGraph(IndexBuilder.mem(), Location.mem()) ;
//    }
//
//    /** Create or connect a TDB dataset (graph-level) */
//    public static DatasetGraphTDB createDatasetGraph(IndexBuilder indexBuilder, Location location)
//    {
//        return _createDatasetGraph(indexBuilder, location, tripleIndexes, quadIndexes) ;
//    }
//    
//    /** Create or connect a TDB dataset (graph-level) */
//    public static DatasetGraphTDB createDatasetGraph(IndexBuilder indexBuilder, Location location, String[] graphDesc, String[] quadDesc)
//    {
//        return _createDatasetGraph(indexBuilder, location, graphDesc, quadDesc) ;
//    }

//    /** Create or connect a TDB dataset */
//    public static Dataset createDataset(Location location)
//    {
//        return new DatasetImpl(createDatasetGraph(location)) ;
//    }
//
//    /** Create or connect a TDB dataset (in-memory - for testing) */
//    public static Dataset createDatasetMem()
//    {
//        return new DatasetImpl(createDatasetGraphMem()) ;
//    }

    /** Given an IndexBuilder, RecordFactory, Location and index descriptions,
     *  make a set of indexes. 
     */
    private static TupleIndex[] indexes(final IndexBuilder indexBuilder, RecordFactory recordFactory, 
                                       final Location location, String primary, String...descs)
    {
        TupleIndex indexes[] = new TupleIndex[descs.length] ;
        int i = 0 ;
        for ( String desc : descs )
        {
            indexes[i] = 
                createTupleIndex(indexBuilder, recordFactory, location, primary, desc) ;
            i++ ;
        }
        return indexes ;
    }
    
    private static TripleTable createTripleTableMem(DatasetControl policy)
    {
        NodeTable nodeTable = NodeTableFactory.createMem(IndexBuilder.mem()) ;
        return createTripleTable(IndexBuilder.mem(), nodeTable, Location.mem(), tripleIndexes, policy) ;
    }

    /** Public for testing only : create a triple table.*/
    private static TripleTable createTripleTable(IndexBuilder indexBuilder, NodeTable nodeTable, Location location, String[]descs, DatasetControl policy)
    {
        TupleIndex indexes[] = indexes(indexBuilder, indexRecordTripleFactory, location, primaryIndexTriples, descs) ;
        return new TripleTable(indexes, nodeTable, policy) ;
    }

    private static QuadTable createQuadTableMem(DatasetControl policy)
    {
        NodeTable nodeTable = NodeTableFactory.createMem(IndexBuilder.mem()) ;
        return createQuadTable(IndexBuilder.mem(), nodeTable, null, tripleIndexes, policy) ;
    }
    
    /** Public for testing only : create a quad table.*/
    private static QuadTable createQuadTable(IndexBuilder indexBuilder, NodeTable nodeTable,
                                             Location location, String[]descs, DatasetControl policy)
    {
        TupleIndex indexes[] = indexes(indexBuilder, indexRecordQuadFactory, location, primaryIndexQuads, descs) ;
        return new QuadTable(indexes, nodeTable, policy) ;
    }
    
    // ---- All creation happens here
    
    /** Create or connect a TDB dataset (graph-level) */
    private static DatasetGraphTDB _createDatasetGraph(IndexBuilder indexBuilder, Location location, String[] graphIndexDesc, String[] quadIndexDesc)
    {
        DatasetControl policy = new DatasetControlMRSW() ;
        @SuppressWarnings("deprecation")
        NodeTable nodeTable = NodeTableFactory.create(indexBuilder, location) ;
        TripleTable triples = createTripleTable(indexBuilder, nodeTable, location, graphIndexDesc, policy) ;
        QuadTable quads = createQuadTable(indexBuilder, nodeTable, location, quadIndexDesc, policy) ;
        @SuppressWarnings("deprecation")
        DatasetPrefixesTDB prefixes = DatasetPrefixesTDB.create(indexBuilder, location, policy) ;
        return new DatasetGraphTDB(triples, quads, prefixes, chooseOptimizer(location), null) ;
    }

    private static TupleIndex createTupleIndex(IndexBuilder indexBuilder, RecordFactory recordFactory, Location location, String primary, String desc)
    {
        // Map name of index to name of file.
        FileSet fileset = new FileSet(location, desc) ;
        RangeIndex rIdx1 = indexBuilder.newRangeIndex(fileset, recordFactory) ;
        TupleIndex tupleIndex = new TupleIndexRecord(desc.length(), new ColumnMap(primary, desc), recordFactory, rIdx1) ; 
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
            logInfo.warn("No BGP optimizer") ;
        
        return reorder ; 
    }
    
//    // ----------
//    public static void enable()
//    {
//        TDBFactory.ImplFactory f = new TDBFactory.ImplFactory()
//        {
//            @Override
//            public Graph createGraph()
//            {
//                return FactoryGraphTDB.createGraphMem() ;
//            }
//    
//            @Override
//            public Graph createGraph(Location loc)
//            {
//                return FactoryGraphTDB.createGraph(loc) ;
//            }
//        } ;
//        TDBFactory.setImplFactory(f) ;
//    }
}
