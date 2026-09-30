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

import org.apache.jena.datatypes.xsd.XSDDatatype;
import org.apache.jena.geosparql.configuration.GeoSPARQLConfig;
import org.apache.jena.geosparql.implementation.GeometryWrapper;
import org.apache.jena.geosparql.implementation.datatype.GMLDatatype;
import org.apache.jena.geosparql.implementation.datatype.WKTDatatype;
import org.apache.jena.geosparql.implementation.index.GeometryLiteralIndex;
import org.apache.jena.geosparql.implementation.jts.CoordinateSequenceDimensions;
import org.apache.jena.geosparql.implementation.vocabulary.SRS_URI;
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
import org.locationtech.jts.geom.CoordinateSequence;
import org.locationtech.jts.geom.LineString;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.geom.Polygon;
import org.opengis.referencing.operation.TransformException;

public class TransformFFTest {
    private static final String CRS84 = SRS_URI.DEFAULT_WKT_CRS84;
    private static final String WGS84 = "http://www.opengis.net/def/crs/EPSG/0/4326";
    private static final String WGS84_3D = "http://www.opengis.net/def/crs/EPSG/0/4979";
    private static final String WEB_MERCATOR = "http://www.opengis.net/def/crs/EPSG/0/3857";
    private static final String UTM34N = "http://www.opengis.net/def/crs/EPSG/0/32634";
    private static final String UNKNOWN = "http://example.org/crs/unknown";
    private final TransformFF function = new TransformFF();

    @BeforeClass
    public static void setup() {
        GeoSPARQLConfig.setupNoIndex();
    }

    @Test
    public void projectedPointAndReverse() {
        NodeValue point = wkt("POINT(24.5887755 41.4035958)");
        NodeValue projected = function.exec(point, iri(UTM34N));
        GeometryWrapper result = GeometryWrapper.extract(projected);
        assertEquals(WKTDatatype.URI, result.getGeometryDatatypeURI());
        assertEquals(UTM34N, result.getSrsURI());
        Point projectedPoint = (Point)result.getParsingGeometry();
        assertEquals(799997.8, projectedPoint.getX(), 1.0);
        assertEquals(4589779.63, projectedPoint.getY(), 1.0);

        NodeValue reversed = function.exec(projected, NodeValue.makeNode(CRS84, XSDDatatype.XSDanyURI));
        GeometryWrapper reversedGeometry = GeometryWrapper.extract(reversed);
        assertEquals(CRS84, reversedGeometry.getSrsURI());
        Point reversedPoint = (Point)reversedGeometry.getParsingGeometry();
        assertEquals(24.5887755, reversedPoint.getX(), 1e-5);
        assertEquals(41.4035958, reversedPoint.getY(), 1e-5);
    }

    @Test
    public void axisOrderAndVertices() {
        NodeValue source = wkt("<" + WGS84 + "> LINESTRING(41 24, 42 25)");
        GeometryWrapper result = GeometryWrapper.extract(function.exec(source, iri(CRS84)));
        LineString line = (LineString)result.getParsingGeometry();
        CoordinateSequence coordinates = line.getCoordinateSequence();
        assertEquals(CRS84, result.getSrsURI());
        assertEquals(2, coordinates.size());
        assertEquals(24, coordinates.getX(0), 0.0);
        assertEquals(41, coordinates.getY(0), 0.0);
        assertEquals(25, coordinates.getX(1), 0.0);
        assertEquals(42, coordinates.getY(1), 0.0);
    }

    @Test
    public void polygonWithHoleRetainsStructure() {
        NodeValue source = wkt("<" + WGS84 + "> POLYGON((40 20, 40 26, 44 26, 40 20),"
                + "(41 22, 42 22, 41 23, 41 22))");
        GeometryWrapper result = GeometryWrapper.extract(function.exec(source, iri(CRS84)));
        Polygon polygon = (Polygon)result.getParsingGeometry();
        assertEquals(1, polygon.getNumInteriorRing());
        assertEquals(4, polygon.getExteriorRing().getNumPoints());
        assertEquals(20, polygon.getExteriorRing().getCoordinateN(0).getX(), 0.0);
        assertEquals(40, polygon.getExteriorRing().getCoordinateN(0).getY(), 0.0);
        assertEquals(4, polygon.getInteriorRingN(0).getNumPoints());
    }

