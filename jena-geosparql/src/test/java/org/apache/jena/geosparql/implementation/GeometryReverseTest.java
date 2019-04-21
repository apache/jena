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
package org.apache.jena.geosparql.implementation;

import org.apache.jena.geosparql.implementation.GeometryReverse;
import org.apache.jena.geosparql.implementation.vocabulary.SRS_URI;
import org.junit.After;
import org.junit.AfterClass;
import static org.junit.Assert.assertEquals;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.LineString;
import org.locationtech.jts.geom.MultiLineString;
import org.locationtech.jts.geom.MultiPoint;
import org.locationtech.jts.geom.MultiPolygon;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.geom.Polygon;
import org.locationtech.jts.io.ParseException;
import org.locationtech.jts.io.WKTReader;
import org.opengis.util.FactoryException;

/**
 *
 *
 */
public class GeometryReverseTest {

    public GeometryReverseTest() {
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
     * Test of check method, of class GeometryReverse.
     *
     * @throws org.opengis.util.FactoryException
     */
    @Test
    public void testCheckPoint() throws FactoryException {
        System.out.println("checkPoint");

        WKTReader reader = new WKTReader();
        try {
            Point geometry = (Point) reader.read("POINT(2 0)");
            String srsURI = "http://www.opengis.net/def/crs/EPSG/0/4326";

            Geometry expResult = reader.read("POINT(0 2)");
            Geometry result = GeometryReverse.check(geometry, srsURI);

            //System.out.println("Expected: " + expResult);
            //System.out.println("Result: " + result);
            assertEquals(expResult, result);
        } catch (ParseException ex) {
            System.err.println("ParseException: " + ex.getMessage());
        }

    }

    /**
     * Test of check method, of class GeometryReverse.
     *
     * @throws org.opengis.util.FactoryException
     */
    @Test
    public void testCheckLineString() throws FactoryException {
        System.out.println("checkLineString");

        WKTReader reader = new WKTReader();
        try {
            LineString geometry = (LineString) reader.read("LINESTRING(0 0, 2 0, 5 0)");
            String srsURI = "http://www.opengis.net/def/crs/EPSG/0/4326";

            Geometry expResult = reader.read("LINESTRING(0 0, 0 2, 0 5)");
            Geometry result = GeometryReverse.check(geometry, srsURI);

            //System.out.println("Expected: " + expResult);
            //System.out.println("Result: " + result);
            assertEquals(expResult, result);
        } catch (ParseException ex) {
            System.err.println("ParseException: " + ex.getMessage());
        }

    }

    /**
     * Test of check method, of class GeometryReverse.
     *
     * @throws org.opengis.util.FactoryException
     */
    @Test
    public void testCheckPolygon() throws FactoryException {
        System.out.println("checkPolygon");

        WKTReader reader = new WKTReader();
        try {
            Polygon geometry = (Polygon) reader.read("POLYGON ((30 10, 40 40, 20 40, 10 20, 30 10))");
            String srsURI = "http://www.opengis.net/def/crs/EPSG/0/4326";

            Geometry expResult = reader.read("POLYGON ((10 30, 40 40, 40 20, 20 10, 10 30))");
            Geometry result = GeometryReverse.check(geometry, srsURI);

            //System.out.println("Expected: " + expResult);
            //System.out.println("Result: " + result);
            assertEquals(expResult, result);
        } catch (ParseException ex) {
            System.err.println("ParseException: " + ex.getMessage());
        }

    }

    /**
     * Test of check method, of class GeometryReverse.
     *
     * @throws org.opengis.util.FactoryException
     */
    @Test
    public void testCheckPolygonHoled() throws FactoryException {
        System.out.println("checkPolygonHoled");

        WKTReader reader = new WKTReader();
        try {
            Polygon geometry = (Polygon) reader.read("POLYGON ((35 10, 45 45, 15 40, 10 20, 35 10),(20 30, 35 35, 30 20, 20 30))");
            String srsURI = "http://www.opengis.net/def/crs/EPSG/0/4326";

            Geometry expResult = reader.read("POLYGON ((10 35, 45 45, 40 15, 20 10, 10 35),(30 20, 35 35, 20 30, 30 20))");
            Geometry result = GeometryReverse.check(geometry, srsURI);

            //System.out.println("Expected: " + expResult);
            //System.out.println("Result: " + result);
            assertEquals(expResult, result);
        } catch (ParseException ex) {
            System.err.println("ParseException: " + ex.getMessage());
        }

    }

    /**
     * Test of check method, of class GeometryReverse.
     *
     * @throws org.opengis.util.FactoryException
     */
    @Test
    public void testCheckMultiPoint() throws FactoryException {
        System.out.println("checkMultiPoint");

        WKTReader reader = new WKTReader();
        try {
            MultiPoint geometry = (MultiPoint) reader.read("MULTIPOINT (10 40, 40 30, 20 20, 30 10)");
            String srsURI = "http://www.opengis.net/def/crs/EPSG/0/4326";

            Geometry expResult = reader.read("MULTIPOINT (40 10, 30 40, 20 20, 10 30)");
            Geometry result = GeometryReverse.check(geometry, srsURI);

            //System.out.println("Expected: " + expResult);
            //System.out.println("Result: " + result);
            assertEquals(expResult, result);
        } catch (ParseException ex) {
            System.err.println("ParseException: " + ex.getMessage());
        }

    }

    /**
     * Test of check method, of class GeometryReverse.
     *
     * @throws org.opengis.util.FactoryException
     */
    @Test
    public void testCheckMultiPolygon() throws FactoryException {
        System.out.println("checkMultiPolygon");

        WKTReader reader = new WKTReader();
        try {
            MultiPolygon geometry = (MultiPolygon) reader.read("MULTIPOLYGON (((30 20, 45 40, 10 40, 30 20)),((15 5, 40 10, 10 20, 5 10, 15 5)))");
            String srsURI = "http://www.opengis.net/def/crs/EPSG/0/4326";

            Geometry expResult = reader.read("MULTIPOLYGON (((20 30, 40 45, 40 10, 20 30)),((5 15, 10 40, 20 10, 10 5, 5 15)))");
            Geometry result = GeometryReverse.check(geometry, srsURI);

            //System.out.println("Expected: " + expResult);
            //System.out.println("Result: " + result);
            assertEquals(expResult, result);
        } catch (ParseException ex) {
            System.err.println("ParseException: " + ex.getMessage());
        }

    }

    /**
     * Test of check method, of class GeometryReverse.
     *
     * @throws org.opengis.util.FactoryException
     */
    @Test
    public void testCheckMultiLineString() throws FactoryException {
        System.out.println("checkMultiLineString");

        WKTReader reader = new WKTReader();
        try {
            MultiLineString geometry = (MultiLineString) reader.read("MULTILINESTRING ((10 10, 20 30, 10 40),(40 45, 30 35, 40 20, 30 10))");
            String srsURI = "http://www.opengis.net/def/crs/EPSG/0/4326";

            Geometry expResult = reader.read("MULTILINESTRING ((10 10, 30 20, 40 10),(45 40, 35 30, 20 40, 10 30))");
            Geometry result = GeometryReverse.check(geometry, srsURI);

            //System.out.println("Expected: " + expResult);
            //System.out.println("Result: " + result);
            assertEquals(expResult, result);
        } catch (ParseException ex) {
            System.err.println("ParseException: " + ex.getMessage());
        }

    }

    /**
     * Test of check method, of class GeometryReverse.
     */
    @Test
    public void testCheckLineStringNotReversed() {
        System.out.println("checkLineStringNotReversed");

        WKTReader reader = new WKTReader();
        try {
            LineString geometry = (LineString) reader.read("LINESTRING(0 0, 2 0, 5 0)");
            String srsURI = SRS_URI.DEFAULT_WKT_CRS84;
            Geometry expResult = reader.read("LINESTRING(0 0, 2 0, 5 0)");
            Geometry result = GeometryReverse.check(geometry, srsURI);

            //System.out.println("Expected: " + expResult);
            //System.out.println("Result: " + result);
            assertEquals(expResult, result);
        } catch (ParseException ex) {
            System.err.println("ParseException: " + ex.getMessage());
        }

    }

    /**
     * Test of check method, of class GeometryReverse.
     *
     * @throws org.opengis.util.FactoryException
     */
    @Test
    public void testCheckMultiPolygonHoled() throws FactoryException {
        System.out.println("checkMultiPolygonHoled");

        WKTReader reader = new WKTReader();
        try {
            MultiPolygon geometry = (MultiPolygon) reader.read("MULTIPOLYGON (((35 10, 45 45, 15 40, 10 20, 35 10),(20 30, 35 35, 30 20, 20 30)),((15 5, 40 10, 10 20, 5 10, 15 5)))");
            String srsURI = "http://www.opengis.net/def/crs/EPSG/0/4326";

            Geometry expResult = reader.read("MULTIPOLYGON (((10 35, 45 45, 40 15, 20 10, 10 35),(30 20, 35 35, 20 30, 30 20)),((5 15, 10 40, 20 10, 10 5, 5 15)))");
            Geometry result = GeometryReverse.check(geometry, srsURI);

            //System.out.println("Expected: " + expResult);
            //System.out.println("Result: " + result);
            assertEquals(expResult, result);
        } catch (ParseException ex) {
            System.err.println("ParseException: " + ex.getMessage());
        }

    }
}
