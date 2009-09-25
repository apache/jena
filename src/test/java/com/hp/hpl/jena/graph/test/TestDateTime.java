/******************************************************************
 * File:        TestAbstractDateTime.java
 * Created by:  Dave Reynolds
 * Created on:  23 Aug 2009
 * 
 * (c) Copyright 2009, Hewlett-Packard Development Company, LP
 * [See end of file]
 * $Id: TestDateTime.java,v 1.2 2009-09-25 09:58:14 der Exp $
 *****************************************************************/

package com.hp.hpl.jena.graph.test;

import java.io.StringReader;
import java.io.StringWriter;
import java.util.Calendar;

import com.hp.hpl.jena.datatypes.xsd.AbstractDateTime;
import com.hp.hpl.jena.datatypes.xsd.XSDDatatype;
import com.hp.hpl.jena.datatypes.xsd.XSDDateTime;
import com.hp.hpl.jena.rdf.model.Literal;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.Resource;

import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * Tests behaviour of the AbstractDateTime support, specifically for 
 * comparison operations. This complements the main tests in
 * TestTypedLiterals.
 * 
 * @author <a href="mailto:der@hplb.hpl.hp.com">Dave Reynolds</a>
 * @version $Revision: 1.2 $
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
    
    public void testRoundTripping() {
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
}


/*
    (c) Copyright 2009 Hewlett-Packard Development Company, LP
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
