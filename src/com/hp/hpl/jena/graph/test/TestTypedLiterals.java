/******************************************************************
 * File:        TestTypedLiterals.java
 * Created by:  Dave Reynolds
 * Created on:  08-Dec-02
 * 
 * (c) Copyright 2002, Hewlett-Packard Company, all rights reserved.
 * [See end of file]
 * $Id: TestTypedLiterals.java,v 1.3 2003-01-30 10:25:18 chris-dollin Exp $
 *****************************************************************/
package com.hp.hpl.jena.graph.test;

import com.hp.hpl.jena.graph.LiteralLabel;
import com.hp.hpl.jena.graph.dt.*;
import com.hp.hpl.jena.mem.ModelMem;
import com.hp.hpl.jena.rdf.model.*;

import junit.framework.TestCase;
import junit.framework.TestSuite;

import java.math.*;
import java.text.DateFormat;
import java.util.*;
import java.io.*;
   
/**
 * Unit test for the typed literal machinery - including RDFDatatype,
 * TypeMapper and LiteralLabel.
 * 
 * @author <a href="mailto:der@hplb.hpl.hp.com">Dave Reynolds</a>
 * @version $Revision: 1.3 $ on $Date: 2003-01-30 10:25:18 $
 */
public class TestTypedLiterals extends TestCase {
      
    /** dummy model used as a literal factory */
    private Model m = new ModelMem();
    
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

        Literal l1 = m.createTypedLiteral("foo", "lang1", typeURI);
        Literal l2 = m.createTypedLiteral("foo", "lang2", typeURI);
        Literal l3 = m.createTypedLiteral("15", "lang1", typeURI);
        Literal l4 = m.createTypedLiteral("foo", "lang1", typeURI);
        Literal l5 = m.createTypedLiteral("foo", "lang1", typeURI2);
        Literal l6 = m.createLiteral("foo", "lang1");
        
        // Check for successful creation
        assertNotNull(l1);
        assertNotNull(l2);
        assertNotNull(l3);
        assertNotNull(l4);
        assertNotNull(l5);
        
        // check equality function
        assertSame("language sensitive comparison", l1, l4);
        assertDiffer("language sensitive", l1, l2);
        assertDiffer("datatype sensitive", l1, l5);
        assertDiffer("value sensitive", l1, l3);
        assertDiffer("typed and plain differ", l1, l6);

