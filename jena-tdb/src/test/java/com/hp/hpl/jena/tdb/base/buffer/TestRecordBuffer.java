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

package com.hp.hpl.jena.tdb.base.buffer;

import static com.hp.hpl.jena.tdb.base.record.RecordLib.intToRecord;
import static com.hp.hpl.jena.tdb.base.record.RecordLib.r;

import java.util.Iterator;
import java.util.List;


import org.apache.jena.atlas.junit.BaseTest ;
import org.junit.AfterClass ;
import org.junit.BeforeClass;
import org.junit.Test;

import com.hp.hpl.jena.tdb.base.record.Record;
import com.hp.hpl.jena.tdb.base.record.RecordFactory;
import com.hp.hpl.jena.tdb.base.record.RecordLib;
import com.hp.hpl.jena.tdb.sys.SystemTDB;

public class TestRecordBuffer extends BaseTest
{
    static RecordFactory recordFactory = new RecordFactory(RecordLib.TestRecordLength, 0) ;
    
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

    @Test public void recBuffer01()
    {
        RecordBuffer rb = make(4, 4) ;
        contains(rb, 2, 4, 6, 8) ;
    }
    
    @Test public void recBuffer02()
    {
        RecordBuffer rb = make(4, 4) ;
        int idx = -1 ;
        idx = find(rb, 6) ;
        assertEquals(2, idx) ;
        idx = find(rb, 8) ;
        
        assertEquals(3, idx) ;
        idx = find(rb, 4) ;
        assertEquals(1, idx) ;
        idx = find(rb, 2) ;
        assertEquals(0, idx) ;

        idx = find(rb, 3) ;
        assertEquals(-2, idx) ;
        idx = find(rb, 0) ;
        assertEquals(-1, idx) ;
        idx = find(rb, 10) ;
        assertEquals(-5, idx) ;
    }

    // Shift at LHS
    @Test public void recBuffer03()
    {
        RecordBuffer rb = make(4,5) ;
        contains(rb, 2, 4, 6, 8) ;
        rb.shiftUp(0) ;
        rb.set(0, r(0)) ;
        contains(rb, 0, 2, 4, 6, 8) ;
        rb.shiftDown(0) ;
        contains(rb, 2, 4, 6, 8) ;
    }    
    
    @Test public void recBuffer04()
    {
        RecordBuffer rb = make(4,5) ;
        contains(rb, 2, 4, 6, 8) ;
        rb.shiftDown(0) ;
        
        contains(rb, 4, 6, 8) ;
        rb.shiftUp(0) ;

        rb.set(0,r(1)) ;
        contains(rb, 1, 4, 6, 8) ;
    }    
    

    // Shift at middle
    @Test public void recBuffer05()
    {
        RecordBuffer rb = make(4,5) ;
        contains(rb, 2, 4, 6, 8) ;
        rb.shiftUp(2) ;
        rb.set(2, r(0)) ;
        contains(rb, 2, 4, 0, 6, 8) ;
        rb.shiftDown(2) ;
        contains(rb, 2, 4, 6, 8) ;
    }    

    @Test public void recBuffer06()
    {
        RecordBuffer rb = make(4,5) ;
        contains(rb, 2, 4, 6, 8) ;
        rb.shiftDown(2) ;
        contains(rb, 2, 4, 8) ;
        rb.shiftUp(2) ;
        contains(rb, 2, 4, -1, 8) ;
    }    

    // Shift RHS - out of bounds
    @Test public void recBuffer07()
    {
        RecordBuffer rb = make(4,5) ;
        contains(rb, 2, 4, 6, 8) ;
        rb.shiftUp(3) ;
        rb.set(3, r(1)) ;
        contains(rb, 2, 4, 6, 1, 8) ;
        rb.shiftDown(3) ;
        contains(rb, 2, 4, 6, 8) ;
    }    

    @Test public void recBuffer08()
    {
        RecordBuffer rb = make(4,5) ;
        contains(rb, 2, 4, 6, 8) ;
        rb.shiftDown(3) ;
        contains(rb, 2, 4, 6) ;
        rb.shiftUp(2) ;
        contains(rb, 2, 4, -1, 6) ;
    }    

    // Errors
    
    @Test(expected=BufferException.class) 
    public void recBuffer09()
    {
        RecordBuffer rb = make(4,5) ;
        contains(rb, 2, 4, 6, 8) ;
        rb.shiftDown(4) ;
    }  
    
    @Test(expected=BufferException.class) 
    public void recBuffer10()
    {
        RecordBuffer rb = make(4,5) ;
        contains(rb, 2, 4, 6, 8) ;
        rb.shiftUp(4) ;
    }  

    @Test(expected=BufferException.class) 
    public void recBuffer11()
    {
        RecordBuffer rb = make(5,5) ;
        contains(rb, 2, 4, 6, 8, 10) ;
        rb.add(r(12)) ;
    }  
    
    // Copy, duplicate, clear
    @Test public void recBuffer12()
    {
        RecordBuffer rb = make(5,5) ;
        contains(rb, 2, 4, 6, 8, 10) ;
        RecordBuffer rb2 = rb.duplicate() ;
        rb2.set(1, r(99)) ;
        contains(rb, 2, 4, 6, 8, 10) ;
        contains(rb2, 2, 99, 6, 8, 10) ;
    }
    
