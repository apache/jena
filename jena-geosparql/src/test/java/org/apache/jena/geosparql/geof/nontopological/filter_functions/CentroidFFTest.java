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
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;

import org.apache.jena.geosparql.configuration.GeoSPARQLConfig;
import org.apache.jena.geosparql.implementation.GeometryWrapper;
import org.apache.jena.geosparql.implementation.datatype.GMLDatatype;
import org.apache.jena.geosparql.implementation.datatype.WKTDatatype;
import org.apache.jena.geosparql.implementation.jts.CoordinateSequenceDimensions;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.query.QueryBuildException;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QuerySolution;
import org.apache.jena.query.ResultSet;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.sparql.expr.ExprEvalException;
import org.apache.jena.sparql.expr.NodeValue;
import org.junit.BeforeClass;
import org.junit.Test;

public class CentroidFFTest {
    @BeforeClass
    public static void setup() {
        GeoSPARQLConfig.setupNoIndex();
    }

    @Test
    public void pointAndMultiPointUseCoordinateMean() {
        assertCentroid("POINT (2 4)", 2, 4);
        assertCentroid("MULTIPOINT ((0 0), (6 0), (0 6))", 2, 2);
    }

    @Test
    public void lineCentroidIsLengthWeighted() {
        assertCentroid("LINESTRING (0 0, 8 0, 8 2)", 4.8, 0.2);
        assertCentroid("MULTILINESTRING ((0 0, 8 0), (8 0, 8 2))", 4.8, 0.2);
    }

    @Test
    public void polygonCentroidUsesArea() {
        assertCentroid("POLYGON ((0 0, 6 0, 0 6, 0 0))", 2, 2);
    }

    @Test
    public void polygonHolesSubtractArea() {
        // Outer area 16 centred at (2,2), hole area 1 centred at (1.5,1.5).
        assertCentroid("POLYGON ((0 0, 4 0, 4 4, 0 4, 0 0), (1 1, 1 2, 2 2, 2 1, 1 1))",
                       30.5 / 15, 30.5 / 15);
    }

    @Test
    public void multiPolygonCentroidIsAreaWeighted() {
        assertCentroid("MULTIPOLYGON (((0 0, 2 0, 2 2, 0 2, 0 0)), ((4 0, 8 0, 8 2, 4 2, 4 0)))",
                       13.0 / 3, 1);
    }

    @Test
    public void mixedCollectionUsesHighestDimensionalMembers() {
        assertCentroid("GEOMETRYCOLLECTION (POINT (90 90), LINESTRING (0 0, 8 0))", 4, 0);
        assertCentroid("GEOMETRYCOLLECTION (POINT (90 90), POLYGON ((0 0, 6 0, 0 6, 0 0)))", 2, 2);
    }

    @Test
    public void resultDropsZAndMAndHasPointMetadata() {
        for (String wkt : new String[] { "POINT Z (2 4 99)", "POINT M (2 4 99)", "POINT ZM (2 4 99 100)",
                                        "LINESTRING ZM (0 4 9 8, 4 4 7 6)" }) {
            assertCentroid(wkt, 2, 4);
        }
    }

    @Test
    public void authorityAxisOrderIsRetainedInTheResult() {
        String crs = "<http://www.opengis.net/def/crs/EPSG/0/4326> ";
        GeometryWrapper result = centroid(crs + "LINESTRING (10 100, 20 120)");
        assertEquals("http://www.opengis.net/def/crs/EPSG/0/4326", result.getSrsURI());
        assertEquals(15, result.getParsingGeometry().getCoordinate().getX(), 0);
        assertEquals(110, result.getParsingGeometry().getCoordinate().getY(), 0);
        assertEquals(110, result.getXYGeometry().getCoordinate().getX(), 0);
        assertEquals(15, result.getXYGeometry().getCoordinate().getY(), 0);
    }

    @Test
    public void projectedCrsIsPreserved() {
        String crs = "<http://www.opengis.net/def/crs/EPSG/0/27700> ";
        GeometryWrapper result = centroid(crs + "LINESTRING (100 200, 300 400)");
        assertEquals("http://www.opengis.net/def/crs/EPSG/0/27700", result.getSrsURI());
        assertEquals(200, result.getXYGeometry().getCoordinate().getX(), 0);
        assertEquals(300, result.getXYGeometry().getCoordinate().getY(), 0);
    }

    @Test
    public void geographicCentroidIsPlanar() {
        assertCentroid("MULTIPOINT ((170 10), (-170 10))", 0, 10);
    }

    @Test
    public void emptyInputsReturnAnEmptyXyPoint() {
        for (String wkt : new String[] { "POINT EMPTY", "LINESTRING EMPTY", "POLYGON EMPTY",
                "MULTIPOINT EMPTY", "MULTILINESTRING EMPTY", "MULTIPOLYGON EMPTY", "GEOMETRYCOLLECTION EMPTY",
                "POINT Z EMPTY", "POINT M EMPTY", "POINT ZM EMPTY" }) {
            GeometryWrapper result = centroid(wkt);
            assertPointMetadata(result);
            assertTrue(wkt, result.isEmpty());
            assertEquals("POINT EMPTY", result.asNodeValue().asNode().getLiteralLexicalForm());
        }
    }

