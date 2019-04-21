/*
 * Copyright 2019 .
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

import org.apache.jena.geosparql.implementation.great_circle.Angle;
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
public class AngleTest {

    public AngleTest() {
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
     * Test of find method, of class Angle.
     */
    @Test
    public void testFind_upper_right() {
        System.out.println("find_upper_right");
        double x1 = 25.0;
        double y1 = 45.0;
        double x2 = 75.0;
        double y2 = 100.0;
        double expResult = Math.toRadians(42.273689);
        double result = Angle.find(x1, y1, x2, y2);

        //System.out.println("Exp: " + expResult);
        //System.out.println("Res: " + result);
        assertEquals(expResult, result, 0.000001);
    }

    /**
     * Test of find method, of class Angle.
     */
    @Test
    public void testFind_upper_left() {
        System.out.println("find_upper_left");
        double x1 = 25.0;
        double y1 = 45.0;
        double x2 = -25.0;
        double y2 = 100.0;
        double expResult = Math.toRadians(312.273689);
        double result = Angle.find(x1, y1, x2, y2);

        //System.out.println("Exp: " + expResult);
        //System.out.println("Res: " + result);
        assertEquals(expResult, result, 0.000001);
    }

    /**
     * Test of find method, of class Angle.
     */
    @Test
    public void testFind_lower_left() {
        System.out.println("find_lower_left");
        double x1 = 75.0;
        double y1 = 100.0;
        double x2 = 25.0;
        double y2 = 45.0;
        double expResult = Math.toRadians(222.273689);
        double result = Angle.find(x1, y1, x2, y2);

        //System.out.println("Exp: " + expResult);
        //System.out.println("Res: " + result);
        assertEquals(expResult, result, 0.000001);
    }

    /**
     * Test of find method, of class Angle.
     */
    @Test
    public void testFind_lower_right() {
        System.out.println("find_lower_right");
        double x1 = 75.0;
        double y1 = 100.0;
        double x2 = 125.0;
        double y2 = 45.0;
        double expResult = Math.toRadians(132.273689);
        double result = Angle.find(x1, y1, x2, y2);

        //System.out.println("Exp: " + expResult);
        //System.out.println("Res: " + result);
        assertEquals(expResult, result, 0.000001);
    }

}
