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

package org.apache.jena.sparql.expr;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import org.apache.jena.datatypes.xsd.XSDDatatype;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.sparql.expr.nodevalue.NodeFunctions;
import org.apache.jena.sparql.sse.SSE;

/**
 * Tests for "RDFterm-equal" - fallback function for testing for the '='operator.
 * Extended for triple terms.
 * Replaced in SPARQL 1.2 by sameValue.
 */
@SuppressWarnings("removal")
public class TestNodeFunctionsMisc {
    @Test public void testRDFtermEquals1() {
        Node n1 = NodeFactory.createURI("xyz");
        Node n2 = NodeFactory.createLiteralString("xyz");
        assertFalse(NodeFunctions.rdfTermEqual11_legacy(n1, n2));
    }

    @Test public void testRDFtermEquals2() {
        Node n1 = NodeFactory.createLiteralLang("xyz", "en");
        Node n2 = NodeFactory.createLiteralLang("xyz", "EN");
        assertTrue(NodeFunctions.rdfTermEqual11_legacy(n1, n2));
    }

    @Test public void testRDFtermEquals3() {
        // Unextended - not known to be same (no language tag support).
        Node n1 = NodeFactory.createLiteralString("xyz");
        Node n2 = NodeFactory.createLiteralLang("xyz", "en");
        assertThrows(ExprEvalException.class, ()-> NodeFunctions.rdfTermEqual11_legacy(n1, n2) );
    }

    @Test public void testRDFtermEquals4() {
        // Unextended - not known to be same.
        Node n1 = NodeFactory.createLiteralDT("123", XSDDatatype.XSDinteger);
        Node n2 = NodeFactory.createLiteralDT("456", XSDDatatype.XSDinteger);
        assertThrows(ExprEvalException.class, ()-> assertTrue(NodeFunctions.rdfTermEqual11_legacy(n1, n2)) );
    }

    @Test public void testRDFtermEquals5() {
        Node n1 = SSE.parseNode("<<(:s :p 123)>>");
        Node n2 = SSE.parseNode("<<(:s :p 123)>>");
        assertTrue(NodeFunctions.rdfTermEqual11_legacy(n1, n2));
    }

    @Test public void testRDFtermEquals6() {
        Node n1 = SSE.parseNode("<<(:s :p1 123)>>");
        Node n2 = SSE.parseNode("<<(:s :p2 123)>>");
        assertFalse(NodeFunctions.rdfTermEqual11_legacy(n1, n2));
    }

    @Test public void testRDFtermEquals7() {
        Node n1 = SSE.parseNode("<<(:s :p <<(:a :b 'abc')>>)>>");
        Node n2 = SSE.parseNode("<<(:s :p <<(:a :b 123)>>)>>");
        assertThrows(ExprEvalException.class, ()-> NodeFunctions.rdfTermEqual11_legacy(n1, n2) );
    }

    @Test public void testRDFtermEquals8() {
        Node n1 = SSE.parseNode("<<(:s :p 123)>>");
        Node n2 = SSE.parseNode("<<(:s :p 'xyz')>>");
        assertThrows(ExprEvalException.class, ()-> assertFalse(NodeFunctions.rdfTermEqual11_legacy(n2, n1)) );
    }

    @Test public void testRDFtermEquals9() {
        Node n1 = SSE.parseNode("<<(:s :p 123)>>");
        Node n2 = SSE.parseNode("'xyz'");
        assertFalse(NodeFunctions.rdfTermEqual11_legacy(n1, n2));
        assertFalse(NodeFunctions.rdfTermEqual11_legacy(n2, n1));
    }
}
