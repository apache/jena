/*
	(c) Copyright 2003, Hewlett-Packard Company, all rights reserved.
	[see end of file]
	$Id: TestStatements.java,v 1.7 2003-05-20 15:15:07 chris-dollin Exp $
*/

package com.hp.hpl.jena.rdf.model.test;

import com.hp.hpl.jena.rdf.model.*;
import com.hp.hpl.jena.graph.*;
import junit.framework.*;
import com.hp.hpl.jena.vocabulary.RDF;

import java.util.*;

public class TestStatements extends ModelTestBase
    {
    public TestStatements( String name )
        { super( name ); }
    
    public static TestSuite suite()
        { return new TestSuite( TestStatements.class ); }   
        
    /**
        this case came up when Chris was sorting out ReifedStatement and
        had mishacked Model.createStatement. A resource created in one
        model and incorporated into a statement asserted constructed by a
        different model should test equal to the resource extracted from that
        statement, even if it's a bnode.
    */
    public void testStuff()
        {
        Model red = ModelFactory.createDefaultModel();
        Model blue = ModelFactory.createDefaultModel();
        Resource r = red.createResource();
        Property p = red.createProperty( "" );
        Statement s = blue.createStatement( r, p, r );
        assertEquals( "subject preserved", r, s.getSubject() );
        assertEquals( "object preserved", r, s.getObject() );
        }
        
    public void testOtherStuff()
        {
        Model A = ModelFactory.createDefaultModel();
        Model B = ModelFactory.createDefaultModel();
        Resource S = A.createResource( "jena:S" );
        Resource R = A.createResource( "jena:R" );
        Property P = A.createProperty( "jena:P" );
        RDFNode O = A.createResource( "jena:O" );
        A.add( S, P, O );
        B.add( S, P, O );
        assertTrue( "X1", A.isIsomorphicWith( B ) );
    /* */
        A.add( R, RDF.subject, S );
        B.add( R, RDF.predicate, P );
        assertFalse( "X2", A.isIsomorphicWith( B ) );
    /* */
        A.add( R, RDF.predicate, P );
        B.add( R, RDF.subject, S );
        assertTrue( "X3", A.isIsomorphicWith( B ) );
    /* */
        A.add( R, RDF.object, O );
        B.add( R, RDF.type, RDF.Statement );
        assertFalse( "X4", A.isIsomorphicWith( B ) );
    /* */
        A.add( R, RDF.type, RDF.Statement );
        B.add( R, RDF.object, O );
        assertTrue( "X5", A.isIsomorphicWith( B ) );
        }
        
    public void testSet()
        {
        Model A = ModelFactory.createDefaultModel();
        Model B = ModelFactory.createDefaultModel();
        Resource S = A.createResource( "jena:S" );
        Resource R = A.createResource( "jena:R" );
        Property P = A.createProperty( "jena:P" );
        RDFNode O = A.createResource( "jena:O" );
        Statement S1 = A.createStatement( S, P, O );
        Statement S2 = A.createStatement( S, P, O );
        assertEquals( S1, S2 );
        S1.changeObject( S );
        // assertEquals( S1, S2 );
        HashMap h = new HashMap();
        h.put( S2, "pontisbright" );
        S2.changeObject( S );
        // System.err.println( h.get( S2 ) );
        }
        
    public void testPortingBlankNodes()
        {
        Model A = ModelFactory.createDefaultModel();
        Model B = ModelFactory.createDefaultModel();
        Resource anon = A.createResource();
        Resource bAnon = (Resource) anon.inModel( B );
        assertTrue( "moved resource should still be blank", bAnon.isAnon() );
        assertEquals( "move resource should equal original", anon, bAnon );
        }
        
    /**
        Feeble test that toString'ing a Statement[Impl] will display the data-type
        of its object if it has one.
    */
    public void testStatementPrintsType()
        {            
        Model m = ModelFactory.createDefaultModel();
        String fakeURI = "fake:URI";
        Resource S = m.createResource( ) ; 
        Property P = property( m, "PP" );
        RDFNode O = m.createTypedLiteral( "42", "", fakeURI);
        Statement st = m.createStatement( S, P, O );
        assertTrue( st.toString().indexOf( fakeURI ) > 0 );  
        }
    }

/*
    (c) Copyright Hewlett-Packard Company 2003
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


