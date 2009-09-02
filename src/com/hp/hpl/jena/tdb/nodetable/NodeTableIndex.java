/*
 * (c) Copyright 2008, 2009 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.tdb.nodetable;

import com.hp.hpl.jena.tdb.base.file.FileFactory;
import com.hp.hpl.jena.tdb.base.file.FileSet;
import com.hp.hpl.jena.tdb.base.objectfile.StringFile;
import com.hp.hpl.jena.tdb.base.objectfile.StringFileMem;
import com.hp.hpl.jena.tdb.index.Index;
import com.hp.hpl.jena.tdb.index.IndexBuilder;
import com.hp.hpl.jena.tdb.store.FactoryGraphTDB;
import com.hp.hpl.jena.tdb.sys.Names;

public class NodeTableIndex extends NodeTableBase
{
    // Disk version
    public NodeTableIndex(IndexBuilder indexBuilder, FileSet fsNodeToId, FileSet fsIdToNode, int nodeToIdCacheSize, int idToNodeCacheSize)
    {
        super() ;
        // Index.
        Index nodeToId = indexBuilder.newIndex(fsNodeToId, FactoryGraphTDB.nodeRecordFactory) ;
        // Node table.
        String filename = fsIdToNode.filename(Names.extNodeData) ;
        StringFile objects = FileFactory.createStringFileDisk(filename);
        init(nodeToId, objects, nodeToIdCacheSize, idToNodeCacheSize) ;
    }
    
    public NodeTableIndex(Index nodeToId, StringFile objects, int nodeToIdCacheSize, int idToNodeCacheSize)
    {
        super(nodeToId, objects, nodeToIdCacheSize, idToNodeCacheSize) ;
    }
    
    // Memory version - testing.
    private NodeTableIndex(IndexBuilder factory)
    {
        super() ;
        Index nodeToId = factory.newIndex(FileSet.mem(), FactoryGraphTDB.nodeRecordFactory) ;
        
        StringFile objects = new StringFileMem() ;
        init(nodeToId, objects, 100, 100) ;
    }

    public static NodeTable createMem(IndexBuilder indexBuilder)
    {
        return new NodeTableIndex(indexBuilder) ;
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