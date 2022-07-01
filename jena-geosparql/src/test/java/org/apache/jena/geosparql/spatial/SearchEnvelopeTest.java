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
package org.apache.jena.geosparql.spatial;

import java.util.Arrays;
import java.util.HashSet;
import org.apache.jena.geosparql.implementation.GeometryWrapper;
import org.apache.jena.geosparql.implementation.datatype.WKTDatatype;
import org.apache.jena.geosparql.implementation.vocabulary.Unit_URI;
import org.apache.jena.rdf.model.Resource;
import org.junit.After;
import org.junit.AfterClass;
import static org.junit.Assert.assertEquals;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.locationtech.jts.geom.Envelope;

/**
 *
 *
 */
public class SearchEnvelopeTest {

    public SearchEnvelopeTest() {
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

    public static final double X1 = -180;
    public static final double X2 = 180;
    public static final double Y1 = -90;
    public static final double Y2 = 90;

    // SIS 1.0
    //public static final double OS_X1 = -118397.00138845091;
    //public static final double OS_X2 = 751441.7790901454;
    //public static final double OS_Y1 = -16627.734375018626;
    //public static final double OS_Y2 = 1272149.3463499574;
    // SIS 1.1
    public static final double OS_X1 = -104009.35713717458;
    public static final double OS_X2 = 688806.0073395987;
    public static final double OS_Y1 = -16627.734528041445;
    public static final double OS_Y2 = 1256558.4455361878;

    /**
     * Test of build method, of class SearchEnvelope.
     */
    @Test
    public void testBuild_NORTH() {

        GeometryWrapper geometryWrapper = GeometryWrapper.extract("<http://www.opengis.net/def/crs/EPSG/0/4326> POINT(10.0 20.0)", WKTDatatype.URI);
        CardinalDirection direction = CardinalDirection.NORTH;
        SearchEnvelope expResult = new SearchEnvelope(new Envelope(X1, X2, 10, Y2), SpatialIndexTestData.WGS_84_SRS_INFO);
        SearchEnvelope result = SearchEnvelope.build(geometryWrapper, SpatialIndexTestData.WGS_84_SRS_INFO, direction);
        assertEquals(expResult, result);
    }

    /**
     * Test of build method, of class SearchEnvelope.
     */
    @Test
    public void testBuild_SOUTH() {

        GeometryWrapper geometryWrapper = GeometryWrapper.extract("<http://www.opengis.net/def/crs/EPSG/0/4326> POINT(10.0 20.0)", WKTDatatype.URI);
        CardinalDirection direction = CardinalDirection.SOUTH;
        SearchEnvelope expResult = new SearchEnvelope(new Envelope(X1, X2, Y1, 10), SpatialIndexTestData.WGS_84_SRS_INFO);
        SearchEnvelope result = SearchEnvelope.build(geometryWrapper, SpatialIndexTestData.WGS_84_SRS_INFO, direction);
        assertEquals(expResult, result);
    }

    /**
     * Test of build method, of class SearchEnvelope.
     */
    @Test
    public void testBuild_EAST() {

        GeometryWrapper geometryWrapper = GeometryWrapper.extract("<http://www.opengis.net/def/crs/EPSG/0/4326> POINT(10.0 -20.0)", WKTDatatype.URI);
        CardinalDirection direction = CardinalDirection.EAST;
        SearchEnvelope expResult = new SearchEnvelope(new Envelope(-20, 160, Y1, Y2), SpatialIndexTestData.WGS_84_SRS_INFO);
        SearchEnvelope result = SearchEnvelope.build(geometryWrapper, SpatialIndexTestData.WGS_84_SRS_INFO, direction);
        assertEquals(expResult, result);
    }

    /**
     * Test of build method, of class SearchEnvelope.
     */
    @Test
    public void testBuild_WEST() {

        GeometryWrapper geometryWrapper = GeometryWrapper.extract("<http://www.opengis.net/def/crs/EPSG/0/4326> POINT(10.0 20.0)", WKTDatatype.URI);
        CardinalDirection direction = CardinalDirection.WEST;
        SearchEnvelope expResult = new SearchEnvelope(new Envelope(-160, 20, Y1, Y2), SpatialIndexTestData.WGS_84_SRS_INFO);
        SearchEnvelope result = SearchEnvelope.build(geometryWrapper, SpatialIndexTestData.WGS_84_SRS_INFO, direction);
        assertEquals(expResult, result);
    }

    /**
     * Test of build method, of class SearchEnvelope.
     */
    @Test
    public void testBuildWrap_EAST() {

        GeometryWrapper geometryWrapper = GeometryWrapper.extract("<http://www.opengis.net/def/crs/EPSG/0/4326> POINT(10.0 100.0)", WKTDatatype.URI);
        CardinalDirection direction = CardinalDirection.EAST;
        SearchEnvelope expResult = new SearchEnvelope(new Envelope(100, 280, Y1, Y2), SpatialIndexTestData.WGS_84_SRS_INFO);
        SearchEnvelope result = SearchEnvelope.build(geometryWrapper, SpatialIndexTestData.WGS_84_SRS_INFO, direction);
        assertEquals(expResult, result);
    }

    /**
     * Test of build method, of class SearchEnvelope.
     */
    @Test
    public void testBuildWrap_WEST() {

        GeometryWrapper geometryWrapper = GeometryWrapper.extract("<http://www.opengis.net/def/crs/EPSG/0/4326> POINT(10.0 -20.0)", WKTDatatype.URI);
        CardinalDirection direction = CardinalDirection.WEST;
        SearchEnvelope expResult = new SearchEnvelope(new Envelope(-200, -20, Y1, Y2), SpatialIndexTestData.WGS_84_SRS_INFO);
        SearchEnvelope result = SearchEnvelope.build(geometryWrapper, SpatialIndexTestData.WGS_84_SRS_INFO, direction);
        assertEquals(expResult, result);
    }

    /**
     * Test of build method, of class SearchEnvelope.
     */
    @Test
    public void testBuild_NORTH_OSGB() {

        GeometryWrapper geometryWrapper = GeometryWrapper.extract("<http://www.opengis.net/def/crs/EPSG/0/27700> POINT(10.0 20.0)", WKTDatatype.URI);
        CardinalDirection direction = CardinalDirection.NORTH;
        SearchEnvelope expResult = new SearchEnvelope(new Envelope(OS_X1, OS_X2, 20, OS_Y2), SpatialIndexTestData.OSGB_SRS_INFO);
        SearchEnvelope result = SearchEnvelope.build(geometryWrapper, SpatialIndexTestData.OSGB_SRS_INFO, direction);
        assertEquals(expResult, result);
    }

    /**
     * Test of build method, of class SearchEnvelope.
     */
    @Test
    public void testBuild_SOUTH_OSGB() {

        GeometryWrapper geometryWrapper = GeometryWrapper.extract("<http://www.opengis.net/def/crs/EPSG/0/27700> POINT(10.0 20.0)", WKTDatatype.URI);
        CardinalDirection direction = CardinalDirection.SOUTH;
        SearchEnvelope expResult = new SearchEnvelope(new Envelope(OS_X1, OS_X2, OS_Y1, 20), SpatialIndexTestData.OSGB_SRS_INFO);
        SearchEnvelope result = SearchEnvelope.build(geometryWrapper, SpatialIndexTestData.OSGB_SRS_INFO, direction);
        assertEquals(expResult, result);
    }

    /**
     * Test of build method, of class SearchEnvelope.
     */
    @Test
    public void testBuild_EAST_OSGB() {

        GeometryWrapper geometryWrapper = GeometryWrapper.extract("<http://www.opengis.net/def/crs/EPSG/0/27700> POINT(10.0 20.0)", WKTDatatype.URI);
        CardinalDirection direction = CardinalDirection.EAST;
        SearchEnvelope expResult = new SearchEnvelope(new Envelope(10, OS_X2, OS_Y1, OS_Y2), SpatialIndexTestData.OSGB_SRS_INFO);
        SearchEnvelope result = SearchEnvelope.build(geometryWrapper, SpatialIndexTestData.OSGB_SRS_INFO, direction);
        assertEquals(expResult, result);
    }

    /**
     * Test of build method, of class SearchEnvelope.
     */
    @Test
    public void testBuild_WEST_OSGB() {

        GeometryWrapper geometryWrapper = GeometryWrapper.extract("<http://www.opengis.net/def/crs/EPSG/0/27700> POINT(10.0 20.0)", WKTDatatype.URI);
        CardinalDirection direction = CardinalDirection.WEST;
        SearchEnvelope expResult = new SearchEnvelope(new Envelope(OS_X1, 10, OS_Y1, OS_Y2), SpatialIndexTestData.OSGB_SRS_INFO);
        SearchEnvelope result = SearchEnvelope.build(geometryWrapper, SpatialIndexTestData.OSGB_SRS_INFO, direction);
        assertEquals(expResult, result);
    }

    /**
     * Test of getMainEnvelope method, of class SearchEnvelope.
     */
    @Test
    public void testGetMainEnvelope() {

        SearchEnvelope instance = new SearchEnvelope(new Envelope(0, 10, 0, 10), SpatialIndexTestData.WGS_84_SRS_INFO);
        Envelope expResult = new Envelope(0, 10, 0, 10);
        Envelope result = instance.getMainEnvelope();
        assertEquals(expResult, result);
    }

    /**
     * Test of getMainEnvelope method, of class SearchEnvelope.
     */
    @Test
    public void testGetMainEnvelope2() {

        SearchEnvelope instance = new SearchEnvelope(new Envelope(40, 220, 0, 10), SpatialIndexTestData.WGS_84_SRS_INFO);
        Envelope expResult = new Envelope(40, 180, 0, 10);
        Envelope result = instance.getMainEnvelope();
        assertEquals(expResult, result);
    }

    /**
     * Test of getMainEnvelope method, of class SearchEnvelope.
     */
    @Test
    public void testGetMainEnvelope3() {

        SearchEnvelope instance = new SearchEnvelope(new Envelope(-220, -40, 0, 10), SpatialIndexTestData.WGS_84_SRS_INFO);
        Envelope expResult = new Envelope(-180, -40, 0, 10);
        Envelope result = instance.getMainEnvelope();
        assertEquals(expResult, result);
    }

    /**
     * Test of getMainEnvelope method, of class SearchEnvelope.
     */
    @Test
    public void testGetMainEnvelope_OSGB() {

        SearchEnvelope instance = new SearchEnvelope(new Envelope(-220, -40, 0, 10), SpatialIndexTestData.OSGB_SRS_INFO);
        Envelope expResult = new Envelope(-220, -40, 0, 10);
        Envelope result = instance.getMainEnvelope();
        assertEquals(expResult, result);
    }

    /**
     * Test of getWrapEnvelope method, of class SearchEnvelope.
     */
    @Test
    public void testGetWrapEnvelope() {

        SearchEnvelope instance = new SearchEnvelope(new Envelope(0, 10, 0, 10), SpatialIndexTestData.WGS_84_SRS_INFO);

        Envelope expResult = null;
        Envelope result = instance.getWrapEnvelope();
        assertEquals(expResult, result);
    }

    /**
     * Test of getWrapEnvelope method, of class SearchEnvelope.
     */
    @Test
    public void testGetWrapEnvelope2() {

        SearchEnvelope instance = new SearchEnvelope(new Envelope(40, 220, 0, 10), SpatialIndexTestData.WGS_84_SRS_INFO);

        Envelope expResult = new Envelope(-180, -140, 0, 10);
        Envelope result = instance.getWrapEnvelope();
        assertEquals(expResult, result);
    }

    /**
     * Test of getWrapEnvelope method, of class SearchEnvelope.
     */
    @Test
    public void testGetWrapEnvelope3() {

        SearchEnvelope instance = new SearchEnvelope(new Envelope(-220, -40, 0, 10), SpatialIndexTestData.WGS_84_SRS_INFO);

        Envelope expResult = new Envelope(140, 180, 0, 10);
        Envelope result = instance.getWrapEnvelope();
        assertEquals(expResult, result);
    }

    /**
     * Test of getWrapEnvelope method, of class SearchEnvelope.
     */
    @Test
    public void testGetWrapEnvelope_OSGB() {

        SearchEnvelope instance = new SearchEnvelope(new Envelope(-220, -40, 0, 10), SpatialIndexTestData.OSGB_SRS_INFO);
        Envelope expResult = null;
        Envelope result = instance.getWrapEnvelope();
        assertEquals(expResult, result);
    }

    /**
     * Test of build method, of class SearchEnvelope.
     */
    @Test
    public void testBuild_3args() {

        GeometryWrapper geometryWrapper = GeometryWrapper.extract("<http://www.opengis.net/def/crs/EPSG/0/4326> POINT(0 0)", WKTDatatype.URI);
        double radius = 10;
        String unitsURI = Unit_URI.KILOMETER_URL;
        SearchEnvelope expResult = new SearchEnvelope(new Envelope(-0.089932, 0.089932, -0.089932, 0.0899320), SpatialIndexTestData.WGS_84_SRS_INFO);
        SearchEnvelope result = SearchEnvelope.build(geometryWrapper, SpatialIndexTestData.WGS_84_SRS_INFO, radius, unitsURI);
        assertEquals(expResult, result);
    }

    /**
     * Test of build method, of class SearchEnvelope.
     */
    @Test
    public void testBuild_3args_OSGB() {

        GeometryWrapper geometryWrapper = GeometryWrapper.extract("<http://www.opengis.net/def/crs/EPSG/0/27700> POINT(0 0)", WKTDatatype.URI);
        double radius = 10;
        String unitsURI = Unit_URI.KILOMETER_URL;
        SearchEnvelope expResult = new SearchEnvelope(new Envelope(-10000, 10000, -10000, 10000), SpatialIndexTestData.OSGB_SRS_INFO);
        SearchEnvelope result = SearchEnvelope.build(geometryWrapper, SpatialIndexTestData.OSGB_SRS_INFO, radius, unitsURI);
        assertEquals(expResult, result);
    }

    /**
     * Test of build method, of class SearchEnvelope.
     */
    @Test
    public void testBuild_GeometryWrapper() {

        GeometryWrapper geometryWrapper = SpatialIndexTestData.PARIS_GEOMETRY_WRAPPER;
        SearchEnvelope expResult = new SearchEnvelope(geometryWrapper.getEnvelope(), SpatialIndexTestData.WGS_84_SRS_INFO);
        SearchEnvelope result = SearchEnvelope.build(geometryWrapper, SpatialIndexTestData.WGS_84_SRS_INFO);
        assertEquals(expResult, result);
    }

    /**
     * Test of testSearchEnvelope method, of class NearbyGeomPF.
     */
    @Test
    public void testCheck() {

        SpatialIndex spatialIndex = SpatialIndexTestData.createTestIndex();

        //Search Envelope
        GeometryWrapper geometryWrapper = SpatialIndexTestData.PARIS_GEOMETRY_WRAPPER;
        float radius = 345;
        String unitsURI = Unit_URI.KILOMETER_URL;
        SearchEnvelope instance = SearchEnvelope.build(geometryWrapper, SpatialIndexTestData.WGS_84_SRS_INFO, radius, unitsURI);

        //Function Test
        HashSet<Resource> expResult = new HashSet<>(Arrays.asList(SpatialIndexTestData.LONDON_FEATURE));
        HashSet<Resource> result = instance.check(spatialIndex);
        assertEquals(expResult, result);
    }

    /**
     * Test of testSearchEnvelope method, of class NearbyGeomPF.
     */
    @Test
    public void testCheck_empty() {

        SpatialIndex spatialIndex = SpatialIndexTestData.createTestIndex();

        //Search Envelope
        GeometryWrapper geometryWrapper = SpatialIndexTestData.PARIS_GEOMETRY_WRAPPER;
        float radius = 2;
        String unitsURI = Unit_URI.KILOMETER_URL;
        SearchEnvelope instance = SearchEnvelope.build(geometryWrapper, SpatialIndexTestData.WGS_84_SRS_INFO, radius, unitsURI);

        //Function Test
        HashSet<Resource> expResult = new HashSet<>();
        HashSet<Resource> result = instance.check(spatialIndex);
        assertEquals(expResult, result);
    }

}
