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

import java.util.Iterator ;
import java.util.NoSuchElementException ;

import org.apache.jena.atlas.iterator.IteratorArray ;
import org.apache.jena.atlas.junit.BaseTest ;
import org.junit.Test ;

public class TestIteratorArray extends BaseTest
{
    IteratorArray<String> create(String ... a)
    {
        return IteratorArray.create(a) ;
    }
    
    IteratorArray<String> create(int start, int finish, String ... a)
    {
        return IteratorArray.create(a, start, finish) ;
    }
    
    @Test public void arrayIterator_1()
    {
        Iterator<String> iter = create() ;
        assertFalse(iter.hasNext()) ;
        assertFalse(iter.hasNext()) ;
    }

    @Test public void arrayIterator_2()
    {
        Iterator<String> iter = create("a") ;
        assertTrue(iter.hasNext()) ;
        assertEquals("a", iter.next()) ;
        assertFalse(iter.hasNext()) ;
        assertFalse(iter.hasNext()) ;
    }

    
    @Test public void arrayIterator_3()
    {
        Iterator<String> iter = create("a", "b", "c") ;
        assertTrue(iter.hasNext()) ;
        assertEquals("a", iter.next()) ;
        assertTrue(iter.hasNext()) ;
        assertEquals("b", iter.next()) ;
        assertTrue(iter.hasNext()) ;
        assertEquals("c", iter.next()) ;
        assertFalse(iter.hasNext()) ;
        assertFalse(iter.hasNext()) ;
    }
    
    @Test public void arrayIterator_4()
    {
        Iterator<String> iter = create("a") ;
        assertEquals("a", iter.next()) ;
        try { iter.next() ; fail("Expected NoSuchElementException") ; }
        catch (NoSuchElementException ex) {}
    }
    
    @Test public void arrayIterator_5()
    {
        Iterator<String> iter = create(0,1, "a", "b", "c") ;
        assertEquals("a", iter.next()) ;
        assertFalse(iter.hasNext()) ;
    }
    
    @Test public void arrayIterator_6()
    {
        Iterator<String> iter = create(1, 3, "a", "b", "c", "d") ;
        assertEquals("b", iter.next()) ;
        assertEquals("c", iter.next()) ;
        assertFalse(iter.hasNext()) ;
    }
    
    @Test public void arrayIterator_7()
    {
        IteratorArray<String> iter = create(1, 3, "a", "b", "c", "d") ;
        assertEquals("b", iter.current()) ;
        assertEquals("b", iter.current()) ;
        assertEquals("b", iter.next()) ;
        assertEquals("c", iter.current()) ;
        assertEquals("c", iter.next()) ;
        assertFalse(iter.hasNext()) ;
    }

}
