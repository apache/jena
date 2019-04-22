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

import org.apache.jena.geosparql.spatial.ConvertLatLonBox;
import org.apache.jena.geosparql.implementation.datatype.WKTDatatype;
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
public class ConvertLatLonBoxTest {

    public ConvertLatLonBoxTest() {
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
     * Test of toWKT method, of class ConvertLatLonBox.
     */
    @Test
    public void testToWKT() {
        System.out.println("toWKT");
        float latMin = 0.0F;
        float lonMin = 1.0F;
        float latMax = 10.0F;
        float lonMax = 11.0F;
        String expResult = "<http://www.opengis.net/def/crs/EPSG/0/4326> POLYGON((0 1, 10 1, 10 11, 0 11, 0 1))";
        String result = ConvertLatLonBox.toWKT(latMin, lonMin, latMax, lonMax);

        //System.out.println("Exp: " + expResult);
        //System.out.println("Res: " + result);
        assertEquals(expResult, result);
    }

    /**
     * Test of toLiteral method, of class ConvertLatLonBox.
     */
    @Test
    public void testToLiteral() {
        System.out.println("toLiteral");
        float latMin = 0.0F;
        float lonMin = 1.0F;
        float latMax = 10.0F;
        float lonMax = 11.0F;
        Literal expResult = ResourceFactory.createTypedLiteral("<http://www.opengis.net/def/crs/EPSG/0/4326> POLYGON((0 1, 10 1, 10 11, 0 11, 0 1))", WKTDatatype.INSTANCE);
        Literal result = ConvertLatLonBox.toLiteral(latMin, lonMin, latMax, lonMax);

        //System.out.println("Exp: " + expResult);
        //System.out.println("Res: " + result);
        assertEquals(expResult, result);
    }

    /**
     * Test of toNodeValue method, of class ConvertLatLonBox.
     */
    @Test
    public void testConvert_4args_1() {
        System.out.println("convert");
        NodeValue v1 = NodeValue.makeFloat(0.0f);
        NodeValue v2 = NodeValue.makeFloat(1.0f);
        NodeValue v3 = NodeValue.makeFloat(10.0f);
        NodeValue v4 = NodeValue.makeFloat(11.0f);
        NodeValue expResult = NodeValue.makeNode("<http://www.opengis.net/def/crs/EPSG/0/4326> POLYGON((0 1, 10 1, 10 11, 0 11, 0 1))", WKTDatatype.INSTANCE);
        NodeValue result = ConvertLatLonBox.toNodeValue(v1, v2, v3, v4);

        //System.out.println("Exp: " + expResult);
        //System.out.println("Res: " + result);
        assertEquals(expResult, result);
    }

    /**
     * Test of toNodeValue method, of class ConvertLatLonBox.
     */
    @Test
    public void testConvert_4args_2() {
        System.out.println("convert");
        Node n1 = NodeValue.makeFloat(0.0f).asNode();
        Node n2 = NodeValue.makeFloat(1.0f).asNode();
        Node n3 = NodeValue.makeFloat(10.0f).asNode();
        Node n4 = NodeValue.makeFloat(11.0f).asNode();
        Node expResult = NodeFactory.createLiteral("<http://www.opengis.net/def/crs/EPSG/0/4326> POLYGON((0 1, 10 1, 10 11, 0 11, 0 1))", WKTDatatype.INSTANCE);
        Node result = ConvertLatLonBox.toNode(n1, n2, n3, n4);

        //System.out.println("Exp: " + expResult);
        //System.out.println("Res: " + result);
        assertEquals(expResult, result);
    }

}
