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

package org.apache.jena.tdb1.base.recordbuffer;

import static org.apache.jena.tdb1.base.recordbuffer.RecordBufferPage.*;

import java.nio.ByteBuffer;

import org.apache.jena.tdb1.base.block.Block;
import org.apache.jena.tdb1.base.block.BlockConverter;
import org.apache.jena.tdb1.base.block.BlockMgr;
import org.apache.jena.tdb1.base.block.BlockType;
import org.apache.jena.tdb1.base.page.PageBlockMgr;
import org.apache.jena.tdb1.base.record.RecordException;
import org.apache.jena.tdb1.base.record.RecordFactory;

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
    
    public RecordBufferPage getReadIterator(int id)
    { 
        Block block = blockMgr.getReadIterator(id) ;
        RecordBufferPage page = pageFactory.fromBlock(block) ;
        return page ;
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
            RecordBufferPage rb = RecordBufferPage.createBlank(block, factory) ;
            return rb ;
        }

        @Override
        public RecordBufferPage fromBlock(Block block)
        {
            synchronized (block)    // Needed? Right place?
            {
                RecordBufferPage rb = RecordBufferPage.format(block, factory) ;
                return rb ;
            }
        }

        @Override
        public Block toBlock(RecordBufferPage rbp)
        {
            int count = rbp.getRecordBuffer().size() ;
            ByteBuffer bb = rbp.getBackingBlock().getByteBuffer() ;
            bb.putInt(COUNT, rbp.getCount()) ;
            bb.putInt(LINK, rbp.getLink()) ;
            return rbp.getBackingBlock() ;
        }
    }
}
