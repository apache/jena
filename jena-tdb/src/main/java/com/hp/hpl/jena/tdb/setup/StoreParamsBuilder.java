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

import com.hp.hpl.jena.tdb.base.block.FileMode ;

public class StoreParamsBuilder {
    // See also StoreParamsConst.
    /** Database and query configuration */ 
    // Key names are the base name -  encode/decode may add a prefix.
    
    private FileMode           fileMode              = StoreParamsConst.fileMode ;

    private int                blockReadCacheSize    = StoreParamsConst.blockReadCacheSize ;

    private int                blockWriteCacheSize   = StoreParamsConst.blockWriteCacheSize ;

    private int                Node2NodeIdCacheSize  = StoreParamsConst.Node2NodeIdCacheSize ;

    private int                NodeId2NodeCacheSize  = StoreParamsConst.NodeId2NodeCacheSize ;

    private int                NodeMissCacheSize     = StoreParamsConst.NodeMissCacheSize ;

    /** Database layout - ignored after a database is created */

    private int                blockSize             = StoreParamsConst.blockSize ;

    private String             indexNode2Id          = StoreParamsConst.indexNode2Id ;

    private String             indexId2Node          = StoreParamsConst.indexId2Node ;

    private String             primaryIndexTriples   = StoreParamsConst.primaryIndexTriples ;

    private String[]           tripleIndexes         = StoreParamsConst.tripleIndexes ;

    private String             primaryIndexQuads     = StoreParamsConst.primaryIndexQuads ;

    private String[]           quadIndexes           = StoreParamsConst.quadIndexes ;

    private String             primaryIndexPrefix    = StoreParamsConst.primaryIndexPrefix ;

    private String[]           prefixIndexes         = StoreParamsConst.prefixIndexes ;

    private String             indexPrefix           = StoreParamsConst.indexPrefix ;

    private String             prefixNode2Id         = StoreParamsConst.prefixNode2Id ;

    private String             prefixId2Node         = StoreParamsConst.prefixId2Node ;

    public static StoreParamsBuilder create() {
        return new StoreParamsBuilder() ;
    }

    public static StoreParamsBuilder create(StoreParams params) {
        return new StoreParamsBuilder(params) ;
    }

    /** Using a base set of {@linkplain StoreParams}, and update with dynamic parameters.
     * 
     * @param baseParams
     * @param additionalParams
     * @return StoreParams
     */
    
    public static StoreParams modify(StoreParams baseParams, StoreParamsDynamic additionalParams) {
        return new StoreParamsBuilder(baseParams)
            .fileMode(additionalParams.getFileMode())
            .blockSize(additionalParams.getBlockSize())
            .blockReadCacheSize(additionalParams.getBlockReadCacheSize())
            .blockWriteCacheSize(additionalParams.getBlockWriteCacheSize())
            .node2NodeIdCacheSize(additionalParams.getNode2NodeIdCacheSize())
            .nodeId2NodeCacheSize(additionalParams.getNodeId2NodeCacheSize())
            .nodeMissCacheSize(additionalParams.getNodeMissCacheSize())
            .build();
    }
    
    private StoreParamsBuilder() {}
    private StoreParamsBuilder(StoreParams other) {
        this.fileMode               = other.getFileMode() ;
        this.blockSize              = other.getBlockSize() ;
        this.blockReadCacheSize     = other.getBlockReadCacheSize() ;
        this.blockWriteCacheSize    = other.getBlockWriteCacheSize() ;
        this.Node2NodeIdCacheSize   = other.getNode2NodeIdCacheSize() ;
        this.NodeId2NodeCacheSize   = other.getNodeId2NodeCacheSize() ;
        this.NodeMissCacheSize      = other.getNodeMissCacheSize() ;

        this.indexNode2Id           = other.getIndexNode2Id() ;
        this.indexId2Node           = other.getIndexId2Node() ;
        
        this.primaryIndexTriples    = other.getPrimaryIndexTriples() ;
        this.tripleIndexes          = other.getTripleIndexes() ;
        
        this.primaryIndexQuads      = other.getPrimaryIndexQuads() ;
        this.quadIndexes            = other.getQuadIndexes() ;
        
        this.primaryIndexPrefix     = other.getPrimaryIndexPrefix() ;
        this.prefixIndexes          = other.getPrefixIndexes() ;
        this.indexPrefix            = other.getIndexPrefix() ;

        this.prefixNode2Id          = other.getPrefixNode2Id() ;
        this.prefixId2Node          = other.getPrefixId2Node() ;
    }
    
    public StoreParams build() {
        return new StoreParams(
                 fileMode, blockSize, blockReadCacheSize, blockWriteCacheSize, 
                 Node2NodeIdCacheSize, NodeId2NodeCacheSize, NodeMissCacheSize,
                 indexNode2Id, indexId2Node, primaryIndexTriples, tripleIndexes,
                 primaryIndexQuads, quadIndexes, primaryIndexPrefix,
                 prefixIndexes, indexPrefix,
                 prefixNode2Id, prefixId2Node) ;
    }
    
//    public SystemParams build(String filename) {
//        JsonObject obj = JSON.read(filename) ;
//        return null ;
//    }

