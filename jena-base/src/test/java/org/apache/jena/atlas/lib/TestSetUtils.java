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

package org.apache.jena.atlas.lib;

import static org.apache.jena.atlas.lib.ListUtils.asList ;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.HashSet ;
import java.util.List ;
import java.util.Set ;

import org.junit.Test ;

public class TestSetUtils
{
    @Test public void set01() 
    {
        Set<Integer> x = set(1,2,3) ;
        test(x,1,2,3) ;
    }

    @Test public void set_intersection_1() 
    {
        Set<Integer> x1 = set(1,2,3) ;
        Set<Integer> x2 = set(1,2,3) ;
        Set<Integer> x3 = SetUtils.intersection(x1, x2) ;
        test(x3, 1,2,3) ;
        x3 = SetUtils.intersection(x2, x1) ;
        test(x3, 1,2,3) ;
    }

    @Test public void set_intersection_2() 
    {
        Set<Integer> x1 = set(1,2,3) ;
        Set<Integer> x2 = set(2,9) ;
        Set<Integer> x3 = SetUtils.intersection(x1, x2) ;
        test(x3, 2) ;
        x3 = SetUtils.intersection(x2, x1) ;
        test(x3, 2) ;
    }

    @Test public void set_intersection_3() 
    {
        Set<Integer> x1 = set(1,2,3) ;
        Set<Integer> x2 = set(6,7,8) ;
        Set<Integer> x3 = SetUtils.intersection(x1, x2) ;
        test(x3) ;
        x3 = SetUtils.intersection(x2, x1) ;
        test(x3) ;
    }

    @Test public void set_union_1() 
    {
        Set<Integer> x1 = set(1,2,3) ;
        Set<Integer> x2 = set(1,2,3) ;
        Set<Integer> x3 = SetUtils.union(x1, x2) ;
        test(x3, 1,2,3) ;
        x3 = SetUtils.union(x2, x1) ;
        test(x3, 1,2,3) ;
    }

    @Test public void set_union_2() 
    {
        Set<Integer> x1 = set(1,2,3) ;
        Set<Integer> x2 = set(2,9) ;
        Set<Integer> x3 = SetUtils.union(x1, x2) ;
        test(x3, 1,2,3,9) ;
        x3 = SetUtils.union(x2, x1) ;
        test(x3, 1,2,3,9) ;
    }

    @Test public void set_union_3() 
    {
        Set<Integer> x1 = set(1,2,3) ;
        Set<Integer> x2 = set() ;
        Set<Integer> x3 = SetUtils.union(x1, x2) ;
        test(x3,1,2,3) ;
        x3 = SetUtils.union(x2, x1) ;
        test(x3,1,2,3) ;
    }

    @Test public void set_difference_1() 
    {
        Set<Integer> x1 = set(1,2,3) ;
        Set<Integer> x2 = set() ;
        Set<Integer> x3 = SetUtils.difference(x1, x2) ;
        test(x3,1,2,3) ;
        x3 = SetUtils.difference(x2, x1) ;
        test(x3) ;
    }

    @Test public void set_difference_2() 
    {
        Set<Integer> x1 = set(1,2,3) ;
        Set<Integer> x2 = set(3) ;
        Set<Integer> x3 = SetUtils.difference(x1, x2) ;
        test(x3,1,2) ;
        x3 = SetUtils.difference(x2, x1) ;
        test(x3) ;
    }

    @Test public void set_difference_3() 
    {
        Set<Integer> x1 = set(1,2,3) ;
        Set<Integer> x2 = set(4,5,6) ;
        Set<Integer> x3 = SetUtils.difference(x1, x2) ;
        test(x3,1,2,3) ;
        x3 = SetUtils.difference(x2, x1) ;
        test(x3,4,5,6) ;
    }

    @Test public void set_symmetric_differnce_1() {
        Set<Integer> x1 = set(1,2,3) ;
        Set<Integer> x2 = set(4,3) ;
        Set<Integer> expected = set(4,1,2) ;
        assertEquals(expected, SetUtils.symmetricDifference(x1, x2)) ;
    }
    
    @Test public void set_symmetric_differnce_2() {
        Set<Integer> x1 = set(1,2,3) ;
        Set<Integer> x2 = set(9,8) ;
        Set<Integer> expected = SetUtils.union(x1, x2) ;
        assertEquals(expected, SetUtils.symmetricDifference(x1, x2)) ;
    }

    @Test public void set_symmetric_differnce_3() {
        Set<Integer> x1 = set(1,2,3) ;
        Set<Integer> x2 = set(2,3,1) ;
        Set<Integer> expected = set() ;
        assertEquals(expected, SetUtils.symmetricDifference(x1, x2)) ;
    }
    
    @Test public void set_symmetric_differnce_4() {
        Set<Integer> x1 = set(1,2,3) ;
        Set<Integer> x2 = set() ;
        Set<Integer> expected = set(1,2,3) ;
        assertEquals(expected, SetUtils.symmetricDifference(x1, x2)) ;
        assertEquals(expected, SetUtils.symmetricDifference(x2, x1)) ;
    }
    // --------
    
    private static Set<Integer> set(int... values)
    {
        return new HashSet<>(asList(values)) ;
    }

    private static void test(Set<Integer> x, int...values)
    {
        List<Integer> y = asList(values) ;
        assertEquals(y.size(), x.size()) ;

        for ( Integer aY : y )
        {
            assertTrue( x.contains( aY ) );
        }
    }
}
