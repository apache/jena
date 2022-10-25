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

package org.apache.jena.tdb2.params;

import org.apache.jena.dboe.base.block.FileMode;
import org.apache.jena.dboe.sys.Names;
import org.apache.jena.tdb2.sys.SystemTDB;

public class StoreParamsConst {
    // SystemParams are built with a SystemParamsBuilder
    // Initial values are the system defaults.

    /** Database and query configuration */
    // Key names are the base name -  encode/decode may add a prefix.

    public static final String   fLabel                = "label";

    public static final String   fFileMode             = "file_mode";
    public static final FileMode fileMode              = SystemTDB.fileMode();

    public static final String   fBlockReadCacheSize   = "block_read_cache_size";
    public static final int      blockReadCacheSize    = SystemTDB.BlockReadCacheSize;

    public static final String   fBlockWriteCacheSize  = "block_write_cache_size";
    public static final int      blockWriteCacheSize   = SystemTDB.BlockWriteCacheSize;

    public static final String   fNode2NodeIdCacheSize = "node2nodeid_cache_size";
    public static final int      Node2NodeIdCacheSize  = SystemTDB.Node2NodeIdCacheSize;

    public static final String   fNodeId2NodeCacheSize = "nodeid2node_cache_size";
    public static final int      NodeId2NodeCacheSize  = SystemTDB.NodeId2NodeCacheSize;

    public static final String   fNodeMissCacheSize    = "node_miss_cache_size";
    public static final int      NodeMissCacheSize     = SystemTDB.NodeMissCacheSize;

    public static final String  fPrefixNode2NodeIdCacheSize  = "prefix_node2nodeid_cache_size";
    public static final int     PrefixNode2NodeIdCacheSize   =  SystemTDB.PrefixNode2NodeIdCacheSize;

    public static final String  fPrefixNodeId2NodeCacheSize  = "prefix_nodeid2node_cache_size";
    public static final int     PrefixNodeId2NodeCacheSize   = SystemTDB.PrefixNodeId2NodeCacheSize;

    public static final String  fPrefixNodeMissCacheSize  = "prefix_node_miss_cache_size";
    public static final int     PrefixNodeMissCacheSize   = SystemTDB.PrefixNodeMissCacheSize;

    /** Database layout - ignored after a database is created */
    public static final String   fBlockSize            = "block_size";
    public static final int      blockSize             = SystemTDB.BlockSize;

    public static final String   fNodeTableBaseName    = "nodetable";
    public static final String   nodeTableBaseName     = Names.nodeTableBaseName;

    public static final String   fPrimaryIndexTriples  = "triple_index_primary";
    public static final String   primaryIndexTriples   = Names.primaryIndexTriples;

    public static final String   fTripleIndexes        = "triple_indexes";
    public static final String[] tripleIndexes         = Names.tripleIndexes;

    public static final String   fPrimaryIndexQuads    = "quad_index_primary";
    public static final String   primaryIndexQuads     = Names.primaryIndexQuads;

    public static final String   fQuadIndexes          = "quad_indexes";
    public static final String[] quadIndexes           = Names.quadIndexes;

    public static final String   fPrefixTableBaseName  = "prefixtable";
    public static final String   prefixTableBaseName   = Names.prefixTableBaseName;

    public static final String   fPrimaryIndexPrefix   = "prefix_index_primary";
    public static final String   primaryIndexPrefix    = Names.primaryIndexPrefix;

    public static final String   fPrefixIndexes        = "prefix_indexes";
    public static final String[] prefixIndexes         = Names.prefixIndexes;

    // Must be after the constants above to get initialization order right
    // because StoreParamsBuilder uses these constants.

    /** The system default parameters for on-disk databases. */
    static StoreParams dftStoreParams = StoreParams.builder("DefaultStoreParams").build();

    /** The system default parameters for in-memory databases. */
    static StoreParams dftMemStoreParams = StoreParams.builder("DefaultMemStoreParams")
        // Small block caches, mainly so it behaves like a direct on-disk database.
        .blockReadCacheSize(10)
        .blockWriteCacheSize(10)
        .node2NodeIdCacheSize(10000)
        .nodeId2NodeCacheSize(10000)
        .nodeMissCacheSize(100)
        .build();

    /** The "small store" parameters. */
    static StoreParams smallStoreParams = StoreParams.builder("SmallStoreParams")
        //.fileMode(FileMode.direct)
        .blockReadCacheSize(100)
        .blockWriteCacheSize(100)
        .node2NodeIdCacheSize(10000)
        .nodeId2NodeCacheSize(10000)
        .nodeMissCacheSize(100)
        .build();

    /** The "small store" parameters (direct for 32bit mode JVMs). */
    static StoreParams smallStoreParams32 = StoreParams.builder("SmallStoreParams32")
        .fileMode(FileMode.direct)
        .blockReadCacheSize(100)
        .blockWriteCacheSize(100)
        .node2NodeIdCacheSize(10000)
        .nodeId2NodeCacheSize(10000)
        .nodeMissCacheSize(100)
        .build();
}

