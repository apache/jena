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

import org.apache.jena.atlas.iterator.IteratorWithBuffer ;
import org.apache.jena.atlas.junit.BaseTest ;
import org.junit.Test ;

public class TestIteratorWithBuffer extends BaseTest
{

    @Test public void iterBuffer_01()
    {
        IteratorWithBuffer<String> iter = createBuffered(1, "a", "b", "c") ;
        assertEquals(1, iter.currentSize()) ;
        assertEquals("a", iter.peek(0)) ;
        assertEquals(1, iter.currentSize()) ;
        assertEquals("a", iter.next()) ;
        assertEquals(1, iter.currentSize()) ;
        assertEquals("b", iter.peek(0)) ;
        assertEquals(1, iter.currentSize()) ;
        assertEquals("b", iter.next()) ;
        assertEquals(1, iter.currentSize()) ;
        assertEquals("c", iter.peek(0)) ;
        assertEquals(1, iter.currentSize()) ;
        assertEquals("c", iter.next()) ;
        assertEquals(0, iter.currentSize()) ;
        assertEquals(null, iter.peek(0)) ;
        assertEquals(0, iter.currentSize()) ;
    }

    @Test public void iterBuffer_02()
    {
        IteratorWithBuffer<String> iter = createBuffered(2, "a", "b", "c") ;
        assertEquals(2, iter.currentSize()) ;
        assertEquals("a", iter.peek(0)) ;
        assertEquals("b", iter.peek(1)) ;
        assertEquals("a", iter.next()) ;
        
        assertEquals("b", iter.peek(0)) ;
        assertEquals("c", iter.peek(1)) ;
        assertEquals("b", iter.next()) ;
        
        assertEquals("c", iter.peek(0)) ;
        assertEquals(null, iter.peek(1)) ;
        assertEquals("c", iter.next()) ;
        assertEquals(null, iter.peek(0)) ;
    }

    @Test public void iterBuffer_03()
    {
        IteratorWithBuffer<String> iter = createBuffered(1) ;
        assertEquals(null, iter.peek(0)) ;
    }

    @Test(expected=IndexOutOfBoundsException.class)
    public void iterBuffer_04()
    {
        IteratorWithBuffer<String> iter = createBuffered(0, "a") ;
        assertEquals(null, iter.peek(0)) ;
    }

    @Test public void iterBuffer_05()
    {
        IteratorWithBuffer<String> iter = createBuffered(2, "a") ;
        assertEquals("a", iter.peek(0)) ;
        assertEquals(null, iter.peek(1)) ;
        assertEquals("a", iter.next()) ;
    }

    private IteratorWithBuffer<String> createBuffered(int N, String... strings)
    {
        List<String> data = Arrays.asList(strings) ;
        IteratorWithBuffer<String> iter = new IteratorWithBuffer<>(data.iterator(), N) ;
        return iter ;
    }

}
