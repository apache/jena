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

import org.apache.jena.rdf.model.helpers.ModelHelper;
import org.apache.jena.vocabulary.RDF;

@ParameterizedClass(name = "{0}")
@MethodSource("org.apache.jena.rdf.model.helpers.ModelCreators#creators")
public class TestStatementTerms extends AbstractModelTestBase {

    @Test
    public void testStatementTerms() {
        String fakeURI = "fake:URI";
        Resource S = model.createResource();
        Property P = ModelHelper.property(model, "PP");
        RDFNode O = model.createTypedLiteral("42", fakeURI);

        Statement stmt = model.createStatement(S, P, O);
        assertTrue(model.isEmpty());

        StatementTerm stmtTerm = model.createStatementTerm(stmt);
        assertTrue(model.isEmpty());

        assertEquals(S, stmtTerm.getStatement().getSubject());
        assertEquals(P, stmtTerm.getStatement().getPredicate());
        assertEquals(O, stmtTerm.getStatement().getObject());
    }

    private static StatementTerm create(Model model) {
        String fakeURI = "fake:URI";
        Resource S = model.createResource();
        Property P = ModelHelper.property(model, "PP");
        RDFNode O = model.createTypedLiteral("42", fakeURI);

        Statement stmt = model.createStatement(S, P, O);
        StatementTerm stmtTerm = model.createStatementTerm(stmt);
        return stmtTerm;
    }

    @Test
    public void testStatementReifierAnon() {
        String fakeURI = "fake:URI";
        Resource S = model.createResource();
        Property P = ModelHelper.property(model, "PP");
        RDFNode O = model.createTypedLiteral("42", fakeURI);
        Statement stmt = model.createStatement(S, P, O);

        Resource r = model.createReifier(stmt);
        assertFalse(model.isEmpty());
        assertEquals(1, model.size());

        Statement s = model.listStatements().next();

        RDFNode x = s.getObject();
        assertTrue(s.getSubject().isAnon());
        assertTrue(s.getPredicate().equals(RDF.reifies));
        assertTrue(s.getObject().isStatementTerm());

        StatementTerm st = s.getObject().asStatementTerm();
        assertTrue(st != null);
        assertEquals(st.getStatement(), stmt);
    }

    @Test
    public void testStatementReifierResource() {
        String fakeURI = "fake:URI";
        String reifURI = "reifier:URI";

        Resource reifier = model.createResource(reifURI);

        Resource S = model.createResource();
        Property P = ModelHelper.property(model, "PP");
        RDFNode O = model.createTypedLiteral("42", fakeURI);
        Statement stmt = model.createStatement(S, P, O);

        Resource r = model.createReifier(reifier, stmt);
        assertEquals(reifURI, r.getURI());

        assertFalse(model.isEmpty());
        assertEquals(1, model.size());

        StatementTerm st = r.getProperty(RDF.reifies).getObject().asStatementTerm();
        assertTrue(st != null);
        assertEquals(st.getStatement(), stmt);
    }
}
