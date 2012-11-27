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
import java.util.Iterator ;
import java.util.List ;

import org.apache.jena.atlas.iterator.IteratorSlotted ;
import org.apache.jena.atlas.junit.BaseTest ;
import org.junit.Test ;

public class TestIteratorSlotted extends BaseTest
{
    static class IterStr extends IteratorSlotted<String>
    {
        private List<String> array ;
        private Iterator<String> iter ;

        IterStr(String...array)
        { 
            this.array = Arrays.asList(array) ;
            iter = this.array.iterator() ;
        }
        
        @Override
        protected String moveToNext()
        {
            return iter.next() ;
        }

        @Override
        protected boolean hasMore()
        {
            return iter.hasNext() ;
        }
        
    }
    
    @Test public void iter_01()
    {
        IterStr iter = new IterStr() ;
        assertFalse(iter.hasNext()) ;
    }
    
    @Test public void iter_02()
    {
        IterStr iter = new IterStr("A") ;
        assertTrue(iter.hasNext()) ;
        assertEquals("A", iter.peek()) ;
        assertEquals("A", iter.peek()) ;
        assertEquals("A", iter.next()) ;
        assertFalse(iter.hasNext()) ;
        assertNull(iter.peek()) ;
    }
    
    @Test public void iter_03()
    {
        IterStr iter = new IterStr("A", "B") ;
        assertTrue(iter.hasNext()) ;
        assertEquals("A", iter.peek()) ;
        assertEquals("A", iter.next()) ;
        assertEquals("B", iter.peek()) ;
        assertEquals("B", iter.next()) ;
        assertFalse(iter.hasNext()) ;
    }
    
    
}
