/*
 * Copyright 2018 the original author or authors.
 * See the notice.md file distributed with this work for additional
 * information regarding copyright ownership.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.jena.geosparql.implementation.parsers.wkt;

import org.apache.jena.geosparql.implementation.parsers.wkt.WKTWriter;
import org.apache.jena.geosparql.implementation.DimensionInfo;
import org.apache.jena.geosparql.implementation.GeometryWrapper;
import org.apache.jena.geosparql.implementation.datatype.WKTDatatype;
import org.apache.jena.geosparql.implementation.jts.CoordinateSequenceDimensions;
import org.apache.jena.geosparql.implementation.jts.CustomCoordinateSequence;
import org.apache.jena.geosparql.implementation.jts.CustomGeometryFactory;
import org.apache.jena.geosparql.implementation.vocabulary.SRS_URI;
import org.junit.After;
import org.junit.AfterClass;
import static org.junit.Assert.assertEquals;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.LineString;
import org.locationtech.jts.geom.LinearRing;
import org.locationtech.jts.geom.Polygon;

/**
 *
 *
 */
public class WKTWriterTest {

    public WKTWriterTest() {
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

    private static final GeometryFactory GEOMETRY_FACTORY = CustomGeometryFactory.theInstance();

    /**
     * Test of write method, of class WKTWriter.
     */
    @Test
    public void testWritePoint() {
        System.out.println("writePoint");
        Geometry geometry = GEOMETRY_FACTORY.createPoint(new CustomCoordinateSequence(CoordinateSequenceDimensions.XY, "11.0 12.1"));

        GeometryWrapper geometryWrapper = new GeometryWrapper(geometry, SRS_URI.WGS84_CRS, WKTDatatype.URI, new DimensionInfo(2, 2, 0));

        String expResult = "<" + SRS_URI.WGS84_CRS + "> POINT(11 12.1)";
        String result = WKTWriter.write(geometryWrapper);

        //System.out.println("Expected: " + expResult);
        //System.out.println("Result: " + result);
        assertEquals(expResult, result);
    }

    /**
     * Test of write method, of class WKTWriter.
     */
    @Test
    public void testWritePoint2() {
        System.out.println("writePoint2");
        Geometry geometry = GEOMETRY_FACTORY.createPoint(new CustomCoordinateSequence(CoordinateSequenceDimensions.XYZM, "11.0 12.1 8.0 5.0"));

        GeometryWrapper geometryWrapper = new GeometryWrapper(geometry, SRS_URI.WGS84_CRS, WKTDatatype.URI, new DimensionInfo(4, 3, 0));

        String expResult = "<" + SRS_URI.WGS84_CRS + "> POINT ZM(11 12.1 8 5)";
        String result = WKTWriter.write(geometryWrapper);

        //System.out.println("Expected: " + expResult);
        //System.out.println("Result: " + result);
        assertEquals(expResult, result);
    }

    /**
     * Test of write method, of class WKTWriter.
     */
    @Test
    public void testWriteLineString() {
        System.out.println("writeLineString");
        Geometry geometry = GEOMETRY_FACTORY.createLineString(new CustomCoordinateSequence(CoordinateSequenceDimensions.XY, "11.0 12.1, 3 4"));

        GeometryWrapper geometryWrapper = new GeometryWrapper(geometry, SRS_URI.WGS84_CRS, WKTDatatype.URI, new DimensionInfo(2, 2, 1));

        String expResult = "<" + SRS_URI.WGS84_CRS + "> LINESTRING(11 12.1, 3 4)";
        String result = WKTWriter.write(geometryWrapper);

        //System.out.println("Expected: " + expResult);
        //System.out.println("Result: " + result);
        assertEquals(expResult, result);
    }

    /**
     * Test of write method, of class WKTWriter.
     */
    @Test
    public void testWriteLineString2() {
        System.out.println("writeLineString2");
        Geometry geometry = GEOMETRY_FACTORY.createLineString(new CustomCoordinateSequence(CoordinateSequenceDimensions.XYZM, "11.0 12.1 8.0 5.0, 3 4 6 2"));

        GeometryWrapper geometryWrapper = new GeometryWrapper(geometry, SRS_URI.WGS84_CRS, WKTDatatype.URI, new DimensionInfo(4, 3, 1));

        String expResult = "<" + SRS_URI.WGS84_CRS + "> LINESTRING ZM(11 12.1 8 5, 3 4 6 2)";
        String result = WKTWriter.write(geometryWrapper);

        //System.out.println("Expected: " + expResult);
        //System.out.println("Result: " + result);
        assertEquals(expResult, result);
    }

    /**
     * Test of write method, of class WKTWriter.
     */
    @Test
    public void testWritePolygon() {
        System.out.println("writePolygon");
        Geometry geometry = GEOMETRY_FACTORY.createPolygon(new CustomCoordinateSequence(CoordinateSequenceDimensions.XY, "30 10, 40 40, 20 40, 10 20, 30 10"));

        GeometryWrapper geometryWrapper = new GeometryWrapper(geometry, SRS_URI.WGS84_CRS, WKTDatatype.URI, new DimensionInfo(2, 2, 2));

        String expResult = "<" + SRS_URI.WGS84_CRS + "> POLYGON((30 10, 40 40, 20 40, 10 20, 30 10))";
        String result = WKTWriter.write(geometryWrapper);

        //System.out.println("Expected: " + expResult);
        //System.out.println("Result: " + result);
        assertEquals(expResult, result);
    }

    /**
     * Test of write method, of class WKTWriter.
     */
    @Test
    public void testWritePolygon2() {
        System.out.println("writePolygon2");
        Geometry geometry = GEOMETRY_FACTORY.createPolygon(new CustomCoordinateSequence(CoordinateSequenceDimensions.XYZM, "30 10 0 1, 40 40 0 1, 20 40 0 1, 10 20 0 1, 30 10 0 1"));

        GeometryWrapper geometryWrapper = new GeometryWrapper(geometry, SRS_URI.WGS84_CRS, WKTDatatype.URI, new DimensionInfo(4, 3, 2));

        String expResult = "<" + SRS_URI.WGS84_CRS + "> POLYGON ZM((30 10 0 1, 40 40 0 1, 20 40 0 1, 10 20 0 1, 30 10 0 1))";
        String result = WKTWriter.write(geometryWrapper);

        //System.out.println("Expected: " + expResult);
        //System.out.println("Result: " + result);
        assertEquals(expResult, result);
    }

    /**
     * Test of write method, of class WKTWriter.
     */
    @Test
    public void testWritePolygon3() {
        System.out.println("writePolygon3");
        LinearRing shell = GEOMETRY_FACTORY.createLinearRing(new CustomCoordinateSequence(CoordinateSequenceDimensions.XYZM, "30 10 0 1, 40 40 0 1, 20 40 0 1, 10 20 0 1, 30 10 0 1"));
        LinearRing[] holes = new LinearRing[]{GEOMETRY_FACTORY.createLinearRing(new CustomCoordinateSequence(CoordinateSequenceDimensions.XYZM, "20 30 0 1, 35 35 0 1, 30 20 0 1, 20 30 0 1"))};
        Geometry geometry = GEOMETRY_FACTORY.createPolygon(shell, holes);

        GeometryWrapper geometryWrapper = new GeometryWrapper(geometry, SRS_URI.WGS84_CRS, WKTDatatype.URI, new DimensionInfo(4, 3, 2));

        String expResult = "<" + SRS_URI.WGS84_CRS + "> POLYGON ZM((30 10 0 1, 40 40 0 1, 20 40 0 1, 10 20 0 1, 30 10 0 1), (20 30 0 1, 35 35 0 1, 30 20 0 1, 20 30 0 1))";
        String result = WKTWriter.write(geometryWrapper);

        //System.out.println("Expected: " + expResult);
        //System.out.println("Result: " + result);
        assertEquals(expResult, result);
    }

    /**
     * Test of write method, of class WKTWriter.
     */
    @Test
    public void testWriteMultiPoint() {
        System.out.println("writeMultiPoint");
        Geometry geometry = GEOMETRY_FACTORY.createMultiPoint(new CustomCoordinateSequence(CoordinateSequenceDimensions.XYZM, "10 40 0 1, 40 30 0 1, 20 20 0 1, 30 10 0 1"));

        GeometryWrapper geometryWrapper = new GeometryWrapper(geometry, SRS_URI.WGS84_CRS, WKTDatatype.URI, new DimensionInfo(4, 3, 0));

        String expResult = "<" + SRS_URI.WGS84_CRS + "> MULTIPOINT ZM((10 40 0 1), (40 30 0 1), (20 20 0 1), (30 10 0 1))";
        String result = WKTWriter.write(geometryWrapper);

        //System.out.println("Expected: " + expResult);
        //System.out.println("Result: " + result);
        assertEquals(expResult, result);
    }

    /**
     * Test of write method, of class WKTWriter.
     */
    @Test
    public void testWriteMultiLineString() {
        System.out.println("writeMultiLineString");

        LineString[] lineStrings = new LineString[2];
        lineStrings[0] = GEOMETRY_FACTORY.createLineString(new CustomCoordinateSequence(CoordinateSequenceDimensions.XYZM, "10 10 0 1, 20 20 0 1, 10 40 0 1"));
        lineStrings[1] = GEOMETRY_FACTORY.createLineString(new CustomCoordinateSequence(CoordinateSequenceDimensions.XYZM, "40 40 0 1, 30 30 0 1, 40 20 0 1, 30 10 0 1"));
        Geometry geometry = GEOMETRY_FACTORY.createMultiLineString(lineStrings);

        GeometryWrapper geometryWrapper = new GeometryWrapper(geometry, SRS_URI.WGS84_CRS, WKTDatatype.URI, new DimensionInfo(4, 3, 1));

        String expResult = "<" + SRS_URI.WGS84_CRS + "> MULTILINESTRING ZM((10 10 0 1, 20 20 0 1, 10 40 0 1), (40 40 0 1, 30 30 0 1, 40 20 0 1, 30 10 0 1))";
        String result = WKTWriter.write(geometryWrapper);

        //System.out.println("Expected: " + expResult);
        //System.out.println("Result: " + result);
        assertEquals(expResult, result);
    }

    /**
     * Test of extract method, of class WKTWriter.
     */
    @Test
    public void testWriteMultiPolygon() {
        System.out.println("writeMultiPolygon");

        Polygon[] polygons = new Polygon[2];
        polygons[0] = GEOMETRY_FACTORY.createPolygon(new CustomCoordinateSequence(CoordinateSequenceDimensions.XY, "40 40, 20 45, 45 30, 40 40"));
        polygons[1] = GEOMETRY_FACTORY.createPolygon(new CustomCoordinateSequence(CoordinateSequenceDimensions.XY, "20 35, 10 30, 10 10, 30 5, 45 20, 20 35"));
        Geometry geometry = GEOMETRY_FACTORY.createMultiPolygon(polygons);

        GeometryWrapper geometryWrapper = new GeometryWrapper(geometry, SRS_URI.WGS84_CRS, WKTDatatype.URI, new DimensionInfo(2, 2, 2));

        String expResult = "<" + SRS_URI.WGS84_CRS + "> MULTIPOLYGON(((40 40, 20 45, 45 30, 40 40)), ((20 35, 10 30, 10 10, 30 5, 45 20, 20 35)))";
        String result = WKTWriter.write(geometryWrapper);

        //System.out.println("Expected: " + expResult);
        //System.out.println("Result: " + result);
        assertEquals(expResult, result);
    }

    /**
     * Test of write method, of class WKTWriter.
     */
    @Test
    public void testWriteMultiPolygon2() {
        System.out.println("writeMultiPolygon2");

        Polygon[] polygons = new Polygon[2];
        polygons[0] = GEOMETRY_FACTORY.createPolygon(new CustomCoordinateSequence(CoordinateSequenceDimensions.XYZM, "40 40 0 1, 20 45 0 1, 45 30 0 1, 40 40 0 1"));
        LinearRing shell = GEOMETRY_FACTORY.createLinearRing(new CustomCoordinateSequence(CoordinateSequenceDimensions.XYZM, "20 35 0 1, 10 30 0 1, 10 10 0 1, 30 5 0 1, 45 20 0 1, 20 35 0 1"));
        LinearRing[] holes = new LinearRing[]{GEOMETRY_FACTORY.createLinearRing(new CustomCoordinateSequence(CoordinateSequenceDimensions.XYZM, "30 20 0 1, 20 15 0 1, 20 25 0 1, 30 20 0 1"))};
        polygons[1] = GEOMETRY_FACTORY.createPolygon(shell, holes);
        Geometry geometry = GEOMETRY_FACTORY.createMultiPolygon(polygons);

        GeometryWrapper geometryWrapper = new GeometryWrapper(geometry, SRS_URI.WGS84_CRS, WKTDatatype.URI, new DimensionInfo(4, 3, 2));

        String expResult = "<" + SRS_URI.WGS84_CRS + "> MULTIPOLYGON ZM(((40 40 0 1, 20 45 0 1, 45 30 0 1, 40 40 0 1)), ((20 35 0 1, 10 30 0 1, 10 10 0 1, 30 5 0 1, 45 20 0 1, 20 35 0 1), (30 20 0 1, 20 15 0 1, 20 25 0 1, 30 20 0 1)))";
        String result = WKTWriter.write(geometryWrapper);

        //System.out.println("Expected: " + expResult);
        //System.out.println("Result: " + result);
        assertEquals(expResult, result);
    }

    /**
     * Test of write method, of class WKTWriter.
     */
    @Test
    public void testWriteGeometryCollection() {
        System.out.println("writeGeometryCollection");

        Geometry[] geometries = new Geometry[2];
        geometries[0] = GEOMETRY_FACTORY.createPoint(new CustomCoordinateSequence(CoordinateSequenceDimensions.XYZM, "4 6 0 1"));
        geometries[1] = GEOMETRY_FACTORY.createLineString(new CustomCoordinateSequence(CoordinateSequenceDimensions.XYZM, "4 6 0 1,7 10 0 1"));
        Geometry geometry = GEOMETRY_FACTORY.createGeometryCollection(geometries);

        GeometryWrapper geometryWrapper = new GeometryWrapper(geometry, SRS_URI.WGS84_CRS, WKTDatatype.URI, new DimensionInfo(4, 3, 1));

        String expResult = "<" + SRS_URI.WGS84_CRS + "> GEOMETRYCOLLECTION ZM(POINT ZM(4 6 0 1), LINESTRING ZM(4 6 0 1, 7 10 0 1))";
        String result = WKTWriter.write(geometryWrapper);

        //System.out.println("Expected: " + expResult);
        //System.out.println("Result: " + result);
        assertEquals(expResult, result);
    }

    /**
     * Test of writePointEmpty method, of class WKTWriter.
     */
    @Test
    public void testWritePointEmpty() {
        System.out.println("writePointEmpty");

        Geometry geometry = GEOMETRY_FACTORY.createPoint();
        GeometryWrapper geometryWrapper = new GeometryWrapper(geometry, SRS_URI.DEFAULT_WKT_CRS84, WKTDatatype.URI, new DimensionInfo(2, 2, 0));
        String result = WKTWriter.write(geometryWrapper);
        String expResult = "POINT EMPTY";

        //System.out.println("Expected: " + expResult);
        //System.out.println("Result: " + result);
        assertEquals(expResult, result);
    }

    /**
     * Test of writeLineStringEmpty method, of class WKTWriter.
     */
    @Test
    public void testWriteLineStringEmpty() {
        System.out.println("writeLineEmpty");
        Geometry geometry = GEOMETRY_FACTORY.createLineString();
        GeometryWrapper geometryWrapper = new GeometryWrapper(geometry, SRS_URI.DEFAULT_WKT_CRS84, WKTDatatype.URI, new DimensionInfo(2, 2, 1));
        String result = WKTWriter.write(geometryWrapper);
        String expResult = "LINESTRING EMPTY";

        //System.out.println("Expected: " + expResult);
        //System.out.println("Result: " + result);
        assertEquals(expResult, result);
    }

    /**
     * Test of writePolygonEmpty method, of class WKTWriter.
     */
    @Test
    public void testWritePolygonEmpty() {
        System.out.println("writePolygonEmpty");
        Geometry geometry = GEOMETRY_FACTORY.createPolygon();
        GeometryWrapper geometryWrapper = new GeometryWrapper(geometry, SRS_URI.DEFAULT_WKT_CRS84, WKTDatatype.URI, new DimensionInfo(2, 2, 2));
        String result = WKTWriter.write(geometryWrapper);
        String expResult = "POLYGON EMPTY";

        //System.out.println("Expected: " + expResult);
        //System.out.println("Result: " + result);
        assertEquals(expResult, result);
    }

    /**
     * Test of writeMultiPointEmpty method, of class WKTWriter.
     */
    @Test
    public void testWriteMultiPointEmpty() {
        System.out.println("writeMultiPointEmpty");
        Geometry geometry = GEOMETRY_FACTORY.createMultiPoint();
        GeometryWrapper geometryWrapper = new GeometryWrapper(geometry, SRS_URI.DEFAULT_WKT_CRS84, WKTDatatype.URI, new DimensionInfo(2, 2, 0));
        String result = WKTWriter.write(geometryWrapper);
        String expResult = "MULTIPOINT EMPTY";

        //System.out.println("Expected: " + expResult);
        //System.out.println("Result: " + result);
        assertEquals(expResult, result);
    }

    /**
     * Test of writeMultiLineString method, of class WKTWriter.
     */
    @Test
    public void testWriteMultiLineStringEmpty() {
        System.out.println("writeMultiLineStringEmpty");

        Geometry geometry = GEOMETRY_FACTORY.createMultiLineString();
        GeometryWrapper geometryWrapper = new GeometryWrapper(geometry, SRS_URI.DEFAULT_WKT_CRS84, WKTDatatype.URI, new DimensionInfo(2, 2, 1));
        String result = WKTWriter.write(geometryWrapper);
        String expResult = "MULTILINESTRING EMPTY";

        //System.out.println("Expected: " + expResult);
        //System.out.println("Result: " + result);
        assertEquals(expResult, result);
    }

    /**
     * Test of writeMultiPolygonEmpty method, of class WKTWriter.
     */
    @Test
    public void testWriteMultiPolygonEmpty() {
        System.out.println("writeMultiPolygonEmpty");

        Geometry geometry = GEOMETRY_FACTORY.createMultiPolygon();
        GeometryWrapper geometryWrapper = new GeometryWrapper(geometry, SRS_URI.DEFAULT_WKT_CRS84, WKTDatatype.URI, new DimensionInfo(2, 2, 2));
        String result = WKTWriter.write(geometryWrapper);
        String expResult = "MULTIPOLYGON EMPTY";

        //System.out.println("Expected: " + expResult);
        //System.out.println("Result: " + result);
        assertEquals(expResult, result);
    }

    /**
     * Test of writeGeometryCollectionEmpty method, of class WKTWriter.
     */
    @Test
    public void testWriteGeometryCollectionEmpty() {
        System.out.println("writeGeometryCollectionEmpty");

        Geometry geometry = GEOMETRY_FACTORY.createGeometryCollection();
        GeometryWrapper geometryWrapper = new GeometryWrapper(geometry, SRS_URI.DEFAULT_WKT_CRS84, WKTDatatype.URI, new DimensionInfo(2, 2, 2));
        String result = WKTWriter.write(geometryWrapper);
        String expResult = "GEOMETRYCOLLECTION EMPTY";

        //System.out.println("Expected: " + expResult);
        //System.out.println("Result: " + result);
        assertEquals(expResult, result);
    }

}
