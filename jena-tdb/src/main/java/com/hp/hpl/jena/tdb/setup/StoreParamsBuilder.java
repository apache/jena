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
import com.hp.hpl.jena.tdb.sys.Names ;
import com.hp.hpl.jena.tdb.sys.SystemTDB ;

public class StoreParamsBuilder {
    // Initial values are the system defaults.
    
    /** Database and query configuration */ 
    
    // public static String fFileMode             = "FileMode" ;
    private FileMode     fileMode              = SystemTDB.fileMode() ;

    // public static String fReadCacheSize        = "blockReadCacheSize" ;
    private int          blockReadCacheSize    = SystemTDB.BlockReadCacheSize ;

    // public static String fWriteCacheSize       = "blockWriteCacheSize" ;
    private int          blockWriteCacheSize   = SystemTDB.BlockWriteCacheSize ;

    // public static String fNode2NodeIdCacheSize = "Node2NodeIdCacheSize" ;
    private int          Node2NodeIdCacheSize  = SystemTDB.Node2NodeIdCacheSize ;

    // public static String fNodeId2NodeCacheSize = "NodeId2NodeCacheSize" ;
    private int          NodeId2NodeCacheSize  = SystemTDB.NodeId2NodeCacheSize ;

    // public static String fNodeMissCacheSize    = "NodeMissCacheSize" ;
    private int          NodeMissCacheSize     = SystemTDB.NodeMissCacheSize ;

    /** Database layout - ignored after a database is created */

    // public static String fBlockSize            = "blockSize" ;
    private int          blockSize             = SystemTDB.BlockSize ;

    // public static String fIndexNode2Id         = "indexNode2Id" ;
    private String       indexNode2Id          = Names.indexNode2Id ;

    // public static String fIndexId2Node         = "indexId2Node" ;
    private String       indexId2Node          = Names.indexId2Node ;

    // public static String fPrimaryIndexTriples  = "primaryIndexTriples" ;
    private String       primaryIndexTriples   = Names.primaryIndexTriples ;

    // public static String fTripleIndexes        = "tripleIndexes" ;
    private String[]     tripleIndexes         = Names.tripleIndexes ;

    // public static String fPrimaryIndexQuads    = "primaryIndexQuads" ;
    private String       primaryIndexQuads     = Names.primaryIndexQuads ;

    // public static String fQuadIndexes          = "quadIndexes" ;
    private String[]     quadIndexes           = Names.quadIndexes ;

    // public static String fPrimaryIndexPrefix   = "primaryIndexPrefix" ;
    private String       primaryIndexPrefix    = Names.primaryIndexPrefix ;

    // public static String fPrefixIndexes        = "prefixIndexes" ;
    private String[]     prefixIndexes         = Names.prefixIndexes ;

    // public static String fIndexPrefix          = "indexPrefix" ;
    private String       indexPrefix           = Names.indexPrefix ;

    // public static String fPrefixNode2Id        = "prefixNode2Id" ;
    private String       prefixNode2Id         = Names.prefixNode2Id ;

    // public static String fPrefixId2Node        = "prefixId2Node" ;
    private String       prefixId2Node         = Names.prefixId2Node ;

    public static StoreParamsBuilder create() { return new StoreParamsBuilder() ; }
    
    public StoreParamsBuilder() {}
    public StoreParamsBuilder(StoreParams other) {
        this.fileMode               = other.getFileMode() ;
        this.blockSize              = other.getBlockSize() ;
        this.blockReadCacheSize     = other.getBlockReadCacheSize() ;
        this.blockWriteCacheSize    = other.getBlockWriteCacheSize() ;
        this.Node2NodeIdCacheSize   = other.getNode2NodeIdCacheSize() ;
        this.NodeId2NodeCacheSize   = other.getNodeId2NodeCacheSize() ;
        this.NodeMissCacheSize      = other.getNodeMissCacheSize() ;

        this.indexNode2Id           = other.getIndexNode2Id() ;
        this.indexId2Node           = other.getIndexId2Node() ;
        this.primaryIndexTriples    = other.getPrimaryIndexPrefix() ;
        this.tripleIndexes          = other.getQuadIndexes() ;
        this.primaryIndexQuads      = other.getPrimaryIndexPrefix() ;
        this.quadIndexes            = other.getPrefixIndexes() ;
        this.primaryIndexPrefix     = other.getIndexPrefix() ;
        this.prefixIndexes          = other.getPrefixIndexes() ;
        this.indexPrefix            = other.getIndexPrefix() ;

        this.prefixNode2Id          = other.getIndexNode2Id() ;
        this.prefixId2Node          = other.getIndexId2Node() ;
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

