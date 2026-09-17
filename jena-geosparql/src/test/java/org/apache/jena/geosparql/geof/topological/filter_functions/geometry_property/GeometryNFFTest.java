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
import org.apache.jena.sparql.expr.ExprEvalTypeException;
import org.apache.jena.sparql.expr.NodeValue;
import org.junit.BeforeClass;
import org.junit.Test;

public class GeometryNFFTest {
    @BeforeClass
    public static void setup() {
        GeoSPARQLConfig.setupNoIndex();
    }

    @Test
    public void selectsFirstAndLastMembersUsingOneBasedIndices() {
        String wkt = "MULTIPOINT ((1 2), (3 4), (5 6))";
        assertGeometry("POINT (1 2)", select(wkt, "1"));
        assertGeometry("POINT (5 6)", select(wkt, "3"));
    }

    @Test
    public void acceptsIntegerAndIntegerDerivedIndexTypes() {
        String wkt = "MULTIPOINT ((1 2), (3 4))";
        assertGeometry("POINT (3 4)", select(wkt, "2"));
        assertGeometry("POINT (3 4)", select(wkt, "'2'^^xsd:short"));
    }

    @Test
    public void nonIntegerNumericIndexesRaiseExpressionErrorsAndLeaveBindUnbound() {
        String wkt = "MULTIPOINT ((1 2), (3 4))";
        for (String index : new String[] { "2.0", "2e0", "'2'^^xsd:float" }) {
            assertNull(index, select(wkt, index));
        }
        GeometryNFF function = new GeometryNFF();
        NodeValue geometry = NodeValue.makeNode(wkt, WKTDatatype.INSTANCE);
        for (NodeValue index : new NodeValue[] {
                NodeValue.makeDecimal("2.0"), NodeValue.makeDouble(2e0), NodeValue.makeFloat(2f) }) {
            assertThrows(index.toString(), ExprEvalTypeException.class, () -> function.exec(geometry, index));
        }
    }

    @Test
    public void atomicIndexOnePreservesOriginalLiteral() {
        for (String wkt : new String[] { "POINT (1 2)", "LINESTRING (0 0, 1 1)",
                "POLYGON ((0 0, 1 0, 0 1, 0 0))", "POINT Z EMPTY", "POINT M EMPTY", "POINT ZM EMPTY" }) {
            assertEquals(wkt, NodeValue.makeNode(wkt, WKTDatatype.INSTANCE).asNode(), select(wkt, "1"));
        }
    }

    @Test
    public void selectedMemberHasItsOwnTopologicalDimension() {
        assertGeometry("LINESTRING (1 2, 3 4)", select(
            "GEOMETRYCOLLECTION (POINT (9 9), LINESTRING (1 2, 3 4))", "2"));
    }

    @Test
    public void selectsMembersOfMultiLinesAndMultiPolygons() {
        assertGeometry("LINESTRING (3 4, 5 6)", select("MULTILINESTRING ((0 0, 1 1), (3 4, 5 6))", "2"));
        assertGeometry("POLYGON ((0 0, 1 0, 0 1, 0 0))", select("MULTIPOLYGON (((0 0, 1 0, 0 1, 0 0)))", "1"));
    }

    @Test
    public void selectedMembersRetainZAndM() {
        for (String marker : new String[] { "Z", "M", "ZM" }) {
            String coordinates = marker.equals("ZM") ? "1 2 3 4" : "1 2 3";
            assertGeometry("POINT " + marker + " (" + coordinates + ")",
                select("GEOMETRYCOLLECTION (POINT " + marker + " (" + coordinates + "))", "1"));
        }
    }

    @Test
    public void selectedMultiPointMemberRetainsMeasureWithoutInventingZ() {
        assertGeometry("POINT M (3 4 9)", select("MULTIPOINT M ((1 2 7), (3 4 9))", "2"));
    }

    @Test
    public void selectedMultiPointMemberRetainsZAndMeasure() {
        assertGeometry("POINT ZM (3 4 9 11)", select("MULTIPOINT ZM ((1 2 7 8), (3 4 9 11))", "2"));
    }

