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

package org.apache.jena.sparql.expr;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

import org.apache.jena.datatypes.xsd.XSDDatatype;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.graph.TextDirection;
import org.apache.jena.query.ARQ;
import org.apache.jena.sparql.expr.nodevalue.NodeFunctions;
import org.apache.jena.sparql.graph.NodeConst;
import org.apache.jena.sparql.sse.SSE;
import org.apache.jena.vocabulary.XSD;

public class TestNodeFunctions {
    private static final double accuracyExact = 0.0d;
    private static final double accuracyClose = 0.000001d;

    @Test public void testSameTerm1() {
        Node n1 = NodeFactory.createLiteralString("xyz");
        Node n2 = NodeFactory.createLiteralString("xyz");
        assertTrue(NodeFunctions.sameTerm(n1, n2));
    }

    @Test public void testSameTerm2() {
        Node n1 = NodeFactory.createLiteralString("xyz");
        Node n2 = NodeFactory.createLiteralString("abc");
        assertFalse(NodeFunctions.sameTerm(n1, n2));
    }

    @Test public void testSameTerm3() {
        Node n1 = NodeFactory.createLiteralString("xyz");
        Node n2 = NodeFactory.createURI("xyz");
        assertFalse(NodeFunctions.sameTerm(n1, n2));
    }

    @Test public void testSameTerm4() {
        Node n1 = NodeFactory.createLiteralString("xyz");
        Node n2 = NodeFactory.createLiteralDT("xyz", XSDDatatype.XSDstring);
        assertTrue(NodeFunctions.sameTerm(n1, n2));
    }

    @Test public void testSameTerm5() {
        Node n1 = NodeFactory.createLiteralLang("xyz", "en");
        Node n2 = NodeFactory.createLiteralString("xyz");
        assertFalse(NodeFunctions.sameTerm(n1, n2));
    }

    @Test public void testSameTerm6() {
        Node n1 = NodeFactory.createLiteralLang("xyz", "en");
        Node n2 = NodeFactory.createLiteralLang("xyz", "EN");
        assertTrue(NodeFunctions.sameTerm(n1, n2));
    }

    @Test public void testRDFtermEquals1() {
        Node n1 = NodeFactory.createURI("xyz");
        Node n2 = NodeFactory.createLiteralString("xyz");
        assertFalse(NodeFunctions.rdfTermEquals(n1, n2));
    }

    @Test public void testRDFtermEquals2() {
        Node n1 = NodeFactory.createLiteralLang("xyz", "en");
        Node n2 = NodeFactory.createLiteralLang("xyz", "EN");
        assertTrue(NodeFunctions.rdfTermEquals(n1, n2));
    }

    @Test
    public void testRDFtermEquals3() {
        // Unextended - not known to be same (no language tag support).
        Node n1 = NodeFactory.createLiteralString("xyz");
        Node n2 = NodeFactory.createLiteralLang("xyz", "en");
        assertThrows(ExprEvalException.class,
                     ()-> NodeFunctions.rdfTermEquals(n1, n2) );
    }

    @Test
    public void testRDFtermEquals4() {
        // Unextended - not known to be same.
        Node n1 = NodeFactory.createLiteralDT("123", XSDDatatype.XSDinteger);
        Node n2 = NodeFactory.createLiteralDT("456", XSDDatatype.XSDinteger);
        assertThrows(ExprEvalException.class,
                     ()-> assertTrue(NodeFunctions.rdfTermEquals(n1, n2)) );
    }

    @Test
    public void testRDFtermEquals5() {
        Node n1 = SSE.parseNode("<<(:s :p 123)>>");
        Node n2 = SSE.parseNode("<<(:s :p 123)>>");
        assertTrue(NodeFunctions.rdfTermEquals(n1, n2));
    }

    @Test
    public void testRDFtermEquals6() {
        Node n1 = SSE.parseNode("<<(:s :p1 123)>>");
        Node n2 = SSE.parseNode("<<(:s :p2 123)>>");
        assertFalse(NodeFunctions.rdfTermEquals(n1, n2));
    }

    @Test
    public void testRDFtermEquals7() {
        Node n1 = SSE.parseNode("<<(:s :p <<(:a :b 'abc')>>)>>");
        Node n2 = SSE.parseNode("<<(:s :p <<(:a :b 123)>>)>>");
        assertThrows(ExprEvalException.class,
                     ()-> NodeFunctions.rdfTermEquals(n1, n2) );
    }

    @Test
    public void testRDFtermEquals8() {
        Node n1 = SSE.parseNode("<<(:s :p 123)>>");
        Node n2 = SSE.parseNode("<<(:s :p 'xyz')>>");
        assertThrows(ExprEvalException.class,
                     ()-> assertFalse(NodeFunctions.rdfTermEquals(n2, n1)) );
    }

    @Test
    public void testRDFtermEquals9() {
        Node n1 = SSE.parseNode("<<(:s :p 123)>>");
        Node n2 = SSE.parseNode("'xyz'");
        assertFalse(NodeFunctions.rdfTermEquals(n1, n2));
        assertFalse(NodeFunctions.rdfTermEquals(n2, n1));
    }

