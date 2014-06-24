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

/* This file contains code under the Apache 2 license - see hashFNV */

package com.hp.hpl.jena.tdb.index.ext;

import static java.lang.String.format;

import java.nio.IntBuffer;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;


import org.apache.jena.atlas.io.IndentedLineBuffer ;
import org.apache.jena.atlas.io.IndentedWriter ;
import org.apache.jena.atlas.lib.BitsLong ;
import org.apache.jena.atlas.lib.Bytes ;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hp.hpl.jena.sparql.util.Utils ;
import com.hp.hpl.jena.tdb.base.StorageException;
import com.hp.hpl.jena.tdb.base.block.BlockMgr;
import com.hp.hpl.jena.tdb.base.block.BlockMgrFactory;
import com.hp.hpl.jena.tdb.base.buffer.RecordBuffer;
import com.hp.hpl.jena.tdb.base.file.PlainFile;
import com.hp.hpl.jena.tdb.base.file.PlainFileMem;
import com.hp.hpl.jena.tdb.base.record.Record;
import com.hp.hpl.jena.tdb.base.record.RecordFactory;
import com.hp.hpl.jena.tdb.index.Index;
import com.hp.hpl.jena.tdb.sys.SystemTDB;

/** Extensible hashing
 * http://en.wikipedia.org/wiki/Extendible_hashing
 */

public final class ExtHash implements Index
{
    /* Hashing.
     * Extendible hashing is based on taking more of the bits of the hash
     * value to address an expanding dictionary.  This is a bit-trie, stored
     * as an array.  One bucket can be used for several hash slots.
     * 
     * We need that the bits are used in decreasing signifance because we
     * keep items in buckets in full-hash sorted order.
     * 
     * Side effect: the whole structure is sorted by full hash, using
     * dictionary and buckets.   
     * 
     * But.
     * Java .hashCode() does not make suitable hash directly because either
     * they are Object.hashCode (not too bad but it tends not to use high bits) or
     * something like Integer.hashCode is the integer value itself.  The
     * latter is very bad as the hash is not using the high bits (most
     * integers are small - especially sequentially allocated numbers).  
     * 
     * Solution: use the hashCode, 31 bits (arrays indexes are signed) 
     * but bit reversed so low bits of the original value are the most
     * significant (when shorter than the currect hash, it's the low bits
     * that are used).
     * 
     *  
     * 
     * Example: using hex chars, not bits.
     * 
     * Record: key: 0xABCD
     *   Length = 1 ==> trie is D
     * if the length changes from 1 to 2, 
     *   Length = 2 ==> trie is DC, that is, the D is most significant.
     * and all buckets Dx point to what was in slot for D.
     * 
     * All hash handling is encapulated in the internal routines.
     */

    static private Logger log = LoggerFactory.getLogger(ExtHash.class) ;

    // Production: make these final and false.
    public static boolean Debugging = false ;       
    public static boolean Checking = false ;        // Perform internal checking
    public static boolean Logging = false ;         // Allow any logging code on critical paths
    
    IntBuffer dictionary ;      // mask(hash) -> Bucket id 
    // Current length of trie bit used.  Invariant: dictionary.length = 1<<bitLen 
    private int bitLen = 0 ;
    
    private final HashBucketMgr hashBucketMgr ;
    private final RecordFactory recordFactory ;
    private final PlainFile dictionaryFile ;
    
    /** Testing version - in-memory but inefficient as it uses a copy-in/copy-out block manager as a RAM disk*/
    static public ExtHash createMem(RecordFactory factory, int bucketSizeBytes )
    {
        BlockMgr mgr = BlockMgrFactory.createMem("ExtHash", bucketSizeBytes) ;
        ExtHash eHash = new ExtHash(new PlainFileMem(), factory, mgr) ;
        return eHash ;
    }
    
