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

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedClass;
import org.junit.jupiter.params.provider.MethodSource;

import org.apache.jena.datatypes.xsd.XSDDatatype;

@ParameterizedClass(name = "{0}")
@MethodSource("org.apache.jena.rdf.model.helpers.ModelCreators#creators")
public class TestStatementCreation extends AbstractModelTestBase {

    static final String subjURI = "http://aldabaran.hpl.hp.com/foo";
    static final String predURI = "http://aldabaran.hpl.hp.com/bar";

    protected Resource r;
    protected Property p;

    @Override
    @BeforeEach
    public void setUp() {
        super.setUp();
        r = model.createResource(TestStatementCreation.subjURI);
        p = model.createProperty(TestStatementCreation.predURI);
    }

    @Override
    @AfterEach
    public void tearDown() {
        r = null;
        p = null;
        super.tearDown();
    }

    @Test
    public void testCreateStatementByteMax() {
        final Statement s = model.createLiteralStatement(r, p, Byte.MAX_VALUE);
        assertEquals(r, s.getSubject());
        assertEquals(p, s.getPredicate());
        assertEquals(Byte.MAX_VALUE, s.getByte());
    }

    @Test
    public void testCreateStatementChar() {
        final Statement s = model.createLiteralStatement(r, p, '$');
        assertEquals(r, s.getSubject());
        assertEquals(p, s.getPredicate());
        assertEquals('$', s.getChar());
    }

    @Test
    public void testCreateStatementDouble() {
        final Statement s = model.createStatement(r, p, model.createTypedLiteral(12345.67890d));
        assertEquals(r, s.getSubject());
        assertEquals(p, s.getPredicate());
        assertEquals(12345.67890d, s.getDouble(), 0.0000005);
    }

    @Test
    public void testCreateStatementFactory() {
        final LitTestObj tv = new LitTestObj(Long.MIN_VALUE);
        final Statement s = model.createLiteralStatement(r, p, tv);
        assertEquals(r, s.getSubject());
        assertEquals(p, s.getPredicate());
        // assertEquals(tv, s.getObject( new LitTestObjF() ) );
    }

    @Test
    public void testCreateStatementFloat() {
        final Statement s = model.createStatement(r, p, model.createTypedLiteral(123.456f));
        assertEquals(r, s.getSubject());
        assertEquals(p, s.getPredicate());
        assertEquals(123.456f, s.getFloat(), 0.0005);
    }

    @Test
    public void testCreateStatementIntMax() {
        final Statement s = model.createLiteralStatement(r, p, Integer.MAX_VALUE);
        assertEquals(r, s.getSubject());
        assertEquals(p, s.getPredicate());
        assertEquals(Integer.MAX_VALUE, s.getInt());
    }

    @Test
    public void testCreateStatementLongMax() {
        final Statement s = model.createLiteralStatement(r, p, Long.MAX_VALUE);
        assertEquals(r, s.getSubject());
        assertEquals(p, s.getPredicate());
        assertEquals(Long.MAX_VALUE, s.getLong());
    }

    @Test
    public void testCreateStatementResource() {
        final Resource tv = model.createResource();
        final Statement s = model.createStatement(r, p, tv);
        assertEquals(r, s.getSubject());
        assertEquals(p, s.getPredicate());
        assertEquals(tv, s.getResource());
    }

    @Test
    public void testCreateStatementShortMax() {
        final Statement s = model.createLiteralStatement(r, p, Short.MAX_VALUE);
        assertEquals(r, s.getSubject());
        assertEquals(p, s.getPredicate());
        assertEquals(Short.MAX_VALUE, s.getShort());
    }

    @Test
    public void testCreateStatementString() {
        final String string = "this is a plain string", lang = "en";
        final Statement s = model.createStatement(r, p, string);
        assertEquals(r, s.getSubject());
        assertEquals(p, s.getPredicate());
        assertEquals(string, s.getString());
        assertEquals(lang, model.createStatement(r, p, string, lang).getLanguage());
    }

    @Test
    public void testCreateStatementTrue() {
        final Statement s = model.createLiteralStatement(r, p, true);
        assertEquals(r, s.getSubject());
        assertEquals(p, s.getPredicate());
        assertEquals(true, s.getBoolean());
    }

    @Test
    public void testCreateStatementTypeLiteral() {
        final Model model = ModelFactory.createDefaultModel();
        final Resource R = model.createResource("http://example/r");
        final Property P = model.createProperty("http://example/p");
        model.add(R, P, "2", XSDDatatype.XSDinteger);
        final Literal L = ResourceFactory.createTypedLiteral("2", XSDDatatype.XSDinteger);
        assertTrue(model.contains(R, P, L));
        assertFalse(model.contains(R, P, "2"));
    }
}