    @Test public void testStr1() {
        NodeValue nv = NodeValue.makeNodeInteger(56);
        NodeValue s = NodeFunctions.str(nv);
        assertEquals("56", s.getString());
    }

    @Test public void testStr2() {
        NodeValue nv = NodeValue.makeInteger(56);
        NodeValue s = NodeFunctions.str(nv);
        assertEquals("56", s.getString());
    }

    @Test public void testStr3() {
        NodeValue nv = NodeValue.makeNode("abc", "fr", (String)null);
        NodeValue s = NodeFunctions.str(nv);
        assertEquals("abc", s.getString());
    }

    // STR(BNODE())/strict
    @Test
    public void testStr4() {
        boolean b = ARQ.isTrue(ARQ.strictSPARQL);
        try {
            ARQ.set(ARQ.strictSPARQL, true);
            try {
                Node n = NodeFactory.createBlankNode();
                String s = NodeFunctions.str(n);
                fail("NodeFunctions.str did not fail");
            } catch (ExprEvalException ex) {}
        } finally {
            ARQ.set(ARQ.strictSPARQL, b);
        }
    }

    // STR(BNODE())/notStrict
    @Test
    public void testStr5() {
        Node n = NodeFactory.createBlankNode();
        String s = NodeFunctions.str(n);
        assertNotNull(s);
        assertEquals("_:"+n.getBlankNodeLabel(), s);
    }

    @Test public void testDatatype1() {
        NodeValue nv = NodeValue.makeInteger(5);
        Node n = nv.asNode();
        Node r = NodeFunctions.datatype(n);
        assertEquals(XSD.integer.asNode(), r);
    }

    @Test public void testDatatype2() {
        NodeValue nv = NodeValue.makeInteger(5);
        NodeValue r = NodeFunctions.datatype(nv);
        NodeValue e = NodeValue.makeNode(XSD.integer.asNode());
        assertEquals(e, r);
    }

    @Test public void testDatatype3() {
        NodeValue nv = NodeValue.makeString("abc");
        NodeValue r = NodeFunctions.datatype(nv);
        NodeValue e = NodeValue.makeNode(XSD.xstring.asNode());
        assertEquals(e, r);
    }

    @Test public void testDatatype4() {
        NodeValue nv = NodeValue.makeNode("abc", "fr", (String)null);
        // SPARQL 1.0
        // try {
        //   NodeValue r = NodeFunctions.datatype(nv);
        //   fail("Expect a type exception but call succeeded");
        // }
        // catch (ExprTypeException ex) {}
        // SPARQL 1.1 / RDF 1.1
        NodeValue r = NodeFunctions.datatype(nv);
        NodeValue e = NodeValue.makeNode(NodeConst.rdfLangString);
        assertEquals(e, r);
    }

    @Test
    public void testDatatype5() {
        NodeValue nv = NodeValue.makeNode(NodeFactory.createURI("http://example"));
        assertThrows(ExprTypeException.class,
                     ()-> NodeFunctions.datatype(nv) );
    }

    @Test
    public void testDatatype6() {
        NodeValue nv = NodeValue.makeNode(NodeFactory.createBlankNode());
        assertThrows(ExprTypeException.class,
                     ()-> NodeFunctions.datatype(nv) );
    }

    @Test public void testLang1() {
        Node n = NodeFactory.createLiteralLang("abc", "en-gb");
        // Jena5: Language tag formatting.
        assertEquals("en-GB", NodeFunctions.lang(n));
    }

    @Test public void testLang2() {
        NodeValue nv = NodeValue.makeNode("abc", "en", (String)null);
        NodeValue r = NodeFunctions.lang(nv);
        NodeValue e = NodeValue.makeString("en");
        assertEquals(e, r);
    }

    @Test public void testLang3() {
        NodeValue nv = NodeValue.makeInteger(5);
        NodeValue r = NodeFunctions.lang(nv);
        NodeValue e = NodeValue.makeString("");
        assertEquals(e, r);
    }

    @Test public void testLang4() {
        NodeValue nv = NodeValue.makeNode(NodeFactory.createLiteralString("simple"));
        NodeValue r = NodeFunctions.lang(nv);
        NodeValue e = NodeValue.makeString("");
        assertEquals(e, r);
    }

    @Test
    public void testLang5() {
        NodeValue nv = NodeValue.makeNode(NodeFactory.createURI("http://example/"));
        assertThrows(ExprTypeException.class,
                     ()-> NodeFunctions.lang(nv) );
    }

    @Test public void testDirLang1() {
        Node n = NodeFactory.createLiteralDirLang("abc",  "en", TextDirection.LTR);
        NodeValue nv = NodeValue.makeNode(n);
        NodeValue lang = NodeFunctions.lang(nv);
        NodeValue dir = NodeFunctions.langdir(nv);
        assertEquals("en", lang.getString());
        assertEquals("ltr", dir.getString());
    }

    @Test public void testDirLang2() {
        Node n = NodeFactory.createLiteralDirLang("abc",  "en", "ltr");
        NodeValue nv = NodeValue.makeDirLangString("abc", "en", "ltr");
        assertEquals(nv.asNode(), n);
    }

