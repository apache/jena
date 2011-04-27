/*
 * (c) Copyright 2010 Talis Systems Ltd.
 * (c) Copyright 2010 Epimorphics Ltd.
 * All rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.tdb.index.bplustree;

import java.util.Iterator;
import java.util.NoSuchElementException;

import com.hp.hpl.jena.tdb.base.buffer.RecordBuffer;
import com.hp.hpl.jena.tdb.base.record.Record;
import com.hp.hpl.jena.tdb.base.recordfile.RecordBufferPage;
import com.hp.hpl.jena.tdb.base.recordfile.RecordBufferPageMgr;

/** Iterate over a stream of records, packing them into RecordBufferPage -- the leaf of a B+Tree 
 *  This class does not write the blocks back to the block manager.
 *  This cleass does allocate block ids and blocks.
 *  @see RecordBufferPageLinker
 */

class RecordBufferPagePacker implements Iterator<RecordBufferPage>
{
    Iterator<Record> records = null ;
    RecordBufferPage recordBufferPage = null ;
    RecordBufferPageMgr rbMgr = null ;
    
    RecordBufferPagePacker(Iterator<Record> records, RecordBufferPageMgr rbMgr)
    {
        this.records = records ;
        this.rbMgr = rbMgr ;
    }
    
    //@Override
    public boolean hasNext()
    {
        if ( recordBufferPage == null )
        {
            if ( records == null )
                return false ;
            
            if ( !records.hasNext() )
            {
                records = null ;
                return false ;
            }
            // At least one record to be processed.
            // No pending RecordBufferPage
            // ==> There will be a RecordBufferPage to yield.

//            int id = rbMgr.allocateId() ;
//            //System.out.println("Allocate : "+id) ;
            Block block = rbMgr.create() ;
            
            recordBufferPage = rbMgr.create(id) ;
            
            RecordBuffer rb = recordBufferPage.getRecordBuffer() ;
            while ( !rb.isFull() && records.hasNext() )
            {
                Record r = records.next();
                rb.add(r) ;
            }
            if ( ! records.hasNext() )
                records = null ;
            return true ;
        }
        return true ;
        
    }
    
    //@Override
    public RecordBufferPage next()
    {
        if ( ! hasNext() ) throw new NoSuchElementException() ;
        RecordBufferPage rbp = recordBufferPage ;
        recordBufferPage = null ;
        return rbp ;
    }
    
    //@Override
    public void remove()
    { throw new UnsupportedOperationException() ; }
}
/*
 * (c) Copyright 2010 Talis Systems Ltd.
 * (c) Copyright 2010 Epimorphics Ltd.
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