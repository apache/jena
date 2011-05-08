/*
 * (c) Copyright 2008, 2009 Hewlett-Packard Development Company, LP
 * (c) Copyright 2011 Epimorphics Ltd.
 * All rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.tdb.base.recordbuffer;

import static com.hp.hpl.jena.tdb.base.recordbuffer.RecordBufferPage.* ;

import java.nio.ByteBuffer;

import com.hp.hpl.jena.tdb.base.block.Block ;
import com.hp.hpl.jena.tdb.base.block.BlockConverter ;
import com.hp.hpl.jena.tdb.base.block.BlockMgr;
import com.hp.hpl.jena.tdb.base.block.BlockType;
import com.hp.hpl.jena.tdb.base.page.PageBlockMgr ;
import com.hp.hpl.jena.tdb.base.record.RecordException;
import com.hp.hpl.jena.tdb.base.record.RecordFactory;

/** Manager for a block that is all records.  
 *  This must be compatible with B+Tree records nodes and with hash buckets. 
 */

public class RecordBufferPageMgr extends PageBlockMgr<RecordBufferPage>
{
    public RecordBufferPageMgr(RecordFactory factory, BlockMgr blockMgr)
    {
        super(null, blockMgr) ;
        Block2RecordBufferPage conv = new Block2RecordBufferPage(factory) ;
        super.setConverter(conv) ;
    }

    public RecordBufferPage create()
    {
        return super.create(BlockType.RECORD_BLOCK) ;
    }
    
    public static class Block2RecordBufferPage implements BlockConverter<RecordBufferPage>
    {
        private RecordFactory factory ;

        public Block2RecordBufferPage(RecordFactory factory)
        {
            this.factory = factory ;
        }
        
        @Override
        public RecordBufferPage createFromBlock(Block block, BlockType blkType)
        {
            if ( blkType != BlockType.RECORD_BLOCK )
                throw new RecordException("Not RECORD_BLOCK: "+blkType) ;
            // Initially empty
            RecordBufferPage rb = new RecordBufferPage(block, NO_ID, factory, 0) ;
            return rb ;
        }

        @Override
        public RecordBufferPage fromBlock(Block block)
        {
            synchronized (block)
            {
                int count = block.getByteBuffer().getInt(COUNT) ;
                int linkId = block.getByteBuffer().getInt(LINK) ;
                RecordBufferPage rb = new RecordBufferPage(block, linkId, factory, count) ;
                return rb ;
            }
        }

        @Override
        public Block toBlock(RecordBufferPage rbp)
        {
            int count = rbp.getRecordBuffer().size() ;
            rbp.setCount(count) ;
            ByteBuffer bb = rbp.getBackingBlock().getByteBuffer() ;
            bb.putInt(COUNT, rbp.getCount()) ;
            bb.putInt(LINK, rbp.getLink()) ;
            return rbp.getBackingBlock() ;
        }
    }
}

/*
 * (c) Copyright 2008, 2009 Hewlett-Packard Development Company, LP
 * (c) Copyright 2011 Epimorphics Ltd.
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