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

package org.apache.jena.atlas.iterator;

import java.util.Arrays ;
import java.util.List ;

import org.apache.jena.atlas.iterator.IteratorWithHistory ;
import org.apache.jena.atlas.junit.BaseTest ;
import org.junit.Test ;

public class TestIteratorWithHistory extends BaseTest
{
    @Test public void iterHistory_01()
    {
        IteratorWithHistory<String> iter = createHistory(1, "a", "b", "c") ;
        assertEquals(0, iter.currentSize()) ;
        assertEquals(null, iter.getPrevious(0)) ;
    }
    
    @Test public void iterHistory_02()
    {
        IteratorWithHistory<String> iter = createHistory(1, "a", "b", "c") ;
        assertEquals("a", iter.next()) ;
        assertEquals(1, iter.currentSize()) ;
    }

    @Test public void iterHistory_03()
    {
        IteratorWithHistory<String> iter = createHistory(2, "a", "b", "c") ;
        assertEquals("a", iter.next()) ;
        assertEquals("b", iter.next()) ;
        assertEquals(2, iter.currentSize()) ;
        assertEquals("b", iter.getPrevious(0)) ;
        assertEquals("a", iter.getPrevious(1)) ;
    }

    @Test(expected=IndexOutOfBoundsException.class)
    public void iterHistory_04()
    {
        IteratorWithHistory<String> iter = createHistory(2, "a", "b", "c") ;
        iter.getPrevious(2) ;
    }
    
    @Test
    public void iterHistory_05()
    {
        IteratorWithHistory<String> iter = createHistory(2, "a", "b", "c") ;
        assertEquals("a", iter.next()) ;
        assertEquals("a", iter.getPrevious(0)) ;
        assertEquals(1, iter.currentSize()) ;
        
        assertEquals("b", iter.next()) ;
        assertEquals("b", iter.getPrevious(0)) ;
        assertEquals("a", iter.getPrevious(1)) ;
        assertEquals(2, iter.currentSize()) ;
        
        assertEquals("c", iter.next()) ;
        assertEquals(2, iter.currentSize()) ;
        assertEquals("c", iter.getPrevious(0)) ;
        assertEquals("b", iter.getPrevious(1)) ;
    }

    private IteratorWithHistory<String> createHistory(int N, String... strings)
    {
        List<String> data = Arrays.asList(strings) ;
        IteratorWithHistory<String> iter = new IteratorWithHistory<>(data.iterator(), N) ;
        return iter ;
    }

}
