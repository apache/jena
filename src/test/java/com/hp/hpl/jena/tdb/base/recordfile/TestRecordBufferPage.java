/*
 * (c) Copyright 2008, 2009 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.tdb.base.recordfile;

import org.junit.BeforeClass ;
import org.junit.Test ;
import org.openjena.atlas.junit.BaseTest ;

import com.hp.hpl.jena.tdb.base.block.BlockMgr ;
import com.hp.hpl.jena.tdb.base.block.BlockMgrFactory ;
import com.hp.hpl.jena.tdb.base.buffer.RecordBuffer ;
import com.hp.hpl.jena.tdb.base.record.Record ;
import com.hp.hpl.jena.tdb.base.record.RecordFactory ;
import com.hp.hpl.jena.tdb.sys.SystemTDB ;

public class TestRecordBufferPage extends BaseTest
{
    // Testing: records are 2 bytes, 3 records per block.  
    
    static final int TestRecordSize = 2 ;           // Size, in bytes.
    static final int TestNumRecord  = 3 ;           // Size, in bytes.
    static RecordFactory factory = new RecordFactory(2, 0) ; 
    
    @BeforeClass static public void before()
    {
        SystemTDB.NullOut = true ;    
    }
    
    @Test public void recBufferPage01()
    {
        BlockMgr blkMgr = makeBlockMgr() ;
        RecordBufferPageMgr rpm = new RecordBufferPageMgr(factory, blkMgr) ;
        RecordBufferPage page = rpm.create() ;
        fill(page.getRecordBuffer(), 10, 20, 30) ;
        assertEquals(10, get(page, 0)) ;
        assertEquals(20, get(page, 1)) ;
        assertEquals(30, get(page, 2)) ;
    }
    
    @Test public void recBufferPage02()
    {
        BlockMgr blkMgr = makeBlockMgr() ;
        RecordBufferPageMgr rpm = new RecordBufferPageMgr(factory, blkMgr) ;
        int x = -99 ;
        {
            RecordBufferPage page1 = rpm.create() ;
            fill(page1.getRecordBuffer(), 10, 20, 30) ;
            // Now forget it.
            rpm.put(page1) ;
            x = page1.getId() ;
            page1 = null ;
        }
        {
            RecordBufferPage page2 = rpm.getRead(x) ;
            assertEquals(10, get(page2, 0)) ;
            assertEquals(20, get(page2, 1)) ;
            assertEquals(30, get(page2, 2)) ;
            rpm.release(page2) ;
        }
    }

    
    private static void fill(RecordBuffer rb, int ... nums)
    {
        for ( int i = 0 ; i < nums.length ; i++ )
        {
            Record rec = record(nums[i]) ; 
            rb.add(rec) ;
        }
    }
    
    private static int get(RecordBufferPage rbp, int idx) { return get(rbp.getRecordBuffer(), idx) ; } 
    
    private static int get(RecordBuffer rb, int idx) 
    {
        Record r = rb.get(idx) ;
        int v = (r.getKey()[0])<<8 | ((r.getKey()[1])&0xFF) ;
        return v ;
    }
    
    private static Record record(int i)
    {
        byte b[] = new byte[]{ 
            (byte)((i>>8)&0xFF),
            (byte)(i&0xFF)} ; 
        Record r = factory.create(b) ;
        return r ;
    }

    private static BlockMgr makeBlockMgr()
    {
        return BlockMgrFactory.createMem("RecordBuffer", RecordBufferPage.calcBlockSize(factory, TestNumRecord)) ; 
    }
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