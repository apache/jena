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

import com.hp.hpl.jena.datatypes.TypeMapper;
import com.hp.hpl.jena.graph.impl.AdhocDatatype;
import com.hp.hpl.jena.rdf.model.*;

import junit.framework.*;

/**
	TestLiteralImpl - minimal, this is the first time an extra test has been needed above
    the regression testing.

	@author kers
*/
public class TestLiteralImpl extends ModelTestBase 
    {
    public TestLiteralImpl( String name )
        { super( name ); }
        
    public static TestSuite suite()
        { return new TestSuite( TestLiteralImpl.class ); }

    /**
        Test that a non-literal node cannot be as'ed into a literal
    */
    public void testCannotAsNonLiteral()
        { Model m = ModelFactory.createDefaultModel(); 
        try
            { resource( m, "plumPie" ).as( Literal.class ); 
            fail( "non-literal cannot be converted to literal" ); }
        catch (LiteralRequiredException l)
            { pass(); } }
    
    /**
        Test that a literal node can be as'ed into a literal.
    */    
    public void testAsLiteral()
        { Model m = ModelFactory.createDefaultModel();  
        literal( m, "17" ).as( Literal.class );  }
    
    public void testLiteralHasModel()
        {
        Model m = ModelFactory.createDefaultModel();
        testLiteralHasModel( m, m.createLiteral( "hello, world" ) );
        testLiteralHasModel( m, m.createLiteral( "hello, world", "en-UK" ) );
        testLiteralHasModel( m, m.createLiteral( "hello, world", true ) );
        testLiteralHasModel( m, m.createTypedLiteral( "hello, world" ) );
        testLiteralHasModel( m, m.createTypedLiteral( false ) );
        testLiteralHasModel( m, m.createTypedLiteral( 17 ) );
        testLiteralHasModel( m, m.createTypedLiteral( 'x' ) );
        }

    private void testLiteralHasModel( Model m, Literal lit )
        { assertSame( m, lit.getModel() ); }
    
    public void testInModel()
        {
        Model m1 = ModelFactory.createDefaultModel();
        Model m2 = ModelFactory.createDefaultModel();
        Literal l1 = m1.createLiteral( "17" );
        Literal l2 = l1.inModel( m2 );
        assertEquals( l1, l2 );
        assertSame( m2, l2.getModel() );
        }    
    
    public void testSameAdhocClassUS()
    	{
        Model m = ModelFactory.createDefaultModel();
        Resource ra = m.createResource( "eh:/rhubarb" );
        Resource rb = m.createResource( "eh:/cottage" );
        assertNull( "not expecting ResourceImpl to have RDF Datatype get", TypeMapper.getInstance().getTypeByValue( ra ) );
        Literal la = m.createTypedLiteral( ra ); 
        Literal lb = m.createTypedLiteral( rb );
        assertInstanceOf( AdhocDatatype.class, la.getDatatype() );
        assertSame( la.getDatatype(), lb.getDatatype() );
    	}
    
    public void testTypedLiteralTypesAndValues()
        {
        Model m = ModelFactory.createDefaultModel();
        Resource r = m.createResource( "eh:/rhubarb" );
        Literal typed = m.createTypedLiteral( r ); 
        Literal string = m.createLiteral( r.getURI() );
        assertEquals( string.getLexicalForm(), typed.getLexicalForm() );
        assertEquals( string.getLanguage(), typed.getLanguage() );
        assertDiffer( string.getDatatypeURI(), typed.getDatatypeURI() );
        assertNotNull( "a datatype should have been invented for Resource[Impl]", typed.getDatatype() );
        assertDiffer( typed, string );
        assertDiffer( typed.getValue(), string.getValue() );
        assertEquals( r, typed.getValue() );
        assertDiffer( typed.hashCode(), string.hashCode() );
        }
    }
