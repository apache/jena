/*
 * (c) Copyright 2008, 2009 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
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
    private Set<Integer> blockIds = new HashSet<Integer>() ; 
    
    public ExtHashIterator(ExtHash extHash)
    {
        this.extHash = extHash ;
        dictionaryIdx = 0 ;
    }

    //@Override
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
    
    //@Override
    public Record next()
    {
        if ( ! hasNext() )
            throw new NoSuchElementException("ExtHashIterator") ;
        return rBuffIterator.next() ;
    }

    //@Override
    public void remove()
    {}
 
  
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