    @Test
    public void gmlResultRetainsDatatypeAndAxisOrder() {
        String gml = """
            '<gml:LineString xmlns:gml="http://www.opengis.net/gml/3.2"
                srsName="http://www.opengis.net/def/crs/EPSG/0/4326">
              <gml:posList>10 100 20 120</gml:posList>
            </gml:LineString>'^^geo:gmlLiteral
            """.replace("\n", " ");
        Node node = evaluate("geof:centroid(" + gml + ")");
        assertNotNull(node);
        assertEquals("http://www.opengis.net/ont/geosparql#gmlLiteral", node.getLiteralDatatypeURI());
        GeometryWrapper result = GeometryWrapper.extract(node);
        assertPointMetadata(result);
        assertEquals("http://www.opengis.net/def/crs/EPSG/0/4326", result.getSrsURI());
        assertEquals(15, result.getParsingGeometry().getCoordinate().getX(), 0);
        assertEquals(110, result.getParsingGeometry().getCoordinate().getY(), 0);
    }

    @Test
    public void threeDimensionalWktCrsReturnsAProjectedPoint() {
        GeometryWrapper result = centroid("<http://www.opengis.net/def/crs/EPSG/0/4979> POINT Z (10 100 7)");
        assertEquals("Point", result.getGeometryType());
        assertEquals(0, result.getTopologicalDimension());
        assertEquals(CoordinateSequenceDimensions.XYZ, result.getCoordinateSequenceDimensions());
        assertEquals("http://www.opengis.net/def/crs/EPSG/0/4979", result.getSrsURI());
        assertEquals(10, result.getParsingGeometry().getCoordinate().getX(), 0);
        assertEquals(100, result.getParsingGeometry().getCoordinate().getY(), 0);
        assertEquals(0, result.getParsingGeometry().getCoordinate().getZ(), 0);
    }

    @Test
    public void threeDimensionalGmlCentroidComposesWithOtherFunctions() {
        String input = """
            '<gml:LineString xmlns:gml="http://www.opengis.net/gml/3.2"
                srsName="http://www.opengis.net/def/crs/EPSG/0/4979">
              <gml:posList>10 100 7 20 120 9</gml:posList>
            </gml:LineString>'^^geo:gmlLiteral
            """.replace("\n", " ");
        Node node = evaluate("geof:centroid(" + input + ")");
        assertNotNull(node);
        assertEquals(GMLDatatype.URI, node.getLiteralDatatypeURI());
        GeometryWrapper result = GeometryWrapper.extract(node);
        assertEquals("http://www.opengis.net/def/crs/EPSG/0/4979", result.getSrsURI());
        assertEquals(CoordinateSequenceDimensions.XYZ, result.getCoordinateSequenceDimensions());
        assertEquals(15, result.getParsingGeometry().getCoordinate().getX(), 0);
        assertEquals(110, result.getParsingGeometry().getCoordinate().getY(), 0);
        assertEquals(0, result.getParsingGeometry().getCoordinate().getZ(), 0);
        assertEquals(NodeValue.makeInteger(3).asNode(), evaluate("geof:coordinateDimension(geof:centroid(" + input + "))"));
    }

    @Test
    public void centroidComposesWithOtherGeometryFunctions() {
        assertEquals(NodeValue.makeInteger(0).asNode(), evaluate(
            "geof:dimension(geof:centroid('POLYGON ((0 0, 6 0, 0 6, 0 0))'^^geo:wktLiteral))"));
    }

    @Test
    public void invalidArgumentsRaiseExpressionErrors() {
        CentroidFF function = new CentroidFF();
        for (NodeValue value : new NodeValue[] { NodeValue.makeInteger(42), NodeValue.makeString("POINT (1 2)"),
                NodeValue.makeNode(NodeFactory.createURI("urn:geometry")), NodeValue.makeNode("invalid", WKTDatatype.INSTANCE) }) {
            assertThrows(ExprEvalException.class, () -> function.exec(value));
        }
        for (String value : new String[] { "42", "'POINT (1 2)'", "<urn:geometry>", "'invalid'^^geo:wktLiteral", "?missing" }) {
            assertNull(evaluate("geof:centroid(" + value + ")"));
        }
    }

    @Test
    public void wrongArityIsRejectedAtQueryBuild() {
        assertThrows(QueryBuildException.class, () -> evaluate("geof:centroid()"));
        assertThrows(QueryBuildException.class,
                     () -> evaluate("geof:centroid('POINT EMPTY'^^geo:wktLiteral, 1)"));
    }

    private static void assertCentroid(String wkt, double x, double y) {
        GeometryWrapper result = centroid(wkt);
        assertPointMetadata(result);
        assertFalse(wkt, result.isEmpty());
        assertEquals(wkt, x, result.getXYGeometry().getCoordinate().getX(), 1e-6);
        assertEquals(wkt, y, result.getXYGeometry().getCoordinate().getY(), 1e-6);
    }

    private static void assertPointMetadata(GeometryWrapper result) {
        assertEquals("Point", result.getGeometryType());
        assertEquals(0, result.getTopologicalDimension());
        assertEquals(CoordinateSequenceDimensions.XY, result.getCoordinateSequenceDimensions());
    }

    private static GeometryWrapper centroid(String wkt) {
        Node node = evaluate("geof:centroid('" + wkt + "'^^geo:wktLiteral)");
        assertNotNull(wkt, node);
        assertEquals(WKTDatatype.URI, node.getLiteralDatatypeURI());
        return GeometryWrapper.extract(node);
    }

    private static Node evaluate(String expression) {
        String query = """
            PREFIX geof: <http://www.opengis.net/def/function/geosparql/>
            PREFIX geo: <http://www.opengis.net/ont/geosparql#>
            SELECT ?result WHERE { BIND(%s AS ?result) }
            """.formatted(expression);
        try (QueryExecution execution = QueryExecution.create(query, ModelFactory.createDefaultModel())) {
            ResultSet results = execution.execSelect();
            assertTrue(expression, results.hasNext());
            QuerySolution solution = results.next();
            assertFalse(expression, results.hasNext());
            return solution.contains("result") ? solution.get("result").asNode() : null;
        }
    }
}
