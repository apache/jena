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

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedClass;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.List;

import org.apache.jena.rdf.model.helpers.ModelHelper;
import org.apache.jena.vocabulary.RDF;

@ParameterizedClass(name = "{0}")
@MethodSource("org.apache.jena.rdf.model.helpers.ModelCreators#creators")
public class TestSimpleListStatements extends AbstractModelTestBase {

    static boolean booleanValue = true;

    static char charValue = 'c';
    static long longValue = 456;
    static float floatValue = 5.67F;
    static double doubleValue = 6.78;
    static String stringValue = "stringValue";
    static String langValue = "en";

    public void checkReturns(final String things, final StmtIterator it) {
        final Model wanted = modelWithStatements(things);
        final Model got = modelWithStatements(it);
        if ( wanted.isIsomorphicWith(got) == false ) {
            fail("wanted " + wanted + " got " + got);
        }
    }

    public Model modelWithStatements(final StmtIterator it) {
        final Model m = createModel();
        while (it.hasNext()) {
            m.add(it.nextStatement());
        }
        return m;
    }

    @Override
    @BeforeEach
    public void setUp() {
        super.setUp();
        model.createResource("http://example.org/boolean").addLiteral(RDF.value,        booleanValue);
        model.createResource("http://example.org/char").addLiteral(RDF.value,           charValue);
        model.createResource("http://example.org/long").addLiteral(RDF.value,           longValue);
        model.createResource("http://example.org/float").addLiteral(RDF.value,          floatValue);
        model.createResource("http://example.org/double").addLiteral(RDF.value,         doubleValue);
        model.createResource("http://example.org/string").addProperty(RDF.value,        stringValue);
        model.createResource("http://example.org/langString").addProperty(RDF.value,    stringValue, langValue);
    }

    @Test
    public void testAll() {
        final StmtIterator iter = model.listStatements(null, null, (RDFNode)null);
        int i = 0;
        while (iter.hasNext()) {
            i++;
            iter.next();
        }
        assertEquals(7, i);
    }

    @Test
    public void testAllString() {
        final StmtIterator iter = model.listStatements(null, null, (String)null);
        int i = 0;
        while (iter.hasNext()) {
            i++;
            iter.next();
        }
        assertEquals(7, i);
    }

    @Test
    public void testBoolean() {
        final List<Statement> got = model.listLiteralStatements(null, null, TestSimpleListStatements.booleanValue).toList();
        assertEquals(1, got.size());
        final Statement it = got.get(0);
        assertEquals(ModelHelper.resource("http://example.org/boolean"), it.getSubject());
        assertEquals(model.createTypedLiteral(TestSimpleListStatements.booleanValue), it.getObject());
    }

    @Test
    public void testChar() {
        final List<Statement> got = model.listLiteralStatements(null, null, TestSimpleListStatements.charValue).toList();
        assertEquals(1, got.size());
        final Statement it = got.get(0);
        assertEquals(ModelHelper.resource("http://example.org/char"), it.getSubject());
        assertEquals(model.createTypedLiteral(TestSimpleListStatements.charValue), it.getObject());
    }

    @Test
    public void testDouble() {
        final List<Statement> got = model.listLiteralStatements(null, null, TestSimpleListStatements.doubleValue).toList();
        assertEquals(1, got.size());
        final Statement it = got.get(0);
        assertEquals(ModelHelper.resource("http://example.org/double"), it.getSubject());
        assertEquals(model.createTypedLiteral(TestSimpleListStatements.doubleValue), it.getObject());
    }

    @Test
    public void testFloat() {
        final List<Statement> got = model.listLiteralStatements(null, null, TestSimpleListStatements.floatValue).toList();
        assertEquals(1, got.size());
        final Statement it = got.get(0);
        assertEquals(ModelHelper.resource("http://example.org/float"), it.getSubject());
        assertEquals(model.createTypedLiteral(TestSimpleListStatements.floatValue), it.getObject());
    }

    @Test
    public void testLangString() {
        final StmtIterator iter = model.listStatements(null, null, TestSimpleListStatements.stringValue,
                                                       TestSimpleListStatements.langValue);
        int i = 0;
        while (iter.hasNext()) {
            i++;
            assertEquals(iter.nextStatement().getSubject().getURI(), "http://example.org/langString");
        }
        assertEquals(1, i);
    }

    @Test
    public void testListStatementsSPO() {

        final Resource A = ModelHelper.resource(model, "A"), X = ModelHelper.resource(model, "X");
        final Property P = ModelHelper.property(model, "P"), P1 = ModelHelper.property(model, "P1");
        final RDFNode O = ModelHelper.resource(model, "O"), Y = ModelHelper.resource(model, "Y");
        final String S1 = "S P O; S1 P O; S2 P O";
        final String S2 = "A P1 B; A P1 B; A P1 C";
        final String S3 = "X P1 Y; X P2 Y; X P3 Y";
        ModelHelper.modelAdd(model, S1);
        ModelHelper.modelAdd(model, S2);
        ModelHelper.modelAdd(model, S3);
        checkReturns(S1, model.listStatements(null, P, O));
        checkReturns(S2, model.listStatements(A, P1, (RDFNode)null));
        checkReturns(S3, model.listStatements(X, null, Y));
    }

    @Test
    public void testLong() {
        final List<Statement> got = model.listLiteralStatements(null, null, TestSimpleListStatements.longValue).toList();
        assertEquals(1, got.size());
        final Statement it = got.get(0);
        assertEquals(ModelHelper.resource("http://example.org/long"), it.getSubject());
        assertEquals(model.createTypedLiteral(TestSimpleListStatements.longValue), it.getObject());
    }

    @Test
    public void testString() {
        final StmtIterator iter = model.listStatements(null, null, TestSimpleListStatements.stringValue);
        int i = 0;
        while (iter.hasNext()) {
            i++;
            assertEquals(iter.nextStatement().getSubject().getURI(), "http://example.org/string");
        }
        assertEquals(1, i);
    }
}
