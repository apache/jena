/*
 	(c) Copyright 2005 Hewlett-Packard Development Company, LP
 	All rights reserved - see end of file.
 	$Id: TestFilters.java,v 1.4 2005-07-04 13:18:19 chris-dollin Exp $
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
        { assertTrue( Filter.any instanceof Filter ); }
    
    public void testFilterAnyAcceptsThings()
        {
        assertTrue( Filter.any.accept( "hello" ) );
        assertTrue( Filter.any.accept( new Integer( 17 ) ) );
        assertTrue( Filter.any.accept( node( "frodo" ) ) );
        assertTrue( Filter.any.accept( node( "_cheshire" ) ) );
        assertTrue( Filter.any.accept( node( "17" ) ) );
        assertTrue( Filter.any.accept( triple( "s p o" ) ) );
        assertTrue( Filter.any.accept( Filter.any ) );
        assertTrue( Filter.any.accept( this ) );
        }
    
    public void testFilterFilterMethod()
        {
        ExtendedIterator it = Filter.any.filterKeep( NullIterator.instance );
        assertFalse( it.hasNext() );
        }
    
    public void testFilteringThings()
        {
        ExtendedIterator it = iteratorOfStrings( "gab geb bag big lava hall end" );
        Filter f = new Filter() 
            {
            public boolean accept( Object o )
                { return ((String) o).charAt( 1 ) == 'a'; }
            };
        assertEquals( listOfStrings( "gab bag lava hall" ), iteratorToList( f.filterKeep( it ) ) );
        }
    
    public void testAnyFilterSimple()
        {
        ExtendedIterator it = iteratorOfStrings( "" );
        assertSame( it, Filter.any.filterKeep( it ) );
        }

    protected Filter containsA = new Filter() 
        { public boolean accept( Object o ) { return contains( o, 'a' ); } };
    
    public void testFilterAnd()
        {
        Filter containsB = new Filter() 
            { public boolean accept( Object o ) { return contains( o, 'b' ); } };
        Filter f12 = containsA.and( containsB );
        assertFalse( f12.accept( "a" ) );
        assertFalse( f12.accept( "b" ) );
        assertTrue( f12.accept( "ab" ) );
        assertTrue( f12.accept( "xyzapqrbijk" ) );
        assertTrue( f12.accept( "ba" ) );
        }
    
    public void testFilterShortcircuit()
        {
        Filter oops = new Filter() 
            { public boolean accept( Object o ) { throw new JenaException( "oops" ); } };
        Filter f12 = containsA.and( oops );
        assertFalse( f12.accept( "z" ) );
        try { f12.accept( "a" ); fail( "oops" ); }
        catch (JenaException e) { assertEquals( "oops", e.getMessage() ); }
        }
    
    public void testAnyAndTrivial()
        { assertSame( containsA, Filter.any.and( containsA ) ); }
    
    public void testSomethingAndAny()
        { assertSame( containsA, containsA.and( Filter.any ) ); }
    
    public void testFilterDropIterator()
        {
        Iterator i = iteratorOfStrings( "there's an a in some animals" );
        Iterator it = new FilterDropIterator( containsA, i );
        assertEquals( listOfStrings( "there's in some" ), iteratorToList( it ) );
        }
    
    public void testFilterKeepIterator()
        {
        Iterator i = iteratorOfStrings( "there's an a in some animals" );
        Iterator it = new FilterKeepIterator( containsA, i );
        assertEquals( listOfStrings( "an a animals" ), iteratorToList( it ) );
        }
    
    protected boolean contains( Object o, char ch )
        { return o.toString().indexOf( ch ) > -1; }
    }


/*
 * (c) Copyright 2005 Hewlett-Packard Development Company, LP
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