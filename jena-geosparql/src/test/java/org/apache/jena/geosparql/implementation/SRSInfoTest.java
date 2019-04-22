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

import org.apache.jena.geosparql.implementation.vocabulary.SRS_URI;
import org.apache.sis.referencing.CRS;
import org.junit.After;
import org.junit.AfterClass;
import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.locationtech.jts.geom.Envelope;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.util.FactoryException;

/**
 *
 *
 */
public class SRSInfoTest {

    public SRSInfoTest() {
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
     * Test of buildDomainEnvelope method, of class SRSInfo.
     *
     * @throws org.opengis.util.FactoryException
     */
    @Test
    public void testBuildDomainEnvelope_WGS84() throws FactoryException {
        System.out.println("buildDomainEnvelope_WGS84");
        String srsURI = SRS_URI.WGS84_CRS;
        CoordinateReferenceSystem crs = CRS.forCode(srsURI);
        Boolean isAxisXY = SRSInfo.checkAxisXY(crs);
        Envelope expResult = new Envelope(-180, 180, -90, 90);
        Envelope result = SRSInfo.buildDomainEnvelope(crs, isAxisXY);

        //System.out.println("Exp: " + expResult);
        //System.out.println("Res: " + result);
        assertEquals(expResult, result);
    }

    /**
     * Test of buildDomainEnvelope method, of class SRSInfo.
     *
     * @throws org.opengis.util.FactoryException
     */
    @Test
    public void testBuildDomainEnvelope_CRS84() throws FactoryException {
        System.out.println("buildDomainEnvelope_CRS84");
        String srsURI = SRS_URI.DEFAULT_WKT_CRS84;
        CoordinateReferenceSystem crs = CRS.forCode(srsURI);
        Boolean isAxisXY = SRSInfo.checkAxisXY(crs);

        Envelope expResult = new Envelope(-180, 180, -90, 90);
        Envelope result = SRSInfo.buildDomainEnvelope(crs, isAxisXY);

        //System.out.println("Exp: " + expResult);
        //System.out.println("Res: " + result);
        assertEquals(expResult, result);
    }

    /**
     * Test of buildDomainEnvelope method, of class SRSInfo.
     *
     * @throws org.opengis.util.FactoryException
     */
    @Test
    public void testBuildDomainEnvelope_OSGB36() throws FactoryException {
        System.out.println("buildDomainEnvelope_OSGB36");
        String srsURI = SRS_URI.OSGB36_CRS;
        CoordinateReferenceSystem crs = CRS.forCode(srsURI);
        Boolean isAxisXY = SRSInfo.checkAxisXY(crs);

        Envelope expResult = new Envelope(-118397.00138845091, 751441.7790901454, -16627.734375018626, 1272149.3463499574);
        Envelope result = SRSInfo.buildDomainEnvelope(crs, isAxisXY);

        //System.out.println("Exp: " + expResult);
        //System.out.println("Res: " + result);
        assertEquals(expResult, result);
    }

    /**
     * Test of checkAxisXY method, of class SRSInfo.
     *
     * @throws org.opengis.util.FactoryException
     */
    @Test
    public void testCheckAxisXY_WGS84() throws FactoryException {
        System.out.println("checkAxisXY_WGS84");
        CoordinateReferenceSystem crs = CRS.forCode(SRS_URI.WGS84_CRS);
        Boolean expResult = false;
        Boolean result = SRSInfo.checkAxisXY(crs);

        //System.out.println("Exp: " + expResult);
        //System.out.println("Res: " + result);
        assertEquals(expResult, result);
    }

    /**
     * Test of checkAxisXY method, of class SRSInfo.
     *
     * @throws org.opengis.util.FactoryException
     */
    @Test
    public void testCheckAxisXY_CRS84() throws FactoryException {
        System.out.println("checkAxisXY_CRS84");
        CoordinateReferenceSystem crs = CRS.forCode("CRS:84");
        Boolean expResult = true;
        Boolean result = SRSInfo.checkAxisXY(crs);

        //System.out.println("Exp: " + expResult);
        //System.out.println("Res: " + result);
        assertEquals(expResult, result);
    }

    /**
     * Test of checkAxisXY method, of class SRSInfo.
     *
     * @throws org.opengis.util.FactoryException
     */
    @Test
    public void testCheckAxisXY_OSGB36() throws FactoryException {
        System.out.println("checkAxisXY_OSGB36");
        CoordinateReferenceSystem crs = CRS.forCode(SRS_URI.OSGB36_CRS);
        Boolean expResult = true;
        Boolean result = SRSInfo.checkAxisXY(crs);

        //System.out.println("Exp: " + expResult);
        //System.out.println("Res: " + result);
        assertEquals(expResult, result);
    }
}
