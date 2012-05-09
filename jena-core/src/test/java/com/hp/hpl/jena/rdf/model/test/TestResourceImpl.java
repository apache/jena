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
import com.hp.hpl.jena.vocabulary.RDF;

import junit.framework.*;

/**
	TestResourceImpl - fresh tests, make sure as-ing works a bit.

	@author kers
*/
public class TestResourceImpl extends ModelTestBase 
    {
	public TestResourceImpl( String name ) 
        { super(name); }

    public static TestSuite suite()
        { return new TestSuite( TestResourceImpl.class ); }

    /**
        Test that a non-literal node can be as'ed into a resource
    */
    public void testCannotAsNonLiteral()
        { Model m = ModelFactory.createDefaultModel();  
        resource( m, "plumPie" ).as( Resource.class ); }
    
    /**
        Test that a literal node cannot be as'ed into a resource.
    */    
    public void testAsLiteral()
        { Model m = ModelFactory.createDefaultModel();  
        try 
            { literal( m, "17" ).as( Resource.class );  
            fail( "literals cannot be resources"); }
        catch (ResourceRequiredException e)
            { pass(); }}
    
    public void testNameSpace()
        { 
        assertEquals( "eh:", resource( "eh:xyz" ).getNameSpace() ); 
        assertEquals( "http://d/", resource( "http://d/stuff" ).getNameSpace() ); 
        assertEquals( "ftp://dd.com/12345", resource( "ftp://dd.com/12345" ).getNameSpace() ); 
        assertEquals( "http://domain/spoo#", resource( "http://domain/spoo#anchor" ).getNameSpace() ); 
        assertEquals( "ftp://abd/def#ghi#", resource( "ftp://abd/def#ghi#e11-2" ).getNameSpace() ); 
        }
    
    public void testGetModel()
        {
        Model m = ModelFactory.createDefaultModel();
        assertSame( m, m.createResource( "eh:/wossname" ).getModel() );
        }
    
    public void testGetLocalNameReturnsLocalName()
        { 
        assertEquals( "xyz", resource( "eh:xyz" ).getLocalName() );
        }
    
    public void testHasURI()
        { 
        assertTrue( resource( "eh:xyz" ).hasURI( "eh:xyz" ) );
        assertFalse( resource( "eh:xyz" ).hasURI( "eh:1yz" ) );
        assertFalse( ResourceFactory.createResource().hasURI( "42" ) );
        }
    
    public void testAddTypedPropertyDouble()
        {
        Model m = ModelFactory.createDefaultModel();
        Resource r = m.createResource();
        r.addLiteral( RDF.value, 1.0d );
        assertEquals( m.createTypedLiteral( 1.0d ), r.getProperty( RDF.value ).getLiteral() );
        }
    
    public void testAddTypedPropertyFloat()
        {
        Model m = ModelFactory.createDefaultModel();
        Resource r = m.createResource();
        r.addLiteral( RDF.value, 1.0f );
        assertEquals( m.createTypedLiteral( 1.0f ), r.getProperty( RDF.value ).getLiteral() );
        }
    
    public void testHasTypedPropertyDouble()
        {
        Model m = ModelFactory.createDefaultModel();
        Resource r = m.createResource();
        r.addLiteral( RDF.value, 1.0d );
        assertTrue( r.hasLiteral( RDF.value, 1.0d ) );       
        }
    
    public void testHasTypedPropertyFloat()
        {
        Model m = ModelFactory.createDefaultModel();
        Resource r = m.createResource();
        r.addLiteral( RDF.value, 1.0f );
        assertTrue( r.hasLiteral( RDF.value, 1.0f ) );       
        }
    
    public void testAddTypedPropertyLong()
        {
        Model m = ModelFactory.createDefaultModel();
        Resource r = m.createResource();
        r.addLiteral( RDF.value, 1L );
        assertEquals( m.createTypedLiteral( 1L ), r.getProperty( RDF.value ).getLiteral() );
        }
    
    public void testHasTypedPropertyLong()
        {
        Model m = ModelFactory.createDefaultModel();
        Resource r = m.createResource();
        r.addLiteral( RDF.value, 1L );
        assertTrue( r.hasLiteral( RDF.value, 1L ) );       
        }
    
    public void testAddTypedPropertyInt()
        {
//        Model m = ModelFactory.createDefaultModel();
//        Resource r = m.createResource();
//        r.addLiteral( RDF.value, 1 );
//        assertEquals( m.createTypedLiteral( 1 ), r.getProperty( RDF.value ).getLiteral() );
        }
    
    public void testHasTypedPropertyInt()
        {
        Model m = ModelFactory.createDefaultModel();
        Resource r = m.createResource();
        r.addLiteral( RDF.value, 1 );
        assertTrue( r.hasLiteral( RDF.value, 1 ) ); 
        }
    
    public void testAddTypedPropertyChar()
        {
        Model m = ModelFactory.createDefaultModel();
        Resource r = m.createResource();
        r.addLiteral( RDF.value, 'x' );
        assertEquals( m.createTypedLiteral( 'x' ), r.getProperty( RDF.value ).getLiteral() );
        }
    
    public void testHasTypedPropertyChar()
        {
        Model m = ModelFactory.createDefaultModel();
        Resource r = m.createResource();
        r.addLiteral( RDF.value, 'x' );
        assertTrue( r.hasLiteral( RDF.value, 'x' ) );     
        }
    
    public void testAddTypedPropertyBoolean()
        {
        Model m = ModelFactory.createDefaultModel();
        Resource r = m.createResource();
        r.addLiteral( RDF.value, true );
        assertEquals( m.createTypedLiteral( true ), r.getProperty( RDF.value ).getLiteral() );
        }
    
    public void testHasTypedPropertyBoolean()
        {
        Model m = ModelFactory.createDefaultModel();
        Resource r = m.createResource();
        r.addLiteral( RDF.value, false );
        assertTrue( r.hasLiteral( RDF.value, false ) ); 
        }
    
    public void testAddTypedPropertyString()
        {
        
        }
    
    public void testHasTypedPropertyString()
        {
        
        }
    
    public void testAddTypedPropertyObject()
        {
        Object z = new Object();
        Model m = ModelFactory.createDefaultModel();
        Resource r = m.createResource();
        r.addLiteral( RDF.value, z );
        assertEquals( m.createTypedLiteral( z ), r.getProperty( RDF.value ).getLiteral() );
        }
    
    public void testAddLiteralPassesLiteralUnmodified()
        {
        Model m = ModelFactory.createDefaultModel();
        Resource r = m.createResource();
        Literal lit = m.createLiteral( "spoo" );
        r.addLiteral( RDF.value, lit );
        assertTrue( "model should contain unmodified literal", m.contains( null, RDF.value, lit ) );       
        }
    
    public void testHasTypedPropertyObject()
        {
        Object z = new Object();
        Model m = ModelFactory.createDefaultModel();
        Resource r = m.createResource();
        r.addLiteral( RDF.value, z );
        assertTrue( r.hasLiteral( RDF.value, z ) ); 
        }
    
    public void testGetPropertyResourceValueReturnsResource()
        {
        Model m = modelWithStatements( "x p 17; x p y" );
        Resource r = m.createResource( "eh:/x" );
        Resource value = r.getPropertyResourceValue( property( "p" ) );
        assertEquals( resource( "y" ), value );
        }
    
    public void testGetPropertyResourceValueReturnsNull()
        {
        Model m = modelWithStatements( "x p 17" );
        Resource r = m.createResource( "eh:/x" );
        assertNull( r.getPropertyResourceValue( property( "q" ) ) );
        assertNull( r.getPropertyResourceValue( property( "p" ) ) );
        }
    }
