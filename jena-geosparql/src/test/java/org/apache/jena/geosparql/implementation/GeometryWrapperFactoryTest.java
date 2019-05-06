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

import java.util.Arrays;
import java.util.List;
import org.apache.jena.geosparql.implementation.datatype.WKTDatatype;
import org.apache.jena.geosparql.implementation.vocabulary.SRS_URI;
import org.junit.After;
import org.junit.AfterClass;
import static org.junit.Assert.assertEquals;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.locationtech.jts.geom.Coordinate;

/**
 *
 *
 */
public class GeometryWrapperFactoryTest {

    public GeometryWrapperFactoryTest() {
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
     * Test of createPoint method, of class GeometryWrapper.
     */
    @Test
    public void testCreatPoint() {
        System.out.println("createPoint");
        Coordinate coordinate = new Coordinate(1, 2);
        String srsURI = SRS_URI.WGS84_CRS;
        String geometryDatatypeURI = WKTDatatype.URI;

        GeometryWrapper instance = GeometryWrapperFactory.createPoint(coordinate, srsURI, geometryDatatypeURI);

        String expResult = "<http://www.opengis.net/def/crs/EPSG/0/4326> POINT(2 1)";
        String result = instance.asLiteral().getLexicalForm();
        assertEquals(expResult, result);
    }

    /**
     * Test of createPoint method, of class GeometryWrapper.
     */
    @Test
    public void testCreatPoint_xy() {
        System.out.println("createPoint_xy");
        Coordinate coordinate = new Coordinate(1, 2);
        String srsURI = SRS_URI.DEFAULT_WKT_CRS84;
        String geometryDatatypeURI = WKTDatatype.URI;

        GeometryWrapper instance = GeometryWrapperFactory.createPoint(coordinate, srsURI, geometryDatatypeURI);

        String expResult = "POINT(1 2)";
        String result = instance.asLiteral().getLexicalForm();
        assertEquals(expResult, result);
    }

    /**
     * Test of createLineString method, of class GeometryWrapper.
     */
    @Test
    public void testCreatLineString() {
        System.out.println("createLineString");
        List<Coordinate> coordinates = Arrays.asList(new Coordinate(1, 2), new Coordinate(10, 20));
        String srsURI = SRS_URI.WGS84_CRS;
        String geometryDatatypeURI = WKTDatatype.URI;

        GeometryWrapper instance = GeometryWrapperFactory.createLineString(coordinates, srsURI, geometryDatatypeURI);

        String expResult = "<http://www.opengis.net/def/crs/EPSG/0/4326> LINESTRING(2 1, 20 10)";
        String result = instance.asLiteral().getLexicalForm();
        assertEquals(expResult, result);
    }

    /**
     * Test of createLineString method, of class GeometryWrapper.
     */
    @Test
    public void testCreatLineString_xy() {
        System.out.println("createLineString_xy");
        List<Coordinate> coordinates = Arrays.asList(new Coordinate(1, 2), new Coordinate(10, 20));
        String srsURI = SRS_URI.DEFAULT_WKT_CRS84;
        String geometryDatatypeURI = WKTDatatype.URI;

        GeometryWrapper instance = GeometryWrapperFactory.createLineString(coordinates, srsURI, geometryDatatypeURI);

        String expResult = "LINESTRING(1 2, 10 20)";
        String result = instance.asLiteral().getLexicalForm();
        assertEquals(expResult, result);
    }

    /**
     * Test of createPolygon method, of class GeometryWrapper.
     */
    @Test
    public void testCreatPolygon() {
        System.out.println("createPolygon");
        List<Coordinate> coordinates = Arrays.asList(new Coordinate(1, 2), new Coordinate(10, 2), new Coordinate(10, 20), new Coordinate(1, 20), new Coordinate(1, 2));
        String srsURI = SRS_URI.WGS84_CRS;
        String geometryDatatypeURI = WKTDatatype.URI;

        GeometryWrapper instance = GeometryWrapperFactory.createPolygon(coordinates, srsURI, geometryDatatypeURI);

        String expResult = "<http://www.opengis.net/def/crs/EPSG/0/4326> POLYGON((2 1, 2 10, 20 10, 20 1, 2 1))";
        String result = instance.asLiteral().getLexicalForm();
        assertEquals(expResult, result);
    }

    /**
     * Test of createPolygon method, of class GeometryWrapper.
     */
    @Test
    public void testCreatPolygon_xy() {
        System.out.println("createPolygon_xy");
        List<Coordinate> coordinates = Arrays.asList(new Coordinate(1, 2), new Coordinate(10, 2), new Coordinate(10, 20), new Coordinate(1, 20), new Coordinate(1, 2));
        String srsURI = SRS_URI.DEFAULT_WKT_CRS84;
        String geometryDatatypeURI = WKTDatatype.URI;

        GeometryWrapper instance = GeometryWrapperFactory.createPolygon(coordinates, srsURI, geometryDatatypeURI);

        String expResult = "POLYGON((1 2, 10 2, 10 20, 1 20, 1 2))";
        String result = instance.asLiteral().getLexicalForm();
        assertEquals(expResult, result);
    }

}
