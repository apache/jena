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

package com.hp.hpl.jena.graph.test;

import java.io.FileReader;
import java.io.IOException;
import java.math.BigDecimal ;
import java.math.BigInteger ;
import java.text.SimpleDateFormat ;
import java.util.* ;

import junit.framework.TestCase ;
import junit.framework.TestSuite ;
import org.apache.xerces.impl.dv.util.HexBin ;
import org.junit.Assert ;

import com.hp.hpl.jena.datatypes.BaseDatatype ;
import com.hp.hpl.jena.datatypes.DatatypeFormatException ;
import com.hp.hpl.jena.datatypes.RDFDatatype ;
import com.hp.hpl.jena.datatypes.TypeMapper ;
import com.hp.hpl.jena.datatypes.xsd.* ;
import com.hp.hpl.jena.datatypes.xsd.impl.XMLLiteralType ;
import com.hp.hpl.jena.enhanced.EnhNode ;
import com.hp.hpl.jena.graph.* ;
import com.hp.hpl.jena.graph.impl.LiteralLabel ;
import com.hp.hpl.jena.graph.impl.LiteralLabelFactory ;
import com.hp.hpl.jena.rdf.model.* ;
import com.hp.hpl.jena.shared.impl.JenaParameters ;
import com.hp.hpl.jena.vocabulary.XSD ;
   
/**
 * Unit test for the typed literal machinery - including RDFDatatype,
 * TypeMapper and LiteralLabel.
 */
public class TestTypedLiterals extends TestCase {
              
    /** dummy model used as a literal factory */
    private Model m = ModelFactory.createDefaultModel();
    
    // Temporary for debug
    /*
    static {
        Locale.setDefault(Locale.ITALY);
        TimeZone.setDefault(TimeZone.getTimeZone("CEST"));
    }
    */
    
    /**
     * Boilerplate for junit
     */ 
    public TestTypedLiterals( String name ) {
        super( name ); 
    }
    
    /**
     * This is its own test suite
     */
    public static TestSuite suite() {
        return new TestSuite( TestTypedLiterals.class ); 
    }  
    
    /**
     * Test the base functioning of unknown datatypes
     */
    public void testUnknown() {
        String typeURI = "urn:x-hp-dt:unknown";
        String typeURI2 = "urn:x-hp-dt:unknown2";

        boolean originalFlag = JenaParameters.enableSilentAcceptanceOfUnknownDatatypes;
        JenaParameters.enableSilentAcceptanceOfUnknownDatatypes = true;
        Literal l1 = m.createTypedLiteral("foo", typeURI);
        Literal l3 = m.createTypedLiteral("15", typeURI);
        Literal l5 = m.createTypedLiteral("foo", typeURI2);
        Literal l6 = m.createLiteral("foo", "lang1");
        JenaParameters.enableSilentAcceptanceOfUnknownDatatypes = originalFlag;
        // Check for successful creation
        
        assertNotNull(l1);
        assertNotNull(l3);
        assertNotNull(l5);
        
        // check equality function
        assertDiffer("datatype sensitive", l1, l5);
        assertDiffer("value sensitive", l1, l3);
        assertDiffer("typed and plain differ", l1, l6);

        // Check typed accessors
        try {
            l3.getInt();
            assertTrue("Allowed int conversion", false);
        } catch (DatatypeFormatException e) {}
        assertEquals("Extract value", l1.getValue(), new BaseDatatype.TypedValue("foo", typeURI));
        assertEquals("Extract xml tag", l1.isWellFormedXML(), false);
        
        JenaParameters.enableSilentAcceptanceOfUnknownDatatypes = false;
        boolean foundException = false;
        try {
            m.createTypedLiteral("food", typeURI+"3");
        } catch (DatatypeFormatException e2) {
            foundException = true;
        }
        JenaParameters.enableSilentAcceptanceOfUnknownDatatypes = originalFlag;
        assertTrue("Detected unknown datatype", foundException);
        
        // Check we can create a literal of an unregistered java type without anything blowing up
        Object foo = new java.sql.Date(123456l);
        LiteralLabel ll = LiteralLabelFactory.create(foo);
        assertEquals(ll.getLexicalForm(), foo.toString());
    }
    
    /**
     * Tests the base functioning of a user defined datatype
     */
    public void testUserDef() {
        // Register the user defined type for rationals
        RDFDatatype rtype = RationalType.theRationalType;
        TypeMapper.getInstance().registerDatatype(rtype);


        Literal l1 = m.createTypedLiteral("3/5", rtype);
        Literal l3 = m.createTypedLiteral("7/5", rtype);
        
        // Check for successful creation
        assertNotNull(l1);
        assertNotNull(l3);
        
        // check equality function
        assertDiffer("values should be tested!", l1, l3);

        // Check typed accessors
        assertSame("Datatype incorrect", l1.getDatatype(), rtype);
        assertEquals("Datatype uri incorrect", l1.getDatatypeURI(), RationalType.theTypeURI);
        Object val = l1.getValue();
        assertTrue("Value space check", val instanceof Rational);
        assertTrue("Value check", ((Rational)val).getNumerator() == 3);
        assertTrue("Value check", ((Rational)val).getDenominator() == 5);
        try {
            l1.getInt();
            assertTrue("Allowed int conversion", false);
        } catch (DatatypeFormatException e) {}
        assertEquals("Extract xml tag", l1.isWellFormedXML(), false);
    }
    
    /**
     * Test user defined data types.
     * This is based on a corrected, modified version of an early DAML+OIL example
     * but is not specific to DAML+OIL.
     */
    public void testUserDefined() throws IOException {
        String uri = "http://www.daml.org/2001/03/daml+oil-ex-dt";
        String filename = "testing/xsd/daml+oil-ex-dt.xsd";
        TypeMapper tm = TypeMapper.getInstance();
        List<String> typenames = XSDDatatype.loadUserDefined(uri, new FileReader(filename), null, tm);
        assertIteratorValues(typenames.iterator(), new Object[] {
            uri + "#XSDEnumerationHeight",
            uri + "#over12",
            uri + "#over17",
            uri + "#over59",
            uri + "#clothingsize"   });
        
        // Check the string restriction
        RDFDatatype heightType = tm.getSafeTypeByName(uri + "#XSDEnumerationHeight");
        checkLegalLiteral("short", heightType, String.class, "short");
        checkLegalLiteral("tall", heightType, String.class, "tall");
        checkIllegalLiteral("shortish", heightType);

        // Check the numeric restriction
        RDFDatatype over12Type = tm.getSafeTypeByName(uri + "#over12");
        checkLegalLiteral("15", over12Type, Integer.class, 15 );
        checkIllegalLiteral("12", over12Type);
        
        // Check the union type
        RDFDatatype clothingsize = tm.getSafeTypeByName(uri + "#clothingsize");
        checkLegalLiteral("42", clothingsize, Integer.class, 42 );
        checkLegalLiteral("short", clothingsize, String.class, "short");
        
        // Check use of isValidLiteral for base versus derived combinations
        LiteralLabel iOver12 = m.createTypedLiteral("13", over12Type).asNode().getLiteral();
        LiteralLabel iDecimal14 = m.createTypedLiteral("14", XSDDatatype.XSDdecimal).asNode().getLiteral();
        LiteralLabel iDecimal10 = m.createTypedLiteral("10", XSDDatatype.XSDdecimal).asNode().getLiteral();
        LiteralLabel iString = m.createTypedLiteral("15", XSDDatatype.XSDstring).asNode().getLiteral();
        LiteralLabel iPlain = m.createLiteral("foo").asNode().getLiteral();
        
        assertTrue(over12Type.isValidLiteral(iOver12));
        assertTrue(over12Type.isValidLiteral(iDecimal14));
        assertTrue( ! over12Type.isValidLiteral(iDecimal10));
        assertTrue( ! over12Type.isValidLiteral(iString));
        assertTrue( ! over12Type.isValidLiteral(iPlain));
        
        assertTrue(XSDDatatype.XSDdecimal.isValidLiteral(iOver12));
        assertTrue(XSDDatatype.XSDdecimal.isValidLiteral(iDecimal14));
        assertTrue(XSDDatatype.XSDdecimal.isValidLiteral(iDecimal10));
        assertTrue( ! XSDDatatype.XSDdecimal.isValidLiteral(iString));
        assertTrue( ! XSDDatatype.XSDdecimal.isValidLiteral(iPlain));
        
        assertTrue(XSDDatatype.XSDstring.isValidLiteral(iString));
        assertTrue(XSDDatatype.XSDstring.isValidLiteral(iPlain));
        assertTrue( ! XSDDatatype.XSDstring.isValidLiteral(iOver12));
        assertTrue( ! XSDDatatype.XSDstring.isValidLiteral(iDecimal10));
        assertTrue( ! XSDDatatype.XSDstring.isValidLiteral(iDecimal14));
    }
    
