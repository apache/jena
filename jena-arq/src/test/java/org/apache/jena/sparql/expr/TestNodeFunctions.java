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

package org.apache.jena.sparql.expr ;

import org.apache.jena.JenaRuntime ;
import org.apache.jena.atlas.junit.BaseTest ;
import org.apache.jena.datatypes.xsd.XSDDatatype ;
import org.apache.jena.graph.Node ;
import org.apache.jena.graph.NodeFactory ;
import org.apache.jena.sparql.expr.nodevalue.NodeFunctions ;
import org.apache.jena.sparql.graph.NodeConst ;
import org.apache.jena.vocabulary.RDF ;
import org.apache.jena.vocabulary.XSD ;
import org.junit.Test ;

public class TestNodeFunctions extends BaseTest {
    private static final double accuracyExact = 0.0d ;
    private static final double accuracyClose = 0.000001d ;

    @Test public void testSameTerm1() {
        Node n1 = NodeFactory.createLiteral("xyz") ;
        Node n2 = NodeFactory.createLiteral("xyz") ;
        assertTrue(NodeFunctions.sameTerm(n1, n2)) ;
    }

    @Test public void testSameTerm2() {
        Node n1 = NodeFactory.createLiteral("xyz") ;
        Node n2 = NodeFactory.createLiteral("abc") ;
        assertFalse(NodeFunctions.sameTerm(n1, n2)) ;
    }

    @Test public void testSameTerm3() {
        Node n1 = NodeFactory.createLiteral("xyz") ;
        Node n2 = NodeFactory.createURI("xyz") ;
        assertFalse(NodeFunctions.sameTerm(n1, n2)) ;
    }

    @Test public void testSameTerm4() {
        Node n1 = NodeFactory.createLiteral("xyz") ;
        Node n2 = NodeFactory.createLiteral("xyz", XSDDatatype.XSDstring) ;
        if ( JenaRuntime.isRDF11 )
            assertTrue(NodeFunctions.sameTerm(n1, n2)) ;
        else
            assertFalse(NodeFunctions.sameTerm(n1, n2)) ;
    }

    @Test public void testSameTerm5() {
        Node n1 = NodeFactory.createLiteral("xyz", "en") ;
        Node n2 = NodeFactory.createLiteral("xyz") ;
        assertFalse(NodeFunctions.sameTerm(n1, n2)) ;
    }

    @Test public void testSameTerm6() {
        Node n1 = NodeFactory.createLiteral("xyz", "en") ;
        Node n2 = NodeFactory.createLiteral("xyz", "EN") ;
        assertTrue(NodeFunctions.sameTerm(n1, n2)) ;
    }

    @Test public void testRDFtermEquals1() {
        Node n1 = NodeFactory.createURI("xyz") ;
        Node n2 = NodeFactory.createLiteral("xyz") ;
        assertFalse(NodeFunctions.rdfTermEquals(n1, n2)) ;
    }

    @Test(expected=ExprEvalException.class)
    public void testRDFtermEquals3() {
        // Unextended - no language tag
        Node n1 = NodeFactory.createLiteral("xyz") ;
        Node n2 = NodeFactory.createLiteral("xyz", "en") ;
        NodeFunctions.rdfTermEquals(n1, n2) ;
    }

    @Test public void testRDFtermEquals2() {
        Node n1 = NodeFactory.createLiteral("xyz", "en") ;
        Node n2 = NodeFactory.createLiteral("xyz", "EN") ;
        assertTrue(NodeFunctions.rdfTermEquals(n1, n2)) ;
    }

    @Test public void testStr1() {
        NodeValue nv = NodeValue.makeNodeInteger(56) ;
        NodeValue s = NodeFunctions.str(nv) ;
        assertEquals("56", s.getString()) ;
    }

    @Test public void testStr2() {
        NodeValue nv = NodeValue.makeInteger(56) ;
        NodeValue s = NodeFunctions.str(nv) ;
        assertEquals("56", s.getString()) ;
    }

    @Test public void testStr3() {
        NodeValue nv = NodeValue.makeNode("abc", "fr", (String)null) ;
        NodeValue s = NodeFunctions.str(nv) ;
        assertEquals("abc", s.getString()) ;
    }

    @Test(expected=ExprTypeException.class)
    public void testStr4() {
        Node n = NodeFactory.createBlankNode() ;
        String s = NodeFunctions.str(n) ;
    }

    @Test public void testDatatype1() {
        NodeValue nv = NodeValue.makeInteger(5) ;
        Node n = nv.asNode() ;
        Node r = NodeFunctions.datatype(n) ;
        assertEquals(XSD.integer.asNode(), r) ;
    }

    @Test public void testDatatype2() {
        NodeValue nv = NodeValue.makeInteger(5) ;
        NodeValue r = NodeFunctions.datatype(nv) ;
        NodeValue e = NodeValue.makeNode(XSD.integer.asNode()) ;
        assertEquals(e, r) ;
    }

