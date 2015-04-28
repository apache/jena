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
import org.apache.jena.atlas.iterator.PushbackIterator ;
import org.apache.jena.atlas.junit.BaseTest ;
import org.junit.Test ;

public class TestIteratorPushback extends BaseTest
{

    static List<String> data = new ArrayList<>() ;
    static {
        data.add("a") ;
        data.add("b") ;
        data.add("c") ;
    }

    @Test(expected=IllegalArgumentException.class)
    public void pushback01() { new PushbackIterator<String>(null) ; }
    
    @Test public void pushback02()
    { 
        PushbackIterator<String> iter = new PushbackIterator<>(data.iterator()) ;
        assertEquals("a", iter.next()) ;
        assertEquals("b", iter.next()) ;
        assertEquals("c", iter.next()) ;
        assertFalse(iter.hasNext()) ;
    }
    
    @Test public void pushback03()
    { 
        PushbackIterator<String> iter = new PushbackIterator<>(data.iterator()) ;
        iter.pushback("x") ;
        assertEquals("x", iter.next()) ;
        assertEquals("a", iter.next()) ;
        assertEquals(2, Iter.count(iter)) ;
    }
    
    @Test public void pushback04()
    { 
        PushbackIterator<String> iter = new PushbackIterator<>(data.iterator()) ;
        assertEquals("a", iter.next()) ;
        iter.pushback("x") ;
        assertEquals("x", iter.next()) ;
        assertEquals("b", iter.next()) ;
        assertEquals(1, Iter.count(iter)) ;
    }
    
    @Test public void pushback05()
    { 
        PushbackIterator<String> iter = new PushbackIterator<>(data.iterator()) ;
        assertEquals("a", iter.next()) ;
        iter.pushback("x") ;
        iter.pushback("y") ;
        assertEquals("y", iter.next()) ;
        assertEquals("x", iter.next()) ;
        assertEquals("b", iter.next()) ;
        assertEquals(1, Iter.count(iter)) ;
    }
    
    @Test public void pushback06()
    { 
        PushbackIterator<String> iter = new PushbackIterator<>(data.iterator()) ;
        assertEquals(3, Iter.count(iter)) ;
        iter.pushback("x") ;
        iter.pushback("y") ;
        assertEquals("y", iter.next()) ;
        assertEquals("x", iter.next()) ;
        assertFalse(iter.hasNext()) ;
    }

}
