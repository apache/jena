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

import java.util.Arrays ;

import org.apache.jena.atlas.lib.StrUtils ;

import com.hp.hpl.jena.tdb.base.block.FileMode ;
import com.hp.hpl.jena.tdb.index.IndexParams ;

/** System parameters for a TDB database instance. */
public class StoreParams implements IndexParams, StoreParamsDynamic
{
    // SystemParams are built with a SystemParamsBuilder
    
    private static StoreParams dftStoreParams = StoreParamsBuilder.create().build() ;
    
    /* These are items you can change JVM to JVM */
    
    private final FileMode fileMode ;
    private final int      blockReadCacheSize ;
    private final int      blockWriteCacheSize ;
    private final int      Node2NodeIdCacheSize ;
    private final int      NodeId2NodeCacheSize ;
    private final int      NodeMissCacheSize ;

    /* These are items affect database layout and
     * only can be applied when a database is created.
     * They do not affect existing databases.
     * If you want to, say, change the index structure,
     * you'll need to use the index tools.  
     */
    private final int      blockSize ;
    private final String   indexNode2Id ;
    private final String   indexId2Node ;
    private final String   primaryIndexTriples ;
    private final String[] tripleIndexes ;
    private final String   primaryIndexQuads ;
    private final String[] quadIndexes ;
    private final String   primaryIndexPrefix ;
    private final String[] prefixIndexes ;
    private final String   indexPrefix ;

    private final String   prefixNode2Id ;
    private final String   prefixId2Node ;
    
    public StoreParams(FileMode fileMode, int blockSize, int blockReadCacheSize, int blockWriteCacheSize,
                       int node2NodeIdCacheSize, int nodeId2NodeCacheSize, int nodeMissCacheSize,
                       String indexNode2Id, String indexId2Node, 
                       String primaryIndexTriples, String[] tripleIndexes,
                       String primaryIndexQuads, String[] quadIndexes,
                       String primaryIndexPrefix, String[] prefixIndexes, String indexPrefix, 
                       String prefixNode2Id, String prefixId2Node) {
        this.fileMode               = fileMode ;
        this.blockSize              = blockSize ;
        this.blockReadCacheSize     = blockReadCacheSize ;
        this.blockWriteCacheSize    = blockWriteCacheSize ;
        this.Node2NodeIdCacheSize   = node2NodeIdCacheSize ;
        this.NodeId2NodeCacheSize   = nodeId2NodeCacheSize ;
        this.NodeMissCacheSize      = nodeMissCacheSize ;

        this.indexNode2Id           = indexNode2Id ;
        this.indexId2Node           = indexId2Node ;
        this.primaryIndexTriples    = primaryIndexTriples ;
        this.tripleIndexes          = tripleIndexes ;
        this.primaryIndexQuads      = primaryIndexQuads ;
        this.quadIndexes            = quadIndexes ;
        this.primaryIndexPrefix     = primaryIndexPrefix ;
        this.prefixIndexes          = prefixIndexes ;
        this.indexPrefix            = indexPrefix ;

        this.prefixNode2Id          = prefixNode2Id ;
        this.prefixId2Node          = prefixId2Node ;
    }

    public static StoreParams getDftStoreParams() {
        return dftStoreParams ;
    }

    @Override
    public FileMode getFileMode() {
        return fileMode ;
    }
    
    @Override
    public int getBlockSize() {
        return blockSize ;
    }

    @Override
    public int getBlockReadCacheSize() {
        return blockReadCacheSize ;
    }

    @Override
    public int getBlockWriteCacheSize() {
        return blockWriteCacheSize ;
    }

    @Override
    public int getNode2NodeIdCacheSize() {
        return Node2NodeIdCacheSize ;
    }

    @Override
    public int getNodeId2NodeCacheSize() {
        return NodeId2NodeCacheSize ;
    }

    @Override
    public int getNodeMissCacheSize() {
        return NodeMissCacheSize ;
    }

    public String getIndexNode2Id() {
        return indexNode2Id ;
    }

    public String getIndexId2Node() {
        return indexId2Node ;
    }

    public String getPrimaryIndexTriples() {
        return primaryIndexTriples ;
    }

    public String[] getTripleIndexes() {
        return tripleIndexes ;
    }

    public String getPrimaryIndexQuads() {
        return primaryIndexQuads ;
    }

    public String[] getQuadIndexes() {
        return quadIndexes ;
    }

    public String getPrimaryIndexPrefix() {
        return primaryIndexPrefix ;
    }

    public String[] getPrefixIndexes() {
        return prefixIndexes ;
    }

    public String getIndexPrefix() {
        return indexPrefix ;
    }

    public String getPrefixNode2Id() {
        return prefixNode2Id ;
    }

    public String getPrefixId2Node() {
        return prefixId2Node ;
    }

    @Override
    public String toString() {
        StringBuilder buff = new StringBuilder() ;
        fmt(buff, "fileMode", getFileMode().toString()) ;
        fmt(buff, "blockSize", getBlockSize()) ;
        fmt(buff, "readCacheSize", getBlockReadCacheSize()) ;
        fmt(buff, "writeCacheSize", getBlockWriteCacheSize()) ;
        fmt(buff, "Node2NodeIdCacheSize", getNode2NodeIdCacheSize()) ;
        fmt(buff, "NodeId2NodeCacheSize", getNodeId2NodeCacheSize()) ;
        fmt(buff, "NodeMissCacheSize", getNodeMissCacheSize()) ;

        fmt(buff, "indexNode2Id", getIndexNode2Id()) ;
        fmt(buff, "indexId2Node", getIndexId2Node()) ;
        fmt(buff, "primaryIndexTriples", getPrimaryIndexTriples()) ;
        fmt(buff, "tripleIndexes", getTripleIndexes()) ;
        fmt(buff, "primaryIndexQuads", getPrimaryIndexQuads()) ;
        fmt(buff, "quadIndexes", getQuadIndexes()) ;
        fmt(buff, "primaryIndexPrefix", getPrimaryIndexPrefix()) ;
        fmt(buff, "prefixIndexes", getPrefixIndexes()) ;
        fmt(buff, "indexPrefix", getIndexPrefix()) ;

        fmt(buff, "prefixNode2Id", getPrefixNode2Id()) ;
        fmt(buff, "prefixId2Node", getPrefixId2Node()) ;
        
        return buff.toString() ;
    }
    