    @Test
    public void emptySelectedMembersRetainCoordinateMarkers() {
        for (String marker : new String[] { "Z", "M", "ZM" }) {
            for (String type : new String[] { "POINT", "LINESTRING", "POLYGON", "MULTIPOINT", "MULTILINESTRING", "MULTIPOLYGON", "GEOMETRYCOLLECTION" }) {
                String member = type + " " + marker + " EMPTY";
                assertGeometry(member, select("GEOMETRYCOLLECTION (" + member + ", POINT (1 2))", "1"));
            }
        }
    }

    @Test
    public void authorityAxisOrderAndZSurviveSerialization() {
        String crs = "<http://www.opengis.net/def/crs/EPSG/0/4979> ";
        assertGeometry(crs + "POINT Z (10 100 7)", select(crs + "MULTIPOINT Z ((10 100 7), (20 120 9))", "1"));
    }

    @Test
    public void gmlResultRetainsSourceDatatypeAndCrs() {
        String gml = """
            '<gml:MultiPoint xmlns:gml="http://www.opengis.net/gml/3.2"
                srsName="http://www.opengis.net/def/crs/EPSG/0/4979">
              <gml:pointMember><gml:Point><gml:pos srsDimension="3">10 100 7</gml:pos></gml:Point></gml:pointMember>
            </gml:MultiPoint>'^^geo:gmlLiteral
            """.replace("\n", " ");
        Node result = evaluate("geof:geometryN(" + gml + ", 1)");
        assertGeometry("<http://www.opengis.net/def/crs/EPSG/0/4979> POINT Z (10 100 7)", result, GMLDatatype.URI);
    }

    @Test
    public void selectedEmptyGmlCollectionRetainsItsMember() {
        String gml = """
            '<gml:MultiGeometry xmlns:gml="http://www.opengis.net/gml/3.2"
                srsName="http://www.opengis.net/def/crs/EPSG/0/4979">
              <gml:geometryMember><gml:MultiGeometry>
                <gml:geometryMember><gml:Point/></gml:geometryMember>
              </gml:MultiGeometry></gml:geometryMember>
            </gml:MultiGeometry>'^^geo:gmlLiteral
            """.replace("\n", " ");
        Node result = evaluate("geof:geometryN(" + gml + ", 1)");
        assertNotNull(result);
        GeometryWrapper selected = GeometryWrapper.extract(result);
        assertEquals(1, selected.getParsingGeometry().getNumGeometries());
        assertEquals("Point", selected.getGeometryN(1).getGeometryType());
        assertEquals(3, selected.getGeometryN(1).getCoordinateDimension());
    }

    @Test
    public void selectedEmptyGmlMultiGeometriesRetainAllMembers() {
        String[][] types = {
            { "MultiPoint", "pointMember", "Point", "MultiPoint" },
            { "MultiCurve", "curveMember", "LineString", "MultiLineString" },
            { "MultiSurface", "surfaceMember", "Polygon", "MultiPolygon" }
        };
        for (String[] type : types) {
            String member = "<gml:" + type[1] + "><gml:" + type[2] + "/></gml:" + type[1] + ">";
            String gml = """
                '<gml:MultiGeometry xmlns:gml="http://www.opengis.net/gml/3.2"
                    srsName="http://www.opengis.net/def/crs/EPSG/0/4979">
                  <gml:geometryMember><gml:%s>%s%s</gml:%s></gml:geometryMember>
                </gml:MultiGeometry>'^^geo:gmlLiteral
                """.formatted(type[0], member, member, type[0]).replace("\n", " ");
            GeometryWrapper source = GeometryWrapper.extract(gml.substring(1, gml.indexOf("'^^")), GMLDatatype.URI);
            GeometryWrapper expected = source.getGeometryN(1);
            assertEquals(type[0], 2, expected.getParsingGeometry().getNumGeometries());
            Node result = evaluate("geof:geometryN(" + gml + ", 1)");
            assertNotNull(type[0], result);
            assertEquals("http://www.opengis.net/ont/geosparql#gmlLiteral", result.getLiteralDatatypeURI());
            GeometryWrapper selected = GeometryWrapper.extract(result);
            assertEquals(type[3], selected.getGeometryType());
            assertEquals("http://www.opengis.net/def/crs/EPSG/0/4979", selected.getSrsURI());
            assertEquals(type[0], 2, selected.getParsingGeometry().getNumGeometries());
            for (int index = 1; index <= 2; index++) {
                GeometryWrapper child = selected.getGeometryN(index);
                assertEquals(type[2], child.getGeometryType());
                assertTrue(child.isEmpty());
                assertEquals(type[0], CoordinateSequenceDimensions.XYZ, child.getCoordinateSequenceDimensions());
            }
        }
    }

