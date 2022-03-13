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
package org.apache.jena.geosparql.geof.nontopological.filter_functions;

import org.apache.jena.geosparql.configuration.GeoSPARQLConfig;
import org.apache.jena.geosparql.implementation.datatype.WKTDatatype;
import org.apache.jena.geosparql.implementation.vocabulary.Unit_URI;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.sparql.expr.ExprEvalException;
import org.apache.jena.sparql.expr.NodeValue;
import org.junit.After;
import org.junit.AfterClass;
import static org.junit.Assert.assertEquals;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 *
 *
 */
public class DistanceFFTest {

    public DistanceFFTest() {
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
     * Test of exec method, of class DistanceFF.
     */
    @Test
    public void testExec_projected_metres() {

        NodeValue v1 = NodeValue.makeNode("<http://www.opengis.net/def/crs/EPSG/0/27700> POINT(60 60)", WKTDatatype.INSTANCE);
        NodeValue v2 = NodeValue.makeNode("<http://www.opengis.net/def/crs/EPSG/0/27700> POINT(90 60)", WKTDatatype.INSTANCE);
        NodeValue v3 = NodeValue.makeNode(NodeFactory.createURI(Unit_URI.METRE_URL));
        DistanceFF instance = new DistanceFF();
        double expResult = 30;
        double result = instance.exec(v1, v2, v3).getDouble();
        assertEquals(expResult, result, 0);
    }

    /**
     * Test of exec method, of class DistanceFF.
     */
    @Test(expected = ExprEvalException.class)
    public void testExec_projected_radians_exception() {

        NodeValue v1 = NodeValue.makeNode("<http://www.opengis.net/def/crs/EPSG/0/27700> POINT(60 60)", WKTDatatype.INSTANCE);
        NodeValue v2 = NodeValue.makeNode("<http://www.opengis.net/def/crs/EPSG/0/27700> POINT(90 60)", WKTDatatype.INSTANCE);
        NodeValue v3 = NodeValue.makeNode(NodeFactory.createURI(Unit_URI.RADIAN_URL));
        DistanceFF instance = new DistanceFF();
        double result = instance.exec(v1, v2, v3).getDouble();
    }

    /**
     * Test of exec method, of class DistanceFF.
     */
    @Test
    public void testExec_geographic_radians() {

        NodeValue v1 = NodeValue.makeNode("Point(11.41 53.63)", WKTDatatype.INSTANCE);
        NodeValue v2 = NodeValue.makeNode("Point(11.57 48.13)", WKTDatatype.INSTANCE);
        NodeValue v3 = NodeValue.makeNode(NodeFactory.createURI(Unit_URI.RADIAN_URL));
        DistanceFF instance = new DistanceFF();
        double expResult = 0.096034;
        double result = instance.exec(v1, v2, v3).getDouble();
        assertEquals(expResult, result, 0.0001);
    }

    /**
     * Test of exec method, of class DistanceFF.
     */
    @Test(expected = ExprEvalException.class)
    public void testExec_geographic_metres_exception() {

        NodeValue v1 = NodeValue.makeNode("Point(11.41 53.63)", WKTDatatype.INSTANCE);
        NodeValue v2 = NodeValue.makeNode("Point(11.57 48.13)", WKTDatatype.INSTANCE);
        NodeValue v3 = NodeValue.makeNode(NodeFactory.createURI(Unit_URI.KILOMETRE_URN));
        DistanceFF instance = new DistanceFF();
        double result = instance.exec(v1, v2, v3).getDouble();
    }

    /**
     * Test of exec method, of class DistanceFF.
     */
    @Test(expected = ExprEvalException.class)
    public void testExec_conversion_exception() {

        GeoSPARQLConfig.allowGeometrySRSTransformation(false);     // Disable default config.
        NodeValue v1 = NodeValue.makeNode("<http://www.opengis.net/def/crs/EPSG/0/27700> POINT(90 60)", WKTDatatype.INSTANCE);
        NodeValue v2 = NodeValue.makeNode("Point(11.57 48.13)", WKTDatatype.INSTANCE);
        NodeValue v3 = NodeValue.makeNode(NodeFactory.createURI(Unit_URI.KILOMETRE_URN));
        DistanceFF instance = new DistanceFF();
        try {
            double aResult = instance.exec(v1, v2, v3).getDouble();
        } catch (ExprEvalException ex) {
            throw ex;
        } finally {
            GeoSPARQLConfig.allowGeometrySRSTransformation(true);
        }
    }

    /**
     * Test of exec method, of class DistanceFF.
     */
    @Test(expected = ExprEvalException.class)
    public void testExec_conversion_exception_units() {
        // Still receive an expection as the units of v1 and v3 don't align.
        GeoSPARQLConfig.allowGeometrySRSTransformation(false);     // Disable default config.
        NodeValue v1 = NodeValue.makeNode("Point(11.57 48.13)", WKTDatatype.INSTANCE);
        NodeValue v2 = NodeValue.makeNode("<http://www.opengis.net/def/crs/EPSG/0/27700> POINT(90 60)", WKTDatatype.INSTANCE);
        NodeValue v3 = NodeValue.makeNode(NodeFactory.createURI(Unit_URI.KILOMETRE_URN));
        DistanceFF instance = new DistanceFF();
        try {
            double aResult = instance.exec(v1, v2, v3).getDouble();
        } catch (ExprEvalException ex) {
            throw ex;
        } finally {
            GeoSPARQLConfig.allowGeometrySRSTransformation(true);
        }
    }
}
