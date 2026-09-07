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
import static org.junit.Assert.assertTrue;

import org.apache.jena.datatypes.xsd.XSDDatatype;
import org.apache.jena.geosparql.configuration.GeoSPARQLConfig;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QuerySolution;
import org.apache.jena.query.ResultSet;
import org.apache.jena.rdf.model.ModelFactory;
import org.junit.BeforeClass;
import org.junit.Test;

public class GeometryMetadataFFTest {
    private static final String PREFIXES = """
        PREFIX geof: <http://www.opengis.net/def/function/geosparql/>
        PREFIX geo: <http://www.opengis.net/ont/geosparql#>
        """;

    @BeforeClass
    public static void setup() {
        GeoSPARQLConfig.setupNoIndex();
    }

    @Test
    public void geometryTypeReturnsAnyUriLiteral() {
        assertResult("geof:geometryType('POINT (1 2)'^^geo:wktLiteral)",
                     "http://www.opengis.net/ont/sf#Point", XSDDatatype.XSDanyURI);
    }

    @Test
    public void geometryTypeRecognizesLinearRingWktExtension() {
        assertResult("geof:geometryType('LINEARRING (0 0, 1 0, 1 1, 0 0)'^^geo:wktLiteral)",
                     "http://www.opengis.net/ont/sf#LinearRing", XSDDatatype.XSDanyURI);
        assertResult("geof:geometryType('LINEARRING EMPTY'^^geo:wktLiteral)",
                     "http://www.opengis.net/ont/sf#LinearRing", XSDDatatype.XSDanyURI);
    }

    @Test
    public void geometryTypeDoesNotInferTriangleFromPolygonCoordinates() {
        assertResult("geof:geometryType('POLYGON ((0 0, 1 0, 0 1, 0 0))'^^geo:wktLiteral)",
                     "http://www.opengis.net/ont/sf#Polygon", XSDDatatype.XSDanyURI);
    }

    @Test
    public void geometryTypeRetainsTheTypeOfEmptyGeometries() {
        assertResult("geof:geometryType('POINT EMPTY'^^geo:wktLiteral)",
                     "http://www.opengis.net/ont/sf#Point", XSDDatatype.XSDanyURI);
        assertResult("geof:geometryType('POLYGON EMPTY'^^geo:wktLiteral)",
                     "http://www.opengis.net/ont/sf#Polygon", XSDDatatype.XSDanyURI);
        assertResult("geof:geometryType('GEOMETRYCOLLECTION EMPTY'^^geo:wktLiteral)",
                     "http://www.opengis.net/ont/sf#GeometryCollection", XSDDatatype.XSDanyURI);
    }

    @Test
    public void is3DDistinguishesZFromM() {
        assertResult("geof:is3D('POINT Z (1 2 3)'^^geo:wktLiteral)", "true", XSDDatatype.XSDboolean);
        assertResult("geof:is3D('POINT M (1 2 3)'^^geo:wktLiteral)", "false", XSDDatatype.XSDboolean);
    }

    @Test
    public void isMeasuredDistinguishesMFromZ() {
        assertResult("geof:isMeasured('POINT M (1 2 3)'^^geo:wktLiteral)", "true", XSDDatatype.XSDboolean);
        assertResult("geof:isMeasured('POINT Z (1 2 3)'^^geo:wktLiteral)", "false", XSDDatatype.XSDboolean);
    }

    @Test
    public void emptyPointsRetainZAndMFlags() {
        assertFlags("POINT EMPTY", false, false);
        assertFlags("POINT Z EMPTY", true, false);
        assertFlags("POINT M EMPTY", false, true);
        assertFlags("POINT ZM EMPTY", true, true);
    }

    @Test
    public void emptyCollectionsRetainDeclaredZAndMFlags() {
        assertFlags("GEOMETRYCOLLECTION EMPTY", false, false);
        assertFlags("GEOMETRYCOLLECTION Z EMPTY", true, false);
        assertFlags("GEOMETRYCOLLECTION M EMPTY", false, true);
        assertFlags("GEOMETRYCOLLECTION ZM EMPTY", true, true);
    }

    @Test
    public void permissiveWktCollectionLayoutDoesNotAggregateMemberLayouts() {
        // Mixed member layouts are a Jena WKT extension, not an SFA conformance case.
        assertFlags("GEOMETRYCOLLECTION (POINT Z (1 2 3), POINT M (4 5 6))", false, false);
    }

    @Test
    public void numGeometriesCountsMembersAsAnInteger() {
        assertResult("geof:numGeometries('MULTIPOINT ((1 2), (3 4))'^^geo:wktLiteral)",
                     "2", XSDDatatype.XSDinteger);
    }

    @Test
    public void numGeometriesDistinguishesEmptyAtomsFromCollections() {
        assertResult("geof:numGeometries('POINT EMPTY'^^geo:wktLiteral)", "1", XSDDatatype.XSDinteger);
        assertResult("geof:numGeometries('GEOMETRYCOLLECTION EMPTY'^^geo:wktLiteral)", "0", XSDDatatype.XSDinteger);
        assertResult("geof:numGeometries('MULTIPOINT EMPTY'^^geo:wktLiteral)", "0", XSDDatatype.XSDinteger);
        assertResult("geof:numGeometries('GEOMETRYCOLLECTION (POINT EMPTY)'^^geo:wktLiteral)",
                     "1", XSDDatatype.XSDinteger);
    }

