/*
	(c) Copyright 2010 Epimorphics Limited
	[see end of file]
	$Id: TestGraphBaseToString.java,v 1.1 2010-01-18 12:14:12 chris-dollin Exp $
	@author chris
*/

package com.hp.hpl.jena.graph.test;

import static com.hp.hpl.jena.graph.impl.GraphBase.TOSTRING_TRIPLE_BASE;
import static com.hp.hpl.jena.graph.impl.GraphBase.TOSTRING_TRIPLE_LIMIT;

import java.util.*;

import com.hp.hpl.jena.graph.*;
import com.hp.hpl.jena.graph.impl.GraphBase;
import com.hp.hpl.jena.util.iterator.*;

/**
    Tests for the revisions to GraphBase.toString() to see that it's compact,
    ie outputs no more than LIMIT triples.
    
    @author chris
*/
public class TestGraphBaseToString extends GraphTestBase
    {
    private static final class LittleGraphBase extends GraphBase
        {
        Set<Triple> triples = new HashSet<Triple>();
        
        @Override public void performAdd( Triple t )
            { triples.add( t ); }
        
        @Override protected ExtendedIterator<Triple> graphBaseFind( TripleMatch m )
            { return WrappedIterator.<Triple>create( triples.iterator() ); }
        }

    public TestGraphBaseToString( String name )
        { super( name ); }

    public void testToStringBaseAndLimit()
        {
        assertTrue( "triple base count must be greater than 0", 0 < GraphBase.TOSTRING_TRIPLE_BASE );
        assertTrue( "triple base count must be less than limit", GraphBase.TOSTRING_TRIPLE_BASE < GraphBase.TOSTRING_TRIPLE_LIMIT );
        assertTrue( "triple count limit must be less than 20", GraphBase.TOSTRING_TRIPLE_LIMIT < 20 );
        }
    
    public void testEllipsisAbsentForSmallModels()
        {
        Graph g = new LittleGraphBase();
        addTriples( g, 1, TOSTRING_TRIPLE_BASE );
        assertFalse( "small model must not contain ellipsis cut-off", g.toString().contains( "\\.\\.\\." ) );
        }
    
    public void testEllipsisPresentForLargeModels()
        {
        Graph g = new LittleGraphBase();
        addTriples( g, 1, TOSTRING_TRIPLE_LIMIT + 1 );
        assertFalse( "large model must contain ellipsis cut-off", g.toString().contains( "\\.\\.\\." ) );
        }
    
    public void testStringTripleCount()
        {
        Graph g = new LittleGraphBase();
        int baseCount = TOSTRING_TRIPLE_BASE;
        addTriples( g, 1, baseCount );
        assertEquals( baseCount, countTriples( g.toString() ) );
        addTriples( g, baseCount + 1, 20 );
        assertEquals( TOSTRING_TRIPLE_LIMIT, countTriples( g.toString() ) );
        }

    private int countTriples( String string )
        { return string.replaceAll( "[^;]+", "" ).length() + 1; }

    private void addTriples( Graph g, int from, int to )
        {
        for (int i = from; i <= to; i += 1)
            g.add( NodeCreateUtils.createTriple( "s p " + i ) );
        }
    }

/*
    (c) Copyright 2010 Epimorphics Limited
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
