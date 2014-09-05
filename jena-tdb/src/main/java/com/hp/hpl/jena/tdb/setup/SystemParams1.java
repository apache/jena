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

import org.apache.jena.atlas.lib.StrUtils ;

import com.hp.hpl.jena.tdb.base.block.FileMode ;
import com.hp.hpl.jena.tdb.sys.Names ;
import com.hp.hpl.jena.tdb.sys.SystemTDB ;

/** System parameters for a TDB database instance. */
public class SystemParams1
{
    private FileMode fileMode             = SystemTDB.fileMode() ;
    private int      blockSize            = SystemTDB.BlockSize ;
    private int      memBlockSize         = SystemTDB.BlockSizeTestMem ;
    private int      readCacheSize        = SystemTDB.BlockReadCacheSize ;
    private int      writeCacheSize       = SystemTDB.BlockWriteCacheSize ;
    private int      Node2NodeIdCacheSize = SystemTDB.Node2NodeIdCacheSize ;
    private int      NodeId2NodeCacheSize = SystemTDB.NodeId2NodeCacheSize ;
    private int      NodeMissCacheSize    = SystemTDB.NodeMissCacheSize ;

    private String   indexNode2Id         = Names.indexNode2Id ;
    private String   indexId2Node         = Names.indexId2Node ;
    private String   primaryIndexTriples  = Names.primaryIndexTriples ;
    private String[] tripleIndexes        = Names.tripleIndexes ;
    private String   primaryIndexQuads    = Names.primaryIndexQuads ;
    private String[] quadIndexes          = Names.quadIndexes ;
    private String   primaryIndexPrefix   = Names.primaryIndexPrefix ;
    private String[] prefixIndexes        = Names.prefixIndexes ;
    private String   indexPrefix          = Names.indexPrefix ;

    private String   prefixNode2Id        = Names.prefixNode2Id ;
    private String   prefixId2Node        = Names.prefixId2Node ;
    
    public SystemParams1() {}
    
    public static SystemParams1 getStdSystemParams() {
        return new SystemParams1() ;
    }
    
    @Override
    public String toString() {
        StringBuilder buff = new StringBuilder() ;
        fmt(buff, "fileMode", fileMode.toString()) ;
        fmt(buff, "blockSize", getBlockSize()) ;
        fmt(buff, "memBlockSize", getMemBlockSize()) ;
        fmt(buff, "readCacheSize", getReadCacheSize()) ;
        fmt(buff, "writeCacheSize", getWriteCacheSize()) ;
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

    public FileMode getFileMode() {
        return fileMode ;
    }

    public void setFileMode(FileMode fileMode) {
        this.fileMode = fileMode ;
    }

    public int getBlockSize() {
        return blockSize ;
    }

    public void setBlockSize(int blockSize) {
        this.blockSize = blockSize;
    }

    public int getMemBlockSize() {
        return memBlockSize;
    }

    public void setMemBlockSize(int memBlockSize) {
        this.memBlockSize = memBlockSize;
    }

    public int getReadCacheSize() {
        return readCacheSize;
    }

    public void setReadCacheSize(int readCacheSize) {
        this.readCacheSize = readCacheSize;
    }

    public int getWriteCacheSize() {
        return writeCacheSize;
    }

    public void setWriteCacheSize(int writeCacheSize) {
        this.writeCacheSize = writeCacheSize;
    }

    public int getNode2NodeIdCacheSize() {
        return Node2NodeIdCacheSize;
    }

    public void setNode2NodeIdCacheSize(int node2NodeIdCacheSize) {
        Node2NodeIdCacheSize = node2NodeIdCacheSize;
    }

    public int getNodeId2NodeCacheSize() {
        return NodeId2NodeCacheSize;
    }

    public void setNodeId2NodeCacheSize(int nodeId2NodeCacheSize) {
        NodeId2NodeCacheSize = nodeId2NodeCacheSize;
    }

    public int getNodeMissCacheSize() {
        return NodeMissCacheSize;
    }

    public void setNodeMissCacheSize(int nodeMissCacheSize) {
        NodeMissCacheSize = nodeMissCacheSize;
    }

    public String getIndexNode2Id() {
        return indexNode2Id;
    }

    public void setIndexNode2Id(String indexNode2Id) {
        this.indexNode2Id = indexNode2Id;
    }

    public String getIndexId2Node() {
        return indexId2Node;
    }

    public void setIndexId2Node(String indexId2Node) {
        this.indexId2Node = indexId2Node;
    }

    public String getPrimaryIndexTriples() {
        return primaryIndexTriples;
    }

    public void setPrimaryIndexTriples(String primaryIndexTriples) {
        this.primaryIndexTriples = primaryIndexTriples;
    }

    public String[] getTripleIndexes() {
        return tripleIndexes;
    }

    public void setTripleIndexes(String[] tripleIndexes) {
        this.tripleIndexes = tripleIndexes;
    }

    public String getPrimaryIndexQuads() {
        return primaryIndexQuads;
    }

    public void setPrimaryIndexQuads(String primaryIndexQuads) {
        this.primaryIndexQuads = primaryIndexQuads;
    }

    public String[] getQuadIndexes() {
        return quadIndexes;
    }

    public void setQuadIndexes(String[] quadIndexes) {
        this.quadIndexes = quadIndexes;
    }

    public String getPrimaryIndexPrefix() {
        return primaryIndexPrefix;
    }

    public void setPrimaryIndexPrefix(String primaryIndexPrefix) {
        this.primaryIndexPrefix = primaryIndexPrefix;
    }

    public String[] getPrefixIndexes() {
        return prefixIndexes;
    }

    public void setPrefixIndexes(String[] prefixIndexes) {
        this.prefixIndexes = prefixIndexes;
    }

    public String getIndexPrefix() {
        return indexPrefix;
    }

    public void setIndexPrefix(String indexPrefix) {
        this.indexPrefix = indexPrefix;
    }

    public String getPrefixNode2Id() {
        return prefixNode2Id;
    }

    public void setPrefixNode2Id(String prefixNode2Id) {
        this.prefixNode2Id = prefixNode2Id;
    }

    public String getPrefixId2Node() {
        return prefixId2Node;
    }

    public void setPrefixId2Node(String prefixId2Node) {
        this.prefixId2Node = prefixId2Node;
    }
    
    
}
