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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import org.apache.jena.datatypes.xsd.XSDDatatype;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.graph.TextDirection;
import org.apache.jena.sparql.sse.SSE;
//import org.apache.jena.sparql.util.NodeCmp;
import org.apache.jena.sparql.util.NodeCmp;

/**
 * See also {@link TestComparison} and {@link TestSortOrdering}.
 */
public class TestOrdering {
    private static NodeValue nvInt2 = NodeValue.makeNodeInteger(2);
    private static NodeValue nvInt3 = NodeValue.makeNodeInteger("3");
    private static NodeValue nvInt03 = NodeValue.makeNodeInteger("03");
    private static NodeValue nvStr3 = NodeValue.makeNodeString("3");
    private static NodeValue nvStr03 = NodeValue.makeNodeString("03");

    private static NodeValue nvInt9 = NodeValue.makeNodeInteger(9);
    private static NodeValue nvPosInt9 = NodeValue.makeNode("9", XSDDatatype.XSDpositiveInteger);
    private static NodeValue nvInt10 = NodeValue.makeNodeInteger(10);
    private static NodeValue nvDouble9 = NodeValue.makeNodeDouble(9.0);
    private static NodeValue nvFloat8 = NodeValue.makeNode("8.0", XSDDatatype.XSDfloat);

    private static NodeValue nvByte10 = NodeValue.makeNode("10", XSDDatatype.XSDbyte);

    private static Node nInt2 = nvInt2.getNode();
    private static Node nInt3 = nvInt3.getNode();
    private static Node nInt03 = nvInt03.getNode();
    private static Node nStr3 = nvStr3.getNode();
    private static Node nStr03 = nvStr03.getNode();

    private static Node nInt9 = nvInt9.getNode();
    private static Node nPosInt9 = nvPosInt9.getNode();
    private static Node nInt10 = nvInt10.getNode();
    private static Node nDouble9 = nvDouble9.getNode();
    private static Node nFloat8 = nvFloat8.getNode();
    private static Node nByte10 = nvByte10.getNode();

    private static Node nStrDirLang = NodeFactory.createLiteralDirLang("ABCD", "en", TextDirection.LTR);
    private static Node nStrLang    = NodeFactory.createLiteralLang("ABCD", "en");
    private static Node nStr        = NodeFactory.createLiteralString("ABCD");

    // Syntax and value tests

    @Test
    public void testComp_2_3() {
        int x = NodeValue.compareAlways(nvInt2, nvInt3);
        assertTrue(Expr.CMP_LESS == x, ()->"2 should be value-less than 3");
        int y = NodeCmp.compareRDFTerms(nInt2, nInt3);
        assertTrue(Expr.CMP_LESS == y, ()->"2 should be strict-less than 3");
    }

    @Test
    public void testComp_3_str3() {
        int x = NodeValue.compareAlways(nvInt3, nvStr3);
        int y = NodeCmp.compareRDFTerms(nInt3, nStr3);

        assertTrue(Expr.CMP_GREATER == x, ()->"3 should be compareAlways greater than \"3\"");
        assertTrue(Expr.CMP_GREATER == y, ()->"3 should be syntactic-greater than to \"3\"");
    }

    @Test
    public void testComp_03_str3() {
        int x = NodeValue.compareAlways(nvInt03, nvStr3);
        int y = NodeCmp.compareRDFTerms(nInt03, nStr3);

        assertTrue(Expr.CMP_GREATER == x, ()->"03 (typed) comes after string \"3\" (simple string)");
        assertTrue(Expr.CMP_GREATER == y, ()->"03 should be syntactic-less than to \"3\"");
    }

    // Syntax: string, lang  and dirlang
    @Test
    public void testComp_string_types_01() {
        int x = NodeCmp.compareRDFTerms(nStr, nStrDirLang);
        assertEquals(Expr.CMP_LESS, x, "string is not less than rdf:strDirLang");
    }

    @Test
    public void testComp_string_types_02() {
        int x = NodeCmp.compareRDFTerms(nStr, nStrLang);
        assertEquals(Expr.CMP_LESS, x, "string is not less than rdf:strLang");
    }

    @Test
    public void testComp_string_types_03() {
        int x = NodeCmp.compareRDFTerms(nStrLang, nStrDirLang);
        assertEquals(Expr.CMP_LESS, x, "rdf:strLang is not less than rdf:strDirLang");
    }

