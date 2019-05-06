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

import org.apache.jena.geosparql.implementation.vocabulary.Unit_URI;
import org.apache.sis.referencing.CRS;
import org.junit.After;
import org.junit.AfterClass;
import static org.junit.Assert.assertEquals;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.util.FactoryException;

/**
 *
 *
 */
public class UnitsOfMeasureTest {

    public UnitsOfMeasureTest() {
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
     * Test of conversion method, of class UnitsOfMeasure.
     *
     * @throws org.opengis.util.FactoryException
     */
    @Test(expected = UnitsConversionException.class)
    public void testConversionRadianToMetre() throws FactoryException {

        double distance = 0.5;
        String sourceDistanceUnitURI = "http://www.opengis.net/def/uom/OGC/1.0/radian";
        CoordinateReferenceSystem crs = CRS.forCode("http://www.opengis.net/def/crs/EPSG/0/27700");  //OSGB - metres projected

        UnitsOfMeasure targetUnitsOfMeasure = new UnitsOfMeasure(crs);
        Double expResult = null;
        Double result = UnitsOfMeasure.conversion(distance, sourceDistanceUnitURI, targetUnitsOfMeasure.getUnitURI());
    }

    /**
     * Test of conversion method, of class UnitsOfMeasure.
     *
     * @throws org.opengis.util.FactoryException
     */
    @Test(expected = UnitsConversionException.class)
    public void testConversionMetreToDegree() throws FactoryException {

        double distance = 100.0;
        String sourceDistanceUnitURI = "http://www.opengis.net/def/uom/OGC/1.0/metre";
        CoordinateReferenceSystem crs = CRS.forCode("http://www.opengis.net/def/crs/EPSG/0/4326");  //OSGB - metres projected

        UnitsOfMeasure targetUnitsOfMeasure = new UnitsOfMeasure(crs);
        Double expResult = null;
        Double result = UnitsOfMeasure.conversion(distance, sourceDistanceUnitURI, targetUnitsOfMeasure.getUnitURI());
    }

    /**
     * Test of conversion method, of class UnitsOfMeasure.
     *
     * @throws org.opengis.util.FactoryException
     */
    @Test
    public void testConversionMetreToMetre() throws FactoryException {

        double distance = 100.0;
        String sourceDistanceUnitURI = "http://www.opengis.net/def/uom/OGC/1.0/metre";
        CoordinateReferenceSystem crs = CRS.forCode("http://www.opengis.net/def/crs/EPSG/0/27700");  //OSGB - metres projected

        UnitsOfMeasure targetUnitsOfMeasure = new UnitsOfMeasure(crs);
        double expResult = 100.0;
        double result = UnitsOfMeasure.conversion(distance, sourceDistanceUnitURI, targetUnitsOfMeasure.getUnitURI());
        assertEquals(expResult, result, 0.0);
    }

    /**
     * Test of conversion method, of class UnitsOfMeasure.
     *
     * @throws org.opengis.util.FactoryException
     */
    @Test
    public void testConversionDegreeToDegree() throws FactoryException {

        double distance = 100.0;
        String sourceDistanceUnitURI = "http://www.opengis.net/def/uom/OGC/1.0/degree";
        CoordinateReferenceSystem crs = CRS.forCode("http://www.opengis.net/def/crs/EPSG/0/4326");  //WGS84 degrees non-projected

        UnitsOfMeasure targetUnitsOfMeasure = new UnitsOfMeasure(crs);
        double expResult = 100.0;
        double result = UnitsOfMeasure.conversion(distance, sourceDistanceUnitURI, targetUnitsOfMeasure.getUnitURI());
        assertEquals(expResult, result, 0.0);
    }

    /**
     * Test of conversion method, of class UnitsOfMeasure.
     *
     * @throws org.opengis.util.FactoryException
     */
    @Test
    public void testConversionRadianToDegree() throws FactoryException {

        double distance = 0.5;
        String sourceDistanceUnitURI = "http://www.opengis.net/def/uom/OGC/1.0/radian";
        CoordinateReferenceSystem crs = CRS.forCode("http://www.opengis.net/def/crs/EPSG/0/4326");  //WGS84 degrees non-projected

        UnitsOfMeasure targetUnitsOfMeasure = new UnitsOfMeasure(crs);
        double radsToDegrees = 180 / Math.PI;
        double expResult = distance * radsToDegrees;
        double result = UnitsOfMeasure.conversion(distance, sourceDistanceUnitURI, targetUnitsOfMeasure.getUnitURI());
        assertEquals(expResult, result, 0.0);
    }

    /**
     * Test of conversion method, of class UnitsOfMeasure.
     *
     * @throws org.opengis.util.FactoryException
     */
    @Test(expected = UnitsConversionException.class)
    public void testConversionDegreeToMetre() throws FactoryException {

        double distance = 10.0;
        String sourceDistanceUnitURI = "http://www.opengis.net/def/uom/OGC/1.0/degree";
        CoordinateReferenceSystem crs = CRS.forCode("http://www.opengis.net/def/crs/EPSG/0/27700");  //OSGB metres projected

        UnitsOfMeasure targetUnitsOfMeasure = new UnitsOfMeasure(crs);

        Double expResult = null;
        Double result = UnitsOfMeasure.conversion(distance, sourceDistanceUnitURI, targetUnitsOfMeasure.getUnitURI());
    }

    /**
     * Test of convert method, of class DistanceToDegrees.
     */
    @Test
    public void testConvert_0_degree() {

        double distance = 111319.9;
        String unitsURI = Unit_URI.METRE_URL;
        double latitude = 0.0;
        double expResult = 1.0;
        double result = UnitsOfMeasure.convertToDegrees(distance, unitsURI, latitude);
        assertEquals(expResult, result, 0.00001);
    }

    /**
     * Test of convert method, of class DistanceToDegrees.
     */
    @Test
    public void testConvert_23_degree() {

        double distance = 102470.508;
        String unitsURI = Unit_URI.METRE_URL;
        double latitude = 23.0;
        double expResult = 1.0;
        double result = UnitsOfMeasure.convertToDegrees(distance, unitsURI, latitude);
        assertEquals(expResult, result, 0.00001);
    }

    /**
     * Test of convert method, of class DistanceToDegrees.
     */
    @Test
    public void testConvert_45_degree() {

        double distance = 78715.05;
        String unitsURI = Unit_URI.METRE_URL;
        double latitude = 45.0;
        double expResult = 1.0;
        double result = UnitsOfMeasure.convertToDegrees(distance, unitsURI, latitude);
        assertEquals(expResult, result, 0.00001);
    }

    /**
     * Test of convert method, of class DistanceToDegrees.
     */
    @Test
    public void testConvert_67_degree() {

        double distance = 43496.15;
        String unitsURI = Unit_URI.METRE_URL;
        double latitude = 67.0;
        double expResult = 1.0;
        double result = UnitsOfMeasure.convertToDegrees(distance, unitsURI, latitude);
        assertEquals(expResult, result, 0.00001);
    }

    /**
     * Test of convert method, of class DistanceToDegrees.
     */
    @Test
    public void testConvert_67_degree2() {

        double distance = 1.0;
        String unitsURI = Unit_URI.DEGREE_URL;
        double latitude = 67.0;
        double expResult = 1.0;
        double result = UnitsOfMeasure.convertToDegrees(distance, unitsURI, latitude);
        assertEquals(expResult, result, 0.00001);
    }

    /**
     * Test of convert method, of class DistanceToDegrees.
     */
    @Test
    public void testConvert_0_degree_metres() {

        double distance = 1.0;
        String unitsURI = Unit_URI.DEGREE_URL;
        double latitude = 0.0;
        double expResult = 111319.8922;
        double result = UnitsOfMeasure.convertToMetres(distance, unitsURI, latitude);
        assertEquals(expResult, result, 0.001);
    }

    /**
     * Test of convert method, of class DistanceToDegrees.
     */
    @Test
    public void testConvert_23_degree_metres() {

        double distance = 1.0;
        String unitsURI = Unit_URI.DEGREE_URL;
        double latitude = 23.0;
        double expResult = 102470.501;
        double result = UnitsOfMeasure.convertToMetres(distance, unitsURI, latitude);
        assertEquals(expResult, result, 0.001);
    }

    /**
     * Test of convert method, of class DistanceToDegrees.
     */
    @Test
    public void testConvert_45_degree_metres() {

        double distance = 1.0;
        String unitsURI = Unit_URI.DEGREE_URL;
        double latitude = 45.0;
        double expResult = 78715.050;
        double result = UnitsOfMeasure.convertToMetres(distance, unitsURI, latitude);
        assertEquals(expResult, result, 0.001);
    }

    /**
     * Test of convert method, of class DistanceToDegrees.
     */
    @Test
    public void testConvert_67_degree_metres() {

        double distance = 1.0;
        String unitsURI = Unit_URI.DEGREE_URL;
        double latitude = 67.0;
        double expResult = 43496.147;
        double result = UnitsOfMeasure.convertToMetres(distance, unitsURI, latitude);
        assertEquals(expResult, result, 0.001);
    }

    /**
     * Test of convert method, of class DistanceToDegrees.
     */
    @Test
    public void testConvert_67_degree2_metres() {

        double distance = 43496.15;
        String unitsURI = Unit_URI.METRE_URL;
        double latitude = 67.0;
        double expResult = 43496.15;
        double result = UnitsOfMeasure.convertToMetres(distance, unitsURI, latitude);
        assertEquals(expResult, result, 0.001);
    }

}
