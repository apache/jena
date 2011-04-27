/*
 * (c) Copyright 2008, 2009 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.tdb.base.recordfile;

import java.nio.ByteBuffer;

import com.hp.hpl.jena.tdb.base.page.Page;
import com.hp.hpl.jena.tdb.base.record.RecordFactory;
import com.hp.hpl.jena.tdb.sys.SystemTDB;

/** The on-disk form of a block of a single RecordBuffer
 * (i.e. not part of a BTree/BPlusTree node).
 * The association of ByteBuffer from the BlockMgr to the RecordBuffer.
 * This must be compatible with B+Tree records nodes and hash buckets.
 */

public class RecordBufferPage extends RecordBufferPageBase
{
    // To Constants
    // Offsets
//    final public static int COUNT      = 0 ;
    final public static int LINK            = 4 ;
    final private static int FIELD_LENGTH   = SystemTDB.SizeOfInt ;

    private RecordBufferPageMgr pageMgr ;
    private int link = Page.NO_ID ;
    
    public int getLink() { return link ; }
    
    public void setLink(int link)
    { 
        this.link = link ;
        getBackingBlock().putInt(LINK, link) ;
    }
    
    public static int calcRecordSize(RecordFactory factory, int blkSize)
    { return RecordBufferPageBase.calcRecordSize(factory, blkSize, FIELD_LENGTH) ; }
    
    public static int calcBlockSize(RecordFactory factory, int maxRec)
    { return RecordBufferPageBase.calcBlockSize(factory, maxRec, FIELD_LENGTH) ; }
    
    
    /*public*/ RecordBufferPage(int id, int linkId, ByteBuffer byteBuffer,
                                RecordFactory factory, RecordBufferPageMgr recordBufferPageMgr, 
                                int count)
    {
        super(id, FIELD_LENGTH, byteBuffer, factory, count) ;
        this.pageMgr = recordBufferPageMgr ; 
        this.link = linkId ;
        
    }
    
    public final RecordBufferPageMgr getPageMgr()
    {
        return pageMgr ;
    }

    public void setPageMgr(RecordBufferPageMgr recordBufferPageMgr)
    {
        this.pageMgr = recordBufferPageMgr ;
    }


    
    @Override
    public String toString()
    { return String.format("RecordBufferPage[id=%d,link=%d]: %s", getId(), getLink(), recBuff) ; }

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