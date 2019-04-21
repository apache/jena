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
package org.apache.jena.geosparql.implementation.datatype;

import org.apache.jena.geosparql.implementation.datatype.WKTDatatype;
import org.apache.jena.geosparql.configuration.GeoSPARQLConfig;
import org.apache.jena.geosparql.implementation.DimensionInfo;
import org.apache.jena.geosparql.implementation.GeometryWrapper;
import org.apache.jena.geosparql.implementation.jts.CoordinateSequenceDimensions;
import org.apache.jena.geosparql.implementation.jts.CustomCoordinateSequence;
import org.apache.jena.geosparql.implementation.jts.CustomGeometryFactory;
import org.apache.jena.geosparql.implementation.vocabulary.SRS_URI;
import static org.hamcrest.CoreMatchers.not;
import org.junit.After;
import org.junit.AfterClass;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.LineString;
import org.locationtech.jts.geom.LinearRing;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.geom.Polygon;

/**
 *
 *
 */
public class WKTDatatypeTest {

    public WKTDatatypeTest() {
    }

    @BeforeClass
    public static void setUpClass() {
        GeoSPARQLConfig.setupNoIndex();
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
    private static final WKTDatatype WKT_DATATYPE = WKTDatatype.INSTANCE;

    /**
     * Test of unparse method, of class WKTDatatype.
     */
    @Test
    public void testUnparse() {
        System.out.println("unparse");

        String expResult = "POINT(-83.38 33.95)";

        Coordinate coord = new Coordinate(-83.38, 33.95);
        Point point = GEOMETRY_FACTORY.createPoint(coord);
        String srsURI = "http://www.opengis.net/def/crs/OGC/1.3/CRS84";

        DimensionInfo dimensionInfo = new DimensionInfo(2, 2, 0);

        GeometryWrapper geometry = new GeometryWrapper(point, srsURI, WKTDatatype.URI, dimensionInfo);

        String result = WKT_DATATYPE.unparse(geometry);

        //System.out.println("Expected: " + expResult);
        //System.out.println("Result: " + result);
        assertEquals(expResult, result);

    }

    /**
     * Test of unparse method, of class WKTDatatype.
     */
    @Test
    public void testUnparse_srs() {
        System.out.println("unparse");

        String expResult = "<http://www.opengis.net/def/crs/EPSG/0/4326> POINT(-83.38 33.95)";

        Coordinate coord = new Coordinate(-83.38, 33.95);
        Point point = GEOMETRY_FACTORY.createPoint(coord);
        String srsURI = "http://www.opengis.net/def/crs/EPSG/0/4326";

        DimensionInfo dimensionInfo = new DimensionInfo(2, 2, 0);

        GeometryWrapper geometry = new GeometryWrapper(point, srsURI, WKTDatatype.URI, dimensionInfo);

        String result = WKT_DATATYPE.unparse(geometry);

        //System.out.println("Expected: " + expResult);
        //System.out.println("Result: " + result);
        assertEquals(expResult, result);

    }

    /**
     * Test of parse method, of class WKTDatatype.
     */
    @Test
    public void testParseNoSRS() {
        System.out.println("parseNoSRS");
        String lexicalForm = "POINT(-83.38 33.95)";

        GeometryWrapper result = WKT_DATATYPE.parse(lexicalForm);

        Coordinate coord = new Coordinate(-83.38, 33.95);
        Point expGeometry = GEOMETRY_FACTORY.createPoint(coord);
        String expSRSURI = SRS_URI.DEFAULT_WKT_CRS84;

        DimensionInfo dimensionInfo = new DimensionInfo(2, 2, 0);

        GeometryWrapper expResult = new GeometryWrapper(expGeometry, expSRSURI, WKTDatatype.URI, dimensionInfo, lexicalForm);

        //System.out.println("Expected: " + expResult);
        //System.out.println("Result: " + result);
        assertEquals(expResult, result);
    }

    /**
     * Test of parse method, of class WKTDatatype.
     */
    @Test
    public void testParseNoSRSNotEqual() {
        System.out.println("parseNoSRSNotEqual");
        String lexicalForm = "POINT(-83.38 33.95)";

        GeometryWrapper result = WKT_DATATYPE.parse(lexicalForm);

        Coordinate coord = new Coordinate(-88.38, 33.95);
        Point expGeometry = GEOMETRY_FACTORY.createPoint(coord);

        String expSRSURI = SRS_URI.DEFAULT_WKT_CRS84;

        DimensionInfo dimensionInfo = new DimensionInfo(2, 2, 0);

        GeometryWrapper expResult = new GeometryWrapper(expGeometry, expSRSURI, WKTDatatype.URI, dimensionInfo);

        //System.out.println("Expected: " + expResult);
        //System.out.println("Result: " + result);
        assertThat(expResult, not(result));
    }

    /**
     * Test of parse method, of class WKTDatatype.
     */
    @Test
    public void testParseNoSRSNotEqual2() {
        System.out.println("parseNoSRSNotEqual2");
        String lexicalForm = "POINT(-83.38 33.95)";

        GeometryWrapper result = WKT_DATATYPE.parse(lexicalForm);

        Coordinate coord = new Coordinate(-83.38, 33.95);
        Point expGeometry = GEOMETRY_FACTORY.createPoint(coord);

        String expSRSURI = "http://www.opengis.net/def/crs/EPSG/0/4326";

        DimensionInfo dimensionInfo = new DimensionInfo(2, 2, 0);

        GeometryWrapper expResult = new GeometryWrapper(expGeometry, expSRSURI, WKTDatatype.URI, dimensionInfo);

        //System.out.println("Expected: " + expResult);
        //System.out.println("Result: " + result);
        assertThat(expResult, not(result));
    }

    /**
     * Test of parse method, of class WKTDatatype.
     */
    @Test
    public void testParseSRS() {
        System.out.println("parseSRS");
        String lexicalForm = "<http://www.opengis.net/def/crs/EPSG/0/4326> POINT(33.95 -88.38)";

        GeometryWrapper result = WKT_DATATYPE.parse(lexicalForm);

        Coordinate coord = new Coordinate(33.95, -88.38);
        Point expGeometry = GEOMETRY_FACTORY.createPoint(coord);

        String expSRSURI = "http://www.opengis.net/def/crs/EPSG/0/4326";

        DimensionInfo dimensionInfo = new DimensionInfo(2, 2, 0);

        GeometryWrapper expResult = new GeometryWrapper(expGeometry, expSRSURI, WKTDatatype.URI, dimensionInfo);

        //System.out.println("Expected: " + expResult);
        //System.out.println("Result: " + result);
        assertEquals(expResult, result);
    }

    /**
     * Test of parse method, of class WKTDatatype.
     */
    @Test
    public void testParseSRSNotEqual() {
        System.out.println("parseSRSNotEqual");
        String lexicalForm = "<http://www.opengis.net/def/crs/EPSG/0/4326> POINT(33.95 -88.38)";

        GeometryWrapper result = WKT_DATATYPE.parse(lexicalForm);

        Coordinate coord = new Coordinate(-88.38, 33.95);
        Point expGeometry = GEOMETRY_FACTORY.createPoint(coord);

        String expSRSURI = "http://www.opengis.net/def/crs/EPSG/0/4326";

        DimensionInfo dimensionInfo = new DimensionInfo(2, 2, 0);

        GeometryWrapper expResult = new GeometryWrapper(expGeometry, expSRSURI, WKTDatatype.URI, dimensionInfo);

        //System.out.println("Expected: " + expResult);
        //System.out.println("Result: " + result);
        assertThat(expResult, not(result));
    }

    /**
     * Test of parse method, of class WKTDatatype.
     */
    @Test
    public void testParseSRSNotEqual2() {
        System.out.println("parseSRSNotEqual2");
        String lexicalForm = "<http://www.opengis.net/def/crs/EPSG/0/4326> POINT(33.95 -88.38)";

        GeometryWrapper result = WKT_DATATYPE.parse(lexicalForm);

        Coordinate coord = new Coordinate(33.95, -88.38);
        Point expGeometry = GEOMETRY_FACTORY.createPoint(coord);

        String expSRSURI = SRS_URI.DEFAULT_WKT_CRS84;

        DimensionInfo dimensionInfo = new DimensionInfo(2, 2, 0);

        GeometryWrapper expResult = new GeometryWrapper(expGeometry, expSRSURI, WKTDatatype.URI, dimensionInfo);

        //System.out.println("Expected: " + expResult);
        //System.out.println("Result: " + result);
        assertThat(expResult, not(result));
    }

    /**
     * Test of read method, of class WKTReader.
     */
    @Test
    public void testReadPoint() {
        System.out.println("readPoint");
        String wktLiteral = "POINT ZM (11.0 12.0 8.0 5.0)";

        Geometry geometry = GEOMETRY_FACTORY.createPoint(new CustomCoordinateSequence(CoordinateSequenceDimensions.XYZM, "11.0 12.0 8.0 5.0"));
        GeometryWrapper expResult = new GeometryWrapper(geometry, SRS_URI.DEFAULT_WKT_CRS84, WKTDatatype.URI, new DimensionInfo(4, 3, 0));
        GeometryWrapper result = WKT_DATATYPE.read(wktLiteral);

        //System.out.println("Expected: " + expResult);
        //System.out.println("Result: " + result);
        assertEquals(expResult, result);
    }

    /**
     * Test of read method, of class WKTReader.
     */
    @Test
    public void testReadPoint2() {
        System.out.println("readPoint2");
        String wktLiteral = "<http://www.opengis.net/def/crs/OGC/1.3/CRS84> POINT ZM(11.0 12.0 8.0 5.0)";

        Geometry geometry = GEOMETRY_FACTORY.createPoint(new CustomCoordinateSequence(CoordinateSequenceDimensions.XYZM, "11.0 12.0 8.0 5.0"));
        GeometryWrapper expResult = new GeometryWrapper(geometry, SRS_URI.DEFAULT_WKT_CRS84, WKTDatatype.URI, new DimensionInfo(4, 3, 0), wktLiteral);
        GeometryWrapper result = WKT_DATATYPE.read(wktLiteral);

        //System.out.println("Expected: " + expResult);
        //System.out.println("Result: " + result);
        assertEquals(expResult, result);
    }

    /**
     * Test of read method, of class WKTReader.
     */
    @Test
    public void testReadLineString() {
        System.out.println("readLineString");
        String wktLiteral = "LINESTRING ZM (11 12.1 8 5, 3 4 6 2)";

        Geometry geometry = GEOMETRY_FACTORY.createLineString(new CustomCoordinateSequence(CoordinateSequenceDimensions.XYZM, "11 12.1 8 5, 3 4 6 2"));
        GeometryWrapper expResult = new GeometryWrapper(geometry, SRS_URI.DEFAULT_WKT_CRS84, WKTDatatype.URI, new DimensionInfo(4, 3, 1));
        GeometryWrapper result = WKT_DATATYPE.read(wktLiteral);

        //System.out.println("Expected: " + expResult);
        //System.out.println("Result: " + result);
        assertEquals(expResult, result);
    }

    /**
     * Test of read method, of class WKTReader.
     */
    @Test
    public void testReadPolygon() {
        System.out.println("readPolygon");
        String wktLiteral = "POLYGON ZM((30 10 0 1, 40 40 0 1, 20 40 0 1, 10 20 0 1, 30 10 0 1))";

        Geometry geometry = GEOMETRY_FACTORY.createPolygon(new CustomCoordinateSequence(CoordinateSequenceDimensions.XYZM, "30 10 0 1, 40 40 0 1, 20 40 0 1, 10 20 0 1, 30 10 0 1"));
        GeometryWrapper expResult = new GeometryWrapper(geometry, SRS_URI.DEFAULT_WKT_CRS84, WKTDatatype.URI, new DimensionInfo(4, 3, 2), wktLiteral);
        GeometryWrapper result = WKT_DATATYPE.read(wktLiteral);

        //System.out.println("Expected: " + expResult);
        //System.out.println("Result: " + result);
        assertEquals(expResult, result);
    }

    /**
     * Test of read method, of class WKTReader.
     */
    @Test
    public void testReadPolygon2() {
        System.out.println("readPolygon2");
        String wktLiteral = "POLYGON ZM((30 10 0 1, 40 40 0 1, 20 40 0 1, 10 20 0 1, 30 10 0 1), (20 30 0 1, 35 35 0 1, 30 20 0 1, 20 30 0 1))";

        LinearRing shell = GEOMETRY_FACTORY.createLinearRing(new CustomCoordinateSequence(CoordinateSequenceDimensions.XYZM, "30 10 0 1, 40 40 0 1, 20 40 0 1, 10 20 0 1, 30 10 0 1"));
        LinearRing[] holes = new LinearRing[]{GEOMETRY_FACTORY.createLinearRing(new CustomCoordinateSequence(CoordinateSequenceDimensions.XYZM, "20 30 0 1, 35 35 0 1, 30 20 0 1, 20 30 0 1"))};
        Geometry geometry = GEOMETRY_FACTORY.createPolygon(shell, holes);

        GeometryWrapper expResult = new GeometryWrapper(geometry, SRS_URI.DEFAULT_WKT_CRS84, WKTDatatype.URI, new DimensionInfo(4, 3, 2));
        GeometryWrapper result = WKT_DATATYPE.read(wktLiteral);

        //System.out.println("Expected: " + expResult);
        //System.out.println("Result: " + result);
        assertEquals(expResult, result);
    }

    /**
     * Test of read method, of class WKTReader.
     */
    @Test
    public void testReadMultiPoint() {
        System.out.println("readMultiPoint");
        String wktLiteral = "MULTIPOINT ZM((10 40 0 1), (40 30 0 1), (20 20 0 1), (30 10 0 1))";

        Geometry geometry = GEOMETRY_FACTORY.createMultiPoint(new CustomCoordinateSequence(CoordinateSequenceDimensions.XYZM, "10 40 0 1, 40 30 0 1, 20 20 0 1, 30 10 0 1"));
        GeometryWrapper expResult = new GeometryWrapper(geometry, SRS_URI.DEFAULT_WKT_CRS84, WKTDatatype.URI, new DimensionInfo(4, 3, 0));
        GeometryWrapper result = WKT_DATATYPE.read(wktLiteral);

        //System.out.println("Expected: " + expResult);
        //System.out.println("Result: " + result);
        assertEquals(expResult, result);
    }

    /**
     * Test of read method, of class WKTReader.
     */
    @Test
    public void testReadMultiLineString() {
        System.out.println("readMultiLineString");
        String wktLiteral = "MULTILINESTRING ZM((10 10 0 1, 20 20 0 1, 10 40 0 1), (40 40 0 1, 30 30 0 1, 40 20 0 1, 30 10 0 1))";

        LineString[] lineStrings = new LineString[2];
        lineStrings[0] = GEOMETRY_FACTORY.createLineString(new CustomCoordinateSequence(CoordinateSequenceDimensions.XYZM, "10 10 0 1, 20 20 0 1, 10 40 0 1"));
        lineStrings[1] = GEOMETRY_FACTORY.createLineString(new CustomCoordinateSequence(CoordinateSequenceDimensions.XYZM, "40 40 0 1, 30 30 0 1, 40 20 0 1, 30 10 0 1"));
        Geometry geometry = GEOMETRY_FACTORY.createMultiLineString(lineStrings);

        GeometryWrapper expResult = new GeometryWrapper(geometry, SRS_URI.DEFAULT_WKT_CRS84, WKTDatatype.URI, new DimensionInfo(4, 3, 1));
        GeometryWrapper result = WKT_DATATYPE.read(wktLiteral);

        //System.out.println("Expected: " + expResult);
        //System.out.println("Result: " + result);
        assertEquals(expResult, result);
    }

    /**
     * Test of read method, of class WKTReader.
     */
    @Test
    public void testReadMultiPolygon() {
        System.out.println("readMultiPolygon");
        String wktLiteral = "MULTIPOLYGON ZM(((40 40 0 1, 20 45 0 1, 45 30 0 1, 40 40 0 1)), ((20 35 0 1, 10 30 0 1, 10 10 0 1, 30 5 0 1, 45 20 0 1, 20 35 0 1), (30 20 0 1, 20 15 0 1, 20 25 0 1, 30 20 0 1)))";

        Polygon[] polygons = new Polygon[2];
        polygons[0] = GEOMETRY_FACTORY.createPolygon(new CustomCoordinateSequence(CoordinateSequenceDimensions.XYZM, "40 40 0 1, 20 45 0 1, 45 30 0 1, 40 40 0 1"));
        LinearRing shell = GEOMETRY_FACTORY.createLinearRing(new CustomCoordinateSequence(CoordinateSequenceDimensions.XYZM, "20 35 0 1, 10 30 0 1, 10 10 0 1, 30 5 0 1, 45 20 0 1, 20 35 0 1"));
        LinearRing[] holes = new LinearRing[]{GEOMETRY_FACTORY.createLinearRing(new CustomCoordinateSequence(CoordinateSequenceDimensions.XYZM, "30 20 0 1, 20 15 0 1, 20 25 0 1, 30 20 0 1"))};
        polygons[1] = GEOMETRY_FACTORY.createPolygon(shell, holes);
        Geometry geometry = GEOMETRY_FACTORY.createMultiPolygon(polygons);

        GeometryWrapper expResult = new GeometryWrapper(geometry, SRS_URI.DEFAULT_WKT_CRS84, WKTDatatype.URI, new DimensionInfo(4, 3, 2));
        GeometryWrapper result = WKT_DATATYPE.read(wktLiteral);

        //System.out.println("Expected: " + expResult);
        //System.out.println("Result: " + result);
        assertEquals(expResult, result);
    }

    /**
     * Test of read method, of class WKTReader.
     */
    @Test
    public void testReadGeometryCollection() {
        System.out.println("readGeometryCollection");
        String wktLiteral = "GEOMETRYCOLLECTION ZM(POINT ZM (4 6 0 1), LINESTRING ZM (4 6 0 1, 7 10 0 1))";

        Geometry[] geometries = new Geometry[2];
        geometries[0] = GEOMETRY_FACTORY.createPoint(new CustomCoordinateSequence(CoordinateSequenceDimensions.XYZM, "4 6 0 1"));
        geometries[1] = GEOMETRY_FACTORY.createLineString(new CustomCoordinateSequence(CoordinateSequenceDimensions.XYZM, "4 6 0 1,7 10 0 1"));
        Geometry geometry = GEOMETRY_FACTORY.createGeometryCollection(geometries);

        GeometryWrapper expResult = new GeometryWrapper(geometry, SRS_URI.DEFAULT_WKT_CRS84, WKTDatatype.URI, new DimensionInfo(4, 3, 1));
        GeometryWrapper result = WKT_DATATYPE.read(wktLiteral);

        //System.out.println("Expected: " + expResult);
        //System.out.println("Result: " + result);
        assertEquals(expResult, result);
    }

    /**
     * Test of empty geometry literal, of class WKTDatatype.<br>
     * Req 16 An empty geo:gmlLiteral shall be interpreted as an empty geometry.
     */
    @Test
    public void testEmpty() {
        GeometryWrapper geo = WKT_DATATYPE.read("");
        Geometry test = GEOMETRY_FACTORY.createPoint();
        GeometryWrapper expResult = new GeometryWrapper(test, SRS_URI.DEFAULT_WKT_CRS84, WKTDatatype.URI, new DimensionInfo(2, 2, 0));

        //System.out.println("Expected: " + expResult);
        //System.out.println("Result: " + geo);
        assertEquals(geo, expResult);
    }

}
