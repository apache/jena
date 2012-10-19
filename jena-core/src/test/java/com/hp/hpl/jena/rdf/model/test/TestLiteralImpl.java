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

import junit.framework.TestSuite ;

import com.hp.hpl.jena.datatypes.TypeMapper ;
import com.hp.hpl.jena.graph.impl.AdhocDatatype ;
import com.hp.hpl.jena.rdf.model.* ;

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
    
    static class UniqueValueClass1 { 
        String value ;
        UniqueValueClass1(String value) { this.value = value ; }
        @Override public String toString() { return value ; } 
    }
    
    static class UniqueValueClass2 { 
        String value ;
        UniqueValueClass2(String value) { this.value = value ; }
        @Override public String toString() { return value ; } 
    }

    public void testSameAdhocClassUS()
    	{
        Model m = ModelFactory.createDefaultModel();
        UniqueValueClass1 ra = new UniqueValueClass1("rhubarb") ;
        UniqueValueClass1 rb = new UniqueValueClass1("cottage") ;
        assertNull( "not expecting registered RDF Datatype", TypeMapper.getInstance().getTypeByValue( ra ) );
        Literal la = m.createTypedLiteral( ra );    // Sets the type mapper - contaminates it with UniqueValueClass1
        Literal lb = m.createTypedLiteral( rb );
        assertInstanceOf( AdhocDatatype.class, la.getDatatype() );
        assertSame( la.getDatatype(), lb.getDatatype() );
        assertNotNull(TypeMapper.getInstance().getTypeByValue( ra ) );
    	}

    // Tests are not necessarily run in order so use UniqueValueClass2
    public void testTypedLiteralTypesAndValues()
        {
        Model m = ModelFactory.createDefaultModel();
        //Resource r = m.createResource( "eh:/rhubarb" );
        UniqueValueClass2 r = new UniqueValueClass2("rhubarb") ;
        assertNull( "not expecting registered RDF Datatype", TypeMapper.getInstance().getTypeByValue( r ) );
        Literal typed = m.createTypedLiteral( r );      // Sets the type mapper - contaminates it with UniqueValueClass2
        Literal string = m.createLiteral( r.value );
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
