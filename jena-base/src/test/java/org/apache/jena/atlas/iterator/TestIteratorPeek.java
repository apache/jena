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

import java.util.ArrayList ;
import java.util.List ;

import org.apache.jena.atlas.iterator.Iter ;
import org.apache.jena.atlas.iterator.IteratorArray ;
import org.apache.jena.atlas.iterator.PeekIterator ;
import org.apache.jena.atlas.junit.BaseTest ;
import org.junit.Test ;

public class TestIteratorPeek extends BaseTest
{
    List<String> data0 = new ArrayList<>() ;
    List<String> data1 = new ArrayList<>() ;
    {
        data1.add("a") ;
    }
    
    List<String> data2 = new ArrayList<>() ;
    {
        data2.add("x") ;
        data2.add("y") ;
        data2.add("z") ;
    }
    
    @Test public void iter_01() 
    {
        Iter<String> iter = Iter.iter(data2) ;
        iter = iter.append(data2.iterator()) ;
        test(iter, "x", "y", "z", "x", "y", "z") ;
    }
    
    private void test(Iter<?> iter, Object... items)
    {
        for ( Object x : items )
        {
            assertTrue(iter.hasNext()) ;
            assertEquals(x, iter.next()) ;
        }
        assertFalse(iter.hasNext()) ;
    }
    
    private static PeekIterator<String> create(String...a)
    { 
        return new PeekIterator<>(IteratorArray.create(a)) ;
    }
    
    @Test public void peek_1()
    {
        PeekIterator<String> peek = create("a", "b", "c") ;
        assertEquals("a", peek.peek()) ;
        test(Iter.iter(peek), "a", "b", "c") ;
    }
    
    @Test public void peek_2()
    {
        PeekIterator<String> peek = create() ;
        assertFalse(peek.hasNext()) ;
    }

    @Test public void peek_3()
    {
        PeekIterator<String> peek = create("a") ;
        assertEquals("a", peek.peek()) ;
    }

    @Test public void peek_4()
    {
        PeekIterator<String> peek = create("a") ;
        assertEquals("a", peek.peek()) ;
        assertEquals("a", peek.peek()) ;
        assertEquals("a", peek.next()) ;
        assertFalse(peek.hasNext()) ;
    }

    @Test public void peek_5()
    {
        PeekIterator<String> peek = create("a", "b") ;
        assertEquals("a", peek.peek()) ;
        assertEquals("a", peek.peek()) ;
        assertEquals("a", peek.next()) ;
        assertTrue(peek.hasNext()) ;
        assertEquals("b", peek.peek()) ;
        assertEquals("b", peek.peek()) ;
        assertEquals("b", peek.next()) ;
        assertFalse(peek.hasNext()) ;
    }
}
