/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 *
 *   SPDX-License-Identifier: Apache-2.0
 */
package org.apache.jena.geosparql.implementation;

import java.util.Arrays;
import java.util.List;
import org.apache.jena.geosparql.implementation.jts.CoordinateSequenceDimensions;
import org.apache.jena.geosparql.implementation.jts.CustomCoordinateSequence;
import org.apache.jena.geosparql.implementation.jts.CustomGeometryFactory;
import org.apache.jena.geosparql.implementation.parsers.wkt.WKTReader;
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

        Coordinate coordinate = new CoordinateXY(1.0, 2.0);
        GeometryFactory factory = new GeometryFactory();
        Geometry geometry = factory.createPoint(coordinate);
        DimensionInfo expResult = DimensionInfo.XY_POINT;
        DimensionInfo result = DimensionInfo.find(coordinate, geometry);
        assertEquals(expResult, result);
    }

    /**
     * Test of find method, of class DimensionInfo.
     */
    @Test
    public void testFind_List_Geometry() {

        List<Coordinate> coordinates = Arrays.asList(new CoordinateXY(1.0, 2.0), new CoordinateXY(10.0, 20.0));
        GeometryFactory factory = new GeometryFactory();
        Geometry geometry = factory.createLineString(coordinates.toArray(new Coordinate[coordinates.size()]));
        DimensionInfo expResult = DimensionInfo.XY_LINESTRING;
        DimensionInfo result = DimensionInfo.find(coordinates, geometry);
        assertEquals(expResult, result);
    }

    /**
     * Test of findCollection method, of class DimensionInfo.
     */
    @Test
    public void testFindCollection() {

        GeometryFactory factory = new GeometryFactory();
        List<Point> points = Arrays.asList(factory.createPoint(new Coordinate(1.0, 2.0)), factory.createPoint(new Coordinate(10.0, 20.0)));

        Geometry geometry = factory.createMultiPoint(points.toArray(new Point[points.size()]));
        DimensionInfo expResult = DimensionInfo.XY_POINT;
        DimensionInfo result = DimensionInfo.findCollection(points, geometry);
        assertEquals(expResult, result);
    }

    @Test
    public void testFindGeometryPreservesEmptyCoordinateSequenceLayout() {
        GeometryFactory factory = CustomGeometryFactory.theInstance();
        Geometry[] geometries = {
            factory.createPoint(new CustomCoordinateSequence(CoordinateSequenceDimensions.XYZ)),
            factory.createLineString(new CustomCoordinateSequence(CoordinateSequenceDimensions.XYM)),
            factory.createPolygon(new CustomCoordinateSequence(CoordinateSequenceDimensions.XYZM))
        };
        CoordinateSequenceDimensions[] expected = {
            CoordinateSequenceDimensions.XYZ,
            CoordinateSequenceDimensions.XYM,
            CoordinateSequenceDimensions.XYZM
        };

        for (int i = 0; i < geometries.length; i++) {
            assertEquals(geometries[i].getGeometryType(), expected[i],
                    DimensionInfo.find(geometries[i], CoordinateSequenceDimensions.XY).getDimensions());
        }
    }

    @Test
    public void testFindGeometryUsesLayoutRetainedOnEmptyAggregate() {
        String[] wkts = {
            "MULTIPOINT Z EMPTY", "MULTILINESTRING M EMPTY",
            "MULTIPOLYGON ZM EMPTY", "GEOMETRYCOLLECTION Z EMPTY"
        };
        CoordinateSequenceDimensions[] expected = {
            CoordinateSequenceDimensions.XYZ,
            CoordinateSequenceDimensions.XYM,
            CoordinateSequenceDimensions.XYZM,
            CoordinateSequenceDimensions.XYZ
        };

        for (int i = 0; i < wkts.length; i++) {
            Geometry geometry = WKTReader.extract(wkts[i]).getGeometry();
            assertEquals(wkts[i], expected[i],
                    DimensionInfo.find(geometry, CoordinateSequenceDimensions.XY).getDimensions());
        }
    }

    @Test
    public void testFindGeometryUsesFirstCollectionMemberLayout() {
        GeometryFactory factory = CustomGeometryFactory.theInstance();
        Point measured = factory.createPoint(new CustomCoordinateSequence(CoordinateSequenceDimensions.XYM, "1 2 3"));
        Point elevated = factory.createPoint(new CustomCoordinateSequence(CoordinateSequenceDimensions.XYZ, "4 5 6"));
        Geometry collection = factory.createGeometryCollection(new Geometry[] { measured, elevated });

        assertEquals(CoordinateSequenceDimensions.XYM,
                DimensionInfo.find(collection, CoordinateSequenceDimensions.XY).getDimensions());
        assertEquals(CoordinateSequenceDimensions.XYZ,
                DimensionInfo.find(elevated, CoordinateSequenceDimensions.XY).getDimensions());
    }

}
