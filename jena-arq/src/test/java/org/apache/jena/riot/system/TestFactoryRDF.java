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

package org.apache.jena.riot.system;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import org.apache.jena.datatypes.xsd.XSDDatatype;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.Triple;
import org.apache.jena.riot.lang.LabelToNode;
import org.apache.jena.sparql.core.Quad;
import org.apache.jena.vocabulary.RDF;

public class TestFactoryRDF {
    protected FactoryRDF factory = new FactoryRDFStd(LabelToNode.createUseLabelAsGiven());

    @Test public void factoryRDF_blanknode_01() {
        Node n1 = factory.createBlankNode();
        assertTrue(n1.isBlank());
        Node n2 = factory.createBlankNode();
        assertNotEquals(n1, n2);
    }

    @Test public void factoryRDF_blanknode_02() {
        Node n1 = factory.createBlankNode("ABCDE");
        assertTrue(n1.isBlank());
        Node n2 = factory.createBlankNode("ABCDE");
        assertEquals(n1, n2);
        assertEquals("ABCDE", n1.getBlankNodeLabel());
    }

    @Test public void factoryRDF_blanknode_03() {
        Node n1 = factory.createBlankNode(0x1234L, 0x5678L);
        assertTrue(n1.isBlank());
        Node n2 = factory.createBlankNode(0x1234L, 0x5678L);
        assertEquals(n1, n2);
        assertEquals("0000123400005678", n1.getBlankNodeLabel());
    }

    @Test public void factoryRDF_uri_02() {
        Node n = factory.createURI("http://example/");
        assertTrue(n.isURI());
        assertEquals("http://example/", n.getURI());
    }

    @Test public void factoryRDF_uri_03() {
        Node n = factory.createURI("_:abc");   // Blank node!
        assertTrue(n.isBlank());
        assertEquals("abc", n.getBlankNodeLabel());
    }

    @Test public void factoryRDF_literal_01() {
        Node n = factory.createStringLiteral("hello");
        assertTrue(n.isLiteral());
        assertEquals("hello", n.getLiteralLexicalForm());
        assertEquals(XSDDatatype.XSDstring, n.getLiteralDatatype());
        assertEquals("", n.getLiteralLanguage());
    }

    @Test public void factoryRDF_literal_02() {
        Node n = factory.createLangLiteral("xyz", "en");
        assertTrue(n.isLiteral());
        assertEquals("xyz", n.getLiteralLexicalForm());
        assertEquals(RDF.dtLangString, n.getLiteralDatatype());
        assertEquals("en", n.getLiteralLanguage());
    }

    @Test public void factoryRDF_literal_03() {
        Node n = factory.createTypedLiteral("1", XSDDatatype.XSDinteger);
        assertTrue(n.isLiteral());
        assertEquals("1", n.getLiteralLexicalForm());
        assertEquals(XSDDatatype.XSDinteger, n.getLiteralDatatype());
        assertEquals("", n.getLiteralLanguage());
    }

    @Test public void factoryRDF_triple_01() {
        Node s = factory.createURI("http://test/s");
        Node p = factory.createURI("http://test/p");
        Node o = factory.createURI("http://test/o");
        Triple triple = factory.createTriple(s, p, o);
        assertEquals(s, triple.getSubject());
        assertEquals(p, triple.getPredicate());
        assertEquals(o, triple.getObject());
    }

    @Test public void factoryRDF_quad_01() {
        Node g = factory.createURI("http://test/g");
        Node s = factory.createURI("http://test/s");
        Node p = factory.createURI("http://test/p");
        Node o = factory.createURI("http://test/o");
        Quad quad = factory.createQuad(g, s, p, o);
        assertEquals(g, quad.getGraph());
        assertEquals(s, quad.getSubject());
        assertEquals(p, quad.getPredicate());
        assertEquals(o, quad.getObject());
    }
}

