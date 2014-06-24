/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.hp.hpl.jena.tdb.base.recordfile;

import org.apache.jena.atlas.junit.BaseTest ;
import org.junit.AfterClass ;
import org.junit.BeforeClass ;
import org.junit.Test ;

import com.hp.hpl.jena.tdb.base.block.BlockMgr ;
import com.hp.hpl.jena.tdb.base.block.BlockMgrFactory ;
import com.hp.hpl.jena.tdb.base.buffer.RecordBuffer ;
import com.hp.hpl.jena.tdb.base.record.Record ;
import com.hp.hpl.jena.tdb.base.record.RecordFactory ;
import com.hp.hpl.jena.tdb.base.recordbuffer.RecordBufferPage ;
import com.hp.hpl.jena.tdb.base.recordbuffer.RecordBufferPageMgr ;
import com.hp.hpl.jena.tdb.sys.SystemTDB ;

public class TestRecordBufferPage extends BaseTest
{
    // Testing: records are 2 bytes, 3 records per block.  
    
    static final int TestRecordSize = 2 ;           // Size, in bytes.
    static final int TestNumRecord  = 3 ;           // Size, in bytes.
    static RecordFactory factory = new RecordFactory(2, 0) ; 
    
    static boolean originalNullOut ; 
    @BeforeClass static public void beforeClass()
    {
        originalNullOut = SystemTDB.NullOut ;
        SystemTDB.NullOut = true ;    
    }
    
    @AfterClass static public void afterClass()
    {
        SystemTDB.NullOut = originalNullOut ;    
    }

    @Test public void recBufferPage01()
    {
        BlockMgr blkMgr = makeBlockMgr() ;
        blkMgr.beginUpdate() ;
        RecordBufferPageMgr rpm = new RecordBufferPageMgr(factory, blkMgr) ;
        RecordBufferPage page = rpm.create() ;
        fill(page.getRecordBuffer(), 10, 20, 30) ;
        assertEquals(10, get(page, 0)) ;
        assertEquals(20, get(page, 1)) ;
        assertEquals(30, get(page, 2)) ;
        rpm.release(page) ;
        blkMgr.endUpdate() ;
    }
    
    @Test public void recBufferPage02()
    {
        BlockMgr blkMgr = makeBlockMgr() ;
        blkMgr.beginUpdate() ;
        RecordBufferPageMgr rpm = new RecordBufferPageMgr(factory, blkMgr) ;
        int x = -99 ;
        {
            RecordBufferPage page1 = rpm.create() ;
            fill(page1.getRecordBuffer(), 10, 20, 30) ;
            x = page1.getId() ;
            rpm.put(page1) ;
            page1 = null ;
        }
        blkMgr.endUpdate() ;
        blkMgr.beginRead() ;
        {
            RecordBufferPage page2 = rpm.getRead(x) ;
            assertEquals(10, get(page2, 0)) ;
            assertEquals(20, get(page2, 1)) ;
            assertEquals(30, get(page2, 2)) ;
            rpm.release(page2) ;
        }
        blkMgr.endRead() ;
    }

    
    private static void fill(RecordBuffer rb, int ... nums)
    {
        for ( int num : nums )
        {
            Record rec = record( num );
            rb.add( rec );
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
