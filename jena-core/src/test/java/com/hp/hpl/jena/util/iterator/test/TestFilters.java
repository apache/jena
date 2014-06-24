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

import java.util.*;

import junit.framework.TestSuite;

import com.hp.hpl.jena.rdf.model.test.ModelTestBase;
import com.hp.hpl.jena.shared.JenaException;
import com.hp.hpl.jena.util.iterator.*;

public class TestFilters extends ModelTestBase
    {
    public TestFilters( String name )
        { super( name ); }
    
    public static TestSuite suite()
        { return new TestSuite( TestFilters.class ); }
    
    public void testFilterAnyExists()
        { assertInstanceOf( Filter.class, Filter.any() ); }
    
    public void testFilterAnyAcceptsThings()
        {
        assertTrue( Filter.any().accept( "hello" ) );
        assertTrue( Filter.any().accept( 17 ) );
        assertTrue( Filter.any().accept( node( "frodo" ) ) );
        assertTrue( Filter.any().accept( node( "_cheshire" ) ) );
        assertTrue( Filter.any().accept( node( "17" ) ) );
        assertTrue( Filter.any().accept( triple( "s p o" ) ) );
        assertTrue( Filter.any().accept( Filter.any() ) );
        assertTrue( Filter.any().accept( this ) );
        }
    
    public void testFilterFilterMethod()
        {
        assertFalse( Filter.any().filterKeep( NullIterator.instance() ).hasNext() );
        }
    
    public void testFilteringThings()
        {
        ExtendedIterator<String> it = iteratorOfStrings( "gab geb bag big lava hall end" );
        Filter<String> f = new Filter<String>() 
            {
            @Override public boolean accept( String o )
                { return o.charAt( 1 ) == 'a'; }
            };
        assertEquals( listOfStrings( "gab bag lava hall" ), iteratorToList( f.filterKeep( it ) ) );
        }
    
    public void testAnyFilterSimple()
        {
        ExtendedIterator<String> it = iteratorOfStrings( "" );
        assertSame( it, Filter.<String>any().filterKeep( it ) );
        }

    protected Filter<String> containsA = new Filter<String>() 
        { @Override public boolean accept( String o ) { return contains( o, 'a' ); } };
    
    public void testFilterAnd()
        {
        Filter<String> containsB = new Filter<String>() 
            { @Override public boolean accept( String o ) { return contains( o, 'b' ); } };
        Filter<String> f12 = containsA.and( containsB );
        assertFalse( f12.accept( "a" ) );
        assertFalse( f12.accept( "b" ) );
        assertTrue( f12.accept( "ab" ) );
        assertTrue( f12.accept( "xyzapqrbijk" ) );
        assertTrue( f12.accept( "ba" ) );
        }
    
    public void testFilterShortcircuit()
        {
        Filter<String> oops = new Filter<String>() 
            { @Override public boolean accept( String o ) { throw new JenaException( "oops" ); } };
        Filter<String> f12 = containsA.and( oops );
        assertFalse( f12.accept( "z" ) );
        try { f12.accept( "a" ); fail( "oops" ); }
        catch (JenaException e) { assertEquals( "oops", e.getMessage() ); }
        }
    
    public void testAnyAndTrivial()
        { assertSame( containsA, Filter.<String>any().and( containsA ) ); }
    
    public void testSomethingAndAny()
        { assertSame( containsA, containsA.and( Filter.<String>any() ) ); }
    
    public void testFilterDropIterator()
        {
        Iterator<String> i = iteratorOfStrings( "there's an a in some animals" );
        Iterator<String> it = new FilterDropIterator<>( containsA, i );
        assertEquals( listOfStrings( "there's in some" ), iteratorToList( it ) );
        }
    
    public void testFilterKeepIterator()
        {
        Iterator<String> i = iteratorOfStrings( "there's an a in some animals" );
        Iterator<String> it = new FilterKeepIterator<>( containsA, i );
        assertEquals( listOfStrings( "an a animals" ), iteratorToList( it ) );
        }
    
    protected boolean contains( Object o, char ch )
        { return o.toString().indexOf( ch ) > -1; }
    }
