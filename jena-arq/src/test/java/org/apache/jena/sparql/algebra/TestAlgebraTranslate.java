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

package org.apache.jena.sparql.algebra;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

import org.apache.jena.atlas.lib.StrUtils;
import org.apache.jena.query.Query;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.query.Syntax;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.sse.SSE;
import org.apache.jena.sparql.syntax.*;

/** Test translation of syntax to algebra - no optimization */
public class TestAlgebraTranslate
{
    @Test public void translate_01() { test("?s ?p ?o", "(bgp (triple ?s ?p ?o))"); }

    @Test public void translate_02() { test("?s ?p ?o2 . BIND(?v+1 AS ?v1)",
                                            "(extend [(?v1 (+ ?v 1))]",
                                            "   (bgp [triple ?s ?p ?o2]))"
                                            ); }

    @Test public void translate_03() { test("?s ?p ?o2 . LET(?v1 := ?v+1) LET(?v2 := ?v+2)",
                                            "(assign ((?v1 (+ ?v 1)) (?v2 (+ ?v 2)))",
                                            "  (bgp (triple ?s ?p ?o2)))"
                                            ); }

    @Test public void translate_04() { test("?s ?p ?o2 . BIND(?v+1 AS ?v1) BIND(?v + 2 AS ?v2)",
                                            // If combining (extend) during generation.
                                            //"(extend ((?v1 (+ ?v 1)) (?v2 (+ ?v 2)))",
                                            "  (extend ((?v2 (+ ?v 2)))",
                                            "    (extend ((?v1 (+ ?v 1)))",
                                            "      (bgp (triple ?s ?p ?o2))))"
                                            ); }


    @Test public void translate_05() { test("?s ?p ?o2 BIND(?v+1 AS ?v1) LET(?v2 := ?v+2)",
                                            "(assign ((?v2 (+ ?v 2)))",
                                            "  (extend ((?v1 (+ ?v 1)))",
                                            "    (bgp (triple ?s ?p ?o2))))"
                                            ); }

    @Test public void translate_06() { test("?s ?p ?o2 LET(?v2 := ?v+2) BIND(?v+1 AS ?v1)",
                                            "(extend ((?v1 (+ ?v 1)))",
                                            "  (assign ((?v2 (+ ?v 2)))",
                                            "    (bgp (triple ?s ?p ?o2))))"
                                            ); }

    @Test public void translate_07() { test("{ ?s ?p ?o1 . } BIND(?v+1 AS ?v1)",
                                            "(extend ((?v1 (+ ?v 1)))",
                                            "  (bgp (triple ?s ?p ?o1)) )"
                                            ); }

    @Test public void translate_08() { test("BIND(5 AS ?v1)", "(extend ((?v1 5)) [table unit])"); }

    @Test public void translate_09() { test("{ ?s ?p ?o1 . } ?s ?p ?o2 . BIND(?v+1 AS ?v1)",
                                            "(extend ((?v1 (+ ?v 1)))",
                                            "  (join",
                                            "    (bgp (triple ?s ?p ?o1))",
                                            "    (bgp (triple ?s ?p ?o2))))"
                                            ); }

    @Test public void translate_10() { test("?s ?p ?o2 . ?s ?p ?o3 . BIND(?v+1 AS ?v1)",
                                            "(extend ((?v1 (+ ?v 1)))",
                                            "   [bgp (triple ?s ?p ?o2) (triple ?s ?p ?o3)])"
                                            ); }

    @Test public void translate_11() { test("{ SELECT * {?s ?p ?o2}} BIND(?o+1 AS ?v1)",
                                            "(extend [(?v1 (+ ?o 1))]",
                                            "   (bgp (triple ?s ?p ?o2)))"
                                            ); }


    @Test public void translate_20() { test("?s1 ?p ?o . ?s2 ?p ?o OPTIONAL { ?s ?p3 ?o3 . ?s ?p4 ?o4 }",
                                            "(leftjoin",
                                            "  [bgp (?s1 ?p ?o) (?s2 ?p ?o)]",
                                            "  [bgp (?s ?p3 ?o3) (?s ?p4 ?o4)] )"); }

