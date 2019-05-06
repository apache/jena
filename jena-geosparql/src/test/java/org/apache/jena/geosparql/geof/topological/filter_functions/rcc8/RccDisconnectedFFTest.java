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
package org.apache.jena.geosparql.geof.topological.filter_functions.rcc8;

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
 * Disconnected returns t (TRUE) if the intersection of the two geometries is an
 * empty set.
 */
public class RccDisconnectedFFTest {

    public RccDisconnectedFFTest() {
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

    //Only Polygon-Polygon
    @Test
    public void testRelate_polygon_polygon() throws FactoryException, MismatchedDimensionException, TransformException {
        System.out.println("relate_polygon_polygon");

        GeometryWrapper subjectGeometryWrapper = GeometryWrapper.extract(ResourceFactory.createTypedLiteral("<http://www.opengis.net/def/crs/EPSG/0/27700> POLYGON((30 40, 30 70, 90 70, 90 40, 30 40))", WKTDatatype.INSTANCE));
        GeometryWrapper objectGeometryWrapper = GeometryWrapper.extract(ResourceFactory.createTypedLiteral("<http://www.opengis.net/def/crs/EPSG/0/27700> POLYGON((140 15, 140 45, 200 45, 200 15, 140 15))", WKTDatatype.INSTANCE));

        RccDisconnectedFF instance = new RccDisconnectedFF();

        Boolean expResult = true;
        Boolean result = instance.relate(subjectGeometryWrapper, objectGeometryWrapper);
        assertEquals(expResult, result);
    }

    @Test
    public void testRelate_polygon_polygon_false() throws FactoryException, MismatchedDimensionException, TransformException {
        System.out.println("relate_polygon_polygon_false");

        GeometryWrapper subjectGeometryWrapper = GeometryWrapper.extract(ResourceFactory.createTypedLiteral("<http://www.opengis.net/def/crs/EPSG/0/27700> POLYGON((30 40, 30 70, 90 70, 90 40, 30 40))", WKTDatatype.INSTANCE));
        GeometryWrapper objectGeometryWrapper = GeometryWrapper.extract(ResourceFactory.createTypedLiteral("<http://www.opengis.net/def/crs/EPSG/0/27700> POLYGON((80 15, 80 45, 140 45, 140 15, 80 15))", WKTDatatype.INSTANCE));

        RccDisconnectedFF instance = new RccDisconnectedFF();

        Boolean expResult = false;
        Boolean result = instance.relate(subjectGeometryWrapper, objectGeometryWrapper);
        assertEquals(expResult, result);
    }

    /**
     * Test of isDisjoint method, of class RccDisconnectedFF.
     */
    @Test
    public void testIsDisjoint() {
        System.out.println("isDisjoint");
        RccDisconnectedFF instance = new RccDisconnectedFF();
        boolean expResult = true;
        boolean result = instance.isDisjoint();
        assertEquals(expResult, result);
    }

    /**
     * Test of isDisconnected method, of class RccDisconnectedFF.
     */
    @Test
    public void testIsDisconnected() {
        System.out.println("isDisconnected");
        RccDisconnectedFF instance = new RccDisconnectedFF();
        boolean expResult = true;
        boolean result = instance.isDisconnected();
        assertEquals(expResult, result);
    }

    /**
     * Test of permittedTopology method, of class RccDisconnectedFF.
     */
    @Test
    public void testPermittedTopology_point_point() {
        System.out.println("permittedTopology_point_point");
        DimensionInfo sourceDimensionInfo = DimensionInfo.XY_POINT;
        DimensionInfo targetDimensionInfo = DimensionInfo.XY_POINT;
        RccDisconnectedFF instance = new RccDisconnectedFF();
        boolean expResult = false;
        boolean result = instance.permittedTopology(sourceDimensionInfo, targetDimensionInfo);
        assertEquals(expResult, result);
    }

    /**
     * Test of permittedTopology method, of class RccDisconnectedFF.
     */
    @Test
    public void testPermittedTopology_point_linestring() {
        System.out.println("permittedTopology_point_linestring");
        DimensionInfo sourceDimensionInfo = DimensionInfo.XY_POINT;
        DimensionInfo targetDimensionInfo = DimensionInfo.XY_LINESTRING;
        RccDisconnectedFF instance = new RccDisconnectedFF();
        boolean expResult = false;
        boolean result = instance.permittedTopology(sourceDimensionInfo, targetDimensionInfo);
        assertEquals(expResult, result);
    }

    /**
     * Test of permittedTopology method, of class RccDisconnectedFF.
     */
    @Test
    public void testPermittedTopology_point_polygon() {
        System.out.println("permittedTopology_point_polygon");
        DimensionInfo sourceDimensionInfo = DimensionInfo.XY_POINT;
        DimensionInfo targetDimensionInfo = DimensionInfo.XY_POLYGON;
        RccDisconnectedFF instance = new RccDisconnectedFF();
        boolean expResult = false;
        boolean result = instance.permittedTopology(sourceDimensionInfo, targetDimensionInfo);
        assertEquals(expResult, result);
    }

    /**
     * Test of permittedTopology method, of class RccDisconnectedFF.
     */
    @Test
    public void testPermittedTopology_linestring_linestring() {
        System.out.println("permittedTopology_linestring_linestring");
        DimensionInfo sourceDimensionInfo = DimensionInfo.XY_LINESTRING;
        DimensionInfo targetDimensionInfo = DimensionInfo.XY_LINESTRING;
        RccDisconnectedFF instance = new RccDisconnectedFF();
        boolean expResult = false;
        boolean result = instance.permittedTopology(sourceDimensionInfo, targetDimensionInfo);
        assertEquals(expResult, result);
    }

    /**
     * Test of permittedTopology method, of class RccDisconnectedFF.
     */
    @Test
    public void testPermittedTopology_linestring_point() {
        System.out.println("permittedTopology_linestring_point");
        DimensionInfo sourceDimensionInfo = DimensionInfo.XY_LINESTRING;
        DimensionInfo targetDimensionInfo = DimensionInfo.XY_POINT;
        RccDisconnectedFF instance = new RccDisconnectedFF();
        boolean expResult = false;
        boolean result = instance.permittedTopology(sourceDimensionInfo, targetDimensionInfo);
        assertEquals(expResult, result);
    }

    /**
     * Test of permittedTopology method, of class RccDisconnectedFF.
     */
    @Test
    public void testPermittedTopology_linestring_polygon() {
        System.out.println("permittedTopology_linestring_polygon");
        DimensionInfo sourceDimensionInfo = DimensionInfo.XY_LINESTRING;
        DimensionInfo targetDimensionInfo = DimensionInfo.XY_POLYGON;
        RccDisconnectedFF instance = new RccDisconnectedFF();
        boolean expResult = false;
        boolean result = instance.permittedTopology(sourceDimensionInfo, targetDimensionInfo);
        assertEquals(expResult, result);
    }

    /**
     * Test of permittedTopology method, of class RccDisconnectedFF.
     */
    @Test
    public void testPermittedTopology_polygon_polygon() {
        System.out.println("permittedTopology_polygon_polygon");
        DimensionInfo sourceDimensionInfo = DimensionInfo.XY_POLYGON;
        DimensionInfo targetDimensionInfo = DimensionInfo.XY_POLYGON;
        RccDisconnectedFF instance = new RccDisconnectedFF();
        boolean expResult = true;
        boolean result = instance.permittedTopology(sourceDimensionInfo, targetDimensionInfo);
        assertEquals(expResult, result);
    }

    /**
     * Test of permittedTopology method, of class RccDisconnectedFF.
     */
    @Test
    public void testPermittedTopology_polygon_linestring() {
        System.out.println("permittedTopology_polygon_linestring");
        DimensionInfo sourceDimensionInfo = DimensionInfo.XY_POLYGON;
        DimensionInfo targetDimensionInfo = DimensionInfo.XY_LINESTRING;
        RccDisconnectedFF instance = new RccDisconnectedFF();
        boolean expResult = false;
        boolean result = instance.permittedTopology(sourceDimensionInfo, targetDimensionInfo);
        assertEquals(expResult, result);
    }

    /**
     * Test of permittedTopology method, of class RccDisconnectedFF.
     */
    @Test
    public void testPermittedTopology_polygon_point() {
        System.out.println("permittedTopology_polygon_point");
        DimensionInfo sourceDimensionInfo = DimensionInfo.XY_POLYGON;
        DimensionInfo targetDimensionInfo = DimensionInfo.XY_POINT;
        RccDisconnectedFF instance = new RccDisconnectedFF();
        boolean expResult = false;
        boolean result = instance.permittedTopology(sourceDimensionInfo, targetDimensionInfo);
        assertEquals(expResult, result);
    }

}
