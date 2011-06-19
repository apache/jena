/*
 * (c) Copyright 2008, 2009 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.tdb.index.ext;

import static org.openjena.atlas.lib.Alg.decodeIndex ;
import org.openjena.atlas.lib.NotImplemented ;

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
    private HashBucketMgr pageMgr ;
    
    /** Create a bucket */
    public HashBucket(int id, int hashValue, int bucketBitLen,
                      Block block,
                      RecordFactory factory, HashBucketMgr hashBucketPageMgr, 
                      int count)
    {
        super(block, FIELD_LENGTH, factory, count) ;
        this.pageMgr = hashBucketPageMgr ;
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
    public void reset(Block block)
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

    public void setPageMgr(HashBucketMgr pageMgr)
    {
        this.pageMgr = pageMgr;
    }

    public HashBucketMgr getPageMgr()
    {
        return pageMgr;
    }

    final void incTrieBitLen()              { bucketBitLen++ ; }

    @Override
    public String toString()
    {
        return String.format("HashBucket [id=%d, trie=0x%04X, bitlen=%d]: %s",getId(), getTrieValue(), getTrieBitLen(), getRecordBuffer().toString());
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