    @Test
    public void multipartGeometryRetainsMembers() {
        NodeValue source = wkt("<" + WGS84 + "> MULTILINESTRING((41 24, 42 25),(43 26, 44 27))");
        GeometryWrapper result = GeometryWrapper.extract(function.exec(source, iri(CRS84)));
        assertEquals("MultiLineString", result.getParsingGeometry().getGeometryType());
        assertEquals(2, result.getParsingGeometry().getNumGeometries());
        LineString second = (LineString)result.getParsingGeometry().getGeometryN(1);
        assertEquals(2, second.getNumPoints());
        assertEquals(26, second.getCoordinateN(0).getX(), 0.0);
        assertEquals(43, second.getCoordinateN(0).getY(), 0.0);
    }

    @Test
    public void identityRetainsWktLayout() {
        NodeValue source = wkt("POINT ZM (1 2 3 4)");
        GeometryWrapper result = GeometryWrapper.extract(function.exec(source, iri(CRS84)));
        CoordinateSequence coordinates = ((Point)result.getParsingGeometry()).getCoordinateSequence();
        assertEquals(WKTDatatype.URI, result.getGeometryDatatypeURI());
        assertEquals(CRS84, result.getSrsURI());
        assertEquals(CoordinateSequenceDimensions.XYZM, result.getCoordinateSequenceDimensions());
        assertEquals(1, coordinates.getX(0), 0.0);
        assertEquals(2, coordinates.getY(0), 0.0);
        assertEquals(3, coordinates.getZ(0), 0.0);
        assertEquals(4, coordinates.getM(0), 0.0);
    }

    @Test
    public void projectedPointRetainsZAndM() {
        GeometryWrapper result = GeometryWrapper.extract(function.exec(
                wkt("POINT ZM (24.5887755 41.4035958 123 456)"), iri(UTM34N)));
        CoordinateSequence coordinates = ((Point)result.getParsingGeometry()).getCoordinateSequence();
        assertEquals(4, coordinates.getDimension());
        assertEquals(1, coordinates.getMeasures());
        assertEquals(123, coordinates.getZ(0), 0.0);
        assertEquals(456, coordinates.getM(0), 0.0);
    }

    @Test
    public void emptyLineRetainsTypeAndTargetSrs() {
        GeometryWrapper result = GeometryWrapper.extract(function.exec(wkt("LINESTRING EMPTY"), iri(UTM34N)));
        assertEquals(WKTDatatype.URI, result.getGeometryDatatypeURI());
        assertEquals(UTM34N, result.getSrsURI());
        assertEquals("LineString", result.getParsingGeometry().getGeometryType());
        assertTrue(result.getParsingGeometry().isEmpty());
    }

    @Test
    public void finitePointOutsideTargetAreaOfUse() {
        GeometryWrapper result = GeometryWrapper.extract(function.exec(wkt("POINT(0 0)"), iri(UTM34N)));
        Point point = (Point)result.getParsingGeometry();
        assertEquals(UTM34N, result.getSrsURI());
        assertTrue(Double.isFinite(point.getX()));
        assertTrue(Double.isFinite(point.getY()));
        assertEquals(-1891310.54, point.getX(), 2.0);
        assertEquals(0.0, point.getY(), 1e-8);
    }

    @Test
    public void gmlRetainsDatatype() {
        String gml = "<gml:Point xmlns:gml=\"http://www.opengis.net/gml/3.2\" srsName=\"" + CRS84
                + "\"><gml:pos>24.5887755 41.4035958</gml:pos></gml:Point>";
        GeometryWrapper result = GeometryWrapper.extract(function.exec(NodeValue.makeNode(gml, GMLDatatype.INSTANCE), iri(UTM34N)));
        assertEquals(GMLDatatype.URI, result.getGeometryDatatypeURI());
        assertEquals(UTM34N, result.getSrsURI());
        Point point = (Point)result.getParsingGeometry();
        assertEquals(799997.8, point.getX(), 1.0);
        assertEquals(4589779.63, point.getY(), 1.0);
    }

