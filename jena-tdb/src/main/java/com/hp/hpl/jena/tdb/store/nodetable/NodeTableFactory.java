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

package com.hp.hpl.jena.tdb.store.nodetable;

import com.hp.hpl.jena.tdb.base.file.FileFactory ;
import com.hp.hpl.jena.tdb.base.file.FileSet ;
import com.hp.hpl.jena.tdb.base.file.Location ;
import com.hp.hpl.jena.tdb.base.objectfile.ObjectFile ;
import com.hp.hpl.jena.tdb.index.Index ;
import com.hp.hpl.jena.tdb.setup.B ;
import com.hp.hpl.jena.tdb.setup.IndexBuilder ;
import com.hp.hpl.jena.tdb.setup.SystemParams ;
import com.hp.hpl.jena.tdb.sys.Names ;
import com.hp.hpl.jena.tdb.sys.SystemTDB ;

public class NodeTableFactory
{
    /** Regular node table */
    @Deprecated
    public static NodeTable create(IndexBuilder indexBuilder, Location location)
    {
        // XXX FIX up names - add SystemParams arg
        // The node table (id to node).
        FileSet filesetNodeTable = null ;
        if ( location != null )
            filesetNodeTable = new FileSet(location, Names.indexId2Node) ;
        
        // The index of node to id
        FileSet filesetIdx = null ;
        if ( location != null )
            filesetIdx = new FileSet(location, Names.indexNode2Id) ;
        
        SystemParams params = SystemParams.getDftSystemParams() ;
        
        return create(indexBuilder, filesetNodeTable, filesetIdx, params) ;
    }

    /** Custom node table */
    public static NodeTable create(IndexBuilder indexBuilder, 
                                   FileSet fsIdToNode, FileSet fsNodeToId,
                                   SystemParams params)
    {
        String filename = fsIdToNode.filename(Names.extNodeData) ;
        
        if ( fsNodeToId.isMem() )
        {
            Index nodeToId = indexBuilder.buildIndex(fsNodeToId, SystemTDB.nodeRecordFactory, params) ;
            ObjectFile objects = FileFactory.createObjectFileMem(filename) ;
            NodeTable nodeTable = new NodeTableNative(nodeToId, objects) ;
            
            nodeTable = NodeTableCache.create(nodeTable, 100, 100, 10) ; 
            nodeTable =  NodeTableInline.create(nodeTable) ;
            
            return nodeTable ;
        }
        
        Index nodeToId = indexBuilder.buildIndex(fsNodeToId, SystemTDB.nodeRecordFactory, params) ;
        // Node table.
        ObjectFile objects = FileFactory.createObjectFileDisk(filename);
        NodeTable nodeTable = new NodeTableNative(nodeToId, objects) ;
        nodeTable = NodeTableCache.create(nodeTable, params) ; 
        nodeTable = NodeTableInline.create(nodeTable) ;
        return nodeTable ;
        
    }

    public static NodeTable createMem() {
        return create(B.createIndexBuilderMem(), FileSet.mem(), FileSet.mem(), SystemParams.getDftSystemParams()) ;
    }
}