        // Check typed accessors
        try {
            int i = l3.getInt();
            assertTrue("Allowed int conversion", false);
        } catch (DatatypeFormatException e) {};
        assertEquals("Extract value", l1.getValue(), "foo");
        assertEquals("Extract lang", l1.getLanguage(), "lang1");
        assertEquals("Extract xml tag", l1.getWellFormed(), false);
    }
    
    /**
     * Tests the base functioning of a user defined datatype
     */
    public void testUserDef() {
        // Register the user defined type for rationals
        RDFDatatype rtype = RationalType.theRationalType;
        TypeMapper.getInstance().registerDatatype(rtype);


        Literal l1 = m.createTypedLiteral("3/5", "lang1", rtype);
        Literal l2 = m.createTypedLiteral("3/5", "lang2", rtype);
        Literal l3 = m.createTypedLiteral("7/5", "lang1", rtype);
        
        // Check for successful creation
        assertNotNull(l1);
        assertNotNull(l2);
        assertNotNull(l3);
        
        // check equality function
        assertSame("language should be ignored", l1, l2);
        assertDiffer("values should be tested!", l1, l3);

        // Check typed accessors
        assertSame("Datatype incorrect", l1.getDatatype(), rtype);
        assertEquals("Datatype uri incorrect", l1.getDatatypeURI(), RationalType.theTypeURI);
        Object val = l1.getValue();
        assertTrue("Value space check", val instanceof Rational);
        assertTrue("Value check", ((Rational)val).getNumerator() == 3);
        assertTrue("Value check", ((Rational)val).getDenominator() == 5);
        try {
            int i = l1.getInt();
            assertTrue("Allowed int conversion", false);
        } catch (DatatypeFormatException e) {};
        assertEquals("Extract lang", l1.getLanguage(), "lang1");
        assertEquals("Extract xml tag", l1.getWellFormed(), false);
    }

    /**
     * Tests basic XSD integer types
     */
    public void testXSDbasics() {
        String xsdIntURI = "http://www.w3.org/2001/XMLSchema#int";
        
        // Check int and basic equality processing
        Literal l1 = m.createTypedLiteral(42);  // default map
        Literal l2 = m.createTypedLiteral("42", "", XSDDatatype.XSDint);
        Literal l3 = m.createTypedLiteral("42", "lang", XSDDatatype.XSDint);
        Literal l4 = m.createTypedLiteral("63");  // default map
        
        assertSame("Default map failed", l1, l2);
        assertEquals("Value wrong", l1.getValue(), new Integer(42));
        assertEquals("class wrong", l1.getValue().getClass(), Integer.class);
        assertEquals("Value accessor problem", l1.getInt(), 42);
        assertEquals("wrong type name", l2.getDatatypeURI(), xsdIntURI);
        assertEquals("wrong type", l2.getDatatype(), XSDDatatype.XSDint);
        assertSame("Lang sensitive", l2, l3);
        assertDiffer("Not value sensitive", l1, l4);
        checkIllegalLiteral("zap", XSDDatatype.XSDint);
        checkIllegalLiteral("42.1", XSDDatatype.XSDint);
        
        // Check float/double
        l1 = m.createTypedLiteral(42.42);  // default map
        l2 = m.createTypedLiteral("42.42", "", XSDDatatype.XSDfloat);
        l3 = m.createTypedLiteral("42.42", "", XSDDatatype.XSDdouble);
        
        assertEquals("class wrong", l1.getValue().getClass(), Double.class);
        assertFloatEquals("value wrong", ((Double)(l1.getValue())).floatValue(), 42.42);
        assertEquals("class wrong", l2.getValue().getClass(), Float.class);
        assertFloatEquals("value wrong", ((Float)(l2.getValue())).floatValue(), 42.42);
        assertFloatEquals("Value accessor problem", l1.getFloat(), 42.42);
        assertEquals("wrong type", l2.getDatatype(), XSDDatatype.XSDfloat);
        assertSame("equality fn", l1, l3);
        
        // Minimal check on long, short, byte
        checkLegalLiteral("12345", XSDDatatype.XSDlong, Long.class, new Long(12345));
        checkLegalLiteral("-12345", XSDDatatype.XSDlong, Long.class, new Long(-12345));
        checkIllegalLiteral("2.3", XSDDatatype.XSDlong);
        
        checkLegalLiteral("1234", XSDDatatype.XSDshort, Short.class, new Short((short)1234));
        checkLegalLiteral("-1234", XSDDatatype.XSDshort, Short.class, new Short((short)-1234));
        checkLegalLiteral("32767", XSDDatatype.XSDshort, Short.class, new Short((short)32767));
        checkLegalLiteral("-32768", XSDDatatype.XSDshort, Short.class, new Short((short)-32768));
        checkIllegalLiteral("32769", XSDDatatype.XSDshort);
        checkIllegalLiteral("2.3", XSDDatatype.XSDshort);

        checkLegalLiteral("42", XSDDatatype.XSDbyte, Byte.class, new Byte((byte)42));
        checkLegalLiteral("-42", XSDDatatype.XSDbyte, Byte.class, new Byte((byte)-42));
        checkLegalLiteral("127", XSDDatatype.XSDbyte, Byte.class, new Byte((byte)127));
        checkLegalLiteral("-128", XSDDatatype.XSDbyte, Byte.class, new Byte((byte)-128));
        checkIllegalLiteral("32769", XSDDatatype.XSDbyte);
        checkIllegalLiteral("128", XSDDatatype.XSDbyte);
        checkIllegalLiteral("2.3", XSDDatatype.XSDbyte);
        
        // Minimal check on unsigned normal types
        checkLegalLiteral("12345", XSDDatatype.XSDunsignedLong, Long.class, new Long(12345));
        checkLegalLiteral("9223372036854775808", XSDDatatype.XSDunsignedLong, BigInteger.class, new BigInteger("9223372036854775808"));
        checkIllegalLiteral("-12345", XSDDatatype.XSDunsignedLong);
        
        checkLegalLiteral("12345", XSDDatatype.XSDunsignedInt, Long.class, new Long(12345));
        checkLegalLiteral("2147483648", XSDDatatype.XSDunsignedInt, Long.class, new Long(2147483648l));
        checkIllegalLiteral("-12345", XSDDatatype.XSDunsignedInt);
        
        checkLegalLiteral("1234", XSDDatatype.XSDunsignedShort, Integer.class, new Integer(1234));
        checkLegalLiteral("32679", XSDDatatype.XSDunsignedShort, Integer.class, new Integer(32679));
        checkIllegalLiteral("-12345", XSDDatatype.XSDunsignedShort);
        
        checkLegalLiteral("123", XSDDatatype.XSDunsignedByte, Short.class, new Short((short)123));
        checkLegalLiteral("129", XSDDatatype.XSDunsignedByte, Short.class, new Short((short)129));
        checkIllegalLiteral("-123", XSDDatatype.XSDunsignedByte);
        
        // Minimal check on the big num types
        checkLegalLiteral("12345", XSDDatatype.XSDinteger, Long.class, new Long(12345));
        checkLegalLiteral("0", XSDDatatype.XSDinteger, Long.class, new Long(0));
        checkLegalLiteral("-12345", XSDDatatype.XSDinteger, Long.class, new Long(-12345));
        checkLegalLiteral("9223372036854775808", XSDDatatype.XSDinteger, BigInteger.class, new BigInteger("9223372036854775808"));
        
        checkLegalLiteral("12345", XSDDatatype.XSDpositiveInteger, Long.class, new Long(12345));
        checkIllegalLiteral("0", XSDDatatype.XSDpositiveInteger);
        checkIllegalLiteral("-12345", XSDDatatype.XSDpositiveInteger);
        checkLegalLiteral("9223372036854775808", XSDDatatype.XSDpositiveInteger, BigInteger.class, new BigInteger("9223372036854775808"));
        
        checkLegalLiteral("12345", XSDDatatype.XSDnonNegativeInteger, Long.class, new Long(12345));
        checkLegalLiteral("0", XSDDatatype.XSDnonNegativeInteger, Long.class, new Long(0));
        checkIllegalLiteral("-12345", XSDDatatype.XSDnonNegativeInteger);
        checkLegalLiteral("9223372036854775808", XSDDatatype.XSDnonNegativeInteger, BigInteger.class, new BigInteger("9223372036854775808"));
        
        checkLegalLiteral("-12345", XSDDatatype.XSDnegativeInteger, Long.class, new Long(-12345));
        checkIllegalLiteral("0", XSDDatatype.XSDnegativeInteger);
        checkIllegalLiteral("12345", XSDDatatype.XSDnegativeInteger);
        checkLegalLiteral("-9223372036854775808", XSDDatatype.XSDnegativeInteger, BigInteger.class, new BigInteger("-9223372036854775808"));
        
        checkLegalLiteral("-12345", XSDDatatype.XSDnonPositiveInteger, Long.class, new Long(-12345));
        checkLegalLiteral("0", XSDDatatype.XSDnonPositiveInteger, Long.class, new Long(0));
        checkIllegalLiteral("12345", XSDDatatype.XSDnonPositiveInteger);
        checkLegalLiteral("-9223372036854775808", XSDDatatype.XSDnonPositiveInteger, BigInteger.class, new BigInteger("-9223372036854775808"));
        
        checkLegalLiteral("12345", XSDDatatype.XSDdecimal, Long.class, new Long("12345"));
        checkLegalLiteral("42.45", XSDDatatype.XSDdecimal, BigDecimal.class, new BigDecimal("42.45"));
        checkLegalLiteral("9223372036854775808.1234", XSDDatatype.XSDdecimal, BigDecimal.class, new BigDecimal("9223372036854775808.1234"));
        
        // Booleans
        checkLegalLiteral("true", XSDDatatype.XSDboolean, Boolean.class, new Boolean(true));
        checkLegalLiteral("false", XSDDatatype.XSDboolean, Boolean.class, new Boolean(false));
        l1 = m.createTypedLiteral(true);
        assertEquals("boolean mapping", XSDDatatype.XSDboolean, l1.getDatatype());
        
        // String types
        checkLegalLiteral("hello world", XSDDatatype.XSDstring, String.class, "hello world");
        l1 = m.createTypedLiteral("foo bar");
        assertEquals("string mapping", XSDDatatype.XSDstring, l1.getDatatype());
        
    }
    
    /**
     * Test user defined data types using the DAML+OIL standard example.
     * N.B. The file on daml.org is not legal (wrong namespace for XMLSchema, missed
     * qualifiers  onsome restriction base types) so we actually load a locally cached
     * correct version but pretend it is from the real URI. 
     */
    public void testUserDefined() throws IOException {
        String uri = "http://www.daml.org/2001/03/daml+oil-ex-dt";
        String filename = "testing/xsd/daml+oil-ex-dt.xsd";
        TypeMapper tm = TypeMapper.getInstance();
        List typenames = XSDDatatype.loadUserDefined(uri, new FileReader(filename), null, tm);
        assertIteratorValues(typenames.iterator(), new Object[] {
            uri + "#XSDEnumerationHeight",
            uri + "#over12",
            uri + "#over17",
            uri + "#over59",
            uri + "#clothingsize"   });
        
        // Check the string restriction
        RDFDatatype heightType = tm.getTypeByName(uri + "#XSDEnumerationHeight");
        checkLegalLiteral("short", heightType, String.class, "short");
        checkLegalLiteral("tall", heightType, String.class, "tall");
        checkIllegalLiteral("shortish", heightType);

        // Check the numeric restriction
        RDFDatatype over12Type = tm.getTypeByName(uri + "#over12");
        checkLegalLiteral("15", over12Type, Integer.class, new Integer(15));
        checkIllegalLiteral("12", over12Type);
        
        // Check the union type
        RDFDatatype clothingsize = tm.getTypeByName(uri + "#clothingsize");
        checkLegalLiteral("42", clothingsize, Integer.class, new Integer(42));
        checkLegalLiteral("short", clothingsize, String.class, "short");
        
    }

    /**
     * Test data/time wrappers
     */
    public void testDateTime() {
        // Duration
        Literal l1 = m.createTypedLiteral("P1Y2M3DT5H6M7.5S", "", XSDDatatype.XSDduration);
        assertEquals("duration data type", XSDDatatype.XSDduration, l1.getDatatype());
        assertEquals("duration java type", XSDDuration.class, l1.getValue().getClass());
        assertEquals("duration value", 1, ((XSDDuration)l1.getValue()).getYears());
        assertEquals("duration value", 2, ((XSDDuration)l1.getValue()).getMonths());
        assertEquals("duration value", 3, ((XSDDuration)l1.getValue()).getDays());
        assertEquals("duration value", 5, ((XSDDuration)l1.getValue()).getHours());
        assertEquals("duration value", 6, ((XSDDuration)l1.getValue()).getMinutes());
        assertEquals("duration value", 7, ((XSDDuration)l1.getValue()).getFullSeconds());
        assertFloatEquals("duration value", 18367.5, ((XSDDuration)l1.getValue()).getTimePart());
        assertEquals("serialization", "P1Y2M3DT5H6M7.5S", l1.getValue().toString());
        
        // dateTime
        l1 = m.createTypedLiteral("1999-05-31T12:56:32Z", "", XSDDatatype.XSDdateTime);
        XSDDateTime xdt = (XSDDateTime)l1.getValue();
        assertEquals("dateTime data type", XSDDatatype.XSDdateTime, l1.getDatatype());
        assertEquals("dateTime java type", XSDDateTime.class, l1.getValue().getClass());
        assertEquals("dateTime value", 1999, xdt.getYears());
        assertEquals("dateTime value", 5, xdt.getMonths());
        assertEquals("dateTime value", 31, xdt.getDays());
        assertEquals("dateTime value", 12, xdt.getHours());
        assertEquals("dateTime value", 56, xdt.getMinutes());
        assertEquals("dateTime value", 32, xdt.getFullSeconds());
        assertEquals("serialization", "1999-5-31T12:56:32.0Z", l1.getValue().toString());
        Calendar cal = xdt.asCalendar();
        String formatedCal = DateFormat.getDateTimeInstance(DateFormat.LONG, DateFormat.LONG, Locale.UK).format(cal.getTime());
        assertEquals("serialization", "01 July 1999 13:56:32 BST", formatedCal);
        
        // date
        l1 = m.createTypedLiteral("1999-05-31", "", XSDDatatype.XSDdate);
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
        l1 = m.createTypedLiteral("12:56:32", "", XSDDatatype.XSDtime);
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
        l1 = m.createTypedLiteral("1999-05", "", XSDDatatype.XSDgYearMonth);
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
        l1 = m.createTypedLiteral("1999", "", XSDDatatype.XSDgYear);
        assertEquals("dateTime data type", XSDDatatype.XSDgYear, l1.getDatatype());
        assertEquals("dateTime java type", XSDDateTime.class, l1.getValue().getClass());
        xdt = (XSDDateTime)l1.getValue();
        assertEquals("dateTime value", 1999, xdt.getYears());
        try {
            xdt.getMonths();
            assertTrue("Failed to prevent illegal access", false);
        } catch (IllegalDateTimeFieldException e) {}
        
        // gMonth
        l1 = m.createTypedLiteral("--05--", "", XSDDatatype.XSDgMonth);
        assertEquals("dateTime data type", XSDDatatype.XSDgMonth, l1.getDatatype());
        assertEquals("dateTime java type", XSDDateTime.class, l1.getValue().getClass());
        xdt = (XSDDateTime)l1.getValue();
        assertEquals("dateTime value", 5, xdt.getMonths());
        try {
            xdt.getYears();
            assertTrue("Failed to prevent illegal access", false);
        } catch (IllegalDateTimeFieldException e) {}
        
        // gMonthDay
        l1 = m.createTypedLiteral("--05-25", "", XSDDatatype.XSDgMonthDay);
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
        l1 = m.createTypedLiteral("---25", "", XSDDatatype.XSDgDay);
        assertEquals("dateTime data type", XSDDatatype.XSDgDay, l1.getDatatype());
        assertEquals("dateTime java type", XSDDateTime.class, l1.getValue().getClass());
        xdt = (XSDDateTime)l1.getValue();
        assertEquals("dateTime value", 25, xdt.getDays());
        try {
            xdt.getMonths();
            assertTrue("Failed to prevent illegal access", false);
        } catch (IllegalDateTimeFieldException e) {}
        
    }
        
    /**
     * Test that two objects are not semantically the same
     */
    private void assertDiffer( String title, Literal x, Literal y ) {
        assertTrue( title, !x.sameValueAs( y ) ); 
    }
     
    /**
     * Test that two objects are not semantically the same
     */
    private void assertSame( String title, Literal x, Literal y ) {
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
            Literal l = m.createTypedLiteral(lex, "lang", dtype);
            l.getValue();
            assertTrue("Failed to catch '" + lex + "' as an illegal " + dtype, false);
        } catch (DatatypeFormatException e1) {
            // OK this is what we expected
        }
    }
    
    /**
     * Check can legally construct a literal with given lex, value and dtype
     */
    public void checkLegalLiteral(String lex, RDFDatatype dtype, Class jtype, Object value) {
        Literal l = m.createTypedLiteral(lex, "lang", dtype);
        assertEquals(l.getValue().getClass(), jtype);
        assertEquals(l.getValue(), value);
        assertEquals(l.getDatatype(), dtype);
    }
    
    /** Helper function test an iterator against a list of objects - order dependent */
    public void assertIteratorValues(Iterator it, Object[] vals) {
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
    public String unparse(Object value) {
        Rational r = (Rational) value;
        return Integer.toString(r.getNumerator()) + "/" + r.getDenominator();
    }
        
    /**
     * Parse a lexical form of this datatype to a value
     * @throws DatatypeFormatException if the lexical form is not legal
     */
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
    public String toString() {
        return "rational[" + numerator + "/" + denominator + "]";
    }
    
    /**
     * Equality check
     */
    public boolean equals(Object o) {
        if (o == null || !(o instanceof Rational)) return false;
        Rational or = (Rational)o;
        return (numerator == or.numerator && denominator == or.denominator);
    }
}    
    

/*
    (c) Copyright Hewlett-Packard Company 2002
    All rights reserved.

    Redistribution and use in source and binary forms, with or without
    modification, are permitted provided that the following conditions
    are met:

    1. Redistributions of source code must retain the above copyright
       notice, this list of conditions and the following disclaimer.

    2. Redistributions in binary form must reproduce the above copyright
       notice, this list of conditions and the following disclaimer in the
       documentation and/or other materials provided with the distribution.

    3. The name of the author may not be used to endorse or promote products
       derived from this software without specific prior written permission.

    THIS SOFTWARE IS PROVIDED BY THE AUTHOR ``AS IS'' AND ANY EXPRESS OR
    IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
    OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.
    IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR ANY DIRECT, INDIRECT,
    INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT
    NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
    DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
    THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
    (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF
    THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
*/