    @Test
    public void emptyGmlPointFrom3DTo2D() {
        assertEmptyGmlTransform(WGS84_3D, WGS84, 2);
    }

    @Test
    public void emptyGmlPointFrom2DTo3D() {
        assertEmptyGmlTransform(WGS84, WGS84_3D, 3);
    }

    @Test
    public void gmlDimensionalReductionIsAnExpressionError() {
        String gml = gmlLine(WGS84_3D, "10 20 7 30 40 9");
        assertThrows(ExprEvalException.class,
                () -> function.exec(NodeValue.makeNode(gml, GMLDatatype.INSTANCE), iri(WGS84)));
    }

    @Test
    public void gmlDimensionalReductionLeavesBindUnbound() {
        assertNull(evaluate("geof:transform('" + gmlLine(WGS84_3D, "10 20 7 30 40 9")
                + "'^^geo:gmlLiteral, <" + WGS84 + ">)"));
    }

    @Test
    public void gmlTwoDimensionalTransformRetainsVertices() {
        NodeValue result = function.exec(NodeValue.makeNode(gmlLine(WGS84, "10 20 30 40"), GMLDatatype.INSTANCE), iri(CRS84));
        GeometryWrapper parsed = GMLDatatype.INSTANCE.read(result.asNode().getLiteralLexicalForm());
        assertEquals(GMLDatatype.URI, result.asNode().getLiteralDatatypeURI());
        assertEquals(CRS84, parsed.getSrsURI());
        CoordinateSequence coordinates = ((LineString)parsed.getParsingGeometry()).getCoordinateSequence();
        assertEquals(2, coordinates.size());
        assertEquals(2, coordinates.getDimension());
        assertEquals(20, coordinates.getX(0), 0.0);
        assertEquals(10, coordinates.getY(0), 0.0);
        assertEquals(40, coordinates.getX(1), 0.0);
        assertEquals(30, coordinates.getY(1), 0.0);
    }

    @Test
    public void gmlThreeDimensionalTransformRetainsVertices() {
        String targetURI = "http://www.opengis.net/def/crs/EPSG/0/4937";
        NodeValue result = function.exec(NodeValue.makeNode(gmlLine(WGS84_3D, "10 20 7 30 40 9"), GMLDatatype.INSTANCE),
                iri(targetURI));
        GeometryWrapper parsed = GMLDatatype.INSTANCE.read(result.asNode().getLiteralLexicalForm());
        assertEquals(GMLDatatype.URI, result.asNode().getLiteralDatatypeURI());
        assertEquals(targetURI, parsed.getSrsURI());
        CoordinateSequence coordinates = ((LineString)parsed.getParsingGeometry()).getCoordinateSequence();
        assertEquals(2, coordinates.size());
        assertEquals(3, coordinates.getDimension());
        assertEquals(10, coordinates.getX(0), 1e-7);
        assertEquals(20, coordinates.getY(0), 1e-7);
        assertEquals(7, coordinates.getZ(0), 1e-4);
        assertEquals(30, coordinates.getX(1), 1e-7);
        assertEquals(40, coordinates.getY(1), 1e-7);
        assertEquals(9, coordinates.getZ(1), 1e-4);
    }

    @Test
    public void northPoleProjectionIsAnExpressionError() {
        expectProjectionExpressionError("POINT(0 90)");
    }

    @Test
    public void northPoleProjectionLeavesBindUnbound() {
        assertNull(evaluate("geof:transform('POINT(0 90)'^^geo:wktLiteral, <" + WEB_MERCATOR + ">)"));
    }

    @Test
    public void invalidLatitudeProjectionIsAnExpressionError() {
        expectProjectionExpressionError("POINT(0 100)");
    }

    @Test
    public void invalidLatitudeProjectionLeavesBindUnbound() {
        assertNull(evaluate("geof:transform('POINT(0 100)'^^geo:wktLiteral, <" + WEB_MERCATOR + ">)"));
    }

