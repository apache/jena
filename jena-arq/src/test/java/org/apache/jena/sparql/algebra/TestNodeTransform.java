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

import org.apache.jena.graph.NodeFactory;
import org.apache.jena.query.Query;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.expr.Expr;
import org.apache.jena.sparql.graph.NodeTransform;
import org.apache.jena.sparql.graph.NodeTransformLib;
import org.apache.jena.sparql.sse.SSE;
import org.apache.jena.sparql.syntax.syntaxtransform.QueryTransformOps;
import org.apache.jena.sparql.util.ExprUtils;

/** Tests of node transformation : {@link NodeTransformLib}. */
public class TestNodeTransform {

    @Test public void transformServiceVar01() {
        Op opInput =    SSE.parseOp("(service ?var (table unit))");
        Op opExpected = SSE.parseOp("(service ?varZZZ (table unit))");
        testOp(opExpected, opInput, nodeTransform);
    }

    @Test public void transformServiceVar02() {
        Op opInput =    SSE.parseOp("(service silent ?var (table unit))");
        Op opExpected = SSE.parseOp("(service silent ?varZZZ (table unit))");
        testOp(opExpected, opInput, nodeTransform);
    }

    @Test public void transformServiceVar03() {
        Op opInput =    SSE.parseOp("(service ?var (bgp (?s ?p ?o)))");
        Op opExpected = SSE.parseOp("(service ?varZZZ (bgp (?sZZZ ?pZZZ ?oZZZ)))");
        testOp(opExpected, opInput, nodeTransform);
    }

    @Test public void transformServiceVar10() {
        String queryInputStr =    "SELECT * { SERVICE ?service {} }";
        String queryExpectedStr = "SELECT * { SERVICE ?serviceZZZ {} }";
        testQuery(queryExpectedStr, queryInputStr, nodeTransform);
    }

    @Test public void transformServiceVar11() {
        String queryInputStr = """
                SELECT * {
                    BIND(<http://example/sparql> AS ?service)
                    SERVICE ?service {
                        <http://dbpedia.org/resource/RDF_query_language> ?p ?o
                        FILTER (?a > 10)
                    }
                }
                """;
        String queryExpectedStr = """
                SELECT * {
                    BIND(<http://example/sparql> AS ?serviceZZZ)
                    SERVICE ?serviceZZZ {
                        <http://dbpedia.org/resource/RDF_query_language> ?pZZZ ?oZZZ
                        FILTER (?aZZZ > 10)
                    }
                }
                """;
        // Equal without the element.
        testQuery(queryExpectedStr, queryInputStr, nodeTransform);
    }

    @Test public void transformNodeValueToVar01() {
        testExpr("?v", "1", x -> Var.alloc("v"));
    }

    @Test public void transformNodeValueToVar02() {
        String queryInputStr    = "SELECT * { } ORDER BY ('foo')";
        String queryExpectedStr = "SELECT * { } ORDER BY (?x)";
        NodeTransform nt = n -> n.isLiteral() && n.getLiteralLexicalForm().equals("foo") ? Var.alloc("x") : n;
        testQuery(queryExpectedStr, queryInputStr, nt);
        testQueryElt(queryExpectedStr, queryInputStr, nt);
    }

    /** Test query in, transform, query out */
    private static void testQuery(String expectedStr, String inputStr, NodeTransform nt) {
        Query qExpected = QueryFactory.create(expectedStr);
        Op opExpected = Algebra.compile(qExpected);

        Query inputQuery = QueryFactory.create(inputStr);
        Op opInput = Algebra.compile(inputQuery);

        testOp(opExpected, opInput, nt);

        // And OpAsQuery
        Op opActual = NodeTransformLib.transform(nt, opInput);
        // Beware that OpAsQuery is not perfect. It may produce an equivalent but not identical query.
        // e.g. redundant groups-of-one
        Query qActual = OpAsQuery.asQuery(opActual);
        Query qActual1 = QueryFactory.create(qActual);
        assertEquals(qExpected, qActual1);
    }

    /** Test query in, transform, query out - Element level. */
    private static void testQueryElt(String expectedStr, String inputStr, NodeTransform nt) {
        Query qExpected = QueryFactory.create(expectedStr);
        Query inputQuery = QueryFactory.create(inputStr);
        Query qActual = QueryTransformOps.transform(inputQuery, nt);
        assertEquals(qExpected, qActual);
    }

    /** Test Op in, transform, Op out */
    private static void testOp(Op opExpected, Op opInput, NodeTransform nt) {
        Op opActual = NodeTransformLib.transform(nt, opInput);
        assertEquals(opExpected, opActual);
    }

    /** Test Expr in, transform, Expr out */
    private static void testExpr(String exprExpectedStr, String exprInputStr, NodeTransform nt) {
        Expr exprExpected = ExprUtils.parse(exprExpectedStr);
        Expr exprInput = ExprUtils.parse(exprInputStr);
        Expr exprActual = NodeTransformLib.transform(nt, exprInput);
        assertEquals(exprExpected, exprActual);
    }

    // Add a string to a variable name
    private static NodeTransform nodeTransform = n -> {
        return n.isVariable()
                ? Var.alloc(n.getName() + "ZZZ")
                : n;
    };

    @Test
    public void transformPropFunc() {
        Op inOp = SSE.parseOp("""
            (propfunc <urn:p>
              <urn:x> ("y" ?z)
              (table unit))
            """);

        // Property function name is not affected by node transform.
        Op expectedOp = SSE.parseOp("""
            (propfunc <urn:p>
              <URN:X> ("Y" ?Z)
              (table unit))
            """);

        Op actualOp = NodeTransformLib.transform(x ->
            x.isURI() ? NodeFactory.createURI(x.getURI().toUpperCase()) :
            x.isVariable() ? Var.alloc(x.getName().toUpperCase()) :
            x.isLiteral() ? NodeFactory.createLiteralString(x.getLiteralLexicalForm().toUpperCase()) :
            x, inOp);

        assertEquals(expectedOp, actualOp);
    }
}
