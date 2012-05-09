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

package com.hp.hpl.jena.regression;

import junit.framework.*;

import com.hp.hpl.jena.datatypes.xsd.XSDDatatype;
import com.hp.hpl.jena.rdf.model.*;
import com.hp.hpl.jena.rdf.model.test.ModelTestBase;
import com.hp.hpl.jena.regression.Regression.*;

public class NewRegressionStatements extends ModelTestBase
    {
    public NewRegressionStatements( String name )
        { super( name ); }

    public static TestSuite suite()
        { return new TestSuite( NewRegressionStatements.class ); }
    
    protected Model getModel()
        { return ModelFactory.createDefaultModel(); }
    
    static final String subjURI = "http://aldabaran.hpl.hp.com/foo";
    static final String predURI = "http://aldabaran.hpl.hp.com/bar";

    protected Resource r;
    protected Property p;
    
    protected Model m;
    
    @Override
    public void setUp()
        {
        m = getModel();
        r = m.createResource( subjURI );
        p = m.createProperty( predURI );
        }
    
    @Override
    public void tearDown()
        { m = null; r = null; p = null; }
    
    public void testCreateStatementTrue()
        {
        Statement s = m.createLiteralStatement( r, p, true );
        assertEquals( r, s.getSubject() );
        assertEquals( p, s.getPredicate() );
        assertEquals( true, s.getBoolean() );
        }

    public void testCreateStatementByteMax()
        {
        Statement s = m.createLiteralStatement( r, p, Byte.MAX_VALUE );
        assertEquals( r, s.getSubject() );
        assertEquals( p, s.getPredicate() );
        assertEquals( Byte.MAX_VALUE, s.getByte() );
        }
    
    public void testCreateStatementShortMax()
        {
        Statement s = m.createLiteralStatement( r, p, Short.MAX_VALUE );
        assertEquals( r, s.getSubject() );
        assertEquals( p, s.getPredicate() );
        assertEquals( Short.MAX_VALUE, s.getShort() );
        }
    
    public void testCreateStatementIntMax()
        {
        Statement s = m.createLiteralStatement( r, p, Integer.MAX_VALUE );
        assertEquals( r, s.getSubject() );
        assertEquals( p, s.getPredicate() );
        assertEquals( Integer.MAX_VALUE, s.getInt() );
        }
    
    public void testCreateStatementLongMax()
        {
        Statement s = m.createLiteralStatement( r, p, Long.MAX_VALUE );
        assertEquals( r, s.getSubject() );
        assertEquals( p, s.getPredicate() );
        assertEquals( Long.MAX_VALUE, s.getLong() );
        }
    
    public void testCreateStatementChar()
        {
        Statement s = m.createLiteralStatement( r, p, '$' );
        assertEquals( r, s.getSubject() );
        assertEquals( p, s.getPredicate() );
        assertEquals( '$', s.getChar() );
        }
    
    public void testCreateStatementFloat()
        {
        Statement s = m.createStatement( r, p, m.createTypedLiteral( 123.456f ) );
        assertEquals( r, s.getSubject() );
        assertEquals( p, s.getPredicate() );
        assertEquals( 123.456f, s.getFloat(), 0.0005 );
        }
    
    public void testCreateStatementDouble()
        {
        Statement s = m.createStatement( r, p, m.createTypedLiteral( 12345.67890d ) );
        assertEquals( r, s.getSubject() );
        assertEquals( p, s.getPredicate() );
        assertEquals( 12345.67890d, s.getDouble(), 0.0000005 );
        }
    
    public void testCreateStatementString()
        {
        String string = "this is a plain string", lang = "en";
        Statement s = m.createStatement( r, p, string );
        assertEquals( r, s.getSubject() );
        assertEquals( p, s.getPredicate() );
        assertEquals( string, s.getString() );
        assertEquals( lang, m.createStatement( r, p, string, lang ).getLanguage() );
        }
    
    public void testCreateStatementTypeLiteral()
    {
        Model m = ModelFactory.createDefaultModel();
        Resource R  = m.createResource("http://example/r") ;
        Property P = m.createProperty("http://example/p") ;
        m.add(R, P, "2", XSDDatatype.XSDinteger) ;
        Literal L = ResourceFactory.createTypedLiteral("2", XSDDatatype.XSDinteger) ;
        assertTrue(m.contains(R, P, L)) ;
        assertFalse(m.contains(R, P, "2")) ;
    }
    
    public void testCreateStatementFactory()
        {
        LitTestObj tv = new LitTestObj( Long.MIN_VALUE );
        Statement s = m.createLiteralStatement( r, p, tv );
        assertEquals( r, s.getSubject() );
        assertEquals( p, s.getPredicate() );
//        assertEquals( tv, s.getObject( new LitTestObjF() ) );
        }

    public void testCreateStatementResource()
        {
        Resource tv = m.createResource();
        Statement s = m.createStatement( r, p, tv );
        assertEquals( r, s.getSubject() );
        assertEquals( p, s.getPredicate() );
        assertEquals( tv, s.getResource() );
        }
    }
