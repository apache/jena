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

import java.io.StringReader;
import java.io.StringWriter;
import java.util.Calendar;
import java.util.GregorianCalendar ;

import com.hp.hpl.jena.datatypes.xsd.AbstractDateTime;
import com.hp.hpl.jena.datatypes.xsd.XSDDatatype;
import com.hp.hpl.jena.datatypes.xsd.XSDDateTime;
import com.hp.hpl.jena.rdf.model.* ;

import junit.framework.TestCase;
import junit.framework.TestSuite;
import org.junit.Assert ;

/**
 * Tests behaviour of the AbstractDateTime support, specifically for 
 * comparison operations. This complements the main tests in
 * TestTypedLiterals.
 */
public class TestDateTime extends TestCase {
    /**
     * Boilerplate for junit
     */ 
    public TestDateTime( String name ) {
        super( name ); 
    }
    
    /**
     * This is its own test suite
     */
    public static TestSuite suite() {
        return new TestSuite( TestDateTime.class ); 
    }  
    
    static final XSDDateTime time0 = makeDateTime( "2009-08-13T17:54:40.348Z" );
    static final XSDDateTime time1 = makeDateTime( "2009-08-13T18:54:39Z" );
    static final XSDDateTime time2 = makeDateTime( "2009-08-13T18:54:40Z" );
    static final XSDDateTime time3 = makeDateTime( "2009-08-13T18:54:40.348Z" );
    static final XSDDateTime time4 = makeDateTime( "2009-08-13T18:54:40.505Z" );
    static final XSDDateTime time5 = makeDateTime( "2009-08-13T18:54:40.77Z" );
    static final XSDDateTime time6 = makeDateTime( "2009-08-13T18:54:40.88Z" );
    static final XSDDateTime time7 = makeDateTime( "2009-08-13T18:54:40.989Z" );
    static final XSDDateTime time8 = makeDateTime( "2009-08-13T19:54:40.989Z" );
    
    static final XSDDateTime time9 = makeDateTime( "2009-08-13T18:54:40.077Z" );
    static final XSDDateTime time10 = makeDateTime( "2009-08-13T18:54:40.770Z" );
    
    static XSDDateTime makeDateTime(String time) {
        return (XSDDateTime) XSDDatatype.XSDdateTime.parse(time);
    }
    
    public void testXSDOrder() {
        assertEquals( time0.compare(time1), AbstractDateTime.LESS_THAN);
        assertEquals( time1.compare(time2), AbstractDateTime.LESS_THAN);
        assertEquals( time2.compare(time3), AbstractDateTime.LESS_THAN);
        assertEquals( time3.compare(time4), AbstractDateTime.LESS_THAN);
        assertEquals( time4.compare(time5), AbstractDateTime.LESS_THAN);
        assertEquals( time5.compare(time6), AbstractDateTime.LESS_THAN);
        assertEquals( time6.compare(time7), AbstractDateTime.LESS_THAN);
        assertEquals( time7.compare(time8), AbstractDateTime.LESS_THAN);
        
        assertEquals( time9.compare(time5), AbstractDateTime.LESS_THAN);
        assertEquals( time9.compare(time3), AbstractDateTime.LESS_THAN);
        assertEquals( time9.compare(time2), AbstractDateTime.GREATER_THAN);
        
        assertEquals( time5.compare(time10), AbstractDateTime.EQUAL);
    }
    
    public void testJavaOrder() {
        assertEquals( time0.compareTo(time1), AbstractDateTime.LESS_THAN);
        assertEquals( time1.compareTo(time2), AbstractDateTime.LESS_THAN);
        assertEquals( time2.compareTo(time3), AbstractDateTime.LESS_THAN);
        assertEquals( time3.compareTo(time4), AbstractDateTime.LESS_THAN);
        assertEquals( time4.compareTo(time5), AbstractDateTime.LESS_THAN);
        assertEquals( time5.compareTo(time6), AbstractDateTime.LESS_THAN);
        assertEquals( time6.compareTo(time7), AbstractDateTime.LESS_THAN);
        assertEquals( time7.compareTo(time8), AbstractDateTime.LESS_THAN);

    }
    
    public void testRoundTripping1() {
        Model m = ModelFactory.createDefaultModel();
        Property startTime = m.createProperty("http://jena.hpl.hp.com/test#startTime");

        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.MILLISECOND, 0);
        Literal xsdlit0 = m.createTypedLiteral(cal);

        Resource event = m.createResource();
        event.addProperty(startTime, xsdlit0);

        StringWriter sw = new StringWriter();
        m.write(sw);
        StringReader reader = new StringReader(sw.toString());
        Model m1 = ModelFactory.createDefaultModel();
        m1.read(reader, null);

        assertTrue( m.isIsomorphicWith(m1) );

        Literal xsdlit1 = m1.listStatements().next().getObject().as(Literal.class);
        assertEquals(xsdlit0, xsdlit1);
    }
    
    // Test that the string and calendar versions are the same.  
    public void testRoundTripping2() {
        //String lex = "2013-04-16T15:40:07.3Z" ;
        testCalendarRT(1366126807300L);
    }
    
    public void testRoundTripping3() {
        //String lex = "2013-04-16T15:40:07.31Z" ;
        testCalendarRT(1366126807310L);
    }

    public void testRoundTripping4() {
        //String lex = "2013-04-16T15:40:07.301Z" ;
        testCalendarRT(1366126807301L);
    }

    private static void testCalendarRT(long value)
    {
        Calendar cal=GregorianCalendar.getInstance();
        cal.setTimeInMillis(value);
        Literal lit1 = ResourceFactory.createTypedLiteral(cal) ;
        Literal lit2 = ResourceFactory.createTypedLiteral(lit1.getLexicalForm(), lit1.getDatatype()) ;

        Assert.assertEquals("equals: ", lit1, lit2) ;
        Assert.assertEquals("hash code: ", lit1.hashCode(), lit2.hashCode()); 
    }
    

}