    public void testXMLLiteral() {
    	Literal ll;
    	
    	ll = m.createLiteral("<bad",true);
    	
    	assertTrue("Error checking must be off.",((EnhNode)ll).asNode().getLiteralIsXML());
		ll = m.createTypedLiteral("<bad/>",XMLLiteralType.theXMLLiteralType);
		assertFalse("Error checking must be on.",((EnhNode)ll).asNode().getLiteralIsXML());
		ll = m.createTypedLiteral("<good></good>",XMLLiteralType.theXMLLiteralType);
		assertTrue("Well-formed XMLLiteral.",((EnhNode)ll).asNode().getLiteralIsXML());
    
    }

    /**
     * Tests basic XSD integer types()
     */
    public void testXSDbasics() {
        String xsdIntURI = "http://www.w3.org/2001/XMLSchema#int";
        
        // Check int and basic equality processing
        Literal l1 = m.createTypedLiteral(42);  // default map
        Literal l2 = m.createTypedLiteral("42", XSDDatatype.XSDint);
        Literal l4 = m.createTypedLiteral("63");  // default map
        
        assertSameValueAs("Default map failed", l1, l2);
        assertEquals("Value wrong", l1.getValue(), new Integer(42));
        assertEquals("class wrong", l1.getValue().getClass(), Integer.class);
        assertEquals("Value accessor problem", l1.getInt(), 42);
        assertEquals("wrong type name", l2.getDatatypeURI(), xsdIntURI);
        assertEquals("wrong type", l2.getDatatype(), XSDDatatype.XSDint);
        assertDiffer("Not value sensitive", l1, l4);
        checkIllegalLiteral("zap", XSDDatatype.XSDint);
        checkIllegalLiteral("42.1", XSDDatatype.XSDint);
        
        Literal l5 = m.createTypedLiteral("42", XSDDatatype.XSDnonNegativeInteger);
        assertSameValueAs("type coercion", l2, l5);
        
        // Check float/double
        l1 = m.createTypedLiteral(42.42);  // default map
        l2 = m.createTypedLiteral("42.42", XSDDatatype.XSDfloat);
        Literal l3 = m.createTypedLiteral("42.42", XSDDatatype.XSDdouble);
        
        assertEquals("class wrong", l1.getValue().getClass(), Double.class);
        assertFloatEquals("value wrong", ((Double)(l1.getValue())).floatValue(), 42.42);
        assertEquals("class wrong", l2.getValue().getClass(), Float.class);
        assertFloatEquals("value wrong", ((Float)(l2.getValue())).floatValue(), 42.42);
        assertFloatEquals("Value accessor problem", l1.getFloat(), 42.42);
        assertEquals("wrong type", l2.getDatatype(), XSDDatatype.XSDfloat);
        assertSameValueAs("equality fn", l1, l3);
        
        // Minimal check on long, short, byte
        checkLegalLiteral("12345", XSDDatatype.XSDlong, Integer.class, 12345 );
        checkLegalLiteral("-12345", XSDDatatype.XSDlong, Integer.class, -12345 );
        checkIllegalLiteral("2.3", XSDDatatype.XSDlong);
        
        checkLegalLiteral("1234", XSDDatatype.XSDshort, Integer.class, (int) (short) 1234 );
        checkLegalLiteral("-1234", XSDDatatype.XSDshort, Integer.class, (int) (short) -1234 );
        checkLegalLiteral("32767", XSDDatatype.XSDshort, Integer.class, (int) (short) 32767 );
        checkLegalLiteral("-32768", XSDDatatype.XSDshort, Integer.class, (int) (short) -32768 );
        checkIllegalLiteral("32769", XSDDatatype.XSDshort);
        checkIllegalLiteral("2.3", XSDDatatype.XSDshort);

        checkLegalLiteral("42", XSDDatatype.XSDbyte, Integer.class, (int) (byte) 42 );
        checkLegalLiteral("-42", XSDDatatype.XSDbyte, Integer.class, (int) (byte) -42 );
        checkLegalLiteral("127", XSDDatatype.XSDbyte, Integer.class, (int) (byte) 127 );
        checkLegalLiteral("-128", XSDDatatype.XSDbyte, Integer.class, (int) (byte) -128 );
        checkIllegalLiteral("32769", XSDDatatype.XSDbyte);
        checkIllegalLiteral("128", XSDDatatype.XSDbyte);
        checkIllegalLiteral("2.3", XSDDatatype.XSDbyte);
        
        // Minimal check on unsigned normal types
        checkLegalLiteral("12345", XSDDatatype.XSDunsignedLong, Integer.class, 12345 );
        checkLegalLiteral("+12345", XSDDatatype.XSDunsignedLong, Integer.class, 12345 );
        checkLegalLiteral("9223372036854775808", XSDDatatype.XSDunsignedLong, BigInteger.class, new BigInteger("9223372036854775808"));
        checkIllegalLiteral("-12345", XSDDatatype.XSDunsignedLong);
        
        checkLegalLiteral("12345", XSDDatatype.XSDunsignedInt, Integer.class, 12345 );
        checkLegalLiteral("2147483648", XSDDatatype.XSDunsignedInt, Long.class, 2147483648l );
        checkIllegalLiteral("-12345", XSDDatatype.XSDunsignedInt);
        
        checkLegalLiteral("1234", XSDDatatype.XSDunsignedShort, Integer.class, 1234 );
        checkLegalLiteral("32679", XSDDatatype.XSDunsignedShort, Integer.class, 32679 );
        checkIllegalLiteral("-12345", XSDDatatype.XSDunsignedShort);
        
        checkLegalLiteral("123", XSDDatatype.XSDunsignedByte, Integer.class, (int) (short) 123 );
        checkLegalLiteral("129", XSDDatatype.XSDunsignedByte, Integer.class, (int) (short) 129 );
        checkIllegalLiteral("-123", XSDDatatype.XSDunsignedByte);
        
        // Minimal check on the big num types
        checkLegalLiteral("12345", XSDDatatype.XSDinteger, Integer.class, 12345 );
        checkLegalLiteral("0", XSDDatatype.XSDinteger, Integer.class, 0 );
        checkLegalLiteral("-12345", XSDDatatype.XSDinteger, Integer.class, -12345 );
        checkLegalLiteral("9223372036854775808", XSDDatatype.XSDinteger, BigInteger.class, new BigInteger("9223372036854775808"));
        
        checkLegalLiteral("12345", XSDDatatype.XSDpositiveInteger, Integer.class, 12345 );
        checkIllegalLiteral("0", XSDDatatype.XSDpositiveInteger);
        checkIllegalLiteral("-12345", XSDDatatype.XSDpositiveInteger);
        checkLegalLiteral("9223372036854775808", XSDDatatype.XSDpositiveInteger, BigInteger.class, new BigInteger("9223372036854775808"));
        
        checkLegalLiteral("12345", XSDDatatype.XSDnonNegativeInteger, Integer.class, 12345 );
        checkLegalLiteral("0", XSDDatatype.XSDnonNegativeInteger, Integer.class, 0 );
        checkIllegalLiteral("-12345", XSDDatatype.XSDnonNegativeInteger);
        checkLegalLiteral("9223372036854775808", XSDDatatype.XSDnonNegativeInteger, BigInteger.class, new BigInteger("9223372036854775808"));
        
        checkLegalLiteral("-12345", XSDDatatype.XSDnegativeInteger, Integer.class, -12345 );
        checkIllegalLiteral("0", XSDDatatype.XSDnegativeInteger);
        checkIllegalLiteral("12345", XSDDatatype.XSDnegativeInteger);
        checkLegalLiteral("-9223372036854775808", XSDDatatype.XSDnegativeInteger, BigInteger.class, new BigInteger("-9223372036854775808"));
        
        checkLegalLiteral("-12345", XSDDatatype.XSDnonPositiveInteger, Integer.class, -12345 );
        checkLegalLiteral("0", XSDDatatype.XSDnonPositiveInteger, Integer.class, 0 );
        checkIllegalLiteral("12345", XSDDatatype.XSDnonPositiveInteger);
        checkLegalLiteral("-9223372036854775808", XSDDatatype.XSDnonPositiveInteger, BigInteger.class, new BigInteger("-9223372036854775808"));
        
        checkLegalLiteral("12345", XSDDatatype.XSDdecimal, Integer.class, new Integer("12345"));
        checkLegalLiteral("0.0", XSDDatatype.XSDdecimal, Integer.class, new Integer("0"));
        checkLegalLiteral("42.45", XSDDatatype.XSDdecimal, BigDecimal.class, new BigDecimal("42.45"));
        checkLegalLiteral("9223372036854775808.1234", XSDDatatype.XSDdecimal, BigDecimal.class, new BigDecimal("9223372036854775808.1234"));
        checkLegalLiteral("123.4", XSDDatatype.XSDdecimal, BigDecimal.class, new BigDecimal("123.4"));
        checkIllegalLiteral("123,4", XSDDatatype.XSDdecimal);
        
        // Booleans
        checkLegalLiteral("true", XSDDatatype.XSDboolean, Boolean.class, true );
        checkLegalLiteral("false", XSDDatatype.XSDboolean, Boolean.class, false );
        l1 = m.createTypedLiteral(true);
        assertEquals("boolean mapping", XSDDatatype.XSDboolean, l1.getDatatype());
        
        // String types
        checkLegalLiteral("hello world", XSDDatatype.XSDstring, String.class, "hello world");
        l1 = m.createTypedLiteral("foo bar");
        assertEquals("string mapping", XSDDatatype.XSDstring, l1.getDatatype());
        
    }
    
