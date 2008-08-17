/*
 * (c) Copyright 2008 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.tdb.base.recordfile;

import io.IndentedWriter;

import java.nio.ByteBuffer;

import com.hp.hpl.jena.tdb.base.buffer.RecordBuffer;
import com.hp.hpl.jena.tdb.base.page.PageBase;
import com.hp.hpl.jena.tdb.base.record.RecordFactory;

/** The on-disk form of a block of a single RecordBuffer
 * (i.e. not part of a BTree/BPlusTree node).
 * The association of ByteBuffer from the BlockMgr to the RecordBuffer.
 * This must be compatible with B+Tree records nodes and hashbuckets.
 */

public class RecordBufferPage extends PageBase //implements Page
{
    // To Constants
    final public static int COUNT      = 0 ;
    final public static int LINK       = 4 ;
    final public static int HEADER     = 8 ;
    final public static int NO_LINK    = -1 ;

    // Interface: "Page" - id, byteBuffer, count
    private RecordBuffer recBuff ;
    private RecordBufferPageMgr pageMgr ;
 
    private int link ;
    public int getLink() { return link ; }
    
    public void setLink(int link)
    { 
        this.link = link ;
        getBackingByteBuffer().putInt(LINK, link) ; // put(Index, Value)
    }
    
    public static int calcRecordSize(RecordFactory factory, int blkSize)
    { 
        // Length = X*recordLength + HEADER
        int x = blkSize-HEADER ; 
        return x / factory.recordLength() ;
    }
    
    public static int calcBlockSize(RecordFactory factory, int maxRec)
    { 
        // Count, link, records 
        return HEADER + factory.recordLength()*maxRec ;
    }
    
    public RecordBufferPage(int id, int linkId, ByteBuffer byteBuffer,
                            RecordFactory factory, RecordBufferPageMgr recordBufferPageMgr, 
                            int count)
    {
        super(id, byteBuffer) ;
        this.link = linkId ;
        this.pageMgr = null ;
        byteBuffer.position(HEADER) ;
        ByteBuffer bb = byteBuffer.slice();
        this.recBuff = new RecordBuffer(bb, factory, count) ;
    }

    public RecordBuffer getRecordBuffer()
    {
        return recBuff ;
    }
    
    public RecordBufferPageMgr getPageMgr()
    {
        return pageMgr ;
    }

    public void setPageMgr(RecordBufferPageMgr recordBufferPageMgr)
    {
        this.pageMgr = recordBufferPageMgr ;
    }

    @Override
    public int getCount()
    {
        return recBuff.size() ;
    }

    @Override
    public int getMaxSize()
    {
        return recBuff.maxSize() ;
    }

    @Override
    public void setCount(int count)
    { recBuff.setSize(count) ; }

    @Override
    public String toString()
    { return String.format("RecordBufferPage[id=%d,link=%d]: %s", getId(), getLink(), recBuff) ; }

    public void output(IndentedWriter out)
    { out.print(toString()) ; }
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