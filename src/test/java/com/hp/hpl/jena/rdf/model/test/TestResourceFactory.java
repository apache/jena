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

import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.TimeZone;

import com.hp.hpl.jena.datatypes.RDFDatatype;
import com.hp.hpl.jena.datatypes.xsd.XSDDatatype;
import com.hp.hpl.jena.rdf.model.*;

import junit.framework.*;

public class TestResourceFactory extends TestCase {
    
    static final String uri1 = "http://example.org/example#a1";
    static final String uri2 = "http://example.org/example#a2";
    
    public static TestSuite suite() {
        return new TestSuite(TestResourceFactory.class);
    }

    public TestResourceFactory(String name) {
        super(name);
    }

    public void testCreateResource() {
        Resource r1 = ResourceFactory.createResource();
        assertTrue(r1.isAnon());
        Resource r2 = ResourceFactory.createResource();
        assertTrue(r2.isAnon());
        assertTrue(!r1.equals(r2));
        
        r1 = ResourceFactory.createResource(uri1);
        assertTrue(r1.getURI().equals(uri1));
    }

    public void testCreateProperty() {
        Property p1 = ResourceFactory.createProperty(uri1);
        assertTrue(p1.getURI().equals(uri1));
        Property p2 = ResourceFactory.createProperty(uri1, "2");
        assertTrue(p2.getURI().equals(uri1+"2"));
    }

    public void testCreateLiteral()
    {
        Literal l = ResourceFactory.createPlainLiteral("lex") ;
        assertTrue(l.getLexicalForm().equals("lex")) ;
        assertTrue(l.getLanguage().equals("")) ;
        assertNull(l.getDatatype()) ;
        assertNull(l.getDatatypeURI()) ;
    }
    
    public void testCreateTypedLiteral()
    {
        Literal l = ResourceFactory.createTypedLiteral("22", XSDDatatype.XSDinteger) ;
        assertTrue(l.getLexicalForm().equals("22")) ;
        assertTrue(l.getLanguage().equals("")) ;
        assertTrue(l.getDatatype()==XSDDatatype.XSDinteger) ;
        assertTrue(l.getDatatypeURI().equals(XSDDatatype.XSDinteger.getURI())) ;
        
    }
    
    public void testCreateTypedLiteralObject()
    {
        Literal l = ResourceFactory.createTypedLiteral(new Integer(22)) ;
        assertEquals("22", l.getLexicalForm()) ;
        assertEquals("", l.getLanguage()) ;
        assertEquals(XSDDatatype.XSDint, l.getDatatype()) ;
    }
    
    public void testCreateTypedLiteralOverload() {
        Calendar testCal = new GregorianCalendar(TimeZone.getTimeZone("GMT"));
        testCal.set(1999, 4, 30, 15, 9, 32);
        testCal.set(Calendar.MILLISECOND, 0);   // ms field can be undefined on Linux
        Literal lc = ResourceFactory.createTypedLiteral(testCal);
        assertEquals("calendar overloading test", 
                ResourceFactory.createTypedLiteral("1999-05-30T15:09:32Z", XSDDatatype.XSDdateTime), lc );
        
    }

    public void testCreateStatement() {
        Resource s = ResourceFactory.createResource();
        Property p = ResourceFactory.createProperty(uri2);
        Resource o = ResourceFactory.createResource();
        Statement stmt = ResourceFactory.createStatement(s, p, o);
        assertTrue(stmt.getSubject().equals(s));
        assertTrue(stmt.getPredicate().equals(p));
        assertTrue(stmt.getObject().equals(o));
    }

    public void testGetInstance() {
        ResourceFactory.Interface factory = ResourceFactory.getInstance();
        Resource r1 = ResourceFactory.createResource();
        assertTrue(r1.isAnon());
        Resource r2 = ResourceFactory.createResource();
        assertTrue(r2.isAnon());
        assertTrue(!r1.equals(r2));
    }

    public void testSetInstance() {
        Resource r = ResourceFactory.createResource();
        ResourceFactory.Interface factory = new TestFactory(r);
        ResourceFactory.setInstance(factory);
        assertTrue(factory.equals(ResourceFactory.getInstance()));
        assertTrue(ResourceFactory.createResource() == r);
    }

    class TestFactory implements ResourceFactory.Interface {

        Resource resource;

        TestFactory(Resource r) {
            resource = r;
        }

        @Override
        public Resource createResource() {
            return resource;
        }

        @Override
        public Resource createResource(String uriref) {
            return null;
        }
        
        @Override
        public Literal createPlainLiteral( String string ) {
            return null;
        }

        @Override
        public Literal createTypedLiteral(String string, RDFDatatype datatype)
        {
            return null ;
        }

        @Override
        public Literal createTypedLiteral(Object value)
        {
            return null ;
        }

        @Override
        public Property createProperty(String uriref) {
            return null;
        }

        @Override
        public Property createProperty(String namespace, String localName) {
            return null;
        }

        @Override
        public Statement createStatement(
            Resource subject,
            Property predicate,
            RDFNode object) {
            return null;
        }

    }
}
