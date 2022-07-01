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
package org.apache.jena.geosparql.spatial;

import org.apache.jena.datatypes.DatatypeFormatException;
import org.apache.jena.geosparql.implementation.GeometryWrapper;
import org.apache.jena.geosparql.implementation.datatype.WKTDatatype;
import static org.apache.jena.geosparql.spatial.ConvertLatLon.extractDouble;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.rdf.model.Literal;
import org.apache.jena.rdf.model.ResourceFactory;
import org.apache.jena.sparql.expr.NodeValue;
import org.junit.After;
import org.junit.AfterClass;
import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 *
 *
 */
public class ConvertLatLonTest {

    public ConvertLatLonTest() {
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
     * Test of toNodeValue method, of class ConvertLatLon.
     */
    @Test
    public void testConvert_NodeValue_NodeValue() {

        NodeValue v1 = NodeValue.makeFloat(10.0f);
        NodeValue v2 = NodeValue.makeFloat(20.0f);
        NodeValue expResult = NodeValue.makeNode("<http://www.opengis.net/def/crs/EPSG/0/4326> POINT(10 20)", WKTDatatype.INSTANCE);
        NodeValue result = ConvertLatLon.toNodeValue(v1, v2);
        assertEquals(expResult, result);
    }

    /**
     * Test of checkBounds method, of class ConvertLatLon.
     */
    @Test(expected = DatatypeFormatException.class)
    public void testCheckBounds_lat_too_big() {

        double latitude = 90.1;
        double longitude = 0.0;
        ConvertLatLon.checkBounds(latitude, longitude);
    }

    /**
     * Test of checkBounds method, of class ConvertLatLon.
     */
    @Test(expected = DatatypeFormatException.class)
    public void testCheckBounds_lat_too_small() {

        double latitude = -90.1;
        double longitude = 0.0;
        ConvertLatLon.checkBounds(latitude, longitude);
    }

    /**
     * Test of checkBounds method, of class ConvertLatLon.
     */
    @Test(expected = DatatypeFormatException.class)
    public void testCheckBounds_lon_too_big() {

        double latitude = 0.0;
        double longitude = 180.1;
        ConvertLatLon.checkBounds(latitude, longitude);
    }

    /**
     * Test of checkBounds method, of class ConvertLatLon.
     */
    @Test(expected = DatatypeFormatException.class)
    public void testCheckBounds_lon_too_small() {

        double latitude = 0.0;
        double longitude = -180.1;
        ConvertLatLon.checkBounds(latitude, longitude);
    }

    /**
     * Test of checkBounds method, of class ConvertLatLon.
     */
    @Test
    public void testCheckBounds_lat_big() {

        double latitude = 90.0;
        double longitude = 0.0;
        ConvertLatLon.checkBounds(latitude, longitude);
    }

    /**
     * Test of checkBounds method, of class ConvertLatLon.
     */
    @Test
    public void testCheckBounds_lat_small() {

        double latitude = -90.0;
        double longitude = 0.0;
        ConvertLatLon.checkBounds(latitude, longitude);
    }

    /**
     * Test of checkBounds method, of class ConvertLatLon.
     */
    @Test
    public void testCheckBounds_lon_big() {

        double latitude = 0.0;
        double longitude = 180.0;
        ConvertLatLon.checkBounds(latitude, longitude);
    }

    /**
     * Test of checkBounds method, of class ConvertLatLon.
     */
    @Test
    public void testCheckBounds_lon_small() {

        double latitude = 0.0;
        double longitude = -180.0;
        ConvertLatLon.checkBounds(latitude, longitude);
    }

    /**
     * Test of toWKT method, of class ConvertLatLon.
     */
    @Test
    public void testToWKT() {

        float lat = 10.0F;
        float lon = 20.0F;
        String expResult = "<http://www.opengis.net/def/crs/EPSG/0/4326> POINT(10 20)";
        String result = ConvertLatLon.toWKT(lat, lon);
        assertEquals(expResult, result);
    }

    /**
     * Test of toLiteral method, of class ConvertLatLon.
     */
    @Test
    public void testToLiteral() {

        float lat = 10.0F;
        float lon = 20.0F;
        Literal expResult = ResourceFactory.createTypedLiteral("<http://www.opengis.net/def/crs/EPSG/0/4326> POINT(10 20)", WKTDatatype.INSTANCE);
        Literal result = ConvertLatLon.toLiteral(lat, lon);
        assertEquals(expResult, result);
    }

    /**
     * Test of toNode method, of class ConvertLatLonFF.
     */
    @Test
    public void testConvertToNode_Node_Node() {

        Node n1 = NodeValue.makeFloat(10.0f).asNode();
        Node n2 = NodeValue.makeFloat(20.0f).asNode();
        Node expResult = NodeFactory.createLiteral("<http://www.opengis.net/def/crs/EPSG/0/4326> POINT(10 20)", WKTDatatype.INSTANCE);
        Node result = ConvertLatLon.toNode(n1, n2);
        assertEquals(expResult, result);
    }
    
    /**
     * Test of toNode method, of class ConvertLatLonFF.
     */
    @Test
    public void testConvertToNode_NodeString_NodeString() {

        Node n1 = NodeValue.makeString("10.0").asNode();
        Node n2 = NodeValue.makeString("20").asNode();
        Node expResult = NodeFactory.createLiteral("<http://www.opengis.net/def/crs/EPSG/0/4326> POINT(10 20)", WKTDatatype.INSTANCE);
        Node result = ConvertLatLon.toNode(n1, n2);
        assertEquals(expResult, result);
    }
    
    /**
     * Test of toNode method, of class ConvertLatLonFF.
     */
    @Test(expected = DatatypeFormatException.class)
    public void testConvertToNode_NodeString_NodeString_Malformed() {

        Node n1 = NodeValue.makeString("val").asNode();
        Node n2 = NodeValue.makeString("20.0").asNode();
        Node expResult = NodeFactory.createLiteral("<http://www.opengis.net/def/crs/EPSG/0/4326> POINT(10 20)", WKTDatatype.INSTANCE);
        Node result = ConvertLatLon.toNode(n1, n2);
        assertEquals(expResult, result);
    }
    
    /**
     * Test of toGeometryWrapper method, of class ConvertLatLonFF.
     */
    @Test
    public void testConvertToGeometryWrapper_NodeString_NodeString() {

        Node n1 = NodeValue.makeString("10.0").asNode();
        Node n2 = NodeValue.makeString("20").asNode();
        GeometryWrapper expResult = GeometryWrapper.extract("<http://www.opengis.net/def/crs/EPSG/0/4326> POINT(10 20)", WKTDatatype.URI);
        GeometryWrapper result = ConvertLatLon.toGeometryWrapper(n1, n2);
        assertEquals(expResult, result);
    }
    
    /**
     * Test of toGeometryWrapper method, of class ConvertLatLonFF.
     */
    @Test(expected = DatatypeFormatException.class)
    public void testConvertToGeometryWrapper_NodeString_NodeString_Malformed() {

        Node n1 = NodeValue.makeString("val").asNode();
        Node n2 = NodeValue.makeString("20.0").asNode();
        GeometryWrapper expResult = GeometryWrapper.extract("<http://www.opengis.net/def/crs/EPSG/0/4326> POINT(10 20)", WKTDatatype.URI);
        GeometryWrapper result = ConvertLatLon.toGeometryWrapper(n1, n2);
        assertEquals(expResult, result);
    }
    
    /**
     * Test of extractDouble method, of class ConvertLatLonFF.
     */
    @Test
    public void testExtractDouble() {
        
        NodeValue nv = NodeValue.makeDouble(10.0);        
        double result = extractDouble(nv);
        assertEquals(10.0, result, 0);
    }
    
    /**
     * Test of extractDouble method, of class ConvertLatLonFF.
     */
    @Test
    public void testExtractDouble_String() {
        
        NodeValue nv = NodeValue.makeString("10.0");        
        double result = extractDouble(nv);
        assertEquals(10.0, result, 0);
    }
    
    /**
     * Test of extractDouble method, of class ConvertLatLonFF.
     */
    @Test(expected = DatatypeFormatException.class)
    public void testExtractDouble_Malformed() {
        
        NodeValue nv = NodeValue.makeString("val");        
        extractDouble(nv);
    }
    
}
