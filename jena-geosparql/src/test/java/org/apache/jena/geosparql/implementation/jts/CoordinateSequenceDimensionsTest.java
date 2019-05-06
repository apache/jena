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
package org.apache.jena.geosparql.implementation.jts;

import org.junit.After;
import org.junit.AfterClass;
import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.CoordinateXY;
import org.locationtech.jts.geom.CoordinateXYM;
import org.locationtech.jts.geom.CoordinateXYZM;

/**
 *
 *
 */
public class CoordinateSequenceDimensionsTest {

    public CoordinateSequenceDimensionsTest() {
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
     * Test of find method, of class CoordinateSequenceDimensions.
     */
    @Test
    public void testFind_xy() {

        Coordinate coordinate = new Coordinate(1.0, 2.0);
        CoordinateSequenceDimensions expResult = CoordinateSequenceDimensions.XY;
        CoordinateSequenceDimensions result = CoordinateSequenceDimensions.find(coordinate);
        assertEquals(expResult, result);
    }

    /**
     * Test of find method, of class CoordinateSequenceDimensions.
     */
    @Test
    public void testFind_xy2() {

        Coordinate coordinate = new CoordinateXY(1.0, 2.0);
        CoordinateSequenceDimensions expResult = CoordinateSequenceDimensions.XY;
        CoordinateSequenceDimensions result = CoordinateSequenceDimensions.find(coordinate);
        assertEquals(expResult, result);
    }

    /**
     * Test of find method, of class CoordinateSequenceDimensions.
     */
    @Test
    public void testFind_xyz() {

        Coordinate coordinate = new Coordinate(1.0, 2.0, 3.0);
        CoordinateSequenceDimensions expResult = CoordinateSequenceDimensions.XYZ;
        CoordinateSequenceDimensions result = CoordinateSequenceDimensions.find(coordinate);
        assertEquals(expResult, result);
    }

    /**
     * Test of find method, of class CoordinateSequenceDimensions.
     */
    @Test
    public void testFind_xym() {

        Coordinate coordinate = new CoordinateXYM(1.0, 2.0, 3.0);
        CoordinateSequenceDimensions expResult = CoordinateSequenceDimensions.XYM;
        CoordinateSequenceDimensions result = CoordinateSequenceDimensions.find(coordinate);
        assertEquals(expResult, result);
    }

    /**
     * Test of find method, of class CoordinateSequenceDimensions.
     */
    @Test
    public void testFind_xyzm() {

        Coordinate coordinate = new CoordinateXYZM(1.0, 2.0, 3.0, 4.0);
        CoordinateSequenceDimensions expResult = CoordinateSequenceDimensions.XYZM;
        CoordinateSequenceDimensions result = CoordinateSequenceDimensions.find(coordinate);
        assertEquals(expResult, result);
    }

}
