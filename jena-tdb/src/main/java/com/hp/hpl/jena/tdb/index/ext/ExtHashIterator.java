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

import java.util.HashSet;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Set;

import com.hp.hpl.jena.tdb.base.record.Record;

public class ExtHashIterator implements Iterator<Record>
{
    // Need to know if a bucket has been seen before.
    // A directory may point several times to a HashBucket for different trie
    // hashes because that bucket is a bucket for a shorter part of the trie. 
    private ExtHash extHash ;
    private int dictionaryIdx ;
    private Iterator<Record> rBuffIterator ;
    private Set<Integer> blockIds = new HashSet<>() ;
    
    public ExtHashIterator(ExtHash extHash)
    {
        this.extHash = extHash ;
        dictionaryIdx = 0 ;
    }

    @Override
    public boolean hasNext()
    {
        if ( dictionaryIdx < 0 )
            return false ;
        
        while ( rBuffIterator == null || ! rBuffIterator.hasNext() )
        {
            rBuffIterator = null ;
            if ( dictionaryIdx >= extHash.dictionarySize() )
                break ;
            int blockId = extHash.getBucketId(dictionaryIdx) ;
            // Move on, always.
            dictionaryIdx++ ;
            if ( blockIds.contains(blockId) )
                continue ;
            HashBucket b = extHash.getBucket(blockId) ;
            blockIds.add(blockId) ;
            rBuffIterator = b.getRecordBuffer().iterator() ;
        }
        
        if ( rBuffIterator == null  )
        {
            finish() ; 
            return false ;
        }
        return true ;
    }

    private void finish()
    {
        blockIds = null ;
        rBuffIterator = null ;
        extHash = null ;
        dictionaryIdx = -99 ;
    }
    
    @Override
    public Record next()
    {
        if ( ! hasNext() )
            throw new NoSuchElementException("ExtHashIterator") ;
        return rBuffIterator.next() ;
    }

    @Override
    public void remove()
    {}
 
  
}
