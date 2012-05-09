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

import com.hp.hpl.jena.rdf.model.*;

import java.util.*;

import junit.framework.*;

/**
 	@author kers
    This class tests various properties of RDFNodes, to start with the
    new Visitor stuff.
*/
public class TestRDFNodes extends ModelTestBase
    {
    public TestRDFNodes(String name)
        { super(name); }

    public static TestSuite suite()
        { return new TestSuite( TestRDFNodes.class ); }
        
    public void testRDFVisitor()
        {
        final List<String> history = new ArrayList<String>();
        Model m = ModelFactory.createDefaultModel();
        final RDFNode S = m.createResource();
        final RDFNode P = m.createProperty( "eh:PP" );
        final RDFNode O = m.createLiteral( "LL" );
    /* */
        RDFVisitor rv = new RDFVisitor() 
            {
            @Override
            public Object visitBlank( Resource R, AnonId id )
                { 
                history.add( "blank" ); 
                assertTrue( "must visit correct node", R == S );
                assertEquals( "must have correct field", R.getId(), id );
                return "blank result"; 
                }
            @Override
            public Object visitURI( Resource R, String uri )
                { 
                history.add( "uri" ); 
                assertTrue( "must visit correct node", R == P );
                assertEquals( "must have correct field", R.getURI(), uri );
                return "uri result"; 
                }
            @Override
            public Object visitLiteral( Literal L )
                { 
                history.add( "literal" );
                assertTrue( "must visit correct node", L == O ); 
                return "literal result"; 
                }
            };
    /* */
        assertEquals( "blank result", S.visitWith( rv ) );
        assertEquals( "uri result", P.visitWith( rv ) );
        assertEquals( "literal result", O.visitWith( rv ) );
        assertEquals( listOfStrings( "blank uri literal" ), history );
        }
        
    public void testRemoveAllRemoves()
        {
        String ps = "x P a; x P b", rest = "x Q c; y P a; y Q b";
        Model m = modelWithStatements( ps + "; " + rest );
        Resource r = resource( m, "x" );
        Resource r2 = r.removeAll( property( m, "P" ) );
        assertSame( "removeAll should deliver its receiver", r, r2 );
        assertIsoModels( "x's P-values should go", modelWithStatements( rest ), m );
        }
        
    public void testRemoveAllBoring()
        {
        Model m1 = modelWithStatements( "x P a; y Q b" );
        Model m2 = modelWithStatements( "x P a; y Q b" );
        resource( m2, "x" ).removeAll( property( m2, "Z" ) );
        assertIsoModels( "m2 should be unchanged", m1, m2 );
        }
        
    public void testInModel()
        {
        Model m1 = modelWithStatements( "" );
        Model m2 = modelWithStatements( "" );
        Resource r1 = resource( m1, "r1" );
        Resource r2 = resource( m1, "_r2" );
    /* */
        assertTrue( r1.getModel() == m1 );
        assertTrue( r2.getModel() == m1 );
        assertFalse( r1.isAnon() );
        assertTrue( r2.isAnon() );
    /* */
        assertTrue( r1.inModel( m2 ).getModel() == m2 );
        assertTrue( r2.inModel( m2 ).getModel() == m2 );
    /* */
        assertEquals( r1, r1.inModel( m2 ) );
        assertEquals( r2, r2.inModel( m2 ) );
        }
    
    public void testIsAnon()
        {
        Model m = modelWithStatements( "" );
        assertEquals( false, m.createResource( "eh:/foo" ).isAnon() );
        assertEquals( true, m.createResource().isAnon() );
        assertEquals( false, m.createTypedLiteral( 17 ).isAnon() );
        assertEquals( false, m.createTypedLiteral( "hello" ).isAnon() );
        }  
    
    public void testIsLiteral()
        {
        Model m = modelWithStatements( "" );
        assertEquals( false, m.createResource( "eh:/foo" ).isLiteral() );
        assertEquals( false, m.createResource().isLiteral() );
        assertEquals( true, m.createTypedLiteral( 17 ).isLiteral() );
        assertEquals( true, m.createTypedLiteral( "hello" ).isLiteral() );
        }
    
    public void testIsURIResource()
        {
        Model m = modelWithStatements( "" );
        assertEquals( true, m.createResource( "eh:/foo" ).isURIResource() );
        assertEquals( false, m.createResource().isURIResource() );
        assertEquals( false, m.createTypedLiteral( 17 ).isURIResource() );
        assertEquals( false, m.createTypedLiteral( "hello" ).isURIResource() );
        }
    
    public void testIsResource()
        {
        Model m = modelWithStatements( "" );
        assertEquals( true, m.createResource( "eh:/foo" ).isResource() );
        assertEquals( true, m.createResource().isResource() );
        assertEquals( false, m.createTypedLiteral( 17 ).isResource() );
        assertEquals( false, m.createTypedLiteral( "hello" ).isResource() );
        }
    
    public void testRDFNodeAsResource()
        {
        Model m = modelWithStatements( "" );
        Resource r = m.createResource( "eh:/spoo" );
        assertSame( r, ((RDFNode) r).asResource() );
        }
    
    public void testLiteralAsResourceThrows()
        {
        Model m = modelWithStatements( "" );
        Resource r = m.createResource( "eh:/spoo" );
        try { r.asLiteral(); fail( "should not be able to do Resource.asLiteral()" ); }
        catch (LiteralRequiredException e) {}
        }
    
    public void testRDFNodeAsLiteral()
        {
        Model m = modelWithStatements( "" );
        Literal l = m.createLiteral( "hello, world" );
        assertSame( l, ((RDFNode) l).asLiteral() );
        }
    
    public void testResourceAsLiteralThrows()
        {
        Model m = modelWithStatements( "" );
        Literal l = m.createLiteral( "hello, world" );
        try { l.asResource(); fail( "should not be able to do Literal.asResource()" ); }
        catch (ResourceRequiredException e) {}
        }
    }
