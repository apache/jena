/*
 * (c) Copyright 2008 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package dev;

import java.util.Iterator;

import lib.Pair;

import com.hp.hpl.jena.tdb.Const;
import com.hp.hpl.jena.tdb.base.block.BlockMgr;
import com.hp.hpl.jena.tdb.base.block.BlockMgrFactory;
import com.hp.hpl.jena.tdb.base.buffer.RecordBuffer;
import com.hp.hpl.jena.tdb.base.record.Record;
import com.hp.hpl.jena.tdb.base.record.RecordFactory;
import com.hp.hpl.jena.tdb.base.recordfile.RecordBufferPage;
import com.hp.hpl.jena.tdb.base.recordfile.RecordBufferPageMgr;
import com.hp.hpl.jena.tdb.base.recordfile.RecordRangeIterator;
import com.hp.hpl.jena.tdb.pgraph.GraphTDB;

public class BPlusTreeRewriter
{
    // Next: WriteDataFile to send output (split keys) somewhere.
    
    public static Pair<Long, Long> rewrite(String rootname, String newRootname)
    {
        String filename = rootname+".dat" ;
        String filename2 = newRootname+".dat" ;

        WriteDataFile writeDataFile = new WriteDataFile(filename2) ;
        Iterator<Record> iter = records(filename) ;

        for ( ; iter.hasNext() ; )
        {
            Record r = iter.next() ;
            writeDataFile.write(r) ;
        }

        writeDataFile.close();
        return new Pair<Long, Long>(writeDataFile.recordCount, writeDataFile.blockCount) ;
    }

    private static RecordFactory recordFactory = GraphTDB.indexRecordFactory ;
    
    private static RecordRangeIterator records(String filename)
    {
        BlockMgr blkMgr = BlockMgrFactory.createStdFileNoCache(filename, Const.BlockSize) ;
        RecordBufferPageMgr recordPageMgr = new RecordBufferPageMgr(recordFactory, blkMgr) ;
        RecordBufferPage page = recordPageMgr.get(0) ;
        return new RecordRangeIterator(page, null,null) ;
    }

    // Record file writer
    private static class WriteDataFile
    {

        private final BlockMgr blkMgr ;
        private final RecordBufferPageMgr recordPageMgr ;

        // The working variables.
        private RecordBuffer currentBuffer = null ;
        private RecordBufferPage currentPage = null ;

        // Counters
        private long blockCount = 0 ;
        private long recordCount = 0 ;

        private WriteDataFile(String filename)
        {
            // No point caching.
            blkMgr = BlockMgrFactory.createStdFileNoCache(filename, Const.BlockSize) ;
            recordPageMgr = new RecordBufferPageMgr(recordFactory, blkMgr) ;
        }

        private void write(Record r)
        {
            if ( currentBuffer == null )
                moveOneOnePage() ;
            currentBuffer.add(r) ;
            recordCount++ ;
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
//              // Check split is the high of lower.
//              Record r = currentPage.getRecordBuffer().getHigh() ;
//              Record k = recordFactory.createKeyOnly(r) ;
//              //System.out.printf("Split = %s\n", k) ;
                flush(id) ;
            }

            // Now get new space
            currentPage = recordPageMgr.create(id) ;
            currentBuffer = currentPage.getRecordBuffer() ;
        }

        private void flush(int linkId)
        {
            if ( currentPage == null )
                return ;
            currentPage.setLink(linkId) ;
            recordPageMgr.put(currentPage.getId(), currentPage) ;
            blockCount++ ;
            currentBuffer = null ;
            currentPage = null ;
        }

        private void close()
        {
            // End block id.
            // Flush always writes a block (if currentPage != null)
            //  currentPage == null only initially because moveOneOnePage allocates
            flush(-1) ;
            blkMgr.close() ;
        }
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