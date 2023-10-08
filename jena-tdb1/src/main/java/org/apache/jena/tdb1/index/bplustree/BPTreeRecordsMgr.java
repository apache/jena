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

package org.apache.jena.tdb1.index.bplustree;

import org.apache.jena.tdb1.base.block.Block;
import org.apache.jena.tdb1.base.block.BlockConverter;
import org.apache.jena.tdb1.base.block.BlockType;
import org.apache.jena.tdb1.base.record.RecordFactory;
import org.apache.jena.tdb1.base.recordbuffer.RecordBufferPage;
import org.apache.jena.tdb1.base.recordbuffer.RecordBufferPageMgr;
import org.apache.jena.tdb1.base.recordbuffer.RecordBufferPageMgr.Block2RecordBufferPage;

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