    @Test
    public void invalidIndicesRaiseExpressionErrorsAndLeaveBindUnbound() {
        for (String index : new String[] { "0", "-1", "3", "1.5", "1.00000000000000000001",
                "2147483648", "999999999999999999999999999999", "'NaN'^^xsd:double",
                "'INF'^^xsd:double", "'-INF'^^xsd:float", "'1'", "true", "<urn:index>" }) {
            assertNull(index, select("MULTIPOINT ((1 2), (3 4))", index));
        }
        GeometryNFF function = new GeometryNFF();
        NodeValue geometry = NodeValue.makeNode("POINT (1 2)", WKTDatatype.INSTANCE);
        for (NodeValue index : new NodeValue[] { NodeValue.makeInteger(0), NodeValue.makeInteger(2) }) {
            assertThrows(ExprEvalException.class, () -> function.exec(geometry, index));
        }
        for (NodeValue index : new NodeValue[] { NodeValue.makeDecimal("1.5"), NodeValue.makeDouble(Double.NaN),
                NodeValue.makeString("1") }) {
            assertThrows(ExprEvalTypeException.class, () -> function.exec(geometry, index));
        }
    }

    @Test
    public void emptyCollectionsHaveNoMemberAtIndexOne() {
        for (String wkt : new String[] { "MULTIPOINT EMPTY", "MULTILINESTRING EMPTY", "MULTIPOLYGON EMPTY", "GEOMETRYCOLLECTION EMPTY" }) {
            assertNull(wkt, select(wkt, "1"));
        }
    }

    @Test
    public void invalidGeometriesRaiseExpressionErrors() {
        GeometryNFF function = new GeometryNFF();
        for (NodeValue value : new NodeValue[] { NodeValue.makeString("POINT (1 2)"), NodeValue.makeInteger(1),
                NodeValue.makeNode(NodeFactory.createURI("urn:geometry")), NodeValue.makeNode("invalid", WKTDatatype.INSTANCE) }) {
            assertThrows(ExprEvalException.class, () -> function.exec(value, NodeValue.makeInteger(1)));
        }
        for (String value : new String[] { "'POINT (1 2)'", "42", "<urn:geometry>", "'invalid'^^geo:wktLiteral", "?missing" }) {
            assertNull(evaluate("geof:geometryN(" + value + ", 1)"));
        }
        assertNull(select("POINT (1 2)", "?missing"));
    }

    @Test
    public void wrongArityIsRejectedAtQueryBuild() {
        for (String arguments : new String[] { "", "'POINT EMPTY'^^geo:wktLiteral", "'POINT EMPTY'^^geo:wktLiteral, 1, 2" }) {
            assertThrows(QueryBuildException.class, () -> evaluate("geof:geometryN(" + arguments + ")"));
        }
    }

    private static Node select(String wkt, String index) {
        return evaluate("geof:geometryN('" + wkt + "'^^geo:wktLiteral, " + index + ")");
    }

    private static void assertGeometry(String expectedWkt, Node result) {
        assertGeometry(expectedWkt, result, WKTDatatype.URI);
    }

    private static void assertGeometry(String expectedWkt, Node result, String datatypeURI) {
        assertNotNull(expectedWkt, result);
        assertEquals(expectedWkt, datatypeURI, result.getLiteralDatatypeURI());
        GeometryWrapper expected = GeometryWrapper.extract(expectedWkt, WKTDatatype.URI);
        GeometryWrapper actual = GeometryWrapper.extract(result);
        assertEquals(expected.getSrsURI(), actual.getSrsURI());
        assertEquals(expected.getCoordinateSequenceDimensions(), actual.getCoordinateSequenceDimensions());
        assertEquals(expected.getTopologicalDimension(), actual.getTopologicalDimension());
        assertEquals(expected.getParsingGeometry().toText(), actual.getParsingGeometry().toText());
        // Serialize both through WKT to compare Z/M ordinates across input datatypes.
        assertEquals(org.apache.jena.geosparql.implementation.parsers.wkt.WKTWriter.write(expected),
                     org.apache.jena.geosparql.implementation.parsers.wkt.WKTWriter.write(actual));
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
