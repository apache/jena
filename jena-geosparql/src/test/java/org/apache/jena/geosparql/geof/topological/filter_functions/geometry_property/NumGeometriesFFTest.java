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

import static org.apache.jena.geosparql.geof.topological.filter_functions.geometry_property.GeometryPropertyFFTestSupport.GML_2D_POINT;
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

public class NumGeometriesFFTest {
    private final NumGeometriesFF function = new NumGeometriesFF();

    @BeforeClass
    public static void setup() {
        GeoSPARQLConfig.setupNoIndex();
    }

    @Test
    public void directExecutionAcceptsExplicitWktCrs() {
        NodeValue geometry = NodeValue.makeNode(
            "<http://www.opengis.net/def/crs/EPSG/0/27700> POINT (1 2)", WKTDatatype.INSTANCE);
        assertEquals(NodeValue.makeInteger(1), function.exec(geometry));
    }

    @Test
    public void countsMembersAsAnInteger() {
        assertResult("MULTIPOINT ((1 2), (3 4))", 2);
    }

    @Test
    public void distinguishesAtomsFromCollections() {
        assertResult("POINT (1 2)", 1);
        assertResult("POINT EMPTY", 1);
        assertResult("GEOMETRYCOLLECTION EMPTY", 0);
        assertResult("MULTIPOINT EMPTY", 0);
        assertResult("GEOMETRYCOLLECTION (POINT EMPTY)", 1);
    }

    @Test
    public void gmlPointCountsAsOne() {
        assertEquals(NodeValue.makeInteger(1).asNode(), evaluate("geof:numGeometries(" + GML_2D_POINT + ")"));
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

    private static void assertResult(String wkt, int expected) {
        assertEquals(wkt, NodeValue.makeInteger(expected).asNode(),
                     evaluate("geof:numGeometries(" + wktArgument(wkt) + ")"));
    }
}
