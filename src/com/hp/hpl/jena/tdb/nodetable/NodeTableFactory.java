/*
 * (c) Copyright 2008, 2009 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.tdb.nodetable;

import com.hp.hpl.jena.tdb.base.file.FileSet;
import com.hp.hpl.jena.tdb.base.file.Location;
import com.hp.hpl.jena.tdb.index.IndexBuilder;
import com.hp.hpl.jena.tdb.sys.Names;
import com.hp.hpl.jena.tdb.sys.SystemTDB;

public class NodeTableFactory
{
    /** Regular node table */
    public static NodeTable create(IndexBuilder indexBuilder, Location location)
    {
        // The index of node to id
        FileSet filesetIdx = null ;
        if ( location != null )
            filesetIdx = new FileSet(location, Names.indexNode2Id) ;
        
        FileSet filesetNodeTable = null ;
        if ( location != null )
            filesetNodeTable = new FileSet(location, Names.nodeTable) ;
        
        // The node table (id to node).
        return  create(indexBuilder, filesetIdx, filesetNodeTable,
                       SystemTDB.Node2NodeIdCacheSize,
                       SystemTDB.NodeId2NodeCacheSize) ;
    }

    /** Custom node table */
    public static NodeTable create(IndexBuilder indexBuilder, FileSet filesetIdx, FileSet filesetNodeTable,
                                   int nodeToIdCacheSize, int idToNodeCacheSize)
    {
        if ( filesetIdx.isMem() )
            return NodeTableIndex.createMem(indexBuilder) ;
        
        return new NodeTableIndex(indexBuilder, filesetIdx, filesetNodeTable, nodeToIdCacheSize, idToNodeCacheSize) ;
    }

    public static NodeTable createMem(IndexBuilder indexBuilder)
    {
        return NodeTableIndex.createMem(indexBuilder) ;
    }
    
    public static NodeTable createSink(IndexBuilder indexBuilder, Location location)
    {
        return new NodeTableSink() ;
    }
}

/*
 * (c) Copyright 2008, 2009 Hewlett-Packard Development Company, LP
 * All rights reserved.
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