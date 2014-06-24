/**
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

package org.apache.jena.riot.lang;

import java.util.ArrayList ;
import java.util.List ;

import org.apache.jena.atlas.junit.BaseTest ;
import org.junit.Test ;
import org.junit.runner.RunWith ;
import org.junit.runners.Parameterized ;
import org.junit.runners.Parameterized.Parameters ;

import com.hp.hpl.jena.graph.Node ;

@RunWith(Parameterized.class)
public class TestBlankNodeAllocator extends BaseTest
{
    public interface Factory { public BlankNodeAllocator create() ; }
    
    @Parameters(name = "{index}: {0}")
    public static Iterable<Object[]> data() {
        List<Object[]> x = new ArrayList<>() ;
        Factory fSeededHashAlloc = new Factory() {
            @Override public BlankNodeAllocator create() { return new BlankNodeAllocatorHash() ; }
            @Override public String toString() { return "SeededHash" ; }
        } ;

        Factory fUIDAlloc = new Factory() {
            @Override public BlankNodeAllocator create() { return new BlankNodeAllocatorTraditional() ; }
            @Override public String toString() { return "UID" ; }
        } ;

        x.add(new Object[]{fSeededHashAlloc}) ;
        x.add(new Object[]{fUIDAlloc}) ;
        return x ; 
    }

    private Factory factory ;
    
    public TestBlankNodeAllocator(Factory factory) { this.factory = factory ; }
    
    @Test public void alloc_01()
    {
        BlankNodeAllocator alloc = factory.create() ;
        Node n = alloc.create() ;
        assertTrue(n.isBlank()) ;
    }
    
    @Test public void alloc_02()
    {
        BlankNodeAllocator alloc = factory.create() ;
        Node n1 = alloc.create() ;
        Node n2 = alloc.create() ;
        assertNotEquals(n1, n2) ;
    }
    
    @Test public void alloc_03()
    {
        BlankNodeAllocator alloc = factory.create() ;
        Node n1 = alloc.alloc("foo") ;
        Node n2 = alloc.alloc("foo") ;
        assertEquals(n1, n2) ;
    }
    
    @Test public void alloc_04()
    {
        BlankNodeAllocator alloc = factory.create() ;
        Node n1 = alloc.alloc("foo") ;
        Node n2 = alloc.alloc("bar") ;
        assertNotEquals(n1, n2) ;
    }
}

