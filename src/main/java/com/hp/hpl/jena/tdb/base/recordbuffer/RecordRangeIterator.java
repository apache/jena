/*
 * (c) Copyright 2008, 2009 Hewlett-Packard Development Company, LP
 * (c) Copyright 2011 Epimorphics Ltd.
 * All rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.tdb.base.recordbuffer;

import static org.openjena.atlas.lib.Alg.decodeIndex ;

import java.util.Iterator;
import java.util.NoSuchElementException;

import org.openjena.atlas.lib.Closeable ;

import com.hp.hpl.jena.tdb.base.StorageException ;
import com.hp.hpl.jena.tdb.base.block.BlockException ;
import com.hp.hpl.jena.tdb.base.record.Record;

final public
class RecordRangeIterator implements Iterator<Record>, Closeable
{
    /** Iterate over a range of fromRec (inclusive) to toRec (exclusive) */
    public static Iterator<Record> iterator(int pageId, Record fromRec, Record toRec, RecordBufferPageMgr pageMgr)
    {
        if ( ! pageMgr.valid(pageId) )
            throw new BlockException("No such block") ;
        return new RecordRangeIterator(pageId, fromRec, toRec, pageMgr) ;
    }

    
    
    // ITER release the page and re-get it as a   
//    /** Iterate over a range of fromRec (inclusive) to toRec (exclusive) */
//    public static Iterator<Record> iterator(RecordBufferPage page, Record fromRec, Record toRec, RecordBufferPageMgr pageMgr)
//    {
//        return new RecordRangeIterator(page, fromRec, toRec, pageMgr) ;
//    }
//    
//    /** Iterate over all records from this page onwards */
//    public static Iterator<Record> iterator(RecordBufferPage page, RecordBufferPageMgr pageMgr)
//    {
//        return new RecordRangeIterator(page, null, null, pageMgr) ;
//    }
    
    private RecordBufferPage currentPage ;      // Set null when finished.
    private RecordBufferPageMgr pageMgr ;
    private int currentIdx ;
    private Record slot = null ;
    private final Record maxRec ;
    private final Record minRec ;
    
    private long countRecords = 0 ;
    private long countBlocks = 0 ;

    private RecordRangeIterator(int id, Record fromRec, Record toRec, RecordBufferPageMgr pageMgr)
    {
        currentIdx = 0 ;
        this.pageMgr = pageMgr;
        this.minRec = fromRec ;
        this.maxRec = toRec ;
        
        if ( toRec != null && fromRec != null && Record.keyLE(toRec, fromRec) )
        {
            currentPage = null ;
            return ;
        }

        pageMgr.getBlockMgr().beginIterator(this) ;
        currentPage = pageMgr.getReadIterator(id) ;
        if ( currentPage.getCount() == 0 )
        {
            // Empty page.
            close() ;
            return ;
        }
            
        if ( fromRec != null )
        {
            currentIdx = currentPage.getRecordBuffer().find(fromRec) ;
            if ( currentIdx < 0 )
                currentIdx = decodeIndex(currentIdx) ;
        }
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
                close() ;
                return false ;
            }
            
            if ( currentPage != null )
                pageMgr.release(currentPage) ;
            
            RecordBufferPage nextPage = pageMgr.getReadIterator(link) ;
            // Check currentPage -> nextPage is strictly increasing keys. 
            Record r1 = currentPage.getRecordBuffer().getHigh() ;
            Record r2 = nextPage.getRecordBuffer().getLow() ;
            if ( Record.keyGE(r1, r2) )
                throw new StorageException("RecordRangeIterator: records not strictly increasing: "+r1+" // "+r2) ;
            currentPage = nextPage ;
            countBlocks++ ;
            currentIdx = 0 ;
        }
            
        slot = currentPage.getRecordBuffer().get(currentIdx) ;
        currentIdx++ ;
        if ( maxRec != null && Record.keyGE(slot, maxRec) )
        {
            close() ;
            return false ;
        }
        
        if ( slot == null )
        {
            close() ;
            return false ;
        }
        countRecords++ ;
        return true ;
    }

    @Override
    public void close()
    {
        if (currentPage != null )
            pageMgr.release(currentPage) ;
        currentPage = null ;
        currentIdx = -99 ;
        slot = null ;
        pageMgr.getBlockMgr().endIterator(this) ;
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

    final public long getCountRecords()     { return countRecords ; }

    final public long getCountBlocks()      { return countBlocks ; }
}

/*
 * (c) Copyright 2008, 2009 Hewlett-Packard Development Company, LP
 * (c) Copyright 2011 Epimorphics Ltd.
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