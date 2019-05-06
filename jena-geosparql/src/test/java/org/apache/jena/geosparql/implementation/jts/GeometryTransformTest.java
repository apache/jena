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
package org.apache.jena.geosparql.implementation.jts;

import org.apache.jena.geosparql.implementation.SRSInfo;
import org.apache.jena.geosparql.implementation.parsers.wkt.WKTReader;
import org.apache.jena.geosparql.implementation.registry.MathTransformRegistry;
import org.apache.jena.geosparql.implementation.vocabulary.SRS_URI;
import org.apache.sis.referencing.CRS;
import org.junit.After;
import org.junit.AfterClass;
import static org.junit.Assert.assertEquals;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.locationtech.jts.geom.Geometry;
import org.opengis.geometry.MismatchedDimensionException;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.referencing.operation.MathTransform;
import org.opengis.referencing.operation.TransformException;
import org.opengis.util.FactoryException;

/**
 *
 *
 */
public class GeometryTransformTest {

    public GeometryTransformTest() {
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
     * Test of transform method, of class GeometryTransformation.
     *
     * @throws org.opengis.util.FactoryException
     * @throws org.opengis.referencing.operation.TransformException
     */
    @Test
    public void testTransformPoint() throws FactoryException, MismatchedDimensionException, TransformException {
        System.out.println("transform_Point");
        Geometry sourceGeometry = WKTReader.extract("POINT ZM(5 10 8 3)").getGeometry();

        CoordinateReferenceSystem sourceCRS = CRS.forCode(SRS_URI.WGS84_CRS);
        CoordinateReferenceSystem targetCRS = CRS.forCode(SRSInfo.DEFAULT_WKT_CRS84_CODE);
        MathTransform transform = MathTransformRegistry.getMathTransform(sourceCRS, targetCRS);

        Geometry expResult = WKTReader.extract("POINT ZM(10 5 8 3)").getGeometry();
        Geometry result = GeometryTransformation.transform(sourceGeometry, transform);

        //System.out.println("Expected: " + expResult);
        //System.out.println("Result: " + result);
        assertEquals(expResult, result);
    }

    /**
     * Test of transform method, of class GeometryTransformation.
     *
     * @throws org.opengis.util.FactoryException
     * @throws org.opengis.referencing.operation.TransformException
     */
    @Test
    public void testTransform_Polygon() throws FactoryException, MismatchedDimensionException, TransformException {
        System.out.println("transform_Polygon");
        Geometry sourceGeometry = WKTReader.extract("POLYGON(5.0 5.0, 5.0 15.0, 15.0 15.0, 15.0 5.0, 5.0 5.0)").getGeometry();

        CoordinateReferenceSystem sourceCRS = CRS.forCode(SRS_URI.WGS84_CRS);
        CoordinateReferenceSystem targetCRS = CRS.forCode(SRSInfo.DEFAULT_WKT_CRS84_CODE);
        MathTransform transform = MathTransformRegistry.getMathTransform(sourceCRS, targetCRS);

        Geometry expResult = WKTReader.extract("POLYGON(5.0 5.0, 15.0 5.0, 15.0 15.0, 5.0 15.0, 5.0 5.0)").getGeometry();
        Geometry result = GeometryTransformation.transform(sourceGeometry, transform);

        //System.out.println("Expected: " + expResult);
        //System.out.println("Result: " + result);
        assertEquals(expResult, result);
    }

    //TODO - additional tests
}
