/*
  (c) Copyright 2004, 2005 Hewlett-Packard Development Company, LP, all rights reserved.
  [See end of file]
  $Id: TestTripleCache.java,v 1.2 2005-02-21 11:52:48 andy_seaborne Exp $
*/

package com.hp.hpl.jena.graph.test;

import junit.framework.*;

import com.hp.hpl.jena.graph.*;

/**
 TestTripleCache

 @author kers
*/
public class TestTripleCache extends GraphTestBase 
    {
	public TestTripleCache( String name ) 
        { super( name ); }
    
    public static TestSuite suite()
        { return new TestSuite( TestTripleCache.class ); }
    
    protected static Triple [] someTriples = makeTriples();
    
    protected static Triple [] makeTriples()
        {
        Node [] S = { node( "x" ), node( "gooseberry" ), node( "_who" ) };
        Node [] P = { node( "p" ), node( "ramshackle" ), node( "_what" ) };
        Node [] O = { node( "o" ), node( "42" ), node( "'alpha'greek" ), node( "_o" ) };
        int here = 0;
        Triple [] result = new Triple[S.length * P.length * O.length];
        for (int i = 0; i < S.length; i += 1)
            for (int j = 0; j < P.length; j += 1)
                for (int k = 0; k < O.length; k += 1)
                    result[here++] = new Triple( S[i], P[j], O[k] );
        return result;
        }
    
    public void testEmptyTripleCache()
        {
        TripleCache tc = new TripleCache();
        for (int i = 0; i < someTriples.length; i += 1)
            {
            Triple t = someTriples[i];
            assertEquals( null, tc.get( t.getSubject(), t.getPredicate(), t.getObject() ) );
            }
        }
    
    public void testPutReturnsTriple()
        {
        TripleCache tc = new TripleCache();
        for (int i = 0; i < someTriples.length; i += 1)
            assertSame( someTriples[i], tc.put( someTriples[i] ) );
        }
    
    public void testPutinTripleCache()
        {
        TripleCache tc = new TripleCache();
        for (int i = 0; i < someTriples.length; i += 1)
            {
            Triple t = someTriples[i];
            tc.put( t );
            assertEquals( t, tc.get( t.getSubject(), t.getPredicate(), t.getObject() ) );
            }
        }
    
    public void testCacheClash()
        {
        TripleCache tc = new TripleCache();
        Triple A = new Triple( node( "eg:Yx" ), node( "p" ), node( "o" ) );
        Triple B = new Triple( node( "eg:ZY" ), node( "p" ), node( "o" ) );
        assertEquals( A.hashCode(), B.hashCode() );
        tc.put( A );
        assertEquals( null, tc.get( B.getSubject(), B.getPredicate(), B.getObject() ) );
        }
    }

/*
    (c) Copyright 2004, 2005 Hewlett-Packard Development Company, LP
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