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
import org.junit.jupiter.params.ParameterizedClass;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.ArrayList;
import java.util.List;

import org.apache.jena.rdf.model.helpers.ModelHelper;
import org.apache.jena.test.JenaTestLib;

/**
 * This class tests various properties of RDFNodes.
 */
@ParameterizedClass(name = "{0}")
@MethodSource("org.apache.jena.rdf.model.helpers.ModelCreators#creators")
public class TestRDFNodes extends AbstractModelTestBase {

    @Test
    public void testInModel() {
        final Model m1 = modelWithStatements("");
        final Model m2 = modelWithStatements("");
        final Resource r1 = ModelHelper.resource(m1, "r1");
        final Resource r2 = ModelHelper.resource(m1, "_r2");
        /* */
        assertEquals(r1.getModel(), m1);
        assertEquals(r2.getModel(), m1);
        assertFalse(r1.isAnon());
        assertTrue(r2.isAnon());
        /* */
        assertEquals(r1.inModel(m2).getModel(), m2);
        assertEquals(r2.inModel(m2).getModel(), m2);
        /* */
        assertEquals(r1, r1.inModel(m2));
        assertEquals(r2, r2.inModel(m2));
    }

    @Test
    public void testIsAnon() {
        final Model m = modelWithStatements("");
        assertEquals(false, m.createResource("eh:/foo").isAnon());
        assertEquals(true, m.createResource().isAnon());
        assertEquals(false, m.createTypedLiteral(17).isAnon());
        assertEquals(false, m.createTypedLiteral("hello").isAnon());
    }

    @Test
    public void testIsLiteral() {
        final Model m = modelWithStatements("");
        assertEquals(false, m.createResource("eh:/foo").isLiteral());
        assertEquals(false, m.createResource().isLiteral());
        assertEquals(true, m.createTypedLiteral(17).isLiteral());
        assertEquals(true, m.createTypedLiteral("hello").isLiteral());
    }

    @Test
    public void testIsResource() {
        final Model m = modelWithStatements("");
        Statement stmt = ModelHelper.statement("S P O");
        StatementTerm tripleTerm = m.createStatementTerm(stmt);
        assertEquals(true, m.createResource("eh:/foo").isResource());
        assertEquals(true, m.createResource().isResource());
        assertEquals(false, m.createTypedLiteral(17).isResource());
        assertEquals(false, m.createTypedLiteral("hello").isResource());
        assertEquals(false, tripleTerm.isResource());
    }

    @Test
    public void testIsURIResource() {
        final Model m = modelWithStatements("");
        assertEquals(true, m.createResource("eh:/foo").isURIResource());
        assertEquals(false, m.createResource().isURIResource());
        assertEquals(false, m.createTypedLiteral(17).isURIResource());
        assertEquals(false, m.createTypedLiteral("hello").isURIResource());
    }

    @Test
    public void testIsStatementTerm1() {
        final Model m = modelWithStatements("");
        Statement stmt = ModelHelper.statement("S P O");
        StatementTerm tripleTerm = m.createStatementTerm(stmt);
        assertEquals(false, m.createResource("eh:/foo").isStatementTerm());
        assertEquals(false, m.createResource().isStatementTerm());
        assertEquals(false, m.createTypedLiteral(17).isStatementTerm());
        assertEquals(false, m.createTypedLiteral("hello").isStatementTerm());
        assertEquals(true, tripleTerm.isStatementTerm());
    }

    @Test
    public void testIsStatementTerm2() {
        final Model m = modelWithStatements("");
        Statement stmt = ModelHelper.statement("S P O");
        StatementTerm tripleTerm = m.createStatementTerm(stmt);
        assertEquals(false, tripleTerm.isAnon());
        assertEquals(false, tripleTerm.isURIResource());
        assertEquals(false, tripleTerm.isLiteral());
        assertEquals(false, tripleTerm.isResource());
        assertEquals(true, tripleTerm.isStatementTerm());
    }

    @Test
    public void testLiteralAsResourceThrows() {
        final Model m = modelWithStatements("");
        final Resource r = m.createResource("eh:/spoo");
        try {
            r.asLiteral();
            fail("should not be able to do Resource.asLiteral()");
        } catch (final LiteralRequiredException e) {}
    }

    @Test
    public void testRDFNodeAsLiteral() {
        final Model m = modelWithStatements("");
        final Literal l = m.createLiteral("hello, world");
        assertSame(l, l.asLiteral());
    }

    @Test
    public void testRDFNodeAsResource() {
        final Model m = modelWithStatements("");
        final Resource r = m.createResource("eh:/spoo");
        assertSame(r, r.asResource());
    }

    @Test
    public void testRDFVisitor() {
        final List<String> history = new ArrayList<>();
        final Model m = ModelFactory.createDefaultModel();
        final RDFNode S = m.createResource();
        final RDFNode P = m.createProperty("eh:PP");
        final RDFNode O = m.createLiteral("LL");
        final Statement stmt = m.createStatement((Resource)S, (Property)P, O);
        final RDFNode ST = m.createStatementTerm(stmt);

        /* */
        final RDFVisitor rv = new RDFVisitor() {
            @Override
            public Object visitBlank(final Resource R, final AnonId id) {
                history.add("blank");
                assertTrue(R == S, "must visit correct node");
                assertEquals(R.getId(), id, "must have correct field");
                return "blank result";
            }

            @Override
            public Object visitLiteral(final Literal L) {
                history.add("literal");
                assertTrue(L == O, "must visit correct node");
                return "literal result";
            }

            @Override
            public Object visitURI(final Resource R, final String uri) {
                history.add("uri");
                assertTrue(R == P, "must visit correct node");
                assertEquals(R.getURI(), uri, "must have correct field");
                return "uri result";
            }

            @Override
            public Object visitStmt(StatementTerm statementTerm, Statement statement) {
                history.add("statementTerm");
                return "statement term result";
            }
        };
        /* */
        assertEquals("blank result", S.visitWith(rv));
        assertEquals("uri result", P.visitWith(rv));
        assertEquals("literal result", O.visitWith(rv));
        assertEquals("statement term result", ST.visitWith(rv));

        assertEquals(JenaTestLib.listOfStrings("blank uri literal statementTerm"), history);
    }

    @Test
    public void testRemoveAllBoring() {
        final Model m1 = modelWithStatements("x P a; y Q b");
        final Model m2 = modelWithStatements("x P a; y Q b");
        ModelHelper.resource(m2, "x").removeAll(ModelHelper.property(m2, "Z"));
        ModelHelper.assertIsoModels("m2 should be unchanged", m1, m2);
    }

    @Test
    public void testRemoveAllRemoves() {
        final String ps = "x P a; x P b", rest = "x Q c; y P a; y Q b";
        final Model m = modelWithStatements(ps + "; " + rest);
        final Resource r = ModelHelper.resource(m, "x");
        final Resource r2 = r.removeAll(ModelHelper.property(m, "P"));
        assertSame(r, r2, "removeAll should deliver its receiver");
        ModelHelper.assertIsoModels("x's P-values should go", modelWithStatements(rest), m);
    }

    @Test
    public void testResourceAsLiteralThrows() {
        final Model m = modelWithStatements("");
        final Literal l = m.createLiteral("hello, world");
        try {
            l.asResource();
            fail("should not be able to do Literal.asResource()");
        } catch (final ResourceRequiredException e) {}
    }
}