    /**
     * Some selected equality tests which caused problems in WG tests
     */
    public void testMiscEquality() {
        Literal l1 = m.createTypedLiteral("10", "http://www.w3.org/2001/XMLSchema#integer");
        Literal l3 = m.createTypedLiteral("010", "http://www.w3.org/2001/XMLSchema#integer");
        assertSameValueAs("Int lex form", l1, l3);
        
        l1 = m.createTypedLiteral("1", XSDDatatype.XSDint);
        l3 = m.createTypedLiteral("1", XSDDatatype.XSDnonNegativeInteger);
        
        assertSameValueAs("numeric comparisons", l1, l3);
    }
    
    /**
     * Check that creating a typed literal from an object traps the interesting 
     * special cases of String and Calendar.
     */
    public void testOverloads() {
        // First case string overloads an explicit type
        boolean old = JenaParameters.enableEagerLiteralValidation;
        try {
            JenaParameters.enableEagerLiteralValidation = true;
            
            // String overloading cases
            boolean test1 = false;
            try {
                m.createTypedLiteral("foo", "http://www.w3.org/2001/XMLSchema#integer");
            } catch (DatatypeFormatException e1 ) {
                test1 = true;
            }
            assertTrue("detected illegal string, direct", test1);
            
            boolean test2 = false;
            try {
                Object foo = "foo";
                m.createTypedLiteral(foo, "http://www.w3.org/2001/XMLSchema#integer");
            } catch (DatatypeFormatException e2 ) {
                test2 = true;
            }
            assertTrue("detected illegal string, overloaded", test2);
            
            // Overloading of calendar convenience functions
            Calendar testCal = new GregorianCalendar(TimeZone.getTimeZone("GMT"));
            testCal.set(1999, 4, 30, 15, 9, 32);
            testCal.set(Calendar.MILLISECOND, 0);   // ms field can be undefined on Linux
            Literal lc = m.createTypedLiteral((Object)testCal);
            assertEquals("calendar overloading test", m.createTypedLiteral("1999-05-30T15:09:32Z", XSDDatatype.XSDdateTime), lc );
            
        } finally {
            JenaParameters.enableEagerLiteralValidation = old;
        }
    }
    
    /**
     * Test plain literal/xsd:string/xsd:int equality operations
     */
    public void testPlainSameValueAs() {
        Literal lString = m.createTypedLiteral("10", XSDDatatype.XSDstring );
        Literal lPlain = m.createTypedLiteral("10", (RDFDatatype)null );
        Literal lPlain3 = m.createTypedLiteral("10", (String)null );
        Literal lPlain2 = m.createLiteral("10");
        Literal lInt =  m.createTypedLiteral("10", XSDDatatype.XSDint );
        
        assertSameValueAs("Null type = plain literal", lPlain, lPlain2);
        assertSameValueAs("Null type = plain literal", lPlain, lPlain3);
        assertSameValueAs("Null type = plain literal", lPlain2, lPlain3);
        assertTrue("null type", lPlain3.getDatatype() == null);
        assertDiffer("String != int", lString, lInt);
        assertDiffer("Plain != int", lPlain, lInt);
        assertDiffer("Plain != int", lPlain2, lInt);
        
        // The correct answer to this is currently up to us
        if (JenaParameters.enablePlainLiteralSameAsString) {
            assertSameValueAs("String != plain??", lString, lPlain);
            assertSameValueAs("String != plain??", lString, lPlain2);
        } else {
            assertDiffer("String != plain??", lString, lPlain);
            assertDiffer("String != plain??", lString, lPlain2);
        }
        
    }
    
    /**
     * Test cases of numeric comparison.
     */
    public void testNumberSameValueAs() {
        Literal lDouble = m.createTypedLiteral("5", XSDDatatype.XSDdouble);
        Literal lDouble2 = m.createTypedLiteral("5.5", XSDDatatype.XSDdouble);
        Literal lDouble3 = m.createTypedLiteral("5.5", XSDDatatype.XSDdouble);
        Literal lDouble4 = m.createTypedLiteral("5.6", XSDDatatype.XSDdouble);
        Literal lFloat = m.createTypedLiteral("5", XSDDatatype.XSDfloat);
        Literal lint = m.createTypedLiteral("5", XSDDatatype.XSDint);
        Literal linteger = m.createTypedLiteral("5", XSDDatatype.XSDinteger);
        Literal lbyte = m.createTypedLiteral("5", XSDDatatype.XSDbyte);
        
        assertSameValueAs("integer subclasses equal", lint, linteger);
        assertSameValueAs("integer subclasses equal", lint, lbyte);
        assertSameValueAs("integer subclasses equal", linteger, lbyte);
        assertSameValueAs("double equality", lDouble2, lDouble3);
        
        assertDiffer("float/double/int distinct", lDouble, lDouble2);
        assertDiffer("float/double/int distinct", lDouble, lFloat);
        assertDiffer("float/double/int distinct", lDouble, lint);
        assertDiffer("float/double/int distinct", lDouble, linteger);
        assertDiffer("float/double/int distinct", lDouble2, lint);
        assertDiffer("float/double/int distinct", lDouble2, lbyte);
        assertDiffer("float/double/int distinct", lint, lDouble);
        assertDiffer("float/double/int distinct", lbyte, lDouble);
        assertDiffer("float/double/int distinct", lint, lDouble2);
        assertDiffer("float/double/int distinct", lbyte, lDouble2);
        
        assertDiffer("double inequality", lDouble3, lDouble4);
        assertDiffer("double inequality", lDouble2, lDouble);
        
        // Check decimals
        Literal lDecimal = m.createTypedLiteral("5.5", XSDDatatype.XSDdecimal);
        Literal lDecimal2 = m.createTypedLiteral("5.6", XSDDatatype.XSDdecimal);
        assertDiffer("decimal inequality", lDecimal, lDecimal2);
    }
    
    /**
     * Check basic handling of big integers and decimals
     */
    public void testBigNums() {
        Literal l1 = m.createTypedLiteral("12345678901234567890", XSDDatatype.XSDinteger);
        Literal l2 = m.createTypedLiteral("12345678901234567891", XSDDatatype.XSDinteger);
        assertDiffer("Big integer equality", l1, l2);
        
        BigInteger bigint1 = new BigInteger("12345678901234567890");
        Literal lb1 = m.createTypedLiteral(bigint1, XSDDatatype.XSDinteger);
        assertSameValueAs("big integer creation equality", l1, lb1);
        
        BigDecimal bigdec1 = new BigDecimal("12345678901234567890.00");
        Literal ld1 = m.createTypedLiteral(bigdec1, XSDDatatype.XSDdecimal);
        BigDecimal bigdec1b = new BigDecimal("12345678901234567890.0");
        Literal ld1b = m.createTypedLiteral(bigdec1b, XSDDatatype.XSDdecimal);
        BigDecimal bigdec2 = new BigDecimal("12345678901234567890.1");
        Literal ld2 = m.createTypedLiteral(bigdec2, XSDDatatype.XSDdecimal);
        assertSameValueAs("big decimal equality check", ld1, ld1b);
        assertSameValueAs("big decimal equality check", ld1, lb1);
        assertDiffer("Decimal equality", ld1, ld2);
        
        BigDecimal bigdecF = new BigDecimal("12345678901234567890.1");
        Literal ldF = m.createTypedLiteral(bigdecF, XSDDatatype.XSDdecimal);
        BigDecimal bigdecFb = new BigDecimal("12345678901234567890.10");
        Literal ldFb = m.createTypedLiteral(bigdecFb, XSDDatatype.XSDdecimal);
        BigDecimal bigdecF2 = new BigDecimal("12345678901234567890.2");
        Literal ldF2 = m.createTypedLiteral(bigdecF2, XSDDatatype.XSDdecimal);
        assertSameValueAs("big decimal equality check", ldF, ldFb);
        assertDiffer("Decimal equality", ldF, ldF2);
    }
    