    public FileMode getFileMode() {
        return fileMode ;
    }
    
    public StoreParamsBuilder fileMode(FileMode fileMode) {
        this.fileMode = fileMode ;
        return this ;
    }
    
    public int getBlockSize() {
        return blockSize ;
    }

    public StoreParamsBuilder blockSize(int blockSize) {
        this.blockSize = blockSize ;
        return this ;
    }

    public int getBlockReadCacheSize() {
        return blockReadCacheSize ;
    }

    public StoreParamsBuilder blockReadCacheSize(int blockReadCacheSize) {
        this.blockReadCacheSize = blockReadCacheSize ;
        return this ;
    }

    public int getBlockWriteCacheSize() {
        return blockWriteCacheSize ;
    }

   public StoreParamsBuilder blockWriteCacheSize(int blockWriteCacheSize) {
       this.blockWriteCacheSize = blockWriteCacheSize ;
       return this ;
   }

    public int getNode2NodeIdCacheSize() {
        return Node2NodeIdCacheSize ;
    }

   public StoreParamsBuilder node2NodeIdCacheSize(int node2NodeIdCacheSize) {
       Node2NodeIdCacheSize = node2NodeIdCacheSize ;
       return this ;
   }

    public int getNodeId2NodeCacheSize() {
        return NodeId2NodeCacheSize ;
    }

   public StoreParamsBuilder nodeId2NodeCacheSize(int nodeId2NodeCacheSize) {
       NodeId2NodeCacheSize = nodeId2NodeCacheSize ;
       return this ;
   }

    public int getNodeMissCacheSize() {
        return NodeMissCacheSize ;
    }

   public StoreParamsBuilder nodeMissCacheSize(int nodeMissCacheSize) {
       NodeMissCacheSize = nodeMissCacheSize ;
       return this ;
   }

    public String getIndexNode2Id() {
        return indexNode2Id ;
    }

   public StoreParamsBuilder indexNode2Id(String indexNode2Id) {
       this.indexNode2Id = indexNode2Id ;
       return this ;
   }

    public String getIndexId2Node() {
        return indexId2Node ;
    }

   public StoreParamsBuilder indexId2Node(String indexId2Node) {
       this.indexId2Node = indexId2Node ;
       return this ;
   }

    public String getPrimaryIndexTriples() {
        return primaryIndexTriples ;
    }

   public StoreParamsBuilder primaryIndexTriples(String primaryIndexTriples) {
       this.primaryIndexTriples = primaryIndexTriples ;
       return this ;
   }

    public String[] getTripleIndexes() {
        return tripleIndexes ;
    }

   public StoreParamsBuilder tripleIndexes(String[] tripleIndexes) {
       this.tripleIndexes = tripleIndexes ;
       return this ;
   }

   public StoreParamsBuilder tripleIndexes(int idx, String tripleIndex) {
       this.tripleIndexes[idx] = tripleIndex ;
       return this ;
   }

    public String getPrimaryIndexQuads() {
        return primaryIndexQuads ;
    }

   public StoreParamsBuilder primaryIndexQuads(String primaryIndexQuads) {
       this.primaryIndexQuads = primaryIndexQuads ;
       return this ;
   }

    public String[] getQuadIndexes() {
        return quadIndexes ;
    }

   public StoreParamsBuilder quadIndexes(int idx, String quadIndex) {
       this.quadIndexes[idx] = quadIndex ;
       return this ;
   }

   public StoreParamsBuilder quadIndexes(String[] quadIndexes) {
       this.quadIndexes = quadIndexes ;
       return this ;
   }

    public String getPrimaryIndexPrefix() {
        return primaryIndexPrefix ;
    }

   public StoreParamsBuilder primaryIndexPrefix(String primaryIndexPrefix) {
       this.primaryIndexPrefix = primaryIndexPrefix ;
       return this ;
   }

    public String[] getPrefixIndexes() {
        return prefixIndexes ;
    }

   public StoreParamsBuilder prefixIndexes(String[] prefixIndexes) {
       this.prefixIndexes = prefixIndexes ;
       return this ;
   }

   public StoreParamsBuilder prefixIndexes(int idx, String prefixIndex) {
       this.prefixIndexes[idx] = prefixIndex ;
       return this ;
   }

    public String getIndexPrefix() {
        return indexPrefix ;
    }

   public StoreParamsBuilder indexPrefix(String indexPrefix) {
       this.indexPrefix = indexPrefix ;
       return this ;
   }

    public String getPrefixNode2Id() {
        return prefixNode2Id ;
    }

   public StoreParamsBuilder prefixNode2Id(String prefixNode2Id) {
       this.prefixNode2Id = prefixNode2Id ;
       return this ;
   }

    public String getPrefixId2Node() {
        return prefixId2Node ;
    }

   public StoreParamsBuilder prefixId2Node(String prefixId2Node) {
       this.prefixId2Node = prefixId2Node ;
       return this ;
   }
}

