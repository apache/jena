/*
 * (c) Copyright 2008 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package dev;

import iterator.Iter;
import iterator.Transform;

import java.util.Iterator;
import java.util.NoSuchElementException;

import lib.Pair;

import com.hp.hpl.jena.tdb.base.block.BlockMgr;
import com.hp.hpl.jena.tdb.base.block.BlockMgrFactory;
import com.hp.hpl.jena.tdb.base.buffer.RecordBuffer;
import com.hp.hpl.jena.tdb.base.record.Record;
import com.hp.hpl.jena.tdb.base.record.RecordFactory;
import com.hp.hpl.jena.tdb.base.recordfile.RecordBufferPage;
import com.hp.hpl.jena.tdb.base.recordfile.RecordBufferPageMgr;
import com.hp.hpl.jena.tdb.base.recordfile.RecordRangeIterator;
import com.hp.hpl.jena.tdb.pgraph.GraphTDB;
import com.hp.hpl.jena.tdb.sys.Const;

public class BPlusTreeRewriter
{
    // Next: WriteDataFile to send output (split keys) somewhere.
    
    public static Pair<Long, Long> rewrite(String rootname, String newRootname)
    {
        String filename = rootname+".dat" ;
        String filename2 = newRootname+".dat" ;

        // Bug : last block may be only one record.  Need to balance last two blocks.
        WriteDataFile writeDataFile = new WriteDataFile(filename2) ;
        Iterator<Record> iter = records(filename) ;

        for ( ; iter.hasNext() ; )
        {
            Record r = iter.next() ;
            writeDataFile.write(r) ;
        }

        // Sort out last two blocks.
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

    // ---- B+Tree Node Writer
    // For now, we do multiple passes.
    // Assume the Records file has been compressed.
    
    public static void phase2(String filename)
    {
        //String filename = rootname+".dat" ;
        BlockMgr blkMgr = BlockMgrFactory.createStdFileNoCache(filename, Const.BlockSize) ;
        RecordBufferPageMgr recordPageMgr = new RecordBufferPageMgr(recordFactory, blkMgr) ;
        
        BlocksIterator iter = new BlocksIterator(recordPageMgr, 0) ;
        
        Transform<Pair<Integer, RecordBufferPage>, Pair<Integer, Record>> converter = 
            new Transform<Pair<Integer, RecordBufferPage>,
                          Pair<Integer, Record>>() 
        {

            @Override
            public Pair<Integer, Record> convert(Pair<Integer, RecordBufferPage> page)
            {
                int x = page.getLeft() ;
                Record r = page.getRight().getRecordBuffer().getHigh() ;
                Record key = recordFactory.createKeyOnly(r) ;
                return new Pair<Integer, Record>(x, key) ;
            }
            
        } ;
        
        Iterator<Pair<Integer, Record>> iter2 = Iter.map(iter, converter) ;
        
        int n = 0 ;
        for ( ; iter2.hasNext() ; )
        {
            Pair<Integer, Record> pair = iter2.next() ;
            //System.out.printf("Split at: (%d, %s)\n",pair.car(), pair.cdr()) ;
            n++ ;
        }
        blkMgr.close() ;
        System.out.printf("++ Count = %d\n", n) ;
    }
    
    // Change to yield id and RecordBufferPage
    private static class BlocksIterator implements Iterator<Pair<Integer, RecordBufferPage>>
    {
        private RecordBufferPage page = null ;
        private Pair<Integer, RecordBufferPage> slot = null ;
        private RecordBufferPageMgr recordPageMgr ;
        
        public BlocksIterator(RecordBufferPageMgr recordPageMgr, int blockId)
        {
            if ( blockId < 0 )
            {
                // End of file
                page = null ; 
                return ;
            }
            
            this.page = recordPageMgr.get(blockId) ;
            this.slot = new Pair<Integer, RecordBufferPage>(blockId, page) ;
            this.recordPageMgr = recordPageMgr ; 
        }
        
        @Override
        public boolean hasNext()
        {
            if ( slot != null ) return true ;
            if ( page == null ) return false ;  // Finished
            int x = page.getLink() ;
            if ( x < 0 )
            {
                // End of file
                page = null ; 
                return false ;
            }
            page = recordPageMgr.get(x) ;
            slot = new Pair<Integer, RecordBufferPage>(x, page) ;
            return true ;
        }
        
        @Override
        public Pair<Integer, RecordBufferPage> next()
        {
            if ( ! hasNext() ) throw new NoSuchElementException() ;
            Pair<Integer, RecordBufferPage> r = slot ;
            slot = null ;
            return r ;
        }
        @Override
        public void remove()
        { throw new UnsupportedOperationException() ; }
        
        
        
    }
    
    // XXX
    
    // ---- Record file writer
    private static class WriteDataFile
    {
        private final BlockMgr blkMgr ;
        private final RecordBufferPageMgr recordPageMgr ;

        // The working variables.
        private RecordBuffer currentBuffer = null ;
        private RecordBufferPage currentPage = null ;
        private RecordBufferPage previousPage = null ;

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
                moveOnOnePage() ;
            currentBuffer.add(r) ;
            recordCount++ ;
            // Now full?
            // Make a note to write next time.
            // Delaying means that the block will not be written until
            // we know another block is needed (at least one record)
            // so when we then know the link id to a non-empty block.
            // Requires a flush(-1) on close.
            if ( currentBuffer.size() >= currentBuffer.maxSize() )
                currentBuffer = null ;
        }

        private void moveOnOnePage()
        {
            // Write, with link, the old block.
            int id = recordPageMgr.allocateId() ;
            if ( currentPage != null )
            {
                Record r = currentPage.getRecordBuffer().getHigh() ;
                Record splitKey = recordFactory.createKeyOnly(r) ;
                // System.out.printf("Split = %s\n", splitKey) ;
                flush(id) ;
            }

            // Now get new space
            RecordBufferPage page = recordPageMgr.create(id) ;
            shift(page) ;
            
        }

        private void flush(int linkId)
        {
            if ( currentPage == null )
                return ;
            currentPage.setLink(linkId) ;
            recordPageMgr.put(currentPage.getId(), currentPage) ;
            blockCount++ ;
            currentBuffer = null ;
        }

        private void close()
        {
            System.out.println(previousPage) ;
            System.out.println(currentPage) ;
            
            // Last needs balancing.
            // Was full and written => no rebalence needed.
            
            // End block id.
            // Flush always writes a block (if currentPage != null)
            // and currentPage == null only initially because moveOneOnePage allocates
            flush(-1) ;
            blkMgr.close() ;
        }
        
        private void shift(RecordBufferPage nextPage)
        {
            previousPage = currentPage ; 
            currentPage = nextPage ;
            if ( nextPage != null )
                currentBuffer = currentPage.getRecordBuffer() ;
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