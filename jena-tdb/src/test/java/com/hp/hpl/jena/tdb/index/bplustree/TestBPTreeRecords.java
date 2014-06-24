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

package com.hp.hpl.jena.tdb.index.bplustree;

import org.apache.jena.atlas.junit.BaseTest ;
import org.junit.After ;
import org.junit.AfterClass;
import org.junit.Before ;
import org.junit.BeforeClass;
import org.junit.Test;

import com.hp.hpl.jena.tdb.base.block.BlockMgr;
import com.hp.hpl.jena.tdb.base.block.BlockMgrFactory;
import com.hp.hpl.jena.tdb.base.buffer.RecordBuffer;
import com.hp.hpl.jena.tdb.base.record.Record;
import com.hp.hpl.jena.tdb.base.record.RecordFactory;
import com.hp.hpl.jena.tdb.base.record.RecordLib;
import com.hp.hpl.jena.tdb.base.recordbuffer.RecordBufferPage ;
import com.hp.hpl.jena.tdb.base.recordbuffer.RecordBufferPageMgr ;
import com.hp.hpl.jena.tdb.index.bplustree.BPTreePage;
import com.hp.hpl.jena.tdb.index.bplustree.BPTreeRecords;
import com.hp.hpl.jena.tdb.index.bplustree.BPlusTree;
import com.hp.hpl.jena.tdb.index.bplustree.BPlusTreeParams;
import com.hp.hpl.jena.tdb.sys.SystemTDB;

public class TestBPTreeRecords extends BaseTest
{
    static private boolean oldNullOut ;
    static private boolean oldCheckingNode ;
    static private boolean oldCheckingBTree ;
    
    static private int blockSize ;
    static private RecordFactory recordFactory ;
    
    static private int bufSizeRecord ;
    static private BlockMgr blkMgrRecords  ;
    static private RecordBufferPageMgr recordBufferPageMgr  ;
    static private BPlusTree bPlusTree ;
    
    @BeforeClass public static void beforeClass()
    {
        oldNullOut = SystemTDB.NullOut ;
        SystemTDB.NullOut = true ;
        
        oldCheckingNode = BPlusTreeParams.CheckingNode ;
        BPlusTreeParams.CheckingNode = true ;
        
        oldCheckingBTree = BPlusTreeParams.CheckingTree ;
        BPlusTreeParams.CheckingTree = true ;
        
        blockSize =  4*8 ;  // Which is 6 int records
        recordFactory = new RecordFactory(4, 0) ;
        
        bufSizeRecord = RecordBufferPage.calcRecordSize(recordFactory, blockSize) ;
        blkMgrRecords = BlockMgrFactory.createMem("BPTreeRecords", blockSize) ;
        recordBufferPageMgr = new RecordBufferPageMgr(recordFactory, blkMgrRecords) ;
        
        // B+Tree order does not matter.
        bPlusTree = BPlusTree.attach(new BPlusTreeParams(3, recordFactory), null, blkMgrRecords) ;
    }
    
    @AfterClass public static void afterClass()
    {
        SystemTDB.NullOut = oldNullOut ;
        BPlusTreeParams.CheckingTree = oldCheckingNode ;
        BPlusTreeParams.CheckingTree = oldCheckingBTree ;
    }

    @Test public void bpt_records_1()
    {
        BPTreeRecords bpr = make() ;
        fill(bpr) ;
        check(bpr) ;
        bpr.release() ;
    }
    
    @Before public void before() { blkMgrRecords.beginUpdate() ; }
    @After public void after() { blkMgrRecords.endUpdate() ; }
    

    @Test public void bpt_records_2()
    {
        BPTreeRecords bpr = make() ;
        fill(bpr) ;
        int s = bpr.getCount();
        assertTrue(bpr.isFull()) ;
        BPTreePage z = bpr.split() ;
        assertTrue(z instanceof BPTreeRecords) ;
        assertEquals(s, z.getCount()+bpr.getCount()) ;
        check(bpr) ;
        check((BPTreeRecords)z) ;
        bpr.release() ;
        z.release() ;
    }