    /**
     * Test case for a bug in retrieving a value like 3.00 from
     * a probe like 3.0
     */
    public void testDecimalFind() {
        RDFDatatype dt = XSDDatatype.XSDdecimal;
        Node ns = NodeFactory.createURI("x") ;
        Node np = NodeFactory.createURI("p") ;
        Node nx1 = NodeFactory.createLiteral("0.50", null, dt) ;
        Node nx2 = NodeFactory.createLiteral("0.500", null, dt) ;
        Graph graph = Factory.createDefaultGraph() ;
        graph.add(new Triple(ns, np, nx1)) ;
        assertTrue( graph.find(Node.ANY, Node.ANY, nx2).hasNext() );  
    }
    
    /**
     * Test the internal machinery of decimal normalization directly
     */
    public void testDecimalCanonicalize() {
        doTestDecimalCanonicalize("0.500", "0.5", BigDecimal.class);
        doTestDecimalCanonicalize("0.50", "0.5", BigDecimal.class);
        doTestDecimalCanonicalize("0.5", "0.5", BigDecimal.class);
        doTestDecimalCanonicalize("0.0", "0", Integer.class);
        doTestDecimalCanonicalize("5.0", "5", Integer.class);
        doTestDecimalCanonicalize("500.0", "500", Integer.class);
        doTestDecimalCanonicalize("5.00100", "5.001", BigDecimal.class);
    }
    
    /**
     * Helper for testDecimalCannonicalize. Run a single
     * cannonicalization test on a value specified in string form.
     */
    private void doTestDecimalCanonicalize(String value, String expected, Class<?> expectedClass) {
        Object normalized = XSDDatatype.XSDdecimal.cannonicalise( new BigDecimal(value) );
        assertEquals(expected, normalized.toString());
        assertEquals(expectedClass, normalized.getClass());
    }
    
