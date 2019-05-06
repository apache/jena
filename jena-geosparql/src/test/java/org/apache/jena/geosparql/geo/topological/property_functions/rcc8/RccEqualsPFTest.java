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
package org.apache.jena.geosparql.geo.topological.property_functions.rcc8;

import org.apache.jena.geosparql.implementation.GeometryWrapper;
import org.apache.jena.geosparql.implementation.datatype.WKTDatatype;
import org.apache.jena.rdf.model.Literal;
import org.apache.jena.rdf.model.ResourceFactory;
import org.junit.After;
import org.junit.AfterClass;
import static org.junit.Assert.assertEquals;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * Equal returns t (TRUE) if two geometries have at least one point in common,
 * and no point of either geometry lies in the exterior of the other geometry.
 */
public class RccEqualsPFTest {

    public RccEqualsPFTest() {
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

    //Only Polygon-Polygon
    @Test
    public void testFilterFunction_polygon_polygon() {
        System.out.println("filterFunction_polygon_polygon");

        Literal subjectGeometryLiteral = ResourceFactory.createTypedLiteral("<http://www.opengis.net/def/crs/EPSG/0/27700> POLYGON((140 15, 140 45, 200 45, 200 15, 140 15))", WKTDatatype.INSTANCE);
        Literal objectGeometryLiteral = ResourceFactory.createTypedLiteral("<http://www.opengis.net/def/crs/EPSG/0/27700> POLYGON((140 15, 140 45, 200 45, 200 15, 140 15))", WKTDatatype.INSTANCE);

        RccEqualsPF instance = new RccEqualsPF();

        Boolean expResult = true;
        Boolean result = instance.testFilterFunction(subjectGeometryLiteral.asNode(), objectGeometryLiteral.asNode());

        //System.out.println("Exp: " + expResult);
        //System.out.println("Res: " + result);
        assertEquals(expResult, result);
    }

    @Test
    public void testFilterFunction_polygon_polygon_false() {
        System.out.println("filterFunction_polygon_polygon_false");

        Literal subjectGeometryLiteral = ResourceFactory.createTypedLiteral("<http://www.opengis.net/def/crs/EPSG/0/27700> POLYGON((30 40, 30 70, 90 70, 90 40, 30 40))", WKTDatatype.INSTANCE);
        Literal objectGeometryLiteral = ResourceFactory.createTypedLiteral("<http://www.opengis.net/def/crs/EPSG/0/27700> POLYGON((140 15, 140 45, 200 45, 200 15, 140 15))", WKTDatatype.INSTANCE);

        RccEqualsPF instance = new RccEqualsPF();

        Boolean expResult = false;
        Boolean result = instance.testFilterFunction(subjectGeometryLiteral.asNode(), objectGeometryLiteral.asNode());

        //System.out.println("Exp: " + expResult);
        //System.out.println("Res: " + result);
        assertEquals(expResult, result);
    }

    /**
     * Empty geometries are not spatially equal.
     */
    @Test
    public void testFilterFunction_emptyWKT_emptyGML() {
        System.out.println("filterFunction_emptyWKT_emptyGML");

        Literal emptyWKT = GeometryWrapper.getEmptyWKT().asLiteral();
        Literal emptyGML = GeometryWrapper.getEmptyGML().asLiteral();

        RccEqualsPF instance = new RccEqualsPF();

        Boolean expResult = false;
        Boolean result = instance.testFilterFunction(emptyWKT.asNode(), emptyGML.asNode());

        //System.out.println("Exp: " + expResult);
        //System.out.println("Res: " + result);
        assertEquals(expResult, result);
    }

    /**
     * Empty geometries are not spatially equal.
     */
    @Test
    public void testFilterFunction_emptyWKT_emptyWKT() {
        System.out.println("filterFunction_empty_empty");

        Literal emptyWKT = GeometryWrapper.getEmptyWKT().asLiteral();
        Literal emptyWKT2 = GeometryWrapper.getEmptyWKT().asLiteral();

        RccEqualsPF instance = new RccEqualsPF();

        Boolean expResult = false;
        Boolean result = instance.testFilterFunction(emptyWKT.asNode(), emptyWKT2.asNode());

        //System.out.println("Exp: " + expResult);
        //System.out.println("Res: " + result);
        assertEquals(expResult, result);
    }
}
