/*
 	(c) Copyright 2007, 2008 Hewlett-Packard Development Company, LP
 	All rights reserved.
 	$Id: TestGraphEvents.java,v 1.2 2008-01-02 12:05:35 andy_seaborne Exp $
*/

package com.hp.hpl.jena.graph.test;

import com.hp.hpl.jena.graph.*;

public class TestGraphEvents extends GraphTestBase
    {
    public TestGraphEvents( String name )
        { super( name ); }

    public void testGraphEventContent()
        {
        testGraphEventContents( "testing", "an example" );
        testGraphEventContents( "toasting", Boolean.TRUE );
        testGraphEventContents( "tasting", Triple.create( "we are here" ) );
        }
    
    public void testGraphEventsRemove()
        {
        testGraphEventsRemove( "s", "p", "o" );
        testGraphEventsRemove( "s", "p", "17" );
        testGraphEventsRemove( "_s", "p", "'object'" );
        testGraphEventsRemove( "not:known", "p", "'chat'fr" );
        }

    private void testGraphEventsRemove( String S, String P, String O )
        {
        Triple expected = Triple.create( S + " " + P + " " + O );
        GraphEvents e = GraphEvents.remove( node( S ), node( P ), node( O ) );
        assertEquals( expected, e.getContent() );
        assertEquals( "remove", e.getTitle() );
        }

    private void testGraphEventContents( String title, Object expected )
        {
        GraphEvents e = new GraphEvents( title, expected );
        assertEquals( title, e.getTitle() );
        assertEquals( expected, e.getContent() );
        }
    }

/*
    (c) Copyright 2007, 2008 Hewlett-Packard Development Company, LP
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
