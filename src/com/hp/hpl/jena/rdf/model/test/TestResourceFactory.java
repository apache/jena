/*
  (c) Copyright 2002, Hewlett-Packard Company, all rights reserved.
  [See end of file]
  $Id: TestResourceFactory.java,v 1.4 2003-08-19 09:53:09 chris-dollin Exp $
*/

package com.hp.hpl.jena.rdf.model.test;

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

        public Resource createResource() {
            return resource;
        }

        public Resource createResource(String uriref) {
            return null;
        }
        
        public Literal createPlainLiteral( String string ) {
            return null;
        }

        public Property createProperty(String uriref) {
            return null;
        }

        public Property createProperty(String namespace, String localName) {
            return null;
        }

        public Statement createStatement(
            Resource subject,
            Property predicate,
            RDFNode object) {
            return null;
        }
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
