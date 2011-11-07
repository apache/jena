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

import com.hp.hpl.jena.rdf.model.*;
import com.hp.hpl.jena.regression.Regression.LitTestObj;
import com.hp.hpl.jena.vocabulary.RDF;

public class NewRegressionAddAndContains extends NewRegressionBase
    {
    public NewRegressionAddAndContains( String name )
        { super( name ); }

    public static TestSuite suite()
        { return new TestSuite( NewRegressionAddAndContains.class ); }
    
    protected Model getModel()
        { return ModelFactory.createDefaultModel(); }
    
    protected Model m;
    protected Resource S;
    protected Property P;
    
    @Override
    public void setUp()
        { 
        m = getModel();
        S = m.createResource( "http://nowhere.man/subject" ); 
        P = m.createProperty( "http://nowhere.man/predicate" ); 
        }
    
    @Override
    public void tearDown()
        { m = null; S = null; P = null; }
    
    public void testEmpty() 
        {
        assertFalse( m.containsLiteral( S, P, tvBoolean ) );
        assertFalse( m.contains( S, P, m.createResource() ) );
        assertFalse( m.containsLiteral( S, P, tvByte ) );
        assertFalse( m.containsLiteral( S, P, tvShort ) );
        assertFalse( m.containsLiteral( S, P, tvInt ) );
        assertFalse( m.containsLiteral( S, P, tvLong ) );
        assertFalse( m.containsLiteral( S, P, tvChar ) );
        assertFalse( m.containsLiteral( S, P, tvFloat ) );
        assertFalse( m.containsLiteral( S, P, tvDouble ) );
        assertFalse( m.containsLiteral( S, P, new LitTestObj( 12345 ) ) );
        assertFalse( m.contains( S, P, "test string" ) );
        assertFalse( m.contains( S, P, "test string", "en" ) );
        }
    
    public void testAddContainsResource()
        {
        Resource r = m.createResource();
        m.add( S, P, r );
        assertTrue( m.contains( S, P, r ) );
        }
    
    public void testAddContainsBoolean()
        {
        m.addLiteral( S, P, tvBoolean );
        assertTrue( m.containsLiteral( S, P, tvBoolean ) );
        }
    
    public void testAddContainsByte()
        {
        m.addLiteral( S, P, tvByte );
        assertTrue( m.containsLiteral( S, P, tvByte ) );
        }
    
    public void testAddContainsShort()
        {
        m.addLiteral( S, P, tvShort );
        assertTrue( m.containsLiteral( S, P, tvShort ) );
        }    
    
    public void testAddContainsInt()
        {
        m.addLiteral( S, P, tvInt );
        assertTrue( m.containsLiteral( S, P, tvInt ) );
        }
    
    public void testAddContainsLong()
        {
        m.addLiteral( S, P, tvLong );
        assertTrue( m.containsLiteral( S, P, tvLong ) );
        }
    
    public void testAddContainsChar()
        {
        m.addLiteral( S, P, tvChar );
        assertTrue( m.containsLiteral( S, P, tvChar ) );
        }
    
    public void testAddContainsFloat()
        {
        m.addLiteral( S, P, tvFloat );
        assertTrue( m.containsLiteral( S, P, tvFloat ) );
        }
    
    public void testAddContainsDouble()
        {
        m.addLiteral( S, P, tvDouble );
        assertTrue( m.containsLiteral( S, P, tvDouble ) );
        }

//    public void testAddContainsObject()
//        {
//        LitTestObj O = new LitTestObj( 12345 );
//        m.addLiteral( S, P, O );
//        assertTrue( m.containsLiteral( S, P, O ) );
//        }
    
    public void testAddContainsPlainString()
        {
        m.add( S, P, "test string" );
        assertTrue( m.contains( S, P, "test string" ) );
        assertFalse( m.contains( S, P, "test string", "en" ) );
        }
    
    public void testAddContainsLanguagedString()
        {
        m.add( S, P, "test string", "en" );
        assertFalse( m.contains( S, P, "test string" ) );
        assertTrue( m.contains( S, P, "test string", "en" ) );
        }
    
    public void testAddContainLiteralByStatement()
        {
        Literal L = m.createTypedLiteral( 210 );
        Statement s = m.createStatement( S, RDF.value, L );
        assertTrue( m.add( s ).contains( s ) );
        assertTrue( m.contains( S, RDF.value ) );
        }
    
    public void testAddDuplicateLeavesSizeSame()
        {
        Statement s = m.createStatement( S, RDF.value, "something" );
        m.add( s );
        long size = m.size();
        m.add( s );
        assertEquals( size, m.size() );
        }

    }
