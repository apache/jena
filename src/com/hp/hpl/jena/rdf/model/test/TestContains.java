/*
  (c) Copyright 2002, Hewlett-Packard Development Company, LP
  [See end of file]
  $Id: TestContains.java,v 1.6 2003-11-10 14:47:33 chris-dollin Exp $
*/

package com.hp.hpl.jena.rdf.model.test;

import com.hp.hpl.jena.rdf.model.*;

import junit.framework.*;

/**
 	@author kers
*/
public class TestContains extends ModelTestBase
    {
    public TestContains( String name )
        { super( name ); }
    
    public static TestSuite suite()
        { return new TestSuite( TestContains.class ); }          
        
    public void testContains( boolean yes, String facts, String resource )
        {
        Model m = modelWithStatements( facts );
        RDFNode r = rdfNode( m, resource );
        if (modelWithStatements( facts ).containsResource( r ) != yes)
            fail( "[" + facts + "] should" + (yes ? "" : " not") + " contain " + resource );
        }
        
    public void testContains()
        {
        testContains( false, "", "x" );
        testContains( false, "a R b", "x" );
        testContains( false, "a R b; c P d", "x" );
    /* */
        testContains( false, "a R b", "z" );
    /* */
        testContains( true, "x R y", "x" );
        testContains( true, "a P b", "P" );
        testContains( true, "i  Q  j", "j" );
        testContains( true, "x R y; a P b; i Q j", "y" );
    /* */
        testContains( true, "x R y; a P b; i Q j", "y" );
        testContains( true, "x R y; a P b; i Q j", "R" );
        testContains( true, "x R y; a P b; i Q j", "a" );
        }
    
    private Resource res( String uri )
        { return ResourceFactory.createResource( "eh:" + uri ); }
    
    private Property prop( String uri )
        { return ResourceFactory.createProperty( "eh:" + uri ); }
        
    public void testContainsWithNull()
        {
        testCWN( false, "", null, null, null );
        testCWN( true, "x R y", null, null, null );
        testCWN( false, "x R y", null, null, res( "z" ) );
        testCWN( true, "x RR y", res( "x" ), prop( "RR" ), null );
        testCWN( true, "a BB c", null, prop( "BB" ), res( "c" ) );
        testCWN( false, "a BB c", null, prop( "ZZ" ), res( "c" ) );
        }
    
    public void testCWN( boolean yes, String facts, Resource S, Property P, RDFNode O )
        {
        Model m = modelWithStatements( facts );
        assertEquals( yes, m.contains( S, P, O ) );
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