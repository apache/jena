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
package org.apache.jena.geosparql.geo.topological.property_functions.simple_features;

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
 * Intersects returns t (TRUE) if the intersection does not result in an empty
 * set, Intersects returns the exact opposite result of disjoint.
 */
public class SfIntersectsPFTest {

    public SfIntersectsPFTest() {
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
    public void testFilterFunction_polygon_point() {


        Literal subjectGeometryLiteral = ResourceFactory.createTypedLiteral("<http://www.opengis.net/def/crs/EPSG/0/27700> POLYGON((30 40, 30 70, 90 70, 90 40, 30 40))", WKTDatatype.INSTANCE);
        Literal objectGeometryLiteral = ResourceFactory.createTypedLiteral("<http://www.opengis.net/def/crs/EPSG/0/27700> POINT(60 60)", WKTDatatype.INSTANCE);

        SfIntersectsPF instance = new SfIntersectsPF();

        Boolean expResult = true;
        Boolean result = instance.testFilterFunction(subjectGeometryLiteral.asNode(), objectGeometryLiteral.asNode());
        assertEquals(expResult, result);
    }

    @Test
    public void testFilterFunction_polygon_linestring() {


        Literal subjectGeometryLiteral = ResourceFactory.createTypedLiteral("<http://www.opengis.net/def/crs/EPSG/0/27700> POLYGON((30 40, 30 70, 90 70, 90 40, 30 40))", WKTDatatype.INSTANCE);
        Literal objectGeometryLiteral = ResourceFactory.createTypedLiteral("<http://www.opengis.net/def/crs/EPSG/0/27700> LINESTRING(40 50, 80 50)", WKTDatatype.INSTANCE);

        SfIntersectsPF instance = new SfIntersectsPF();

        Boolean expResult = true;
        Boolean result = instance.testFilterFunction(subjectGeometryLiteral.asNode(), objectGeometryLiteral.asNode());
        assertEquals(expResult, result);
    }

    @Test
    public void testFilterFunction_polygon_polygon() {


        Literal subjectGeometryLiteral = ResourceFactory.createTypedLiteral("<http://www.opengis.net/def/crs/EPSG/0/27700> POLYGON((140 15, 140 45, 200 45, 200 15, 140 15))", WKTDatatype.INSTANCE);
        Literal objectGeometryLiteral = ResourceFactory.createTypedLiteral("<http://www.opengis.net/def/crs/EPSG/0/27700> POLYGON((145 30, 145 40, 160 40, 160 30, 145 30))", WKTDatatype.INSTANCE);

        SfIntersectsPF instance = new SfIntersectsPF();

        Boolean expResult = true;
        Boolean result = instance.testFilterFunction(subjectGeometryLiteral.asNode(), objectGeometryLiteral.asNode());
        assertEquals(expResult, result);
    }

    @Test
    public void testFilterFunction_polygon_point_false() {


        Literal subjectGeometryLiteral = ResourceFactory.createTypedLiteral("<http://www.opengis.net/def/crs/EPSG/0/27700> POLYGON((30 40, 30 70, 90 70, 90 40, 30 40))", WKTDatatype.INSTANCE);
        Literal objectGeometryLiteral = ResourceFactory.createTypedLiteral("<http://www.opengis.net/def/crs/EPSG/0/27700> POINT(30 20)", WKTDatatype.INSTANCE);

        SfIntersectsPF instance = new SfIntersectsPF();

        Boolean expResult = false;
        Boolean result = instance.testFilterFunction(subjectGeometryLiteral.asNode(), objectGeometryLiteral.asNode());
        assertEquals(expResult, result);
    }

    @Test
    public void testFilterFunction_polygon_linestring_false() {


        Literal subjectGeometryLiteral = ResourceFactory.createTypedLiteral("<http://www.opengis.net/def/crs/EPSG/0/27700> POLYGON((30 40, 30 70, 90 70, 90 40, 30 40))", WKTDatatype.INSTANCE);
        Literal objectGeometryLiteral = ResourceFactory.createTypedLiteral("<http://www.opengis.net/def/crs/EPSG/0/27700> LINESTRING(75 30, 145 30)", WKTDatatype.INSTANCE);

        SfIntersectsPF instance = new SfIntersectsPF();

        Boolean expResult = false;
        Boolean result = instance.testFilterFunction(subjectGeometryLiteral.asNode(), objectGeometryLiteral.asNode());
        assertEquals(expResult, result);
    }

    @Test
    public void testFilterFunction_polygon_polygon_false() {


        Literal subjectGeometryLiteral = ResourceFactory.createTypedLiteral("<http://www.opengis.net/def/crs/EPSG/0/27700> POLYGON((30 40, 30 70, 90 70, 90 40, 30 40))", WKTDatatype.INSTANCE);
        Literal objectGeometryLiteral = ResourceFactory.createTypedLiteral("<http://www.opengis.net/def/crs/EPSG/0/27700> POLYGON((140 15, 140 45, 200 45, 200 15, 140 15))", WKTDatatype.INSTANCE);

        SfIntersectsPF instance = new SfIntersectsPF();

        Boolean expResult = false;
        Boolean result = instance.testFilterFunction(subjectGeometryLiteral.asNode(), objectGeometryLiteral.asNode());
        assertEquals(expResult, result);
    }

}