    // Compare things of different types.
    @Test
    public void testComp_int_double_1() {
        int x = NodeValue.compareAlways(nvInt10, nvDouble9);
        int y = NodeCmp.compareRDFTerms(nInt10, nDouble9);
        assertTrue(Expr.CMP_GREATER == x, ()->"Int 10 less than double 9");
        assertTrue(Expr.CMP_LESS == y, ()->"Int 10 less than double 9 in syntactic compare");
    }

    @Test
    public void testComp_byte_double_1() {
        int x = NodeValue.compareAlways(nvByte10, nvDouble9);
        int y = NodeCmp.compareRDFTerms(nByte10, nDouble9);
        assertTrue(Expr.CMP_GREATER == x, ()->"Byte 10 less than double 9");
        assertTrue(Expr.CMP_LESS == y, ()->"Byte 10 greater than double 9 in non-value compare (dataype URIs compare)");
    }

    @Test
    public void testComp_int_float_1() {
        int x = NodeValue.compareAlways(nvInt10, nvFloat8);
        int y = NodeCmp.compareRDFTerms(nInt10, nFloat8);
        assertTrue(Expr.CMP_GREATER == x, ()->"Int 10 less than float 8");
        assertTrue(Expr.CMP_LESS == y, ()->"Int 10 less than float 8 in syntatic compare");
    }

    @Test
    public void testComp_int_posint_1() {
        int x = NodeValue.compareAlways(nvInt9, nvPosInt9);
        assertTrue(Expr.CMP_EQUAL != x, ()->"Int 9 should be not equals to positive integer 9");
    }

    @Test
    public void testComp_int_posint_2() {
        int x = NodeValue.compareAlways(nvInt10, nvPosInt9);
        assertTrue(Expr.CMP_GREATER == x, ()->"Int 10 not greater than positive integer 9");
    }

    @Test
    public void test_xsd_string1() {
        NodeValue nv1 = NodeValue.makeNode(NodeFactory.createLiteralString("abc"));
        NodeValue nv2 = NodeValue.makeNode(NodeFactory.createLiteralDT("abc", XSDDatatype.XSDstring));
        int x = NodeValue.compare(nv1, nv2);
        assertTrue(Expr.CMP_EQUAL == x);
    }

    @Test
    public void test_xsd_string2() {
        NodeValue nv1 = NodeValue.makeNode(NodeFactory.createLiteralString("xyz"));
        NodeValue nv2 = NodeValue.makeNode(NodeFactory.createLiteralDT("abc", XSDDatatype.XSDstring));
        int x = NodeValue.compare(nv1, nv2);
        assertTrue(Expr.CMP_GREATER == x);
    }

    @Test
    public void test_xsd_string3() {
        NodeValue nv1 = NodeValue.makeNode(NodeFactory.createLiteralDT("xyz", XSDDatatype.XSDstring));
        NodeValue nv2 = NodeValue.makeNode(NodeFactory.createLiteralString("abc"));
        int x = NodeValue.compare(nv1, nv2);
        assertTrue(Expr.CMP_GREATER == x);
    }

    @Test
    public void test_xsd_string4() {
        NodeValue nv1 = NodeValue.makeNode(NodeFactory.createLiteralString("abc"));
        NodeValue nv2 = NodeValue.makeNode(NodeFactory.createLiteralDT("xyz", XSDDatatype.XSDstring));
        int x = NodeValue.compare(nv1, nv2);
        assertTrue(Expr.CMP_LESS == x);
    }

    @Test
    public void test_xsd_string5() {
        NodeValue nv1 = NodeValue.makeNode(NodeFactory.createLiteralDT("abc", XSDDatatype.XSDstring));
        NodeValue nv2 = NodeValue.makeNode(NodeFactory.createLiteralString("xyz"));
        int x = NodeValue.compare(nv1, nv2);
        assertTrue(Expr.CMP_LESS == x);
    }

    @Test
    public void test_lang1() {
        NodeValue nv1 = NodeValue.makeNode(NodeFactory.createLiteralLang("abc", "en"));
        NodeValue nv2 = NodeValue.makeNode(NodeFactory.createLiteralString("abc"));

        int x = NodeCmp.compareRDFTerms(nv1.asNode(), nv2.asNode());
        assertTrue(Expr.CMP_GREATER == x, ()->"Lang tags should sort after plain literal");
    }

    @Test
    public void test_lang2() {
        NodeValue nv1 = NodeValue.makeNode(NodeFactory.createLiteralLang("abc", "en"));
        NodeValue nv2 = NodeValue.makeNode(NodeFactory.createLiteralLang("abc", "EN"));

        // Jena5: same langtag (there are the same RDF term)
        int x = NodeCmp.compareRDFTerms(nv1.asNode(), nv2.asNode());
        assertTrue(Expr.CMP_EQUAL == x, ()->"Lang tags should sort by case");
    }

