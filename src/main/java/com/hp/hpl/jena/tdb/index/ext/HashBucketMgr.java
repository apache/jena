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

package com.hp.hpl.jena.tdb.index.ext;

import static com.hp.hpl.jena.tdb.base.recordbuffer.RecordBufferPageBase.COUNT ;
import static com.hp.hpl.jena.tdb.index.ext.HashBucket.BITLEN ;
import static com.hp.hpl.jena.tdb.index.ext.HashBucket.TRIE ;

import java.nio.ByteBuffer ;

import com.hp.hpl.jena.tdb.base.block.Block ;
import com.hp.hpl.jena.tdb.base.block.BlockConverter ;
import com.hp.hpl.jena.tdb.base.block.BlockMgr ;
import com.hp.hpl.jena.tdb.base.block.BlockType ;
import com.hp.hpl.jena.tdb.base.page.PageBlockMgr ;
import com.hp.hpl.jena.tdb.base.record.RecordException ;
import com.hp.hpl.jena.tdb.base.record.RecordFactory ;


public class HashBucketMgr extends PageBlockMgr<HashBucket>
{
    public HashBucketMgr(RecordFactory factory, BlockMgr blockMgr)
    {
        super(null, blockMgr) ;
        Block2HashBucketMgr conv = new Block2HashBucketMgr(factory) ;
        super.setConverter(conv) ;
    }

    public HashBucket create(int hash, int hashBitLen)
    {
        HashBucket bucket = super.create(BlockType.RECORD_BLOCK) ;
        bucket.setTrieValue(hash) ;
        bucket.setTrieLength(hashBitLen) ;
        return bucket ;
    }
    
    // [TxTDB:PATCH-UP]
    @Override
    public HashBucket getWrite(int id)
    { 
        HashBucket page = _get(id) ;
        page.getBackingBlock().setModified(true) ;
        return page ;
    }
    // [TxTDB:PATCH-UP]
    @Override
    public HashBucket getRead(int id)        { return _get(id) ; }
    
    // [TxTDB:PATCH-UP]
    //@Override
    public HashBucket get(int id)        { return getWrite(id) ; }
    
   
    public HashBucket _get(int id)
    {
        // [TxTDB:PATCH-UP]
        HashBucket bucket = super.getWrite(id) ;
        // Link and Count are in the block and got done by the converter.
        return bucket ;
    }
    
    private static class Block2HashBucketMgr implements BlockConverter<HashBucket>
    {
        private RecordFactory factory ;

        Block2HashBucketMgr(RecordFactory factory)
        {
            this.factory = factory ;
        }
        
        @Override
        public HashBucket createFromBlock(Block block, BlockType blkType)
        {
            // No need to additionally sync - this is a triggered by write operations so only one writer.
            if ( blkType != BlockType.RECORD_BLOCK )
                throw new RecordException("Not RECORD_BLOCK: "+blkType) ;
            // Initially empty
            HashBucket bucket = HashBucket.createBlank(block, factory) ; // NO_ID, -1, -1, block, factory, 0) ;
            return bucket ;
        }

        @Override
        public HashBucket fromBlock(Block block)
        {
            synchronized (block)
            {
                HashBucket bucket = HashBucket.format(block, factory) ;
//                // Be safe - one reader only.
//                // But it is likely that the caller needs to also
//                // perform internal updates so syncrhoized on
//                // the bytebuffer here is not enough.
//                ByteBuffer byteBuffer = block.getByteBuffer() ;
//                int count = byteBuffer.getInt(COUNT) ;
//                int hash = byteBuffer.getInt(TRIE) ;
//                int hashLen = byteBuffer.getInt(BITLEN) ;
//                HashBucket bucket = new HashBucket(NO_ID, hash, hashLen, block, factory, pageMgr, count) ;
                return bucket ;
            }
        }

        @Override
        public Block toBlock(HashBucket bucket)
        {
            // No need to additionally sync - this is a triggered by write operations so only one writer.
            int count = bucket.getRecordBuffer().size() ;
            ByteBuffer bb = bucket.getBackingBlock().getByteBuffer() ;
            bb.putInt(COUNT, bucket.getCount()) ;
            bb.putInt(TRIE,  bucket.getTrieValue()) ;
            bb.putInt(BITLEN,  bucket.getTrieBitLen()) ;
            return bucket.getBackingBlock() ;
        }
    }
}