    @Test
    public void unknownSourceOrTargetIsAnExpressionError() {
        expectExpressionError(wkt("<" + UNKNOWN + "> POINT(1 2)"), iri(CRS84));
        expectExpressionError(wkt("POINT(1 2)"), iri(UNKNOWN));
    }

    @Test
    public void unknownSrsIdentitySucceeds() {
        GeometryWrapper result = GeometryWrapper.extract(function.exec(
                wkt("<" + UNKNOWN + "> POINT(1 2)"), iri(UNKNOWN)));
        assertEquals(WKTDatatype.URI, result.getGeometryDatatypeURI());
        assertEquals(UNKNOWN, result.getSrsURI());
        Point point = (Point)result.getParsingGeometry();
        assertEquals(1, point.getX(), 0.0);
        assertEquals(2, point.getY(), 0.0);
    }

    @Test
    public void transformDoesNotCacheGeneratedLiteral() {
        GeoSPARQLConfig.setupMemoryIndex();
        try {
            NodeValue source = wkt("POINT(1 2)");
            GeometryWrapper.extract(source);
            assertEquals(1, GeometryLiteralIndex.getPrimaryIndexSize());

            NodeValue result = function.exec(source, iri(WGS84));
            assertEquals(WKTDatatype.URI, result.asNode().getLiteralDatatypeURI());
            assertEquals(1, GeometryLiteralIndex.getPrimaryIndexSize());
        } finally {
            GeoSPARQLConfig.setupNoIndex();
        }
    }

    @Test
    public void anyUriTargetNormalizesSpaces() {
        assertNormalizedAnyUriTarget("  " + CRS84 + "  ");
    }

    @Test
    public void anyUriTargetNormalizesXmlWhitespace() {
        assertNormalizedAnyUriTarget("\t \r\n" + CRS84 + "\r\n \t");
    }

    @Test
    public void registeredFunctionNormalizesAnyUriTarget() {
        Node result = evaluate("geof:transform('<" + WGS84 + "> POINT(41 24)'^^geo:wktLiteral, '  "
                + CRS84 + "  '^^xsd:anyURI)");
        assertNotNull(result);
        assertEquals("POINT(24 41)", result.getLiteralLexicalForm());
    }

    @Test
    public void targetMustBeIriOrAnyURI() {
        NodeValue point = wkt("POINT(1 2)");
        expectExpressionError(point, NodeValue.makeString(CRS84));
        expectExpressionError(point, NodeValue.makeInteger(1));
        expectExpressionError(point, NodeValue.makeNode(" ", XSDDatatype.XSDanyURI));
        expectExpressionError(point, NodeValue.makeNode("http://example.com/%ZZ", XSDDatatype.XSDanyURI));
    }

    @Test
    public void geometryMustBeALiteral() {
        expectExpressionError(iri(CRS84), iri(UTM34N));
        expectExpressionError(NodeValue.makeString("POINT(1 2)"), iri(UTM34N));
        expectExpressionError(wkt("not valid WKT"), iri(UTM34N));
    }

    @Test
    public void registeredFunctionReturnsGeometryLiteral() {
        Node result = evaluate("geof:transform('<" + WGS84 + "> POINT(41 24)'^^geo:wktLiteral, <" + CRS84 + ">)");
        assertNotNull(result);
        assertEquals(WKTDatatype.URI, result.getLiteralDatatypeURI());
        assertEquals("POINT(24 41)", result.getLiteralLexicalForm());
    }

    @Test
    public void registeredFunctionAcceptsAnyURITarget() {
        Node result = evaluate("geof:transform('<" + WGS84 + "> POINT(41 24)'^^geo:wktLiteral, '"
                + CRS84 + "'^^xsd:anyURI)");
        assertNotNull(result);
        assertEquals("POINT(24 41)", result.getLiteralLexicalForm());
    }

