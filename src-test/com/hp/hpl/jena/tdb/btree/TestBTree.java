/*
 * (c) Copyright 2007, 2008 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.tdb.btree;

import static com.hp.hpl.jena.tdb.btree.BTreeTestBase.r;
import static com.hp.hpl.jena.tdb.btree.BTreeTestBase.toIntList;

import java.util.List;

import com.hp.hpl.jena.tdb.base.BaseConfig;
import com.hp.hpl.jena.tdb.base.record.R;
import com.hp.hpl.jena.tdb.base.record.Record;
import com.hp.hpl.jena.tdb.btree.BTree;
import com.hp.hpl.jena.tdb.btree.BTreeParams;

import org.junit.After;
import org.junit.BeforeClass;
import org.junit.Test;
import test.BaseTest;

public class TestBTree extends BaseTest 
{
 
    @BeforeClass public static void before()
    { 
        BTreeParams.CheckingNode = true ;
        BaseConfig.NullOut = true ;
    }
    
    
    BTree bt = null ;
    
    @After public void afterTest()
    { 
        if ( bt != null )
            bt.close();
        bt = null ;
    }
    
    // -- Root-only
    
    @Test public void btree_ins_0_1()
    {
        // Just a root.
        int[] keys = {0, 1, 2};
        bt = BTreeTestBase.testInsert(2, keys) ;
        assertEquals(0, r(bt.minKey())) ;
        assertEquals(2, r(bt.maxKey())) ;
    }
    
    @Test public void btree_ins_0_2()
    {
        // Empty tree
        int[] keys = {};
        bt = BTreeTestBase.testInsert(2, keys) ;
        assertNull(bt.minKey()) ;
        assertNull(bt.maxKey()) ;
    }
    
    @Test public void btree_ins_2_01() 
    {
        int[] keys = {0, 1, 2, 3, 4, 5, 6, 7, 8, 9};
        bt = BTreeTestBase.testInsert(2, keys) ;
        assertEquals(0, r(bt.minKey())) ;
        assertEquals(9, r(bt.maxKey())) ;
    }
    
    @Test public void btree_ins_2_02() 
    {
        int[] keys = {9,8,7,6,5,4,3,2,1,0};
        bt = BTreeTestBase.testInsert(2, keys) ;
        assertEquals(0, r(bt.minKey())) ;
        assertEquals(9, r(bt.maxKey())) ;
    }

    @Test public void btree_ins_2_03()
    {
        int[] keys = {0,2,4,6,8,1,3,5,7,9};
        bt = BTreeTestBase.testInsert(2, keys) ;
        assertEquals(0, r(bt.minKey())) ;
        assertEquals(9, r(bt.maxKey())) ;
    }

    @Test public void btree_ins_2_04()
    {
        int[] keys = {0,9,2,7,4,5,6,3,8,1};
        bt = BTreeTestBase.testInsert(2, keys) ;
        assertEquals(0, r(bt.minKey())) ;
        assertEquals(9, r(bt.maxKey())) ;
    }
    
    @Test public void btree_ins_2_05()
    {
        int[] keys = {0,18,4,14,8,10,12,6,16,2};
        bt = BTreeTestBase.testInsert(2, keys) ;
        assertFalse(bt.contains(r(1))) ;
        assertFalse(bt.contains(r(999))) ;
        assertFalse(bt.contains(r(-9))) ;
        assertFalse(bt.contains(r(7))) ;
        assertEquals(0, r(bt.minKey())) ;
        assertEquals(18, r(bt.maxKey())) ;
    }
    
    @Test public void btree_del_0_1()
    {
        int[] keys1 = {0, 1, 2};
        int[] keys2 = {0, 1, 2};
        BTreeTestBase.testDelete(2, keys1, keys2) ;
    }

    @Test public void btree_del_0_2()
    {
        int[] keys1 = {0, 1, 2};
        int[] keys2 = {2, 1, 0};
        BTreeTestBase.testDelete(2, keys1, keys2) ;
    }

    @Test public void btree_del_0_3()
    {
        int[] keys1 = {0, 1, 2};
        int[] keys2 = {1, 0, 2};
        BTreeTestBase.testDelete(2, keys1, keys2) ;
    }

    @Test public void btree_del_2_01()
    {
        int[] keys1 = {0, 1, 2, 3, 4, 5, 6, 7, 8, 9} ;
        int[] keys2 = {0, 1, 2, 3, 4, 5, 6, 7, 8, 9} ;
        BTreeTestBase.testDelete(2,  keys1, keys2) ;
    }
    
    @Test public void btree_del_2_02()
    {
        int[] keys1 = {0, 1, 2, 3, 4, 5, 6, 7, 8, 9} ;
        int[] keys2 = {9, 8, 7, 6, 5, 4, 3, 2, 1, 0} ;
        BTreeTestBase.testDelete(2,  keys1, keys2) ;
    }
    
    @Test public void btree_del_2_03()
    {
        int[] keys1 = {0, 1, 2, 3, 4, 5, 6, 7, 8, 9} ;
        int[] keys2 = {0, 2, 4, 6, 8, 1, 3, 5, 7, 9} ;
        BTreeTestBase.testDelete(2,  keys1, keys2) ;
    }
    
    @Test public void btree_del_2_04()
    {
        int[] keys1 = {0, 1, 2, 3, 4, 5, 6, 7, 8, 9} ;
        int[] keys2 = {0, 9, 2, 7, 4, 5, 6, 3, 8, 1} ;
        BTreeTestBase.testDelete(2,  keys1, keys2) ;
    }
    
    @Test public void btree_del_2_05()
    {
        int[] keys1 = {9, 8, 7, 6, 5, 4, 3, 2, 1, 0} ;
        int[] keys2 = {0, 1, 2, 3, 4, 5, 6, 7, 8, 9} ;
        BTreeTestBase.testDelete(2,  keys1, keys2) ;
    }
    
    @Test public void btree_del_2_06()
    {
        int[] keys1 = {9, 8, 7, 6, 5, 4, 3, 2, 1, 0} ;
        int[] keys2 = {9, 8, 7, 6, 5, 4, 3, 2, 1, 0} ;
        BTreeTestBase.testDelete(2,  keys1, keys2) ;
    }
    
    @Test public void btree_del_2_07()
    {
        int[] keys1 = {9, 8, 7, 6, 5, 4, 3, 2, 1, 0} ;
        int[] keys2 = {0, 2, 4, 6, 8, 1, 3, 5, 7, 9} ;
        BTreeTestBase.testDelete(2,  keys1, keys2) ;
    }
    
    @Test public void btree_del_2_08()
    {
        int[] keys1 = {9, 8, 7, 6, 5, 4, 3, 2, 1, 0} ;
        int[] keys2 = {0, 9, 2, 7, 4, 5, 6, 3, 8, 1} ;
        BTreeTestBase.testDelete(2, keys1, keys2) ;
    }
    
    @Test public void btree_del_2_09()
    {
        int[] keys1 = {0, 2, 4, 6, 8, 1, 3, 5, 7, 9} ;
        int[] keys2 = {0, 1, 2, 3, 4, 5, 6, 7, 8, 9} ;
        BTreeTestBase.testDelete(2,  keys1, keys2) ;
    }
    
    @Test public void btree_del_2_10()
    {
        int[] keys1 = {0, 2, 4, 6, 8, 1, 3, 5, 7, 9} ;
        int[] keys2 = {9, 8, 7, 6, 5, 4, 3, 2, 1, 0} ;
        BTreeTestBase.testDelete(2,  keys1, keys2) ;
    }
    
    @Test public void btree_del_2_11()
    {
        int[] keys1 = {0, 2, 4, 6, 8, 1, 3, 5, 7, 9} ;
        int[] keys2 = {0, 2, 4, 6, 8, 1, 3, 5, 7, 9} ;
        BTreeTestBase.testDelete(2,  keys1, keys2) ;
    }
    
    @Test public void btree_del_2_12()
    {
        int[] keys1 = {0, 2, 4, 6, 8, 1, 3, 5, 7, 9} ;
        int[] keys2 = {0, 9, 2, 7, 4, 5, 6, 3, 8, 1} ;
        BTreeTestBase.testDelete(2,  keys1, keys2) ;
    }
    
    @Test public void btree_iter_2_01()
    {
        int[] keys = {0, 2, 4, 6, 8, 1, 3, 5, 7, 9} ;
        bt = BTreeTestBase.buildBTree(2, keys) ;
        List<Integer> x = R.toIntList(bt.iterator(r(4), r(6))) ;
        List<Integer> expected = toIntList(4,5) ;
        assertEquals(expected, x) ;
    }
    
    @Test public void btree_iter_2_02()
    {
        int[] keys = {0, 1, 2, 3, 4, 5, 6, 7, 8, 9} ;
        bt = BTreeTestBase.buildBTree(2, keys) ;
        List<Integer> x = R.toIntList(bt.iterator(r(4), r(7))) ;
        List<Integer> expected = toIntList(4,5,6) ;
        assertEquals(expected, x) ;
    }
    
    @Test public void btree_iter_2_03()
    {
        int[] keys = {0, 1, 2, 3, 4, 5, 6, 7, 8, 9} ;
        bt = BTreeTestBase.buildBTree(2, keys) ;
        List<Integer> x = R.toIntList(bt.iterator(r(4), null)) ;
        List<Integer> expected = toIntList(4,5,6,7,8,9) ;
        assertEquals(expected, x) ;
    }
    
    @Test public void btree_iter_2_04()
    {
        int[] keys = {0, 1, 2, 3, 4, 5, 6, 7, 8, 9} ;
        bt = BTreeTestBase.buildBTree(2, keys) ;
        List<Integer> x = R.toIntList(bt.iterator(null, null)) ;
        List<Integer> expected = toIntList(keys) ;
        assertEquals(expected, x) ;
    }

    @Test public void btree_iter_2_05()
    {
        int[] keys = {0, 1, 2, 3, 4, 5, 6, 7, 8, 9} ;
        bt = BTreeTestBase.buildBTree(2, keys) ;
        List<Integer> x = R.toIntList(bt.iterator(null, r(4))) ;
        List<Integer> expected = toIntList(0,1,2,3) ;
        assertEquals(expected, x) ;
    }
    
    @Test public void btree_iter_2_07()
    {
        int[] keys = {0, 1, 2, 3, 4, 5, 6, 7, 8, 9} ;
        bt = BTreeTestBase.buildBTree(2, keys) ;
        List<Integer> x = R.toIntList(bt.iterator(null, r(99))) ;
        List<Integer> expected = toIntList(keys) ;
        assertEquals(expected, x) ;
    }
    
    @Test public void btree_iter_2_08()
    {
        int[] keys = {1, 2, 3, 4, 5, 6, 7, 8, 9} ;
        bt = BTreeTestBase.buildBTree(2, keys) ;
        List<Integer> x = R.toIntList(bt.iterator(r(0), r(99))) ;
        List<Integer> expected = toIntList(keys) ;
        assertEquals(expected, x) ;
    }

    @Test public void btree_iter_2_09()
    {
        int[] keys = {1, 2, 3, 4, /*5, 6,*/ 7, 8, 9, 10 ,11} ;
        bt = BTreeTestBase.buildBTree(2, keys) ;
        List<Integer> x = R.toIntList(bt.iterator(r(5), r(7))) ;
        List<Integer> expected = toIntList() ;
        assertEquals(expected, x) ;
    }
    
    // Root
    @Test public void btree_iter_0_01()
    {
        int[] keys = {1, 2, 3, 4, 5} ;
        bt = BTreeTestBase.buildBTree(5, keys) ;
        List<Integer> x = R.toIntList(bt.iterator(r(2), r(4))) ;
        List<Integer> expected = toIntList(2,3) ;
        assertEquals(expected, x) ;
    }

    @Test public void btree_iter_0_02()
    {
        int[] keys = {1, 2, 3, 4, 5} ;
        bt = BTreeTestBase.buildBTree(5, keys) ;
        List<Integer> x = R.toIntList(bt.iterator(null, null)) ;
        List<Integer> expected = toIntList(keys) ;
        assertEquals(expected, x) ;
    }

    @Test public void btree_iter_0_03()
    {
        int[] keys = {1, 2, 3, 4, 5} ;
        bt = BTreeTestBase.buildBTree(5, keys) ;
        List<Integer> x = R.toIntList(bt.iterator(r(5), null)) ;
        List<Integer> expected = toIntList(5) ;
        assertEquals(expected, x) ;
    }

    @Test public void btree_iter_0_04()
    {
        int[] keys = {1, 2, 3, 4, 5} ;
        bt = BTreeTestBase.buildBTree(5, keys) ;
        List<Integer> x = R.toIntList(bt.iterator(r(0), r(0))) ;
        List<Integer> expected = toIntList() ;
        assertEquals(expected, x) ;
    }
    
    @Test public void btree_iter_0_05()
    {
        int[] keys = {1, 2, 3, 4, 5} ;
        bt = BTreeTestBase.buildBTree(5, keys) ;
        List<Integer> x = R.toIntList(bt.iterator(r(1), r(0))) ;
        List<Integer> expected = toIntList() ;
        assertEquals(expected, x) ;
    }
    
    @Test public void btree_ret_1()
    {
        int[] keys = {1, 2, 3, 4, 5} ;
        bt = BTreeTestBase.buildBTree(2, keys) ;
        Record r = bt.addAndReturnOld(R.intToRecord(3)) ;
        assertNotNull(r) ;
        assertEquals(R.intToRecord(3), r) ;
        r = bt.addAndReturnOld(R.intToRecord(9)) ;
        assertNull(r) ;
    }
    
    @Test public void btree_ret_2()
    {
        int[] keys = {1, 2, 3, 4, 5} ;
        bt = BTreeTestBase.buildBTree(2, keys) ;
        Record r = bt.deleteAndReturnOld(R.intToRecord(9)) ;
        assertNull(r) ;
        r = bt.addAndReturnOld(R.intToRecord(1)) ;
        assertNotNull(r) ;
        assertEquals(R.intToRecord(1), r) ;
    }
    
    
    @Test public void btree_2_N()
    {
        for ( int i = 0 ; i < 10 ; i++ )
            BTreeTestBase.randTest(2, 999, 20) ;
    }

    @Test public void btree_3_N()
    {
        for ( int i = 0 ; i < 10 ; i++ )
            BTreeTestBase.randTest(3, 9999, 100) ;
    }
}

/*
 * (c) Copyright 2007, 2008 Hewlett-Packard Development Company, LP
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