    @Test public void bpt_records_3()
    {
        BPTreeRecords bpr = make() ;
        for ( int i = 0 ; bpr.getCount() < bpr.getMaxSize() ; i++ )
            insert(bpr, (i+0x20)) ;
        check(bpr) ;
        bpr.release() ;
    }
    
    @Test public void bpt_records_4()
    {
        BPTreeRecords bpr = make() ;
        for ( int i = bpr.getMaxSize()-1 ; i >= 0 ; i-- )
            insert(bpr, i+0x20) ;
        check(bpr) ;
        bpr.release() ;
    }
    
    @Test public void bpt_records_5()
    {
        BPTreeRecords bpr = make() ;
        int N =  bpr.getMaxSize() ;
        
        for ( int i = bpr.getMaxSize()-1 ; i >= 0 ; i-- )
            insert(bpr, (i+0x20)) ;
        
        delete(bpr, (1+0x20)) ;
        assertEquals(N-1, bpr.getCount()) ;
        check(bpr) ;

        delete(bpr, (2+0x20)) ;
        assertEquals(N-2, bpr.getCount()) ;
        check(bpr) ;
        
        delete(bpr, bpr.getLowRecord()) ;
        assertEquals(N-3, bpr.getCount()) ;
        check(bpr) ;

        bpr.internalDelete(bpr.getHighRecord()) ;
        assertEquals(N-4, bpr.getCount()) ;
        check(bpr) ;
        
        bpr.release() ;
    }
    
    @Test public void bpt_records_6()
    {
        BPTreeRecords bpr = make() ;
        fill(bpr) ;
        
        // No match.
        assertNull(bpr.internalSearch(RecordLib.intToRecord(0x20))) ;

        Record r = RecordLib.intToRecord(0x32) ;
        Record r2 = search(bpr, r) ;
        assertTrue(Record.keyEQ(r, r2)) ;

        r = bpr.getLowRecord() ;
        r2 = search(bpr, r) ;
        assertTrue(Record.keyEQ(r, r2)) ;
        
        r = bpr.getHighRecord() ;
        r2 = search(bpr, r) ;
        assertTrue(Record.keyEQ(r, r2)) ;
        
        bpr.release() ;
    }
    
    @Test public void bpt_shift_1()
    {
        BPTreeRecords bpr1 = make() ;
        BPTreeRecords bpr2 = make() ;

        insert(bpr1, 10) ;
        Record r = bpr1.shiftRight(bpr2, null) ;
        assertNull(r) ;
        //assertTrue(Record.keyEQ(r, RecordTestLib.intToRecord(10))) ;
        contains(bpr1) ;
        contains(bpr2, 10) ;
        
        bpr1.release() ;
        bpr2.release() ;

    }
    
    @Test public void bpt_shift_2()
    {
        BPTreeRecords bpr1 = make() ;
        BPTreeRecords bpr2 = make() ;
        
        insert(bpr1, 10) ;
        Record r = bpr2.shiftLeft(bpr1, null) ;
        
        assertTrue(Record.keyEQ(r, RecordLib.intToRecord(10))) ;
        contains(bpr1) ;
        contains(bpr2, 10) ;
        bpr1.release() ;
        bpr2.release() ;
    }

    @Test public void bpt_shift_3()
    {
        BPTreeRecords bpr1 = make() ;
        BPTreeRecords bpr2 = make() ;
        
        insert(bpr1, 10, 20) ;
        insert(bpr2, 99) ;
        
        Record r = bpr1.shiftRight(bpr2, null) ;

        assertTrue(r+" != "+RecordLib.intToRecord(10), Record.keyEQ(r, RecordLib.intToRecord(10))) ;
        contains(bpr1, 10) ;
        contains(bpr2, 20, 99) ;
        bpr1.release() ;
        bpr2.release() ;
    }

