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
    public void gmlPointUsesMetadataFunctions() {
        String point = """
            '<gml:Point xmlns:gml="http://www.opengis.net/gml/3.2"
                        srsName="http://www.opengis.net/def/crs/OGC/1.3/CRS84">
               <gml:pos>1 2</gml:pos>
             </gml:Point>'^^geo:gmlLiteral
            """.replace("\n", " ");
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
