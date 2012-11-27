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

package com.hp.hpl.jena.tdb.setup;

import org.apache.jena.atlas.lib.ColumnMap ;
import org.apache.jena.atlas.lib.StrUtils ;
import org.slf4j.Logger ;
import org.slf4j.LoggerFactory ;

import com.hp.hpl.jena.sparql.engine.optimizer.reorder.ReorderTransformation ;
import com.hp.hpl.jena.tdb.TDBException ;
import com.hp.hpl.jena.tdb.base.file.FileSet ;
import com.hp.hpl.jena.tdb.base.file.Location ;
import com.hp.hpl.jena.tdb.index.TupleIndex ;
import com.hp.hpl.jena.tdb.nodetable.NodeTable ;
import com.hp.hpl.jena.tdb.store.* ;
import com.hp.hpl.jena.tdb.sys.DatasetControl ;
import com.hp.hpl.jena.tdb.sys.DatasetControlMRSW ;

/** A general way to make TDB storage dataset graphs : not for transactional datasets */ 

public class DatasetBuilderBasic implements DatasetBuilder
{
    private static final Logger log = LoggerFactory.getLogger(DatasetBuilderBasic.class) ;
    
    NodeTableBuilder nodeTableBuilder ;
    TupleIndexBuilder tupleIndexBuilder ;
    SystemParams params ;
    
    public DatasetBuilderBasic(IndexBuilder indexBuilder, RangeIndexBuilder rangeIndexBuilder)
    {
        ObjectFileBuilder objectFileBuilder = new Builder.ObjectFileBuilderStd()  ;
        nodeTableBuilder    = new Builder.NodeTableBuilderStd(indexBuilder, objectFileBuilder) ;
        tupleIndexBuilder   = new Builder.TupleIndexBuilderStd(rangeIndexBuilder) ;
    }

    @Override
    public DatasetGraphTDB build(Location location, SystemParams config)
    {
        DatasetControl policy = createConcurrencyPolicy() ;
        
        params = config ;
        if ( config == null )
            params = SystemParams.getStdSystemParams() ;
        
        NodeTable nodeTable = makeNodeTable(location, params.indexNode2Id, params.indexId2Node,
                                            -1, -1, -1) ; // No caches
                                            // Small caches 
                                            //10, 1000, 10) ;
                                            //params.Node2NodeIdCacheSize, params.NodeId2NodeCacheSize, params.NodeMissCacheSize) ;
        
        //nodeTable = new NodeTableLogger(null, nodeTable) ;
        
        TripleTable tripleTable = makeTripleTable(location, nodeTable, policy) ; 
        QuadTable quadTable = makeQuadTable(location, nodeTable, policy) ;
        DatasetPrefixesTDB prefixes = makePrefixTable(location, policy) ;
        ReorderTransformation transform  = chooseReorderTransformation(location) ;
        
        DatasetGraphTDB dsg = new DatasetGraphTDB(tripleTable, quadTable, prefixes, transform, null) ;
        return dsg ;
    }
    
    protected DatasetControl createConcurrencyPolicy() { return new DatasetControlMRSW() ; }
    
    protected ReorderTransformation chooseReorderTransformation(Location location)
    {    
        return DatasetBuilderStd.chooseOptimizer(location) ;
    }
    
    protected NodeTable makeNodeTable(Location location, String indexNode2Id, String indexId2Node, 
                                      int sizeNode2NodeIdCache, int sizeNodeId2NodeCache, int sizeNodeMissCache)
    {
        FileSet fsNodeToId = new FileSet(location, indexNode2Id) ;
        FileSet fsId2Node = new FileSet(location, indexId2Node) ;
        NodeTable nt = nodeTableBuilder.buildNodeTable(fsNodeToId, fsId2Node, sizeNode2NodeIdCache, sizeNodeId2NodeCache, sizeNodeMissCache) ;
        return nt ;
    }
    