    public ExtHash(PlainFile dictionaryBackingFile,  
                   RecordFactory recordFactory, BlockMgr blockMgrHashBuckets)
    {
        this.dictionaryFile = dictionaryBackingFile ;
        // Start bigger?
        int dictionarySize = 1 ;
        dictionary = dictionaryFile.ensure(SystemTDB.SizeOfInt).asIntBuffer() ;
        this.recordFactory = recordFactory ; 
        
        hashBucketMgr = new HashBucketMgr(recordFactory, blockMgrHashBuckets) ;
        hashBucketMgr.valid(0) ;
        
        // Did it exist?
        if ( hashBucketMgr.valid(0) )
        {
            
        }
        else
        {
//            int id = hashBucketMgr.allocateId() ;
//            if ( id != 0 )
//                throw new StorageException("ExtHash: First bucket is not id zero") ;
            HashBucket hb = hashBucketMgr.create(0, 0) ;
            dictionary.put(0, hb.getId()) ;    
            bitLen = 0 ;
            hashBucketMgr.put(hb) ;
        }
    }
    
    // =====================
    // Hashing routines for converting to a bit-trie (i.e. lowest bit
    // is most significant in the trie).
    // It's important that bits "appear" as the length changes in decreasing
    // significance order - we reveal bits by lengthing the key up the integer.

    // Use 31 bits (not the top hashCode bit) because array indexes are signed 32bit.

    // See java.util.HashMap#hash for discussion of supplemental hashing. 

    interface HashRecordKey { public int hashCode(byte[] key) ; }
    private HashRecordKey hashFunction = hashFNV ; //hash4bytes ;
    
    // Hash function that is the first 4 bytes of the key (key must be at least 4 bytes long). 
    static HashRecordKey hash4bytes = new HashRecordKey(){
        @Override
        public final int hashCode(byte[] key)
        { return Bytes.getInt(key) ; }
    } ;
    
    /** From Project Voldemort / Apache License / Thanks! 
     *  who in turn got it from 
     *  
     *  Taken from http://www.isthe.com/chongo/tech/comp/fnv
     * 
     * hash = basis for each octet_of_data to be hashed hash 
     *      = hash * FNV_prime hash
     *      = hash xor octet_of_data return hash
     * 
     */
    
    static HashRecordKey hashFNV = new HashRecordKey(){
        private static final long FNV_BASIS = 0x811c9dc5;
        private static final long FNV_PRIME = (1 << 24) + 0x193;
        @Override
        public final int hashCode(byte[] key)
        {
            long hash = FNV_BASIS;
            for ( byte aKey : key )
            {
                hash ^= 0xFF & aKey;
                hash *= FNV_PRIME;
            }
            return (int) hash;
        }
    } ;
    

    /** Turn a key into a bit trie hash value */ 
    private int trieKey(Record k)             
    { 
        // ***** Record key to hash: high bits most significant
        // Does not have to be a perfect hash.
        int x = hashFunction.hashCode(k.getKey()) ;
        return Integer.reverse(x)>>>1 ;
    }
        
    /** Calculate the array index for a key given the dictionary bit length */ 
    private int trieKey(Record key, int bitLen)     { return trieKey(trieKey(key), bitLen) ; }
    
    /** Convert from full hash to array index for a dictionary bit length */ 
    private int trieKey(int fullTrie, int bitLen)   { return fullTrie >>> (31-bitLen) ; }

    /** Calculate the bucket id for a key given the dictionary bit length */ 
    private int bucketId(Record key, int bitLen)
    { 
        int x = trieKey(trieKey(key), bitLen) ;
        int id = dictionary.get(x) ;
        return id ;
    }
    
    /** Size of the file, in bytes */
    private static long filesize(int dictionarySize) { return 4L*dictionarySize ; }  

    // =====================
    
    private void resizeDictionary()
    {
        int oldSize = 1<<bitLen ;
        int newBitLen = bitLen+1 ;
        int newSize = 1<<newBitLen ;
        if ( logging() )
        {
            log(">>>>Resize") ;
            log("resize: %d ==> %d", oldSize, newSize) ;
        }
        
        IntBuffer newDictionary = dictionaryFile.ensure(newSize*SystemTDB.SizeOfInt).asIntBuffer() ;
        if ( dictionary != null )
        {
            // Fill new dictionary
            // NB Fills from high to low so that it works "in place"
            for ( int i = oldSize-1 ; i>=0 ; i-- )
            {
                int b = newDictionary.get(i) ; 
                //if ( logging() ) log("Resize: put: (%d, %d)", 2*i, b) ;
                newDictionary.put(2*i, b) ; 
                newDictionary.put(2*i+1, b) ; 
            }
        }
        
        dictionary = newDictionary ;
        bitLen = newBitLen ;

        if ( logging() )
        {
            if ( false ) dump() ;
            if ( false ) log(this) ; 
            log("<<<<Resize") ;
        }
        internalCheck() ;
    }
    
