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
package org.apache.jena.geosparql.geof.nontopological.filter_functions;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;

import org.apache.jena.geosparql.configuration.GeoSPARQLConfig;
import org.apache.jena.geosparql.implementation.datatype.GMLDatatype;
import org.apache.jena.geosparql.implementation.datatype.WKTDatatype;
import org.apache.jena.geosparql.implementation.vocabulary.Unit_URI;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.query.QueryBuildException;
import org.apache.jena.sparql.expr.ExprEvalException;
import org.apache.jena.sparql.expr.NodeValue;
import org.junit.BeforeClass;
import org.junit.Test;

public class AreaFFTest {
    private final AreaFF function = new AreaFF();
    private static final String PROJECTED = "<http://www.opengis.net/def/crs/EPSG/0/27700> ";
    private static final String POLYGON = "'<http://www.opengis.net/def/crs/EPSG/0/27700> POLYGON ((0 0, 1000 0, 1000 1000, 0 1000, 0 0))'^^geo:wktLiteral";
    private static final String SQUARE_METRE = "<" + Unit_URI.SQUARE_METRE_QUDT + ">";

    @BeforeClass
    public static void setup() {
        GeoSPARQLConfig.setupNoIndex();
    }

    @Test
    public void polygonReturnsDoubleSquareMetres() {
        String polygon = "'" + PROJECTED + "POLYGON ((0 0, 3 0, 3 4, 0 4, 0 0))'^^geo:wktLiteral";
        assertEquals(NodeValue.makeDouble(12).asNode(), evaluate(polygon, SQUARE_METRE));
    }

    @Test
    public void nonPolygonAndEmptyGeometriesReturnZero() {
        for (String wkt : new String[] { "POINT (1 2)", PROJECTED + "POLYGON EMPTY",
                PROJECTED + "MULTIPOLYGON(((0 0,2 0,2 2,0 2,0 0)))",
                "GEOMETRYCOLLECTION(POLYGON((0 0,2 0,2 2,0 2,0 0)))" }) {
            assertEquals(NodeValue.makeDouble(0).asNode(), evaluate("'" + wkt + "'^^geo:wktLiteral", SQUARE_METRE));
        }
    }

    @Test
    public void geographicPolygonReturnsArea() {
        String wkt = "POLYGON ((0 0, 1 0, 1 1, 0 1, 0 0))";
        Node result = evaluate("'" + wkt + "'^^geo:wktLiteral", SQUARE_METRE);
        double area = ((Number) result.getLiteralValue()).doubleValue();
        assertTrue(area > 12_000_000_000.0 && area < 12_500_000_000.0);
    }

    @Test
    public void gmlPolygonIsAccepted() {
        String gml = """
            '<gml:Polygon xmlns:gml="http://www.opengis.net/gml/3.2"
                srsName="http://www.opengis.net/def/crs/EPSG/0/27700">
              <gml:exterior><gml:LinearRing><gml:posList>0 0 3 0 3 4 0 4 0 0</gml:posList></gml:LinearRing></gml:exterior>
            </gml:Polygon>'^^geo:gmlLiteral
            """.replace("\n", " ");
        assertEquals(NodeValue.makeDouble(12).asNode(), evaluate(gml, SQUARE_METRE));
    }

    @Test
    public void geographicGmlUsesDeclaredAxisOrder() {
        String wkt = "'<http://www.opengis.net/def/crs/EPSG/0/4326> "
                + "POLYGON((0 0,0 2,1 2,1 0,0 0))'^^geo:wktLiteral";
        String gml = """
            '<gml:Polygon xmlns:gml="http://www.opengis.net/gml/3.2"
                srsName="http://www.opengis.net/def/crs/EPSG/0/4326">
              <gml:exterior><gml:LinearRing><gml:posList>0 0 0 2 1 2 1 0 0 0</gml:posList></gml:LinearRing></gml:exterior>
            </gml:Polygon>'^^geo:gmlLiteral
            """.replace("\n", " ");

        double expected = ((Number) evaluate(wkt, SQUARE_METRE).getLiteralValue()).doubleValue();
        double actual = ((Number) evaluate(gml, SQUARE_METRE).getLiteralValue()).doubleValue();
        assertEquals(expected, actual, expected * 1e-8);
    }

    @Test
    public void malformedOrUnboundGeometryLeavesBindUnbound() {
        for (String value : new String[] { "42", "'POINT (1 2)'", "<urn:geometry>", "'invalid'^^geo:wktLiteral", "?missing" }) {
            assertNull(value, evaluate(value, SQUARE_METRE));
        }
    }

    @Test
    public void invalidGeometryArgumentsRaiseExpressionErrors() {
        NodeValue squareMetre = NodeValue.makeNode(NodeFactory.createURI(Unit_URI.SQUARE_METRE_QUDT));
        for (NodeValue value : new NodeValue[] { NodeValue.makeInteger(42), NodeValue.makeString("POINT (1 2)"),
                NodeValue.makeNode(NodeFactory.createURI("urn:geometry")), NodeValue.makeNode("invalid", WKTDatatype.INSTANCE) }) {
            assertThrows(ExprEvalException.class, () -> function.exec(value, squareMetre));
        }
    }

