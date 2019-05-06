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
import org.junit.Test;

/**
 *
 *
 */
public class AzimuthTest {

    public AzimuthTest() {
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
     * Test of find method, of class Azimuth.
     */
    @Test
    public void testFind_East() {

        double lat1 = 0.0;
        double lon1 = 0.0;
        double lat2 = 0.0;
        double lon2 = 10.0;
        double expResult = Math.toRadians(90);
        double result = Azimuth.find(lat1, lon1, lat2, lon2);
        assertEquals(expResult, result, 0.0);
    }

    /**
     * Test of find method, of class Azimuth.
     */
    @Test
    public void testFind_North() {

        double lat1 = 0.0;
        double lon1 = 0.0;
        double lat2 = 10.0;
        double lon2 = 0.0;
        double expResult = Math.toRadians(0);
        double result = Azimuth.find(lat1, lon1, lat2, lon2);
        assertEquals(expResult, result, 0.0);
    }

    /**
     * Test of find method, of class Azimuth.
     */
    @Test
    public void testFind_South() {

        double lat1 = 0.0;
        double lon1 = 0.0;
        double lat2 = -10.0;
        double lon2 = 0.0;
        double expResult = Math.toRadians(180);
        double result = Azimuth.find(lat1, lon1, lat2, lon2);
        assertEquals(expResult, result, 0.0);
    }

    /**
     * Test of find method, of class Azimuth.
     */
    @Test
    public void testFind_West() {

        double lat1 = 0.0;
        double lon1 = 0.0;
        double lat2 = 0.0;
        double lon2 = -10.0;
        double expResult = Math.toRadians(270);
        double result = Azimuth.find(lat1, lon1, lat2, lon2);
        assertEquals(expResult, result, 0.0);
    }

    /**
     * Test of find method, of class Azimuth.
     */
    @Test
    public void testFind_East_cross_boundary() {

        double lat1 = 0.0;
        double lon1 = 170.0;
        double lat2 = 0.0;
        double lon2 = -170.0;
        double expResult = Math.toRadians(90);
        double result = Azimuth.find(lat1, lon1, lat2, lon2);
        assertEquals(expResult, result, 0.0);
    }

    /**
     * Test of find method, of class Azimuth.
     */
    @Test
    public void testFind_West_cross_boundary() {

        double lat1 = 0.0;
        double lon1 = -170.0;
        double lat2 = 0.0;
        double lon2 = 170.0;
        double expResult = Math.toRadians(270);
        double result = Azimuth.find(lat1, lon1, lat2, lon2);
        assertEquals(expResult, result, 0.0);
    }

}