    // =====================

    // Used by the iterator
    final int getBucketId(int dictionaryIdx)
    {
        return  dictionary.get(dictionaryIdx) ;
    }
    
    
    final HashBucket getBucket(int blockId)
    {
        return hashBucketMgr.get(blockId) ;
    }
    
    public final int dictionarySize()
    {
        return dictionary.capacity() ;
    }
    
    // =====================
    
    @Override
    public boolean contains(Record key)
    {
        return find(key) != null ;
    }
    
    @Override
    public Record find(Record key)
    {
        if ( logging() ) log(">> get(%s)", key) ;
        int blockId = bucketId(key, bitLen) ;
        HashBucket bucket = hashBucketMgr.get(blockId) ;
        Record value = bucket.find(key) ;
        if ( logging() ) log("<< get(%s) -> %s", key.getKey(), value) ;
        return value ;
    }

    
    @Override
    public boolean add(Record record)
    {
        if ( logging() ) log(">> add(%s)", record) ;
        int h = trieKey(record) ;
        boolean b = put(record, h) ;
        if ( logging() )
        {
            log("<< add(%s)", record) ;
            //dump() ;
        }
        internalCheck() ;
        return b ;
    }
        
    @Override
    public boolean delete(Record record)
    {
        if ( logging() ) log(">> remove(%s)", record) ;
        int blockId = bucketId(record, bitLen) ;
        HashBucket bucket = hashBucketMgr.get(blockId) ;

        boolean b = bucket.removeByKey(record) ;
        hashBucketMgr.put(bucket) ;
        internalCheck() ;
        if ( logging() ) log("<< remove(%s)", record) ;
        return b ;
    }

    @Override
    public RecordFactory getRecordFactory()
    { return recordFactory ; }

    @Override
    public Iterator<Record> iterator()
    {
        return new ExtHashIterator(this) ; 
    }

    @Override
    public boolean isEmpty()
    { 
       if ( dictionary.limit() == 1 )
       {
           HashBucket b = hashBucketMgr.get(1) ;
           return b.isEmpty() ;
       }
       // No idea.
       return false ;
    }
    
    @Override
    public void clear()
    { throw new UnsupportedOperationException("RangeIndex("+Utils.classShortName(this.getClass())+").clear") ; }

    @Override
    public long size()
    { return count() ; }

    /** Explicitly count the items in the hash table */
    public long count()
    {
        Set<Integer> seen = new HashSet<>() ;
        long count = 0 ;
        for ( int i = 0 ; i < dictionary.capacity() ; i++ )
        {
            int id = dictionary.get(i) ;
            if ( seen.contains(id) )
                continue ;
            seen.add(id) ;
            HashBucket bucket = hashBucketMgr.get(id) ;
            count += bucket.getCount() ;
        }
        return count ;
    }

    @Override
    public void sync()
    { 
        hashBucketMgr.getBlockMgr().sync() ;
        dictionaryFile.sync() ;
    }

    @Override
    public void close()
    {
        hashBucketMgr.getBlockMgr().close() ;
        dictionaryFile.close() ;
    }

    // =====================
    // Insert
    