    private void fmt(StringBuilder buff, String name, String[] strings) {
        buff.append(String.format("%-20s   [%s]\n", name, StrUtils.strjoin(", ", strings))) ;
    }

    private void fmt(StringBuilder buff, String name, String value) {
        buff.append(String.format("%-20s   %s\n", name, value)) ;
    }

    private void fmt(StringBuilder buff, String name, int value) {
        buff.append(String.format("%-20s   %s\n", name, value)) ;
    }

    @Override
    public int hashCode() {
        final int prime = 31 ;
        int result = 1 ;
        result = prime * result + Node2NodeIdCacheSize ;
        result = prime * result + NodeId2NodeCacheSize ;
        result = prime * result + NodeMissCacheSize ;
        result = prime * result + blockReadCacheSize ;
        result = prime * result + blockSize ;
        result = prime * result + blockWriteCacheSize ;
        result = prime * result + ((fileMode == null) ? 0 : fileMode.hashCode()) ;
        result = prime * result + ((indexId2Node == null) ? 0 : indexId2Node.hashCode()) ;
        result = prime * result + ((indexNode2Id == null) ? 0 : indexNode2Id.hashCode()) ;
        result = prime * result + ((indexPrefix == null) ? 0 : indexPrefix.hashCode()) ;
        result = prime * result + ((prefixId2Node == null) ? 0 : prefixId2Node.hashCode()) ;
        result = prime * result + Arrays.hashCode(prefixIndexes) ;
        result = prime * result + ((prefixNode2Id == null) ? 0 : prefixNode2Id.hashCode()) ;
        result = prime * result + ((primaryIndexPrefix == null) ? 0 : primaryIndexPrefix.hashCode()) ;
        result = prime * result + ((primaryIndexQuads == null) ? 0 : primaryIndexQuads.hashCode()) ;
        result = prime * result + ((primaryIndexTriples == null) ? 0 : primaryIndexTriples.hashCode()) ;
        result = prime * result + Arrays.hashCode(quadIndexes) ;
        result = prime * result + Arrays.hashCode(tripleIndexes) ;
        return result ;
    }

    @Override
    public boolean equals(Object obj) {
        if ( this == obj )
            return true ;
        if ( obj == null )
            return false ;
        if ( getClass() != obj.getClass() )
            return false ;
        StoreParams other = (StoreParams)obj ;
        if ( Node2NodeIdCacheSize != other.Node2NodeIdCacheSize )
            return false ;
        if ( NodeId2NodeCacheSize != other.NodeId2NodeCacheSize )
            return false ;
        if ( NodeMissCacheSize != other.NodeMissCacheSize )
            return false ;
        if ( blockReadCacheSize != other.blockReadCacheSize )
            return false ;
        if ( blockSize != other.blockSize )
            return false ;
        if ( blockWriteCacheSize != other.blockWriteCacheSize )
            return false ;
        if ( fileMode != other.fileMode )
            return false ;
        if ( indexId2Node == null ) {
            if ( other.indexId2Node != null )
                return false ;
        } else if ( !indexId2Node.equals(other.indexId2Node) )
            return false ;
        if ( indexNode2Id == null ) {
            if ( other.indexNode2Id != null )
                return false ;
        } else if ( !indexNode2Id.equals(other.indexNode2Id) )
            return false ;
        if ( indexPrefix == null ) {
            if ( other.indexPrefix != null )
                return false ;
        } else if ( !indexPrefix.equals(other.indexPrefix) )
            return false ;
        if ( prefixId2Node == null ) {
            if ( other.prefixId2Node != null )
                return false ;
        } else if ( !prefixId2Node.equals(other.prefixId2Node) )
            return false ;
        if ( !Arrays.equals(prefixIndexes, other.prefixIndexes) )
            return false ;
        if ( prefixNode2Id == null ) {
            if ( other.prefixNode2Id != null )
                return false ;
        } else if ( !prefixNode2Id.equals(other.prefixNode2Id) )
            return false ;
        if ( primaryIndexPrefix == null ) {
            if ( other.primaryIndexPrefix != null )
                return false ;
        } else if ( !primaryIndexPrefix.equals(other.primaryIndexPrefix) )
            return false ;
        if ( primaryIndexQuads == null ) {
            if ( other.primaryIndexQuads != null )
                return false ;
        } else if ( !primaryIndexQuads.equals(other.primaryIndexQuads) )
            return false ;
        if ( primaryIndexTriples == null ) {
            if ( other.primaryIndexTriples != null )
                return false ;
        } else if ( !primaryIndexTriples.equals(other.primaryIndexTriples) )
            return false ;
        if ( !Arrays.equals(quadIndexes, other.quadIndexes) )
            return false ;
        if ( !Arrays.equals(tripleIndexes, other.tripleIndexes) )
            return false ;
        return true ;
    }

}