    /**
     * Test data/time wrappers
     */
    public void testDateTime() {
        // Duration
        Literal l1 = m.createTypedLiteral("P1Y2M3DT5H6M7.50S", XSDDatatype.XSDduration);
        assertEquals("duration data type", XSDDatatype.XSDduration, l1.getDatatype());
        assertEquals("duration java type", XSDDuration.class, l1.getValue().getClass());
        assertEquals("duration value", 1, ((XSDDuration)l1.getValue()).getYears());
        assertEquals("duration value", 2, ((XSDDuration)l1.getValue()).getMonths());
        assertEquals("duration value", 3, ((XSDDuration)l1.getValue()).getDays());
        assertEquals("duration value", 5, ((XSDDuration)l1.getValue()).getHours());
        assertEquals("duration value", 6, ((XSDDuration)l1.getValue()).getMinutes());
        assertEquals("duration value", 7, ((XSDDuration)l1.getValue()).getFullSeconds());
        assertEquals("duration value", BigDecimal.valueOf(75,1), ((XSDDuration)l1.getValue()).getBigSeconds());
        assertFloatEquals("duration value", 18367.5, ((XSDDuration)l1.getValue()).getTimePart());
        assertEquals("serialization", "P1Y2M3DT5H6M7.5S", l1.getValue().toString());
        assertTrue("equality test", l1.sameValueAs( m.createTypedLiteral("P1Y2M3DT5H6M7.5S", XSDDatatype.XSDduration) ) );
        assertTrue("inequality test", l1 != m.createTypedLiteral("P1Y2M2DT5H6M7.5S", XSDDatatype.XSDduration));
        
        l1 = m.createTypedLiteral("P1Y2M3DT5H0M", XSDDatatype.XSDduration);
        assertEquals("serialization", "P1Y2M3DT5H", l1.getValue().toString());
        
        l1 = m.createTypedLiteral("P1Y", XSDDatatype.XSDduration);
        assertEquals("duration data type", XSDDatatype.XSDduration, l1.getDatatype());
        assertEquals("duration java type", XSDDuration.class, l1.getValue().getClass());
        assertEquals("duration value", 1, ((XSDDuration)l1.getValue()).getYears());
        assertEquals("serialization", "P1Y", l1.getValue().toString());
        assertTrue("equality test", l1.sameValueAs( m.createTypedLiteral("P1Y", XSDDatatype.XSDduration) ) );
        assertTrue("inequality test", l1 != m.createTypedLiteral("P1Y", XSDDatatype.XSDduration));

        l1 = m.createTypedLiteral("-P120D", XSDDatatype.XSDduration);
        Literal l2 = m.createTypedLiteral( l1.getValue() );
        assertEquals("-P120D", l2.getLexicalForm() );

        // Duration equality bug
        Literal d1 = m.createTypedLiteral("PT1H1M1S", XSDDatatype.XSDduration);
        Literal d2 = m.createTypedLiteral("PT1H1M1.1S", XSDDatatype.XSDduration);
        assertTrue("duration compare", !d1.sameValueAs(d2));
        XSDDuration dur1 = (XSDDuration)d1.getValue() ;
        XSDDuration dur2 = (XSDDuration)d2.getValue() ;
        assertEquals("duration compare order", 1, dur2.compare(dur1)) ;
        
        // dateTime
        l1 = m.createTypedLiteral("1999-05-31T02:09:32Z", XSDDatatype.XSDdateTime);
        XSDDateTime xdt = (XSDDateTime)l1.getValue();
        assertEquals("dateTime data type", XSDDatatype.XSDdateTime, l1.getDatatype());
        assertEquals("dateTime java type", XSDDateTime.class, l1.getValue().getClass());
        assertEquals("dateTime value", 1999, xdt.getYears());
        assertEquals("dateTime value", 5, xdt.getMonths());
        assertEquals("dateTime value", 31, xdt.getDays());
        assertEquals("dateTime value", 2, xdt.getHours());
        assertEquals("dateTime value", 9, xdt.getMinutes());
        assertEquals("dateTime value", 32, xdt.getFullSeconds());
        assertEquals("serialization", "1999-05-31T02:09:32Z", l1.getValue().toString());
        Calendar cal = xdt.asCalendar();
        Calendar testCal = new GregorianCalendar(TimeZone.getTimeZone("GMT"));
        testCal.set(1999, 4, 31, 2, 9, 32);
        /*
        assertEquals("calendar value", cal.get(Calendar.YEAR), testCal.get(Calendar.YEAR) );
        assertEquals("calendar value", cal.get(Calendar.MONTH), testCal.get(Calendar.MONTH) );
        assertEquals("calendar value", cal.get(Calendar.DATE), testCal.get(Calendar.DATE) );
        assertEquals("calendar value", cal.get(Calendar.HOUR), testCal.get(Calendar.HOUR) );
        assertEquals("calendar value", cal.get(Calendar.MINUTE), testCal.get(Calendar.MINUTE) );
        assertEquals("calendar value", cal.get(Calendar.SECOND), testCal.get(Calendar.SECOND) );
        */
        testCal.set(Calendar.MILLISECOND, 0);   // ms field can be undefined on Linux
        assertEquals("calendar value", cal, testCal);
        assertEquals("equality test", l1, m.createTypedLiteral("1999-05-31T02:09:32Z", XSDDatatype.XSDdateTime));
        assertTrue("inequality test", l1 != m.createTypedLiteral("1999-04-31T02:09:32Z", XSDDatatype.XSDdateTime));
        
        Calendar testCal2 = new GregorianCalendar(TimeZone.getTimeZone("GMT"));
        testCal2.set(1999, 4, 30, 15, 9, 32);
        testCal2.set(Calendar.MILLISECOND, 0);   // ms field can be undefined on Linux
        Literal lc = m.createTypedLiteral(testCal2);
        assertEquals("calendar 24 hour test", m.createTypedLiteral("1999-05-30T15:09:32Z", XSDDatatype.XSDdateTime), lc );
        
        assertEquals("calendar value", cal, testCal);
        assertEquals("equality test", l1, m.createTypedLiteral("1999-05-31T02:09:32Z", XSDDatatype.XSDdateTime));

        Calendar testCal3 = new GregorianCalendar(TimeZone.getTimeZone("GMT"));
        testCal3.clear();
        testCal3.set(1999, Calendar.JANUARY, 30, 15, 9, 32);
        lc = m.createTypedLiteral(testCal3);
        assertEquals("1999-01-30T15:09:32Z", lc.getLexicalForm());
        String urib="rdf://test.com#";
        String uri1=urib+"1";
        String urip=urib+"prop";
        String testN3 = "<"+uri1+"> <"+urip+"> \""+lc.getLexicalForm()+"\"^^<"+lc.getDatatypeURI()+"> .";
        java.io.StringReader sr = new java.io.StringReader(testN3);
        m.read(sr, urib, "N3");
        assertTrue(m.contains(m.getResource(uri1),m.getProperty(urip)));
        Resource r1 = m.getResource(uri1);
        Property p = m.getProperty(urip);
        XSDDateTime returnedDateTime = (XSDDateTime) r1.getProperty(p).getLiteral().getValue();
        assertEquals("deserialized calendar value", testCal3, returnedDateTime.asCalendar());

        // dateTime to calendar with milliseconds
        Calendar testCal4 = new GregorianCalendar(TimeZone.getTimeZone("GMT"));
        testCal4.set(1999, 4, 30, 15, 9, 32);
        testCal4.set(Calendar.MILLISECOND, 25);
        doDateTimeTest(testCal4, "1999-05-30T15:09:32.025Z", 32.025);
        testCal4.set(Calendar.MILLISECOND, 250);
        doDateTimeTest(testCal4, "1999-05-30T15:09:32.25Z", 32.25);
        testCal4.set(Calendar.MILLISECOND, 2);
        doDateTimeTest(testCal4, "1999-05-30T15:09:32.002Z", 32.002);
        
        // Years before 1000 : xsd:dateTime requires at least a four digit year.
        int[] years = { -7777, -777, -77, -7, 7, 77, 777 , 7777 } ;
        for ( int y : years ) {
            Calendar calM1 = Calendar.getInstance();
            calM1.set(Calendar.YEAR,  y);
            calM1.set(Calendar.MONTH, 10);
            calM1.set(Calendar.DATE,  23);
            XSDDateTime xdtM = new XSDDateTime(calM1);
            LiteralLabel xdtM_ll = LiteralLabelFactory.create(xdtM, "", XSDDatatype.XSDdateTime);
            
            assertTrue("Pre-1000 calendar value", xdtM_ll.getLexicalForm().matches("-?[0-9]{4}-.*")) ;
            assertTrue("Pre-1000 calendar value", xdtM_ll.isWellFormed()) ;
        }
        // Illegal dateTimes
        boolean ok = false;
        boolean old = JenaParameters.enableEagerLiteralValidation;
        try {
            JenaParameters.enableEagerLiteralValidation = true;
            l1 = m.createTypedLiteral(new Date(12345656l), XSDDatatype.XSDdateTime);
        } catch (DatatypeFormatException e) {
            ok = true;
        } finally {
            JenaParameters.enableEagerLiteralValidation = old;
        }
        assertTrue("Early detection of invalid literals", ok);
            

        // date
        l1 = m.createTypedLiteral("1999-05-31", XSDDatatype.XSDdate);
        assertEquals("dateTime data type", XSDDatatype.XSDdate, l1.getDatatype());
        assertEquals("dateTime java type", XSDDateTime.class, l1.getValue().getClass());
        xdt = (XSDDateTime)l1.getValue();
        assertEquals("dateTime value", 1999, xdt.getYears());
        assertEquals("dateTime value", 5, xdt.getMonths());
        assertEquals("dateTime value", 31, xdt.getDays());
        try {
            xdt.getHours();
            assertTrue("Failed to prevent illegal access", false);
        } catch (IllegalDateTimeFieldException e) {}
        
        // time
        l1 = m.createTypedLiteral("12:56:32", XSDDatatype.XSDtime);
        assertEquals("dateTime data type", XSDDatatype.XSDtime, l1.getDatatype());
        assertEquals("dateTime java type", XSDDateTime.class, l1.getValue().getClass());
        xdt = (XSDDateTime)l1.getValue();
        assertEquals("dateTime value", 12, xdt.getHours());
        assertEquals("dateTime value", 56, xdt.getMinutes());
        assertEquals("dateTime value", 32, xdt.getFullSeconds());
        try {
            xdt.getDays();
            assertTrue("Failed to prevent illegal access", false);
        } catch (IllegalDateTimeFieldException e) {}
        
        // gYearMonth
        l1 = m.createTypedLiteral("1999-05", XSDDatatype.XSDgYearMonth);
        assertEquals("dateTime data type", XSDDatatype.XSDgYearMonth, l1.getDatatype());
        assertEquals("dateTime java type", XSDDateTime.class, l1.getValue().getClass());
        xdt = (XSDDateTime)l1.getValue();
        assertEquals("dateTime value", 1999, xdt.getYears());
        assertEquals("dateTime value", 5, xdt.getMonths());
        try {
            xdt.getDays();
            assertTrue("Failed to prevent illegal access", false);
        } catch (IllegalDateTimeFieldException e) {}
        
        // gYear
        l1 = m.createTypedLiteral("1999", XSDDatatype.XSDgYear);
        assertEquals("dateTime data type", XSDDatatype.XSDgYear, l1.getDatatype());
        assertEquals("dateTime java type", XSDDateTime.class, l1.getValue().getClass());
        xdt = (XSDDateTime)l1.getValue();
        assertEquals("dateTime value", 1999, xdt.getYears());
        try {
            xdt.getMonths();
            assertTrue("Failed to prevent illegal access", false);
        } catch (IllegalDateTimeFieldException e) {}
        
        // gMonth
        l1 = m.createTypedLiteral("--05--", XSDDatatype.XSDgMonth);
        assertEquals("dateTime data type", XSDDatatype.XSDgMonth, l1.getDatatype());
        assertEquals("dateTime java type", XSDDateTime.class, l1.getValue().getClass());
        xdt = (XSDDateTime)l1.getValue();
        assertEquals("dateTime value", 5, xdt.getMonths());
        try {
            xdt.getYears();
            assertTrue("Failed to prevent illegal access", false);
        } catch (IllegalDateTimeFieldException e) {}
        
        // gMonthDay
        l1 = m.createTypedLiteral("--05-25", XSDDatatype.XSDgMonthDay);
        assertEquals("dateTime data type", XSDDatatype.XSDgMonthDay, l1.getDatatype());
        assertEquals("dateTime java type", XSDDateTime.class, l1.getValue().getClass());
        xdt = (XSDDateTime)l1.getValue();
        assertEquals("dateTime value", 5, xdt.getMonths());
        assertEquals("dateTime value", 25, xdt.getDays());
        try {
            xdt.getYears();
            assertTrue("Failed to prevent illegal access", false);
        } catch (IllegalDateTimeFieldException e) {}
        
        // gDay
        l1 = m.createTypedLiteral("---25", XSDDatatype.XSDgDay);
        assertEquals("dateTime data type", XSDDatatype.XSDgDay, l1.getDatatype());
        assertEquals("dateTime java type", XSDDateTime.class, l1.getValue().getClass());
        xdt = (XSDDateTime)l1.getValue();
        assertEquals("dateTime value", 25, xdt.getDays());
        try {
            xdt.getMonths();
            assertTrue("Failed to prevent illegal access", false);
        } catch (IllegalDateTimeFieldException e) {}
        
        // Creation of datetime from a date object
        Calendar ncal = new GregorianCalendar(TimeZone.getTimeZone("GMT"));
        ncal.set(2003, 11, 8, 10, 50, 42);
        ncal.set(Calendar.MILLISECOND, 0);
        l1 = m.createTypedLiteral(ncal);
        assertEquals("DateTime from date", XSDDatatype.XSDdateTime, l1.getDatatype());
        assertEquals("DateTime from date", XSDDateTime.class, l1.getValue().getClass());
        assertEquals("DateTime from date", "2003-12-08T10:50:42Z", l1.getValue().toString());
        
        // Thanks to Greg Shueler for DST patch and test case
        //////some of below code from java.util.GregorianCalendar javadoc///////
        // create a Pacific Standard Time time zone
        SimpleTimeZone pdt = new SimpleTimeZone(-8 * 60 * 60 * 1000,  "America/Los_Angeles");

        // set up rules for daylight savings time
        pdt.setStartRule(Calendar.APRIL, 1, Calendar.SUNDAY, 2 * 60 *  60 * 1000);
        pdt.setEndRule(Calendar.OCTOBER, -1, Calendar.SUNDAY, 2 * 60 * 60 * 1000);

        // create a GregorianCalendar with the Pacific Daylight time  zone
        ncal = new GregorianCalendar(pdt);
        ncal.set(2004, 02, 21, 12, 50, 42);//before daylight savings time
        ncal.set(Calendar.MILLISECOND, 0);
        //System.err.println("cal is: "+ncal);
        l1 = m.createTypedLiteral(ncal);
        assertEquals("DateTime from date", XSDDatatype.XSDdateTime, l1.getDatatype());
        assertEquals("DateTime from date", XSDDateTime.class, l1.getValue().getClass());
        assertEquals("DateTime from date", "2004-03-21T20:50:42Z", l1.getValue().toString());
        //System.err.println("date is: "+ncal.getTime());
        ncal = new GregorianCalendar(pdt);
        ncal.set(2004, 03, 21, 12, 50, 42);//within daylight savings time
        ncal.set(Calendar.MILLISECOND, 0);
        //System.err.println("cal is: "+ncal);
        l1 = m.createTypedLiteral(ncal);
        assertEquals("DateTime from date", XSDDatatype.XSDdateTime, l1.getDatatype());
        assertEquals("DateTime from date", XSDDateTime.class, l1.getValue().getClass());
        assertEquals("DateTime from date", "2004-04-21T19:50:42Z", l1.getValue().toString());
        //System.err.println("date is: "+ncal.getTime());

    } 
    