    // Reentrant part of "put"
    private boolean put(Record record, int hash)  
    {
        if ( logging() ) log("put(%s,0x%08X)", record, hash) ;
        int dictIdx = trieKey(hash, bitLen) ;       // Dictionary index
        int blockId = dictionary.get(dictIdx) ;
        
        HashBucket bucket = hashBucketMgr.get(blockId) ;
        
        if ( ! bucket.isFull() )
        {
            if ( Debugging ) log("Insert [(0x%04X) %s]: %d", hash, record, bucket.getId()) ; 
            boolean b = bucket.put(record) ;
            hashBucketMgr.put(bucket) ;
            return b ;
        }

        //Is this and +1 the same?  Is the block splitable? 
        
        // Bucket full.
        if (  bitLen == bucket.getTrieBitLen() )
        {
//            // Log it anyway
//            if ( ! logging() ) log("put(%s,0x%08X)", record, hash) ;

            boolean oldLogging = Logging ;
            boolean oldDebugging = Debugging ;
            try {
//                Logging = true ;
//                Debugging = true ; 
                
                if ( Debugging ) 
                { 
                    log("Bucket full: %d", bucket.getId()) ; 
                    log("Bucket can't be split - dictionary resize needed") ;
                    //log(bucket) ;
                    this.dump() ;
                }
            
                // Bucket not splitable..
                // TODO Overflow buckets.
                
                // Expand the dictionary.
                int x = dictionarySize() ;
                resizeDictionary() ;
                if ( Debugging ) log("Resize: %d -> %d", x, dictionarySize()) ; 
                // Try again
                return put(record, hash) ;
            } finally { Logging = oldLogging ; Debugging = oldDebugging ;} 
        }

        if ( Debugging ) log("Split bucket: %d", bucket.getId()) ;
        
        // bitLen >  bucket.getHashBitLen() : bucket can be split
        splitAndReorganise(bucket, dictIdx, blockId, hash) ;
        
        // Reorg done - try again.
        return put(record, hash) ;
    }


    // Bucket bitlength is less than that of the dictionary. 
    private void splitAndReorganise(HashBucket bucket, int dictionaryIdx, int bucketId, int hash)
    {
        if ( logging() )
        {
            log("splitAndReorganise: idx=%d, id=%d, bitLen=%d, bucket.hashLength=%d",
                dictionaryIdx, bucketId, bitLen, bucket.getTrieBitLen()) ;
            if ( false ) dump() ;
        }

        if ( Checking )
        {
            if ( bucket.getTrieBitLen() >= bitLen )
                error("splitAndReorganise: idx=0x%X : hash=0x%X[0x%X,0x%X] : Hash not shorter : %s",
                      dictionaryIdx, hash, trieKey(hash, bucket.getTrieBitLen()), bucket.getTrieValue(), bucket) ;
            if ( trieKey(hash, bucket.getTrieBitLen()) != bucket.getTrieValue() )
                error("splitAndReorganise: idx=0x%X : hash=0x%X[0x%X,0x%X] : Inconsistency : %s",
                      dictionaryIdx, hash, trieKey(hash, bucket.getTrieBitLen()), bucket.getTrieValue(), bucket) ;
        }

        // Bucket did not have a full length hash so split it. 
        // Find the companion slots.
        // Remember before messing with split.
        int bucketHash = bucket.getTrieValue() ;
        int bucketHashLength = bucket.getTrieBitLen() ;

        // Split the bucket in two.  bucket2 is the upper bucket.
        HashBucket bucket2 = split(bucketId, bucket) ;

        // Determine the slots affected: 
        // All the dictionary entries that in the extension of the bit trie, have a 1
        // in the newly exposed bit.  These will point to bucket2.
        // All the slots for bit 0 will continue to point to the existing (reorganised) bucket.
        
        // The hash is reversed (the low bits the hash value are most significant).
        // So extending a hash is shift up, and OR in a 0 or 1.
        // Zeros in the difference the bucket bit length and the dictionary bitlength. 
        
        // Upper section of bucket hash, extended, then the gap in lengths zero filled. 
        int trieUpperRoot = ((bucketHash<<1)|0x1) << (bitLen-bucketHashLength-1) ;
        
        // Upper bound (exclusive) of values affected between dictionary and current bucket.
        // NB relationship to the second shift on trieUpperRoot
        int trieUpperRange = (1<<(bitLen-bucketHashLength-1)) ;
        
        for ( int j = 0 ; j < trieUpperRange ; j++ )
        {
            // j runs over the values of the unused bits of the trie start for the upper bucket positions.
            int k = trieUpperRoot | j ;
            if ( logging() )
                log("Point to split bucket: 0x%04X", k) ;
            
            if ( Checking )
            {
                if ( (trieUpperRoot&j) != 0 )
                    error("put: idx=%d : trieRoot=0x%X, sub=%d: Broken trie pattern ", dictionaryIdx, trieUpperRoot, j) ;
                
                if ( ! BitsLong.isSet(k, (bitLen-(bucketHashLength+1)) ) )
                    error("put: Broken trie pattern (0x%X,%d)", trieUpperRoot, j) ;
                
                // We should looking at the original bucket
                int id = dictionary.get(k) ;
                HashBucket hb = hashBucketMgr.get(id) ;
                
                if ( hb.getId() != bucket.getId() )
                    error("put: Wrong bucket at trie 0x%X %d: (%d,%d)", trieUpperRoot, j, hb.getId(), bucket.getId()) ;
            }
            
            dictionary.put(k, bucket2.getId()) ;
        }
        if ( logging() )
        {
            log("Reorg complete") ;
            if ( false ) dump() ;
        }
    }
    
