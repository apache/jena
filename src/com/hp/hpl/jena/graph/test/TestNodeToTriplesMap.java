/*
  (c) Copyright 2004, Hewlett-Packard Development Company, LP, all rights reserved.
  [See end of file]
  $Id: TestNodeToTriplesMap.java,v 1.1 2004-07-07 15:42:26 chris-dollin Exp $
*/
package com.hp.hpl.jena.graph.test;

import java.util.*;

import com.hp.hpl.jena.graph.*;
import com.hp.hpl.jena.mem.NodeToTriplesMap;

import junit.framework.TestSuite;

/**
 	TestNodeToTriplesMap: added, post-hoc, by kers once NTM got
 	rather complicated. So these tests may be (are, at the moment)
 	incomplete.
 	
 	@author kers
*/
public class TestNodeToTriplesMap extends GraphTestBase
    {
    public TestNodeToTriplesMap( String name )
        { super( name ); }
    
    public static TestSuite suite()
        { return new TestSuite( TestNodeToTriplesMap.class ); }
    
    protected NodeToTriplesMap nt = new NodeToTriplesMap();

    protected static final Node x = node( "x" );
    
    protected static final Node y= node( "y" );
    
    public void testZeroSize()
        {
        testZeroSize( "fresh NTM", nt );
        }
    
    protected void testZeroSize( String title, NodeToTriplesMap nt )
        {
        assertEquals( title + " should have size 0", 0, nt.size() );
        assertEquals( title + " should be isEmpty()", true, nt.isEmpty() );
        assertEquals( title + " should have empty domain", false, nt.domain().hasNext() );
        }
    
    public void testAddOne()
        {
        nt.add( x, triple( "x P y" ) );
        testJustOne( x, nt );
        }
    
    public void testAddOneTwice()
        {
        nt.add( x, triple( "x P y" ) );
        nt.add( x, triple( "x P y" ) );
        testJustOne( x, nt );
        }
    
    protected void testJustOne( Node x, NodeToTriplesMap nt )
        {
        assertEquals( 1, nt.size() );
        assertEquals( false, nt.isEmpty() );
        assertEquals( just( x ), iteratorToSet( nt.domain() ) );
        }
    
    public void testAddTwoUnshared()
        {
        nt.add( node( "x" ), triple( "x P a" ) );
        nt.add( node( "y" ), triple( "y Q b" ) );
        assertEquals( 2, nt.size() );
        assertEquals( false, nt.isEmpty() );
        assertEquals( both( x, y ), iteratorToSet( nt.domain() ) );
        }
    
    public void testAddTwoShared()
        {
        nt.add( x, triple( "x P a" ) );
        nt.add( x, triple( "x Q b" ) );
        assertEquals( 2, nt.size() );
        assertEquals( false, nt.isEmpty() );
        assertEquals( just( x ), iteratorToSet( nt.domain() ) );
        }
    
    public void testClear()
        {
        nt.add( x, triple( "x P a" ) );
        nt.add( x, triple( "x Q b" ) );
        nt.add( y, triple( "y R z" ) );
        nt.clear();
        testZeroSize( "cleared NTM", nt );
        }
    
    public void testAllIterator()
        {
        Set triples = tripleSet( "x P b; y P d; y P f" );
        nt.add( x, triple( "x P b" ) );
        nt.add( y, triple( "y P d" ) );
        nt.add( y, triple( "y P f " ) );
        assertEquals( triples, iteratorToSet( nt.iterator() ) );
        }
    
    public void testOneIterator()
        {
        nt.add( x, triple( "x P b" ) );
        nt.add( y, triple( "y P d" ) );
        nt.add( y, triple( "y P f " ) );
        assertEquals( tripleSet( "x P b" ), iteratorToSet( nt.iterator( x ) ) );
        assertEquals( tripleSet( "y P d; y P f" ), iteratorToSet( nt.iterator( y ) ) );
        }
    
    public void testRemove()
        {
        nt.add( x, triple( "x P b" ) );
        nt.add( y, triple( "y P d" ) );
        nt.add( y, triple( "y R f " ) );
        nt.remove( y, triple( "y P d" ) );
        assertEquals( 2, nt.size() );
        assertEquals( tripleSet( "x P b; y R f" ), iteratorToSet( nt.iterator() ) );
        }
    
    public void testRemoveByIterator()
        {
        nt.add( x, triple( "x nice a" ) );
        nt.add( x, triple( "x nasty b" ) );
        nt.add( x, triple( "x nice c" ) );
        nt.add( y, triple( "y nice d" ) );
        nt.add( y, triple( "y nasty e" ) );
        nt.add( y, triple( "y nice f" ) );
        Iterator it = nt.iterator();
        while (it.hasNext())
            {
            Triple t = (Triple) it.next();
            if (t.getPredicate().equals( node( "nasty") )) it.remove();
            }
        assertEquals( tripleSet( "x nice a; x nice c; y nice d; y nice f" ), iteratorToSet( nt.iterator() ) );
        }
    
    // TODO more here
    
    protected static Set just( Object x )
        {
        Set result = new HashSet();
        result.add( x );
        return result;
        }

    protected static Set both( Object x, Object y )
        {
        Set result = just( x );
        result.add( y );
        return result;
        }
    
    }


/*
	(c) Copyright 2004, Hewlett-Packard Development Company, LP
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