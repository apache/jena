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
package org.apache.jena.geosparql.implementation.great_circle;

import org.apache.jena.geosparql.implementation.GeometryWrapper;
import org.apache.jena.geosparql.implementation.datatype.WKTDatatype;
import org.junit.After;
import org.junit.AfterClass;
import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.locationtech.jts.geom.Coordinate;

/**
 *
 *
 */
public class CoordinatePairTest {

    public CoordinatePairTest() {
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
     * Test of findNearestPair method, of class CoordinatePair.
     */
    @Test
    public void testFindNearestPair_boundary() {
        System.out.println("findNearestPair_boundary");
        GeometryWrapper sourceGeometry = GeometryWrapper.extract("<http://www.opengis.net/def/crs/EPSG/0/4326> POLYGON((10.0 -180.0, 20.0 -180.0, 20.0 -170.0, 10.0 -170.0, 10.0 -180.0))", WKTDatatype.URI);
        GeometryWrapper targetGeometry = GeometryWrapper.extract("<http://www.opengis.net/def/crs/EPSG/0/4326> POLYGON((10.0 170.0, 20.0 170.0, 20.0 179.0, 10.0 179.0, 10.0 170.0))", WKTDatatype.URI);

        CoordinatePair expResult = new CoordinatePair(new Coordinate(180, 20), new Coordinate(179, 20));
        CoordinatePair result = CoordinatePair.findNearestPair(sourceGeometry, targetGeometry);

        //System.out.println("Exp: " + expResult);
        //System.out.println("Res: " + result);
        assertEquals(expResult, result);
    }

    /**
     * Test of findNearestPair method, of class CoordinatePair.
     */
    @Test
    public void testFindNearestPair_boundary2() {
        System.out.println("findNearestPair_boundary2");
        GeometryWrapper sourceGeometry = GeometryWrapper.extract("<http://www.opengis.net/def/crs/EPSG/0/4326> POLYGON((10.0 170.0, 20.0 170.0, 20.0 179.0, 10.0 179.0, 10.0 170.0))", WKTDatatype.URI);
        GeometryWrapper targetGeometry = GeometryWrapper.extract("<http://www.opengis.net/def/crs/EPSG/0/4326> POLYGON((10.0 -180.0, 20.0 -180.0, 20.0 -170.0, 10.0 -170.0, 10.0 -180.0))", WKTDatatype.URI);

        CoordinatePair expResult = new CoordinatePair(new Coordinate(179, 20), new Coordinate(180, 20));
        CoordinatePair result = CoordinatePair.findNearestPair(sourceGeometry, targetGeometry);

        //System.out.println("Exp: " + expResult);
        //System.out.println("Res: " + result);
        assertEquals(expResult, result);
    }

    /**
     * Test of findNearestPair method, of class CoordinatePair.
     */
    @Test
    public void testFindNearestPair_near() {
        System.out.println("findNearestPair_near");
        GeometryWrapper sourceGeometry = GeometryWrapper.extract("<http://www.opengis.net/def/crs/EPSG/0/4326> POLYGON((10.0 1.0, 20.0 1.0, 20.0 2.0, 10.0 2.0, 10.0 1.0))", WKTDatatype.URI);
        GeometryWrapper targetGeometry = GeometryWrapper.extract("<http://www.opengis.net/def/crs/EPSG/0/4326> POLYGON((10.0 -2.0, 20.0 -2.0, 20.0 -1.0, 10.0 -1.0, 10.0 -2.0))", WKTDatatype.URI);

        CoordinatePair expResult = new CoordinatePair(new Coordinate(1, 20), new Coordinate(-1, 20));
        CoordinatePair result = CoordinatePair.findNearestPair(sourceGeometry, targetGeometry);

        //System.out.println("Exp: " + expResult);
        //System.out.println("Res: " + result);
        assertEquals(expResult, result);
    }

    /**
     * Test of findNearestPair method, of class CoordinatePair.
     */
    @Test
    public void testFindNearestPair_near_postive() {
        System.out.println("findNearestPair_near_positive");
        GeometryWrapper sourceGeometry = GeometryWrapper.extract("<http://www.opengis.net/def/crs/EPSG/0/4326> POLYGON((10.0 179.0, 20.0 179.0, 20.0 180.0, 10.0 180.0, 10.0 179.0))", WKTDatatype.URI);
        GeometryWrapper targetGeometry = GeometryWrapper.extract("<http://www.opengis.net/def/crs/EPSG/0/4326> POLYGON((10.0 177.0, 20.0 177.0, 20.0 178.0, 10.0 178.0, 10.0 177.0))", WKTDatatype.URI);

        CoordinatePair expResult = new CoordinatePair(new Coordinate(179, 20), new Coordinate(178, 20));
        CoordinatePair result = CoordinatePair.findNearestPair(sourceGeometry, targetGeometry);

        //System.out.println("Exp: " + expResult);
        //System.out.println("Res: " + result);
        assertEquals(expResult, result);
    }

    /**
     * Test of findNearestPair method, of class CoordinatePair.
     */
    @Test
    public void testFindNearestPair_near_negative() {
        System.out.println("findNearestPair_near_negative");
        GeometryWrapper sourceGeometry = GeometryWrapper.extract("<http://www.opengis.net/def/crs/EPSG/0/4326> POLYGON((10.0 -180.0, 20.0 -180.0, 20.0 -179.0, 10.0 -179.0, 10.0 -180.0))", WKTDatatype.URI);
        GeometryWrapper targetGeometry = GeometryWrapper.extract("<http://www.opengis.net/def/crs/EPSG/0/4326> POLYGON((10.0 -178.0, 20.0 -178.0, 20.0 -177.0, 10.0 -177.0, 10.0 -178.0))", WKTDatatype.URI);

        CoordinatePair expResult = new CoordinatePair(new Coordinate(-179, 20), new Coordinate(-178, 20));
        CoordinatePair result = CoordinatePair.findNearestPair(sourceGeometry, targetGeometry);

        //System.out.println("Exp: " + expResult);
        //System.out.println("Res: " + result);
        assertEquals(expResult, result);
    }

    /**
     * Test of findNearestPair method, of class CoordinatePair.
     */
    @Test
    public void testFindNearestPair_same_positive_half() {
        System.out.println("findNearestPair_same_positive_half");
        GeometryWrapper sourceGeometry = GeometryWrapper.extract("<http://www.opengis.net/def/crs/EPSG/0/4326> POLYGON((10.0 179.0, 20.0 179.0, 20.0 180.0, 10.0 180.0, 10.0 179.0))", WKTDatatype.URI);
        GeometryWrapper targetGeometry = GeometryWrapper.extract("<http://www.opengis.net/def/crs/EPSG/0/4326> POLYGON((10.0 0.0, 20.0 0.0, 20.0 1.0, 10.0 1.0, 10.0 0.0))", WKTDatatype.URI);

        CoordinatePair expResult = new CoordinatePair(new Coordinate(179, 20), new Coordinate(1, 20));
        CoordinatePair result = CoordinatePair.findNearestPair(sourceGeometry, targetGeometry);

        //System.out.println("Exp: " + expResult);
        //System.out.println("Res: " + result);
        assertEquals(expResult, result);
    }

    /**
     * Test of findNearestPair method, of class CoordinatePair.
     */
    @Test
    public void testFindNearestPair_same_negative_half() {
        System.out.println("findNearestPair_same_negative_half");
        GeometryWrapper sourceGeometry = GeometryWrapper.extract("<http://www.opengis.net/def/crs/EPSG/0/4326> POLYGON((10.0 -180.0, 20.0 -180.0, 20.0 -179.0, 10.0 -179.0, 10.0 -180.0))", WKTDatatype.URI);
        GeometryWrapper targetGeometry = GeometryWrapper.extract("<http://www.opengis.net/def/crs/EPSG/0/4326> POLYGON((10.0 -1.0, 20.0 -1.0, 20.0 0.0, 10.0 0.0, 10.0 -1.0))", WKTDatatype.URI);

        CoordinatePair expResult = new CoordinatePair(new Coordinate(-179, 20), new Coordinate(-1, 20));
        CoordinatePair result = CoordinatePair.findNearestPair(sourceGeometry, targetGeometry);

        //System.out.println("Exp: " + expResult);
        //System.out.println("Res: " + result);
        assertEquals(expResult, result);
    }

    /**
     * Test of isEqual method, of class CoordinatePair.
     */
    @Test
    public void testIsEqual_overlap() {
        System.out.println("isEqual_overlap");
        GeometryWrapper sourceGeometry = GeometryWrapper.extract("<http://www.opengis.net/def/crs/EPSG/0/4326> POLYGON((0.0 0.0, 10.0 0.0, 10.0 10.0, 0.0 10.0, 0.0 0.0))", WKTDatatype.URI);
        GeometryWrapper targetGeometry = GeometryWrapper.extract("<http://www.opengis.net/def/crs/EPSG/0/4326> POLYGON((5.0 5.0, 15.0 5.0, 15.0 15.0, 5.0 15.0, 5.0 5.0))", WKTDatatype.URI);

        CoordinatePair instance = CoordinatePair.findNearestPair(sourceGeometry, targetGeometry);

        boolean expResult = true;
        boolean result = instance.isEqual();

        //System.out.println("Exp: " + expResult);
        //System.out.println("Res: " + result);
        assertEquals(expResult, result);
    }

    /**
     * Test of isEqual method, of class CoordinatePair.
     */
    @Test
    public void testIsEqual_no_overlap() {
        System.out.println("isEqual_no_overlap");
        GeometryWrapper sourceGeometry = GeometryWrapper.extract("<http://www.opengis.net/def/crs/EPSG/0/4326> POLYGON((0.0 0.0, 10.0 0.0, 10.0 10.0, 0.0 10.0, 0.0 0.0))", WKTDatatype.URI);
        GeometryWrapper targetGeometry = GeometryWrapper.extract("<http://www.opengis.net/def/crs/EPSG/0/4326> POLYGON((15.0 15.0, 25.0 15.0, 25.0 25.0, 15.0 25.0, 15.0 15.0))", WKTDatatype.URI);

        CoordinatePair instance = CoordinatePair.findNearestPair(sourceGeometry, targetGeometry);

        boolean expResult = false;
        boolean result = instance.isEqual();

        //System.out.println("Exp: " + expResult);
        //System.out.println("Res: " + result);
        assertEquals(expResult, result);
    }

    /**
     * Test of isEqual method, of class CoordinatePair.
     */
    @Test
    public void testIsEqual_within() {
        System.out.println("isEqual_within");
        GeometryWrapper sourceGeometry = GeometryWrapper.extract("<http://www.opengis.net/def/crs/EPSG/0/4326> POLYGON((0.0 0.0, 10.0 0.0, 10.0 10.0, 0.0 10.0, 0.0 0.0))", WKTDatatype.URI);
        GeometryWrapper targetGeometry = GeometryWrapper.extract("<http://www.opengis.net/def/crs/EPSG/0/4326> POINT(5.0 5.0)", WKTDatatype.URI);

        CoordinatePair instance = CoordinatePair.findNearestPair(sourceGeometry, targetGeometry);

        boolean expResult = true;
        boolean result = instance.isEqual();

        //System.out.println("Exp: " + expResult);
        //System.out.println("Res: " + result);
        assertEquals(expResult, result);
    }

    /**
     * Test of isEqual method, of class CoordinatePair.
     */
    @Test
    public void testIsEqual_no_within() {
        System.out.println("isEqual_no_within");
        GeometryWrapper sourceGeometry = GeometryWrapper.extract("<http://www.opengis.net/def/crs/EPSG/0/4326> POLYGON((0.0 0.0, 10.0 0.0, 10.0 10.0, 0.0 10.0, 0.0 0.0))", WKTDatatype.URI);
        GeometryWrapper targetGeometry = GeometryWrapper.extract("<http://www.opengis.net/def/crs/EPSG/0/4326> POINT(15.0 15.0)", WKTDatatype.URI);

        CoordinatePair instance = CoordinatePair.findNearestPair(sourceGeometry, targetGeometry);

        boolean expResult = false;
        boolean result = instance.isEqual();

        //System.out.println("Exp: " + expResult);
        //System.out.println("Res: " + result);
        assertEquals(expResult, result);
    }
}
