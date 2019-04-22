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

import org.apache.jena.geosparql.implementation.parsers.gml.GMLWriter;
import org.apache.jena.geosparql.implementation.DimensionInfo;
import org.apache.jena.geosparql.implementation.GeometryWrapper;
import org.apache.jena.geosparql.implementation.datatype.GMLDatatype;
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
public class GMLWriterTest {

    public final String GML_SRS_NAMESPACE = "urn:ogc:def:crs:EPSG::27700";

    public GMLWriterTest() {
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

    @Test
    public void testWritePoint() {
        System.out.println("writePoint");
        Geometry geometry = GEOMETRY_FACTORY.createPoint(new CustomCoordinateSequence(CoordinateSequenceDimensions.XY, "11.0 12.1"));
        GeometryWrapper geometryWrapper = new GeometryWrapper(geometry, GML_SRS_NAMESPACE, GMLDatatype.URI, new DimensionInfo(2, 2, 0));

        String result = GMLWriter.write(geometryWrapper);
        String expResult = "<gml:Point xmlns:gml=\"http://www.opengis.net/ont/gml\" srsName=\"urn:ogc:def:crs:EPSG::27700\"><gml:pos>11 12.1</gml:pos></gml:Point>";

        //System.out.println("Expected: " + expResult);
        //System.out.println("Result: " + result);
        assertEquals(expResult.trim(), result.trim());

    }

    @Test
    public void testWriteLineString() {
        System.out.println("writeLineString");
        Geometry geometry = GEOMETRY_FACTORY.createLineString(new CustomCoordinateSequence(CoordinateSequenceDimensions.XY, "11.0 12.1, 15.0 8.0"));
        GeometryWrapper geometryWrapper = new GeometryWrapper(geometry, GML_SRS_NAMESPACE, GMLDatatype.URI, new DimensionInfo(2, 2, 0));

        String result = GMLWriter.write(geometryWrapper);
        String expResult = "<gml:LineString xmlns:gml=\"http://www.opengis.net/ont/gml\" srsName=\"urn:ogc:def:crs:EPSG::27700\"><gml:posList>11 12.1 15 8</gml:posList></gml:LineString>";

        //System.out.println("Expected: " + expResult);
        //System.out.println("Result: " + result);
        assertEquals(expResult.trim(), result.trim());

    }

    @Test
    public void testWritePolygon() {
        System.out.println("writePolygon");
        Geometry geometry = GEOMETRY_FACTORY.createPolygon(new CustomCoordinateSequence(CoordinateSequenceDimensions.XY, "30 10, 40 40, 20 40, 10 20, 30 10"));
        GeometryWrapper geometryWrapper = new GeometryWrapper(geometry, GML_SRS_NAMESPACE, GMLDatatype.URI, new DimensionInfo(2, 2, 0));

        String result = GMLWriter.write(geometryWrapper);
        String expResult = "<gml:Polygon xmlns:gml=\"http://www.opengis.net/ont/gml\" srsName=\"urn:ogc:def:crs:EPSG::27700\"><gml:exterior><gml:posList>30 10 40 40 20 40 10 20 30 10</gml:posList></gml:exterior></gml:Polygon>";

        //System.out.println("Expected: " + expResult);
        //System.out.println("Result: " + result);
        assertEquals(expResult.trim(), result.trim());
    }

    @Test
    public void testWritePolygon2() {
        System.out.println("writePolygon2");
        LinearRing shell = GEOMETRY_FACTORY.createLinearRing(new CustomCoordinateSequence(CoordinateSequenceDimensions.XY, "30 10, 40 40, 20 40, 10 20, 30 10"));
        LinearRing[] holes = new LinearRing[]{GEOMETRY_FACTORY.createLinearRing(new CustomCoordinateSequence(CoordinateSequenceDimensions.XY, "20 30, 35 35, 30 20, 20 30"))};
        Geometry geometry = GEOMETRY_FACTORY.createPolygon(shell, holes);
        GeometryWrapper geometryWrapper = new GeometryWrapper(geometry, GML_SRS_NAMESPACE, GMLDatatype.URI, new DimensionInfo(2, 2, 0));

        String result = GMLWriter.write(geometryWrapper);
        String expResult = "<gml:Polygon xmlns:gml=\"http://www.opengis.net/ont/gml\" srsName=\"urn:ogc:def:crs:EPSG::27700\"><gml:exterior><gml:posList>30 10 40 40 20 40 10 20 30 10</gml:posList></gml:exterior><gml:interior><gml:posList>20 30 35 35 30 20 20 30</gml:posList></gml:interior></gml:Polygon>";

        //System.out.println("Expected: " + expResult);
        //System.out.println("Result: " + result);
        assertEquals(expResult.trim(), result.trim());
    }

    @Test
    public void testWriteMultiPoint() {
        System.out.println("writeMultiPoint");
        Geometry geometry = GEOMETRY_FACTORY.createMultiPoint(new CustomCoordinateSequence(CoordinateSequenceDimensions.XY, "10 40, 40 30, 20 20, 30 10"));
        GeometryWrapper geometryWrapper = new GeometryWrapper(geometry, GML_SRS_NAMESPACE, GMLDatatype.URI, new DimensionInfo(2, 2, 0));

        String result = GMLWriter.write(geometryWrapper);
        String expResult = "<gml:MultiPoint xmlns:gml=\"http://www.opengis.net/ont/gml\" srsName=\"urn:ogc:def:crs:EPSG::27700\"><gml:pointMember><gml:Point srsName=\"urn:ogc:def:crs:EPSG::27700\"><gml:pos>10 40</gml:pos></gml:Point></gml:pointMember><gml:pointMember><gml:Point srsName=\"urn:ogc:def:crs:EPSG::27700\"><gml:pos>40 30</gml:pos></gml:Point></gml:pointMember><gml:pointMember><gml:Point srsName=\"urn:ogc:def:crs:EPSG::27700\"><gml:pos>20 20</gml:pos></gml:Point></gml:pointMember><gml:pointMember><gml:Point srsName=\"urn:ogc:def:crs:EPSG::27700\"><gml:pos>30 10</gml:pos></gml:Point></gml:pointMember></gml:MultiPoint>";

        //System.out.println("Expected: " + expResult);
        //System.out.println("Result: " + result);
        assertEquals(expResult.trim(), result.trim());
    }

    @Test
    public void testWriteMultiCurve() {
        System.out.println("writeMultiCurve");
        LineString[] lineStrings = new LineString[2];
        lineStrings[0] = GEOMETRY_FACTORY.createLineString(new CustomCoordinateSequence(CoordinateSequenceDimensions.XY, "10 10, 20 20, 10 40"));
        lineStrings[1] = GEOMETRY_FACTORY.createLineString(new CustomCoordinateSequence(CoordinateSequenceDimensions.XY, "40 40, 30 30, 40 20, 30 10"));
        Geometry geometry = GEOMETRY_FACTORY.createMultiLineString(lineStrings);
        GeometryWrapper geometryWrapper = new GeometryWrapper(geometry, GML_SRS_NAMESPACE, GMLDatatype.URI, new DimensionInfo(2, 2, 0));

        String result = GMLWriter.write(geometryWrapper);
        String expResult = "<gml:MultiCurve xmlns:gml=\"http://www.opengis.net/ont/gml\" srsName=\"urn:ogc:def:crs:EPSG::27700\"><gml:curveMember><gml:LineString srsName=\"urn:ogc:def:crs:EPSG::27700\"><gml:posList>10 10 20 20 10 40</gml:posList></gml:LineString></gml:curveMember><gml:curveMember><gml:LineString srsName=\"urn:ogc:def:crs:EPSG::27700\"><gml:posList>40 40 30 30 40 20 30 10</gml:posList></gml:LineString></gml:curveMember></gml:MultiCurve>";

        //System.out.println("Expected: " + expResult);
        //System.out.println("Result: " + result);
        assertEquals(expResult.trim(), result.trim());
    }

    @Test
    public void testWriteMultiSurface() {
        System.out.println("writeMultiSurface");
        Polygon[] polygons = new Polygon[2];
        polygons[0] = GEOMETRY_FACTORY.createPolygon(new CustomCoordinateSequence(CoordinateSequenceDimensions.XY, "40 40, 20 45, 45 30, 40 40"));
        polygons[1] = GEOMETRY_FACTORY.createPolygon(new CustomCoordinateSequence(CoordinateSequenceDimensions.XY, "20 35, 10 30, 10 10, 30 5, 45 20, 20 35"));
        Geometry geometry = GEOMETRY_FACTORY.createMultiPolygon(polygons);
        GeometryWrapper geometryWrapper = new GeometryWrapper(geometry, GML_SRS_NAMESPACE, GMLDatatype.URI, new DimensionInfo(2, 2, 0));

        String result = GMLWriter.write(geometryWrapper);
        String expResult = "<gml:MultiSurface xmlns:gml=\"http://www.opengis.net/ont/gml\" srsName=\"urn:ogc:def:crs:EPSG::27700\"><gml:surfaceMember><gml:Polygon srsName=\"urn:ogc:def:crs:EPSG::27700\"><gml:exterior><gml:posList>40 40 20 45 45 30 40 40</gml:posList></gml:exterior></gml:Polygon></gml:surfaceMember><gml:surfaceMember><gml:Polygon srsName=\"urn:ogc:def:crs:EPSG::27700\"><gml:exterior><gml:posList>20 35 10 30 10 10 30 5 45 20 20 35</gml:posList></gml:exterior></gml:Polygon></gml:surfaceMember></gml:MultiSurface>";

        //System.out.println("Expected: " + expResult);
        //System.out.println("Result: " + result);
        assertEquals(expResult.trim(), result.trim());
    }

    @Test
    public void testWriteMultiSurface2() {
        System.out.println("writeMultiSurface2");
        Polygon[] polygons = new Polygon[2];
        polygons[0] = GEOMETRY_FACTORY.createPolygon(new CustomCoordinateSequence(CoordinateSequenceDimensions.XY, "40 40, 20 45, 45 30, 40 40"));
        LinearRing shell = GEOMETRY_FACTORY.createLinearRing(new CustomCoordinateSequence(CoordinateSequenceDimensions.XY, "20 35, 10 30, 10 10, 30 5, 45 20, 20 35"));
        LinearRing[] holes = new LinearRing[]{GEOMETRY_FACTORY.createLinearRing(new CustomCoordinateSequence(CoordinateSequenceDimensions.XY, "30 20, 20 15, 20 25, 30 20"))};
        polygons[1] = GEOMETRY_FACTORY.createPolygon(shell, holes);
        Geometry geometry = GEOMETRY_FACTORY.createMultiPolygon(polygons);
        GeometryWrapper geometryWrapper = new GeometryWrapper(geometry, GML_SRS_NAMESPACE, GMLDatatype.URI, new DimensionInfo(2, 2, 0));

        String result = GMLWriter.write(geometryWrapper);
        String expResult = "<gml:MultiSurface xmlns:gml=\"http://www.opengis.net/ont/gml\" srsName=\"urn:ogc:def:crs:EPSG::27700\"><gml:surfaceMember><gml:Polygon srsName=\"urn:ogc:def:crs:EPSG::27700\"><gml:exterior><gml:posList>40 40 20 45 45 30 40 40</gml:posList></gml:exterior></gml:Polygon></gml:surfaceMember><gml:surfaceMember><gml:Polygon srsName=\"urn:ogc:def:crs:EPSG::27700\"><gml:exterior><gml:posList>20 35 10 30 10 10 30 5 45 20 20 35</gml:posList></gml:exterior><gml:interior><gml:posList>30 20 20 15 20 25 30 20</gml:posList></gml:interior></gml:Polygon></gml:surfaceMember></gml:MultiSurface>";

        //System.out.println("Expected: " + expResult);
        //System.out.println("Result: " + result);
        assertEquals(expResult.trim(), result.trim());
    }

    @Test
    public void testWriteMultiGeometry() {
        System.out.println("writeMultiGeometry");
        Geometry[] geometries = new Geometry[2];
        geometries[0] = GEOMETRY_FACTORY.createPoint(new CustomCoordinateSequence(CoordinateSequenceDimensions.XY, "4 6"));
        geometries[1] = GEOMETRY_FACTORY.createLineString(new CustomCoordinateSequence(CoordinateSequenceDimensions.XY, "4 6,7 10"));
        Geometry geometry = GEOMETRY_FACTORY.createGeometryCollection(geometries);
        GeometryWrapper geometryWrapper = new GeometryWrapper(geometry, GML_SRS_NAMESPACE, GMLDatatype.URI, new DimensionInfo(2, 2, 0));

        String result = GMLWriter.write(geometryWrapper);
        String expResult = "<gml:MultiGeometry xmlns:gml=\"http://www.opengis.net/ont/gml\" srsName=\"urn:ogc:def:crs:EPSG::27700\"><gml:geometryMember><gml:Point srsName=\"urn:ogc:def:crs:EPSG::27700\"><gml:pos>4 6</gml:pos></gml:Point></gml:geometryMember><gml:geometryMember><gml:LineString srsName=\"urn:ogc:def:crs:EPSG::27700\"><gml:posList>4 6 7 10</gml:posList></gml:LineString></gml:geometryMember></gml:MultiGeometry>";

        //System.out.println("Expected: " + expResult);
        //System.out.println("Result: " + result);
        assertEquals(expResult.trim(), result.trim());
    }

    /**
     * Test of writePointEmpty method, of class GMLWriter.
     */
    @Test
    public void testWritePointEmpty() {
        System.out.println("writePointEmpty");

        Geometry geometry = GEOMETRY_FACTORY.createPoint();
        GeometryWrapper geometryWrapper = new GeometryWrapper(geometry, SRS_URI.WGS84_CRS, GMLDatatype.URI, new DimensionInfo(4, 3, 0));
        String result = GMLWriter.write(geometryWrapper);
        String expResult = "<gml:Point xmlns:gml=\"http://www.opengis.net/ont/gml\" srsName=\"http://www.opengis.net/def/crs/EPSG/0/4326\" />";

        //System.out.println("Expected: " + expResult);
        //System.out.println("Result: " + result);
        assertEquals(expResult, result);
    }

    /**
     * Test of writeLineStringEmpty method, of class GMLWriter.
     */
    @Test
    public void testWriteLineStringEmpty() {
        System.out.println("writeLineEmpty");
        Geometry geometry = GEOMETRY_FACTORY.createLineString();
        GeometryWrapper geometryWrapper = new GeometryWrapper(geometry, SRS_URI.WGS84_CRS, GMLDatatype.URI, new DimensionInfo(4, 3, 0));
        String result = GMLWriter.write(geometryWrapper);
        String expResult = "<gml:LineString xmlns:gml=\"http://www.opengis.net/ont/gml\" srsName=\"http://www.opengis.net/def/crs/EPSG/0/4326\" />";

        //System.out.println("Expected: " + expResult);
        //System.out.println("Result: " + result);
        assertEquals(expResult, result);
    }

    /**
     * Test of writePolygonEmpty method, of class GMLWriter.
     */
    @Test
    public void testWritePolygonEmpty() {
        System.out.println("writePolygonEmpty");
        Geometry geometry = GEOMETRY_FACTORY.createPolygon();
        GeometryWrapper geometryWrapper = new GeometryWrapper(geometry, SRS_URI.WGS84_CRS, GMLDatatype.URI, new DimensionInfo(4, 3, 0));
        String result = GMLWriter.write(geometryWrapper);
        String expResult = "<gml:Polygon xmlns:gml=\"http://www.opengis.net/ont/gml\" srsName=\"http://www.opengis.net/def/crs/EPSG/0/4326\" />";

        //System.out.println("Expected: " + expResult);
        //System.out.println("Result: " + result);
        assertEquals(expResult, result);
    }

    /**
     * Test of writeMultiPointEmpty method, of class GMLWriter.
     */
    @Test
    public void testWriteMultiPointEmpty() {
        System.out.println("writeMultiPointEmpty");
        Geometry geometry = GEOMETRY_FACTORY.createMultiPoint();
        GeometryWrapper geometryWrapper = new GeometryWrapper(geometry, SRS_URI.WGS84_CRS, GMLDatatype.URI, new DimensionInfo(4, 3, 0));
        String result = GMLWriter.write(geometryWrapper);
        String expResult = "<gml:MultiPoint xmlns:gml=\"http://www.opengis.net/ont/gml\" srsName=\"http://www.opengis.net/def/crs/EPSG/0/4326\" />";

        //System.out.println("Expected: " + expResult);
        //System.out.println("Result: " + result);
        assertEquals(expResult, result);
    }

    /**
     * Test of writeMultiCurve method, of class GMLWriter.
     */
    @Test
    public void testWriteMultiCurveEmpty() {
        System.out.println("writeMultiCurveEmpty");

        Geometry geometry = GEOMETRY_FACTORY.createMultiLineString();
        GeometryWrapper geometryWrapper = new GeometryWrapper(geometry, SRS_URI.WGS84_CRS, GMLDatatype.URI, new DimensionInfo(4, 3, 0));
        String result = GMLWriter.write(geometryWrapper);
        String expResult = "<gml:MultiCurve xmlns:gml=\"http://www.opengis.net/ont/gml\" srsName=\"http://www.opengis.net/def/crs/EPSG/0/4326\" />";

        //System.out.println("Expected: " + expResult);
        //System.out.println("Result: " + result);
        assertEquals(expResult, result);
    }

    /**
     * Test of writeMultiSurfaceEmpty method, of class GMLWriter.
     */
    @Test
    public void testWriteMultiSurfaceEmpty() {
        System.out.println("writeMultiSurfaceEmpty");

        Geometry geometry = GEOMETRY_FACTORY.createMultiPolygon();
        GeometryWrapper geometryWrapper = new GeometryWrapper(geometry, SRS_URI.WGS84_CRS, GMLDatatype.URI, new DimensionInfo(4, 3, 0));
        String result = GMLWriter.write(geometryWrapper);
        String expResult = "<gml:MultiSurface xmlns:gml=\"http://www.opengis.net/ont/gml\" srsName=\"http://www.opengis.net/def/crs/EPSG/0/4326\" />";

        //System.out.println("Expected: " + expResult);
        //System.out.println("Result: " + result);
        assertEquals(expResult, result);
    }

    /**
     * Test of writeMultiGeometryEmpty method, of class GMLWriter.
     */
    @Test
    public void testWriteMultiGeometryEmpty() {
        System.out.println("writeMultiGeometryEmpty");

        Geometry geometry = GEOMETRY_FACTORY.createGeometryCollection();
        GeometryWrapper geometryWrapper = new GeometryWrapper(geometry, SRS_URI.WGS84_CRS, GMLDatatype.URI, new DimensionInfo(4, 3, 0));
        String result = GMLWriter.write(geometryWrapper);
        String expResult = "<gml:MultiGeometry xmlns:gml=\"http://www.opengis.net/ont/gml\" srsName=\"http://www.opengis.net/def/crs/EPSG/0/4326\" />";

        //System.out.println("Expected: " + expResult);
        //System.out.println("Result: " + result);
        assertEquals(expResult, result);
    }

}
