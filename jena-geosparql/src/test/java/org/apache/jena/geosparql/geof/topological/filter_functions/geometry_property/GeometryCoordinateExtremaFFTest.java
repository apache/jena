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
package org.apache.jena.geosparql.geof.topological.filter_functions.geometry_property;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertThrows;
import static org.junit.Assume.assumeFalse;

import java.util.List;

import org.apache.jena.geosparql.configuration.GeoSPARQLConfig;
import org.apache.jena.geosparql.implementation.datatype.WKTDatatype;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.query.QueryBuildException;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QuerySolution;
import org.apache.jena.query.ResultSet;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.sparql.expr.ExprEvalException;
import org.apache.jena.sparql.expr.NodeValue;
import org.apache.jena.sparql.function.FunctionBase1;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

@RunWith(Parameterized.class)
public class GeometryCoordinateExtremaFFTest {
    @Parameterized.Parameters(name = "function: {0}")
    public static List<Object[]> functions() {
        return List.of(
            new Object[] { "minX", new MinXFF() },
            new Object[] { "minY", new MinYFF() },
            new Object[] { "minZ", new MinZFF() },
            new Object[] { "maxX", new MaxXFF() },
            new Object[] { "maxY", new MaxYFF() },
            new Object[] { "maxZ", new MaxZFF() }
        );
    }

    private final String name;
    private final FunctionBase1 function;

    public GeometryCoordinateExtremaFFTest(String name, FunctionBase1 function) {
        this.name = name;
        this.function = function;
    }

    @BeforeClass
    public static void setup() {
        GeoSPARQLConfig.setupNoIndex();
    }

    @Test
    public void iriArgumentRaisesExpressionError() {
        assertThrows(ExprEvalException.class, () -> function.exec(NodeValue.makeNode(NodeFactory.createURI("urn:not-a-literal"))));
    }

    @Test
    public void numericArgumentRaisesExpressionError() {
        assertThrows(ExprEvalException.class, () -> function.exec(NodeValue.makeInteger(42)));
    }

    @Test
    public void stringWithoutGeometryDatatypeRaisesExpressionError() {
        assertThrows(ExprEvalException.class, () -> function.exec(NodeValue.makeString("POINT (1 2)")));
    }

    @Test
    public void malformedWktRaisesExpressionError() {
        NodeValue malformed = NodeValue.makeNode("invalid", WKTDatatype.INSTANCE);
        assertThrows(ExprEvalException.class, () -> function.exec(malformed));
    }

    @Test
    public void iriArgumentLeavesBindUnbound() {
        assertUnbound("<urn:not-a-literal>");
    }

    @Test
    public void numericArgumentLeavesBindUnbound() {
        assertUnbound("42");
    }

    @Test
    public void stringWithoutGeometryDatatypeLeavesBindUnbound() {
        assertUnbound("'POINT (1 2)'");
    }

    @Test
    public void malformedWktLeavesBindUnbound() {
        assertUnbound("'invalid'^^geo:wktLiteral");
    }

    @Test
    public void unboundArgumentLeavesBindUnbound() {
        assertUnbound("?missing");
    }

    @Test
    public void missingArgumentIsRejectedAtQueryBuild() {
        assertThrows(QueryBuildException.class, () -> evaluate("geof:" + name + "()"));
    }

    @Test
    public void extraArgumentIsRejectedAtQueryBuild() {
        assertThrows(QueryBuildException.class,
                     () -> evaluate("geof:" + name + "('POINT EMPTY'^^geo:wktLiteral, 1)"));
    }

    @Test
    public void extremaReturnDoubleLiterals() {
        double expected = switch (name) {
            case "minX" -> -9;
            case "maxX" -> -2;
            case "minY" -> 3;
            case "maxY" -> 8;
            case "minZ" -> -7;
            case "maxZ" -> -1;
            default -> throw new AssertionError(name);
        };
        assertEquals(NodeValue.makeDouble(expected).asNode(),
                     evaluate("geof:" + name + "('LINESTRING ZM (-9 8 -7 100, -2 3 -1 -100)'^^geo:wktLiteral)"));
    }

    @Test
    public void epsg4326LineStringExtremaFollowSrsDimensionOrder() {
        String argument = "'<http://www.opengis.net/def/crs/EPSG/0/4326> "
                        + "LINESTRING (10 100, 20 120)'^^geo:wktLiteral";
        if (name.endsWith("Z")) {
            assertUnbound(argument);
        } else {
            double expected = switch (name) {
                case "minX" -> 10;
                case "maxX" -> 20;
                case "minY" -> 100;
                case "maxY" -> 120;
                default -> throw new AssertionError(name);
            };
            assertEquals(NodeValue.makeDouble(expected).asNode(),
                         evaluate("geof:" + name + "(" + argument + ")"));
        }
    }

    @Test
    public void infiniteOrdinateRaisesExpressionErrorAndLeavesBindUnbound() {
        for (String infinity : new String[] { "Infinity", "-Infinity" }) {
            String coordinate = switch (name.charAt(name.length() - 1)) {
                case 'X' -> infinity + " 1 1";
                case 'Y' -> "1 " + infinity + " 1";
                case 'Z' -> "1 1 " + infinity;
                default -> throw new AssertionError(name);
            };
            String wkt = "LINESTRING Z (-4 -4 -4, " + coordinate + ", 9 9 9)";
            assertThrows(wkt, ExprEvalException.class,
                         () -> function.exec(NodeValue.makeNode(wkt, WKTDatatype.INSTANCE)));
            assertUnbound("'" + wkt + "'^^geo:wktLiteral");
        }
    }

