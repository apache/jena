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
package org.apache.jena.geosparql.implementation.parsers.wkt;

import org.apache.jena.geosparql.implementation.DimensionInfo;
import org.apache.jena.geosparql.implementation.jts.CoordinateSequenceDimensions;
import org.apache.jena.geosparql.implementation.jts.CustomCoordinateSequence;
import org.apache.jena.geosparql.implementation.jts.CustomGeometryFactory;
import org.junit.After;
import org.junit.AfterClass;
import static org.junit.Assert.assertEquals;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.LineString;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.geom.Polygon;

/**
 *
 *
 */
public class WKTReaderTest {

    private static final GeometryFactory GEOMETRY_FACTORY = CustomGeometryFactory.theInstance();

    public WKTReaderTest() {
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
     * Test of getDimensionInfo method, of class WKTReader.
     */
    @Test
    public void testGetDimensionInfo0() {

        WKTReader instance = new WKTReader("point", "", "");
        DimensionInfo expResult = new DimensionInfo(2, 2, 0);
        DimensionInfo result = instance.getDimensionInfo();

        //
        //
        assertEquals(expResult, result);
    }

    /**
     * Test of getDimensionInfo method, of class WKTReader.
     */
    @Test
    public void testGetDimensionInfo2() {

        WKTReader instance = new WKTReader("point", "", "(11.0 12.0)");
        DimensionInfo expResult = new DimensionInfo(2, 2, 0);
        DimensionInfo result = instance.getDimensionInfo();

        //
        //
        assertEquals(expResult, result);
    }

    /**
     * Test of getDimensionInfo method, of class WKTReader.
     */
    @Test
    public void testGetDimensionInfo3a() {

        WKTReader instance = new WKTReader("point", "z", "(11.0 12.0 7.0)");
        DimensionInfo expResult = new DimensionInfo(3, 3, 0);
        DimensionInfo result = instance.getDimensionInfo();

        //
        //
        assertEquals(expResult, result);
    }

    /**
     * Test of getDimensionInfo method, of class WKTReader.
     */
    @Test
    public void testGetDimensionInfo3b() {

        WKTReader instance = new WKTReader("point", "m", "(11.0 12.0 7.0)");
        DimensionInfo expResult = new DimensionInfo(3, 2, 0);
        DimensionInfo result = instance.getDimensionInfo();

        //
        //
        assertEquals(expResult, result);
    }

    /**
     * Test of getDimensionInfo method, of class WKTReader.
     */
    @Test
    public void testGetDimensionInfo4() {

        WKTReader instance = new WKTReader("point", "zm", "(11.0 12.0 7.0 5.0)");
        DimensionInfo expResult = new DimensionInfo(4, 3, 0);
        DimensionInfo result = instance.getDimensionInfo();

        //
        //
        assertEquals(expResult, result);
    }

    /**
     * Test of getGeometry method, of class WKTReader.
     */
    @Test
    public void testGetGeometryPoint() {

        WKTReader instance = new WKTReader("point", "", "(11.0 12.0)");
        Geometry expResult = GEOMETRY_FACTORY.createPoint(new CustomCoordinateSequence(CoordinateSequenceDimensions.XY, "11.0 12.0"));
        Geometry result = instance.getGeometry();

        //
        //
        assertEquals(expResult, result);
    }

    /**
     * Test of getGeometry method, of class WKTReader.
     */
    @Test
    public void testGetGeometryPointZ() {

        WKTReader instance = new WKTReader("point", "z", "(11.0 12.0 8.0)");
        Geometry expResult = GEOMETRY_FACTORY.createPoint(new CustomCoordinateSequence(CoordinateSequenceDimensions.XYZ, "11.0 12.0 8.0"));
        Geometry result = instance.getGeometry();

        //
        //
        assertEquals(expResult, result);
    }

    /**
     * Test of extract method, of class WKTReader.
     */
    @Test
    public void testExtractPoint2() {

        String wktText = "POINT (11.0 12.0)";
        WKTReader expResult = new WKTReader("point", "", "(11.0 12.0)");
        WKTReader result = WKTReader.extract(wktText);

        //
        //
        assertEquals(expResult, result);
    }

    /**
     * Test of extract method, of class WKTReader.
     */
    @Test
    public void testExtractPoint3() {

        String wktText = "POINT Z (11.0 12.0 8.0)";
        WKTReader expResult = new WKTReader("point", "z", "(11.0 12.0 8.0)");
        WKTReader result = WKTReader.extract(wktText);

        //
        //
        assertEquals(expResult, result);
    }

    /**
     * Test of extract method, of class WKTReader.
     */
    @Test
    public void testExtractPoint3b() {

        String wktText = "POINT M (11.0 12.0 5.0)";
        WKTReader expResult = new WKTReader("point", "m", "(11.0 12.0 5.0)");
        WKTReader result = WKTReader.extract(wktText);

        //
        //
        assertEquals(expResult, result);
    }

    /**
     * Test of extract method, of class WKTReader.
     */
    @Test
    public void testExtractPoint4() {

        String wktText = "POINT ZM (11.0 12.0 8.0 5.0)";
        WKTReader expResult = new WKTReader("point", "zm", "(11.0 12.0 8.0 5.0)");
        WKTReader result = WKTReader.extract(wktText);

        //
        //
        assertEquals(expResult, result);
    }

    /**
     * Test of extract method, of class WKTReader.
     */
    @Test
    public void testExtractPolygon() {

        String wktText = "POLYGON ((30 10, 40 40, 20 40, 10 20, 30 10))";
        WKTReader expResult = new WKTReader("polygon", "", "(30 10, 40 40, 20 40, 10 20, 30 10)");
        WKTReader result = WKTReader.extract(wktText);

        //
        //
        assertEquals(expResult, result);
    }

    /**
     * Test of extract method, of class WKTReader.
     */
    @Test
    public void testExtractPolygonHole() {

        String wktText = "POLYGON ((35 10, 45 45, 15 40, 10 20, 35 10),(20 30, 35 35, 30 20, 20 30))";
        WKTReader expResult = new WKTReader("polygon", "", "(35 10, 45 45, 15 40, 10 20, 35 10),(20 30, 35 35, 30 20, 20 30)");
        WKTReader result = WKTReader.extract(wktText);

        //
        //
        assertEquals(expResult, result);
    }

    /**
     * Test of extract method, of class WKTReader.
     */
    @Test
    public void testExtractLineString() {

        String wktText = "LINESTRING (30 10, 10 30, 40 40)";
        WKTReader expResult = new WKTReader("linestring", "", "(30 10, 10 30, 40 40)");
        WKTReader result = WKTReader.extract(wktText);

        //
        //
        assertEquals(expResult, result);
    }

    /**
     * Test of extract method, of class WKTReader.
     */
    @Test
    public void testExtractMultiPoint() {

        String wktText = "MULTIPOINT ((10 40), (40 30), (20 20), (30 10))";
        WKTReader expResult = new WKTReader("multipoint", "", "((10 40), (40 30), (20 20), (30 10))");
        WKTReader result = WKTReader.extract(wktText);

        //
        //
        assertEquals(expResult, result);
    }

    /**
     * Test of extract method, of class WKTReader.
     */
    @Test
    public void testExtractMultiPoint2() {

        String wktText = "MULTIPOINT (10 40, 40 30, 20 20, 30 10)";
        WKTReader expResult = new WKTReader("multipoint", "", "(10 40, 40 30, 20 20, 30 10)");
        WKTReader result = WKTReader.extract(wktText);

        //
        //
        assertEquals(expResult, result);
    }

    /**
     * Test of extract method, of class WKTReader.
     */
    @Test
    public void testExtractMutliLineString() {

        String wktText = "MULTILINESTRING ((10 10, 20 20, 10 40),(40 40, 30 30, 40 20, 30 10))";
        WKTReader expResult = new WKTReader("multilinestring", "", "((10 10, 20 20, 10 40),(40 40, 30 30, 40 20, 30 10))");
        WKTReader result = WKTReader.extract(wktText);

        //
        //
        assertEquals(expResult, result);
    }

    /**
     * Test of extract method, of class WKTReader.
     */
    @Test
    public void testExtractMultiPolygon() {

        String wktText = "MULTIPOLYGON (((30 20, 45 40, 10 40, 30 20)),((15 5, 40 10, 10 20, 5 10, 15 5)))";
        WKTReader expResult = new WKTReader("multipolygon", "", "(((30 20, 45 40, 10 40, 30 20)),((15 5, 40 10, 10 20, 5 10, 15 5)))");
        WKTReader result = WKTReader.extract(wktText);

        //
        //
        assertEquals(expResult, result);
    }

    /**
     * Test of extract method, of class WKTReader.
     */
    @Test
    public void testExtractMultiPolygon2() {

        String wktText = "MULTIPOLYGON (((40 40, 20 45, 45 30, 40 40)),((20 35, 10 30, 10 10, 30 5, 45 20, 20 35),(30 20, 20 15, 20 25, 30 20)))";
        WKTReader expResult = new WKTReader("multipolygon", "", "(((40 40, 20 45, 45 30, 40 40)),((20 35, 10 30, 10 10, 30 5, 45 20, 20 35),(30 20, 20 15, 20 25, 30 20)))");
        WKTReader result = WKTReader.extract(wktText);

        //
        //
        assertEquals(expResult, result);
    }

    /**
     * Test of extract method, of class WKTReader.
     */
    @Test
    public void testExtractGeometryCollection() {

        String wktText = "GEOMETRYCOLLECTION(POINT(4 6),LINESTRING(4 6,7 10), MULTIPOINT((6 8),(2 3)))";
        WKTReader expResult = new WKTReader("geometrycollection", "", "(POINT(4 6),LINESTRING(4 6,7 10), MULTIPOINT((6 8),(2 3)))");
        WKTReader result = WKTReader.extract(wktText);

        //
        //
        assertEquals(expResult, result);
    }

    /**
     * Test of buildPointEmpty method, of class WKTReader.
     */
    @Test
    public void testBuildPointEmpty() {

        WKTReader instance = WKTReader.extract("POINT EMPTY");
        Geometry result = instance.getGeometry();

        CustomCoordinateSequence pointSequence = new CustomCoordinateSequence(CoordinateSequenceDimensions.XY, "");
        Geometry expResult = GEOMETRY_FACTORY.createPoint(pointSequence);

        //
        //
        assertEquals(expResult, result);
    }

    /**
     * Test of buildLineStringEmpty method, of class WKTReader.
     */
    @Test
    public void testBuildLineStringEmpty() {

        WKTReader instance = WKTReader.extract("LINESTRING EMPTY");
        Geometry result = instance.getGeometry();

        CustomCoordinateSequence pointSequence = new CustomCoordinateSequence(CoordinateSequenceDimensions.XY, "");
        Geometry expResult = GEOMETRY_FACTORY.createLineString(pointSequence);

        //
        //
        assertEquals(expResult, result);
    }

    /**
     * Test of buildPolygonEmpty method, of class WKTReader.
     */
    @Test
    public void testBuildPolygonEmpty() {

        WKTReader instance = WKTReader.extract("POLYGON EMPTY");
        Geometry result = instance.getGeometry();

        CustomCoordinateSequence pointSequence = new CustomCoordinateSequence(CoordinateSequenceDimensions.XY, "");
        Geometry expResult = GEOMETRY_FACTORY.createPolygon(pointSequence);

        //
        //
        assertEquals(expResult, result);
    }

    /**
     * Test of buildMultiPointEmpty method, of class WKTReader.
     */
    @Test
    public void testBuildMultiPointEmpty() {

        WKTReader instance = WKTReader.extract("MULTIPOINT EMPTY");
        Geometry result = instance.getGeometry();

        Geometry expResult = GEOMETRY_FACTORY.createMultiPoint(new Point[0]);

        //
        //
        assertEquals(expResult, result);
    }

    /**
     * Test of buildMultiLineString method, of class WKTReader.
     */
    @Test
    public void testBuildMultiLineStringEmpty() {

        WKTReader instance = WKTReader.extract("MULTILINESTRING EMPTY");
        Geometry result = instance.getGeometry();

        Geometry expResult = GEOMETRY_FACTORY.createMultiLineString(new LineString[0]);

        //
        //
        assertEquals(expResult, result);
    }

    /**
     * Test of buildMultiPolygonEmpty method, of class WKTReader.
     */
    @Test
    public void testBuildMultiPolygonEmpty() {

        WKTReader instance = WKTReader.extract("MULTIPOLYGON EMPTY");
        Geometry result = instance.getGeometry();

        Geometry expResult = GEOMETRY_FACTORY.createMultiPolygon(new Polygon[0]);

        //
        //
        assertEquals(expResult, result);
    }

    /**
     * Test of buildGeometryCollectionEmpty method, of class WKTReader.
     */
    @Test
    public void testBuildGeometryCollectionEmpty() {

        WKTReader instance = WKTReader.extract("GEOMETRYCOLLECTION EMPTY");
        Geometry result = instance.getGeometry();

        Geometry expResult = GEOMETRY_FACTORY.createGeometryCollection(new Geometry[0]);

        //
        //
        assertEquals(expResult, result);
    }

    /**
     * Test of buildEmpty method, of class WKTReader.<br>
     * Req 13 An empty RDFS Literal of type geo:wktLiteral shall be interpreted
     * as an empty geometry.
     */
    @Test
    public void testBuildEmpty() {

        WKTReader instance = WKTReader.extract("");
        Geometry result = instance.getGeometry();

        CustomCoordinateSequence pointSequence = new CustomCoordinateSequence(CoordinateSequenceDimensions.XY, "");
        Geometry expResult = GEOMETRY_FACTORY.createPoint(pointSequence);

        //
        //
        assertEquals(expResult, result);
    }

}
