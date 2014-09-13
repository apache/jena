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

package com.hp.hpl.jena.tdb.setup;

import org.apache.jena.atlas.lib.ColumnMap ;
import org.slf4j.Logger ;
import org.slf4j.LoggerFactory ;

import com.hp.hpl.jena.tdb.base.file.FileSet ;
import com.hp.hpl.jena.tdb.base.file.Location ;
import com.hp.hpl.jena.tdb.base.record.RecordFactory ;
import com.hp.hpl.jena.tdb.index.IndexFactory ;
import com.hp.hpl.jena.tdb.index.IndexParams ;
import com.hp.hpl.jena.tdb.index.RangeIndex ;
import com.hp.hpl.jena.tdb.store.DatasetPrefixesTDB ;
import com.hp.hpl.jena.tdb.store.nodetable.NodeTable ;
import com.hp.hpl.jena.tdb.store.tupletable.TupleIndex ;
import com.hp.hpl.jena.tdb.store.tupletable.TupleIndexRecord ;
import com.hp.hpl.jena.tdb.sys.DatasetControl ;

/** Building datastructures on top of the base file
 *  abstractions of indexes, block managers and object files.  
 */
public class Build
{
    private static boolean VERBOSE = true ;
    private static Logger log = LoggerFactory.getLogger(Build.class) ;
    private static StoreParams params = StoreParams.getDftStoreParams() ;
    
    public static TupleIndex openTupleIndex(Location location, String indexName, String primary, String indexOrder, int readCacheSize, int writeCacheSize, int dftKeyLength, int dftValueLength)
    {
        // XXX replace with:
        // return DatasetBuilderStd.stdBuilder().makeTupleIndex(location, indexName, primary, indexOrder) ;
        // All this to BuilderDB.
        StoreParamsBuilder spb = new StoreParamsBuilder() ;
        spb.blockReadCacheSize(readCacheSize) ;
        spb.blockWriteCacheSize(writeCacheSize) ;
        RecordFactory recordFactory = new RecordFactory(dftKeyLength, dftValueLength) ;
        IndexParams idxParams = spb.build() ;
        FileSet fs = new FileSet(location, indexName) ;
        RangeIndex rIndex = IndexFactory.buildRangeIndex(fs, recordFactory, idxParams) ;
        TupleIndex tupleIndex = new TupleIndexRecord(primary.length(), new ColumnMap(primary, indexOrder), indexOrder, rIndex.getRecordFactory(), rIndex) ;
        return tupleIndex ;
    }
    
    public static DatasetPrefixesTDB makePrefixes(Location location, DatasetControl policy) {
        return DatasetBuilderStd.stdBuilder().makePrefixTable(location, policy, params) ;
    }

    public static NodeTable makeNodeTable(Location location) {
        return makeNodeTable(location, params) ;
    }

    public static NodeTable makeNodeTable(Location location, StoreParams params) {
        DatasetBuilderStd dbBuild = DatasetBuilderStd.stdBuilder() ;
        return dbBuild.makeNodeTable(location, params) ; 
    }
    
    //XXX Reorg all calls to NodeTableBuilder to this argument order.
    public static NodeTable makeNodeTable(Location location, 
                                          String indexNode2Id, int node2NodeIdCacheSize,
                                          String indexId2Node, int nodeId2NodeCacheSize,
                                          int sizeNodeMissCacheSize) {
        StoreParamsBuilder spb = new StoreParamsBuilder() ;
        spb.indexNode2Id(indexNode2Id).node2NodeIdCacheSize(node2NodeIdCacheSize) ;
        spb.indexId2Node(indexId2Node).nodeId2NodeCacheSize(nodeId2NodeCacheSize) ;
        DatasetBuilderStd dbBuild = DatasetBuilderStd.stdBuilder() ;
        return makeNodeTable(location, spb.build()) ; 
    }
}
