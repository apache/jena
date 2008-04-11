/*
 * (c) Copyright 2007, 2008 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.tdb.btree;

import static com.hp.hpl.jena.tdb.base.ConfigTest.TestRecordLength;
import static java.lang.String.format;
import static lib.ListUtils.unique;
import static lib.RandomLib.random;
import static test.Gen.permute;
import static test.Gen.rand;
import static test.Gen.strings;
import static lib.ListUtils.* ;
import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;

import com.hp.hpl.jena.tdb.base.block.BlockMgr;
import com.hp.hpl.jena.tdb.base.block.BlockMgrFactory;
import com.hp.hpl.jena.tdb.base.record.R;
import com.hp.hpl.jena.tdb.base.record.Record;
import com.hp.hpl.jena.tdb.btree.BTree;
import com.hp.hpl.jena.tdb.btree.BTreeParams;


import lib.RandomLib;
import test.BaseTest;
import test.RandomExecution;
import test.RandomExecution.ExecGenerator;

/** Support for testing BTrees */

public class BTreeTestBase extends BaseTest
{
    public static void randTests(int order, int maxValue, int maxNumKeys, int iterations, boolean showProgess)
    {
        BTreeTestGenerator test = new BTreeTestGenerator(order, maxValue, maxNumKeys) ;
        RandomExecution.randExecGenerators(test, iterations, showProgess) ;
    }
    
    static class BTreeTestGenerator implements ExecGenerator
    {
        int maxNumKeys ;
        int maxValue ;
        int order ;
        
        BTreeTestGenerator(int order, int maxValue, int maxNumKeys)
        {
            if ( maxValue <= maxNumKeys )
                throw new IllegalArgumentException("BTreeTest: Max value less than number of keys") ;
            this.order = order ;
            this.maxValue = maxValue ; 
            this.maxNumKeys = maxNumKeys ;
        }
        
        @Override
        public void executeOneTest()
        {
            int numKeys = RandomLib.random.nextInt(maxNumKeys)+1 ;
            BTreeTestBase.randTest(order, maxValue, numKeys) ;
        }
    }
    
    /* One random test : print the keys if there was a problem */ 
    
    public static void randTest(int order, int maxValue, int numKeys)
    {
        if ( numKeys >= 3000 )
            System.err.printf("Warning: too many keys\n") ;
            
        int[] keys1 = rand(numKeys, 0, maxValue) ;
        int[] keys2 = permute(keys1, 4*numKeys) ;
        try {
            BTree bTree = buildBTree(order, keys1);
            if ( true )
            {
                // Checking tests.
                testBTree(bTree, keys2);
                // Test iteration - quite expensive.
                testIteration(bTree, keys1, 10) ;
            }
            testDelete(bTree, keys2) ;
            bTree.close() ;
        } catch (RuntimeException ex)
        {
            System.err.printf("int order=%d ;\n", order) ;
            System.err.printf("int[] keys1 = {%s} ;\n", strings(keys1)) ;
            System.err.printf("int[] keys2 = {%s}; \n", strings(keys2)) ;
            throw ex ;
        }
    }

    // ---------- Utilities
    
    public static void testIteration(BTree bTree, int[] keys, int numIterations)
    {
        // Shared across test-lets
        SortedSet<Integer> x = new TreeSet<Integer>() ;
        for ( int v : keys )
            x.add(v) ;
        
        for ( int i = 0 ; i < numIterations ; i++ )
        {
            int lo = random.nextInt(keys.length) ;
            int hi = random.nextInt(keys.length) ;
            if ( lo > hi )
            {
                int t = lo ;
                lo = hi ;
                hi = t ;
            }
            // Does not consider nulls - assumed to be part of functional testing.
            // Tweak lo and hi
            if ( lo != 0 && random.nextFloat() < 0.5 )
                lo-- ;  // Negatives confuse the int/record code.
            if ( random.nextFloat() < 0.5 )
                hi++ ;
            
            List<Integer> slice = r(bTree.iterator(r(lo), r(hi))) ;
            List<Integer> expected = new ArrayList<Integer>(keys.length) ;  
            for ( Integer ii : x.subSet(lo, hi) )
                expected.add(ii) ;
            assertEquals(format("(%d,%d)",lo, hi), expected, slice) ;
        }
    }
    
