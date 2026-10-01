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
import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.query.QueryBuildException;
import org.apache.jena.sparql.expr.ExprEvalException;
import org.apache.jena.sparql.expr.NodeValue;
import org.junit.BeforeClass;
import org.junit.Test;

public class MetricAreaFFTest {
    private static final String PROJECTED = "<http://www.opengis.net/def/crs/EPSG/0/27700> ";
    private final MetricAreaFF function = new MetricAreaFF();

    @BeforeClass
    public static void setup() {
        GeoSPARQLConfig.setupNoIndex();
    }

    @Test
    public void polygonReturnsDoubleSquareMetres() {
        String polygon = "'" + PROJECTED + "POLYGON ((0 0, 3 0, 3 4, 0 4, 0 0))'^^geo:wktLiteral";
        assertEquals(NodeValue.makeDouble(12).asNode(), evaluate(polygon));
    }

    @Test
    public void nonPolygonAndEmptyGeometriesReturnZero() {
        for (String wkt : new String[] { "POINT (1 2)", PROJECTED + "POLYGON EMPTY",
                PROJECTED + "MULTIPOLYGON(((0 0,2 0,2 2,0 2,0 0)))",
                "GEOMETRYCOLLECTION(POLYGON((0 0,2 0,2 2,0 2,0 0)))" }) {
            assertEquals(NodeValue.makeDouble(0).asNode(), evaluate("'" + wkt + "'^^geo:wktLiteral"));
        }
    }

    @Test
    public void geographicPolygonReturnsSquareMetres() {
        String wkt = "POLYGON ((0 0, 1 0, 1 1, 0 1, 0 0))";
        Node result = evaluate("'" + wkt + "'^^geo:wktLiteral");
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
        assertEquals(NodeValue.makeDouble(12).asNode(), evaluate(gml));
    }

    @Test
    public void malformedOrUnboundGeometryLeavesBindUnbound() {
        for (String value : new String[] { "42", "'POINT (1 2)'", "<urn:geometry>", "'invalid'^^geo:wktLiteral", "?missing" }) {
            assertNull(value, evaluate(value));
        }
    }

    @Test
    public void invalidGeometryArgumentsRaiseExpressionErrors() {
        for (NodeValue value : new NodeValue[] { NodeValue.makeInteger(42), NodeValue.makeString("POINT (1 2)"),
                NodeValue.makeNode(NodeFactory.createURI("urn:geometry")), NodeValue.makeNode("invalid", WKTDatatype.INSTANCE) }) {
            assertThrows(ExprEvalException.class, () -> exec(value));
        }
    }

    @Test
    public void nonFiniteCoordinatesAndAreaRaiseExpressionErrors() {
        for (String wkt : new String[] {
                PROJECTED + "LINESTRING(0 0,1e309 1)",
                PROJECTED + "POLYGON((0 0,1 0,1e309 1,0 1,0 0))",
                PROJECTED + "POLYGON((0 0,1e200 0,1e200 1e200,0 1e200,0 0))" }) {
            assertThrows(wkt, ExprEvalException.class,
                    () -> function.exec(NodeValue.makeNode(wkt, WKTDatatype.INSTANCE)));
            assertNull(wkt, evaluate("'" + wkt + "'^^geo:wktLiteral"));
        }

        String gml = """
            <gml:Polygon xmlns:gml="http://www.opengis.net/gml/3.2"
                srsName="http://www.opengis.net/def/crs/EPSG/0/27700">
              <gml:exterior><gml:LinearRing><gml:posList>0 0 1 0 NaN 1 0 1 0 0</gml:posList></gml:LinearRing></gml:exterior>
            </gml:Polygon>
            """.replace("\n", " ");
        assertThrows(ExprEvalException.class,
                () -> function.exec(NodeValue.makeNode(gml, GMLDatatype.INSTANCE)));
        assertNull(evaluate("'" + gml + "'^^geo:gmlLiteral"));
    }

    @Test
    public void wrongArityIsRejectedAtQueryBuild() {
        String geometry = "'POINT EMPTY'^^geo:wktLiteral";
        assertThrows(QueryBuildException.class, () -> AreaFunctionTestSupport.evaluate("geof:metricArea()"));
        assertThrows(QueryBuildException.class, () -> AreaFunctionTestSupport.evaluate("geof:metricArea(" + geometry + ", 1)"));
        assertThrows(QueryBuildException.class, () -> AreaFunctionTestSupport.evaluate("geof:metricArea(" + geometry + ", 1, 2)"));
    }

    private NodeValue exec(NodeValue value) {
        return function.exec(value);
    }

    private Node evaluate(String geometry) {
        return AreaFunctionTestSupport.evaluate("geof:metricArea(" + geometry + ")");
    }
}
