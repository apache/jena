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

import junit.framework.TestSuite;

import com.hp.hpl.jena.rdf.model.*;
//import com.hp.hpl.jena.regression.Regression.*;

public class NewRegressionLiterals extends NewRegressionBase
    {
    public NewRegressionLiterals( String name )
        { super( name ); }
    
    public static TestSuite suite()
        { return new TestSuite( NewRegressionLiterals.class ); }
    
    protected Model getModel() 
        { return ModelFactory.createDefaultModel(); }
    
    public void testBooleans()
        {
        Model m = getModel();
        assertTrue( m.createTypedLiteral( true ).getBoolean() );
        assertFalse( m.createTypedLiteral( false ).getBoolean() );
        }

    public void testByteLiterals()
        {
        Model m = getModel();
        testByte( m, (byte) 0 );
        testByte( m, (byte) -1 );
        testByte( m, Byte.MIN_VALUE );
        testByte( m, Byte.MAX_VALUE );
        }
    
    public void testShortLiterals()
        {
        Model m = getModel();
        testShort( m, (short) 0 );
        testShort( m, (short) -1 );
        testShort( m, Short.MIN_VALUE );
        testShort( m, Short.MAX_VALUE );
        }

    public void testIntLiterals()
        {
        Model m = getModel();
        testInt( m, 0 );
        testInt( m, -1 );
        testInt( m, Integer.MIN_VALUE );
        testInt( m, Integer.MAX_VALUE );        
        }

    public void testLongLiterals()
        {
        Model m = getModel();
        testLong( m, 0 );
        testLong( m, -1 );
        testLong( m, Long.MIN_VALUE );
        testLong( m, Long.MAX_VALUE );        
        }
    
    public void testFloatLiterals()
        {
        Model m = getModel();
        testFloat( m, 0.0f );
        testFloat( m, 1.0f );
        testFloat( m, -1.0f );
        testFloat( m, 12345.6789f );
        testFloat( m, Float.MIN_VALUE );
        testFloat( m, Float.MAX_VALUE );
        }
    
    public void testDoubleLiterals()
        {
        Model m = getModel();
        testDouble( m, 0.0 );
        testDouble( m, 1.0 );
        testDouble( m, -1.0 );
        testDouble( m, 12345.678901 );
        testDouble( m, Double.MIN_VALUE );
        testDouble( m, Double.MAX_VALUE );
        }
    
    public void testCharacterLiterals()
        {
        Model m = getModel();
        testCharacter( m, 'A' );
        testCharacter( m, 'a' );
        testCharacter( m, '#' );
        testCharacter( m, '@' );
        testCharacter( m, '0' );
        testCharacter( m, '9' );
        testCharacter( m, '\u1234' );
        testCharacter( m, '\u5678' );
        }    
    
    public void testPlainStringLiterals()
        {
        Model m = getModel();
        testPlainString( m, "" );
        testPlainString( m, "A test string" );
        testPlainString( m, "Another test string" );
        }
    
    public void testLanguagedStringLiterals()
        {
        Model m = getModel();
        testLanguagedString( m, "", "en" );
        testLanguagedString( m, "chat", "fr" );
        }
    
    public void testStringLiteralEquality()
        {
        Model m = getModel();
        assertEquals( m.createLiteral( "A" ), m.createLiteral( "A" ) );
        assertEquals( m.createLiteral( "Alpha" ), m.createLiteral( "Alpha" ) );
        assertDiffer( m.createLiteral( "Alpha" ), m.createLiteral( "Beta" ) );
        assertDiffer( m.createLiteral( "A", "en" ), m.createLiteral( "A" ) );
        assertDiffer( m.createLiteral( "A" ), m.createLiteral( "A", "en" ) );
        assertDiffer( m.createLiteral( "A", "en" ), m.createLiteral( "A", "fr" ) );
        assertEquals( m.createLiteral( "A", "en" ), m.createLiteral( "A", "en" ) );
        }

//    public void testLiteralObjects()
//        {
//        Model m = getModel();
////        testLiteralObject( m, 0 );
////        testLiteralObject( m, 12345 );
////        testLiteralObject( m, -67890 );
//        }

    protected void testByte( Model m, byte tv )
        {
        Literal l = m.createTypedLiteral( tv );
        assertEquals( tv, l.getByte() );
        assertEquals( tv, l.getShort() );
        assertEquals( tv, l.getInt() );
        assertEquals( tv, l.getLong() );
        }
    
    protected void testShort( Model m, short tv )
        {
        Literal l = m.createTypedLiteral( tv );
        try { assertEquals( tv, l.getByte() ); assertInRange( Byte.MIN_VALUE, tv, Byte.MAX_VALUE ); }
        catch (NumberFormatException e) { assertOutsideRange( Byte.MIN_VALUE, tv, Byte.MAX_VALUE ); }
        catch (IllegalArgumentException e) { assertOutsideRange( Byte.MIN_VALUE, tv, Byte.MAX_VALUE ); }
        assertEquals( tv, l.getShort() );
        assertEquals( tv, l.getInt() );
        assertEquals( tv, l.getLong() );
        }  
    
    protected void testInt( Model m, int tv )
        {
        Literal l = m.createTypedLiteral( tv );
        try { assertEquals( tv, l.getByte() ); assertInRange( Byte.MIN_VALUE, tv, Byte.MAX_VALUE ); }
        catch (NumberFormatException e) { assertOutsideRange( Byte.MIN_VALUE, tv, Byte.MAX_VALUE ); }
        catch (IllegalArgumentException e) { assertOutsideRange( Byte.MIN_VALUE, tv, Byte.MAX_VALUE ); }
        try { assertEquals( tv, l.getShort() ); assertInRange( Short.MIN_VALUE, tv, Short.MAX_VALUE ); }
        catch (NumberFormatException e) { assertOutsideRange( Short.MIN_VALUE, tv, Short.MAX_VALUE ); }
        catch (IllegalArgumentException e) { assertOutsideRange( Short.MIN_VALUE, tv, Short.MAX_VALUE ); }
        assertEquals( tv, l.getInt() );
        assertEquals( tv, l.getLong() );
        }
    
    protected void testLong( Model m, long tv )
        {
        Literal l = m.createTypedLiteral( tv );
        try { assertEquals( tv, l.getByte() ); assertInRange( Byte.MIN_VALUE, tv, Byte.MAX_VALUE ); }
        catch (NumberFormatException e) { assertOutsideRange( Byte.MIN_VALUE, tv, Byte.MAX_VALUE ); }
        catch (IllegalArgumentException e) { assertOutsideRange( Byte.MIN_VALUE, tv, Byte.MAX_VALUE ); }
        try { assertEquals( tv, l.getShort() ); assertInRange( Short.MIN_VALUE, tv, Short.MAX_VALUE ); }
        catch (NumberFormatException e) { assertOutsideRange( Short.MIN_VALUE, tv, Short.MAX_VALUE ); }
        catch (IllegalArgumentException e) { assertOutsideRange( Short.MIN_VALUE, tv, Short.MAX_VALUE ); }
        try { assertEquals( tv, l.getInt() ); assertInRange( Integer.MIN_VALUE, tv, Integer.MAX_VALUE ); }
        catch (NumberFormatException e) { assertOutsideRange( Integer.MIN_VALUE, tv, Integer.MAX_VALUE ); }
        catch (IllegalArgumentException e) { assertOutsideRange( Integer.MIN_VALUE, tv, Integer.MAX_VALUE ); }
        assertEquals( tv, l.getLong() );
        }
    
    protected void assertOutsideRange( long min, long x, long max )
        {
        if (min <= x && x <= max)
            fail( "inside range: " + x + " min: " + min + " max: " + max );
        }
    
    protected void assertInRange( long min, long x, long max )
        {
        if (min <= x && x <= max)
            return;
        else
            fail( "outside range: " + x + " min: " + min + " max: " + max );
        }
    
    protected void testFloat( Model m, float tv )
        {
        assertEquals( tv, m.createTypedLiteral( tv ).getFloat(), fDelta );
        }
    
    protected void testDouble( Model m, double tv )
        {
        final double delta = 0.000000005;
        assertEquals( tv, m.createTypedLiteral( tv ).getDouble(), dDelta );
        }
    
    protected void testCharacter( Model m, char tv )
        {
        assertEquals( tv, m.createTypedLiteral( tv ).getChar() );
        }

    protected void testLanguagedString( Model m, String tv, String lang )
        {
        Literal l = m.createLiteral( tv, lang );
        assertEquals( tv, l.getString() );
        assertEquals( tv, l.getLexicalForm() );
        assertEquals( lang, l.getLanguage() );
        }

    protected void testPlainString( Model m, String tv )
        { 
        Literal l = m.createLiteral( tv );
        assertEquals( tv, l.getString() );
        assertEquals( tv, l.getLexicalForm() );
        assertEquals( "", l.getLanguage() );
        }

//    protected void testLiteralObject( Model m, int x )
//        {
//        LitTestObj tv = new LitTestObj( x );
//        LitTestObjF factory = new LitTestObjF();
//        assertEquals( tv, m.createTypedLiteral( tv ).getObject( factory ) );
//        }
    }
