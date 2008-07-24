/*
 * (c) Copyright 2008 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package dev;

import com.hp.hpl.jena.tdb.Const;
import com.hp.hpl.jena.tdb.base.block.BlockMgr;
import com.hp.hpl.jena.tdb.base.block.BlockMgrFactory;
import com.hp.hpl.jena.tdb.base.buffer.RecordBuffer;
import com.hp.hpl.jena.tdb.base.record.Record;
import com.hp.hpl.jena.tdb.base.record.RecordFactory;
import com.hp.hpl.jena.tdb.base.recordfile.RecordBufferPage;
import com.hp.hpl.jena.tdb.base.recordfile.RecordBufferPageMgr;
import com.hp.hpl.jena.tdb.pgraph.PGraphBase;

public class BPlusTreeRewriter
{
    RecordFactory f = PGraphBase.indexRecordFactory ; 
    BlockMgr blkMgr = null ;
    RecordBufferPageMgr recordPageMgr = null ;
    RecordBuffer currentBuffer = null ;
    RecordBufferPage currentPage = null ;
    
    public BPlusTreeRewriter(String filename)
    {
        // No point caching.
        blkMgr = BlockMgrFactory.createStdFileNoCache(filename, Const.BlockSize) ;
        recordPageMgr = new RecordBufferPageMgr(f, blkMgr) ;
    }
    
    public void write(Record r)
    {
        if ( currentBuffer == null )
            moveOneOnePage() ;
        currentBuffer.add(r) ;
        // Now full?
        // Make a note to write next time.
        // Delaying means an empty (last) block is not handled until a record is written  
        if ( currentBuffer.size() >= currentBuffer.maxSize() )
            currentBuffer = null ;
            
    }
    
    private void moveOneOnePage()
    {
        // Write, with link, the old block.
        int id = recordPageMgr.allocateId() ;
        if ( currentPage != null )
        {
            // Check split is the high of lower.
            Record r = currentPage.getRecordBuffer().getHigh() ;
            Record k = f.createKeyOnly(r) ;
            //System.out.printf("Split = %s\n", k) ;
            flush(id) ;
        }
        // Now get new space
        
        currentPage = recordPageMgr.create(id) ;
        currentBuffer = currentPage.getRecordBuffer() ;
    }
    
    private void flush(int _linkId)
    {
        if ( currentPage == null )
            return ;
        currentPage.setLink(_linkId) ;
        recordPageMgr.put(currentPage.getId(), currentPage) ;
        currentBuffer = null ;
        currentPage = null ;
    }
    
    public void close()
    {
        // End block id.
        // Flush always writes a block (if currentPage != null)
        flush(-1) ;
        blkMgr.close() ;
    }
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