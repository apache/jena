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

import java.util.Arrays ;
import java.util.Collections ;
import java.util.List ;

import org.apache.jena.atlas.iterator.Iter ;
import org.apache.jena.atlas.junit.BaseTest ;
import org.apache.jena.atlas.lib.MultiSet ;
import org.junit.Test ;


public class TestMultiSet extends BaseTest
{
    @Test public void multiSet_01()
    {
        MultiSet<String> x = new MultiSet<>() ;
        assertTrue(x.isEmpty()) ;
        assertEquals(0, x.count("A")) ;
    }

    @Test public void multiSet_02()
    {
        MultiSet<String> x = new MultiSet<>() ;
        x.add("A") ;
        assertFalse(x.isEmpty()) ;
        assertEquals(1, x.count("A") ) ;
        x.add("A") ;
        assertEquals(2, x.count("A") ) ;
    }

    @Test public void multiSet_03()
    {
        MultiSet<String> x = new MultiSet<>() ;
        x.add("A") ;
        x.add("A") ;
        x.remove("A") ;
        assertEquals(1, x.count("A") ) ;
        assertTrue(x.contains("A")) ;
        x.remove("A") ;
        assertEquals(0, x.count("A") ) ;
        assertFalse(x.contains("A")) ;
    }

    @Test public void multiSet_04()
    {
        String[] data = { } ;
        iterTest(data) ;
    }


    @Test public void multiSet_05()
    {
        String[] data = { "A" } ;
        iterTest(data) ;
    }

    @Test public void multiSet_06()
    {
        String[] data = { "A", "B", "C" } ;
        iterTest(data) ;
    }


    @Test public void multiSet_07()
    {
        String[] data = { "A", "B", "C", "A" } ;
        iterTest(data) ;
    }

    @Test public void multiSet_08()
    {
        String[] data = {  } ;
        MultiSet<String> x = add(data) ;
        assertEquals(0, x.size()) ;
    }
    
    @Test public void multiSet_09()
    {
        String[] data = { "A", "A" } ;
        MultiSet<String> x = add(data) ;
        assertEquals(2, x.size()) ;
    }

    @Test public void multiSet_10()
    {
        String[] data = { "A", "A" } ;
        MultiSet<String> x = add(data) ;
        x.remove("A") ;
        assertEquals(1, x.size()) ;
        x.remove("A") ;
        assertEquals(0, x.size()) ;
        x.remove("A") ;
        assertEquals(0, x.size()) ;
    }
    
    @Test public void multiSet_11()
    {
        String[] data = { "A", "A" } ;
        MultiSet<String> x = add(data) ;
        long c = Iter.count(x.elements()) ;
        assertEquals(1, c) ;
    }
    
    private static MultiSet<String> add(String[] data)
    {
        MultiSet<String> x = new MultiSet<>() ;
        for ( String str : data )
            x.add(str) ;
        return x ;
    }
    
    private static void iterTest(String[] data)
    {
        List<String> expected = Arrays.asList(data) ;
        MultiSet<String> x = new MultiSet<>() ;
        for ( String str : data )
            x.add(str) ;
        List<String> actual = Iter.toList(x.iterator()) ;
        Collections.sort(expected) ;
        Collections.sort(actual) ;
        assertEquals(expected, actual) ;
    }
    
}
