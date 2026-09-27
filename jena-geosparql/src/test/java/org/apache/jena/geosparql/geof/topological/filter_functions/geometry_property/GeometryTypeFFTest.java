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

import static org.junit.Assert.*;

import org.apache.jena.datatypes.xsd.XSDDatatype;
import org.apache.jena.geosparql.configuration.GeoSPARQLConfig;
import org.apache.jena.geosparql.implementation.datatype.WKTDatatype;
import org.apache.jena.geosparql.implementation.index.GeometryLiteralIndex;
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

public class GeometryTypeFFTest {
    private static final String GML = "http://www.opengis.net/ont/gml#";
    private static final String SF = "http://www.opengis.net/ont/sf#";

    @BeforeClass
    public static void setup() {
        GeoSPARQLConfig.setupNoIndex();
    }

    @Test
    public void wktReturnsTypedSubtypeUriIncludingEmptyGeometries() {
        assertType("'POINT (1 2)'^^geo:wktLiteral", SF + "Point");
        assertType("'LINESTRING (0 0, 1 1)'^^geo:wktLiteral", SF + "LineString");
        assertType("'LINEARRING EMPTY'^^geo:wktLiteral", SF + "LinearRing");
        assertType("'POLYGON ((0 0, 1 0, 0 1, 0 0))'^^geo:wktLiteral", SF + "Polygon");
        assertType("'MULTIPOINT ((1 2), (3 4))'^^geo:wktLiteral", SF + "MultiPoint");
        assertType("'MULTILINESTRING ((0 0, 1 1))'^^geo:wktLiteral", SF + "MultiLineString");
        assertType("'MULTIPOLYGON (((0 0, 1 0, 0 1, 0 0)))'^^geo:wktLiteral", SF + "MultiPolygon");
        assertType("'GEOMETRYCOLLECTION EMPTY'^^geo:wktLiteral", SF + "GeometryCollection");
    }

    @Test
    public void memoryIndexKeepsWktAndGmlTypesSeparate() {
        GeoSPARQLConfig.setupMemoryIndex();
        try {
            assertType("''^^geo:wktLiteral", SF + "Point");
            assertType("''^^geo:gmlLiteral", GML + "Point");

            GeometryLiteralIndex.clear();
            assertType("''^^geo:gmlLiteral", GML + "Point");
            assertType("''^^geo:wktLiteral", SF + "Point");
        } finally {
            GeoSPARQLConfig.setupNoIndex();
        }
    }

    @Test
    public void gmlRetainsSourceSubtype() {
        assertGmlType("Curve", "<g:segments><g:Arc><g:posList>5 0 0 5 -5 0</g:posList>"
                + "</g:Arc></g:segments>");
        assertGmlType("Surface", "<g:patches><g:PolygonPatch>" + polygonRing()
                + "</g:PolygonPatch></g:patches>");
        assertGmlType("MultiCurve", "<g:curveMember><g:LineString><g:posList>0 0 1 1</g:posList>"
                + "</g:LineString></g:curveMember>");
        assertGmlType("MultiSurface", "<g:surfaceMember><g:Polygon>" + polygonRing()
                + "</g:Polygon></g:surfaceMember>");
    }

    @Test
    public void acceptedEmptyGmlElementsRetainTheirTypes() {
        for (String type : new String[] {
                "Point", "LineString", "Curve", "MultiCurve", "MultiSurface", "MultiGeometry" }) {
            assertGmlType(type, "");
        }
    }

    @Test
    public void derivedGeometryUsesItsOwnType() {
        String curve = gml("Curve", "<g:segments><g:LineStringSegment>"
                + "<g:posList>0 0 10 10 20 0</g:posList></g:LineStringSegment></g:segments>");
        assertEquals(NodeFactory.createLiteralDT(GML + "Polygon", XSDDatatype.XSDanyURI),
                     evaluate("geof:geometryType(geof:envelope('" + curve + "'^^geo:gmlLiteral))"));
    }

    @Test
    public void invalidArgumentsRaiseExpressionErrorsAndLeaveBindUnbound() {
        GeometryTypeFF function = new GeometryTypeFF();
        Node nonLiteral = NodeFactory.createURI("urn:not-a-literal");
        assertThrows(ExprEvalException.class,
                () -> function.exec(NodeValue.makeNode(nonLiteral)));
        assertThrows(ExprEvalException.class, () -> function.exec(NodeValue.makeInteger(42)));
        assertThrows(ExprEvalException.class,
                () -> function.exec(NodeValue.makeString("POINT (1 2)")));
        assertThrows(ExprEvalException.class,
                () -> function.exec(NodeValue.makeNode("invalid", WKTDatatype.INSTANCE)));
        for (String argument : new String[] {
                "<urn:not-a-literal>", "42", "'POINT (1 2)'",
                "'invalid'^^geo:wktLiteral" }) {
            assertNull(argument, evaluate("geof:geometryType(" + argument + ")"));
        }
    }

    @Test
    public void wrongArityIsRejected() {
        assertThrows(QueryBuildException.class, () -> evaluate("geof:geometryType()"));
        assertThrows(QueryBuildException.class,
                     () -> evaluate("geof:geometryType('POINT EMPTY'^^geo:wktLiteral, 1)"));
    }

    private static void assertGmlType(String type, String contents) {
        assertType("'" + gml(type, contents) + "'^^geo:gmlLiteral", GML + type);
    }

    private static void assertType(String geometry, String expectedUri) {
        assertEquals(geometry, NodeFactory.createLiteralDT(expectedUri, XSDDatatype.XSDanyURI),
                     evaluate("geof:geometryType(" + geometry + ")"));
    }

    private static String polygonRing() {
        return "<g:exterior><g:LinearRing><g:posList>0 0 10 0 10 10 0 0</g:posList>"
                + "</g:LinearRing></g:exterior>";
    }

    private static String gml(String type, String contents) {
        return "<g:" + type + " xmlns:g=\"http://www.opengis.net/gml/3.2\" "
               + "srsName=\"http://www.opengis.net/def/crs/EPSG/0/27700\">"
               + contents + "</g:" + type + ">";
    }

    private static Node evaluate(String expression) {
        String query = """
            PREFIX geof: <http://www.opengis.net/def/function/geosparql/>
            PREFIX geo: <http://www.opengis.net/ont/geosparql#>
            SELECT ?result WHERE { BIND(%s AS ?result) }
            """.formatted(expression);
        try (QueryExecution execution = QueryExecution.create(
                query, ModelFactory.createDefaultModel())) {
            ResultSet results = execution.execSelect();
            assertTrue("Expected one solution for " + expression, results.hasNext());
            QuerySolution solution = results.next();
            assertFalse("Expected only one solution for " + expression, results.hasNext());
            return solution.contains("result") ? solution.get("result").asNode() : null;
        }
    }
}
