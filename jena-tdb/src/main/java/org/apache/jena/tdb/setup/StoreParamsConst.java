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

package org.apache.jena.tdb.setup;

import org.apache.jena.tdb.base.block.FileMode ;
import org.apache.jena.tdb.sys.Names ;
import org.apache.jena.tdb.sys.SystemTDB ;

public class StoreParamsConst {
    /** Filename of the TDB configuration file */
    public static final String TDB_CONFIG_FILE = "tdb.cfg" ;
    
    // SystemParams are built with a SystemParamsBuilder
    
    // Initial values are the system defaults.
    
    /** Database and query configuration */ 
    // Key names are the base name -  encode/decode may add a prefix.
    
    public static final String   fFileMode             = "file_mode" ;
    public static final FileMode fileMode              = SystemTDB.fileMode() ;
    
    public static final String   fBlockReadCacheSize   = "block_read_cache_size" ;
    public static final int      blockReadCacheSize    = SystemTDB.BlockReadCacheSize ;
    
    public static final String   fBlockWriteCacheSize  = "block_write_cache_size" ;
    public static final int      blockWriteCacheSize   = SystemTDB.BlockWriteCacheSize ;
    
    public static final String   fNode2NodeIdCacheSize = "node2nodeid_cache_size" ;
    public static final int      Node2NodeIdCacheSize  = SystemTDB.Node2NodeIdCacheSize ;
    
    public static final String   fNodeId2NodeCacheSize = "nodeid2node_cache_size" ;
    public static final int      NodeId2NodeCacheSize  = SystemTDB.NodeId2NodeCacheSize ;
    
    public static final String   fNodeMissCacheSize    = "node_miss_cache_size" ;
    public static final int      NodeMissCacheSize     = SystemTDB.NodeMissCacheSize ;
    
    /** Database layout - ignored after a database is created */
    public static final String   fBlockSize            = "block_size" ;
    public static final int      blockSize             = SystemTDB.BlockSize ;
    
    public static final String   fIndexNode2Id         = "index_node2id" ;
    public static final String   indexNode2Id          = Names.indexNode2Id ;
    
    public static final String   fIndexId2Node         = "index_id2node" ;
    public static final String   indexId2Node          = Names.indexId2Node ;
    
    public static final String   fPrimaryIndexTriples  = "triple_index_primary" ;
    public static final String   primaryIndexTriples   = Names.primaryIndexTriples ;
    
    public static final String   fTripleIndexes        = "triple_indexes" ;
    public static final String[] tripleIndexes         = Names.tripleIndexes ;
    
    public static final String   fPrimaryIndexQuads    = "quad_index_primary" ;
    public static final String   primaryIndexQuads     = Names.primaryIndexQuads ;
    
    public static final String   fQuadIndexes          = "quad_indexes" ;
    public static final String[] quadIndexes           = Names.quadIndexes ;
    
    public static final String   fPrimaryIndexPrefix   = "prefix_index_primary" ;
    public static final String   primaryIndexPrefix    = Names.primaryIndexPrefix ;
    
    public static final String   fPrefixIndexes        = "prefix_indexes" ;
    public static final String[] prefixIndexes         = Names.prefixIndexes ;
    
    public static final String   fIndexPrefix          = "file_prefix_index" ;
    public static final String   indexPrefix           = Names.indexPrefix ;
    
    public static final String   fPrefixNode2Id        = "file_prefix_nodeid" ;
    public static final String   prefixNode2Id         = Names.prefixNode2Id ;
    
    public static final String   fPrefixId2Node        = "file_prefix_id2node" ;
    public static final String   prefixId2Node         = Names.prefixId2Node ;

    // Must be after the constants above to get initialization order right
    // because StoreParamsBuilder uses these constants.
     
    /** The system default parameters for on-disk databases. */
    static StoreParams dftStoreParams = StoreParams.builder().build() ;

    /** The system default parameters for in-memory databases. */
    static StoreParams dftMemStoreParams = StoreParams.builder()
        .fileMode(FileMode.direct)
        // Small block caches, mainly so it behaves like a direct on-disk database.  
        .blockReadCacheSize(10)
        .blockWriteCacheSize(10)
        .node2NodeIdCacheSize(10000)
        .nodeId2NodeCacheSize(10000)
        .nodeMissCacheSize(100)
        .build() ;
    
    /** The "small store" parameters. */
    static StoreParams smallStoreParams = StoreParams.builder()
        .fileMode(FileMode.direct)
        .blockReadCacheSize(100)
        .blockWriteCacheSize(100)
        .node2NodeIdCacheSize(10000)
        .nodeId2NodeCacheSize(10000)
        .nodeMissCacheSize(100)
        .build() ;
}