    // ======== Dataset level
    protected TripleTable makeTripleTable(Location location, NodeTable nodeTable, DatasetControl policy)
    {    
        String primary = params.primaryIndexTriples ;
        String[] indexes = params.tripleIndexes ;
        
        if ( indexes.length != 3 )
            error(log, "Wrong number of triple table indexes: "+StrUtils.strjoin(",", indexes)) ;
        log.debug("Triple table: "+primary+" :: "+StrUtils.strjoin(",", indexes)) ;
        
        TupleIndex tripleIndexes[] = makeTupleIndexes(location, primary, indexes) ;
        
        if ( tripleIndexes.length != indexes.length )
            error(log, "Wrong number of triple table tuples indexes: "+tripleIndexes.length) ;
        TripleTable tripleTable = new TripleTable(tripleIndexes, nodeTable, policy) ;
        return tripleTable ;
    }
    
    protected QuadTable makeQuadTable(Location location, NodeTable nodeTable, DatasetControl policy)
    {    
        String primary = params.primaryIndexQuads ;
        String[] indexes = params.quadIndexes ;
        
        if ( indexes.length != 6 )
            error(log, "Wrong number of quad table indexes: "+StrUtils.strjoin(",", indexes)) ;
        
        log.debug("Quad table: "+primary+" :: "+StrUtils.strjoin(",", indexes)) ;
        
        TupleIndex quadIndexes[] = makeTupleIndexes(location, primary, indexes) ;
        if ( quadIndexes.length != indexes.length )
            error(log, "Wrong number of quad table tuples indexes: "+quadIndexes.length) ;
        QuadTable quadTable = new QuadTable(quadIndexes, nodeTable, policy) ;
        return quadTable ;
    }

    protected DatasetPrefixesTDB makePrefixTable(Location location, DatasetControl policy)
    {    
        String primary = params.primaryIndexPrefix ;
        String[] indexes = params.prefixIndexes ;
        
        TupleIndex prefixIndexes[] = makeTupleIndexes(location, primary, indexes, new String[]{params.indexPrefix}) ;
        if ( prefixIndexes.length != 1 )
            error(log, "Wrong number of triple table tuples indexes: "+prefixIndexes.length) ;
        
        String pnNode2Id = params.prefixNode2Id ;
        String pnId2Node = params.prefixId2Node ;
        
        // No cache - the prefix mapping is a cache
        NodeTable prefixNodes = makeNodeTable(location, pnNode2Id, pnId2Node, -1, -1, -1)  ;
        
        DatasetPrefixesTDB prefixes = new DatasetPrefixesTDB(prefixIndexes, prefixNodes, policy) ; 
        
        log.debug("Prefixes: "+primary+" :: "+StrUtils.strjoin(",", indexes)) ;
        
        return prefixes ;
    }
    
    private TupleIndex[] makeTupleIndexes(Location location, String primary, String[] indexNames)
    {
        return makeTupleIndexes(location, primary, indexNames, indexNames) ;
    }
    
    private TupleIndex[] makeTupleIndexes(Location location, String primary, String[] indexNames, String[] filenames)
    {
        if ( primary.length() != 3 && primary.length() != 4 )
            error(log, "Bad primary key length: "+primary.length()) ;
    
        int indexRecordLen = primary.length()*NodeId.SIZE ;
        TupleIndex indexes[] = new TupleIndex[indexNames.length] ;
        for (int i = 0 ; i < indexes.length ; i++)
            indexes[i] = makeTupleIndex(location, filenames[i], primary, indexNames[i]) ;
        return indexes ;
    }

    // ----
    protected TupleIndex makeTupleIndex(Location location, String name, String primary, String indexOrder)
    {
        // Commonly,  name == indexOrder.
        // FileSet
        FileSet fs = new FileSet(location, name) ;
        ColumnMap colMap = new ColumnMap(primary, indexOrder) ;
        return tupleIndexBuilder.buildTupleIndex(fs, colMap, indexOrder) ;
    }

    private static void error(Logger log, String msg)
    {
        if ( log != null )
            log.error(msg) ;
        throw new TDBException(msg) ;
    }
}
