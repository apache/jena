/*
 	(c) Copyright 2005, 2006, 2007, 2008, 2009 Hewlett-Packard Development Company, LP
 	All rights reserved - see end of file.
 	$Id: TestFilters.java,v 1.1 2009-06-29 08:55:59 castagna Exp $
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
        assertTrue( Filter.any().accept( new Integer( 17 ) ) );
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
        Iterator<String> it = new FilterDropIterator<String>( containsA, i );
        assertEquals( listOfStrings( "there's in some" ), iteratorToList( it ) );
        }
    
    public void testFilterKeepIterator()
        {
        Iterator<String> i = iteratorOfStrings( "there's an a in some animals" );
        Iterator<String> it = new FilterKeepIterator<String>( containsA, i );
        assertEquals( listOfStrings( "an a animals" ), iteratorToList( it ) );
        }
    
    protected boolean contains( Object o, char ch )
        { return o.toString().indexOf( ch ) > -1; }
    }


/*
 * (c) Copyright 2005, 2006, 2007, 2008, 2009 Hewlett-Packard Development Company, LP
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in the
 *    documentation and/or other materials provided with the distribution.
 * 3. The name of the author may not be used to endorse or promote products
 *    derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE AUTHOR ``AS IS'' AND ANY EXPRESS OR
 * IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.
 * IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR ANY DIRECT, INDIRECT,
 * INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT
 * NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 * THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF
 * THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
*/