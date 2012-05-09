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

import com.hp.hpl.jena.rdf.model.*;
import com.hp.hpl.jena.vocabulary.RDF;
//import com.hp.hpl.jena.regression.Regression.*;

import junit.framework.*;

public class NewRegressionStatementMethods extends NewRegressionBase
    {
    public NewRegressionStatementMethods( String name )
        { super( name ); }

    public static Test suite()
        { return new TestSuite( NewRegressionStatementMethods.class ); }

    protected Model getModel()
        { return ModelFactory.createDefaultModel(); }

    protected Model m;
    protected Resource r;

    @Override
    public void setUp()
        { 
        m = getModel(); 
        r = m.createResource();
        }

    public void testGetResource()
        {
        assertEquals( r, m.createStatement( r, RDF.value, r ).getResource() );
        }
    
    public void testGetResourceFailure()
        {
        try { m.createLiteralStatement( r, RDF.value, false ).getResource(); fail( "should trap non-resource object" ); }
        catch (ResourceRequiredException e) { pass(); }
        }
    
    public void testGetTrueBoolean()
        {
        assertEquals( true, m.createLiteralStatement( r, RDF.value, true ).getLiteral().getBoolean() );
        }
    
    public void testGetLiteralFailure()
        {
        try { m.createStatement( r, RDF.value, r ).getLiteral(); fail( "should trap non-literal object" ); }
        catch (LiteralRequiredException e) { pass(); }
        }
    
    public void testBoolean()
        {
        Statement s = m.createLiteralStatement( r, RDF.value, true );
        assertEquals( m.createTypedLiteral( true ), s.getObject() );
        assertEquals( true, s.getBoolean() );
        }
    
    public void testByte()
        {
        Statement s = m.createLiteralStatement( r, RDF.value, tvByte );
        assertEquals( m.createTypedLiteral( tvByte ), s.getObject() );
        assertEquals( tvByte, s.getLong() );
        }
    
    public void testShort()
        {
        Statement s = m.createLiteralStatement( r, RDF.value, tvShort );
        assertEquals( m.createTypedLiteral( tvShort ), s.getObject() );
        assertEquals( tvShort, s.getShort() );
        }
    
    public void testInt()
        {
        Statement s = m.createLiteralStatement( r, RDF.value, tvInt );
        assertEquals( m.createTypedLiteral( tvInt ), s.getObject() );
        assertEquals( tvInt, s.getInt() );
        }
    
    public void testLong()
        {
        Statement s = m.createLiteralStatement( r, RDF.value, tvLong );
        assertEquals( m.createTypedLiteral( tvLong ), s.getObject() );
        assertEquals( tvLong, s.getLong() );
        }
    
    public void testChar()
        {
        Statement s = m.createLiteralStatement( r, RDF.value, tvChar );
        assertEquals( m.createTypedLiteral( tvChar ), s.getObject() );
        assertEquals( tvChar, s.getChar() );
        }
    
    public void testFloat()
        {
        Statement s = m.createLiteralStatement( r, RDF.value, tvFloat );
        assertEquals( m.createTypedLiteral( tvFloat ), s.getObject() );
        assertEquals( tvFloat, s.getFloat(), fDelta );
        }
    
    public void testDouble()
        {
        Statement s = m.createLiteralStatement( r, RDF.value, tvDouble );
        assertEquals( m.createTypedLiteral( tvDouble ), s.getObject() );
        assertEquals( tvDouble, s.getDouble(), dDelta );
        }
    
    public void testString()
        {
        assertEquals( tvString, m.createStatement( r, RDF.value, tvString ).getString() );
        }
    
    public void testStringWithLanguage()
        {
        String lang = "fr";
        assertEquals( tvString, m.createStatement( r, RDF.value, tvString, lang ).getString() );
        assertEquals( lang, m.createStatement( r, RDF.value, tvString, lang ).getLanguage() );
        }
    
//    public void testResObj()
//        {
//        Resource   tvResObj = m.createResource( new ResTestObjF() );
//        assertEquals( tvResObj, m.createStatement( r, RDF.value, tvResObj ).getResource() );
//        }
    
//    public void testLitObj()
//        {
//        assertEquals( tvLitObj, m.createLiteralStatement( r, RDF.value, tvLitObj ).getObject( new LitTestObjF() ) );
//        }
    
    public void testBag()
        {
        Bag tvBag = m.createBag();
        assertEquals( tvBag, m.createStatement( r, RDF.value, tvBag ).getBag() );
        }
    
    public void testSeq()
        {
        Seq tvSeq = m.createSeq();
        assertEquals( tvSeq, m.createStatement( r, RDF.value, tvSeq ).getSeq() );
        }
    
    public void testAlt()
        {
        Alt tvAlt = m.createAlt();
        assertEquals( tvAlt, m.createStatement( r, RDF.value, tvAlt ).getAlt() );
        }

    public void testChangeObjectBoolean()
        {
        Statement sTrue = loadInitialStatement();
        Statement sFalse = sTrue.changeLiteralObject( false );
        checkChangedStatementSP( sFalse );
        assertEquals( m.createTypedLiteral( false ), sFalse.getObject() );
        assertEquals( false, sFalse.getBoolean() );
        checkCorrectStatements( sTrue, sFalse );
        assertTrue( m.containsLiteral( r, RDF.value, false ) );
        }

    public void testChangeObjectByte()
        {
        Statement sTrue = loadInitialStatement();
        Statement changed = sTrue.changeLiteralObject( tvByte );
        checkChangedStatementSP( changed );
        assertEquals( m.createTypedLiteral( tvByte ), changed.getObject() );
        assertEquals( tvByte, changed.getByte() );
        checkCorrectStatements( sTrue, changed );
        assertTrue( m.containsLiteral( r, RDF.value, tvByte ) );
        }

    public void testChangeObjectShort()
        {
        Statement sTrue = loadInitialStatement();
        Statement changed = sTrue.changeLiteralObject( tvShort );
        checkChangedStatementSP( changed );
        assertEquals( m.createTypedLiteral( tvShort ), changed.getObject() );
        assertEquals( tvShort, changed.getShort() );
        checkCorrectStatements( sTrue, changed );
        assertTrue( m.containsLiteral( r, RDF.value, tvShort ) );
        }

    public void testChangeObjectInt()
        {
        Statement sTrue = loadInitialStatement();
        Statement changed = sTrue.changeLiteralObject( tvInt );
        checkChangedStatementSP( changed );
        assertEquals( m.createTypedLiteral( tvInt ), changed.getObject() );
        assertEquals( tvInt, changed.getInt() );
        checkCorrectStatements( sTrue, changed );
        assertTrue( m.containsLiteral( r, RDF.value, tvInt ) );
        }

    public void testChangeObjectLong()
        {
        Statement sTrue = loadInitialStatement();
        Statement changed = sTrue.changeLiteralObject( tvLong );
        checkChangedStatementSP( changed );
        assertEquals( m.createTypedLiteral( tvLong ), changed.getObject() );
        assertEquals( tvLong, changed.getLong() );
        checkCorrectStatements( sTrue, changed );
        assertTrue( m.containsLiteral( r, RDF.value, tvLong ) );
        }

    public void testChangeObjectChar()
        {
        Statement sTrue = loadInitialStatement();
        Statement changed = sTrue.changeLiteralObject( tvChar );
        checkChangedStatementSP( changed );
        assertEquals( tvChar, changed.getChar() );
        assertEquals( m.createTypedLiteral( tvChar ), changed.getObject() );
        checkCorrectStatements( sTrue, changed );
        assertTrue( m.containsLiteral( r, RDF.value, tvChar ) );
        }
    
    public void testChangeObjectFloat()
        {
        Statement sTrue = loadInitialStatement();
        Statement changed = sTrue.changeLiteralObject( tvFloat );
        checkChangedStatementSP( changed );
        assertEquals( m.createTypedLiteral( tvFloat ), changed.getObject() );
        assertEquals( tvFloat, changed.getFloat(), fDelta );
        checkCorrectStatements( sTrue, changed );
        assertTrue( m.containsLiteral( r, RDF.value, tvFloat ) );
        }

    public void testChangeObjectDouble()
        {
        Statement sTrue = loadInitialStatement();
        Statement changed = sTrue.changeLiteralObject( tvDouble );
        checkChangedStatementSP( changed );
        assertEquals( m.createTypedLiteral( tvDouble ), changed.getObject() );
        assertEquals( tvDouble, changed.getDouble(), dDelta );
        checkCorrectStatements( sTrue, changed );
        assertTrue( m.containsLiteral( r, RDF.value, tvDouble ) );
        }

    public void testChangeObjectString()
        {
        Statement sTrue = loadInitialStatement();
        Statement changed = sTrue.changeObject( tvString );
        checkChangedStatementSP( changed );
        assertEquals( tvString, changed.getString() );
        checkCorrectStatements( sTrue, changed );
        assertTrue( m.contains( r, RDF.value, tvString ) );
        }

    public void testChangeObjectStringWithLanguage()
        {
        String lang = "en";
        Statement sTrue = loadInitialStatement();
        Statement changed = sTrue.changeObject( tvString, lang );
        checkChangedStatementSP( changed );
        assertEquals( tvString, changed.getString() );
        assertEquals( lang, changed.getLanguage() );
        checkCorrectStatements( sTrue, changed );
        assertTrue( m.contains( r, RDF.value, tvString, lang ) );
        }

//    public void testChangeObjectResObject()
//        {
//        Resource   tvResObj = m.createResource( new ResTestObjF() );
//        Statement sTrue = loadInitialStatement();
//        Statement changed = sTrue.changeObject( tvResObj );
//        checkChangedStatementSP( changed );
//        assertEquals( tvResObj, changed.getResource() );
//        checkCorrectStatements( sTrue, changed );
//        assertTrue( m.contains( r, RDF.value, tvResObj ) );
//        }

    public void testChangeObjectLiteral()
        {
        Statement sTrue = loadInitialStatement();
        m.remove( sTrue );
        assertFalse( m.contains( sTrue ) );
        assertFalse( m.containsLiteral( r, RDF.value, true ) );
        }

    public void testChangeObjectYByte()
        {
        Statement sTrue = loadInitialStatement();
        Statement changed = sTrue.changeLiteralObject( tvByte );
        checkChangedStatementSP( changed );
        assertEquals( tvByte, changed.getByte() );
        checkCorrectStatements( sTrue, changed );
        assertTrue( m.containsLiteral( r, RDF.value, tvByte ) );
        }
    
    protected void checkCorrectStatements( Statement sTrue, Statement changed )
        {
        assertFalse( m.contains( sTrue ) );
        assertFalse( m.containsLiteral( r, RDF.value, true ) );
        assertTrue( m.contains( changed ) );
        }

    protected void checkChangedStatementSP( Statement changed )
        {
        assertEquals( r, changed.getSubject() );
        assertEquals( RDF.value, changed.getPredicate() );
        }

    protected Statement loadInitialStatement()
        {
        Statement sTrue = m.createLiteralStatement( r, RDF.value, true );
        m.add( sTrue );
        return sTrue;
        }
    }
