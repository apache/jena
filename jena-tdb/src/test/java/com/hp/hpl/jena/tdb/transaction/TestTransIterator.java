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

package com.hp.hpl.jena.tdb.transaction;

import java.util.Iterator ;

import org.apache.jena.atlas.iterator.Iter ;
import org.apache.jena.atlas.junit.BaseTest ;
import org.junit.Test ;

import com.hp.hpl.jena.tdb.base.record.Record ;
import com.hp.hpl.jena.tdb.base.record.RecordLib ;
import com.hp.hpl.jena.tdb.index.IndexTestLib ;
import com.hp.hpl.jena.tdb.index.RangeIndex ;
import com.hp.hpl.jena.tdb.index.bplustree.BPlusTree ;

public class TestTransIterator extends BaseTest
{
    static BPlusTree build(int order, int[] values)
    {
        // See TestBPlusTree
        BPlusTree bpt = BPlusTree.makeMem(order, order, RecordLib.TestRecordLength, 0) ;
        bpt = BPlusTree.addTracking(bpt) ;
        IndexTestLib.add(bpt, values) ;
        return bpt ;
    }
 
    @Test public void transIter_01()
    {
        int vals[] = { } ;
        RangeIndex rIndex = build(2, vals) ;
        Iterator<Record> iter1 = rIndex.iterator() ;
        Iterator<Record> iter2 = rIndex.iterator() ;
        count(iter1, vals.length) ;
        count(iter2, vals.length) ;
    }

    @Test public void transIter_02()
    {
        int vals[] = { 1, 2, 3, 4, 5, 6, 7 } ;
        RangeIndex rIndex = build(2, vals) ;
        Iterator<Record> iter1 = rIndex.iterator() ;
        Iterator<Record> iter2 = rIndex.iterator() ;
        count(iter1, vals.length) ;
        count(iter2, vals.length) ;
    }
    
    @Test public void transIter_03()
    {
        // Interleaved.
        int vals[] = { 1, 2, 3, 4, 5, 6, 7 } ;
        RangeIndex rIndex = build(2, vals) ;
        Iterator<Record> iter1 = rIndex.iterator() ;
        Iterator<Record> iter2 = rIndex.iterator() ;
        for ( ; iter1.hasNext() ; )
        {
            Record r1 = iter1.next();
            Record r2 = iter2.next();
        }
        assertFalse(iter2.hasNext()) ;
    }
    
    private static void count(Iterator<Record> iter, long expected)
    {
        long x = Iter.count(iter) ; 
        assertEquals(expected, x) ;
    }
    
}
