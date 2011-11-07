/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.hp.hpl.jena.rdf.model.test;

import com.hp.hpl.jena.graph.FrontsTriple;
import com.hp.hpl.jena.rdf.model.*;
import junit.framework.*;

import com.hp.hpl.jena.vocabulary.RDF;

public class TestStatements extends ModelTestBase
    {
    public TestStatements( String name )
        { super( name ); }
    
    public static TestSuite suite()
        { return new TestSuite( TestStatements.class ); }   
    
    public void testStatmentMap1Selectors()
        {
        Statement s = statement( "sub pred obj" );
        assertEquals( resource( "sub" ), Statement.Util.getSubject.map1( s ) );
        assertEquals( resource( "pred" ), Statement.Util.getPredicate.map1( s ) );
        assertEquals( resource( "obj" ), Statement.Util.getObject.map1( s ) );
        }
    
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
        Statement spo = A.createStatement( S, P, O );
        A.add( spo );
        Statement sps = A.createStatement( S, P, S );
        assertEquals( sps, spo.changeObject( S ) );
        assertFalse( A.contains( spo ) );
        assertTrue( A.contains( sps ) );
        }
        
    public void testPortingBlankNodes()
        {
        Model A = ModelFactory.createDefaultModel();
        Model B = ModelFactory.createDefaultModel();
        Resource anon = A.createResource();
        Resource bAnon = anon.inModel( B );
        assertTrue( "moved resource should still be blank", bAnon.isAnon() );
        assertEquals( "move resource should equal original", anon, bAnon );
        }
        
    public void testTripleWrapper()
    	{
    	Model A = ModelFactory.createDefaultModel();
    	assertInstanceOf( FrontsTriple.class, statement( A, "s p o" ) );
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
        RDFNode O = m.createTypedLiteral( "42",  fakeURI);
        Statement st = m.createStatement( S, P, O );
        assertTrue( st.toString().indexOf( fakeURI ) > 0 );  
        }
    
    public void testHasWellFormedXML()
        {
        assertFalse( statement( "s P 1" ).hasWellFormedXML() );
        assertFalse( statement( "S P '<x>/x>'rdf:XMLLiteral" ).hasWellFormedXML() );
        assertTrue( statement( "S P '<x></x>'rdf:XMLLiteral" ).hasWellFormedXML() );
        }
    }
