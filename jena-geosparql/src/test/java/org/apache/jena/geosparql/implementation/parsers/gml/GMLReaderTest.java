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
package org.apache.jena.geosparql.implementation.parsers.gml;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import org.apache.jena.geosparql.implementation.DimensionInfo;
import org.apache.jena.geosparql.implementation.jts.CoordinateSequenceDimensions;
import org.apache.jena.geosparql.implementation.jts.CustomCoordinateSequence;
import org.apache.jena.geosparql.implementation.jts.CustomGeometryFactory;
import org.apache.jena.geosparql.implementation.vocabulary.SRS_URI;
import org.jdom2.JDOMException;
import org.junit.After;
import org.junit.AfterClass;
import static org.junit.Assert.assertEquals;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.CoordinateXY;
import org.locationtech.jts.geom.CoordinateXYZM;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.LineString;
import org.locationtech.jts.geom.LinearRing;
import org.locationtech.jts.geom.Polygon;
import org.locationtech.jts.util.GeometricShapeFactory;

/**
 *
 *
 */
public class GMLReaderTest {

    private static final GeometryFactory GEOMETRY_FACTORY = CustomGeometryFactory.theInstance();

    public GMLReaderTest() {
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
     * Test of getDimensionInfo method, of class GMLReader.
     */
    @Test
    public void testGetDimensionInfo0() {

        GMLReader instance = new GMLReader(GEOMETRY_FACTORY.createPoint(), 2);
        DimensionInfo expResult = new DimensionInfo(2, 2, 0);
        DimensionInfo result = instance.getDimensionInfo();

        //
        //
        assertEquals(expResult, result);
    }

    /**
     * Test of getDimensionInfo method, of class GMLReader.
     */
    @Test
    public void testGetDimensionInfo2() {

        GMLReader instance = new GMLReader(GEOMETRY_FACTORY.createPoint(new CoordinateXY(11.0, 12.0)), 2);

        DimensionInfo expResult = new DimensionInfo(2, 2, 0);
        DimensionInfo result = instance.getDimensionInfo();

        //
        //
        assertEquals(expResult, result);
    }

    /**
     * Test of getDimensionInfo method, of class GMLReader.
     */
    @Test
    public void testGetDimensionInfo3a() {

        GMLReader instance = new GMLReader(GEOMETRY_FACTORY.createPoint(new Coordinate(11.0, 12.0, 13.0)), 3);
        DimensionInfo expResult = new DimensionInfo(3, 3, 0);
        DimensionInfo result = instance.getDimensionInfo();

        //
        //
        assertEquals(expResult, result);
    }

    /**
     * Test of getDimensionInfo method, of class GMLReader. GML standards don't
     * seem to define a separate spatial dimension, i.e. no distinct M
     * dimension.<br>
     * 07-036, page 310 states "srsDimension is the dimension of the coordinate
     * reference system as stated in the coordinate reference system
     * definition."<br>
     * 10-100r3, page 22 states "c) coordinate reference systems may have 1, 2
     * or 3 dimensions".
     */
    /*
    @Test
    @Ignore
    public void testGetDimensionInfo3b() {

        GMLReader instance = new GMLReader(GEOMETRY_FACTORY.createPoint(new CoordinateXYM(11.0, 12.0, 13.0)), 3);
        DimensionInfo expResult = new DimensionInfo(3, 2, 0);
        DimensionInfo result = instance.getDimensionInfo();

        //
        //
        assertEquals(expResult, result);
    }
     */
    /**
     * Test of getDimensionInfo method, of class GMLReader.
     */
    @Test
    public void testGetDimensionInfo4() {

        GMLReader instance = new GMLReader(GEOMETRY_FACTORY.createPoint(new CoordinateXYZM(11.0, 12.0, 13.0, 14.0)), 4);
        DimensionInfo expResult = new DimensionInfo(4, 3, 0);
        DimensionInfo result = instance.getDimensionInfo();

        //
        //
        assertEquals(expResult, result);
    }

    /**
     * Test of getGeometry method, of class GMLReader.
     */
    @Test
    public void testGetGeometryPoint() {

        GMLReader instance = new GMLReader(GEOMETRY_FACTORY.createPoint(new CoordinateXY(11.0, 12.0)), 2);
        Geometry expResult = GEOMETRY_FACTORY.createPoint(new CustomCoordinateSequence(CoordinateSequenceDimensions.XY, "11.0 12.0"));
        Geometry result = instance.getGeometry();

        //
        //
        assertEquals(expResult, result);
    }

    /**
     * Test of getGeometry method, of class GMLReader.
     */
    @Test
    public void testGetGeometryPointZ() {

        GMLReader instance = new GMLReader(GEOMETRY_FACTORY.createPoint(new Coordinate(11.0, 12.0, 13.0)), 2);
        Geometry expResult = GEOMETRY_FACTORY.createPoint(new CustomCoordinateSequence(CoordinateSequenceDimensions.XYZ, "11.0 12.0 8.0"));
        Geometry result = instance.getGeometry();

        //
        //
        assertEquals(expResult, result);
    }

    /**
     * Test of extract method, of class GMLReader.
     *
     * @throws org.jdom2.JDOMException
     * @throws java.io.IOException
     */
    @Test
    public void testExtractPoint2() throws JDOMException, IOException {

        String gmlText = "<gml:Point xmlns:gml=\"http://www.opengis.net/ont/gml\" srsName=\"http://www.opengis.net/def/crs/EPSG/0/27700\"><gml:pos>11.0 12.0</gml:pos></gml:Point>";
        GMLReader expResult = new GMLReader(GEOMETRY_FACTORY.createPoint(new CoordinateXY(11.0, 12.0)), 2, SRS_URI.OSGB36_CRS);
        GMLReader result = GMLReader.extract(gmlText);

        //
        //
        assertEquals(expResult, result);
    }

    /**
     * Test of extract method, of class GMLReader.
     *
     * @throws org.jdom2.JDOMException
     * @throws java.io.IOException
     */
    @Test
    public void testExtractPoint3() throws JDOMException, IOException {


        String gmlText = "<gml:Point xmlns:gml=\"http://www.opengis.net/ont/gml\" srsName=\"http://www.opengis.net/def/crs/EPSG/0/27700\"><gml:pos>11.0 12.0 8.0</gml:pos></gml:Point>";
        GMLReader expResult = new GMLReader(GEOMETRY_FACTORY.createPoint(new Coordinate(11.0, 12.0, 8.0)), 2, SRS_URI.OSGB36_CRS);
        GMLReader result = GMLReader.extract(gmlText);

        //
        //
        assertEquals(expResult, result);
    }

    /**
     * Test of extract method, of class GMLReader.
     *
     * @throws org.jdom2.JDOMException
     * @throws java.io.IOException
     */
    /*
    @Test
    @Ignore
    public void testExtractPoint3b() throws JDOMException, IOException {


        String gmlText = "<gml:Point xmlns:gml=\"http://www.opengis.net/ont/gml\" srsName=\"http://www.opengis.net/def/crs/EPSG/0/27700\"><gml:pos>11.0 12.0 5.0</gml:pos></gml:Point>";
        GMLReader expResult = new GMLReader(GEOMETRY_FACTORY.createPoint(new CoordinateXYM(11.0, 12.0, 5.0)), 2, SRS_URI.OSGB36_CRS);
        GMLReader result = GMLReader.extract(gmlText);

        //
        //
        assertEquals(expResult, result);
    }
     */
    /**
     * Test of extract method, of class GMLReader.
     *
     * @throws org.jdom2.JDOMException
     * @throws java.io.IOException
     */
    @Test
    public void testExtractPoint4() throws JDOMException, IOException {


        String gmlText = "<gml:Point xmlns:gml=\"http://www.opengis.net/ont/gml\" srsName=\"http://www.opengis.net/def/crs/EPSG/0/27700\"><gml:pos>11.0 12.0 8.0 5.0</gml:pos></gml:Point>";
        GMLReader expResult = new GMLReader(GEOMETRY_FACTORY.createPoint(new CoordinateXYZM(11.0, 12.0, 8.0, 5.0)), 2, SRS_URI.OSGB36_CRS);
        GMLReader result = GMLReader.extract(gmlText);

        //
        //
        assertEquals(expResult, result);
    }

    /**
     * Test of extract method, of class GMLReader.
     *
     * @throws org.jdom2.JDOMException
     * @throws java.io.IOException
     */
    @Test
    public void testExtractPolygon() throws JDOMException, IOException {


        String gmlText = "<gml:Polygon xmlns:gml=\"http://www.opengis.net/ont/gml\" srsName=\"http://www.opengis.net/def/crs/EPSG/0/27700\"><gml:exterior><gml:LinearRing><gml:posList>30 10 40 40 20 40 10 20 30 10</gml:posList></gml:LinearRing></gml:exterior></gml:Polygon>";
        Geometry geometry = GEOMETRY_FACTORY.createPolygon(new CustomCoordinateSequence(CoordinateSequenceDimensions.XY, "30 10, 40 40, 20 40, 10 20, 30 10"));
        GMLReader expResult = new GMLReader(geometry, 2, SRS_URI.OSGB36_CRS);
        GMLReader result = GMLReader.extract(gmlText);

        //
        //
        assertEquals(expResult, result);
    }

    /**
     * Test of extract method, of class GMLReader.
     *
     * @throws org.jdom2.JDOMException
     * @throws java.io.IOException
     */
    @Test
    public void testExtractPolygonHole() throws JDOMException, IOException {


        LinearRing shell = GEOMETRY_FACTORY.createLinearRing(new CustomCoordinateSequence(CoordinateSequenceDimensions.XY, "30 10, 40 40, 20 40, 10 20, 30 10"));
        LinearRing[] holes = new LinearRing[]{GEOMETRY_FACTORY.createLinearRing(new CustomCoordinateSequence(CoordinateSequenceDimensions.XY, "20 30, 35 35, 30 20, 20 30"))};
        Geometry geometry = GEOMETRY_FACTORY.createPolygon(shell, holes);
        GMLReader expResult = new GMLReader(geometry, 2, SRS_URI.OSGB36_CRS);

        String gmlText = "<gml:Polygon xmlns:gml=\"http://www.opengis.net/ont/gml\" srsName=\"http://www.opengis.net/def/crs/EPSG/0/27700\"><gml:exterior><gml:LinearRing><gml:posList>30 10 40 40 20 40 10 20 30 10</gml:posList></gml:LinearRing></gml:exterior><gml:interior><gml:LinearRing><gml:posList>20 30 35 35 30 20 20 30</gml:posList></gml:LinearRing></gml:interior></gml:Polygon>";
        GMLReader result = GMLReader.extract(gmlText);

        //
        //
        assertEquals(expResult, result);
    }

    /**
     * Test of extract method, of class GMLReader.
     *
     * @throws org.jdom2.JDOMException
     * @throws java.io.IOException
     */
    @Test
    public void testExtractLineString() throws JDOMException, IOException {


        Geometry geometry = GEOMETRY_FACTORY.createLineString(new CustomCoordinateSequence(CoordinateSequenceDimensions.XY, "11.0 12.1, 15.0 8.0"));
        GMLReader expResult = new GMLReader(geometry, 2, SRS_URI.OSGB36_CRS);

        String gmlText = "<gml:LineString xmlns:gml=\"http://www.opengis.net/ont/gml\" srsName=\"http://www.opengis.net/def/crs/EPSG/0/27700\"><gml:posList>11 12.1 15 8</gml:posList></gml:LineString>";
        GMLReader result = GMLReader.extract(gmlText);

        //
        //
        assertEquals(expResult, result);
    }

    /**
     * Test of extract method, of class GMLReader.
     *
     * @throws org.jdom2.JDOMException
     * @throws java.io.IOException
     */
    @Test
    public void testExtractLineStringSegment() throws JDOMException, IOException {


        Geometry geometry = GEOMETRY_FACTORY.createLineString(new CustomCoordinateSequence(CoordinateSequenceDimensions.XY, "11.0 12.1, 15.0 8.0, 20.0 14.0, 25.0 14.0"));
        GMLReader expResult = new GMLReader(geometry, 2, SRS_URI.OSGB36_CRS);

        String gmlText = "<gml:Curve xmlns:gml=\"http://www.opengis.net/ont/gml\" srsName=\"http://www.opengis.net/def/crs/EPSG/0/27700\"><gml:segments><gml:LineStringSegment><gml:posList>11 12.1 15 8</gml:posList></gml:LineStringSegment><gml:LineStringSegment><gml:posList>15.0 8.0 20.0 14.0 25.0 14.0</gml:posList></gml:LineStringSegment></gml:segments></gml:Curve>";
        GMLReader result = GMLReader.extract(gmlText);

        //
        //
        assertEquals(expResult, result);
    }

    /**
     * Test of extract method, of class GMLReader.
     *
     * @throws org.jdom2.JDOMException
     * @throws java.io.IOException
     */
    @Test
    public void testExtractArc() throws JDOMException, IOException {


        GeometricShapeFactory shapeFactory = new GeometricShapeFactory(GEOMETRY_FACTORY);
        shapeFactory.setCentre(new CoordinateXY(0, 0));
        shapeFactory.setWidth(10);
        Geometry arc = shapeFactory.createArc(0, Math.PI);  //Semi-cicle centred around 0,0 and radius 5.
        GMLReader expResult = new GMLReader(arc, 2, SRS_URI.OSGB36_CRS);

        String gmlText = "<gml:Curve xmlns:gml=\"http://www.opengis.net/ont/gml\" srsName=\"http://www.opengis.net/def/crs/EPSG/0/27700\"><gml:segments><gml:Arc><gml:posList>5 0 0 5 -5 0</gml:posList></gml:Arc></gml:segments></gml:Curve>";
        GMLReader result = GMLReader.extract(gmlText);

        //
        //
        assertEquals(expResult, result);
    }

    /**
     * Test of extract method, of class GMLReader.
     *
     * @throws org.jdom2.JDOMException
     * @throws java.io.IOException
     */
    @Test
    public void testExtractCircle() throws JDOMException, IOException {


        GeometricShapeFactory shapeFactory = new GeometricShapeFactory(GEOMETRY_FACTORY);
        shapeFactory.setCentre(new CoordinateXY(0, 0));
        shapeFactory.setWidth(10);
        Geometry circle = shapeFactory.createCircle().getExteriorRing();
        GMLReader expResult = new GMLReader(circle, 2, SRS_URI.OSGB36_CRS);

        String gmlText = "<gml:Curve xmlns:gml=\"http://www.opengis.net/ont/gml\" srsName=\"http://www.opengis.net/def/crs/EPSG/0/27700\"><gml:segments><gml:Circle><gml:posList>5 0 0 5 -5 0</gml:posList></gml:Circle></gml:segments></gml:Curve>";
        GMLReader result = GMLReader.extract(gmlText);

        //
        //
        assertEquals(expResult, result);
    }

    /**
     * Test of extract method, of class GMLReader.
     *
     * @throws org.jdom2.JDOMException
     * @throws java.io.IOException
     */
    @Test
    public void testExtractCircleByCentrePoint() throws JDOMException, IOException {


        GeometricShapeFactory shapeFactory = new GeometricShapeFactory(GEOMETRY_FACTORY);
        shapeFactory.setCentre(new Coordinate(0, 0));
        shapeFactory.setSize(10);
        Geometry circle = shapeFactory.createCircle().getExteriorRing();
        GMLReader expResult = new GMLReader(circle, 2, SRS_URI.OSGB36_CRS);
        String gmlText = "<gml:Curve xmlns:gml=\"http://www.opengis.net/ont/gml\" srsName=\"http://www.opengis.net/def/crs/EPSG/0/27700\"><gml:segments><gml:CircleByCenterPoint ><gml:pos>0 0</gml:pos><gml:radius uom=\"http://www.opengis.net/def/uom/OGC/1.0/metre\">5.0</gml:radius></gml:CircleByCenterPoint></gml:segments></gml:Curve>";
        GMLReader result = GMLReader.extract(gmlText);

        //
        //
        assertEquals(expResult, result);
    }

    /**
     * Test of extract method, of class GMLReader.
     *
     * @throws org.jdom2.JDOMException
     * @throws java.io.IOException
     */
    @Test
    public void testExtractMultiPoint() throws JDOMException, IOException {


        Geometry geometry = GEOMETRY_FACTORY.createMultiPoint(new CustomCoordinateSequence(CoordinateSequenceDimensions.XY, "10 40, 40 30, 20 20, 30 10"));
        GMLReader expResult = new GMLReader(geometry, 2, SRS_URI.OSGB36_CRS);

        String gmlText = "<gml:MultiPoint xmlns:gml=\"http://www.opengis.net/ont/gml\" srsName=\"http://www.opengis.net/def/crs/EPSG/0/27700\"><gml:pointMember><gml:Point srsName=\"http://www.opengis.net/def/crs/EPSG/0/27700\"><gml:pos>10 40</gml:pos></gml:Point></gml:pointMember><gml:pointMember><gml:Point srsName=\"http://www.opengis.net/def/crs/EPSG/0/27700\"><gml:pos>40 30</gml:pos></gml:Point></gml:pointMember><gml:pointMember><gml:Point srsName=\"http://www.opengis.net/def/crs/EPSG/0/27700\"><gml:pos>20 20</gml:pos></gml:Point></gml:pointMember><gml:pointMember><gml:Point srsName=\"http://www.opengis.net/def/crs/EPSG/0/27700\"><gml:pos>30 10</gml:pos></gml:Point></gml:pointMember></gml:MultiPoint>";
        GMLReader result = GMLReader.extract(gmlText);

        //
        //
        assertEquals(expResult, result);
    }

    /**
     * Test of extract method, of class GMLReader.
     *
     * @throws org.jdom2.JDOMException
     * @throws java.io.IOException
     */
    @Test
    public void testExtractMutliCurve() throws JDOMException, IOException {


        LineString[] lineStrings = new LineString[2];
        lineStrings[0] = GEOMETRY_FACTORY.createLineString(new CustomCoordinateSequence(CoordinateSequenceDimensions.XY, "10 10, 20 20, 10 40"));
        lineStrings[1] = GEOMETRY_FACTORY.createLineString(new CustomCoordinateSequence(CoordinateSequenceDimensions.XY, "40 40, 30 30, 40 20, 30 10"));
        Geometry geometry = GEOMETRY_FACTORY.createMultiLineString(lineStrings);
        GMLReader expResult = new GMLReader(geometry, 2, SRS_URI.OSGB36_CRS);

        String gmlText = "<gml:MultiCurve xmlns:gml=\"http://www.opengis.net/ont/gml\" srsName=\"http://www.opengis.net/def/crs/EPSG/0/27700\"><gml:curveMember><gml:LineString srsName=\"http://www.opengis.net/def/crs/EPSG/0/27700\"><gml:posList>10 10 20 20 10 40</gml:posList></gml:LineString></gml:curveMember><gml:curveMember><gml:LineString srsName=\"http://www.opengis.net/def/crs/EPSG/0/27700\"><gml:posList>40 40 30 30 40 20 30 10</gml:posList></gml:LineString></gml:curveMember></gml:MultiCurve>";
        GMLReader result = GMLReader.extract(gmlText);

        //
        //
        assertEquals(expResult, result);
    }

    /**
     * Test of extract method, of class GMLReader.
     *
     * @throws org.jdom2.JDOMException
     * @throws java.io.IOException
     */
    @Test
    public void testExtractMultiSurface() throws JDOMException, IOException {


        Polygon[] polygons = new Polygon[2];
        polygons[0] = GEOMETRY_FACTORY.createPolygon(new CustomCoordinateSequence(CoordinateSequenceDimensions.XY, "40 40, 20 45, 45 30, 40 40"));
        polygons[1] = GEOMETRY_FACTORY.createPolygon(new CustomCoordinateSequence(CoordinateSequenceDimensions.XY, "20 35, 10 30, 10 10, 30 5, 45 20, 20 35"));
        Geometry geometry = GEOMETRY_FACTORY.createMultiPolygon(polygons);
        GMLReader expResult = new GMLReader(geometry, 2, SRS_URI.OSGB36_CRS);

        String gmlText = "<gml:MultiSurface xmlns:gml=\"http://www.opengis.net/ont/gml\" srsName=\"http://www.opengis.net/def/crs/EPSG/0/27700\"><gml:surfaceMember><gml:Polygon srsName=\"http://www.opengis.net/def/crs/EPSG/0/27700\"><gml:exterior><gml:LinearRing><gml:posList>40 40 20 45 45 30 40 40</gml:posList></gml:LinearRing></gml:exterior></gml:Polygon></gml:surfaceMember><gml:surfaceMember><gml:Polygon srsName=\"http://www.opengis.net/def/crs/EPSG/0/27700\"><gml:exterior><gml:LinearRing><gml:posList>20 35 10 30 10 10 30 5 45 20 20 35</gml:posList></gml:LinearRing></gml:exterior></gml:Polygon></gml:surfaceMember></gml:MultiSurface>";
        GMLReader result = GMLReader.extract(gmlText);

        //
        //
        assertEquals(expResult, result);
    }

    /**
     * Test of extract method, of class GMLReader.
     *
     * @throws org.jdom2.JDOMException
     * @throws java.io.IOException
     */
    @Test
    public void testExtractMultiSurface2() throws JDOMException, IOException {


        Polygon[] polygons = new Polygon[2];
        polygons[0] = GEOMETRY_FACTORY.createPolygon(new CustomCoordinateSequence(CoordinateSequenceDimensions.XY, "40 40, 20 45, 45 30, 40 40"));
        LinearRing shell = GEOMETRY_FACTORY.createLinearRing(new CustomCoordinateSequence(CoordinateSequenceDimensions.XY, "20 35, 10 30, 10 10, 30 5, 45 20, 20 35"));
        LinearRing[] holes = new LinearRing[]{GEOMETRY_FACTORY.createLinearRing(new CustomCoordinateSequence(CoordinateSequenceDimensions.XY, "30 20, 20 15, 20 25, 30 20"))};
        polygons[1] = GEOMETRY_FACTORY.createPolygon(shell, holes);
        Geometry geometry = GEOMETRY_FACTORY.createMultiPolygon(polygons);
        GMLReader expResult = new GMLReader(geometry, 2, SRS_URI.OSGB36_CRS);

        String gmlText = "<gml:MultiSurface xmlns:gml=\"http://www.opengis.net/ont/gml\" srsName=\"http://www.opengis.net/def/crs/EPSG/0/27700\"><gml:surfaceMember><gml:Polygon srsName=\"http://www.opengis.net/def/crs/EPSG/0/27700\"><gml:exterior><gml:LinearRing><gml:posList>40 40 20 45 45 30 40 40</gml:posList></gml:LinearRing></gml:exterior></gml:Polygon></gml:surfaceMember><gml:surfaceMember><gml:Polygon srsName=\"http://www.opengis.net/def/crs/EPSG/0/27700\"><gml:exterior><gml:LinearRing><gml:posList>20 35 10 30 10 10 30 5 45 20 20 35</gml:posList></gml:LinearRing></gml:exterior><gml:interior><gml:LinearRing><gml:posList>30 20 20 15 20 25 30 20</gml:posList></gml:LinearRing></gml:interior></gml:Polygon></gml:surfaceMember></gml:MultiSurface>";
        GMLReader result = GMLReader.extract(gmlText);

        //
        //
        assertEquals(expResult, result);
    }

    /**
     * Test of extract method, of class GMLReader.
     *
     * @throws org.jdom2.JDOMException
     * @throws java.io.IOException
     */
    @Test
    public void testExtractMultiGeometry() throws JDOMException, IOException {


        Geometry[] geometries = new Geometry[2];
        geometries[0] = GEOMETRY_FACTORY.createPoint(new CustomCoordinateSequence(CoordinateSequenceDimensions.XY, "4 6"));
        geometries[1] = GEOMETRY_FACTORY.createLineString(new CustomCoordinateSequence(CoordinateSequenceDimensions.XY, "4 6,7 10"));
        Geometry geometry = GEOMETRY_FACTORY.createGeometryCollection(geometries);
        GMLReader expResult = new GMLReader(geometry, 2, SRS_URI.OSGB36_CRS);

        String gmlText = "<gml:MultiGeometry xmlns:gml=\"http://www.opengis.net/ont/gml\" srsName=\"http://www.opengis.net/def/crs/EPSG/0/27700\"><gml:geometryMember><gml:Point srsName=\"http://www.opengis.net/def/crs/EPSG/0/27700\"><gml:pos>4 6</gml:pos></gml:Point></gml:geometryMember><gml:geometryMember><gml:LineString srsName=\"http://www.opengis.net/def/crs/EPSG/0/27700\"><gml:posList>4 6 7 10</gml:posList></gml:LineString></gml:geometryMember></gml:MultiGeometry>";
        GMLReader result = GMLReader.extract(gmlText);

        //
        //
        assertEquals(expResult, result);
    }

    /**
     * Test of buildPointEmpty method, of class GMLReader.
     *
     * @throws org.jdom2.JDOMException
     * @throws java.io.IOException
     */
    @Test
    public void testBuildPointEmpty() throws JDOMException, IOException {

        GMLReader instance = GMLReader.extract("<gml:Point xmlns:gml=\"http://www.opengis.net/ont/gml\" srsName=\"http://www.opengis.net/def/crs/EPSG/0/27700\"></gml:Point>");
        Geometry result = instance.getGeometry();

        Geometry expResult = GEOMETRY_FACTORY.createPoint();

        //
        //
        assertEquals(expResult, result);
    }

    /**
     * Test of buildLineStringEmpty method, of class GMLReader.
     *
     * @throws org.jdom2.JDOMException
     * @throws java.io.IOException
     */
    @Test
    public void testBuildLineStringEmpty() throws JDOMException, IOException {

        GMLReader instance = GMLReader.extract("<gml:LineString xmlns:gml=\"http://www.opengis.net/ont/gml\" srsName=\"http://www.opengis.net/def/crs/EPSG/0/27700\"></gml:LineString>");
        Geometry result = instance.getGeometry();

        Geometry expResult = GEOMETRY_FACTORY.createLineString();

        //
        //
        assertEquals(expResult, result);
    }

    /**
     * Test of buildCurveEmpty method, of class GMLReader.
     *
     * @throws org.jdom2.JDOMException
     * @throws java.io.IOException
     */
    @Test
    public void testBuildCurveEmpty() throws JDOMException, IOException {

        GMLReader instance = GMLReader.extract("<gml:Curve xmlns:gml=\"http://www.opengis.net/ont/gml\" srsName=\"http://www.opengis.net/def/crs/EPSG/0/27700\"></gml:Curve>");
        Geometry result = instance.getGeometry();

        Geometry expResult = GEOMETRY_FACTORY.createLineString();

        //
        //
        assertEquals(expResult, result);
    }

    /**
     * Test of buildPolygonEmpty method, of class GMLReader.
     *
     * @throws org.jdom2.JDOMException
     * @throws java.io.IOException
     */
    @Test
    public void testBuildPolygonEmpty() throws JDOMException, IOException {

        GMLReader instance = GMLReader.extract("<gml:Polygon xmlns:gml=\"http://www.opengis.net/ont/gml\" srsName=\"http://www.opengis.net/def/crs/EPSG/0/27700\"></gml:Polygon>");
        Geometry result = instance.getGeometry();

        Geometry expResult = GEOMETRY_FACTORY.createPolygon();

        //
        //
        assertEquals(expResult, result);
    }

    /**
     * Test of buildMultiPointEmpty method, of class GMLReader.
     *
     * @throws org.jdom2.JDOMException
     * @throws java.io.IOException
     */
    @Test
    public void testBuildMultiPointEmpty() throws JDOMException, IOException {

        GMLReader instance = GMLReader.extract("<gml:MultiPoint xmlns:gml=\"http://www.opengis.net/ont/gml\" srsName=\"http://www.opengis.net/def/crs/EPSG/0/27700\"></gml:MultiPoint>");
        Geometry result = instance.getGeometry();

        Geometry expResult = GEOMETRY_FACTORY.createMultiPoint();

        //
        //
        assertEquals(expResult, result);
    }

    /**
     * Test of buildMultiCurve method, of class GMLReader.
     *
     * @throws org.jdom2.JDOMException
     * @throws java.io.IOException
     */
    @Test
    public void testBuildMultiCurveEmpty() throws JDOMException, IOException {

        GMLReader instance = GMLReader.extract("<gml:MultiCurve xmlns:gml=\"http://www.opengis.net/ont/gml\" srsName=\"http://www.opengis.net/def/crs/EPSG/0/27700\"></gml:MultiCurve>");
        Geometry result = instance.getGeometry();

        Geometry expResult = GEOMETRY_FACTORY.createMultiLineString();

        //
        //
        assertEquals(expResult, result);
    }

    /**
     * Test of buildMultiSurfaceEmpty method, of class GMLReader.
     *
     * @throws org.jdom2.JDOMException
     * @throws java.io.IOException
     */
    @Test
    public void testBuildMultiSurfaceEmpty() throws JDOMException, IOException {

        GMLReader instance = GMLReader.extract("<gml:MultiSurface xmlns:gml=\"http://www.opengis.net/ont/gml\" srsName=\"http://www.opengis.net/def/crs/EPSG/0/27700\"></gml:MultiSurface>");
        Geometry result = instance.getGeometry();

        Geometry expResult = GEOMETRY_FACTORY.createMultiPolygon();

        //
        //
        assertEquals(expResult, result);
    }

    /**
     * Test of buildMultiGeometryEmpty method, of class GMLReader.
     *
     * @throws org.jdom2.JDOMException
     * @throws java.io.IOException
     */
    @Test
    public void testBuildMultiGeometryEmpty() throws JDOMException, IOException {

        GMLReader instance = GMLReader.extract("<gml:MultiGeometry xmlns:gml=\"http://www.opengis.net/ont/gml\" srsName=\"http://www.opengis.net/def/crs/EPSG/0/27700\"></gml:MultiGeometry>");
        Geometry result = instance.getGeometry();

        Geometry expResult = GEOMETRY_FACTORY.createGeometryCollection();

        //
        //
        assertEquals(expResult, result);
    }

    /**
     * Test of buildEmpty method, of class GMLReader.<br>
     * Req 16 An empty geo:gmlLiteral shall be interpreted as an empty geometry.
     *
     * @throws org.jdom2.JDOMException
     * @throws java.io.IOException
     */
    @Test
    public void testBuildEmpty() throws JDOMException, IOException {

        GMLReader instance = GMLReader.extract("");
        Geometry result = instance.getGeometry();

        CustomCoordinateSequence pointSequence = new CustomCoordinateSequence(CoordinateSequenceDimensions.XY, "");
        Geometry expResult = GEOMETRY_FACTORY.createPoint(pointSequence);

        //
        //
        assertEquals(expResult, result);
    }

    /**
     * Test of findCentre method, of class GMLReader.
     */
    @Test
    public void testFindCentre() {

        List<Coordinate> coordinates = Arrays.asList(new Coordinate(-3, 4), new Coordinate(4, 5), new Coordinate(1, -4));
        Coordinate expResult = new Coordinate(1, 1);
        Coordinate result = GMLReader.findCentre(coordinates);

        //
        //
        assertEquals(expResult, result);
    }

    /**
     * Test of findAngle method, of class GMLReader.
     */
    @Test
    public void testFindAngle() {

        Coordinate coord0 = new Coordinate(0, 0);
        Coordinate coord1 = new Coordinate(5, 5);
        double expResult = Math.PI / 4; //45 degrees from x-axis.
        double result = GMLReader.findAngle(coord0, coord1);

        //
        //
        assertEquals(expResult, result, 0.0);
    }

}
