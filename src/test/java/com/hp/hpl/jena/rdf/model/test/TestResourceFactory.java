/*
  (c) Copyright 2003, 2004, 2005, 2006, 2007, 2008, 2009 Hewlett-Packard Development Company, LP
  [See end of file]
  $Id: TestResourceFactory.java,v 1.1 2009-06-29 08:55:33 castagna Exp $
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

/*
    (c) Copyright 2003, 2004, 2005, 2006, 2007, 2008, 2009 Hewlett-Packard Development Company, LP
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
