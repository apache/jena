/*
 	(c) Copyright 2005, 2006, 2007, 2008, 2009 Hewlett-Packard Development Company, LP
 	All rights reserved - see end of file.
 	$Id: TestAndThen.java,v 1.2 2009-09-28 13:27:38 chris-dollin Exp $
*/

package com.hp.hpl.jena.util.iterator.test;

import java.util.List ;

import junit.framework.TestSuite ;

import com.hp.hpl.jena.rdf.model.test.ModelTestBase ;
import com.hp.hpl.jena.util.iterator.ExtendedIterator ;
import com.hp.hpl.jena.util.iterator.NiceIterator ;
import com.hp.hpl.jena.util.iterator.WrappedIterator ;

public class TestAndThen extends ModelTestBase
    {
    public TestAndThen( String name )
        { super( name ); }
    
    public static TestSuite suite()
        { return new TestSuite( TestAndThen.class ); }

    public void testAndThen()
        { 
        ExtendedIterator<String> L = iteratorOfStrings( "a b c" );
        ExtendedIterator<String> R = iteratorOfStrings( "d e f" );
        assertInstanceOf( NiceIterator.class, L );
        assertInstanceOf( NiceIterator.class, R );
        assertEquals( listOfStrings( "a b c d e f" ), iteratorToList( L.andThen( R ) ) );
        }
    
    public void testAndThenExtension()
        {
        ExtendedIterator<String> L = iteratorOfStrings( "a b c" );
        ExtendedIterator<String> R = iteratorOfStrings( "d e f" );
        ExtendedIterator<String> X = iteratorOfStrings( "g h i" );
        ExtendedIterator<String> LR = L.andThen( R );
        ExtendedIterator<String> LRX = LR.andThen( X );
        assertSame( LR, LRX );
        List<String> aToI = listOfStrings( "a b c d e f g h i" );
        assertEquals( aToI, iteratorToList( LRX ) );
        }
    
    public void testClosingConcatenationClosesRemainingIterators()
        {
        LoggingClosableIterator<String> L = new LoggingClosableIterator<String>( iteratorOfStrings( "only" ) );
        LoggingClosableIterator<String> M = new LoggingClosableIterator<String>( iteratorOfStrings( "single" ) );
        LoggingClosableIterator<String> R = new LoggingClosableIterator<String>( iteratorOfStrings( "it" ) );
        ExtendedIterator<String> cat = L.andThen( M ).andThen( R );
        cat.next();
        cat.close();
        assertTrue( "middle iterator should have been closed", M.isClosed() );
        assertTrue( "final iterator should have been closed", R.isClosed() );
        }
    
    public void testRemove1()
    {
        List<String> L = listOfStrings("a b c");
        List<String> R = listOfStrings("d e f");
        
        ExtendedIterator<String> Lit = WrappedIterator.create(L.iterator());
        ExtendedIterator<String> Rit = WrappedIterator.create(R.iterator());
        
        ExtendedIterator<String> LR = Lit.andThen( Rit ) ;
        
        while (LR.hasNext())
        {
            String s = LR.next();
            
            if ("c".equals(s))
            {
                LR.hasNext();  // test for JENA-60
                LR.remove();
            }
        }
        
        assertEquals("ab", concatAsString(L));
        assertEquals("def", concatAsString(R));
    }
    
    public void testRemove2()
    {
        List<String> L = listOfStrings("a b c");
        List<String> R = listOfStrings("d e f");
        
        ExtendedIterator<String> Lit = WrappedIterator.create(L.iterator());
        ExtendedIterator<String> Rit = WrappedIterator.create(R.iterator());
        
        ExtendedIterator<String> LR = Lit.andThen( Rit ) ;
        
        while (LR.hasNext())
        {
            String s = LR.next();
            
            if ("d".equals(s))
            {
                LR.hasNext();  // test for JENA-60
                LR.remove();
            }
        }
        
        assertEquals("abc", concatAsString(L));
        assertEquals("ef", concatAsString(R));
    }
    
    public void testRemove3()
    {
        List<String> L = listOfStrings("a b c");
        List<String> R = listOfStrings("d e f");
        
        ExtendedIterator<String> Lit = WrappedIterator.create(L.iterator());
        ExtendedIterator<String> Rit = WrappedIterator.create(R.iterator());
        
        ExtendedIterator<String> LR = Lit.andThen( Rit ) ;
        
        while (LR.hasNext())
        {
            LR.next();
        }
        LR.remove();
        
        assertEquals("abc", concatAsString(L));
        assertEquals("de", concatAsString(R));
    }
    
    private String concatAsString(List<String> strings)
    {
        String toReturn = "";
        for(String s : strings)
        {
            toReturn += s;
        }
        return toReturn;
    }
    
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