    @Test
    public void expressionErrorLeavesBindUnbound() {
        assertNull(evaluate("geof:transform('POINT(1 2)'^^geo:wktLiteral, <" + UNKNOWN + ">)"));
        assertNull(evaluate("geof:transform('POINT(1 2)'^^geo:wktLiteral, '" + CRS84 + "')"));
        assertNull(evaluate("geof:transform('invalid'^^geo:wktLiteral, <" + CRS84 + ">)"));
        assertNull(evaluate("geof:transform('POINT(1 2)'^^geo:wktLiteral, 'http://example.com/%ZZ'^^xsd:anyURI)"));
    }

    @Test
    public void wrongArityIsRejectedAtQueryBuild() {
        assertThrows(QueryBuildException.class, () -> evaluate("geof:transform('POINT(1 2)'^^geo:wktLiteral)"));
        assertThrows(QueryBuildException.class,
                     () -> evaluate("geof:transform('POINT(1 2)'^^geo:wktLiteral, <" + CRS84 + ">, 1)"));
    }

    private void expectExpressionError(NodeValue geometry, NodeValue target) {
        ExprEvalException error = assertThrows(ExprEvalException.class, () -> function.exec(geometry, target));
        assertFalse(error.getMessage().isEmpty());
    }

    private void expectProjectionExpressionError(String point) {
        ExprEvalException error = assertThrows(ExprEvalException.class,
                () -> function.exec(wkt(point), iri(WEB_MERCATOR)));
        assertTrue(error.getCause() instanceof TransformException);
    }

    private void assertNormalizedAnyUriTarget(String lexicalForm) {
        NodeValue target = NodeValue.makeNode(lexicalForm, XSDDatatype.XSDanyURI);
        assertEquals(CRS84, target.asNode().getLiteralValue().toString());
        assertEquals(lexicalForm, target.asNode().getLiteralLexicalForm());
        NodeValue source = wkt("<" + WGS84 + "> POINT(41 24)");
        assertEquals(function.exec(source, NodeValue.makeNode(CRS84, XSDDatatype.XSDanyURI)).asNode(),
                function.exec(source, target).asNode());
    }

    private void assertEmptyGmlTransform(String sourceURI, String targetURI, int dimension) {
        String gml = "<gml:Point xmlns:gml=\"http://www.opengis.net/gml/3.2\" srsName=\"" + sourceURI + "\"/>";
        NodeValue source = NodeValue.makeNode(gml, GMLDatatype.INSTANCE);
        assertEmptyGmlResult(function.exec(source, iri(targetURI)).asNode(), targetURI, dimension);
        assertEmptyGmlResult(evaluate("geof:transform('" + gml + "'^^geo:gmlLiteral, <" + targetURI + ">)"),
                targetURI, dimension);
    }

    private static void assertEmptyGmlResult(Node result, String targetURI, int dimension) {
        assertNotNull(result);
        assertEquals(GMLDatatype.URI, result.getLiteralDatatypeURI());
        assertFalse(result.getLiteralLexicalForm().contains("<gml:pos"));
        GeometryWrapper parsed = GMLDatatype.INSTANCE.read(result.getLiteralLexicalForm());
        assertEquals(targetURI, parsed.getSrsURI());
        assertEquals("Point", parsed.getGeometryType());
        assertTrue(parsed.isEmpty());
        assertEquals(dimension, parsed.getCoordinateDimension());
    }

    private static String gmlLine(String srsURI, String positions) {
        return "<gml:LineString xmlns:gml=\"http://www.opengis.net/gml/3.2\" srsName=\"" + srsURI
                + "\"><gml:posList>" + positions + "</gml:posList></gml:LineString>";
    }

    private static NodeValue wkt(String lexicalForm) {
        return NodeValue.makeNode(lexicalForm, WKTDatatype.INSTANCE);
    }

    private static NodeValue iri(String uri) {
        return NodeValue.makeNode(NodeFactory.createURI(uri));
    }

    private static Node evaluate(String expression) {
        String query = """
            PREFIX geof: <http://www.opengis.net/def/function/geosparql/>
            PREFIX geo: <http://www.opengis.net/ont/geosparql#>
            PREFIX xsd: <http://www.w3.org/2001/XMLSchema#>
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