    @Test public void testDatatype3() {
        NodeValue nv = NodeValue.makeString("abc") ;
        NodeValue r = NodeFunctions.datatype(nv) ;
        NodeValue e = NodeValue.makeNode(XSD.xstring.asNode()) ;
        assertEquals(e, r) ;
    }

    @Test public void testDatatype4() {
        NodeValue nv = NodeValue.makeNode("abc", "fr", (String)null) ;
        // SPARQL 1.0
        // try {
        //   NodeValue r = NodeFunctions.datatype(nv) ;
        //   fail("Expect a type exception but call succeeded") ;
        // }
        // catch (ExprTypeException ex) {}
        // SPARQL 1.1 / RDF 1.1
        NodeValue r = NodeFunctions.datatype(nv) ;
        NodeValue e = NodeValue.makeNode(NodeConst.rdfLangString) ;
        assertEquals(e, r) ;
    }

    @Test(expected=ExprTypeException.class)
    public void testDatatype5() {
        NodeValue nv = NodeValue.makeNode(NodeFactory.createURI("http://example")) ;
        NodeValue r = NodeFunctions.datatype(nv) ;
    }

    @Test(expected=ExprTypeException.class)
    public void testDatatype6() {
        NodeValue nv = NodeValue.makeNode(NodeFactory.createBlankNode()) ;
        NodeValue r = NodeFunctions.datatype(nv) ;
    }

    @Test public void testLang1() {
        Node n = NodeFactory.createLiteral("abc", "en-gb") ;
        assertEquals("en-gb", NodeFunctions.lang(n)) ;
    }

    @Test public void testLang2() {
        NodeValue nv = NodeValue.makeNode("abc", "en", (String)null) ;
        NodeValue r = NodeFunctions.lang(nv) ;
        NodeValue e = NodeValue.makeString("en") ;
        assertEquals(e, r) ;
    }

    @Test public void testLang3() {
        NodeValue nv = NodeValue.makeInteger(5) ;
        NodeValue r = NodeFunctions.lang(nv) ;
        NodeValue e = NodeValue.makeString("") ;
        assertEquals(e, r) ;
    }

    @Test public void testLang4() {
        NodeValue nv = NodeValue.makeNode(NodeFactory.createLiteral("simple")) ;
        NodeValue r = NodeFunctions.lang(nv) ;
        NodeValue e = NodeValue.makeString("") ;
        assertEquals(e, r) ;
    }

    @Test(expected=ExprTypeException.class)
    public void testLang5() {
        NodeValue nv = NodeValue.makeNode(NodeFactory.createURI("http://example/")) ;
        NodeValue r = NodeFunctions.lang(nv) ;
    }

    @Test public void testLangMatches1() {
        NodeValue nv = NodeValue.makeString("en") ;
        NodeValue pat = NodeValue.makeString("en") ;
        NodeValue r = NodeFunctions.langMatches(nv, pat) ;
        assertEquals(NodeValue.TRUE, r) ;
        assertFalse(NodeValue.FALSE.equals(r)) ;
    }

    @Test public void testLangMatches2() {
        NodeValue nv = NodeValue.makeString("en") ;
        NodeValue pat = NodeValue.makeString("fr") ;
        NodeValue r = NodeFunctions.langMatches(nv, pat) ;
        assertEquals(NodeValue.FALSE, r) ;
        assertFalse(NodeValue.TRUE.equals(r)) ;
    }

    @Test public void testLangMatches3() {
        NodeValue nv = NodeValue.makeString("en-gb") ;
        NodeValue pat = NodeValue.makeString("en-gb") ;
        NodeValue r = NodeFunctions.langMatches(nv, pat) ;
        assertEquals(NodeValue.TRUE, r) ;
    }

    @Test public void testLangMatches4() {
        NodeValue nv = NodeValue.makeString("en-gb") ;
        NodeValue pat = NodeValue.makeString("en") ;
        NodeValue r = NodeFunctions.langMatches(nv, pat) ;
        assertEquals(NodeValue.TRUE, r) ;
    }

    @Test public void testLangMatches5() {
        NodeValue nv = NodeValue.makeString("abc") ;
        NodeValue pat = NodeValue.makeString("*") ;
        NodeValue r = NodeFunctions.langMatches(nv, pat) ;
        assertEquals(NodeValue.TRUE, r) ;
    }

    @Test public void testLangMatches6() {
        NodeValue nv = NodeValue.makeString("x-y-z") ;
        NodeValue pat = NodeValue.makeString("x") ;
        NodeValue r = NodeFunctions.langMatches(nv, pat) ;
        assertEquals(NodeValue.TRUE, r) ;
    }

