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

package com.hp.hpl.jena.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import junit.framework.TestSuite;

import com.hp.hpl.jena.graph.test.GraphTestBase;
import com.hp.hpl.jena.util.iterator.NullIterator;
import com.hp.hpl.jena.util.iterator.SingletonIterator;
import com.hp.hpl.jena.util.iterator.WrappedIterator;

public class TestIteratorCollection extends GraphTestBase
    {
    public TestIteratorCollection( String name )
        { super( name ); }

    public static TestSuite suite()
        { return new TestSuite( TestIteratorCollection.class ); }
    
    public void testEmptyToEmptySet()
        {
        assertEquals( CollectionFactory.createHashedSet(), IteratorCollection.iteratorToSet( NullIterator.instance() ) );
        }
    
    public void testSingletonToSingleSet()
        {
        assertEquals( oneSet( "single" ), iteratorToSet( new SingletonIterator<>( "single" ) ) );
        }
    
    public void testLotsToSet()
        {
        Object [] elements = new Object[] {"now", "is", "the", "time"};
        Iterator<Object> it = Arrays.asList( elements ).iterator();
        assertEquals( setLots( elements ), IteratorCollection.iteratorToSet( it ) );
        }
    
    public void testCloseForSet()
        {
        testCloseForSet( new Object[] {} );
        testCloseForSet( new Object[] {"one"} );
        testCloseForSet( new Object[] {"to", "free", "for"} );
        testCloseForSet( new Object[] {"another", "one", "plus", Boolean.FALSE} );
        testCloseForSet( new Object[] {"the", "king", "is", "in", "his", "counting", "house"} );
        }
    
    protected void testCloseForSet( Object[] objects )
        {
        final boolean [] closed = {false};
        Iterator<Object> iterator = new WrappedIterator<Object>( Arrays.asList( objects ).iterator() ) 
            { @Override public void close() { super.close(); closed[0] = true; } };
        iteratorToSet( iterator );
        assertTrue( closed[0] );
        }

    public void testEmptyToEmptyList()
        {
        assertEquals( new ArrayList<>(), IteratorCollection.iteratorToList( NullIterator.instance() ) );
        }
    
    public void testSingletonToSingletonList()
        {
        assertEquals( oneList( "just one" ), IteratorCollection.iteratorToList( new SingletonIterator<>( "just one" ) ) );
        }
    
    public void testLotsToList()
        {
        List<Object> list = Arrays.asList( new Object[] {"to", "be", "or", "not", "to", "be"}  );
        assertEquals( list, IteratorCollection.iteratorToList( list.iterator() ) );
        }
        
    public void testCloseForList()
        {
        testCloseForList( new Object[] {} );
        testCloseForList( new Object[] {"one"} );
        testCloseForList( new Object[] {"to", "free", "for"} );
        testCloseForList( new Object[] {"another", "one", "plus", Boolean.FALSE} );
        testCloseForList( new Object[] {"the", "king", "is", "in", "his", "counting", "house"} );
        }
    
    protected void testCloseForList( Object[] objects )
        {
        final boolean [] closed = {false};
        Iterator<Object> iterator = new WrappedIterator<Object>( Arrays.asList( objects ).iterator() ) 
            { @Override public void close() { super.close(); closed[0] = true; } };
        iteratorToList( iterator );
        assertTrue( closed[0] );
        }

    protected Set<Object> oneSet( Object x )
        {
        Set<Object> result = new HashSet<>();
        result.add( x );
        return result;
        }
    
    protected Set<Object> setLots( Object [] elements )
        {
        Set<Object> result = new HashSet<>();
            for ( Object element : elements )
            {
                result.add( element );
            }
        return result;
        }
    
    protected List<Object> oneList( Object x )
        {
        List<Object> result = new ArrayList<>();
        result.add( x );
        return result;
        }
    }
