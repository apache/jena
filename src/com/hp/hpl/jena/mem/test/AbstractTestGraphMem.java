/*
 	(c) Copyright 2005, 2006, 2007, 2008 Hewlett-Packard Development Company, LP
 	All rights reserved - see end of file.
 	$Id: AbstractTestGraphMem.java,v 1.2 2008-01-02 12:09:00 andy_seaborne Exp $
*/

package com.hp.hpl.jena.mem.test;

import java.util.Iterator;

import com.hp.hpl.jena.graph.*;
import com.hp.hpl.jena.graph.impl.SimpleReifier;
import com.hp.hpl.jena.graph.test.AbstractTestGraph;
import com.hp.hpl.jena.util.iterator.ExtendedIterator;

public class AbstractTestGraphMem extends AbstractTestGraph
    {
    public AbstractTestGraphMem(String name)
        { super( name ); }
    
    public void testClosesReifier()
        {
        Graph g = getGraph();
        SimpleReifier r = (SimpleReifier) g.getReifier();
        g.close();
        assertTrue( r.isClosed() );
        }
    
    public void testBrokenIndexes()
        {
        Graph g = getGraphWith( "x R y; x S z" );
        ExtendedIterator it = g.find( Node.ANY, Node.ANY, Node.ANY );
        it.removeNext(); it.removeNext();
        assertFalse( g.find( node( "x" ), Node.ANY, Node.ANY ).hasNext() );
        assertFalse( g.find( Node.ANY, node( "R" ), Node.ANY ).hasNext() );
        assertFalse( g.find( Node.ANY, Node.ANY, node( "y" ) ).hasNext() );
        }   
            
    public void testBrokenSubject()
        {
        Graph g = getGraphWith( "x brokenSubject y" );
        ExtendedIterator it = g.find( node( "x" ), Node.ANY, Node.ANY );
        it.removeNext();
        assertFalse( g.find( Node.ANY, Node.ANY, Node.ANY ).hasNext() );
        }
        
    public void testBrokenPredicate()
        {
        Graph g = getGraphWith( "x brokenPredicate y" );
        ExtendedIterator it = g.find( Node.ANY, node( "brokenPredicate"), Node.ANY );
        it.removeNext();
        assertFalse( g.find( Node.ANY, Node.ANY, Node.ANY ).hasNext() );
        }
        
    public void testBrokenObject()
        {
        Graph g = getGraphWith( "x brokenObject y" );
        ExtendedIterator it = g.find( Node.ANY, Node.ANY, node( "y" ) );
        it.removeNext();
        assertFalse( g.find( Node.ANY, Node.ANY, Node.ANY ).hasNext() );
        }
    
    public void testSizeAfterRemove() 
        {
        Graph g = getGraphWith( "x p y" );
        ExtendedIterator it = g.find( triple( "x ?? ??" ) );
        it.removeNext();
        assertEquals( 0, g.size() );        
        }
    
    public void testUnnecessaryMatches() 
        {
        Node special = new Node_URI( "eg:foo" ) 
            {
            public boolean matches( Node s ) 
                {
                fail( "Matched called superfluously." );
                return true;
                }
            };
        Graph g = getGraphWith( "x p y" );
        g.add( new Triple( special, special, special ) );
        exhaust( g.find( special, Node.ANY, Node.ANY ) );
        exhaust( g.find( Node.ANY, special, Node.ANY ) );
        exhaust( g.find( Node.ANY, Node.ANY, special ) );
    }
    
    protected void exhaust( Iterator it )
        { while (it.hasNext()) it.next(); }
    }

/*
 * (c) Copyright 2005, 2006, 2007, 2008 Hewlett-Packard Development Company, LP
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
