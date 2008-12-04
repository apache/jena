/*
 * (c) Copyright 2008 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.tdb.nodetable;

import static com.hp.hpl.jena.tdb.sys.SystemTDB.Node2NodeIdCacheSize;
import static com.hp.hpl.jena.tdb.sys.SystemTDB.NodeId2NodeCacheSize;

import com.hp.hpl.jena.tdb.base.file.FileFactory;
import com.hp.hpl.jena.tdb.base.file.Location;
import com.hp.hpl.jena.tdb.base.objectfile.ObjectFile;
import com.hp.hpl.jena.tdb.base.objectfile.ObjectFileMem;
import com.hp.hpl.jena.tdb.index.Index;
import com.hp.hpl.jena.tdb.index.IndexBuilder;
import com.hp.hpl.jena.tdb.store.FactoryGraphTDB;
import com.hp.hpl.jena.tdb.sys.Names;

public class NodeTableIndex extends NodeTableBase
{
    // Disk version
    public NodeTableIndex(IndexBuilder factory, Location loc)
    {
        super() ;
        Index nodeToId = factory.newIndex(loc, FactoryGraphTDB.nodeRecordFactory, Names.indexNode2Id) ;
            
        // Data file.
        ObjectFile objects = FileFactory.createObjectFileDisk(loc.getPath(Names.nodesData));
        init(nodeToId, objects, Node2NodeIdCacheSize, NodeId2NodeCacheSize) ;
    }
    
    // Memory version - testing.
    public NodeTableIndex(IndexBuilder factory)
    {
        super() ;
        Index nodeToId = factory.newIndex(null, FactoryGraphTDB.nodeRecordFactory, Names.indexNode2Id) ;
        
        ObjectFile objects = new ObjectFileMem() ;
        init(nodeToId, objects, 100, 100) ;
    }
}
/*
 * (c) Copyright 2008 Hewlett-Packard Development Company, LP
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