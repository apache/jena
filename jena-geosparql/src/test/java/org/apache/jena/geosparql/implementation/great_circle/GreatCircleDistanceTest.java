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

import org.junit.After;
import org.junit.AfterClass;
import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

/**
 *
 *
 */
public class GreatCircleDistanceTest {

    public GreatCircleDistanceTest() {
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
     * Test of vincentyFormula method, of class GreatCircleDistance.
     */
    @Test
    public void testVincentyFormula_London_Paris() {

        double lat1 = 51.50853;
        double lon1 = -0.12574;
        double lat2 = 48.85341;
        double lon2 = 2.34880;
        double expResult = 343771;
        double result = GreatCircleDistance.vincentyFormula(lat1, lon1, lat2, lon2);
        assertEquals(expResult, result, 1);
    }

    /**
     * Test of vincentyFormula method, of class GreatCircleDistance.
     */
    @Test
    public void testVincentyFormula_Paris_London() {

        double lat1 = 48.85341;
        double lon1 = 2.34880;
        double lat2 = 51.50853;
        double lon2 = -0.12574;
        double expResult = 343771;
        double result = GreatCircleDistance.vincentyFormula(lat1, lon1, lat2, lon2);
        assertEquals(expResult, result, 1);
    }

    /**
     * Test of vincentyFormula method, test values from issue JENA-1915.
     */
    @Test
    public void testVincentyFormula_Distance1() {

        double lat1 = 41.2572;
        double lon1 = -95.9656;
        double lat2 = 41.2592;
        double lon2 = -95.9339;
        double expResult = 2.8736;
        double result = GreatCircleDistance.vincentyFormula(lat1, lon1, lat2, lon2) / 1000; // Adjustment to kilometres.
        assertEquals(expResult, result, 0.3);
    }

    /**
     * Test of vincentyFormula method, test values from issue JENA-1915.
     */
    @Test
    public void testVincentyFormula_Distance2() {

        double lat1 = 41.2572;
        double lon1 = -95.9656;
        double lat2 = 41.2482;
        double lon2 = -96.072;
        double expResult = 9.1893;
        double result = GreatCircleDistance.vincentyFormula(lat1, lon1, lat2, lon2) / 1000; // Adjustment to kilometres.
        assertEquals(expResult, result, 0.3);
    }

    /**
     * Test of vincentyFormula method, test values from issue JENA-1915.
     */
    @Test
    public void testVincentyFormula_Distance3() {

        double lat1 = 41.2572;
        double lon1 = -95.9656;
        double lat2 = 41.5871;
        double lon2 = -93.626;
        double expResult = 198.3806;
        double result = GreatCircleDistance.vincentyFormula(lat1, lon1, lat2, lon2) / 1000; // Adjustment to kilometres.
        assertEquals(expResult, result, 0.3);
    }

    /**
     * Test of vincentyFormula method, test values from issue JENA-1915.
     */
    @Test
    public void testVincentyFormula_Distance4() {

        double lat1 = 41.2572;
        double lon1 = -95.9656;
        double lat2 = 51.0472;
        double lon2 = -113.9998;
        double expResult = 1757.1519;
        double result = GreatCircleDistance.vincentyFormula(lat1, lon1, lat2, lon2) / 1000; // Adjustment to kilometres.
        assertEquals(expResult, result, 0.3);
    }

    /**
     * Test of vincentyFormula method, test values from issue JENA-1915.
     */
    @Test
    public void testVincentyFormula_Distance5() {

        double lat1 = 41.2572;
        double lon1 = -95.9656;
        double lat2 = 40.7528;
        double lon2 = -73.9876;
        double expResult = 1840.1915;
        double result = GreatCircleDistance.vincentyFormula(lat1, lon1, lat2, lon2) / 1000; // Adjustment to kilometres.
        assertEquals(expResult, result, 0.3);
    }

    /**
     * Test of vincentyFormula method, test values from issue JENA-1915.
     */
    @Test
    public void testVincentyFormula_Distance6() {

        double lat1 = 41.2572;
        double lon1 = -95.9656;
        double lat2 = 49.7237;
        double lon2 = 13.3422;
        double expResult = 7781.1365;
        double result = GreatCircleDistance.vincentyFormula(lat1, lon1, lat2, lon2) / 1000; // Adjustment to kilometres.
        assertEquals(expResult, result, 0.3);
    }

    /**
     * Test of vincentyFormula method, test values from issue JENA-1915.
     */
    @Ignore
    @Test
    public void testVincentyFormula_Distance7() {

        double lat1 = 41.2572;
        double lon1 = -95.9656;
        double lat2 = -33.9065;
        double lon2 = 18.4175;
        double expResult = 14312.6630;
        double result = GreatCircleDistance.vincentyFormula(lat1, lon1, lat2, lon2) / 1000; // Adjustment to kilometres.
        assertEquals(expResult, result, 0.3);
    }

    /**
     * Test of vincentyFormula method, test values from issue JENA-1915.
     */
    @Ignore
    @Test
    public void testVincentyFormula_Distance8() {

        double lat1 = 41.2572;
        double lon1 = -95.9656;
        double lat2 = -33.8646;
        double lon2 = 151.2099;
        double expResult = 14184.1430;
        double result = GreatCircleDistance.vincentyFormula(lat1, lon1, lat2, lon2) / 1000; // Adjustment to kilometres.
        assertEquals(expResult, result, 0.3);
    }

    /**
     * Test of haversineFormula method, of class GreatCircleDistance.
     */
    @Test
    public void testHaversineFormula_London_Paris() {

        double lat1 = 51.50853;
        double lon1 = -0.12574;
        double lat2 = 48.85341;
        double lon2 = 2.34880;
        double expResult = 343771;
        double result = GreatCircleDistance.haversineFormula(lat1, lon1, lat2, lon2);
        assertEquals(expResult, result, 1);
    }

    /**
     * Test of haversineFormula method, of class GreatCircleDistance.
     */
    @Test
    public void testHaversineFormula_Paris_London() {

        double lat1 = 48.85341;
        double lon1 = 2.34880;
        double lat2 = 51.50853;
        double lon2 = -0.12574;
        double expResult = 343771;
        double result = GreatCircleDistance.haversineFormula(lat1, lon1, lat2, lon2);
        assertEquals(expResult, result, 1);
    }

    /**
     * Test of haversineFormula method, test values from issue JENA-1915.
     */
    @Test
    public void testHaversineFormula_Distance1() {

        double lat1 = 41.2572;
        double lon1 = -95.9656;
        double lat2 = 41.2592;
        double lon2 = -95.9339;
        double expResult = 2.8736;
        double result = GreatCircleDistance.haversineFormula(lat1, lon1, lat2, lon2) / 1000; // Adjustment to kilometres.
        assertEquals(expResult, result, 0.3);
    }

    /**
     * Test of haversineFormula method, test values from issue JENA-1915.
     */
    @Test
    public void testHaversineFormula_Distance2() {

        double lat1 = 41.2572;
        double lon1 = -95.9656;
        double lat2 = 41.2482;
        double lon2 = -96.072;
        double expResult = 9.1893;
        double result = GreatCircleDistance.haversineFormula(lat1, lon1, lat2, lon2) / 1000; // Adjustment to kilometres.
        assertEquals(expResult, result, 0.3);
    }

    /**
     * Test of haversineFormula method, test values from issue JENA-1915.
     */
    @Test
    public void testHaversineFormula_Distance3() {

        double lat1 = 41.2572;
        double lon1 = -95.9656;
        double lat2 = 41.5871;
        double lon2 = -93.626;
        double expResult = 198.3806;
        double result = GreatCircleDistance.haversineFormula(lat1, lon1, lat2, lon2) / 1000; // Adjustment to kilometres.
        assertEquals(expResult, result, 0.3);
    }

    /**
     * Test of haversineFormula method, test values from issue JENA-1915.
     */
    @Test
    public void testHaversineFormula_Distance4() {

        double lat1 = 41.2572;
        double lon1 = -95.9656;
        double lat2 = 51.0472;
        double lon2 = -113.9998;
        double expResult = 1757.1519;
        double result = GreatCircleDistance.haversineFormula(lat1, lon1, lat2, lon2) / 1000; // Adjustment to kilometres.
        assertEquals(expResult, result, 0.3);
    }

    /**
     * Test of haversineFormula method, test values from issue JENA-1915.
     */
    @Test
    public void testHaversineFormula_Distance5() {

        double lat1 = 41.2572;
        double lon1 = -95.9656;
        double lat2 = 40.7528;
        double lon2 = -73.9876;
        double expResult = 1840.1915;
        double result = GreatCircleDistance.haversineFormula(lat1, lon1, lat2, lon2) / 1000; // Adjustment to kilometres.
        assertEquals(expResult, result, 0.3);
    }

    /**
     * Test of haversineFormula method, test values from issue JENA-1915.
     */
    @Test
    public void testHaversineFormula_Distance6() {

        double lat1 = 41.2572;
        double lon1 = -95.9656;
        double lat2 = 49.7237;
        double lon2 = 13.3422;
        double expResult = 7781.1365;
        double result = GreatCircleDistance.haversineFormula(lat1, lon1, lat2, lon2) / 1000; // Adjustment to kilometres.
        assertEquals(expResult, result, 0.3);
    }

    /**
     * Test of haversineFormula method, test values from issue JENA-1915.
     */
    @Test
    public void testHaversineFormula_Distance7() {

        double lat1 = 41.2572;
        double lon1 = -95.9656;
        double lat2 = -33.9065;
        double lon2 = 18.4175;
        double expResult = 14312.6630;
        double result = GreatCircleDistance.haversineFormula(lat1, lon1, lat2, lon2) / 1000; // Adjustment to kilometres.
        assertEquals(expResult, result, 0.3);
    }

    /**
     * Test of haversineFormula method, test values from issue JENA-1915.
     */
    @Test
    public void testHaversineFormula_Distance8() {

        double lat1 = 41.2572;
        double lon1 = -95.9656;
        double lat2 = -33.8646;
        double lon2 = 151.2099;
        double expResult = 14184.1430;
        double result = GreatCircleDistance.haversineFormula(lat1, lon1, lat2, lon2) / 1000; // Adjustment to kilometres.
        assertEquals(expResult, result, 0.3);
    }

}