    // Internal helper
    private void doDateTimeTest(Calendar cal, String lex, double time) {
        Literal lc4 = m.createTypedLiteral(cal);
        assertEquals("serialization", lex, lc4.getValue().toString());
        assertEquals("calendar ms test", m.createTypedLiteral(lex, XSDDatatype.XSDdateTime), lc4 );
        XSDDateTime dt4 = (XSDDateTime)lc4.getValue();
        assertTrue("Fraction time check", Math.abs(dt4.getSeconds() - time) < 0.0001);
        assertEquals(dt4.asCalendar(), cal);
    }
    
    /**
     * Test query applied to graphs containing typed values
     */
    public void testTypedContains() {
        Model model = ModelFactory.createDefaultModel();
        Property p = model.createProperty("urn:x-eg/p");
        Literal l1 = model.createTypedLiteral("10", "http://www.w3.org/2001/XMLSchema#integer");
        Literal l2 = model.createTypedLiteral("010", "http://www.w3.org/2001/XMLSchema#integer");
        assertSameValueAs( "sameas test", l1, l2 );
        Resource a = model.createResource("urn:x-eg/a");
        a.addProperty( p, l1 );
        assertTrue( model.getGraph().contains( a.asNode(), p.asNode(), l1.asNode() ) );
        assertTrue( model.getGraph().contains( a.asNode(), p.asNode(), l2.asNode() ) );
    }
      
    /**
     * Test the isValidLiteral machinery
     */
    public void testIsValidLiteral() {
        Literal l = m.createTypedLiteral("1000", XSDDatatype.XSDinteger);
        LiteralLabel ll = l.asNode().getLiteral();
        assertTrue(XSDDatatype.XSDlong.isValidLiteral(ll));
        assertTrue(XSDDatatype.XSDint.isValidLiteral(ll));
        assertTrue(XSDDatatype.XSDshort.isValidLiteral(ll));
        assertTrue(XSDDatatype.XSDunsignedInt.isValidLiteral(ll));
        assertTrue(XSDDatatype.XSDunsignedLong.isValidLiteral(ll));
        assertTrue(XSDDatatype.XSDunsignedShort.isValidLiteral(ll));
        assertTrue(XSDDatatype.XSDpositiveInteger.isValidLiteral(ll));
        assertTrue(XSDDatatype.XSDdecimal.isValidLiteral(ll));
        assertTrue( ! XSDDatatype.XSDstring.isValidLiteral(ll));
        assertTrue( ! XSDDatatype.XSDbyte.isValidLiteral(ll));
        assertTrue( ! XSDDatatype.XSDnegativeInteger.isValidLiteral(ll));
        
        l = m.createTypedLiteral("-2", XSDDatatype.XSDinteger);
        ll = l.asNode().getLiteral();
        assertTrue(XSDDatatype.XSDlong.isValidLiteral(ll));
        assertTrue(XSDDatatype.XSDint.isValidLiteral(ll));
        assertTrue(XSDDatatype.XSDshort.isValidLiteral(ll));
        assertTrue(! XSDDatatype.XSDunsignedInt.isValidLiteral(ll));
        assertTrue(! XSDDatatype.XSDunsignedLong.isValidLiteral(ll));
        assertTrue(! XSDDatatype.XSDunsignedShort.isValidLiteral(ll));
        assertTrue(XSDDatatype.XSDdecimal.isValidLiteral(ll));
        assertTrue(! XSDDatatype.XSDpositiveInteger.isValidLiteral(ll));
        assertTrue( ! XSDDatatype.XSDstring.isValidLiteral(ll));
        assertTrue(XSDDatatype.XSDbyte.isValidLiteral(ll));
        assertTrue(XSDDatatype.XSDnegativeInteger.isValidLiteral(ll));

        l = m.createTypedLiteral("4.5", XSDDatatype.XSDfloat);
        ll = l.asNode().getLiteral();
        assertTrue(! XSDDatatype.XSDdouble.isValidLiteral(ll));
        assertTrue(! XSDDatatype.XSDdecimal.isValidLiteral(ll));
                  
        Literal l2 = m.createTypedLiteral("foo", XSDDatatype.XSDstring);
        assertTrue(XSDDatatype.XSDstring.isValidLiteral(l2.asNode().getLiteral()));
        assertTrue(XSDDatatype.XSDnormalizedString.isValidLiteral(l2.asNode().getLiteral()));
        assertTrue( ! XSDDatatype.XSDint.isValidLiteral(l2.asNode().getLiteral()));
        
        l = m.createTypedLiteral("foo bar");
        ll = l.asNode().getLiteral();
        assertTrue(XSDDatatype.XSDstring.isValidLiteral(ll));
        assertTrue(! XSDDatatype.XSDint.isValidLiteral(ll));
       
        l = m.createTypedLiteral("12");
        ll = l.asNode().getLiteral();
        assertTrue(XSDDatatype.XSDstring.isValidLiteral(ll));
        assertTrue(! XSDDatatype.XSDint.isValidLiteral(ll));
       
       // Test the isValidValue form which had a problem with numbers
       assertTrue(XSDDatatype.XSDnonNegativeInteger.isValidValue(new Integer(10)));
       assertTrue(XSDDatatype.XSDnonNegativeInteger.isValidValue(new Integer(10)));
       assertTrue(!XSDDatatype.XSDnonNegativeInteger.isValidValue(new Integer(-10)));
       assertTrue(!XSDDatatype.XSDnonNegativeInteger.isValidValue("10"));
       
       // The correct behaviour on float/double is unclear but will be clarified
       // by the SWBP working group task force on XML schema.
       // For now we leave that float, double and the decimal tree are all distinct
       assertTrue(XSDDatatype.XSDfloat.isValidValue(new Float("2.3")));
       assertTrue(XSDDatatype.XSDdouble.isValidValue(new Double("2.3")));
       assertTrue( ! XSDDatatype.XSDfloat.isValidValue(new Integer("2")));
       assertTrue( ! XSDDatatype.XSDfloat.isValidValue(new Double("2.3")));
    }

    // These should not be used in data but we test they don't crash anything.
    public void testIsValidLiteral1()
    {
        Literal lit = m.createTypedLiteral("100", XSDDatatype.XSD+"#anyType") ;
        assertFalse(XSDDatatype.XSDinteger.isValidLiteral(lit.asNode().getLiteral()));
    }
    public void testIsValidLiteral2()
    {
        Literal lit = m.createTypedLiteral("100", XSDDatatype.XSD+"#anySimpleType") ;
        assertFalse(XSDDatatype.XSDinteger.isValidLiteral(lit.asNode().getLiteral()));
    }
    

    private static byte[] data = new byte[]{12, 42, 99};
    
    /**
     * Test binary types base64 and hexbinary
     */
    public void testBinary1() {
        // Check byte[] maps onto a binary type  - specifically base64Binary.
        byte[] data = new byte[]{12, 42, 99};
        Literal l = m.createTypedLiteral(data);
        LiteralLabel ll = l.asNode().getLiteral();
        
        assertTrue("binary test 1", ll.getDatatype() instanceof XSDbinary);
        
        // base64 is registered for byte[] 
        // hexBinary is not registered as a type for byte[] 
        assertTrue("binary test 1a", ll.getDatatype() instanceof XSDbase64Binary) ;
        assertEquals("binary test 1b", "DCpj", ll.getLexicalForm());
    }
    