    @Test
    public void test_lang3() {
        NodeValue nv1 = NodeValue.makeNode(NodeFactory.createLiteralLang("ABC", "en"));
        NodeValue nv2 = NodeValue.makeNode(NodeFactory.createLiteralLang("abc", "EN"));

        int x = NodeValue.compareAlways(nv1, nv2);
        assertTrue(Expr.CMP_LESS == x, ()->"Lang nodes should sort by lexical if tags value-same");
        int y = NodeCmp.compareRDFTerms(nv1.asNode(), nv2.asNode());
        assertTrue(Expr.CMP_LESS == y, ()->"Lang nodes should sort by case (syntactically)");
    }

    @Test
    public void test_lang4() {
        NodeValue nv1 = NodeValue.makeNode(NodeFactory.createLiteralLang("ABC", "en"));
        NodeValue nv2 = NodeValue.makeNode(NodeFactory.createLiteralLang("abc", "en"));

        int x = NodeValue.compareAlways(nv1, nv2);
        assertTrue(Expr.CMP_LESS == x, ()->"Lang nodes should sort by lexical if tags the same");
        int y = NodeCmp.compareRDFTerms(nv1.asNode(), nv2.asNode());
        assertTrue(Expr.CMP_LESS == x, ()->"Lang nodes should sort by lexical form if lang tags the same");
    }

    @Test
    public void test_lang5() {
        NodeValue nv1 = NodeValue.makeNode(NodeFactory.createLiteralString("abc"));
        NodeValue nv2 = NodeValue.makeNode(NodeFactory.createLiteralLang("xyz", "en"));

        int x = NodeValue.compareAlways(nv1, nv2);
        assertTrue(Expr.CMP_LESS == x, ()->"Lang nodes should sort by lexical form if one is plain");
        int y = NodeCmp.compareRDFTerms(nv1.asNode(), nv2.asNode());
        assertTrue(Expr.CMP_LESS == x);
    }

    @Test
    public void test_lang6() {
        NodeValue nv1 = NodeValue.makeNode(NodeFactory.createLiteralString("xyz"));
        NodeValue nv2 = NodeValue.makeNode(NodeFactory.createLiteralLang("abc", "en"));

        int x = NodeValue.compareAlways(nv1, nv2);
        assertTrue(Expr.CMP_LESS == x, ()->"Lang nodes should sort by language before lexical form");
        int y = NodeCmp.compareRDFTerms(nv1.asNode(), nv2.asNode());
        assertTrue(Expr.CMP_LESS == x);
    }

    @Test
    public void test_lang7() {
        NodeValue nv1 = NodeValue.makeNode(NodeFactory.createLiteralLang("xyz", "de"));
        NodeValue nv2 = NodeValue.makeNode(NodeFactory.createLiteralLang("abc", "en"));

        int x = NodeValue.compareAlways(nv1, nv2);
        assertTrue(Expr.CMP_LESS == x, ()->"Lang nodes should sort by language before lexical form");
        int y = NodeCmp.compareRDFTerms(nv1.asNode(), nv2.asNode());
        assertTrue(Expr.CMP_LESS == x);
    }

    @Test
    public void test_lang8() {
        NodeValue nv1 = NodeValue.makeNode(NodeFactory.createLiteralDT("abc", XSDDatatype.XSDstring));
        NodeValue nv2 = NodeValue.makeNode(NodeFactory.createLiteralLang("xyz", "en"));

        int x = NodeValue.compareAlways(nv1, nv2);
        assertTrue(Expr.CMP_LESS == x, ()->"Lang nodes should sort by lexical form if other is XSD string");
        int y = NodeCmp.compareRDFTerms(nv1.asNode(), nv2.asNode());
        assertTrue(Expr.CMP_LESS == x);
    }

    @Test
    public void test_lang9() {
        NodeValue nv1 = NodeValue.makeNode(NodeFactory.createLiteralDT("xyz", XSDDatatype.XSDstring));
        NodeValue nv2 = NodeValue.makeNode(NodeFactory.createLiteralLang("abc", "en"));
        // xsd:string (RDF 1.1) is a simple string and before @en.
        int x = NodeValue.compareAlways(nv1, nv2);
        assertTrue(Expr.CMP_LESS == x, ()->"Lang nodes should sort by lexical form if other is XSD string");
        int y = NodeCmp.compareRDFTerms(nv1.asNode(), nv2.asNode());
        assertTrue(Expr.CMP_LESS == x);
    }

