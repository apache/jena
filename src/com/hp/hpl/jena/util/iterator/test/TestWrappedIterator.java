/*
  (c) Copyright 2002, 2004, 2005 Hewlett-Packard Development Company, LP
  [See end of file]
  $Id: TestWrappedIterator.java,v 1.6 2005-02-21 12:19:20 andy_seaborne Exp $
*/

package com.hp.hpl.jena.util.iterator.test;

/**
    test the WrappedIterator class. TODO: test _remove_, which means having
    some fake base iterator to do the checking, and _close_, ditto.
*/

import com.hp.hpl.jena.graph.Triple;
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
        Iterator i = Arrays.asList( new String [] {"bill", "and", "ben"} ).iterator();
        ExtendedIterator e = WrappedIterator.create( i );
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
        ExtendedIterator i = graphWith( "a R b" ).find( Triple.ANY );
        assertSame( i, WrappedIterator.create( i ) );
        }
    
    public void testWrappedNoRemove()
        {
        Iterator base = nodeSet( "a b c" ).iterator();
        base.next();
        base.remove();
        ExtendedIterator wrapped = WrappedIterator.createNoRemove( base );
        wrapped.next();
        try { wrapped.remove(); fail( "wrapped-no-remove iterator should deny .remove()" ); }
        catch (UnsupportedOperationException e) { pass(); }
        }
    }

/*
    (c) Copyright 2002, 2004, 2005 Hewlett-Packard Development Company, LP
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