    public void testBinary2() {
        // Check round tripping from value
        LiteralLabel l2 = m.createTypedLiteral("DCpj", XSDDatatype.XSDbase64Binary).asNode().getLiteral();
        Object data2 = l2.getValue();
        assertTrue("binary test 3", data2 instanceof byte[]);
        byte[] data2b = (byte[])data2;
        assertEquals("binary test 4", data2b[0], data[0]);
        assertEquals("binary test 5", data2b[1], data[1]);
        assertEquals("binary test 6", data2b[2], data[2]);
    }
    
    public void testBinary3() {
        // Check hexBinary
        Literal l = m.createTypedLiteral(data, XSDDatatype.XSDhexBinary);
        LiteralLabel ll = l.asNode().getLiteral();
        assertEquals("binary test 1b", ll.getDatatype(), XSDDatatype.XSDhexBinary);
        assertEquals("binary test 2b", HexBin.encode(data), ll.getLexicalForm());
        
        // Check round tripping from value
        LiteralLabel l2 = m.createTypedLiteral(ll.getLexicalForm(), XSDDatatype.XSDhexBinary).asNode().getLiteral();
        Object data2 = l2.getValue();
        assertTrue("binary test 3b", data2 instanceof byte[]);
        byte[] data2b = ((byte[])data2);
        assertEquals("binary test 4b", data2b[0], data[0]);
        assertEquals("binary test 5b", data2b[1], data[1]);
        assertEquals("binary test 6b", data2b[2], data[2]);
        assertEquals(l2, ll);
    }
        
    public void testBinary4() {   
        Literal la = m.createTypedLiteral("GpM7", XSDDatatype.XSDbase64Binary);
        Literal lb = m.createTypedLiteral("GpM7", XSDDatatype.XSDbase64Binary);
        assertTrue("equality test", la.sameValueAs(lb));
        
        data = new byte[] {15, (byte)0xB7};
        Literal l = m.createTypedLiteral(data, XSDDatatype.XSDhexBinary);
        assertEquals("hexBinary encoding", "0FB7", l.getLexicalForm());
    }
    
    /** Test that XSD anyURI is not sameValueAs XSD string (Xerces returns a string as the value for both) */ 
    public void testXSDanyURI() {
        Node node1 = NodeFactory.createLiteral("http://example/", null, XSDDatatype.XSDanyURI) ;
        Node node2 = NodeFactory.createLiteral("http://example/", null, XSDDatatype.XSDstring) ;
        assertFalse(node1.sameValueAs(node2)) ;
    }
    
    /**
     * Test a user error report concerning date/time literals
     */
    public void testDateTimeBug() {
        // Bug in serialization
        String XSDDateURI = XSD.date.getURI(); 
        TypeMapper typeMapper=TypeMapper.getInstance(); 
        RDFDatatype dt = typeMapper.getSafeTypeByName(XSDDateURI); 
        Object obj = dt.parse("2003-05-21"); 
        Literal literal = m.createTypedLiteral(obj, dt);        
        literal.toString();     
        Object value2 = dt.parse(obj.toString());
        assertEquals(obj, value2);
        
        // Check alternative form doesn't provoke exceptions
        RDFDatatype dateType = XSDDatatype.XSDdate;
        m.createTypedLiteral("2003-05-21", dateType);
        
        // Check alt time times
        checkSerialization("2003-05-21", XSDDatatype.XSDdate);
        checkSerialization("2003-05-21T12:56:10Z", XSDDatatype.XSDdateTime);
        checkSerialization("2003-05", XSDDatatype.XSDgYearMonth);
        checkSerialization("2003", XSDDatatype.XSDgYear);
        checkSerialization("--05", XSDDatatype.XSDgMonth);
        checkSerialization("--05-12", XSDDatatype.XSDgMonthDay);
        checkSerialization("---12", XSDDatatype.XSDgDay);
    }
    
    private static Date getDateFromPattern(String ts, String format, String timezoneid) throws Exception {
        return getDateFromPattern(ts, new String[]{format}, TimeZone.getTimeZone(timezoneid));
    }

    private static Date getDateFromPattern(String ts, String[] formats, TimeZone tz) throws Exception {
        java.util.Date date = null;
        java.text.DateFormat sdf = java.text.DateFormat.getInstance();
        {
            sdf.setTimeZone(tz == null ? java.util.TimeZone.getDefault() : tz);
            for (int i=0; date == null && i<formats.length;i++){
                ((java.text.SimpleDateFormat)sdf).applyPattern(formats[i]);
                try {
                    date = sdf.parse(ts);
                } catch (java.text.ParseException pe){} // keep trying
            }
        }
        return date;
    }
    
    public void testDateTimeBug2() throws Exception {
        String[] timezonelist = {
            "GMT",
            "America/New_York",
            "America/Chicago",
        };

        for (String timezoneid : timezonelist) {
            TimeZone tz = TimeZone.getTimeZone(timezoneid);
            String[] sampletimelist = {
                "03/10/2012 01:29", 
                // 03/11/2012 DST time change at 2 am
                "03/11/2012 00:29",
                "03/11/2012 01:29",
                "03/11/2012 02:29",
                "03/11/2012 03:29",
                "03/11/2012 04:29",

                "03/12/2012 01:29",
                "11/03/2012 23:29",
                // 11/04/2012 standard time change at 2 am
                "11/04/2012 00:29",
                "11/04/2012 01:29",
                "11/04/2012 02:29",
                "11/04/2012 03:29",
            };

            String format = "MM/dd/yyy HH:mm";
            for (String tstr : sampletimelist){
                Date dt=getDateFromPattern(tstr, format, timezoneid);
                SimpleDateFormat df = new SimpleDateFormat("yyyy.MM.dd HH:mm:ss z");
                df.setTimeZone(tz);
                Calendar cal = Calendar.getInstance();
                cal.setTimeZone(tz);
                cal.setTime(dt);
                XSDDateTime xdt = new XSDDateTime(cal);
                int offset = tz.getOffset(dt.getTime()) /( 60 * 60 * 1000);
                int xhr = xdt.getHours();
                int dhr = cal.get(Calendar.HOUR_OF_DAY);
                int dif = (xhr -dhr + offset) % 24;
                Assert.assertEquals("Difference between cal and xdt", 0, dif) ;
                
//                //System.out.println("xhr="+xhr+",dhr="+dhr+",dif="+dif);
//                System.out.println(""
//                    +"tstr="+tstr
//                    +"\tdate="+df.format(dt)
//                    +(dif==0?"\t ":"\tX")
//                    +" xsddt="+xdt
//                    +"\toffset="+offset);
            }
            //System.out.println();
        }
    }
    
    
    /**
     * Test global parameter flags.
     */
    public void testFlags() {
        boolean originalFlag = JenaParameters.enableEagerLiteralValidation;
        JenaParameters.enableEagerLiteralValidation = true;
        boolean foundException = false;
        try {
            m.createTypedLiteral("fool", XSDDatatype.XSDint);
        } catch (DatatypeFormatException e1) {
            foundException = true;
        }
        JenaParameters.enableEagerLiteralValidation = originalFlag;
        assertTrue("Early datatype format exception", foundException);
        
        JenaParameters.enableEagerLiteralValidation = false;
        foundException = false;
        Literal l = null;
        try {
            l = m.createTypedLiteral("fool", XSDDatatype.XSDint);
        } catch (DatatypeFormatException e1) {
            JenaParameters.enableEagerLiteralValidation = originalFlag;
            assertTrue("Delayed datatype format validation", false);
        }
        try {
            l.getValue();
        } catch (DatatypeFormatException e2) {
            foundException = true;
        }
        JenaParameters.enableEagerLiteralValidation = originalFlag;
        assertTrue("Early datatype format exception", foundException);
        
        originalFlag = JenaParameters.enablePlainLiteralSameAsString;
        Literal l1 = m.createLiteral("test string");
        Literal l2 = m.createTypedLiteral("test string", XSDDatatype.XSDstring);
        JenaParameters.enablePlainLiteralSameAsString = true;
        boolean ok1 = l1.sameValueAs(l2); 
        JenaParameters.enablePlainLiteralSameAsString = false;
        boolean ok2 = ! l1.sameValueAs(l2); 
        JenaParameters.enablePlainLiteralSameAsString = originalFlag;
        assertTrue( ok1 );
        assertTrue( ok2 );
    }
    
