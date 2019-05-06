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
package org.apache.jena.geosparql.spatial.property_functions;

import org.apache.jena.geosparql.implementation.GeometryWrapper;
import org.apache.jena.geosparql.implementation.datatype.WKTDatatype;
import org.apache.jena.rdf.model.Literal;
import org.apache.jena.rdf.model.ResourceFactory;
import org.junit.After;
import org.junit.AfterClass;
import static org.junit.Assert.assertEquals;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * Equal returns t (TRUE) if two geometries have at least one point in common,
 * and no point of either geometry lies in the exterior of the other geometry.
 */
public class EqualsPFTest {

    public EqualsPFTest() {
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

    @Test
    public void testFilterFunction_point_point() {
        System.out.println("filterFunction_point_point");

        Literal subjectGeometryLiteral = ResourceFactory.createTypedLiteral("<http://www.opengis.net/def/crs/EPSG/0/27700> POINT(60 60)", WKTDatatype.INSTANCE);
        Literal objectGeometryLiteral = ResourceFactory.createTypedLiteral("<http://www.opengis.net/def/crs/EPSG/0/27700> POINT(60 60)", WKTDatatype.INSTANCE);

        EqualsPF instance = new EqualsPF();

        Boolean expResult = true;
        Boolean result = instance.testFilterFunction(subjectGeometryLiteral.asNode(), objectGeometryLiteral.asNode());
        assertEquals(expResult, result);
    }

    @Test
    public void testFilterFunction_linestring_linestring() {
        System.out.println("filterFunction_linestring_linestring");

        Literal subjectGeometryLiteral = ResourceFactory.createTypedLiteral("<http://www.opengis.net/def/crs/EPSG/0/27700> LINESTRING(40 50, 80 50)", WKTDatatype.INSTANCE);
        Literal objectGeometryLiteral = ResourceFactory.createTypedLiteral("<http://www.opengis.net/def/crs/EPSG/0/27700> LINESTRING(40 50, 60 50, 80 50)", WKTDatatype.INSTANCE);

        EqualsPF instance = new EqualsPF();

        Boolean expResult = true;
        Boolean result = instance.testFilterFunction(subjectGeometryLiteral.asNode(), objectGeometryLiteral.asNode());
        assertEquals(expResult, result);
    }

    @Test
    public void testFilterFunction_polygon_polygon() {
        System.out.println("filterFunction_polygon_polygon");

        Literal subjectGeometryLiteral = ResourceFactory.createTypedLiteral("<http://www.opengis.net/def/crs/EPSG/0/27700> POLYGON((140 15, 140 45, 200 45, 200 15, 140 15))", WKTDatatype.INSTANCE);
        Literal objectGeometryLiteral = ResourceFactory.createTypedLiteral("<http://www.opengis.net/def/crs/EPSG/0/27700> POLYGON((140 15, 140 45, 200 45, 200 15, 140 15))", WKTDatatype.INSTANCE);

        EqualsPF instance = new EqualsPF();

        Boolean expResult = true;
        Boolean result = instance.testFilterFunction(subjectGeometryLiteral.asNode(), objectGeometryLiteral.asNode());
        assertEquals(expResult, result);
    }

    @Test
    public void testFilterFunction_point_point_false() {
        System.out.println("filterFunction_point_point_false");

        Literal subjectGeometryLiteral = ResourceFactory.createTypedLiteral("<http://www.opengis.net/def/crs/EPSG/0/27700> POINT(60 60)", WKTDatatype.INSTANCE);
        Literal objectGeometryLiteral = ResourceFactory.createTypedLiteral("<http://www.opengis.net/def/crs/EPSG/0/27700> POINT(65 65)", WKTDatatype.INSTANCE);

        EqualsPF instance = new EqualsPF();

        Boolean expResult = false;
        Boolean result = instance.testFilterFunction(subjectGeometryLiteral.asNode(), objectGeometryLiteral.asNode());
        assertEquals(expResult, result);
    }

    @Test
    public void testFilterFunction_linestring_linestring_false() {
        System.out.println("filterFunction_linestring_linestring");

        Literal subjectGeometryLiteral = ResourceFactory.createTypedLiteral("<http://www.opengis.net/def/crs/EPSG/0/27700> LINESTRING(50 50, 60 50, 80 50)", WKTDatatype.INSTANCE);
        Literal objectGeometryLiteral = ResourceFactory.createTypedLiteral("<http://www.opengis.net/def/crs/EPSG/0/27700> LINESTRING(40 50, 60 50, 80 50)", WKTDatatype.INSTANCE);

        EqualsPF instance = new EqualsPF();

        Boolean expResult = false;
        Boolean result = instance.testFilterFunction(subjectGeometryLiteral.asNode(), objectGeometryLiteral.asNode());
        assertEquals(expResult, result);
    }

    @Test
    public void testFilterFunction_polygon_point_false() {
        System.out.println("filterFunction_polygon_point_false");

        Literal subjectGeometryLiteral = ResourceFactory.createTypedLiteral("<http://www.opengis.net/def/crs/EPSG/0/27700> POLYGON((30 40, 30 70, 90 70, 90 40, 30 40))", WKTDatatype.INSTANCE);
        Literal objectGeometryLiteral = ResourceFactory.createTypedLiteral("<http://www.opengis.net/def/crs/EPSG/0/27700> POINT(30 20)", WKTDatatype.INSTANCE);

        EqualsPF instance = new EqualsPF();

        Boolean expResult = false;
        Boolean result = instance.testFilterFunction(subjectGeometryLiteral.asNode(), objectGeometryLiteral.asNode());
        assertEquals(expResult, result);
    }

    @Test
    public void testFilterFunction_polygon_linestring_false() {
        System.out.println("filterFunction_polygon_linestring_false");

        Literal subjectGeometryLiteral = ResourceFactory.createTypedLiteral("<http://www.opengis.net/def/crs/EPSG/0/27700> POLYGON((30 40, 30 70, 90 70, 90 40, 30 40))", WKTDatatype.INSTANCE);
        Literal objectGeometryLiteral = ResourceFactory.createTypedLiteral("<http://www.opengis.net/def/crs/EPSG/0/27700> LINESTRING(75 60, 145 60)", WKTDatatype.INSTANCE);

        EqualsPF instance = new EqualsPF();

        Boolean expResult = false;
        Boolean result = instance.testFilterFunction(subjectGeometryLiteral.asNode(), objectGeometryLiteral.asNode());
        assertEquals(expResult, result);
    }

    @Test
    public void testFilterFunction_polygon_polygon_false() {
        System.out.println("filterFunction_polygon_polygon_false");

        Literal subjectGeometryLiteral = ResourceFactory.createTypedLiteral("<http://www.opengis.net/def/crs/EPSG/0/27700> POLYGON((30 40, 30 70, 90 70, 90 40, 30 40))", WKTDatatype.INSTANCE);
        Literal objectGeometryLiteral = ResourceFactory.createTypedLiteral("<http://www.opengis.net/def/crs/EPSG/0/27700> POLYGON((140 15, 140 45, 200 45, 200 15, 140 15))", WKTDatatype.INSTANCE);

        EqualsPF instance = new EqualsPF();

        Boolean expResult = false;
        Boolean result = instance.testFilterFunction(subjectGeometryLiteral.asNode(), objectGeometryLiteral.asNode());
        assertEquals(expResult, result);
    }

    /**
     * Empty geometries are not spatially equal.
     */
    @Test
    public void testFilterFunction_emptyWKT_emptyGML() {
        System.out.println("filterFunction_emptyWKT_emptyGML");

        Literal emptyWKT = GeometryWrapper.getEmptyWKT().asLiteral();
        Literal emptyGML = GeometryWrapper.getEmptyGML().asLiteral();

        EqualsPF instance = new EqualsPF();

        Boolean expResult = false;
        Boolean result = instance.testFilterFunction(emptyWKT.asNode(), emptyGML.asNode());
        assertEquals(expResult, result);
    }

    /**
     * Empty geometries are not spatially equal.
     */
    @Test
    public void testFilterFunction_emptyWKT_emptyWKT() {
        System.out.println("filterFunction_empty_empty");

        Literal emptyWKT = GeometryWrapper.getEmptyWKT().asLiteral();
        Literal emptyWKT2 = GeometryWrapper.getEmptyWKT().asLiteral();

        EqualsPF instance = new EqualsPF();

        Boolean expResult = false;
        Boolean result = instance.testFilterFunction(emptyWKT.asNode(), emptyWKT2.asNode());
        assertEquals(expResult, result);
    }

}
