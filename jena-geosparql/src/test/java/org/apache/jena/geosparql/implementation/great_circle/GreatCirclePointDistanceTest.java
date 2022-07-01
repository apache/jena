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

import org.apache.jena.geosparql.implementation.jts.CustomGeometryFactory;
import org.junit.After;
import org.junit.AfterClass;
import static org.junit.Assert.assertEquals;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;
import org.locationtech.jts.geom.GeometryFactory;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class GreatCirclePointDistanceTest {

    private static final GeometryFactory GEOMETRY_FACTORY = CustomGeometryFactory.theInstance();

    public GreatCirclePointDistanceTest() {
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
     * Test of latitude method, of class GreatCirclePointDistance.
     */
    @Test
    public void testLatitude() {

        double startLat = 48.85341;
        double startLon = -2.3488;
        double distance = 100000.0;
        double bearingRad = 0.0;
        GreatCirclePointDistance instance = new GreatCirclePointDistance(startLat, startLon, distance);
        double expResult = 0.868348;
        double result = instance.latitude(bearingRad);
        assertEquals(expResult, result, 0.00001);
    }

    /**
     * Test of longitude method, of class GreatCirclePointDistance.
     */
    @Test
    public void testLongitude() {

        double endLatRad = 0.86833;
        double startLat = 48.85341;
        double startLon = -2.3488;
        double distance = 100000.0;
        double bearingRad = 0.0;
        GreatCirclePointDistance instance = new GreatCirclePointDistance(startLat, startLon, distance);
        double expResult = -0.040994;
        double result = instance.longitude(endLatRad, bearingRad);
        assertEquals(expResult, result, 0.00001);
    }

    /**
     * Test of radToPoint method, of class GreatCirclePointDistance.
     */
    @Test
    public void testRadToPoint_double_double() {

        double latRad = 0.5;
        double lonRad = 0.1;
        LatLonPoint expResult = new LatLonPoint(Math.toDegrees(latRad), Math.toDegrees(lonRad));
        LatLonPoint result = GreatCirclePointDistance.radToPoint(latRad, lonRad);
        assertEquals(expResult, result);
    }

    /**
     * Test of radToPoint method, of class GreatCirclePointDistance.
     */
    @Test
    public void testRadToPoint_3args_false() {

        double latRad = 0.5;
        double lonRad = Math.PI + 0.1;
        boolean isNormaliseLon = false;
        LatLonPoint expResult = new LatLonPoint(Math.toDegrees(latRad), Math.toDegrees(lonRad));
        LatLonPoint result = GreatCirclePointDistance.radToPoint(latRad, lonRad, isNormaliseLon);
        assertEquals(expResult, result);
    }

    /**
     * Test of radToPoint method, of class GreatCirclePointDistance.
     */
    @Test
    public void testRadToPoint_3args_true() {

        double latRad = 0.5;
        double lonRad = Math.PI + 0.2;
        boolean isNormaliseLon = true;
        LatLonPoint expResult = new LatLonPoint(Math.toDegrees(latRad), Math.toDegrees(-Math.PI + 0.2));
        LatLonPoint result = GreatCirclePointDistance.radToPoint(latRad, lonRad, isNormaliseLon);
        assertEquals(expResult, result);
    }

    /**
     * Test of normaliseLongitude method, of class GreatCirclePointDistance.
     */
    @Test
    public void testNormaliseLongitude() {

        double lonDegrees = 185;
        double expResult = -175;
        double result = GreatCirclePointDistance.normaliseLongitude(lonDegrees);
        assertEquals(expResult, result, 0.0);
    }

    /**
     * Test of getPoint method, of class GreatCirclePointDistance.
     */
    @Test
    public void testGetPoint_3args_North() {

        double startLat = 48.85341;
        double startLon = -2.3488;
        double distance = 100000.0;
        LatLonPoint startPoint = new LatLonPoint(startLat, startLon);
        double bearing = 0.0;
        LatLonPoint expResult = new LatLonPoint(49.752730, -2.3488);
        LatLonPoint result = GreatCirclePointDistance.getPoint(startPoint, distance, bearing);
        assertEquals(expResult, result);
    }

    /**
     * Test of getPoint method, of class GreatCirclePointDistance.
     */
    @Test
    public void testGetPoint_4args_North() {

        double startLat = 48.85341;
        double startLon = -2.3488;
        double distance = 100000.0;
        double bearing = 0.0;
        LatLonPoint expResult = new LatLonPoint(49.752730, -2.3488);
        LatLonPoint result = GreatCirclePointDistance.getPoint(startLat, startLon, distance, bearing);
        assertEquals(expResult, result);
    }

    /**
     * Test of getPoint method, of class GreatCirclePointDistance.
     */
    @Test
    public void testGetPoint_3args_East() {

        double startLat = 48.85341;
        double startLon = -2.3488;
        double distance = 100000.0;
        LatLonPoint startPoint = new LatLonPoint(startLat, startLon);
        double bearing = 90.0;
        LatLonPoint expResult = new LatLonPoint(48.845333, -0.982173);
        LatLonPoint result = GreatCirclePointDistance.getPoint(startPoint, distance, bearing);
        assertEquals(expResult, result);
    }

    /**
     * Test of getPoint method, of class GreatCirclePointDistance.
     */
    @Test
    public void testGetPoint_4args_East() {

        double startLat = 48.85341;
        double startLon = -2.3488;
        double distance = 100000.0;
        double bearing = 90.0;
        LatLonPoint expResult = new LatLonPoint(48.845333, -0.982173);
        LatLonPoint result = GreatCirclePointDistance.getPoint(startLat, startLon, distance, bearing);
        assertEquals(expResult, result);
    }

}
