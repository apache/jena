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

package com.hp.hpl.jena.tdb.nodetable;

import com.hp.hpl.jena.tdb.base.file.FileFactory ;
import com.hp.hpl.jena.tdb.base.file.FileSet ;
import com.hp.hpl.jena.tdb.base.file.Location ;
import com.hp.hpl.jena.tdb.base.objectfile.ObjectFile ;
import com.hp.hpl.jena.tdb.index.Index ;
import com.hp.hpl.jena.tdb.index.IndexBuilder ;
import com.hp.hpl.jena.tdb.setup.SystemParams ;
import com.hp.hpl.jena.tdb.sys.Names ;
import com.hp.hpl.jena.tdb.sys.SystemTDB ;

public class NodeTableFactory
{
    /** Regular node table */
    @Deprecated
    public static NodeTable create(IndexBuilder indexBuilder, Location location)
    {
        // The node table (id to node).
        FileSet filesetNodeTable = null ;
        if ( location != null )
            filesetNodeTable = new FileSet(location, Names.indexId2Node) ;
        
        // The index of node to id
        FileSet filesetIdx = null ;
        if ( location != null )
            filesetIdx = new FileSet(location, Names.indexNode2Id) ;
        
        SystemParams params = SystemParams.getStdSystemParams() ;
        
        return  create(indexBuilder, filesetNodeTable, filesetIdx,
                       params.Node2NodeIdCacheSize ,
                       params.NodeId2NodeCacheSize ,
                       params.NodeMissCacheSize ) ;
    }

    /** Custom node table */
    public static NodeTable create(IndexBuilder indexBuilder, 
                                   FileSet fsIdToNode, FileSet fsNodeToId,
                                   int nodeToIdCacheSize, int idToNodeCacheSize, int nodeMissCacheSize)
    {
        String filename = fsIdToNode.filename(Names.extNodeData) ;
        
        if ( fsNodeToId.isMem() )
        {
            Index nodeToId = indexBuilder.newIndex(FileSet.mem(), SystemTDB.nodeRecordFactory) ;
            ObjectFile objects = FileFactory.createObjectFileMem(filename) ;
            NodeTable nodeTable = new NodeTableNative(nodeToId, objects) ;
            
            nodeTable = NodeTableCache.create(nodeTable, 100, 100, 100) ; 
            nodeTable =  NodeTableInline.create(nodeTable) ;
            
            return nodeTable ;
            
            //return NodeTableIndex.createMem(indexBuilder) ;
        }
        
        Index nodeToId = indexBuilder.newIndex(fsNodeToId, SystemTDB.nodeRecordFactory) ;
        // Node table.
        ObjectFile objects = FileFactory.createObjectFileDisk(filename);
        NodeTable nodeTable = new NodeTableNative(nodeToId, objects) ;
        nodeTable = NodeTableCache.create(nodeTable, nodeToIdCacheSize, idToNodeCacheSize, nodeMissCacheSize) ; 
        nodeTable = NodeTableInline.create(nodeTable) ;
        return nodeTable ;
        
    }

    public static NodeTable createMem(IndexBuilder indexBuilder)
    {
        return create(indexBuilder, FileSet.mem(), FileSet.mem(), 100, 100, 10) ;
    }
    
    public static NodeTable createSink(IndexBuilder indexBuilder, Location location)
    {
        return new NodeTableSink() ;
    }
}
