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

import static org.apache.jena.geosparql.geof.topological.filter_functions.geometry_property.GeometryPropertyFFTestSupport.GML_3D_POINT;
import static org.apache.jena.geosparql.geof.topological.filter_functions.geometry_property.GeometryPropertyFFTestSupport.evaluate;
import static org.apache.jena.geosparql.geof.topological.filter_functions.geometry_property.GeometryPropertyFFTestSupport.wktArgument;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;

import org.apache.jena.geosparql.configuration.GeoSPARQLConfig;
import org.apache.jena.geosparql.implementation.datatype.WKTDatatype;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.sparql.expr.ExprEvalException;
import org.apache.jena.sparql.expr.NodeValue;
import org.junit.BeforeClass;
import org.junit.Test;

public class MinZFFTest {
    private final MinZFF function = new MinZFF();

    @BeforeClass
    public static void setup() {
        GeoSPARQLConfig.setupNoIndex();
    }

    @Test
    public void directExecutionAcceptsExplicitWktCrs() {
        NodeValue geometry = NodeValue.makeNode(
            "<http://www.opengis.net/def/crs/EPSG/0/27700> LINESTRING ZM (-9 8 -7 100, -2 3 -1 -100)",
            WKTDatatype.INSTANCE);
        assertEquals(NodeValue.makeDouble(-7), function.exec(geometry));
    }

    @Test
    public void returnsDoubleLiteralThroughSparql() {
        String wkt = "LINESTRING ZM (-9 8 -7 100, -2 3 -1 -100)";
        assertEquals(NodeValue.makeDouble(-7).asNode(),
                     evaluate("geof:minZ(" + wktArgument(wkt) + ")"));
    }

    @Test
    public void followsSrsDimensionOrder() {
        String wkt = "<http://www.opengis.net/def/crs/EPSG/0/4979> LINESTRING Z (10 100 -4, 20 120 9)";
        assertEquals(NodeValue.makeDouble(-4).asNode(),
                     evaluate("geof:minZ(" + wktArgument(wkt) + ")"));
        assertThrows(ExprEvalException.class, () -> function.exec(NodeValue.makeNode(
            "<http://www.opengis.net/def/crs/EPSG/0/4326> LINESTRING (10 100, 20 120)", WKTDatatype.INSTANCE)));
    }

    @Test
    public void infiniteOrdinateRaisesExpressionError() {
        for (String infinity : new String[] { "Infinity", "-Infinity" }) {
            String wkt = "LINESTRING Z (-4 -4 -4, " + "1 1 " + infinity + ", 9 9 9)";
            assertThrows(wkt, ExprEvalException.class,
                         () -> function.exec(NodeValue.makeNode(wkt, WKTDatatype.INSTANCE)));
        }
    }

    @Test
    public void emptyGeometryRaisesExpressionError() {
        assertThrows(ExprEvalException.class,
                     () -> function.exec(NodeValue.makeNode("POINT Z EMPTY", WKTDatatype.INSTANCE)));
    }

    @Test
    public void layoutsWithoutZHaveTheExpectedResult() {
        for (String wkt : new String[] { "POINT (1 2)", "POINT M (1 2 99)" }) {
            assertThrows(wkt, ExprEvalException.class,
                         () -> function.exec(NodeValue.makeNode(wkt, WKTDatatype.INSTANCE)));
        }
    }

    @Test
    public void multiPointExtremaDistinguishZFromMeasures() {
        assertThrows(ExprEvalException.class,
                     () -> function.exec(NodeValue.makeNode("MULTIPOINT M ((1 2 10), (3 4 20))", WKTDatatype.INSTANCE)));
        String wkt = "MULTIPOINT ZM ((1 2 -4 10), (3 4 9 20))";
        NodeValue expected = NodeValue.makeDouble(-4);
        assertEquals(expected, function.exec(NodeValue.makeNode(wkt, WKTDatatype.INSTANCE)));
        assertEquals(expected.asNode(), evaluate("geof:minZ(" + wktArgument(wkt) + ")"));
    }

    @Test
    public void mixedCollectionExtremaDoNotDependOnMemberOrder() {
        for (String wkt : new String[] {
                "GEOMETRYCOLLECTION (POINT Z (1 2 3))",
                "GEOMETRYCOLLECTION (POINT (1 2), POINT M (1 2 999), POINT Z (1 2 3))",
                "GEOMETRYCOLLECTION (POINT Z (1 2 3), POINT M (1 2 999), POINT (1 2))" }) {
            NodeValue expected = NodeValue.makeDouble(3);
            assertEquals(wkt, expected, function.exec(NodeValue.makeNode(wkt, WKTDatatype.INSTANCE)));
            assertEquals(wkt, expected.asNode(), evaluate("geof:minZ(" + wktArgument(wkt) + ")"));
        }
    }

    @Test
    public void gmlExtremaFollowSrsDimensionOrder() {
        assertEquals(NodeValue.makeDouble(3).asNode(),
                     evaluate("geof:minZ(" + GML_3D_POINT + ")"));
    }

    @Test
    public void invalidArgumentsRaiseExpressionErrors() {
        for (NodeValue value : new NodeValue[] {
                NodeValue.makeNode(NodeFactory.createURI("urn:not-a-literal")),
                NodeValue.makeInteger(42),
                NodeValue.makeString("POINT (1 2)"),
                NodeValue.makeNode("invalid", WKTDatatype.INSTANCE) }) {
            assertThrows(ExprEvalException.class, () -> function.exec(value));
        }
    }
}
