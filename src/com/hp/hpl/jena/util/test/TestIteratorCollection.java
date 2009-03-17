/*
  (c) Copyright 2004, 2005, 2006, 2007, 2008, 2009 Hewlett-Packard Development Company, LP
  [See end of file]
  $Id: TestIteratorCollection.java,v 1.9 2009-03-17 08:38:00 chris-dollin Exp $
*/

package com.hp.hpl.jena.util.test;

import java.util.*;

import junit.framework.TestSuite;

import com.hp.hpl.jena.graph.test.GraphTestBase;
import com.hp.hpl.jena.util.*;
import com.hp.hpl.jena.util.iterator.*;

/**
 @author hedgehog
 */
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
        assertEquals( oneSet( "single" ), iteratorToSet( new SingletonIterator<String>( "single" ) ) );
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
        assertEquals( new ArrayList<Object>(), IteratorCollection.iteratorToList( NullIterator.instance() ) );
        }
    
    public void testSingletonToSingletonList()
        {
        assertEquals( oneList( "just one" ), IteratorCollection.iteratorToList( new SingletonIterator<String>( "just one" ) ) );
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
        Set<Object> result = new HashSet<Object>();
        result.add( x );
        return result;
        }
    
    protected Set<Object> setLots( Object [] elements )
        {
        Set<Object> result = new HashSet<Object>();
        for (int i = 0; i < elements.length; i += 1) result.add( elements[i] );
        return result;
        }
    
    protected List<Object> oneList( Object x )
        {
        List<Object> result = new ArrayList<Object>();
        result.add( x );
        return result;
        }
    }

/*
    (c) Copyright 2004, 2005, 2006, 2007, 2008, 2009 Hewlett-Packard Development Company, LP
    All rights reserved.

    Redistribution and use in source and binary forms, with or without
    modification, are permitted provided that the following conditions
    are met:

    1. Redistributions of source code must retain the above copyright
       notice, this list of conditions and the following disclaimer.

    2. Redistributions in binary form must reproduce the above copyright
       notice, this list of conditions and the following disclaimer in the
       documentation and/or other materials provided with the distribution.

    3. The name of the author may not be used to endorse or promote products
       derived from this software without specific prior written permission.

    THIS SOFTWARE IS PROVIDED BY THE AUTHOR ``AS IS'' AND ANY EXPRESS OR
    IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
    OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.
    IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR ANY DIRECT, INDIRECT,
    INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT
    NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
    DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
    THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
    (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF
    THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
*/