    @Test
    public void test_variable1() {
        Node x = NodeFactory.createVariable("x");
        Node y = NodeFactory.createVariable("x");

        int res = NodeCmp.compareRDFTerms(x, y);
        assertTrue(Expr.CMP_EQUAL == res, ()->"Variable nodes should sort by variable names");
    }

    @Test
    public void test_variable2() {
        Node x = NodeFactory.createVariable("x");
        Node y = NodeFactory.createVariable("y");

        int res = NodeCmp.compareRDFTerms(x, y);
        assertTrue(Expr.CMP_LESS == res, ()->"Variable nodes should sort by variable names");
        res = NodeCmp.compareRDFTerms(y, x);
        assertTrue(Expr.CMP_GREATER == res, ()->"Variable nodes should sort by variable names");
    }

    @Test
    public void test_variable3() {
        Node x = NodeFactory.createVariable("x");
        Node y = NodeFactory.createBlankNode();

        int res = NodeCmp.compareRDFTerms(x, y);
        assertTrue(Expr.CMP_LESS == res, ()->"Variable nodes should be less than blank nodes");
        res = NodeCmp.compareRDFTerms(y, x);
        assertTrue(Expr.CMP_GREATER == res, ()->"Variable nodes should be less than blank nodes");
    }

    @Test
    public void test_variable4() {
        Node x = NodeFactory.createVariable("x");
        Node y = NodeFactory.createURI("http://uri");

        int res = NodeCmp.compareRDFTerms(x, y);
        assertTrue(Expr.CMP_LESS == res, ()->"Variable nodes should be less than URI nodes");
        res = NodeCmp.compareRDFTerms(y, x);
        assertTrue(Expr.CMP_GREATER == res, ()->"Variable nodes should be less than URI nodes");
    }

    @Test
    public void test_variable5() {
        Node x = NodeFactory.createVariable("x");
        Node y = NodeFactory.createLiteralString("test");

        int res = NodeCmp.compareRDFTerms(x, y);
        assertTrue(Expr.CMP_LESS == res, ()->"Variable nodes should be less than literal nodes");
        res = NodeCmp.compareRDFTerms(y, x);
        assertTrue(Expr.CMP_GREATER == res, ()->"Variable nodes should be less than literal nodes");
    }

    @Test
    public void test_tripleterm_1() {
        Node x = SSE.parseNode("<<(:s :p 1)>>");
        Node y = SSE.parseNode("<<(:s :p 2)>>");
        int res = NodeCmp.compareRDFTerms(x, y);
        assertTrue(Expr.CMP_LESS == res);
        res = NodeCmp.compareRDFTerms(y, x);
        assertTrue(Expr.CMP_GREATER == res);
    }

    @Test
    public void test_tripleterm_2() {
        Node x = SSE.parseNode("<<(:s2 :p 1)>>");
        Node y = SSE.parseNode("<<(:s1 :p 2)>>");
        int res = NodeCmp.compareRDFTerms(x, y);
        assertTrue(Expr.CMP_GREATER == res);
        res = NodeCmp.compareRDFTerms(y, x);
        assertTrue(Expr.CMP_LESS == res);
    }

    @Test
    public void test_tripleterm_3() {
        Node x = SSE.parseNode("<<(:s :p 2)>>");
        Node y = SSE.parseNode("<<(:s :p 2)>>");
        int res = NodeCmp.compareRDFTerms(x, y);
        assertTrue(Expr.CMP_EQUAL == res);
    }

    @Test
    public void test_tripleterm_4() {
        Node x = SSE.parseNode("'abc'");
        Node y = SSE.parseNode("<<(:s :p 2)>>");
        int res = NodeCmp.compareRDFTerms(x, y);
        // After literals.
        assertTrue(Expr.CMP_LESS == res);
        res = NodeCmp.compareRDFTerms(y, x);
        assertTrue(Expr.CMP_GREATER == res);
    }

    @Test
    public void test_tripleterm_5() {
        Node x = SSE.parseNode("<uri>");
        Node y = SSE.parseNode("<<(:s :p 2)>>");
        int res = NodeCmp.compareRDFTerms(x, y);
        // After URIs
        assertTrue(Expr.CMP_LESS == res);
        res = NodeCmp.compareRDFTerms(y, x);
        assertTrue(Expr.CMP_GREATER == res);
    }
}
