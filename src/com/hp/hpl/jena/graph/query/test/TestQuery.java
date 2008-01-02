/*
 	(c) Copyright 2007, 2008 Hewlett-Packard Development Company, LP
 	All rights reserved.
 	$Id: TestQuery.java,v 1.2 2008-01-02 12:08:56 andy_seaborne Exp $
*/

package com.hp.hpl.jena.graph.query.test;

import java.util.*;

import com.hp.hpl.jena.graph.Triple;
import com.hp.hpl.jena.graph.query.Query;

public class TestQuery extends QueryTestBase
    {

    public TestQuery( String name )
        { super( name ); }

    public void testEmptyQueryPattern()
        {
        assertEquals( new ArrayList(), new Query().getPattern() );
        }
    
    public void testIOneTriple()
        {
        Query q = new Query();
        Triple spo = triple( "S P O" );
        q.addMatch( spo );
        assertEquals( listOfOne( spo ), q.getPattern() );
        }
    
    public void testSeveralTriples()
        {
        Triple [] triples = tripleArray( "a P b; c Q ?d; ?e R '17'" );
        List expected = new ArrayList();
        Query q = new Query();
        for (int i = 0; i < triples.length; i += 1)
            {
            expected.add( triples[i] );
            q.addMatch( triples[i] );
            assertEquals( expected, q.getPattern() );            
            }
        }
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