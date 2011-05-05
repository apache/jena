/*
 * (c) Copyright 2007, 2008, 2009 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.tdb.index.btree;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.openjena.atlas.junit.BaseTest ;

import com.hp.hpl.jena.tdb.base.record.Record;
import com.hp.hpl.jena.tdb.base.record.RecordFactory;
import com.hp.hpl.jena.tdb.index.btree.BTree;
import com.hp.hpl.jena.tdb.index.btree.BTreeParams;
import com.hp.hpl.jena.tdb.sys.SystemTDB;

public class TestBTreeLong extends BaseTest
{
    static RecordFactory recordFactory  ;
    static final int LongRecordLength = 16 ;
    static boolean b ;
    
    @BeforeClass public static void before()
    { 
        recordFactory = new RecordFactory(LongRecordLength, 0) ;
        BTreeParams.CheckingNode = true ;
        b = SystemTDB.NullOut ;
        SystemTDB.NullOut = true ;
    }
    
    @AfterClass public static void after()
    { 
        SystemTDB.NullOut = b ;
    }
    
    @Test public void record1()
    {
        BTree bTree = create(2) ;
        Record r = record(1) ;
        bTree.add(r) ;
    }
    
    @Test public void record2()
    {
        BTree bTree = create(2) ;
        Record r = record(1) ;
        bTree.add(r) ;
        assertTrue(bTree.contains(r)) ;
        bTree.add(r) ;
        assertTrue(bTree.contains(r)) ;
        assertEquals(1, bTree.sizeByCounting()) ;
        bTree.delete(r) ;
        assertFalse(bTree.contains(r)) ;
        assertEquals(0, bTree.sizeByCounting()) ;
    }
    
    @Test public void record3()
    {
        BTree bTree = create(2) ;
        Record r1 = record(1) ;
        Record r2 = record(10) ;
        bTree.add(r1) ;
        assertTrue(bTree.contains(r1)) ;
        bTree.add(r2) ;
        assertTrue(bTree.contains(r2)) ;
        assertEquals(2, bTree.sizeByCounting()) ;
        bTree.delete(r1) ;
        assertFalse(bTree.contains(r1)) ;
        assertTrue(bTree.contains(r2)) ;
        assertEquals(1, bTree.sizeByCounting()) ;
    }
    
    @Test public void record4()
    {
        BTree bTree = create(2) ;
        for ( int i = 0 ; i < 5 ; i++ )
            bTree.add(record(i)) ;
        
        Record r1 = record(10) ;
        Record r2 = record(20) ;
        
        bTree.add(r1) ;
        assertTrue(bTree.contains(r1)) ;
        bTree.add(r2) ;
        assertTrue(bTree.contains(r2)) ;
        assertEquals(7, bTree.sizeByCounting()) ;
        bTree.delete(r1) ;
        assertFalse(bTree.contains(r1)) ;
        assertTrue(bTree.contains(r2)) ;
        assertEquals(6, bTree.sizeByCounting()) ;
    }
    
    // --------
    
    static BTree create(int order)
    {
        BTree bTree = BTree.makeMem(order, LongRecordLength, 0) ;
        return bTree ;
    }
    
    static Record record(int v)
    {
        byte[]data = new byte[LongRecordLength] ;
        for ( int i = 0 ; i < LongRecordLength ; i++ )
            data[i] = (byte)(i+v) ;
        return recordFactory.create(data) ;
    }
}

/*
 * (c) Copyright 2007, 2008, 2009 Hewlett-Packard Development Company, LP
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