/*
 * Copyright 2019 the original author or authors.
 * See the notice.md file distributed with this work for additional
 * information regarding copyright ownership.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
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
        System.out.println("vincentyFormula_London_Paris");
        double lat1 = 51.50853;
        double lon1 = -0.12574;
        double lat2 = 48.85341;
        double lon2 = 2.34880;
        double expResult = 343771;
        double result = GreatCircleDistance.vincentyFormula(lat1, lon1, lat2, lon2);

        //System.out.println("Exp: " + expResult);
        //System.out.println("Res: " + result);
        assertEquals(expResult, result, 1);
    }

    /**
     * Test of vincentyFormula method, of class GreatCircleDistance.
     */
    @Test
    public void testVincentyFormula_Paris_London() {
        System.out.println("vincentyFormula_Paris_London");
        double lat2 = 51.50853;
        double lon2 = -0.12574;
        double lat1 = 48.85341;
        double lon1 = 2.34880;
        double expResult = 343771;
        double result = GreatCircleDistance.vincentyFormula(lat1, lon1, lat2, lon2);

        //System.out.println("Exp: " + expResult);
        //System.out.println("Res: " + result);
        assertEquals(expResult, result, 1);
    }

}
