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
package org.apache.jena.geosparql.implementation;

import java.util.Arrays;
import java.util.List;
import org.junit.After;
import org.junit.AfterClass;
import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.CoordinateXY;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;

/**
 *
 *
 */
public class DimensionInfoTest {

    public DimensionInfoTest() {
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
     * Test of find method, of class DimensionInfo.
     */
    @Test
    public void testFind_Coordinate_Geometry() {
        System.out.println("find");
        Coordinate coordinate = new CoordinateXY(1.0, 2.0);
        GeometryFactory factory = new GeometryFactory();
        Geometry geometry = factory.createPoint(coordinate);
        DimensionInfo expResult = DimensionInfo.XY_POINT;
        DimensionInfo result = DimensionInfo.find(coordinate, geometry);

        //System.out.println("Exp: " + expResult);
        //System.out.println("Res: " + result);
        assertEquals(expResult, result);
    }

    /**
     * Test of find method, of class DimensionInfo.
     */
    @Test
    public void testFind_List_Geometry() {
        System.out.println("find");
        List<Coordinate> coordinates = Arrays.asList(new CoordinateXY(1.0, 2.0), new CoordinateXY(10.0, 20.0));
        GeometryFactory factory = new GeometryFactory();
        Geometry geometry = factory.createLineString(coordinates.toArray(new Coordinate[coordinates.size()]));
        DimensionInfo expResult = DimensionInfo.XY_LINESTRING;
        DimensionInfo result = DimensionInfo.find(coordinates, geometry);

        //System.out.println("Exp: " + expResult);
        //System.out.println("Res: " + result);
        assertEquals(expResult, result);
    }

    /**
     * Test of findCollection method, of class DimensionInfo.
     */
    @Test
    public void testFindCollection() {
        System.out.println("findCollection");
        GeometryFactory factory = new GeometryFactory();
        List<Point> points = Arrays.asList(factory.createPoint(new Coordinate(1.0, 2.0)), factory.createPoint(new Coordinate(10.0, 20.0)));

        Geometry geometry = factory.createMultiPoint(points.toArray(new Point[points.size()]));
        DimensionInfo expResult = DimensionInfo.XY_POINT;
        DimensionInfo result = DimensionInfo.findCollection(points, geometry);

        //System.out.println("Exp: " + expResult);
        //System.out.println("Res: " + result);
        assertEquals(expResult, result);
    }

}
