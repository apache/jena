/*
 * (c) Copyright 2008 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.tdb.btree;

import static junit.TestBase.* ;
import static com.hp.hpl.jena.tdb.base.ConfigTest.TestRecordLength;
import static com.hp.hpl.jena.tdb.base.record.RecordTestLib.intToRecord;
import static com.hp.hpl.jena.tdb.base.record.RecordTestLib.r;
import static com.hp.hpl.jena.tdb.base.record.RecordTestLib.toIntList;
import static java.lang.String.format;
import static lib.ListUtils.asList;
import static lib.ListUtils.unique;
import static lib.RandomLib.random;
import static test.Gen.permute;
import static test.Gen.rand;
import static test.Gen.strings;

import java.util.ArrayList;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;

import junit.TestBase;

import test.RandomExecution;

import com.hp.hpl.jena.tdb.base.record.Record;
import com.hp.hpl.jena.tdb.index.RangeIndex;

public class RangeIndexTestLib
{

    // ---------- Utilities
    
    public static RangeIndex buildRangeIndex(RangeIndexMaker maker, int[] keys)
    {
        RangeIndex rIndex = maker.make() ;
        RangeIndexTestLib.add(rIndex, keys) ;
        return rIndex ;
    }
    
    public static void testIteration(RangeIndex rIndex, int[] keys, int numIterations)
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
            
            List<Integer> slice = r(rIndex.iterator(r(lo), r(hi))) ;
            List<Integer> expected = new ArrayList<Integer>(keys.length) ;  
            for ( Integer ii : x.subSet(lo, hi) )
                expected.add(ii) ;
            assertEquals(format("(%d,%d)",lo, hi), expected, slice) ;
        }
    }

    /* One random test : print the keys if there was a problem */ 
    
    public static void randTest(RangeIndexMaker maker, int maxValue, int numKeys)
    {
        if ( numKeys >= 3000 )
            System.err.printf("Warning: too many keys\n") ;
            
        int[] keys1 = rand(numKeys, 0, maxValue) ;
        int[] keys2 = permute(keys1, 4*numKeys) ;
        try {
            RangeIndex rIndex = maker.make() ;
            testInsert(rIndex, keys1);
            if ( true )
            {
                // Checking tests.
                testRangeIndex(rIndex, keys2);
                // Test iteration - quite expensive.
                testIteration(rIndex, keys1, 10) ;
            }
            testDelete(rIndex, keys2) ;
            rIndex.close() ;
        } catch (RuntimeException ex)
        {
            System.err.printf("Maker : \n", maker.getLabel()) ;
            System.err.printf("int[] keys1 = {%s} ;\n", strings(keys1)) ;
            System.err.printf("int[] keys2 = {%s}; \n", strings(keys2)) ;
            throw ex ;
        }
    }

    // ---- Test utils
    
    public static void testInsert(RangeIndex rIndex, int[] keys)
    {
        RangeIndexTestLib.add(rIndex, keys) ;
        testRangeIndex(rIndex, keys);
    }

    public static RangeIndex testInsert(RangeIndexMaker maker, int[] keys)
    {
        RangeIndex rIndex = maker.make() ;
        testInsert(rIndex, keys);
        return rIndex ; 
    }

    public static void testInsertDelete(RangeIndex rIndex, int[] buildKeys, int[] deleteKeys)
    {
        testInsert(rIndex, buildKeys) ;
        testDelete(rIndex, deleteKeys) ;
    }

    public static void testDelete(RangeIndex rIndex, int[] vals)
    {
        long size1 = rIndex.sessionTripleCount() ;
        int count = 0 ;
        count = delete(rIndex, vals) ;
    
        List<Record> x =  intToRecord(vals, TestRecordLength) ;
        for ( Record r : x )
        {
            boolean b = rIndex.delete(r) ;
            if ( b )
                count ++ ;
        }
        
        for ( Record r : x )
            TestBase.assertFalse(rIndex.contains(r)) ;
        long size2 = rIndex.sessionTripleCount() ;
        assertEquals(size1-count, size2) ;
    }

    public static int delete(RangeIndex bTree, int[] vals)
    {
        int count = 0 ;
        for ( int v : vals )
        {
            boolean b = bTree.delete(r(v)) ;
            if ( b )
                count ++ ;
        }
        return count ;
    }

    public static void add(RangeIndex rIndex, int[] vals)
    {
        List<Record> x = intToRecord(vals, TestRecordLength) ;
        for ( Record r : x )
            rIndex.add(r) ;
    }

    public static void testRangeIndex(RangeIndex rIndex, int[] records)
    {
        if ( BTreeParams.CheckingBTree ) 
            rIndex.check() ;
    
        List<Integer> x = toIntList(rIndex.iterator());
        
        // Make a unique list of expected records.  Remove duplicates
        List<Integer> y = unique(asList(records)) ;
        
        assertEquals("Expected records size and tree size different", y.size(), rIndex.sessionTripleCount()) ;
        assertEquals("Expected records size and iteration over all keys are of different sizes", y.size(), x.size()) ;
        
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
            Record rec = intToRecord(k) ;
            Record r2 = rIndex.find(rec) ;
            assertNotNull("Finding "+rec, r2) ;
        }
    }

    public static void randTests(RangeIndexMaker order, int maxValue, int maxNumKeys, int iterations, boolean showProgess)
    {
        RangeIndexTestGenerator test = new RangeIndexTestGenerator(order, maxValue, maxNumKeys) ;
        RandomExecution.randExecGenerators(test, iterations, showProgess) ;
    }
    
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