/*
 * (c) Copyright 2008, 2009 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.tdb.index.bplustree;

import com.hp.hpl.jena.tdb.base.block.Block ;
import com.hp.hpl.jena.tdb.base.block.BlockConverter ;
import com.hp.hpl.jena.tdb.base.block.BlockType ;
import com.hp.hpl.jena.tdb.base.record.RecordFactory ;
import com.hp.hpl.jena.tdb.base.recordbuffer.RecordBufferPage ;
import com.hp.hpl.jena.tdb.base.recordbuffer.RecordBufferPageMgr ;
import com.hp.hpl.jena.tdb.base.recordbuffer.RecordBufferPageMgr.Block2RecordBufferPage ;

/** Bridge for making, getting and putting BPTreeRecords over a RecordBufferPageMgr */
final public class BPTreeRecordsMgr extends BPTreePageMgr<BPTreeRecords>
{
    // Only "public" for external very low level tools in development to access this class.
    // Assume package access.

    private RecordBufferPageMgr rBuffPageMgr ;
    
    BPTreeRecordsMgr(BPlusTree bpTree, RecordBufferPageMgr rBuffPageMgr)
    {
        super(bpTree, null, rBuffPageMgr.getBlockMgr()) ;
        this.rBuffPageMgr = rBuffPageMgr ;
        super.setConverter(new Block2BPTreeRecords(bpTree, bpTree.getRecordFactory())) ;
    }
    
    /** Converter BPTreeRecords -- make a RecordBufferPage and wraps it.*/ 
    static class Block2BPTreeRecords implements BlockConverter<BPTreeRecords>
    {
        private Block2RecordBufferPage recordBufferConverter ;
        private BPlusTree bpTree ;

        Block2BPTreeRecords(BPlusTree bpTree, RecordFactory recordFactory)
        { 
            this.bpTree = bpTree ; 
            this.recordBufferConverter = new RecordBufferPageMgr.Block2RecordBufferPage(recordFactory) ;
        }
        
        @Override
        public BPTreeRecords fromBlock(Block block)
        {
            RecordBufferPage rbp = recordBufferConverter.fromBlock(block) ;
            return new BPTreeRecords(bpTree, rbp) ;
        }

        @Override
        public Block toBlock(BPTreeRecords t)
        {
            return recordBufferConverter.toBlock(t.getRecordBufferPage()) ;
        }

        @Override
        public BPTreeRecords createFromBlock(Block block, BlockType bType)
        {
            RecordBufferPage rbp = recordBufferConverter.createFromBlock(block, bType) ;
            return new BPTreeRecords(bpTree, rbp) ;
        }
    }
    
    public BPTreeRecords create()
    {
        return super.create(BlockType.RECORD_BLOCK) ;
//        
//        RecordBufferPage rbp = rBuffPageMgr.create() ;
//        BPTreeRecords bRec = new BPTreeRecords(bpTree, rbp) ;
//        return bRec ;
    }
    
    public RecordBufferPageMgr getRecordBufferPageMgr() { return rBuffPageMgr ; }

    @Override
    public void startRead()         { rBuffPageMgr.startRead() ; }
    @Override
    public void finishRead()        { rBuffPageMgr.finishRead() ; }

    @Override
    public void startUpdate()       { rBuffPageMgr.startUpdate() ; }
    @Override
    public void finishUpdate()      { rBuffPageMgr.finishUpdate() ; }
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