    private HashBucket split(int bucketId, HashBucket bucket)
    {
        // idx is the array offset to the lower of the bucket point pair.
        if ( logging() )
        {
            log("split: Bucket %d : size: %d; Bucket bitlength %d", bucketId, bucket.getCount(), bucket.getTrieBitLen()) ;
            log("split: %s", bucket) ;
        }
        
        // Create new bucket, which will be the upper bucket.
        // Low bucket will have the old hash value, 
        // Lengthen the hash; the new will be one more.
        
        bucket.incTrieBitLen() ;
        
        // Bucket hash value is kept in index-order (i.e. high bits are most significant). 
        int hash1 = bucket.getTrieValue() << 1 ;
        int hash2 = (bucket.getTrieValue() << 1) | 0x1 ;
        
        // Reset, now it's longer
        bucket.setTrieValue(hash1) ;
        
        if ( logging() ) 
            log("split: bucket hashes 0x%04X 0x%04X", hash1, hash2) ;

//        // New bucket
        HashBucket bucket2 = hashBucketMgr.create(hash2, bucket.getTrieBitLen()) ;
        
        if ( logging() ) log("New bucket: %s", bucket2) ;
        //bucket2.setTrieValue(hash2) ;
        
        RecordBuffer rBuff1 = bucket.getRecordBuffer() ;
        RecordBuffer rBuff2 = bucket2.getRecordBuffer() ;
        int idx1 = 0 ;  // Destination indexes into the above
        int idx2 = 0 ;
        
        for ( int i = 0 ; i < rBuff1.size() ; i++ )
        {
            Record r = rBuff1.get(i) ; 
            int x = trieKey(r, bucket.getTrieBitLen()) ;  // Incremented bit length
            if ( x == hash1 )
            {
                if ( logging() )
                    log("Allocate index %d to bucket1", i) ;
                // idx1 <= i (we are writing less records back).
                // So this foes not interfer with the loop
                // We're shifting down records that saty in this bucket.  
                if ( idx1 != i )
                    rBuff1.set(idx1, r) ;
                idx1++ ; 
            }
            else if ( x == hash2 )
            {
                if ( logging() )
                    log("Allocate index %d to bucket2", i) ;
                rBuff2.add(r) ;
                idx2 ++ ;
            }
            else
                error("Bad trie for allocation to split buckets") ;
        }
        
        if ( true )
            rBuff1.clear(idx1, bucket.getCount()-idx1)  ;
        rBuff1.setSize(idx1) ;
        // rBuff2 was fresh so still clean.
        
        if ( logging() )
        {
            log("split: Lower bucket: %s", bucket) ;
            log("split: Upper bucket: %s", bucket2) ;
        }
        
        // Check with splitAndReorganise()
        hashBucketMgr.put(bucket) ;
        hashBucketMgr.put(bucket2) ;
        
        return bucket2 ;
    }
    
    // =====================