    public static BTree testInsert(int order, int[] keys)
    {
        BTree bTree = buildBTree(order, keys);
        testBTree(bTree, keys);
        return bTree ; 
    }

    public static void testDelete(int order, int[] buildKeys, int[] deleteKeys)
    {
        BTree bTree = buildBTree(order, buildKeys);
        testDelete(bTree, deleteKeys) ;
        bTree.close() ;
    }

    public static void testDelete(BTree bTree, int[] vals)
    {
        long size1 = bTree.sessionTripleCount() ;
        int count = 0 ;
        count = delete(bTree, vals) ;

        List<Record> x =  R.intToRecord(vals, TestRecordLength) ;
        for ( Record r : x )
        {
            Record rd = bTree.deleteAndReturnOld(r) ;
            if ( rd != null )
                count ++ ;
        }
        
        for ( Record r : x )
            assertFalse(bTree.contains(r)) ;
        long size2 = bTree.sessionTripleCount() ;
        assertEquals(size1-count, size2) ;
    }

    public static int delete(BTree bTree, int[] vals)
    {
        int count = 0 ;
        for ( int v : vals )
        {
            Record rd = bTree.deleteAndReturnOld(r(v)) ;
            if ( rd != null )
                count ++ ;
        }
        return count ;
    }
    
    static String filename = "tmp/test.btree" ;
    public static BTree createBTree(int order)
    {
        BTreeParams p = new BTreeParams(order, TestRecordLength, 0) ;
        BlockMgr mgr = null ;
        
        if ( true )
            mgr = BlockMgrFactory.createMem(p.getBlockSize()) ;
        else
        {
            File f = new File(filename) ;
            f.delete() ;
            mgr = BlockMgrFactory.createFile(filename, p.getBlockSize()) ;
        }
        BTree bTree = new BTree(order, TestRecordLength, mgr) ;
        return bTree ;
    }

    public static BTree buildBTree(int order, int[] keys)
    {
        BTree bTree = createBTree(order) ;
        add(bTree, keys) ;
        return bTree ;
    }

    public static void add(BTree btree, int[] vals)
    {
        List<Record> x = R.intToRecord(vals, TestRecordLength) ;
        for ( Record r : x )
            btree.add(r) ;
    }

    public static void testBTree(BTree btree, int[] records)
    {
        if ( BTreeParams.CheckingBTree ) 
            btree.check() ;

        List<Integer> x = R.toIntList(btree.iterator());
        
        // Make a unique list of expected records.  Remove duplicates
        List<Integer> y = unique(asList(records)) ;
        
        assertEquals("Expected records size and tree size different", y.size(), btree.sessionTripleCount()) ;
        assertEquals("Excpect records size and iteration over all keys are of different sizes", y.size(), x.size()) ;
        
        // Check sorted order
        for ( int i = 0 ; i < x.size()-2 ; i++ )
        {
            if ( x.get(i) > x.get(i+1) )
            {
                fail("check failed: "+strings(records)) ;
                return ;
            }
        }

        // Check each expected record is in the tree
        for ( int k : y)
        {
            Record rec = R.intToRecord(k) ;
            Record r2 = btree.find(rec) ;
            if ( r2 == null )
            {
                btree.dump() ;
                assertNotNull("Finding "+rec, r2) ;
            }
        }
    }
    
    public static Record r(int v)
    {
        return R.intToRecord(v, TestRecordLength) ; 
    }

    public static int r(Record rec)
    {
        return R.recordToInt(rec) ; 
    }

    public static List<Integer> toIntList(int... vals)
    {
        List<Integer> x = new ArrayList<Integer>() ;
        for ( int i : vals )
            x.add(i) ;
        return x ;
    }
    
    public static List<Integer> r(Iterator<Record> iter)
    {
        return R.toIntList(iter) ;
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