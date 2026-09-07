/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 *
 *   SPDX-License-Identifier: Apache-2.0
 */

package org.apache.jena.rdf.model;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.TimeZone;

import org.apache.jena.datatypes.RDFDatatype;
import org.apache.jena.datatypes.xsd.XSDDatatype;

public class TestResourceFactory {

    class TestFactory implements ResourceFactory.Interface {

        Resource resource;

        TestFactory(final Resource r) {
            resource = r;
        }

        @Override
        public Literal createLangLiteral(final String string, final String lang) {
            return null;
        }

        @Override
        public Literal createStringLiteral(final String string) {
            return null;
        }

        @Override
        public Property createProperty(final String uriref) {
            return null;
        }

        @Override
        public Property createProperty(final String namespace, final String localName) {
            return null;
        }

        @Override
        public Resource createResource() {
            return resource;
        }

        @Override
        public Resource createResource(final String uriref) {
            return null;
        }

        @Override
        public StatementTerm createStatementTerm(final Statement statement) {
            return null;
        }

        @Override
        public Statement createStatement(final Resource subject, final Property predicate, final RDFNode object) {
            return null;
        }

        @Override
        public Literal createTypedLiteral(final Object value) {
            return null;
        }

        @Override
        public Literal createTypedLiteral(final String string, final RDFDatatype datatype) {
            return null;
        }
    }

    static final String uri1 = "http://example.org/example#a1";

    static final String uri2 = "http://example.org/example#a2";

    @Test
    public void testCreateLiteral() {
        final Literal l = ResourceFactory.createPlainLiteral("lex");
        assertTrue(l.getLexicalForm().equals("lex"));
        assertTrue(l.getLanguage().equals(""));
        assertNotNull(l.getDatatype());
        assertNotNull(l.getDatatypeURI());
    }

    @Test
    public void testCreateProperty() {
        final Property p1 = ResourceFactory.createProperty(TestResourceFactory.uri1);
        assertTrue(p1.getURI().equals(TestResourceFactory.uri1));
        final Property p2 = ResourceFactory.createProperty(TestResourceFactory.uri1, "2");
        assertTrue(p2.getURI().equals(TestResourceFactory.uri1 + "2"));
    }

    @Test
    public void testCreateResource() {
        Resource r1 = ResourceFactory.createResource();
        assertTrue(r1.isAnon());
        final Resource r2 = ResourceFactory.createResource();
        assertTrue(r2.isAnon());
        assertTrue(!r1.equals(r2));

        r1 = ResourceFactory.createResource(TestResourceFactory.uri1);
        assertTrue(r1.getURI().equals(TestResourceFactory.uri1));
    }

    @Test
    public void testCreateStatement() {
        final Resource s = ResourceFactory.createResource();
        final Property p = ResourceFactory.createProperty(TestResourceFactory.uri2);
        final Resource o = ResourceFactory.createResource();
        final Statement stmt = ResourceFactory.createStatement(s, p, o);
        assertTrue(stmt.getSubject().equals(s));
        assertTrue(stmt.getPredicate().equals(p));
        assertTrue(stmt.getObject().equals(o));
    }

    @Test
    public void testCreateTypedLiteral() {
        final Literal l = ResourceFactory.createTypedLiteral("22", XSDDatatype.XSDinteger);
        assertTrue(l.getLexicalForm().equals("22"));
        assertTrue(l.getLanguage().equals(""));
        assertTrue(l.getDatatype() == XSDDatatype.XSDinteger);
        assertTrue(l.getDatatypeURI().equals(XSDDatatype.XSDinteger.getURI()));
    }

    @Test
    public void testCreateTypedLiteralObject() {
        final Literal l = ResourceFactory.createTypedLiteral(22);
        assertEquals("22", l.getLexicalForm());
        assertEquals("", l.getLanguage());
        assertEquals(XSDDatatype.XSDint, l.getDatatype());
    }

    @Test
    public void testCreateTypedLiteralOverload() {
        final Calendar testCal = new GregorianCalendar(TimeZone.getTimeZone("GMT"));
        testCal.set(1999, 4, 30, 15, 9, 32);
        testCal.set(Calendar.MILLISECOND, 0); // ms field can be undefined on
        // Linux
        final Literal lc = ResourceFactory.createTypedLiteral(testCal);
        assertEquals(ResourceFactory.createTypedLiteral("1999-05-30T15:09:32Z", XSDDatatype.XSDdateTime), lc, "calendar overloading test");

    }

    @Test
    public void testCreateStatementTerm() {
        final Resource s = ResourceFactory.createResource();
        final Property p = ResourceFactory.createProperty(TestResourceFactory.uri2);
        final Resource o = ResourceFactory.createResource();
        final Statement stmt0 = ResourceFactory.createStatement(s, p, o);

        final StatementTerm stmtTerm = ResourceFactory.createStatementTerm(stmt0);
        assertEquals(stmt0, stmtTerm.getStatement());

        final Statement stmt = stmtTerm.getStatement();
        assertTrue(stmt.getSubject().equals(s));
        assertTrue(stmt.getPredicate().equals(p));
        assertTrue(stmt.getObject().equals(o));
    }

    @Test
    public void testGetInstance() {
        ResourceFactory.getInstance();
        final Resource r1 = ResourceFactory.createResource();
        assertTrue(r1.isAnon());
        final Resource r2 = ResourceFactory.createResource();
        assertTrue(r2.isAnon());
        assertTrue(!r1.equals(r2));
    }

    @Test
    public void testSetInstance() {
        final Resource r = ResourceFactory.createResource();
        final ResourceFactory.Interface oldFactory = ResourceFactory.getInstance();
        final ResourceFactory.Interface factory = new TestFactory(r);
        try {
            ResourceFactory.setInstance(factory);
            assertTrue(factory.equals(ResourceFactory.getInstance()));
            assertTrue(ResourceFactory.createResource() == r);
        } finally {
            ResourceFactory.setInstance(oldFactory);
        }
    }
}