    @Test public void bpt_shift_4()
    {
        BPTreeRecords bpr1 = make() ;
        BPTreeRecords bpr2 = make() ;
        
        insert(bpr1, 10, 20) ;
        insert(bpr2, 5) ;
        
        Record r = bpr2.shiftLeft(bpr1, null) ;
        assertTrue(Record.keyEQ(r, RecordLib.intToRecord(10))) ;
        
        contains(bpr1, 20) ;
        contains(bpr2, 5, 10) ;
        bpr1.release() ;
        bpr2.release() ;
    }
    
    @Test public void bpt_merge_1()
    {
        BPTreeRecords bpr1 = make() ;
        BPTreeRecords bpr2 = make() ;
        
        insert(bpr1, 10, 20) ;
        insert(bpr2, 99) ;
        
        BPTreeRecords bpr3 = (BPTreeRecords)bpr1.merge(bpr2, null) ;
        contains(bpr1, 10, 20, 99) ;
        contains(bpr2) ;
        assertSame(bpr1, bpr3) ;
        bpr1.release() ;
        bpr2.release() ;
    }

    @Test public void bpt_merge_2()
    {
        BPTreeRecords bpr1 = make() ;
        BPTreeRecords bpr2 = make() ;
        
        insert(bpr1, 10, 20) ;
        insert(bpr2, 5) ;
        
        BPTreeRecords bpr3 = (BPTreeRecords)bpr2.merge(bpr1, null) ;
        contains(bpr1) ;
        contains(bpr2, 5, 10, 20) ;
        assertSame(bpr2, bpr3) ;
        bpr1.release() ;
        bpr2.release() ;
    }
    
    private static void check(BPTreeRecords bpr)
    {
        assertTrue(bpr.getCount() >= 0 ) ;
        assertTrue(bpr.getCount() <= bpr.getMaxSize() ) ;
        
        assertEquals(bpr.getRecordBuffer().getLow(), bpr.getLowRecord()) ;
        assertEquals(bpr.getRecordBuffer().getHigh(), bpr.getHighRecord()) ;
        
        for ( int i = 1 ; i < bpr.getCount() ; i++ )
        {
            Record r1 = bpr.getRecordBuffer().get(i-1) ;
            Record r2 = bpr.getRecordBuffer().get(i) ;
            assertTrue(Record.keyLE(r1, r2)) ;
        }
    }

    private static Record search(BPTreeRecords bpr, int x)
    {
        return search(bpr, RecordLib.intToRecord(x)) ;
    }

    
    private static Record search(BPTreeRecords bpr, Record r)
    {
        return bpr.internalSearch(r) ;
    }

    private static void insert(BPTreeRecords bpr, int ... values)
    {
        for ( int value : values )
        {
            bpr.internalInsert( RecordLib.intToRecord( value ) );
        }
    }
    
    private static void insert(BPTreeRecords bpr, Record r)
    {
        bpr.internalInsert(r) ;
    }
    

    private static void delete(BPTreeRecords bpr, int ... values)
    {
        for ( int value : values )
        {
            delete( bpr, RecordLib.intToRecord( value ) );
        }
    }
    
    private static void delete(BPTreeRecords bpr, Record r)
    {
        bpr.internalDelete(r) ;
    }
    

    private static void contains(BPTreeRecords bpr, int ... values)
    {
        assertEquals(values.length, bpr.getCount() ) ;
        for ( int i = 1 ; i < values.length ; i++ )
            assertTrue(Record.compareByKeyValue(RecordLib.intToRecord(values[i]),bpr.getRecordBuffer().get(i)) == 0 ) ;
    }

    private static BPTreeRecords make()
    {
        RecordBufferPage page = recordBufferPageMgr.create() ;
        return new BPTreeRecords(bPlusTree, page) ;
    }
    
    private static void fill(BPTreeRecords bpr)
    {
        RecordBuffer rb = bpr.getRecordBuffer() ;
        for ( int i = 0 ; rb.size() < rb.maxSize() ; i++ )
            insert(bpr, (i+0x30)) ;
    }
}
