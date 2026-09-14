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

import java.util.ArrayList;
import java.util.List;

import org.apache.jena.geosparql.implementation.datatype.WKTDatatype;
import org.apache.jena.geosparql.implementation.jts.CoordinateSequenceDimensions;
import org.apache.jena.geosparql.implementation.jts.CustomCoordinateSequence;
import org.apache.jena.geosparql.implementation.jts.CustomGeometryFactory;
import org.apache.jena.geosparql.implementation.vocabulary.SRS_URI;
import org.junit.After;
import org.junit.AfterClass;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotSame;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.CoordinateSequence;
import org.locationtech.jts.geom.GeometryComponentFilter;
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

    @Test
    public void reversalPreservesZAndMForStandardAndCustomSequences() throws ParseException {
        String[] examples = {
            "POINT Z (100 10 -4)",
            "LINESTRING M (100 10 7, 120 20 8)",
            "LINESTRING ZM (100 10 -4 7, 120 20 9 8)",
            "POLYGON ZM ((0 0 1 10, 10 0 2 20, 10 10 3 30, 0 0 1 10), "
            + "(2 1 -9 40, 3 1 8 50, 3 2 7 60, 2 1 -9 40))",
            "MULTIPOINT Z ((100 10 -4), (120 20 9))"
        };
        for (String wkt : examples) {
            assertReversalPreservesOrdinates(new WKTReader().read(wkt));
            assertReversalPreservesOrdinates(GeometryWrapper.extract(wkt, WKTDatatype.URI).getParsingGeometry());
        }
    }

    @Test
    public void reversalPreservesNestedMembersAndEmptyLayouts() {
        var factory = CustomGeometryFactory.theInstance();
        Geometry point = GeometryWrapper.extract("POINT ZM (100 10 -4 7)", WKTDatatype.URI).getParsingGeometry();
        Geometry empty = factory.createPoint(new CustomCoordinateSequence(0, CoordinateSequenceDimensions.XYZM));
        Geometry nested = factory.createGeometryCollection(new Geometry[] { empty, point });
        Geometry collection = factory.createGeometryCollection(new Geometry[] { point, nested });
        Geometry reversed = GeometryReverse.reverseGeometry(collection);
        assertEquals(2, reversed.getNumGeometries());
        assertEquals(2, reversed.getGeometryN(1).getNumGeometries());
        assertReversalPreservesOrdinates(collection);
        assertReversalPreservesOrdinates(empty);
    }

    @Test
    public void reversalPreservesDeclaredLayoutWithMissingZAndM() {
        var coordinates = new CustomCoordinateSequence(CoordinateSequenceDimensions.XYZM,
                                                       "100 10 NaN NaN,120 20 9 8");
        assertReversalPreservesOrdinates(CustomGeometryFactory.theInstance().createLineString(coordinates));
    }

    private static void assertReversalPreservesOrdinates(Geometry original) {
        // Capture values independently so a shallow copy cannot hide input mutation.
        List<CoordinateSequence> source = sequences(original);
        List<double[]> values = new ArrayList<>();
        for (CoordinateSequence sequence : source) {
            for (int i = 0; i < sequence.size(); i++) {
                values.add(new double[] { sequence.getX(i), sequence.getY(i), sequence.getZ(i), sequence.getM(i) });
            }
        }
        original.getEnvelopeInternal(); // Populate the envelope cache before reversal.
        Geometry reversed = GeometryReverse.reverseGeometry(original);
        assertNotSame(original, reversed);
        assertEquals(original.getGeometryType(), reversed.getGeometryType());
        List<CoordinateSequence> result = sequences(reversed);
        assertEquals(source.size(), result.size());
        int coordinate = 0;
        for (int n = 0; n < source.size(); n++) {
            CoordinateSequence before = source.get(n);
            CoordinateSequence after = result.get(n);
            assertNotSame(before, after);
            assertEquals(before.getDimension(), after.getDimension());
            assertEquals(before.getMeasures(), after.getMeasures());
            assertEquals(before.size(), after.size());
            for (int i = 0; i < before.size(); i++) {
                double[] expected = values.get(coordinate++);
                assertEquals(expected[0], before.getX(i), 0);
                assertEquals(expected[1], before.getY(i), 0);
                assertEquals(expected[2], before.getZ(i), 0);
                assertEquals(expected[3], before.getM(i), 0);
                assertEquals(expected[1], after.getX(i), 0);
                assertEquals(expected[0], after.getY(i), 0);
                assertEquals(expected[2], after.getZ(i), 0);
                assertEquals(expected[3], after.getM(i), 0);
            }
        }
        if (!original.isEmpty()) {
            assertEquals(original.getEnvelopeInternal().getMinY(), reversed.getEnvelopeInternal().getMinX(), 0);
            assertEquals(original.getEnvelopeInternal().getMaxY(), reversed.getEnvelopeInternal().getMaxX(), 0);
            assertEquals(original.getEnvelopeInternal().getMinX(), reversed.getEnvelopeInternal().getMinY(), 0);
            assertEquals(original.getEnvelopeInternal().getMaxX(), reversed.getEnvelopeInternal().getMaxY(), 0);
        }
    }

    private static List<CoordinateSequence> sequences(Geometry geometry) {
        List<CoordinateSequence> result = new ArrayList<>();
        geometry.apply((GeometryComponentFilter) component -> {
            if (component instanceof Point point)
                result.add(point.getCoordinateSequence());
            else if (component instanceof LineString line)
                result.add(line.getCoordinateSequence());
        });
        return result;
    }

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


        WKTReader reader = new WKTReader();
        try {
            Point geometry = (Point) reader.read("POINT(2 0)");
            String srsURI = "http://www.opengis.net/def/crs/EPSG/0/4326";

            Geometry expResult = reader.read("POINT(0 2)");
            Geometry result = GeometryReverse.check(geometry, srsURI);

            //
            //
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


        WKTReader reader = new WKTReader();
        try {
            LineString geometry = (LineString) reader.read("LINESTRING(0 0, 2 0, 5 0)");
            String srsURI = "http://www.opengis.net/def/crs/EPSG/0/4326";

            Geometry expResult = reader.read("LINESTRING(0 0, 0 2, 0 5)");
            Geometry result = GeometryReverse.check(geometry, srsURI);

            //
            //
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


        WKTReader reader = new WKTReader();
        try {
            Polygon geometry = (Polygon) reader.read("POLYGON ((30 10, 40 40, 20 40, 10 20, 30 10))");
            String srsURI = "http://www.opengis.net/def/crs/EPSG/0/4326";

            Geometry expResult = reader.read("POLYGON ((10 30, 40 40, 40 20, 20 10, 10 30))");
            Geometry result = GeometryReverse.check(geometry, srsURI);

            //
            //
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


        WKTReader reader = new WKTReader();
        try {
            Polygon geometry = (Polygon) reader.read("POLYGON ((35 10, 45 45, 15 40, 10 20, 35 10),(20 30, 35 35, 30 20, 20 30))");
            String srsURI = "http://www.opengis.net/def/crs/EPSG/0/4326";

            Geometry expResult = reader.read("POLYGON ((10 35, 45 45, 40 15, 20 10, 10 35),(30 20, 35 35, 20 30, 30 20))");
            Geometry result = GeometryReverse.check(geometry, srsURI);

            //
            //
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


        WKTReader reader = new WKTReader();
        try {
            MultiPoint geometry = (MultiPoint) reader.read("MULTIPOINT (10 40, 40 30, 20 20, 30 10)");
            String srsURI = "http://www.opengis.net/def/crs/EPSG/0/4326";

            Geometry expResult = reader.read("MULTIPOINT (40 10, 30 40, 20 20, 10 30)");
            Geometry result = GeometryReverse.check(geometry, srsURI);

            //
            //
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


        WKTReader reader = new WKTReader();
        try {
            MultiPolygon geometry = (MultiPolygon) reader.read("MULTIPOLYGON (((30 20, 45 40, 10 40, 30 20)),((15 5, 40 10, 10 20, 5 10, 15 5)))");
            String srsURI = "http://www.opengis.net/def/crs/EPSG/0/4326";

            Geometry expResult = reader.read("MULTIPOLYGON (((20 30, 40 45, 40 10, 20 30)),((5 15, 10 40, 20 10, 10 5, 5 15)))");
            Geometry result = GeometryReverse.check(geometry, srsURI);

            //
            //
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


        WKTReader reader = new WKTReader();
        try {
            MultiLineString geometry = (MultiLineString) reader.read("MULTILINESTRING ((10 10, 20 30, 10 40),(40 45, 30 35, 40 20, 30 10))");
            String srsURI = "http://www.opengis.net/def/crs/EPSG/0/4326";

            Geometry expResult = reader.read("MULTILINESTRING ((10 10, 30 20, 40 10),(45 40, 35 30, 20 40, 10 30))");
            Geometry result = GeometryReverse.check(geometry, srsURI);

            //
            //
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


        WKTReader reader = new WKTReader();
        try {
            LineString geometry = (LineString) reader.read("LINESTRING(0 0, 2 0, 5 0)");
            String srsURI = SRS_URI.DEFAULT_WKT_CRS84;
            Geometry expResult = reader.read("LINESTRING(0 0, 2 0, 5 0)");
            Geometry result = GeometryReverse.check(geometry, srsURI);

            //
            //
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


        WKTReader reader = new WKTReader();
        try {
            MultiPolygon geometry = (MultiPolygon) reader.read("MULTIPOLYGON (((35 10, 45 45, 15 40, 10 20, 35 10),(20 30, 35 35, 30 20, 20 30)),((15 5, 40 10, 10 20, 5 10, 15 5)))");
            String srsURI = "http://www.opengis.net/def/crs/EPSG/0/4326";

            Geometry expResult = reader.read("MULTIPOLYGON (((10 35, 45 45, 40 15, 20 10, 10 35),(30 20, 35 35, 20 30, 30 20)),((5 15, 10 40, 20 10, 10 5, 5 15)))");
            Geometry result = GeometryReverse.check(geometry, srsURI);

            //
            //
            assertEquals(expResult, result);
        } catch (ParseException ex) {
            System.err.println("ParseException: " + ex.getMessage());
        }

    }
}