    @Test public void translate_21() { test("?s1 ?p ?o . ?s2 ?p ?o BIND (99 AS ?z) OPTIONAL { ?s ?p3 ?o3 . ?s ?p4 ?o4 }",
                                            "(leftjoin",
                                            "  (extend ((?z 99))[bgp (?s1 ?p ?o) (?s2 ?p ?o)])",
                                            "  [bgp (?s ?p3 ?o3) (?s ?p4 ?o4)] )"); }

    @Test public void translate_22() { test("BIND (99 AS ?z) OPTIONAL { ?s ?p3 ?o3 . ?s ?p4 ?o4 }",
                                            "(leftjoin",
                                            "  (extend ((?z 99))[table unit])",
                                            "  [bgp (?s ?p3 ?o3) (?s ?p4 ?o4)] )"); }

    @Test public void translate_23() { test("OPTIONAL { BIND (99 AS ?z)}",
                                            "(leftjoin",
                                            "  [table unit]",
                                            "  [extend ((?z 99)) (table unit)] )" ); }

    // ---- Cases that can not be written in query syntax
    // GH-2153.
    // From Jena 3.7.0.
    // This comment written for Jena 5.0.0.
    //
    // The querybuilder creates forms of the syntax tree that
    // will not occur from the parser. In particular, it creates sub-element
    // with no group. In SPARQL {} is a group even if it has one member.
    // The parser does not have any smarts - it is done in the optimizer.
    // The querybuilder can generate forms where the sub-element is directly
    // the element that would be inside a group of one.
    // See BuildElementVisitor.visit(ElementGroup).

    @Test public void translate_80() {
        // No group. This can be generated by querybuilder. GH-2153.
        Element el = new ElementFilter(SSE.parseExpr("true"));
        testElement(el, "(filter true [table unit])");
    }

    @Test public void translate_81() {
        Element el = new ElementBind(Var.alloc("x"), SSE.parseExpr("true"));
        testElement(el, "(extend ((?x true)) [table unit])");
    }

    @Test public void translate_82() {
        Element el = new ElementAssign(Var.alloc("x"), SSE.parseExpr("true"));
        testElement(el, "(assign ((?x true)) [table unit])");
    }

    @Test public void translate_83() {
        ElementGroup elSub = new ElementGroup();
        Element el = new ElementOptional(elSub);
        testElement(el, "(leftjoin [table unit] [table unit])");
    }

    @Test public void translate_84() {
        // Variant of translate_83 adding something to make the optional look different.
        ElementGroup elSub = new ElementGroup();
        ElementTriplesBlock etb = new ElementTriplesBlock();
        etb.addTriple(SSE.parseTriple("(:s :p :o)"));
        elSub.addElement(etb);
        Element el = new ElementOptional(elSub);
        testElement(el, "(leftjoin [table unit] (bgp (:s :p :o)))");
    }

    @Test public void translate_85() {
        ElementGroup elSub = new ElementGroup();
        Element el = new ElementMinus(elSub);
        testElement(el, "(minus [table unit] [table unit])");
    }

    // ----

    protected AlgebraGenerator getGenerator() {
        return new AlgebraGenerator();
    }

    // Helper. Prints the test actual Op.
    protected void printDetails(String qs) {
        qs = "SELECT * {\n" + qs + "\n}";
        Query query = QueryFactory.create(qs, Syntax.syntaxARQ);
        Op opActual = this.getGenerator().compile(query);
        String x = opActual.toString();
        x = x.replaceAll("\n$", "");
        x = x.replace("\n", "\", \n\"");
        System.out.print('"');
        System.out.print(x);
        System.out.println('"');
        System.out.println();
    }

    protected void test(String qs, String...y) {
        qs = "SELECT * {\n" + qs + "\n}";
        Query query = QueryFactory.create(qs, Syntax.syntaxARQ);
        String opStr = StrUtils.strjoinNL(y);
        Op opExpected = SSE.parseOp(opStr);
        Op opActual = this.getGenerator().compile(query);
        assertEquals(opExpected, opActual);
    }

    // Test based on an exact element structure.
    protected void testElement(Element el, String...y) {
        String opStr = StrUtils.strjoinNL(y);
        Op opExpected = SSE.parseOp(opStr);
        Op opActual = this.getGenerator().compile(el);
        assertEquals(opExpected, opActual);
    }
}