    @Test public void testLangMatches1() {
        NodeValue nv = NodeValue.makeString("en");
        NodeValue pat = NodeValue.makeString("en");
        NodeValue r = NodeFunctions.langMatches(nv, pat);
        assertEquals(NodeValue.TRUE, r);
        assertFalse(NodeValue.FALSE.equals(r));
    }

    @Test public void testLangMatches2() {
        NodeValue nv = NodeValue.makeString("en");
        NodeValue pat = NodeValue.makeString("fr");
        NodeValue r = NodeFunctions.langMatches(nv, pat);
        assertEquals(NodeValue.FALSE, r);
        assertFalse(NodeValue.TRUE.equals(r));
    }

    @Test public void testLangMatches3() {
        NodeValue nv = NodeValue.makeString("en-gb");
        NodeValue pat = NodeValue.makeString("en-gb");
        NodeValue r = NodeFunctions.langMatches(nv, pat);
        assertEquals(NodeValue.TRUE, r);
    }

    @Test public void testLangMatches4() {
        NodeValue nv = NodeValue.makeString("en-gb");
        NodeValue pat = NodeValue.makeString("en");
        NodeValue r = NodeFunctions.langMatches(nv, pat);
        assertEquals(NodeValue.TRUE, r);
    }

    @Test public void testLangMatches5() {
        NodeValue nv = NodeValue.makeString("abc");
        NodeValue pat = NodeValue.makeString("*");
        NodeValue r = NodeFunctions.langMatches(nv, pat);
        assertEquals(NodeValue.TRUE, r);
    }

    @Test public void testLangMatches6() {
        NodeValue nv = NodeValue.makeString("x-y-z");
        NodeValue pat = NodeValue.makeString("x");
        NodeValue r = NodeFunctions.langMatches(nv, pat);
        assertEquals(NodeValue.TRUE, r);
    }

    @Test public void testLangMatches7() {
        NodeValue nv = NodeValue.makeString("x");
        NodeValue pat = NodeValue.makeString("x-y-z");
        NodeValue r = NodeFunctions.langMatches(nv, pat);
        assertEquals(NodeValue.FALSE, r);
    }

    @Test public void testLangMatches8() {
        // The language tag of a plain literal is ""
        // A language tag is not allowed to be the empty string (by RFC 3066)
        NodeValue nv = NodeValue.makeString("");
        NodeValue pat = NodeValue.makeString("*");
        NodeValue r = NodeFunctions.langMatches(nv, pat);
        assertEquals(NodeValue.FALSE, r);
    }

    @Test
    public void testLangMatches9() {
        // The language-match of "" is not a legal by RFC 4647 but useful for language tags of ""
        NodeValue nv = NodeValue.makeString("");
        NodeValue pat = NodeValue.makeString("");
        NodeValue r = NodeFunctions.langMatches(nv, pat);
        assertEquals(NodeValue.TRUE, r);
    }

    @Test
    public void testLangMatches10() {
        // The language-match of "" is not a legal by RFC 4647 but useful for language tags of ""
        NodeValue nv = NodeValue.makeString("en");
        NodeValue pat = NodeValue.makeString("");
        NodeValue r = NodeFunctions.langMatches(nv, pat);
        assertEquals(NodeValue.FALSE, r);
    }

    @Test public void testIsIRI_1() {
        NodeValue nv = NodeValue.makeNode(NodeFactory.createURI("http://example/"));
        NodeValue r = NodeFunctions.isIRI(nv);
        assertEquals(NodeValue.TRUE, r);
    }

    @Test public void testIsIRI_2() {
        NodeValue nv = NodeValue.makeNode(NodeFactory.createLiteralString("http://example/"));
        NodeValue r = NodeFunctions.isIRI(nv);
        assertEquals(NodeValue.FALSE, r);
    }

    @Test public void testIsBlank1() {
        NodeValue nv = NodeValue.makeNode(NodeFactory.createBlankNode());
        NodeValue r = NodeFunctions.isBlank(nv);
        assertEquals(NodeValue.TRUE, r);
    }

    @Test public void testIsBlank2() {
        NodeValue nv = NodeValue.makeNode(NodeFactory.createLiteralString("xyz"));
        NodeValue r = NodeFunctions.isBlank(nv);
        assertEquals(NodeValue.FALSE, r);
    }

    @Test public void testIsBlank3() {
        NodeValue nv = NodeValue.makeNode(NodeFactory.createURI("http://example/"));
        NodeValue r = NodeFunctions.isBlank(nv);
        assertEquals(NodeValue.FALSE, r);

    }

    @Test public void testIsLiteral1() {
        NodeValue nv = NodeValue.makeNode(NodeFactory.createLiteralString("xyz"));
        NodeValue r = NodeFunctions.isLiteral(nv);
        assertEquals(NodeValue.TRUE, r);
    }

    @Test public void testIsLiteral2() {
        NodeValue nv = NodeValue.makeNode(NodeFactory.createURI("http://example/"));
        NodeValue r = NodeFunctions.isLiteral(nv);
        assertEquals(NodeValue.FALSE, r);
    }
}
