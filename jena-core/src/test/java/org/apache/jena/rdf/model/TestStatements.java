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

import org.apache.jena.graph.FrontsTriple;
import org.apache.jena.rdf.model.helpers.ModelHelper;
import org.apache.jena.test.JenaTestLib;
import org.apache.jena.vocabulary.RDF;

@ParameterizedClass(name = "{0}")
@MethodSource("org.apache.jena.rdf.model.helpers.ModelCreators#creators")
public class TestStatements extends AbstractModelTestBase {

    @Test
    public void testOtherStuff() {
        final Model A = createModel();
        final Model B = createModel();
        final Resource S = A.createResource("jena:S");
        final Resource R = A.createResource("jena:R");
        final Property P = A.createProperty("jena:P");
        final RDFNode O = A.createResource("jena:O");
        A.add(S, P, O);
        B.add(S, P, O);
        assertTrue(A.isIsomorphicWith(B), "X1");
        /* */
        A.add(R, RDF.subject, S);
        B.add(R, RDF.predicate, P);
        assertFalse(A.isIsomorphicWith(B), "X2");
        /* */
        A.add(R, RDF.predicate, P);
        B.add(R, RDF.subject, S);
        assertTrue(A.isIsomorphicWith(B), "X3");
        /* */
        A.add(R, RDF.object, O);
        B.add(R, RDF.type, RDF.Statement);
        assertFalse(A.isIsomorphicWith(B), "X4");
        /* */
        A.add(R, RDF.type, RDF.Statement);
        B.add(R, RDF.object, O);
        assertTrue(A.isIsomorphicWith(B), "X5");
    }

    @Test
    public void testPortingBlankNodes() {
        final Model B = createModel();
        final Resource anon = model.createResource();
        final Resource bAnon = anon.inModel(B);
        assertTrue(bAnon.isAnon(), "moved resource should still be blank");
        assertEquals(anon, bAnon, "move resource should equal original");
    }

    @Test
    public void testSet() {
        final Model A = createModel();
        createModel();
        final Resource S = A.createResource("jena:S");
        A.createResource("jena:R");
        final Property P = A.createProperty("jena:P");
        final RDFNode O = A.createResource("jena:O");
        final Statement spo = A.createStatement(S, P, O);
        A.add(spo);
        final Statement sps = A.createStatement(S, P, S);
        assertEquals(sps, spo.changeObject(S));
        assertFalse(A.contains(spo));
        assertTrue(A.contains(sps));
    }

    /**
     * Feeble test that toString'ing a Statement[Impl] will display the data-type of
     * its object if it has one.
     */
    @Test
    public void testStatementPrintsType() {
        final String fakeURI = "fake:URI";
        final Resource S = model.createResource();
        final Property P = ModelHelper.property(model, "PP");
        final RDFNode O = model.createTypedLiteral("42", fakeURI);
        final Statement st = model.createStatement(S, P, O);
        assertTrue(st.toString().indexOf(fakeURI) > 0);
    }

    @Test
    public void testStatmentMap1Selectors() {
        final Statement stmt = ModelHelper.statement("sub pred obj");
        assertEquals(ModelHelper.resource("sub"), stmt.getSubject());
        assertEquals(ModelHelper.resource("pred"), stmt.getPredicate());
        assertEquals(ModelHelper.resource("obj"), stmt.getObject());
    }

    /**
     * A resource created in one model and incorporated into a statement asserted
     * constructed by a different model should test equal to the resource extracted
     * from that statement, even if it's a bnode.
     */
    @Test
    public void testStuff() {
        final Model red = createModel();
        final Model blue = createModel();
        final Resource r = red.createResource();
        final Property p = red.createProperty("");
        final Statement s = blue.createStatement(r, p, r);
        assertEquals(r, s.getSubject(), "subject preserved");
        assertEquals(r, s.getObject(), "object preserved");
    }

    @Test
    public void testTripleWrapper() {
        JenaTestLib.assertInstanceOf(FrontsTriple.class, ModelHelper.statement(model, "s p o"));
    }
}