    @Test
    public void nanXYOrdinateRaisesExpressionErrorAndLeavesBindUnbound() {
        assumeFalse("Only X/Y extrema are covered by this test", name.endsWith("Z"));
        String coordinate = name.endsWith("Y") ? "2 NaN" : "NaN 2";
        String wkt = "LINESTRING (1 1, " + coordinate + ", 3 3)";
        assertThrows(wkt, ExprEvalException.class,
                     () -> function.exec(NodeValue.makeNode(wkt, WKTDatatype.INSTANCE)));
        assertUnbound("'" + wkt + "'^^geo:wktLiteral");
    }

    @Test
    public void emptyGeometryRaisesExpressionErrorAndLeavesBindUnbound() {
        assertThrows(ExprEvalException.class,
                     () -> function.exec(NodeValue.makeNode("POINT Z EMPTY", WKTDatatype.INSTANCE)));
        assertUnbound("'POINT Z EMPTY'^^geo:wktLiteral");
    }

    @Test
    public void layoutsWithoutZStillHaveXYExtrema() {
        for (String wkt : new String[] { "POINT (1 2)", "POINT M (1 2 99)" }) {
            if (name.endsWith("Z")) {
                assertThrows(wkt, ExprEvalException.class,
                             () -> function.exec(NodeValue.makeNode(wkt, WKTDatatype.INSTANCE)));
                assertUnbound("'" + wkt + "'^^geo:wktLiteral");
            } else {
                double expected = name.endsWith("X") ? 1 : 2;
                assertEquals(wkt, NodeValue.makeDouble(expected).asNode(),
                             evaluate("geof:" + name + "('" + wkt + "'^^geo:wktLiteral)"));
            }
        }
    }

    @Test
    public void multiPointExtremaDistinguishZFromMeasures() {
        for (boolean hasZ : new boolean[] { false, true }) {
            String wkt = hasZ ? "MULTIPOINT ZM ((1 2 -4 10), (3 4 9 20))"
                              : "MULTIPOINT M ((1 2 10), (3 4 20))";
            if (name.endsWith("Z") && !hasZ) {
                assertThrows(ExprEvalException.class,
                             () -> function.exec(NodeValue.makeNode(wkt, WKTDatatype.INSTANCE)));
                assertUnbound("'" + wkt + "'^^geo:wktLiteral");
            } else {
                double value = switch (name) {
                    case "minX" -> 1;
                    case "maxX" -> 3;
                    case "minY" -> 2;
                    case "maxY" -> 4;
                    case "minZ" -> -4;
                    case "maxZ" -> 9;
                    default -> throw new AssertionError(name);
                };
                NodeValue expected = NodeValue.makeDouble(value);
                assertEquals(expected, function.exec(NodeValue.makeNode(wkt, WKTDatatype.INSTANCE)));
                assertEquals(expected.asNode(), evaluate("geof:" + name + "('" + wkt + "'^^geo:wktLiteral)"));
            }
        }
    }

    @Test
    public void mixedCollectionExtremaDoNotDependOnMemberOrder() {
        for (String wkt : new String[] {
                "GEOMETRYCOLLECTION (POINT Z (1 2 3))",
                "GEOMETRYCOLLECTION (POINT (1 2), POINT M (1 2 999), POINT Z (1 2 3))",
                "GEOMETRYCOLLECTION (POINT Z (1 2 3), POINT M (1 2 999), POINT (1 2))" }) {
            NodeValue expected = NodeValue.makeDouble(name.endsWith("X") ? 1 : name.endsWith("Y") ? 2 : 3);
            assertEquals(expected, function.exec(NodeValue.makeNode(wkt, WKTDatatype.INSTANCE)));
            assertEquals(expected.asNode(), evaluate("geof:" + name + "('" + wkt + "'^^geo:wktLiteral)"));
        }
    }

    @Test
    public void gmlExtremaFollowSrsDimensionOrder() {
        String point = """
            '<gml:Point xmlns:gml="http://www.opengis.net/gml/3.2"
                        srsName="http://www.opengis.net/def/crs/EPSG/0/4979">
               <gml:pos srsDimension="3">10 100 -4</gml:pos>
             </gml:Point>'^^geo:gmlLiteral
            """.replace("\n", " ");
        double expected = name.endsWith("X") ? 10 : name.endsWith("Y") ? 100 : -4;
        assertEquals(NodeValue.makeDouble(expected).asNode(), evaluate("geof:" + name + "(" + point + ")"));
    }

    private static Node evaluate(String expression) {
        String query = """
            PREFIX geof: <http://www.opengis.net/def/function/geosparql/>
            PREFIX geo: <http://www.opengis.net/ont/geosparql#>
            SELECT ?result WHERE { BIND(%s AS ?result) }
            """.formatted(expression);
        try (QueryExecution execution = QueryExecution.create(query, ModelFactory.createDefaultModel())) {
            ResultSet results = execution.execSelect();
            assertTrue("Expected one solution for " + expression, results.hasNext());
            QuerySolution solution = results.next();
            assertFalse("Expected only one solution for " + expression, results.hasNext());
            return solution.contains("result") ? solution.get("result").asNode() : null;
        }
    }

    private void assertUnbound(String argument) {
        String expression = "geof:" + name + "(" + argument + ")";
        assertNull(expression, evaluate(expression));
    }
}
