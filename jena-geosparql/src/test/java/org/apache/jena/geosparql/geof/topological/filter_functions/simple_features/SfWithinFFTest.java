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
package org.apache.jena.geosparql.geof.topological.filter_functions.simple_features;

import org.apache.jena.geosparql.implementation.DimensionInfo;
import org.apache.jena.geosparql.implementation.GeometryWrapper;
import org.apache.jena.geosparql.implementation.datatype.WKTDatatype;
import org.apache.jena.rdf.model.ResourceFactory;
import org.junit.After;
import org.junit.AfterClass;
import static org.junit.Assert.assertEquals;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.opengis.geometry.MismatchedDimensionException;
import org.opengis.referencing.operation.TransformException;
import org.opengis.util.FactoryException;

/**
 * Within returns t (TRUE) if the first geometry is completely within the second
 * geometry, Within tests for the exact opposite result of contains.
 */
public class SfWithinFFTest {

    public SfWithinFFTest() {
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
    public void testRelate_polygon_point() throws FactoryException, MismatchedDimensionException, TransformException {


        GeometryWrapper subjectGeometryWrapper = GeometryWrapper.extract(ResourceFactory.createTypedLiteral("<http://www.opengis.net/def/crs/EPSG/0/27700> POINT(60 60)", WKTDatatype.INSTANCE));
        GeometryWrapper objectGeometryWrapper = GeometryWrapper.extract(ResourceFactory.createTypedLiteral("<http://www.opengis.net/def/crs/EPSG/0/27700> POLYGON((30 40, 30 70, 90 70, 90 40, 30 40))", WKTDatatype.INSTANCE));

        SfWithinFF instance = new SfWithinFF();

        Boolean expResult = true;
        Boolean result = instance.relate(subjectGeometryWrapper, objectGeometryWrapper);
        assertEquals(expResult, result);
    }

    @Test
    public void testRelate_polygon_linestring() throws FactoryException, MismatchedDimensionException, TransformException {


        GeometryWrapper subjectGeometryWrapper = GeometryWrapper.extract(ResourceFactory.createTypedLiteral("<http://www.opengis.net/def/crs/EPSG/0/27700> LINESTRING(40 50, 80 50)", WKTDatatype.INSTANCE));
        GeometryWrapper objectGeometryWrapper = GeometryWrapper.extract(ResourceFactory.createTypedLiteral("<http://www.opengis.net/def/crs/EPSG/0/27700> POLYGON((30 40, 30 70, 90 70, 90 40, 30 40))", WKTDatatype.INSTANCE));

        SfWithinFF instance = new SfWithinFF();

        Boolean expResult = true;
        Boolean result = instance.relate(subjectGeometryWrapper, objectGeometryWrapper);
        assertEquals(expResult, result);
    }

    @Test
    public void testRelate_polygon_polygon() throws FactoryException, MismatchedDimensionException, TransformException {


        GeometryWrapper subjectGeometryWrapper = GeometryWrapper.extract(ResourceFactory.createTypedLiteral("<http://www.opengis.net/def/crs/EPSG/0/27700> POLYGON((145 30, 145 40, 160 40, 160 30, 145 30))", WKTDatatype.INSTANCE));
        GeometryWrapper objectGeometryWrapper = GeometryWrapper.extract(ResourceFactory.createTypedLiteral("<http://www.opengis.net/def/crs/EPSG/0/27700> POLYGON((140 15, 140 45, 200 45, 200 15, 140 15))", WKTDatatype.INSTANCE));

        SfWithinFF instance = new SfWithinFF();

        Boolean expResult = true;
        Boolean result = instance.relate(subjectGeometryWrapper, objectGeometryWrapper);
        assertEquals(expResult, result);
    }

    @Test
    public void testRelate_polygon_point_false() throws FactoryException, MismatchedDimensionException, TransformException {


        GeometryWrapper subjectGeometryWrapper = GeometryWrapper.extract(ResourceFactory.createTypedLiteral("<http://www.opengis.net/def/crs/EPSG/0/27700> POLYGON((30 40, 30 70, 90 70, 90 40, 30 40))", WKTDatatype.INSTANCE));
        GeometryWrapper objectGeometryWrapper = GeometryWrapper.extract(ResourceFactory.createTypedLiteral("<http://www.opengis.net/def/crs/EPSG/0/27700> POINT(30 20)", WKTDatatype.INSTANCE));

        SfWithinFF instance = new SfWithinFF();

        Boolean expResult = false;
        Boolean result = instance.relate(subjectGeometryWrapper, objectGeometryWrapper);
        assertEquals(expResult, result);
    }

    @Test
    public void testRelate_polygon_linestring_false() throws FactoryException, MismatchedDimensionException, TransformException {


        GeometryWrapper subjectGeometryWrapper = GeometryWrapper.extract(ResourceFactory.createTypedLiteral("<http://www.opengis.net/def/crs/EPSG/0/27700> POLYGON((30 40, 30 70, 90 70, 90 40, 30 40))", WKTDatatype.INSTANCE));
        GeometryWrapper objectGeometryWrapper = GeometryWrapper.extract(ResourceFactory.createTypedLiteral("<http://www.opengis.net/def/crs/EPSG/0/27700> LINESTRING(75 60, 145 60)", WKTDatatype.INSTANCE));

        SfWithinFF instance = new SfWithinFF();

        Boolean expResult = false;
        Boolean result = instance.relate(subjectGeometryWrapper, objectGeometryWrapper);
        assertEquals(expResult, result);
    }

    @Test
    public void testRelate_polygon_polygon_false() throws FactoryException, MismatchedDimensionException, TransformException {


        GeometryWrapper subjectGeometryWrapper = GeometryWrapper.extract(ResourceFactory.createTypedLiteral("<http://www.opengis.net/def/crs/EPSG/0/27700> POLYGON((30 40, 30 70, 90 70, 90 40, 30 40))", WKTDatatype.INSTANCE));
        GeometryWrapper objectGeometryWrapper = GeometryWrapper.extract(ResourceFactory.createTypedLiteral("<http://www.opengis.net/def/crs/EPSG/0/27700> POLYGON((140 15, 140 45, 200 45, 200 15, 140 15))", WKTDatatype.INSTANCE));

        SfWithinFF instance = new SfWithinFF();

        Boolean expResult = false;
        Boolean result = instance.relate(subjectGeometryWrapper, objectGeometryWrapper);
        assertEquals(expResult, result);
    }

    /**
     * Test of isDisjoint method, of class SfWithinFF.
     */
    @Test
    public void testIsDisjoint() {

        SfWithinFF instance = new SfWithinFF();
        boolean expResult = false;
        boolean result = instance.isDisjoint();
        assertEquals(expResult, result);
    }

    /**
     * Test of isDisconnected method, of class SfWithinFF.
     */
    @Test
    public void testIsDisconnected() {

        SfWithinFF instance = new SfWithinFF();
        boolean expResult = false;
        boolean result = instance.isDisconnected();
        assertEquals(expResult, result);
    }

    /**
     * Test of permittedTopology method, of class SfWithinFF.
     */
    @Test
    public void testPermittedTopology_point_point() {

        DimensionInfo sourceDimensionInfo = DimensionInfo.XY_POINT;
        DimensionInfo targetDimensionInfo = DimensionInfo.XY_POINT;
        SfWithinFF instance = new SfWithinFF();
        boolean expResult = true;
        boolean result = instance.permittedTopology(sourceDimensionInfo, targetDimensionInfo);
        assertEquals(expResult, result);
    }

    /**
     * Test of permittedTopology method, of class SfWithinFF.
     */
    @Test
    public void testPermittedTopology_point_linestring() {

        DimensionInfo sourceDimensionInfo = DimensionInfo.XY_POINT;
        DimensionInfo targetDimensionInfo = DimensionInfo.XY_LINESTRING;
        SfWithinFF instance = new SfWithinFF();
        boolean expResult = true;
        boolean result = instance.permittedTopology(sourceDimensionInfo, targetDimensionInfo);
        assertEquals(expResult, result);
    }

    /**
     * Test of permittedTopology method, of class SfWithinFF.
     */
    @Test
    public void testPermittedTopology_point_polygon() {

        DimensionInfo sourceDimensionInfo = DimensionInfo.XY_POINT;
        DimensionInfo targetDimensionInfo = DimensionInfo.XY_POLYGON;
        SfWithinFF instance = new SfWithinFF();
        boolean expResult = true;
        boolean result = instance.permittedTopology(sourceDimensionInfo, targetDimensionInfo);
        assertEquals(expResult, result);
    }

    /**
     * Test of permittedTopology method, of class SfWithinFF.
     */
    @Test
    public void testPermittedTopology_linestring_linestring() {

        DimensionInfo sourceDimensionInfo = DimensionInfo.XY_LINESTRING;
        DimensionInfo targetDimensionInfo = DimensionInfo.XY_LINESTRING;
        SfWithinFF instance = new SfWithinFF();
        boolean expResult = true;
        boolean result = instance.permittedTopology(sourceDimensionInfo, targetDimensionInfo);
        assertEquals(expResult, result);
    }

    /**
     * Test of permittedTopology method, of class SfWithinFF.
     */
    @Test
    public void testPermittedTopology_linestring_point() {

        DimensionInfo sourceDimensionInfo = DimensionInfo.XY_LINESTRING;
        DimensionInfo targetDimensionInfo = DimensionInfo.XY_POINT;
        SfWithinFF instance = new SfWithinFF();
        boolean expResult = true;
        boolean result = instance.permittedTopology(sourceDimensionInfo, targetDimensionInfo);
        assertEquals(expResult, result);
    }

    /**
     * Test of permittedTopology method, of class SfWithinFF.
     */
    @Test
    public void testPermittedTopology_linestring_polygon() {

        DimensionInfo sourceDimensionInfo = DimensionInfo.XY_LINESTRING;
        DimensionInfo targetDimensionInfo = DimensionInfo.XY_POLYGON;
        SfWithinFF instance = new SfWithinFF();
        boolean expResult = true;
        boolean result = instance.permittedTopology(sourceDimensionInfo, targetDimensionInfo);
        assertEquals(expResult, result);
    }

    /**
     * Test of permittedTopology method, of class SfWithinFF.
     */
    @Test
    public void testPermittedTopology_polygon_polygon() {

        DimensionInfo sourceDimensionInfo = DimensionInfo.XY_POLYGON;
        DimensionInfo targetDimensionInfo = DimensionInfo.XY_POLYGON;
        SfWithinFF instance = new SfWithinFF();
        boolean expResult = true;
        boolean result = instance.permittedTopology(sourceDimensionInfo, targetDimensionInfo);
        assertEquals(expResult, result);
    }

    /**
     * Test of permittedTopology method, of class SfWithinFF.
     */
    @Test
    public void testPermittedTopology_polygon_linestring() {

        DimensionInfo sourceDimensionInfo = DimensionInfo.XY_POLYGON;
        DimensionInfo targetDimensionInfo = DimensionInfo.XY_LINESTRING;
        SfWithinFF instance = new SfWithinFF();
        boolean expResult = true;
        boolean result = instance.permittedTopology(sourceDimensionInfo, targetDimensionInfo);
        assertEquals(expResult, result);
    }

    /**
     * Test of permittedTopology method, of class SfWithinFF.
     */
    @Test
    public void testPermittedTopology_polygon_point() {

        DimensionInfo sourceDimensionInfo = DimensionInfo.XY_POLYGON;
        DimensionInfo targetDimensionInfo = DimensionInfo.XY_POINT;
        SfWithinFF instance = new SfWithinFF();
        boolean expResult = true;
        boolean result = instance.permittedTopology(sourceDimensionInfo, targetDimensionInfo);
        assertEquals(expResult, result);
    }
}
