/*
 * (c) Copyright 2008 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.tdb.pgraph;

import static com.hp.hpl.jena.tdb.Const.NodeCacheSize;

import com.hp.hpl.jena.tdb.Const;
import com.hp.hpl.jena.tdb.base.block.BlockMgr;
import com.hp.hpl.jena.tdb.base.block.BlockMgrFactory;
import com.hp.hpl.jena.tdb.base.file.FileFactory;
import com.hp.hpl.jena.tdb.base.file.Location;
import com.hp.hpl.jena.tdb.base.file.ObjectFile;
import com.hp.hpl.jena.tdb.base.file.ObjectFileMem;
import com.hp.hpl.jena.tdb.btree.BTree;
import com.hp.hpl.jena.tdb.btree.BTreeParams;

public class NodeTableStd extends NodeTableBase
{
    // Disk version
    public NodeTableStd(Location loc)
    {
        super() ;

        // IndexFactory
        int order = BTreeParams.calcOrder(Const.BlockSize, PGraphFactory.nodeRecordFactory) ;
        BTreeParams params = new BTreeParams(order, PGraphFactory.nodeRecordFactory) ;
        BTree nodeToId = new BTree(params, BlockMgrFactory.createFile(loc.getPath("node2id", "idx"), Const.BlockSize)) ;
        
        // Data file.
        ObjectFile objects = FileFactory.createObjectFileDisk(loc.getPath("nodes", "dat"));
        // See how fast everything except writing the bytes for each node is.
        //ObjectFile objects = new ObjectFileSink() ;
        
        // Prefixes
//        String prefixesFN = loc.getPath("prefixes", "ttl") ;
//        File prefixes = new File(prefixesFN);
//        PrefixMapping pmap = null ; 
//        if ( prefixes.exists() )
//            pmap = (PrefixMapping)AssemblerUtils.build(prefixesFN, JA.PrefixMapping) ;
        
        init(nodeToId, objects, NodeCacheSize, NodeCacheSize) ;
    }
    
    // Memory version - testing.
    public NodeTableStd()
    {
        super() ;
        int order = 32 ;
        int blkSize = BTreeParams.calcBlockSize(order, PGraphFactory.nodeRecordFactory) ;
        BTreeParams params = new BTreeParams(order, PGraphFactory.nodeRecordFactory) ;
        BlockMgr blkMgr = BlockMgrFactory.createMem(blkSize) ;
        BTree bTree = new BTree(params, blkMgr) ; 
        ObjectFile objects = new ObjectFileMem() ;
        init(bTree, objects, 100, 100) ;
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