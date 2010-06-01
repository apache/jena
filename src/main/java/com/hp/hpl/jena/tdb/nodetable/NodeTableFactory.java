/*
 * (c) Copyright 2008, 2009 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * (c) Copyright 2010 IBM Corp. All rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.tdb.nodetable;

import com.hp.hpl.jena.tdb.base.file.FileFactory;
import com.hp.hpl.jena.tdb.base.file.FileSet;
import com.hp.hpl.jena.tdb.base.file.Location;
import com.hp.hpl.jena.tdb.base.objectfile.ObjectFile;
import com.hp.hpl.jena.tdb.index.Index;
import com.hp.hpl.jena.tdb.index.IndexBuilder;
import com.hp.hpl.jena.tdb.sys.Names;
import com.hp.hpl.jena.tdb.sys.SetupTDB;
import com.hp.hpl.jena.tdb.sys.SystemTDB;

public class NodeTableFactory
{
//    private class NodeTableBuilderStd implements NodeTableBuilder 
//    {
//        private IndexBuilder indexBuilder ;
//
//        NodeTableBuilderStd(IndexBuilder indexBuilder)
//        {
//            this.indexBuilder = indexBuilder ; 
//        }
//        
//        public NodeTable create(FileSet id2Node, FileSet node2Id)
//        {
//            return new NodeTableIndex(indexBuilder, id2Node, node2Id, SystemTDB.Node2NodeIdCacheSize, SystemTDB.NodeId2NodeCacheSize) ;
//        }
//    }
    
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
        
        return  create(indexBuilder, filesetNodeTable, filesetIdx,
                       SetupTDB.systemInfo.getNode2NodeIdCacheSize(),
                       SetupTDB.systemInfo.getNodeId2NodeCacheSize()) ;
    }

    /** Custom node table */
    public static NodeTable create(IndexBuilder indexBuilder, 
                                   FileSet fsIdToNode, FileSet fsNodeToId,
                                   int nodeToIdCacheSize, int idToNodeCacheSize)
    {
        if ( fsNodeToId.isMem() )
        {
            Index nodeToId = indexBuilder.newIndex(FileSet.mem(), SystemTDB.nodeRecordFactory) ;
            ObjectFile objects = FileFactory.createObjectFileMem() ;
            NodeTable nodeTable = new NodeTableNative(nodeToId, objects) ;
            
            nodeTable = NodeTableCache.create(nodeTable, 100, 100) ; 
            nodeTable =  NodeTableInline.create(nodeTable) ;
            
            return nodeTable ;
            
            //return NodeTableIndex.createMem(indexBuilder) ;
        }
        
        Index nodeToId = indexBuilder.newIndex(fsNodeToId, SystemTDB.nodeRecordFactory) ;
        // Node table.
        String filename = fsIdToNode.filename(Names.extNodeData) ;
        ObjectFile objects = FileFactory.createObjectFileDisk(filename);
        NodeTable nodeTable = new NodeTableNative(nodeToId, objects) ;
        nodeTable = NodeTableCache.create(nodeTable, nodeToIdCacheSize, idToNodeCacheSize) ; 
        nodeTable = NodeTableInline.create(nodeTable) ;
        return nodeTable ;
        
    }

    public static NodeTable createMem(IndexBuilder indexBuilder)
    {
        return create(indexBuilder, FileSet.mem(), FileSet.mem(), 100, 100) ;
    }
    
    public static NodeTable createSink(IndexBuilder indexBuilder, Location location)
    {
        return new NodeTableSink() ;
    }
}

/*
 * (c) Copyright 2008, 2009 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * (c) Copyright 2010 IBM Corp. All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in the
 *    documentation and/or other materials provided with the distribution.
 * 3. The name of the author may not be used to endorse or promote products
 *    derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE AUTHOR ``AS IS'' AND ANY EXPRESS OR
 * IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.
 * IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR ANY DIRECT, INDIRECT,
 * INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT
 * NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 * THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF
 * THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */