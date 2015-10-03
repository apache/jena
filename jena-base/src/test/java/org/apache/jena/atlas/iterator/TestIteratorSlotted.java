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
import java.util.Collection ;
import java.util.Iterator ;
import java.util.List ;

import org.apache.jena.atlas.junit.BaseTest ;
import org.junit.Test ;
import org.junit.runner.RunWith ;
import org.junit.runners.Parameterized ;
import org.junit.runners.Parameterized.Parameters ;

@RunWith(Parameterized.class)

public class TestIteratorSlotted extends BaseTest
{
    @Parameters(name = "{index}: {0}")
    public static Collection<Object[]> implementations() {
        IterFactory factory1 = new IterFactory() {
            @Override
            public IteratorSlotted<String> create(String... array) {
                return new IterStr1(array) ;
            }} ;  
        IterFactory factory2 = new IterFactory() {
                @Override
                public IteratorSlotted<String> create(String... array) {
                    return new IterStr2(array) ;
                }} ;  
        return Arrays.asList(new Object[][] { {"hasMore accurate", factory1}, {"hasMore always true", factory2} }) ;
    }
    
    /** Accurate hasMore */
    static class IterStr1 extends IteratorSlotted<String>
    {
        private List<String>     array ;
        private Iterator<String> iter ;

        IterStr1(String... array) {
            this.array = Arrays.asList(array) ;
            iter = this.array.iterator() ;
        }

        @Override
        protected String moveToNext() {
            return iter.next() ;
        }

        @Override
        protected boolean hasMore() {
            return iter.hasNext() ;
        }
    }
    
    /** hasMore ios always true, returns null in moveToNext */
    static class IterStr2 extends IteratorSlotted<String>
    {
        private List<String>     array ;
        private Iterator<String> iter ;

        IterStr2(String... array) {
            this.array = Arrays.asList(array) ;
            iter = this.array.iterator() ;
        }

        @Override
        protected String moveToNext() {
            if ( !iter.hasNext() )
                return null ;
            return iter.next() ;
        }

        @Override
        protected boolean hasMore() {
            return true ;
        }
    }
    

    interface IterFactory { IteratorSlotted<String> create(String...array) ; }
    

    private IterFactory factory ;
    
    public TestIteratorSlotted(String name, IterFactory factory) {
        this.factory = factory ;
    }
    
    @Test public void iter_01()
    {
        IteratorSlotted<String> iter = factory.create() ;
        assertFalse(iter.hasNext()) ;
    }
    
    @Test public void iter_02()
    {
        IteratorSlotted<String> iter = factory.create("A") ;
        assertTrue(iter.hasNext()) ;
        assertEquals("A", iter.peek()) ;
        assertEquals("A", iter.peek()) ;
        assertEquals("A", iter.next()) ;
        assertFalse(iter.hasNext()) ;
        assertNull(iter.peek()) ;
    }
    
    @Test public void iter_03()
    {
        IteratorSlotted<String> iter = factory.create("A", "B") ;
        assertTrue(iter.hasNext()) ;
        assertEquals("A", iter.peek()) ;
        assertEquals("A", iter.next()) ;
        assertEquals("B", iter.peek()) ;
        assertEquals("B", iter.next()) ;
        assertFalse(iter.hasNext()) ;
    }
    
    
}