    @Test
    public void nonFiniteCoordinatesAndAreaRaiseExpressionErrors() {
        NodeValue squareMetre = NodeValue.makeNode(NodeFactory.createURI(Unit_URI.SQUARE_METRE_QUDT));
        for (String wkt : new String[] {
                PROJECTED + "LINESTRING(0 0,1e309 1)",
                PROJECTED + "POLYGON((0 0,1 0,1e309 1,0 1,0 0))",
                PROJECTED + "POLYGON((0 0,1e200 0,1e200 1e200,0 1e200,0 0))" }) {
            assertThrows(wkt, ExprEvalException.class,
                    () -> function.exec(NodeValue.makeNode(wkt, WKTDatatype.INSTANCE), squareMetre));
            assertNull(wkt, evaluate("'" + wkt + "'^^geo:wktLiteral", SQUARE_METRE));
        }

        String gml = """
            <gml:Polygon xmlns:gml="http://www.opengis.net/gml/3.2"
                srsName="http://www.opengis.net/def/crs/EPSG/0/27700">
              <gml:exterior><gml:LinearRing><gml:posList>0 0 1 0 NaN 1 0 1 0 0</gml:posList></gml:LinearRing></gml:exterior>
            </gml:Polygon>
            """.replace("\n", " ");
        assertThrows(ExprEvalException.class,
                () -> function.exec(NodeValue.makeNode(gml, GMLDatatype.INSTANCE), squareMetre));
        assertNull(evaluate("'" + gml + "'^^geo:gmlLiteral", SQUARE_METRE));
    }

    @Test
    public void wrongArityIsRejectedAtQueryBuild() {
        String geometry = "'POINT EMPTY'^^geo:wktLiteral";
        assertThrows(QueryBuildException.class, () -> AreaFunctionTestSupport.evaluate("geof:area()"));
        assertThrows(QueryBuildException.class, () -> AreaFunctionTestSupport.evaluate("geof:area(" + geometry + ")"));
        assertThrows(QueryBuildException.class, () -> AreaFunctionTestSupport.evaluate("geof:area(" + geometry + ", 1, 2)"));
    }

    @Test
    public void explicitAreaUnitsConvertTheAreaQuantity() {
        Object[][] expected = {
                { Unit_URI.SQUARE_METRE_QUDT, 1_000_000.0 },
                { Unit_URI.SQUARE_KILOMETRE_QUDT, 1.0 },
                { Unit_URI.SQUARE_CENTIMETRE_QUDT, 10_000_000_000.0 },
                { Unit_URI.SQUARE_MILLIMETRE_QUDT, 1_000_000_000_000.0 },
                { Unit_URI.SQUARE_FOOT_QUDT, 10_763_910.416709722 },
                { Unit_URI.SQUARE_YARD_QUDT, 1_195_990.0463010803 },
                { Unit_URI.SQUARE_INCH_QUDT, 1_550_003_100.0062 },
                { Unit_URI.SQUARE_MILE_QUDT, 0.38610215854244585 },
                { Unit_URI.HECTARE_QUDT, 100.0 },
                { Unit_URI.ACRE_QUDT, 247.10538146716534 },
        };
        for (Object[] entry : expected) {
            String uri = (String) entry[0];
            double area = (double) entry[1];
            Node result = evaluate(POLYGON, "<" + uri + ">");
            assertEquals(uri, area, ((Number) result.getLiteralValue()).doubleValue(), area * 1e-12);
        }
    }

    @Test
    public void undeclaredOgcLookingAreaUriIsRejected() {
        String uri = "http://www.opengis.net/def/uom/OGC/1.0/squareMetre";
        assertThrows(ExprEvalException.class,
                () -> function.exec(NodeValue.makeNode("POINT EMPTY", WKTDatatype.INSTANCE),
                        NodeValue.makeNode(NodeFactory.createURI(uri))));
        assertNull(evaluate(POLYGON, "<" + uri + ">"));
    }

    @Test
    public void anyUriUnitLiteralIsAccepted() {
        assertEquals(NodeValue.makeDouble(1).asNode(), evaluate(POLYGON, "'" + Unit_URI.SQUARE_KILOMETRE_QUDT + "'^^xsd:anyURI"));
    }

    @Test
    public void invalidUnitsRaiseExpressionErrorsEvenForEmptyGeometry() {
        NodeValue empty = NodeValue.makeNode("POINT EMPTY", WKTDatatype.INSTANCE);
        for (NodeValue unit : new NodeValue[] { NodeValue.makeString(Unit_URI.SQUARE_METRE_QUDT), NodeValue.makeInteger(1),
                NodeValue.makeNode(NodeFactory.createURI("urn:unknown-unit")),
                NodeValue.makeNode(NodeFactory.createURI(Unit_URI.KILOMETRE_URL)) }) {
            assertThrows(ExprEvalException.class, () -> function.exec(empty, unit));
        }
    }

    @Test
    public void invalidUnitsLeaveBindUnboundForEmptyAndNonemptyInputs() {
        for (String unit : new String[] { "'" + Unit_URI.SQUARE_METRE_QUDT + "'", "1", "<urn:unknown-unit>",
                                        "<" + Unit_URI.KILOMETRE_URL + ">", "?missing" }) {
            assertNull(unit, evaluate(POLYGON, unit));
            assertNull(unit, evaluate("'POINT EMPTY'^^geo:wktLiteral", unit));
        }
    }

    private Node evaluate(String geometry, String unit) {
        return AreaFunctionTestSupport.evaluate("geof:area(" + geometry + ", " + unit + ")");
    }
}