    @Test
    public void gmlPointUsesTheSameMetadataFunctions() {
        String point = """
            '<gml:Point xmlns:gml="http://www.opengis.net/gml/3.2"
                        srsName="http://www.opengis.net/def/crs/OGC/1.3/CRS84">
               <gml:pos>1 2</gml:pos>
             </gml:Point>'^^geo:gmlLiteral
            """.replace("\n", " ");
        assertResult("geof:geometryType(" + point + ")", "http://www.opengis.net/ont/gml#Point", XSDDatatype.XSDanyURI);
        assertResult("geof:numGeometries(" + point + ")", "1", XSDDatatype.XSDinteger);
        assertResult("geof:is3D(" + point + ")", "false", XSDDatatype.XSDboolean);
        assertResult("geof:isMeasured(" + point + ")", "false", XSDDatatype.XSDboolean);
        assertResult("geof:isEmpty(" + point + ")", "false", XSDDatatype.XSDboolean);
    }

    @Test
    public void threeDimensionalGmlPointHasZButNoMeasure() {
        String point = """
            '<gml:Point xmlns:gml="http://www.opengis.net/gml/3.2"
                        srsName="http://www.opengis.net/def/crs/EPSG/0/4979">
               <gml:pos srsDimension="3">2 1 3</gml:pos>
             </gml:Point>'^^geo:gmlLiteral
            """.replace("\n", " ");
        assertResult("geof:is3D(" + point + ")", "true", XSDDatatype.XSDboolean);
        assertResult("geof:isMeasured(" + point + ")", "false", XSDDatatype.XSDboolean);
        assertResult("geof:isEmpty(" + point + ")", "false", XSDDatatype.XSDboolean);
    }

    @Test
    public void metadataFunctionsComposeWithGeometryFunctions() {
        assertResult("geof:geometryType(geof:envelope('LINESTRING (0 0, 1 1)'^^geo:wktLiteral))",
                     "http://www.opengis.net/ont/sf#Polygon", XSDDatatype.XSDanyURI);
    }

    @Test
    public void gmlSubtypesSurviveJtsApproximation() {
        assertGmlType("Curve", "<g:segments><g:Arc><g:posList>5 0 0 5 -5 0</g:posList></g:Arc></g:segments>");
        assertGmlType("Surface", "<g:patches><g:PolygonPatch>" + polygonRing() + "</g:PolygonPatch></g:patches>");
        assertGmlType("MultiCurve", "<g:curveMember><g:LineString><g:posList>0 0 1 1</g:posList></g:LineString></g:curveMember>");
        assertGmlType("MultiSurface", "<g:surfaceMember><g:Polygon>" + polygonRing() + "</g:Polygon></g:surfaceMember>");
    }

    @Test
    public void gmlEnvelopeReportsItsOwnType() {
        String curve = gml("Curve", "<g:segments><g:LineStringSegment><g:posList>0 0 10 10 20 0</g:posList></g:LineStringSegment></g:segments>");
        assertResult("geof:geometryType(geof:envelope('" + curve + "'^^geo:gmlLiteral))",
                     "http://www.opengis.net/ont/gml#Polygon", XSDDatatype.XSDanyURI);
    }

    @Test
    public void permissiveGmlEmptyElementsRetainTheirTypes() {
        // Exercises Jena's acceptance of empty elements, not GML-schema conformance.
        // For example, Curve without segments is accepted as an empty geometry.
        for (String type : new String[] { "Point", "LineString", "Curve", "MultiCurve", "MultiSurface", "MultiGeometry" }) {
            assertGmlType(type, "");
        }
    }

    private static String polygonRing() {
        return "<g:exterior><g:LinearRing><g:posList>0 0 10 0 10 10 0 0</g:posList></g:LinearRing></g:exterior>";
    }

    private static String gml(String type, String contents) {
        return "<g:" + type + " xmlns:g=\"http://www.opengis.net/gml/3.2\" "
               + "srsName=\"http://www.opengis.net/def/crs/EPSG/0/27700\">" + contents + "</g:" + type + ">";
    }

    private static void assertGmlType(String type, String contents) {
        assertResult("geof:geometryType('" + gml(type, contents) + "'^^geo:gmlLiteral)",
                     "http://www.opengis.net/ont/gml#" + type, XSDDatatype.XSDanyURI);
    }

    private static void assertFlags(String wkt, boolean hasZ, boolean hasM) {
        assertResult("geof:is3D('" + wkt + "'^^geo:wktLiteral)", Boolean.toString(hasZ), XSDDatatype.XSDboolean);
        assertResult("geof:isMeasured('" + wkt + "'^^geo:wktLiteral)", Boolean.toString(hasM), XSDDatatype.XSDboolean);
    }

    private static void assertResult(String expression, String lexicalForm, XSDDatatype datatype) {
        Node expected = NodeFactory.createLiteralDT(lexicalForm, datatype);
        assertEquals(expression, expected, evaluate(expression));
    }

    private static Node evaluate(String expression) {
        String query = PREFIXES + "SELECT ?result WHERE { BIND(" + expression + " AS ?result) }";
        try (QueryExecution execution = QueryExecution.create(query, ModelFactory.createDefaultModel())) {
            ResultSet results = execution.execSelect();
            assertTrue("Expected one solution for " + expression, results.hasNext());
            QuerySolution solution = results.next();
            assertFalse("Expected only one solution for " + expression, results.hasNext());
            return solution.contains("result") ? solution.get("result").asNode() : null;
        }
    }
}
