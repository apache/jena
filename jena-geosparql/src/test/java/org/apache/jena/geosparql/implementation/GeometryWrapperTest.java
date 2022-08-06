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

import org.apache.jena.geosparql.implementation.datatype.GMLDatatype;
import org.apache.jena.geosparql.implementation.datatype.WKTDatatype;
import org.apache.jena.geosparql.implementation.jts.CustomGeometryFactory;
import org.apache.jena.geosparql.implementation.vocabulary.SRS_URI;
import org.apache.jena.geosparql.implementation.vocabulary.Unit_URI;
import org.apache.jena.geosparql.spatial.SpatialIndexTestData;
import org.apache.jena.rdf.model.Literal;
import org.apache.jena.rdf.model.ResourceFactory;
import org.apache.sis.referencing.CRS;
import org.junit.After;
import org.junit.AfterClass;
import static org.junit.Assert.assertEquals;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryFactory;
import org.opengis.geometry.MismatchedDimensionException;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.referencing.operation.TransformException;
import org.opengis.util.FactoryException;

/**
 *
 *
 */
public class GeometryWrapperTest {

    GeometryFactory GEOMETRY_FACTORY = CustomGeometryFactory.theInstance();

    public GeometryWrapperTest() {
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
     * Test of checkTransformSRS method, of class GeometryWrapper.
     *
     * @throws java.lang.Exception
     */
    @Test
    public void testCheckTransformSRS() throws Exception {

        Geometry geometry = GEOMETRY_FACTORY.createPoint(new Coordinate(1.0, 2.0));
        String sourceSRSURI = SRS_URI.WGS84_CRS;
        GeometryWrapper sourceSRSGeometry = new GeometryWrapper(geometry, sourceSRSURI, WKTDatatype.URI, DimensionInfo.XY_POINT);

        //Only the SRS_URI is important in the instance.
        String targetSRSURI = SRS_URI.DEFAULT_WKT_CRS84;
        GeometryWrapper instance = new GeometryWrapper(geometry, targetSRSURI, WKTDatatype.URI, DimensionInfo.XY_POINT);

        //Expecting the coordinates to be reveresed.
        Geometry geometryTarget = GEOMETRY_FACTORY.createPoint(new Coordinate(2.0, 1.0));
        GeometryWrapper expResult = new GeometryWrapper(geometryTarget, targetSRSURI, WKTDatatype.URI, DimensionInfo.XY_POINT);
        GeometryWrapper result = instance.checkTransformSRS(sourceSRSGeometry);
        assertEquals(expResult, result);
    }

    /**
     * Test of getCRS method, of class GeometryWrapper.
     *
     * @throws org.opengis.util.FactoryException
     */
    @Test
    public void testGetCRS() throws FactoryException {

        Geometry geometry = GEOMETRY_FACTORY.createPoint(new Coordinate(1.0, 2.0));
        String sourceSRSURI = SRS_URI.WGS84_CRS;
        GeometryWrapper instance = new GeometryWrapper(geometry, sourceSRSURI, WKTDatatype.URI, DimensionInfo.XY_POINT);

        CoordinateReferenceSystem expResult = CRS.forCode(sourceSRSURI);
        CoordinateReferenceSystem result = instance.getCRS();
        assertEquals(expResult, result);
    }

    /**
     * Test of getXYGeometry method, of class GeometryWrapper.
     */
    @Test
    public void testGetXYGeometry() {

        Geometry geometry = GEOMETRY_FACTORY.createPoint(new Coordinate(1.0, 2.0));
        String sourceSRSURI = SRS_URI.WGS84_CRS;
        GeometryWrapper instance = new GeometryWrapper(geometry, sourceSRSURI, WKTDatatype.URI, DimensionInfo.XY_POINT);

        //Expect the coordinates to be reversed as JTS is x,y and WGS84 is y,x
        Geometry expResult = GEOMETRY_FACTORY.createPoint(new Coordinate(2.0, 1.0));
        Geometry result = instance.getXYGeometry();
        assertEquals(expResult, result);
    }

    /**
     * Test of getXYGeometry method, of class GeometryWrapper.
     *
     * @throws org.opengis.util.FactoryException
     * @throws org.opengis.referencing.operation.TransformException
     */
    @Test
    public void testGetXYGeometry_polygon() throws FactoryException, MismatchedDimensionException, TransformException {

        GeometryWrapper instance = GeometryWrapper.extract("<http://www.opengis.net/def/crs/EPSG/0/4326> POLYGON((10.0 -180.0, 20.0 -180.0, 20.0 -170.0, 10.0 -170.0, 10.0 -180.0))", WKTDatatype.URI);

        Coordinate[] coordinates = {new Coordinate(-180.0, 10.0), new Coordinate(-180.0, 20.0), new Coordinate(-170.0, 20.0), new Coordinate(-170.0, 10.0), new Coordinate(-180.0, 10.0)};
        Geometry expResult = GEOMETRY_FACTORY.createPolygon(coordinates);
        Geometry result = instance.getXYGeometry();
        assertEquals(expResult, result);
    }

    /**
     * Test of getParsingGeometry method, of class GeometryWrapper.
     */
    @Test
    public void testGetParsingGeometry() {

        Geometry geometry = GEOMETRY_FACTORY.createPoint(new Coordinate(1.0, 2.0));
        String sourceSRSURI = SRS_URI.WGS84_CRS;
        GeometryWrapper instance = new GeometryWrapper(geometry, sourceSRSURI, WKTDatatype.URI, DimensionInfo.XY_POINT);

        //Expect coordinates to be same as supplied.
        Geometry expResult = GEOMETRY_FACTORY.createPoint(new Coordinate(1.0, 2.0));
        Geometry result = instance.getParsingGeometry();
        assertEquals(expResult, result);
    }

    /**
     * Test of getSrsURI method, of class GeometryWrapper.
     */
    @Test
    public void testGetSrsURI() {

        Geometry geometry = GEOMETRY_FACTORY.createPoint(new Coordinate(1.0, 2.0));
        String sourceSRSURI = SRS_URI.WGS84_CRS;
        GeometryWrapper instance = new GeometryWrapper(geometry, sourceSRSURI, WKTDatatype.URI, DimensionInfo.XY_POINT);

        String expResult = SRS_URI.WGS84_CRS;
        String result = instance.getSrsURI();
        assertEquals(expResult, result);
    }

    /**
     * Test of getSRID method, of class GeometryWrapper.
     */
    @Test
    public void testGetSRID() {

        Geometry geometry = GEOMETRY_FACTORY.createPoint(new Coordinate(1.0, 2.0));
        String sourceSRSURI = SRS_URI.WGS84_CRS;
        GeometryWrapper instance = new GeometryWrapper(geometry, sourceSRSURI, WKTDatatype.URI, DimensionInfo.XY_POINT);

        String expResult = SRS_URI.WGS84_CRS;
        String result = instance.getSRID();
        assertEquals(expResult, result);
    }

    /**
     * Test of getGeometryDatatypeURI method, of class GeometryWrapper.
     */
    @Test
    public void testGetGeometryDatatypeURI() {

        Geometry geometry = GEOMETRY_FACTORY.createPoint(new Coordinate(1.0, 2.0));
        String sourceSRSURI = SRS_URI.WGS84_CRS;
        GeometryWrapper instance = new GeometryWrapper(geometry, sourceSRSURI, WKTDatatype.URI, DimensionInfo.XY_POINT);

        String expResult = WKTDatatype.URI;
        String result = instance.getGeometryDatatypeURI();
        assertEquals(expResult, result);
    }

    /**
     * Test of distanceEuclidean same SRS_URI method, of class GeometryWrapper.
     *
     * @throws org.opengis.util.FactoryException
     * @throws org.opengis.referencing.operation.TransformException
     */
    @Test
    public void testDistanceSameSRSSameUnit() throws FactoryException, MismatchedDimensionException, TransformException, UnitsConversionException {

        Geometry targetGeo = GEOMETRY_FACTORY.createPoint(new Coordinate(2.0, 1.0));
        String targetSRSURI = SRS_URI.OSGB36_CRS;
        GeometryWrapper targetGeometry = new GeometryWrapper(targetGeo, targetSRSURI, WKTDatatype.URI, DimensionInfo.XY_POINT);

        Geometry instanceGeo = GEOMETRY_FACTORY.createPoint(new Coordinate(12.0, 1.0));
        String instanceSRSURI = SRS_URI.OSGB36_CRS;
        GeometryWrapper instance = new GeometryWrapper(instanceGeo, instanceSRSURI, WKTDatatype.URI, DimensionInfo.XY_POINT);

        //SRS is in metres.
        String distanceUnitsURL = Unit_URI.METRE_URL;

        double expResult = 10.0;
        double result = instance.distanceEuclidean(targetGeometry, distanceUnitsURL);
        assertEquals(expResult, result, 0.0);
    }

    /**
     * Test of distanceEuclidean same SRS_URI method, of class GeometryWrapper.
     *
     * @throws org.opengis.util.FactoryException
     * @throws org.opengis.referencing.operation.TransformException
     */
    @Test(expected = UnitsConversionException.class)
    public void testDistanceSameSRSDifferentUnit_exception() throws FactoryException, MismatchedDimensionException, TransformException, UnitsConversionException {

        Geometry targetGeo = GEOMETRY_FACTORY.createPoint(new Coordinate(385458, 156785)); //LatLon - 51.31, -2.21
        String targetSRSURI = SRS_URI.OSGB36_CRS;
        GeometryWrapper targetGeometry = new GeometryWrapper(targetGeo, targetSRSURI, WKTDatatype.URI, DimensionInfo.XY_POINT);

        Geometry instanceGeo = GEOMETRY_FACTORY.createPoint(new Coordinate(487920, 157518)); //LatLon: 51.31, -0.74
        String instanceSRSURI = SRS_URI.OSGB36_CRS;
        GeometryWrapper instance = new GeometryWrapper(instanceGeo, instanceSRSURI, WKTDatatype.URI, DimensionInfo.XY_POINT);

        //SRS is in metres.
        String distanceUnitsURL = Unit_URI.RADIAN_URL;

        double result = instance.distanceEuclidean(targetGeometry, distanceUnitsURL);
    }

    /**
     * Test of distanceEuclidean different SRS_URI method, of class
     * GeometryWrapper.
     *
     * @throws org.opengis.util.FactoryException
     * @throws org.opengis.referencing.operation.TransformException
     */
    @Test
    public void testDistanceDifferentSRSSameUnit() throws FactoryException, MismatchedDimensionException, TransformException, UnitsConversionException {

        Geometry targetGeo = GEOMETRY_FACTORY.createPoint(new Coordinate(2.0, 1.0));
        String targetSRSURI = SRS_URI.WGS84_CRS;
        GeometryWrapper targetGeometry = new GeometryWrapper(targetGeo, targetSRSURI, WKTDatatype.URI, DimensionInfo.XY_POINT);

        Geometry instanceGeo = GEOMETRY_FACTORY.createPoint(new Coordinate(1.0, 12.0));
        String instanceSRSURI = SRS_URI.DEFAULT_WKT_CRS84;
        GeometryWrapper instance = new GeometryWrapper(instanceGeo, instanceSRSURI, WKTDatatype.URI, DimensionInfo.XY_POINT);

        //SRS is in degrees.
        String distanceUnitsURL = Unit_URI.DEGREE_URL;

        double expResult = 10.0;
        double result = instance.distanceEuclidean(targetGeometry, distanceUnitsURL);
        assertEquals(expResult, result, 0.0);
    }

    /**
     * Test of distanceEuclidean different SRS_URI method, of class
     * GeometryWrapper.
     *
     * @throws org.opengis.util.FactoryException
     * @throws org.opengis.referencing.operation.TransformException
     */
    @Test(expected = UnitsConversionException.class)
    public void testDistanceDifferentSRSDifferentUnit_exception() throws FactoryException, MismatchedDimensionException, TransformException, UnitsConversionException {

        Geometry targetGeo = GEOMETRY_FACTORY.createPoint(new Coordinate(0.0, 1.0));
        String targetSRSURI = SRS_URI.WGS84_CRS;
        GeometryWrapper targetGeometry = new GeometryWrapper(targetGeo, targetSRSURI, WKTDatatype.URI, DimensionInfo.XY_POINT);

        Geometry instanceGeo = GEOMETRY_FACTORY.createPoint(new Coordinate(2.0, 0.0));
        String instanceSRSURI = SRS_URI.DEFAULT_WKT_CRS84;
        GeometryWrapper instance = new GeometryWrapper(instanceGeo, instanceSRSURI, WKTDatatype.URI, DimensionInfo.XY_POINT);

        //SRS is in degrees.
        String distanceUnitsURL = Unit_URI.METRE_URL;

        double result = instance.distanceEuclidean(targetGeometry, distanceUnitsURL);
    }

    /**
     * Test of empty WKT GeometryWrapper.
     *
     */
    @Test
    public void testEmptyWKT() {

        Geometry instanceGeo = GEOMETRY_FACTORY.createPoint();
        String instanceSRSURI = SRS_URI.DEFAULT_WKT_CRS84;
        GeometryWrapper result = new GeometryWrapper(instanceGeo, instanceSRSURI, WKTDatatype.URI, DimensionInfo.XY_POINT);

        GeometryWrapper expResult = GeometryWrapper.getEmptyWKT();
        assertEquals(expResult, result);
    }

    /**
     * Test of empty WKT GeometryWrapper.
     *
     */
    @Test
    public void testEmptyWKTGeometryWrapper() {

        Geometry instanceGeo = GEOMETRY_FACTORY.createPoint();
        String instanceSRSURI = SRS_URI.DEFAULT_WKT_CRS84;
        GeometryWrapper result = new GeometryWrapper(instanceGeo, instanceSRSURI, WKTDatatype.URI, DimensionInfo.XY_POINT);

        GeometryWrapper expResult = GeometryWrapper.getEmptyWKT();
        assertEquals(expResult, result);
    }

    /**
     * Test of empty GML GeometryWrapper.
     *
     */
    @Test
    public void testEmptyGMLGeometryWrapper() {

        Geometry instanceGeo = GEOMETRY_FACTORY.createPoint();
        String instanceSRSURI = SRS_URI.DEFAULT_WKT_CRS84;
        GeometryWrapper result = new GeometryWrapper(instanceGeo, instanceSRSURI, GMLDatatype.URI, DimensionInfo.XY_POINT);

        GeometryWrapper expResult = GeometryWrapper.getEmptyGML();
        assertEquals(expResult, result);
    }

    /**
     * Test of asLiteral.
     *
     */
    @Test
    public void testAsLiteral() {

        String lexicalForm = "POINT(-83.38 33.95)";
        GeometryWrapper instance = WKTDatatype.INSTANCE.parse(lexicalForm);

        Literal result = instance.asLiteral();
        Literal expResult = ResourceFactory.createTypedLiteral(lexicalForm, WKTDatatype.INSTANCE);
        assertEquals(expResult, result);
    }

    /**
     * Test of asLiteral conversion URI.
     *
     */
    @Test
    public void testAsLiteralConversionURI() {

        String lexicalForm = "POINT(-83.38 33.95)";
        GeometryWrapper instance = WKTDatatype.INSTANCE.parse(lexicalForm);

        Literal result = instance.asLiteral(GMLDatatype.URI);
        String gmlGeometryLiteral = "<gml:Point xmlns:gml=\"http://www.opengis.net/gml/3.2\" srsName=\"http://www.opengis.net/def/crs/OGC/1.3/CRS84\"><gml:pos>-83.38 33.95</gml:pos></gml:Point>";
        Literal expResult = ResourceFactory.createTypedLiteral(gmlGeometryLiteral, GMLDatatype.INSTANCE);
        assertEquals(expResult, result);
    }

    /**
     * Test of asLiteral conversion datatype.
     *
     */
    @Test
    public void testAsLiteralConversionDatatype() {

        String lexicalForm = "POINT(-83.38 33.95)";
        GeometryWrapper instance = WKTDatatype.INSTANCE.parse(lexicalForm);

        Literal result = instance.asLiteral(GMLDatatype.INSTANCE);
        String gmlGeometryLiteral = "<gml:Point xmlns:gml=\"http://www.opengis.net/gml/3.2\" srsName=\"http://www.opengis.net/def/crs/OGC/1.3/CRS84\"><gml:pos>-83.38 33.95</gml:pos></gml:Point>";
        Literal expResult = ResourceFactory.createTypedLiteral(gmlGeometryLiteral, GMLDatatype.INSTANCE);
        assertEquals(expResult, result);
    }

    /**
     * Test of distanceGreatCircle method, of class GeometryWrapper.
     *
     * @throws org.opengis.util.FactoryException
     * @throws org.opengis.referencing.operation.TransformException
     */
    @Test
    public void testDistanceGreatCircle() throws FactoryException, MismatchedDimensionException, TransformException {

        GeometryWrapper instance = SpatialIndexTestData.PARIS_GEOMETRY_WRAPPER;
        GeometryWrapper testGeometryWrapper = SpatialIndexTestData.LONDON_GEOMETRY_WRAPPER;
        String unitsURI = Unit_URI.KILOMETER_URL;

        double expResult = 343.77;
        double result = instance.distanceGreatCircle(testGeometryWrapper, unitsURI);
        assertEquals(expResult, result, 0.1);
    }

    /**
     * Test of distanceGreatCircle method, of class GeometryWrapper.
     *
     * @throws org.opengis.util.FactoryException
     * @throws org.opengis.referencing.operation.TransformException
     */
    @Test
    public void testDistanceGreatCircle_polygon() throws FactoryException, MismatchedDimensionException, TransformException {

        GeometryWrapper instance = GeometryWrapper.extract("<http://www.opengis.net/def/crs/EPSG/0/4326> POLYGON((10.0 -180.0, 20.0 -180.0, 20.0 -170.0, 10.0 -170.0, 10.0 -180.0))", WKTDatatype.URI);
        GeometryWrapper testGeometryWrapper = GeometryWrapper.extract("<http://www.opengis.net/def/crs/EPSG/0/4326> POLYGON((10.0 170.0, 20.0 170.0, 20.0 179.0, 10.0 179.0, 10.0 170.0))", WKTDatatype.URI);
        String unitsURI = Unit_URI.KILOMETER_URL;

        double expResult = 104.4890;
        double result = instance.distanceGreatCircle(testGeometryWrapper, unitsURI);
        assertEquals(expResult, result, 0.0001);
    }

    /**
     * Test of distanceGreatCircle method, of class GeometryWrapper.
     *
     * @throws org.opengis.util.FactoryException
     * @throws org.opengis.referencing.operation.TransformException
     */
    @Test
    public void testDistanceGreatCircle_polygon2() throws FactoryException, MismatchedDimensionException, TransformException {

        GeometryWrapper instance = GeometryWrapper.extract("<http://www.opengis.net/def/crs/EPSG/0/4326> POLYGON((10.0 0.0, 20.0 0.0, 20.0 10.0, 10.0 10.0, 10.0 0.0))", WKTDatatype.URI);
        GeometryWrapper testGeometryWrapper = GeometryWrapper.extract("<http://www.opengis.net/def/crs/EPSG/0/4326> POLYGON((10.0 -10.0, 20.0 -10.0, 20.0 -1.0, 10.0 -1.0, 10.0 -10.0))", WKTDatatype.URI);
        String unitsURI = Unit_URI.KILOMETER_URL;

        double expResult = 104.4890;
        double result = instance.distanceGreatCircle(testGeometryWrapper, unitsURI);
        assertEquals(expResult, result, 0.0001);
    }

    /**
     * Test of translateXYGeometry method, of class GeometryWrapper.
     *
     */
    @Test
    public void testTranslateXYGeometry_geographic() {

        GeometryWrapper instance = GeometryWrapper.extract("<http://www.opengis.net/def/crs/EPSG/0/4326> POLYGON((10.0 -180.0, 20.0 -180.0, 20.0 -170.0, 10.0 -170.0, 10.0 -180.0))", WKTDatatype.URI);

        //Exp Result is based on the same WGS84 coordinates but shifted by 360 degrees along longitude.
        GeometryWrapper geometryWrapper = GeometryWrapper.extract("<http://www.opengis.net/def/crs/EPSG/0/4326> POLYGON((10.0 180.0, 20.0 180.0, 20.0 190.0, 10.0 190.0, 10.0 180.0))", WKTDatatype.URI);
        Geometry expResult = geometryWrapper.getXYGeometry();
        Geometry result = instance.translateXYGeometry();
        assertEquals(expResult, result);
    }

    /**
     * Test of translateXYGeometry method, of class GeometryWrapper.
     *
     */
    @Test
    public void testTranslateXYGeometry_non_geographic() {

        GeometryWrapper instance = GeometryWrapper.extract("<http://www.opengis.net/def/crs/EPSG/0/27700> POLYGON((10.0 -180.0, 20.0 -180.0, 20.0 -170.0, 10.0 -170.0, 10.0 -180.0))", WKTDatatype.URI);

        //Exp Result is unchanged as only geographic SRS are translated.
        Geometry expResult = instance.getXYGeometry();
        Geometry result = instance.translateXYGeometry();
        assertEquals(expResult, result);
    }

    /**
     * Test of distanceGreatCircle method, of class GeometryWrapper.
     *
     * @throws org.opengis.util.FactoryException
     * @throws org.opengis.referencing.operation.TransformException
     */
    @Test
    public void testDistanceGreatCircle_overlap() throws FactoryException, MismatchedDimensionException, TransformException {

        GeometryWrapper instance = GeometryWrapper.extract("<http://www.opengis.net/def/crs/EPSG/0/4326> POLYGON((10.0 0.0, 20.0 0.0, 20.0 10.0, 10.0 10.0, 10.0 0.0))", WKTDatatype.URI);
        GeometryWrapper testGeometryWrapper = GeometryWrapper.extract("<http://www.opengis.net/def/crs/EPSG/0/4326> POINT(10.0 0.0)", WKTDatatype.URI);
        String unitsURI = Unit_URI.KILOMETER_URL;

        double expResult = 0.0;
        double result = instance.distanceGreatCircle(testGeometryWrapper, unitsURI);
        assertEquals(expResult, result, 0.0001);
    }

    /**
     * Test of testGetUTMZoneURI_wgs84 method, of class GeometryWrapper.
     *
     * @throws org.opengis.util.FactoryException
     * @throws org.opengis.referencing.operation.TransformException
     */
    @Test
    public void testGetUTMZoneURI_wgs84() throws FactoryException, MismatchedDimensionException, TransformException {

        GeometryWrapper instance = GeometryWrapper.extract("<http://www.opengis.net/def/crs/EPSG/0/4326> POINT(52.28 -1.58)", WKTDatatype.URI);      //Warwick Castle 
        String expResult = "http://www.opengis.net/def/crs/EPSG/0/32630";
        String result = instance.getUTMZoneURI();
        assertEquals(expResult, result);
    }
    
    /**
     * Test of testGetUTMZoneURI_crs84 method, of class GeometryWrapper.
     *
     * @throws org.opengis.util.FactoryException
     * @throws org.opengis.referencing.operation.TransformException
     */
    @Test
    public void testGetUTMZoneURI_crs84() throws FactoryException, MismatchedDimensionException, TransformException {

        GeometryWrapper instance = GeometryWrapper.extract("POINT(-1.58 52.28)", WKTDatatype.URI);      //Warwick Castle 
        String expResult = "http://www.opengis.net/def/crs/EPSG/0/32630";
        String result = instance.getUTMZoneURI();
        assertEquals(expResult, result);
    }
    
}
