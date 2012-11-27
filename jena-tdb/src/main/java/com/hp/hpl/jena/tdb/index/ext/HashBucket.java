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

import static org.apache.jena.atlas.lib.Alg.decodeIndex ;

import java.nio.ByteBuffer ;

import org.apache.jena.atlas.lib.NotImplemented ;

import com.hp.hpl.jena.tdb.base.StorageException ;
import com.hp.hpl.jena.tdb.base.block.Block ;
import com.hp.hpl.jena.tdb.base.record.Record ;
import com.hp.hpl.jena.tdb.base.record.RecordFactory ;
import com.hp.hpl.jena.tdb.base.recordbuffer.RecordBufferPageBase ;

/** A HashBucket is a record buffer, with space store it's hash value and the bit length */  

public final class HashBucket extends RecordBufferPageBase
{
    // Offsets in the block.
    // Count is 0.
    final public static int TRIE        = COUNT+4 ;
    final public static int BITLEN      = TRIE+4 ;
    
    final public static int FIELD_LENGTH      = BITLEN ;      // Length of the space needed here (not count)
    
    // Trie/Hash of this bucket.
    private int trie ;
    // How many bits are used for storing in this bucket.
    private int bucketBitLen ;

    public static HashBucket format(Block block, RecordFactory factory)
    {
        ByteBuffer byteBuffer = block.getByteBuffer() ;
        int count = byteBuffer.getInt(COUNT) ;
        int hash = byteBuffer.getInt(TRIE) ;
        int hashLen = byteBuffer.getInt(BITLEN) ;
        HashBucket bucket = new HashBucket(NO_ID, hash, hashLen, block, factory, count) ;
        return bucket ;
    }
    
    public static HashBucket createBlank(Block block, RecordFactory factory)
    {
        ByteBuffer byteBuffer = block.getByteBuffer() ;
        int count = 0 ; 
        int hash = -1 ;
        int hashLen = -1 ;
        HashBucket bucket = new HashBucket(NO_ID, hash, hashLen, block, factory, count) ;
        return bucket ;
    }


    
    /** Create a bucket */
    public HashBucket(int id, int hashValue, int bucketBitLen,
                      Block block,
                      RecordFactory factory, 
                      int count)
    {
        super(block, FIELD_LENGTH, factory, count) ;
        this.bucketBitLen = bucketBitLen ;
        this.trie = hashValue ;
    }

    // Find the index of the key, return insertion a point if not found as -(i+1)
    public final int findIndex(Record key)
    { 
        int i = getRecordBuffer().find(key) ;
        return i ;
    }
    
    public final Record find(Record key)
    { 
        int i = getRecordBuffer().find(key) ;
        if ( i < 0 )
            return null ;
        return getRecordBuffer().get(i) ;
    }
    
    // Return true is added a new value 
    public final boolean put(Record record)
    {
        int i = findIndex(record) ;
        if ( i < 0 )
            i = decodeIndex(i) ;
        else
        {
            Record recordOrig = getRecordBuffer().get(i) ;
            if ( record.equals(recordOrig) )
                return false ;
            // Same key, different values. Replace.
            getRecordBuffer().set(i, record) ;
            return true ;
        }
        
        if ( getRecordBuffer().isFull() )
            throw new StorageException("Bucket overflow") ; 
        
        getRecordBuffer().add(i, record) ;
        return true ;
    }          
    
    public void set(int x, Record record)
    {
        getRecordBuffer().set(x, record) ;
    }

    public final boolean removeByKey(Record key)
    {
        int i = findIndex(key) ;
        if ( i < 0 )
            return false ;
        
        getRecordBuffer().remove(i) ;
        return true ;         
    }
    
    @Override
    public void _reset(Block block)
    { throw new NotImplemented("reset") ; }

    public final boolean isFull()
    {
        return getRecordBuffer().isFull() ;
    }
    
    public final boolean isEmpty()
    {
        return getRecordBuffer().isEmpty() ;
    }
    
    // Return the item in slot idx
    public final Record get(int idx)
    { 
        if ( idx >= getRecordBuffer().size() )
            return null ;
        return getRecordBuffer().get(idx) ;
    }
    
    public final int getTrieValue()         { return trie ; }
    
    final void setTrieValue(int newHash)    {  trie = newHash; }

    public final int getTrieBitLen()        { return bucketBitLen ; }
    
    public void setTrieLength(int trieBitLen) { bucketBitLen = trieBitLen ; }
//
//    public void setPageMgr(HashBucketMgr pageMgr)
//    {
//        this.pageMgr = pageMgr;
//    }
//
//    public HashBucketMgr getPageMgr()
//    {
//        return pageMgr;
//    }

    final void incTrieBitLen()              { bucketBitLen++ ; }

    @Override
    public String toString()
    {
        return String.format("HashBucket [id=%d, trie=0x%04X, bitlen=%d]: %s",getId(), getTrieValue(), getTrieBitLen(), getRecordBuffer().toString());
    }

}
