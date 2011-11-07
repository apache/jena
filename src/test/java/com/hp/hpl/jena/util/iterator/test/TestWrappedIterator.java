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

package com.hp.hpl.jena.util.iterator.test;

/**
    test the WrappedIterator class. TODO: test _remove_, which means having
    some fake base iterator to do the checking, and _close_, ditto.
*/

import com.hp.hpl.jena.graph.*;
import com.hp.hpl.jena.graph.test.GraphTestBase;
import com.hp.hpl.jena.util.iterator.*;
import java.util.*;
import junit.framework.*;

public class TestWrappedIterator extends GraphTestBase
    {
    public static TestSuite suite()
        { return new TestSuite( TestWrappedIterator.class ); }   
            
    public TestWrappedIterator(String name)
        { super(name); }

    public void testWrappedIterator()
        {
        Iterator<String> i = Arrays.asList( new String [] {"bill", "and", "ben"} ).iterator();
        ExtendedIterator<String> e = WrappedIterator.create( i );
        assertTrue( "wrapper has at least one element", e.hasNext() );
        assertEquals( "", "bill", e.next() );
        assertTrue( "wrapper has at least two elements", e.hasNext() );
        assertEquals( "", "and", e.next() );
        assertTrue( "wrapper has at least three elements", e.hasNext() );
        assertEquals( "", "ben", e.next() );
        assertFalse( "wrapper is now empty", e.hasNext() );
        }
    
    public void testUnwrapExtendedIterator()
        {
        ExtendedIterator<Triple> i = graphWith( "a R b" ).find( Triple.ANY );
        assertSame( i, WrappedIterator.create( i ) );
        }
    
    public void testWrappedNoRemove()
        {
        Iterator<Node> base = nodeSet( "a b c" ).iterator();
        base.next();
        base.remove();
        ExtendedIterator<Node> wrapped = WrappedIterator.createNoRemove( base );
        wrapped.next();
        try { wrapped.remove(); fail( "wrapped-no-remove iterator should deny .remove()" ); }
        catch (UnsupportedOperationException e) { pass(); }
        }
    }