    @Test public void testLangMatches7() {
        NodeValue nv = NodeValue.makeString("x") ;
        NodeValue pat = NodeValue.makeString("x-y-z") ;
        NodeValue r = NodeFunctions.langMatches(nv, pat) ;
        assertEquals(NodeValue.FALSE, r) ;
    }

    @Test public void testLangMatches8() {
        // The language tag of a plain literal is ""
        // A language tag is not allowed to be the empty string (by RFC 3066)
        NodeValue nv = NodeValue.makeString("") ;
        NodeValue pat = NodeValue.makeString("*") ;
        NodeValue r = NodeFunctions.langMatches(nv, pat) ;
        assertEquals(NodeValue.FALSE, r) ;
    }

    @Test
    public void testLangMatches9() {
        // The language-match of "" is not a legal by RFC 4647 but useful for language tags of "" 
        NodeValue nv = NodeValue.makeString("") ;
        NodeValue pat = NodeValue.makeString("") ;
        NodeValue r = NodeFunctions.langMatches(nv, pat) ;
        assertEquals(NodeValue.TRUE, r) ;
    }

    @Test
    public void testLangMatches10() {
        // The language-match of "" is not a legal by RFC 4647 but useful for language tags of "" 
        NodeValue nv = NodeValue.makeString("en") ;
        NodeValue pat = NodeValue.makeString("") ;
        NodeValue r = NodeFunctions.langMatches(nv, pat) ;
        assertEquals(NodeValue.FALSE, r) ;
    }
    
    @Test public void testIsIRI_1() {
        NodeValue nv = NodeValue.makeNode(NodeFactory.createURI("http://example/")) ;
        NodeValue r = NodeFunctions.isIRI(nv) ;
        assertEquals(NodeValue.TRUE, r) ;
    }

    @Test public void testIsIRI_2() {
        NodeValue nv = NodeValue.makeNode(NodeFactory.createLiteral("http://example/")) ;
        NodeValue r = NodeFunctions.isIRI(nv) ;
        assertEquals(NodeValue.FALSE, r) ;
    }

    @Test public void testIsBlank1() {
        NodeValue nv = NodeValue.makeNode(NodeFactory.createBlankNode()) ;
        NodeValue r = NodeFunctions.isBlank(nv) ;
        assertEquals(NodeValue.TRUE, r) ;

    }

    @Test public void testIsBlank2() {
        NodeValue nv = NodeValue.makeNode(NodeFactory.createLiteral("xyz")) ;
        NodeValue r = NodeFunctions.isBlank(nv) ;
        assertEquals(NodeValue.FALSE, r) ;
    }

    @Test public void testIsBlank3() {
        NodeValue nv = NodeValue.makeNode(NodeFactory.createURI("http://example/")) ;
        NodeValue r = NodeFunctions.isBlank(nv) ;
        assertEquals(NodeValue.FALSE, r) ;

    }

    @Test public void testIsLiteral1() {
        NodeValue nv = NodeValue.makeNode(NodeFactory.createLiteral("xyz")) ;
        NodeValue r = NodeFunctions.isLiteral(nv) ;
        assertEquals(NodeValue.TRUE, r) ;
    }

    @Test public void testIsLiteral2() {
        NodeValue nv = NodeValue.makeNode(NodeFactory.createURI("http://example/")) ;
        NodeValue r = NodeFunctions.isLiteral(nv) ;
        assertEquals(NodeValue.FALSE, r) ;
    }
    
    @Test public void testCheckAndGetStringLiteral1() {
        NodeValue nv = NodeValue.makeNode("abc", XSDDatatype.XSDstring) ;
        Node n = NodeFunctions.checkAndGetStringLiteral("Test", nv);
        assertEquals( "abc", n.getLiteralLexicalForm());
    }
    
    @Test public void testCheckAndGetStringLiteral2() {
        NodeValue nv = NodeValue.makeNode("abc", XSDDatatype.XSDnormalizedString) ;
        Node n = NodeFunctions.checkAndGetStringLiteral("Test", nv);
        assertEquals( "abc", n.getLiteralLexicalForm());
    }

    @Test public void testCheckAndGetStringLiteral3() {
        NodeValue nv = NodeValue.makeString("abc") ;
        Node n = NodeFunctions.checkAndGetStringLiteral("Test", nv);
        assertEquals( "abc", n.getLiteralLexicalForm());
    }
    
    @Test(expected=ExprEvalException.class)
    public void testCheckAndGetStringLiteral4() {
        // The form "abc"^^rdf:langString (no lang tag) is not derived from xsd:string. 
        NodeValue nv = NodeValue.makeNode("abc", RDF.dtLangString) ;
        Node n = NodeFunctions.checkAndGetStringLiteral("Test", nv);
        assertEquals( "abc", n.getLiteralLexicalForm());
    }

}
