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

package com.hp.hpl.jena.tdb.index;

import static com.hp.hpl.jena.tdb.base.record.RecordLib.intToRecord ;
import static com.hp.hpl.jena.tdb.base.record.RecordLib.r ;
import static com.hp.hpl.jena.tdb.base.record.RecordLib.toIntList ;
import static java.lang.String.format ;
import static org.apache.jena.atlas.lib.ListUtils.asList ;
import static org.apache.jena.atlas.lib.ListUtils.unique ;
import static org.apache.jena.atlas.lib.RandomLib.random ;
import static org.apache.jena.atlas.test.Gen.permute ;
import static org.apache.jena.atlas.test.Gen.rand ;
import static org.apache.jena.atlas.test.Gen.strings ;
import static org.junit.Assert.assertEquals ;
import static org.junit.Assert.assertNotNull ;
import static org.junit.Assert.fail ;

import java.util.ArrayList ;
import java.util.List ;
import java.util.SortedSet ;
import java.util.TreeSet ;

import org.apache.jena.atlas.junit.BaseTest ;

import com.hp.hpl.jena.tdb.base.record.Record ;
import com.hp.hpl.jena.tdb.base.record.RecordLib ;

public class IndexTestLib
{

    // ---------- Utilities
    
//    public static RangeIndex buildRangeIndex(RangeIndexMaker maker, int[] keys)
//    {
//        RangeIndex index = maker.make() ;
//        IndexTestLib.add(index, keys) ;
//        return index ;
//    }

    public static Index buildIndex(IndexMaker maker, int[] keys)
    {
        Index index = maker.makeIndex() ;
        IndexTestLib.add(index, keys) ;
        return index ;
    }

    
    public static void testIteration(RangeIndex index, int[] keys, int numIterations)
    {
        // Shared across test-lets
        SortedSet<Integer> x = new TreeSet<>() ;
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
            
            List<Integer> slice = r(index.iterator(r(lo), r(hi))) ;
            List<Integer> expected = new ArrayList<>(keys.length) ;
            for ( Integer ii : x.subSet(lo, hi) )
                expected.add(ii) ;
            assertEquals(format("(%d,%d)",lo, hi), expected, slice) ;
        }
    }

    /* One random test : print the keys if there was a problem */ 
    
    public static void randTest(Index index, int maxValue, int numKeys)
    {
        if ( numKeys >= 5000 )
            System.err.printf("Warning: too many keys\n") ;
            
        int[] keys1 = rand(numKeys, 0, maxValue) ;
        int[] keys2 = permute(keys1, 4*numKeys) ;
        try {
            testInsert(index, keys1);
            if ( true )
            {
                // Checking tests.
                testIndexContents(index, keys2);
                // Test iteration - quite expensive.
                if ( index instanceof RangeIndex )
                    testIteration((RangeIndex)index, keys1, 10) ;
            }
            testDelete(index, keys2) ;
            index.close() ;
        } catch (RuntimeException ex)
        {
            System.err.printf("Index : %s\n", index.getClass().getName()) ;
            System.err.printf("int[] keys1 = {%s} ;\n", strings(keys1)) ;
            System.err.printf("int[] keys2 = {%s}; \n", strings(keys2)) ;
            throw ex ;
        }
    }

    // ---- Test utils
    
    public static void testInsert(Index index, int[] keys)
    {
        IndexTestLib.add(index, keys) ;
        testIndexContents(index, keys);
    }

    public static Index testInsert(IndexMaker maker, int[] keys)
    {
        Index index = maker.makeIndex() ;
        testInsert(index, keys);
        return index ; 
    }

    public static void testInsertDelete(Index index, int[] buildKeys, int[] deleteKeys)
    {
        testInsert(index, buildKeys) ;
        testDelete(index, deleteKeys) ;
    }

    public static void testDelete(Index index, int[] vals)
    {
        long size1 = index.size() ;
        
        int count = 0 ;
        count = delete(index, vals) ;
    
        List<Record> x =  intToRecord(vals, RecordLib.TestRecordLength) ;
        for ( Record r : x )
        {
            boolean b = index.delete(r) ;
            if ( b )
                count ++ ;
        }
        
        for ( Record r : x )
            BaseTest.assertFalse(index.contains(r)) ;
        long size2 = index.size() ;

        assertEquals(size1-count, size2) ;
    }

    public static int delete(Index index, int[] vals)
    {
        int count = 0 ;
        for ( int v : vals )
        {
            boolean b = index.delete(r(v)) ;
            if ( b )
                count ++ ;
        }
        return count ;
    }

    public static void add(Index index, int[] vals)
    {
        //System.out.println("Add: "+Arrays.toString(vals)) ;
        List<Record> x = intToRecord(vals, RecordLib.TestRecordLength) ;
        for ( Record r : x )
        {
            //System.out.println("  Add: "+r) ;
            index.add(r) ;
        }
    }

    public static void testIndexContents(Index index, int[] records)
    {
        List<Integer> x = toIntList(index.iterator());
        
        // Make a unique list of expected records.  Remove duplicates
        List<Integer> y = unique(asList(records)) ;
        
        assertEquals("Expected records size and tree size different", y.size(), index.size()) ;
        assertEquals("Expected records size and iteration over all keys are of different sizes", y.size(), x.size()) ;
        
        if ( index instanceof RangeIndex )
        {
            // Check sorted order
            for ( int i = 0 ; i < x.size()-2 ; i++ )
            {
                if ( x.get(i) > x.get(i+1) )
                {
                    fail("check failed: "+strings(records)) ;
                    return ;
                }
            }
        }
        
        // Check each expected record is in the tree
        for ( int k : y)
        {
            Record rec = intToRecord(k) ;
            Record r2 = index.find(rec) ;
            assertNotNull("Finding "+rec, r2) ;
        }
    }
}
