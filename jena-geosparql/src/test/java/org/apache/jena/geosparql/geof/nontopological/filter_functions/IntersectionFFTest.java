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

import org.apache.jena.geosparql.geof.nontopological.filter_functions.IntersectionFF;
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
public class IntersectionFFTest {

    public IntersectionFFTest() {
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
     * Test of exec method, of class IntersectionFF.
     */
    @Test
    public void testExec_point_polygon() {
        System.out.println("exec_point_polygon");
        NodeValue v1 = NodeValue.makeNode("<http://www.opengis.net/def/crs/EPSG/0/27700> POINT(30 70)", WKTDatatype.INSTANCE);
        NodeValue v2 = NodeValue.makeNode("<http://www.opengis.net/def/crs/EPSG/0/27700> POLYGON((30 40, 30 70, 90 70, 90 40, 30 40))", WKTDatatype.INSTANCE);
        IntersectionFF instance = new IntersectionFF();
        NodeValue expResult = NodeValue.makeNode("<http://www.opengis.net/def/crs/EPSG/0/27700> POINT(30 70)", WKTDatatype.INSTANCE);
        NodeValue result = instance.exec(v1, v2);

        //System.out.println("Exp: " + expResult);
        //System.out.println("Res: " + result);
        assertEquals(expResult, result);
    }

    /**
     * Test of exec method, of class IntersectionFF.
     */
    @Test
    public void testExec_linestring_polygon() {
        System.out.println("exec_linestring_polygon");
        NodeValue v1 = NodeValue.makeNode("<http://www.opengis.net/def/crs/EPSG/0/27700> LINESTRING(80 85, 80 30)", WKTDatatype.INSTANCE);
        NodeValue v2 = NodeValue.makeNode("<http://www.opengis.net/def/crs/EPSG/0/27700> POLYGON((30 40, 30 70, 90 70, 90 40, 30 40))", WKTDatatype.INSTANCE);
        IntersectionFF instance = new IntersectionFF();
        NodeValue expResult = NodeValue.makeNode("<http://www.opengis.net/def/crs/EPSG/0/27700> LINESTRING(80 70, 80 40)", WKTDatatype.INSTANCE);
        NodeValue result = instance.exec(v1, v2);

        //System.out.println("Exp: " + expResult);
        //System.out.println("Res: " + result);
        assertEquals(expResult, result);
    }

    /**
     * Test of exec method, of class IntersectionFF.
     */
    @Test
    public void testExec_linestring_polygon_empty() {
        System.out.println("exec_linestring_polygon_empty");
        NodeValue v1 = NodeValue.makeNode("<http://www.opengis.net/def/crs/EPSG/0/27700> LINESTRING(0 0, 0 30)", WKTDatatype.INSTANCE);
        NodeValue v2 = NodeValue.makeNode("<http://www.opengis.net/def/crs/EPSG/0/27700> POLYGON((30 40, 30 70, 90 70, 90 40, 30 40))", WKTDatatype.INSTANCE);
        IntersectionFF instance = new IntersectionFF();
        NodeValue expResult = NodeValue.makeNode("<http://www.opengis.net/def/crs/EPSG/0/27700> LINESTRING EMPTY", WKTDatatype.INSTANCE);
        NodeValue result = instance.exec(v1, v2);

        //System.out.println("Exp: " + expResult);
        //System.out.println("Res: " + result);
        assertEquals(expResult, result);
    }

}
