/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.jena.geosparql.spatial.filter_functions;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.apache.jena.geosparql.configuration.GeoSPARQLConfig;
import org.apache.jena.geosparql.implementation.datatype.WKTDatatype;
import org.apache.jena.sparql.expr.NodeValue;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * Test for POINT ZM coordinate transformation bug fix
 */
public class TransformSRSFFPointZMTest {

    public TransformSRSFFPointZMTest() {
        GeoSPARQLConfig.setupNoIndex();
    }

    @BeforeClass
    public static void setUpClass() {
    }

    @AfterClass
    public static void tearDownClass() {
    }

    @Before
    public void setUp() {
    }

    @After
    public void tearDown() {
    }

    /**
     * Test of exec method with POINT ZM to ensure M coordinate is preserved during transformation.
     * This test addresses the bug where POINT ZM coordinates lose their M value during SRS transformation.
     */
    @Test
    public void testTransformSRS_PointZM_PreservesMCoordinate() {

        // Test case from the bug report: EPSG:25832 to EPSG:4326 transformation
        NodeValue v1 = NodeValue.makeNode("<http://www.opengis.net/def/crs/EPSG/0/25832> POINT ZM(628319.4 6654189.1 -99999 -1)", WKTDatatype.INSTANCE);
        NodeValue v2 = NodeValue.makeString("http://www.opengis.net/def/crs/EPSG/0/4326");
        TransformSRSFF instance = new TransformSRSFF();

        NodeValue result = instance.exec(v1, v2);

        // The result should maintain the M coordinate (-1) from the original point
        // The exact X,Y,Z coordinates will be transformed, but M should be preserved
        String resultString = result.asNode().getLiteralLexicalForm();

        // Check that the result contains ZM and has 4 coordinates
        assertTrue("Result should contain 'POINT ZM' indicating 4D coordinates", resultString.contains("POINT ZM"));

        // Extract coordinates from the result string
        // Format should be: "<srs> POINT ZM(x y z m)"
        int startParen = resultString.indexOf('(');
        int endParen = resultString.indexOf(')', startParen);
        String coordsString = resultString.substring(startParen + 1, endParen);
        String[] coords = coordsString.trim().split("\\s+");

        // Should have exactly 4 coordinates for POINT ZM
        assertEquals("POINT ZM should have exactly 4 coordinates", 4, coords.length);

        // The M coordinate should be preserved as -1
        assertEquals("M coordinate should be preserved", "-1", coords[3]);

        // Z coordinate should also be preserved as -99999
        assertEquals("Z coordinate should be preserved", "-99999", coords[2]);
    }
}