    /**
     * Test that equality function takes lexical distinction into account. 
     */
    public void testLexicalDistinction() {
        Literal l1 = m.createTypedLiteral("3.0", XSDDatatype.XSDdecimal);
        Literal l2 = m.createTypedLiteral("3.00", XSDDatatype.XSDdecimal);
        Literal l3 = m.createTypedLiteral("3.0", XSDDatatype.XSDdecimal);
        assertSameValueAs("lexical form does not affect value", l1, l2);
        assertSameValueAs("lexical form does not affect value", l3, l2);
        assertTrue("lexical form affects equality", ! l1.equals(l2));
        assertTrue("lexical form affects equality",   l1.equals(l3));
        
        // This version will become illegal in the future and will be removed then
        l1 = m.createTypedLiteral("3", XSDDatatype.XSDint);
        l2 = m.createTypedLiteral(" 3 ", XSDDatatype.XSDint);
        l3 = m.createTypedLiteral("3", XSDDatatype.XSDint);
        assertSameValueAs("lexical form does not affect value", l1, l2);
        assertSameValueAs("lexical form does not affect value", l3, l2);
        assertTrue("lexical form affects equality", ! l1.equals(l2));
        assertTrue("lexical form affects equality",   l1.equals(l3));
    }
    
    /**
     * Test parse/unparse pairing for problem datatypes
     */
    public void testRoundTrip() {
        // Prior problem cases with unparsing
        doTestRoundTrip("13:20:00.000", XSDDatatype.XSDtime, false);
        doTestRoundTrip("GpM7", XSDDatatype.XSDbase64Binary, true);
        doTestRoundTrip("0FB7", XSDDatatype.XSDhexBinary, true);
        
        // check value round tripping
        doTestValueRoundTrip("2005-06-27", XSDDatatype.XSDdate, true);
        doTestValueRoundTrip("2005", XSDDatatype.XSDgYear, true);
        doTestValueRoundTrip("2005-06", XSDDatatype.XSDgYearMonth, true);
        doTestValueRoundTrip("13:20:00.000", XSDDatatype.XSDtime, true);
    }
    
    /**
     * Check parse/unparse loop.
     */
    public void doTestRoundTrip(String lex, RDFDatatype dt, boolean testeq) {
        LiteralLabel ll = LiteralLabelFactory.createLiteralLabel( lex, "", dt );
        String lex2 = dt.unparse(ll.getValue());
        if (testeq) {
            assertEquals(lex, lex2);
        }
        LiteralLabel ll2 = LiteralLabelFactory.createLiteralLabel( lex2, "", dt );
        assertTrue( ll2.isWellFormed() );
    }
    
    /**
     * Check getValue/rewrap loop.
     */
    public void doTestValueRoundTrip(String lex, RDFDatatype dt, boolean testType) {
        Literal l1 = m.createTypedLiteral(lex, dt);
        Object o1 = l1.getValue();
        Literal l2 = m.createTypedLiteral(o1);
        assertTrue("value round trip", l1.sameValueAs(l2));
        Object o2 = l2.getValue();
        assertTrue("value round trip2", o1.equals(o2));
        if (testType) {
            assertEquals("Datatype round trip", dt, l2.getDatatype());
        }
    }
    
    /**
     * Test ability to override an apparent DateTime to be just a date
     */
    public void testDateOverride() {
        Calendar date = new GregorianCalendar(2007, 3, 4);
        date.setTimeZone( TimeZone.getTimeZone("GMT+0") );
        XSDDateTime  xsdDate = new XSDDateTime( date );
        Literal l1 = m.createTypedLiteral(xsdDate, XSDDatatype.XSDdate);
        
        assertEquals(XSDDatatype.XSDdate, l1.getDatatype());
        assertEquals("2007-04-04Z", l1.getLexicalForm());
    }
    
    /**
     * Test that two objects are not semantically the same
     */
    private void assertDiffer( String title, Literal x, Literal y ) {
        assertTrue( title, !x.sameValueAs( y ) ); 
    }
     
    /**
     * Test that two objects are semantically the same
     */
    private void assertSameValueAs( String title, Literal x, Literal y ) {
        assertTrue( title, x.sameValueAs( y ) ); 
    }
     
    /**
     * Test two doubles are equal to within 0.001
     */
    private void assertFloatEquals(String title, double x, double y) {
        assertTrue(title, Math.abs(x - y) < 0.001);
    }

    /**
     * Check that constructing an illegal literal throws
     * the right exception
     */
    public void checkIllegalLiteral(String lex, RDFDatatype dtype) {
        try {
            Literal l = m.createTypedLiteral(lex, dtype);
            l.getValue();
            assertTrue("Failed to catch '" + lex + "' as an illegal " + dtype, false);
        } catch (DatatypeFormatException e1) {
            // OK this is what we expected
        }
    }
    
    /**
     * Check can legally construct a literal with given lex, value and dtype
     */
    public void checkLegalLiteral(String lex, RDFDatatype dtype, Class<?> jtype, Object value) {
        Literal l = m.createTypedLiteral(lex, dtype);
        assertEquals(l.getValue().getClass(), jtype);
        assertEquals(l.getValue(), value);
        assertEquals(l.getDatatype(), dtype);
    }
    
    /**
     * Chek the serialization of the parse of a value.
     */
    public void checkSerialization(String lex, RDFDatatype dtype) {
        Literal l = m.createTypedLiteral(lex, dtype);
        assertEquals(l.getValue().toString(), lex);
    }
    
    /** Helper function test an iterator against a list of objects - order dependent */
    public void assertIteratorValues( Iterator<String> it, Object[] vals ) {
        boolean[] found = new boolean[vals.length];
        for (int i = 0; i < vals.length; i++) found[i] = false;
        while (it.hasNext()) {
            Object n = it.next();
            boolean gotit = false;
            for (int i = 0; i < vals.length; i++) {
                if (n.equals(vals[i])) {
                    gotit = true;
                    found[i] = true;
                }
            }
            assertTrue(gotit);
        }
        for (int i = 0; i < vals.length; i++) {
            assertTrue(found[i]);
        }
    }
    
    
}

/**
 * Datatype definition for the rational number representation
 * defined below.
 */
class RationalType extends BaseDatatype {
    public static final String theTypeURI = "urn:x-hp-dt:rational";
    public static final RDFDatatype theRationalType = new RationalType();

    /** private constructor - single global instance */    
    private RationalType() {
        super(theTypeURI);
    }
   
    /**
     * Convert a value of this datatype out
     * to lexical form.
     */
    @Override
    public String unparse(Object value) {
        Rational r = (Rational) value;
        return Integer.toString(r.getNumerator()) + "/" + r.getDenominator();
    }
        
    /**
     * Parse a lexical form of this datatype to a value
     * @throws DatatypeFormatException if the lexical form is not legal
     */
    @Override
    public Object parse(String lexicalForm) throws DatatypeFormatException {
        int index = lexicalForm.indexOf("/");
        if (index == -1) {
            throw new DatatypeFormatException(lexicalForm, theRationalType, "");
        }
        try {
            int numerator = Integer.parseInt(lexicalForm.substring(0, index));
            int denominator = Integer.parseInt(lexicalForm.substring(index+1));
            return new Rational(numerator, denominator);
        } catch (NumberFormatException e) {
            throw new DatatypeFormatException(lexicalForm, theRationalType, "");
        }
    }
    
    /**
     * Compares two instances of values of the given datatype.
     * This does not allow rationals to be compared to other number
     * formats, lang tag is not significant.
     */
    @Override
    public boolean isEqual(LiteralLabel value1, LiteralLabel value2) {
        return value1.getDatatype() == value2.getDatatype()
             && value1.getValue().equals(value2.getValue());
    }
  
}

/**
 * Representation of a rational number. Used for testing
 * user defined datatypes
 */
class Rational {
    private int numerator;
    private int denominator;
    
    Rational(int numerator, int denominator) {
        this.numerator = numerator;
        this.denominator = denominator;
    }
    /**
     * Returns the denominator.
     * @return int
     */
    public int getDenominator() {
        return denominator;
    }

    /**
     * Returns the numerator.
     * @return int
     */
    public int getNumerator() {
        return numerator;
    }

    /**
     * Sets the denominator.
     * @param denominator The denominator to set
     */
    public void setDenominator(int denominator) {
        this.denominator = denominator;
    }

    /**
     * Sets the numerator.
     * @param numerator The numerator to set
     */
    public void setNumerator(int numerator) {
        this.numerator = numerator;
    }
    
    /**
     * Printable form - not parsable
     */
    @Override
    public String toString() {
        return "rational[" + numerator + "/" + denominator + "]";
    }
    
    /**
     * Equality check
     */
    @Override
    public boolean equals(Object o) {
        if (o == null || !(o instanceof Rational)) return false;
        Rational or = (Rational)o;
        return (numerator == or.numerator && denominator == or.denominator);
    }
}
