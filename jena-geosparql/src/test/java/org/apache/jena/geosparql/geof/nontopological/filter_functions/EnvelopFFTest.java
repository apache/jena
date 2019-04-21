/*
 * Copyright 2018 the original author or authors.
 * See the notice.md file distributed with this work for additional
 * information regarding copyright ownership.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.jena.geosparql.geof.nontopological.filter_functions;

import org.apache.jena.geosparql.geof.nontopological.filter_functions.EnvelopFF;
import org.apache.jena.geosparql.implementation.datatype.WKTDatatype;
import org.apache.jena.sparql.expr.NodeValue;
import org.junit.After;
import org.junit.AfterClass;
import static org.junit.Assert.assertEquals;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 *
 *
 */
public class EnvelopFFTest {

    public EnvelopFFTest() {
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
     * Test of exec method, of class ConvexHullFF.
     */
    @Test
    public void testExec_point() {
        System.out.println("exec_point");
        NodeValue v = NodeValue.makeNode("<http://www.opengis.net/def/crs/EPSG/0/27700> POINT(10 10)", WKTDatatype.INSTANCE);
        EnvelopFF instance = new EnvelopFF();
        NodeValue expResult = NodeValue.makeNode("<http://www.opengis.net/def/crs/EPSG/0/27700> POINT(10 10)", WKTDatatype.INSTANCE);
        NodeValue result = instance.exec(v);

        //System.out.println("Exp: " + expResult);
        //System.out.println("Res: " + result);
        assertEquals(expResult, result);
    }

    /**
     * Test of exec method, of class ConvexHullFF.
     */
    @Test
    public void testExec_linestring() {
        System.out.println("exec_linestring");
        NodeValue v = NodeValue.makeNode("<http://www.opengis.net/def/crs/EPSG/0/27700> LINESTRING(0 0, 10 10, 5 5)", WKTDatatype.INSTANCE);
        EnvelopFF instance = new EnvelopFF();
        NodeValue expResult = NodeValue.makeNode("<http://www.opengis.net/def/crs/EPSG/0/27700> POLYGON((0 0, 0 10, 10 10, 10 0, 0 0))", WKTDatatype.INSTANCE);
        NodeValue result = instance.exec(v);

        //System.out.println("Exp: " + expResult);
        //System.out.println("Res: " + result);
        assertEquals(expResult, result);
    }

    /**
     * Test of exec method, of class ConvexHullFF.
     */
    @Test
    public void testExec_polygon() {
        System.out.println("exec_polygon");
        NodeValue v = NodeValue.makeNode("<http://www.opengis.net/def/crs/EPSG/0/27700> POLYGON((0 0, 0 10, 10 10, 10 0, 0 0))", WKTDatatype.INSTANCE);
        EnvelopFF instance = new EnvelopFF();
        NodeValue expResult = NodeValue.makeNode("<http://www.opengis.net/def/crs/EPSG/0/27700> POLYGON((0 0, 0 10, 10 10, 10 0, 0 0))", WKTDatatype.INSTANCE);
        NodeValue result = instance.exec(v);

        //System.out.println("Exp: " + expResult);
        //System.out.println("Res: " + result);
        assertEquals(expResult, result);
    }

}
