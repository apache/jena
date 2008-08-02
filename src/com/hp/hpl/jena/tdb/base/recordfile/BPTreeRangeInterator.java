/*
 * (c) Copyright 2008 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.tdb.base.recordfile;

import iterator.Iter;

import java.util.Iterator;
import java.util.NoSuchElementException;


import com.hp.hpl.jena.tdb.base.record.Record;
import static com.hp.hpl.jena.tdb.lib.Lib.decodeIndex; 


final public
class BPTreeRangeInterator implements Iterator<Record>
{
    
    private RecordBufferPage currentPage ;
    private RecordBufferPageMgr pageMgr ;
    private int currentIdx ;
    private Record slot = null ;
    private final Record maxRec ;
    private final Record minRec ;
    
    /** Iterate over a range of fromRec (inclusive) to toRec (exclusive) */
    
    public static Iterator<Record> iterator(RecordBufferPage page, Record fromRec, Record toRec)
    {
        if ( toRec != null && fromRec != null && Record.keyLE(toRec, fromRec) )
            return Iter.nullIter() ;

        return new BPTreeRangeInterator(page, fromRec, toRec) ;
    }
    
    
    private BPTreeRangeInterator(RecordBufferPage page, Record fromRec, Record toRec)
    {
        currentPage = page ;
        currentIdx = 0 ;
        this.pageMgr = page.getPageMgr() ;
        this.minRec = fromRec ;
        this.maxRec = toRec ;
        
        if ( fromRec != null )
        {
            currentIdx = page.getRecordBuffer().find(fromRec) ;
            if ( currentIdx < 0 )
                currentIdx = decodeIndex(currentIdx) ;
        }
    }
    
    public static Iterator<Record> iterator(RecordBufferPage page)
    {
        return new BPTreeRangeInterator(page, null, null) ;
    }


    @Override
    public boolean hasNext()
    {
        if ( slot != null )
            return true ;
        if ( currentPage == null )
            return false ;
        // Set slot.
        while ( currentIdx >= currentPage.getCount() )
        {
            // Move to next.
            int link = currentPage.getLink() ;
            if ( link < 0 )
            {
                finish() ;
                return false ;
            }
            
            currentPage = pageMgr.get(link) ;
            currentIdx = 0 ;
        }
            
        slot = currentPage.getRecordBuffer().get(currentIdx) ;
        currentIdx++ ;
        if ( maxRec != null && Record.keyGE(slot, maxRec) )
            finish() ;
        return slot != null ;
    }


    private void finish()
    {
        currentPage = null ;
        currentIdx = -99 ;
        slot = null ;
    }


    @Override
    public Record next()
    {
        if ( ! hasNext() )
            throw new NoSuchElementException() ;
        
        Record x = slot ;
        slot = null ;
        return x ;
    }

    @Override
    public void remove()
    { throw new UnsupportedOperationException("remove") ; }

}

/*
 * (c) Copyright 2008 Hewlett-Packard Development Company, LP
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