    @Override
    public String toString()
    {
        IndentedLineBuffer buff = new IndentedLineBuffer() ;
        dump(buff) ;
        return buff.asString() ;
    }        
        
    public void dump()
    {
        dump(IndentedWriter.stdout) ;
        IndentedWriter.stdout.ensureStartOfLine() ;
        IndentedWriter.stdout.flush() ;
    }
    
    private void dump(IndentedWriter out)
    {
        out.printf("Bitlen      = %d\n" , bitLen) ;
        out.printf("Dictionary  = %d\n" , 1<<bitLen ) ;
        out.incIndent(4) ;
        for ( int i = 0 ; i < (1<<bitLen) ; i++ )
        {
            out.ensureStartOfLine() ;
            int id = dictionary.get(i) ;
            HashBucket bucket = hashBucketMgr.get(id) ;
            out.printf("[%d] %02d %s", i, id, bucket) ;
        }
        out.decIndent(4) ;
    }
    
    @Override
    public void check()
    {
        performCheck() ;
    }
        
    private final void internalCheck()
    {
        if ( Checking )
            performCheck() ;
    }
    
    private final void performCheck()
    {
        int len = 1<<bitLen ;
        int d = dictionary.limit() ;
        //int d = (dictionary.limit()/4) ;
        
        if ( len != d )
            error("Dictionary size = %d : expected = %d", d, len) ;

        Set<Integer> seen = new HashSet<>() ;
        for ( int i = 0 ; i < d ; i++ )
        {
            int id = dictionary.get(i) ;
            if ( seen.contains(id) )
                continue ;
            
            seen.add(id) ;
            HashBucket bucket = hashBucketMgr.get(id) ;
            performCheck(i, bucket) ;
            
        }
    }
    
    private void performCheck(int idx, HashBucket bucket)
    {
        if ( bucket.getTrieBitLen() > bitLen )
            error("[%d] Bucket %d has bit length longer than the dictionary's (%d, %d)", idx, bucket.getId(), bucket.getTrieBitLen(), bitLen) ;

        // Check the bucket hash against the slot it's in.
        // Convert directory index to bucket hash
        int tmp = (idx >>> (bitLen-bucket.getTrieBitLen())) ;
        if ( tmp != bucket.getTrieValue())
            error("[%d] Bucket %d : hash prefix 0x%X, expected 0x%X : %s", idx, bucket.getId(), bucket.getTrieValue(), tmp, bucket) ;
        
        // Check the contents.
        Record prevKey = Record.NO_REC ;
        for ( int i = 0 ; i < bucket.getCount() ; i++ )
        {
            Record rec = bucket.get(i) ;
            if ( prevKey != Record.NO_REC && Record.keyLT(rec,prevKey) )
                error("[%d] Bucket %d: Not sorted (slot %d) : %s", idx, bucket.getId(), i, bucket) ;
            prevKey = rec ;
            int x = trieKey(rec, bucket.getTrieBitLen()) ;
            // Check the key is bucket-compatible.
            if ( x != bucket.getTrieValue() )
                error("[%d] Bucket %d: Key (0x%04X) does not match the hash (0x%04X) : %s",
                             idx, bucket.getId(), x, bucket.getTrieValue(), bucket) ;
        }
        
        if ( SystemTDB.NullOut )
        {
            for ( int i = bucket.getCount() ; i < bucket.getMaxSize() ; i++ )
            {
                if ( ! bucket.getRecordBuffer().isClear(i)  )
                    error("[%d] Bucket %d : overspill at [%d]: %s", idx, bucket.getId(), i, bucket) ;
            }
        }
    }

    private void error(String msg, Object... args)
    {
        msg = format(msg, args) ;
        log.error(msg) ;
        throw new StorageException(msg) ;
    }
    
    private final boolean logging() { return Logging /* && log.isDebugEnabled()*/ ; }
    private final void log(String format, Object... args)
    {
        //if ( ! logging() ) return ;
        log.debug(format(format, args)) ;
    }
    
    private final void log(Object obj)
    {
        //if ( ! logging() ) return ;
        log.debug(obj.toString()) ;
    }
}
