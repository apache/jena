/*
  (c) Copyright 2003, Hewlett-Packard Development Company, LP
  [See end of file]
  $Id: TestGraphMem.java,v 1.5 2004-06-30 17:16:56 chris-dollin Exp $
*/

package com.hp.hpl.jena.mem.test;

import com.hp.hpl.jena.graph.*;
import com.hp.hpl.jena.graph.test.*;
import com.hp.hpl.jena.mem.GraphMem;
import com.hp.hpl.jena.shared.JenaException;
import com.hp.hpl.jena.util.iterator.ExtendedIterator;

import junit.framework.*;

/**
 	@author kers
*/
public class TestGraphMem extends AbstractTestGraph
    {
    public TestGraphMem(String name)
        { super(name); }
    
    public static TestSuite suite()
        { return new TestSuite( TestGraphMem.class ); }
        
    public Graph getGraph()
        { return new GraphMem(); }
        
    public void testBrokenIndexes()
        {
        Graph g = getGraphWith( "x R y; x S z" );
        ExtendedIterator it = g.find( Node.ANY, Node.ANY, Node.ANY );
        it.next(); it.remove(); it.next(); it.remove();
        assertFalse( g.find( node( "x" ), Node.ANY, Node.ANY ).hasNext() );
        assertFalse( g.find( Node.ANY, node( "R" ), Node.ANY ).hasNext() );
        assertFalse( g.find( Node.ANY, Node.ANY, node( "y" ) ).hasNext() );
        }   
            
    public void testBrokenSubject()
        {
        Graph g = getGraphWith( "x R y" );
        ExtendedIterator it = g.find( node( "x" ), Node.ANY, Node.ANY );
        it.next(); it.remove();
        assertFalse( g.find( Node.ANY, Node.ANY, Node.ANY ).hasNext() );
        }
        
    public void testBrokenPredicate()
        {
        Graph g = getGraphWith( "x R y" );
        ExtendedIterator it = g.find( Node.ANY, node( "R"), Node.ANY );
        it.next(); it.remove();
        assertFalse( g.find( Node.ANY, Node.ANY, Node.ANY ).hasNext() );
        }
        
    public void testBrokenObject()
        {
        Graph g = getGraphWith( "x R y" );
        ExtendedIterator it = g.find( Node.ANY, Node.ANY, node( "y" ) );
        it.next(); it.remove();
        assertFalse( g.find( Node.ANY, Node.ANY, Node.ANY ).hasNext() );
        }
    
    public void testRemoveAllDoesntUseFind()
        {
        Graph g = new GraphMem()
        	{
            public ExtendedIterator find( Node s, Node p, Node o )
                { throw new JenaException( "find is Not Allowed" ); }
            
            public ExtendedIterator find( Triple t )
                { throw new JenaException( "find is Not Allowed" ); }
        	};
        graphAdd( g, "x P y; a Q b" );
        g.getBulkUpdateHandler().removeAll();
        assertEquals( 0, g.size() );
        }
    }


/*
    (c) Copyright 2003 Hewlett-Packard Development Company, LP
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