    @Test public void recBuffer13()
    {
        RecordBuffer rb = make(5,5) ;
        contains(rb, 2, 4, 6, 8, 10) ;
        rb.clear(1, 3) ;
        contains(rb, 2, -1, -1, -1, 10) ;
    }
    
    @Test public void recBuffer14()
    {
        RecordBuffer rb = make(5,5) ;
        contains(rb, 2, 4, 6, 8, 10) ;
        RecordBuffer rb2 = make(5,5) ;
        contains(rb2, 2, 4, 6, 8, 10) ;
        rb.copy(0, rb2, 1, 4) ;
        contains(rb2, 2, 2, 4, 6, 8) ;
    }

    // Remove tests
    
    @Test public void recBuffer15()
    {
        RecordBuffer rb = make(5,5) ;
        contains(rb, 2, 4, 6, 8, 10) ;
        rb.removeTop() ;
        contains(rb, 2, 4, 6, 8) ;
        rb.remove(1) ;
        contains(rb, 2, 6, 8) ;
        rb.remove(2) ;
        contains(rb, 2, 6) ;
        rb.remove(0) ;
        contains(rb, 6) ;
        rb.remove(0) ;
        contains(rb) ;
    }

    @Test public void recBufferIterate01()
    {
        RecordBuffer rb = make(5,5) ;
        same(rb.iterator(), 2,4,6,8,10) ;
    }

    @Test public void recBufferIterate02()
    {
        RecordBuffer rb = make(3,5) ;
        Iterator<Record> iter = rb.iterator() ;
        same(iter, 2, 4, 6) ;
    }

    @Test public void recBufferIterate03()
    {
        RecordBuffer rb = make(3,5) ;
        Iterator<Record> iter = rb.iterator( intToRecord(4), null) ;
        same(iter, 4, 6) ;
    }

    @Test public void recBufferIterate04()
    {
        RecordBuffer rb = make(3,5) ;
        Iterator<Record> iter = rb.iterator( intToRecord(3), null) ;
        same(iter, 4, 6) ;
    }

    @Test public void recBufferIterate05()
    {
        RecordBuffer rb = make(3,5) ;
        Iterator<Record> iter = rb.iterator( intToRecord(1), null) ;
        same(iter, 2, 4, 6) ;
    }

    @Test public void recBufferIterate06()
    {
        RecordBuffer rb = make(3,5) ;
        Iterator<Record> iter = rb.iterator( null, intToRecord(1)) ;
        same(iter) ;
    }

    @Test public void recBufferIterate07()
    {
        RecordBuffer rb = make(3,5) ;
        Iterator<Record> iter = rb.iterator( null, intToRecord(2)) ;
        same(iter ) ;
    }

    @Test public void recBufferIterate08()
    {
        RecordBuffer rb = make(3,5) ;
        Iterator<Record> iter = rb.iterator( null, intToRecord(3)) ;
        same(iter,2 ) ;
    }
    
    @Test public void recBufferIterate09()
    {
        RecordBuffer rb = make(5,5) ;
        Iterator<Record> iter = rb.iterator( null, intToRecord(99)) ;
        same(iter, 2, 4, 6, 8, 10) ;
    }

    @Test public void recBufferIterate10()
    {
        RecordBuffer rb = make(5,5) ;
        Iterator<Record> iter = rb.iterator( intToRecord(4), intToRecord(8)) ;
        same(iter, 4, 6 ) ;
    }

    @Test public void recBufferIterate11()
    {
        RecordBuffer rb = make(5,5) ;
        Iterator<Record> iter = rb.iterator( intToRecord(3), intToRecord(9)) ;
        same(iter, 4, 6, 8 ) ;
    }
    
    // ---- Support
    private static void contains(RecordBuffer rb, int... vals)
    {
        assertEquals("Length mismatch: ", vals.length, rb.size()) ;
        
        for ( int i = 0 ; i < vals.length ; i++ )
            if ( vals[i] == -1 )
                assertTrue(rb.isClear(i))  ;
            else
            {
                Record r = RecordLib.intToRecord(vals[i]) ;
                Record r2 = rb.get(i) ;
                int x = RecordLib.recordToInt(r2) ;
                assertEquals("Value mismatch: ", vals[i], x) ;
            }
    }
    
    private static void same(Iterator<Record> iter, int... vals)
    {
        List<Integer> list = RecordLib.toIntList(iter) ;
        assertEquals("Length mismatch: ", vals.length, list.size()) ;
        
        for ( int i = 0 ; i < vals.length ; i++ )
        {
            int x = list.get(i) ;
            assertEquals("Value mismatch: ", vals[i], x) ;
            }
    }

    
    public int find(RecordBuffer rb, int v)
    {
        return rb.find(r(v)) ;
    }

    private static RecordBuffer make(int n, int len)
    { 
        RecordBuffer rb = new RecordBuffer(recordFactory, len) ;
        for ( int i = 0 ; i < n ; i++ )
        {
            Record r = RecordLib.intToRecord(2*i+2) ;  
            rb.add(r) ;
        }
        return rb ;
    }
}
