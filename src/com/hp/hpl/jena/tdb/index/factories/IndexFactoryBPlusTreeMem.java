/*
 * (c) Copyright 2008, 2009 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.tdb.index.factories;

import com.hp.hpl.jena.tdb.base.block.BlockMgr;
import com.hp.hpl.jena.tdb.base.block.BlockMgrFactory;
import com.hp.hpl.jena.tdb.base.file.FileSet;
import com.hp.hpl.jena.tdb.base.record.RecordFactory;
import com.hp.hpl.jena.tdb.index.Index;
import com.hp.hpl.jena.tdb.index.IndexFactory;
import com.hp.hpl.jena.tdb.index.IndexRangeFactory;
import com.hp.hpl.jena.tdb.index.RangeIndex;
import com.hp.hpl.jena.tdb.index.bplustree.BPlusTree;
import com.hp.hpl.jena.tdb.index.bplustree.BPlusTreeParams;
import com.hp.hpl.jena.tdb.sys.SystemTDB;

public class IndexFactoryBPlusTreeMem implements IndexFactory, IndexRangeFactory
{
    private final int order ;

    public IndexFactoryBPlusTreeMem()
    { this(SystemTDB.OrderMem) ; }
    
    public IndexFactoryBPlusTreeMem(int order)
    {
        this.order = order ;
    }
    
    //@Override
    public Index createIndex(FileSet fileset, RecordFactory recordFactory)
    {
        return createRangeIndex(fileset, recordFactory) ;
    }
    
    //@Override
    public RangeIndex createRangeIndex(FileSet fileset, RecordFactory recordFactory)
    {
        int blkSize = BPlusTreeParams.calcBlockSize(order, recordFactory) ;
        BPlusTreeParams params = new BPlusTreeParams(order, recordFactory) ;
        BlockMgr blkMgr1 = BlockMgrFactory.createMem(fileset.getBasename(), blkSize) ;
        BlockMgr blkMgr2 = BlockMgrFactory.createMem(fileset.getBasename(), blkSize) ;
        BPlusTree bTree = BPlusTree.attach(params, blkMgr1, blkMgr2) ; 
        return bTree ;
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