/*
 * (c) Copyright 2008, 2009 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.tdb.index.ext;

import static com.hp.hpl.jena.tdb.base.recordfile.RecordBufferPageBase.COUNT;
import static com.hp.hpl.jena.tdb.base.recordfile.RecordBufferPageBase.NO_ID;
import static com.hp.hpl.jena.tdb.index.ext.HashBucket.BITLEN;
import static com.hp.hpl.jena.tdb.index.ext.HashBucket.TRIE;

import java.nio.ByteBuffer;

import com.hp.hpl.jena.tdb.base.block.Block ;
import com.hp.hpl.jena.tdb.base.block.BlockConverter ;
import com.hp.hpl.jena.tdb.base.block.BlockMgr;
import com.hp.hpl.jena.tdb.base.block.BlockType;
import com.hp.hpl.jena.tdb.base.page.PageBlockMgr ;
import com.hp.hpl.jena.tdb.base.record.RecordException;
import com.hp.hpl.jena.tdb.base.record.RecordFactory;


public class HashBucketMgr extends PageBlockMgr<HashBucket>
{
    public HashBucketMgr(RecordFactory factory, BlockMgr blockMgr)
    {
        super(null, blockMgr) ;
        Block2HashBucketMgr conv = new Block2HashBucketMgr(factory, null) ;
        super.setConverter(conv) ;
    }

    public HashBucket create(int hash, int hashBitLen)
    {
        HashBucket bucket = super.create(BlockType.RECORD_BLOCK) ;
        bucket.setTrieValue(hash) ;
        bucket.setTrieLength(hashBitLen) ;
        return bucket ;
    }
    
    @Override
    public HashBucket get(int id)
    {
        HashBucket bucket = super.get(id) ;
        bucket.setPageMgr(this) ;
        // Link and Count are in the block and got done by the converter.
        return bucket ;
    }
    
    private static class Block2HashBucketMgr implements BlockConverter<HashBucket>
    {
        private RecordFactory factory ;
        private HashBucketMgr pageMgr ;

        Block2HashBucketMgr(RecordFactory factory, HashBucketMgr pageMgr)
        {
            this.factory = factory ;
            this.pageMgr = pageMgr ;
        }
        
        @Override
        public HashBucket createFromBlock(Block block, BlockType blkType)
        {
            // No need to additionally sync - this is a triggered by write operations so only one writer.
            if ( blkType != BlockType.RECORD_BLOCK )
                throw new RecordException("Not RECORD_BLOCK: "+blkType) ;
            // Initially empty
            HashBucket bucket = new HashBucket(NO_ID, -1, -1, block, factory, pageMgr, 0) ;
            return bucket ;
        }

        @Override
        public HashBucket fromBlock(Block block)
        {
            synchronized (block)
            {
                // Be safe - one reader only.
                // But it is likely that the caller needs to also
                // perform internal updates so syncrhoized on
                // the bytebuffer here is not enough.
                ByteBuffer byteBuffer = block.getByteBuffer() ;
                int count = byteBuffer.getInt(COUNT) ;
                int hash = byteBuffer.getInt(TRIE) ;
                int hashLen = byteBuffer.getInt(BITLEN) ;
                HashBucket bucket = new HashBucket(NO_ID, hash, hashLen, block, factory, pageMgr, count) ;
                return bucket ;
            }
        }

        @Override
        public Block toBlock(HashBucket bucket)
        {
            // No need to additionally sync - this is a triggered by write operations so only one writer.
            int count = bucket.getRecordBuffer().size() ;
            bucket.setCount(count) ;
            ByteBuffer bb = bucket.getBackingBlock().getByteBuffer() ;
            bb.putInt(COUNT, bucket.getCount()) ;
            bb.putInt(TRIE,  bucket.getTrieValue()) ;
            bb.putInt(BITLEN,  bucket.getTrieBitLen()) ;
            return bucket.getBackingBlock() ;
        }
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