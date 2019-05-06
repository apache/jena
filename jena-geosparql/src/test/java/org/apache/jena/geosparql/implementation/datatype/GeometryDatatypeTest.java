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
package org.apache.jena.geosparql.implementation.datatype;

import org.apache.jena.datatypes.DatatypeFormatException;
import org.apache.jena.datatypes.RDFDatatype;
import org.apache.jena.datatypes.xsd.XSDDatatype;
import org.junit.After;
import org.junit.AfterClass;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 *
 *
 */
public class GeometryDatatypeTest {

    public GeometryDatatypeTest() {
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
     * Test of get method, of class DatatypeUtil.
     */
    @Test
    public void testGetGeometryDatatype_RDFDatatype() {
        System.out.println("getGeometryDatatype");
        RDFDatatype rdfDatatype = WKTDatatype.INSTANCE;
        GeometryDatatype expResult = WKTDatatype.INSTANCE;
        GeometryDatatype result = GeometryDatatype.get(rdfDatatype);

        //System.out.println("Exp: " + expResult);
        //System.out.println("Res: " + result);
        assertEquals(expResult, result);
    }

    /**
     * Test of get method, of class DatatypeUtil.
     */
    @Test(expected = DatatypeFormatException.class)
    public void testGetGeometryDatatype_RDFDatatype_Fail() {
        System.out.println("getGeometryDatatype_Fail");
        RDFDatatype rdfDatatype = XSDDatatype.XSDdouble;
        GeometryDatatype.get(rdfDatatype);
        fail("Exception not thrown when expected.");
    }

    /**
     * Test of get method, of class DatatypeUtil.
     */
    @Test
    public void testGetGeometryDatatype_String() {
        System.out.println("getGeometryDatatype");
        String datatypeURI = WKTDatatype.URI;
        GeometryDatatype expResult = WKTDatatype.INSTANCE;
        GeometryDatatype result = GeometryDatatype.get(datatypeURI);

        //System.out.println("Exp: " + expResult);
        //System.out.println("Res: " + result);
        assertEquals(expResult, result);
    }

    /**
     * Test of checkURI method, of class DatatypeUtil.
     */
    @Test
    public void testCheckGeometryDatatypeURI() {
        System.out.println("checkGeometryDatatypeURI");
        String datatypeURI = WKTDatatype.URI;
        boolean expResult = true;
        boolean result = GeometryDatatype.checkURI(datatypeURI);

        //System.out.println("Exp: " + expResult);
        //System.out.println("Res: " + result);
        assertEquals(expResult, result);
    }

    /**
     * Test of checkURI method, of class DatatypeUtil.
     */
    @Test
    public void testCheckGeometryDatatypeURI_Fail() {
        System.out.println("checkGeometryDatatypeURI_Fail");
        String datatypeURI = XSDDatatype.XSDdouble.getURI();
        GeometryDatatype.checkURI(datatypeURI);
        boolean expResult = false;
        boolean result = GeometryDatatype.checkURI(datatypeURI);

        //System.out.println("Exp: " + expResult);
        //System.out.println("Res: " + result);
        assertEquals(expResult, result);
    }

    /**
     * Test of check method, of class DatatypeUtil.
     */
    @Test
    public void testCheckGeometryDatatype() {
        System.out.println("checkGeometryDatatype");
        RDFDatatype rdfDatatype = WKTDatatype.INSTANCE;
        boolean expResult = true;
        boolean result = GeometryDatatype.check(rdfDatatype);

        //System.out.println("Exp: " + expResult);
        //System.out.println("Res: " + result);
        assertEquals(expResult, result);
    }

    /**
     * Test of check method, of class DatatypeUtil.
     */
    @Test
    public void testCheckGeometryDatatype_Fail() {
        System.out.println("checkGeometryDatatype_Fail");
        RDFDatatype rdfDatatype = XSDDatatype.XSDdouble;
        boolean expResult = false;
        boolean result = GeometryDatatype.check(rdfDatatype);

        //System.out.println("Exp: " + expResult);
        //System.out.println("Res: " + result);
        assertEquals(expResult, result);
    }

}
