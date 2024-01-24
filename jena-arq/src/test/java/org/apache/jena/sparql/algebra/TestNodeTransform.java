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

import static org.junit.Assert.assertEquals;

import org.apache.jena.query.Query;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.graph.NodeTransform;
import org.apache.jena.sparql.graph.NodeTransformLib;
import org.apache.jena.sparql.sse.SSE;
import org.junit.Test;

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

    /** Test Op in, transform, Op out */
    private static void testOp(Op opExpected, Op opInput, NodeTransform nt) {
        Op opActual = NodeTransformLib.transform(nt, opInput);
        assertEquals(opExpected, opActual);
    }

    // Add a string to a variable name
    private static NodeTransform nodeTransform = n -> {
        return n.isVariable()
                ? Var.alloc(n.getName() + "ZZZ")
                : n;
    };
}
