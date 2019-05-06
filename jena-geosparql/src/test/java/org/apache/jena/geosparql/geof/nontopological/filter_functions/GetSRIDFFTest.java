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
package org.apache.jena.geosparql.geof.nontopological.filter_functions;

import org.apache.jena.geosparql.implementation.datatype.WKTDatatype;
import org.apache.jena.geosparql.implementation.vocabulary.SRS_URI;
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
public class GetSRIDFFTest {

    public GetSRIDFFTest() {
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
     * Test of exec method, of class GetSRIDFF.
     */
    @Test
    public void testExec_asserted() {
        System.out.println("exec_asserted");
        NodeValue v = NodeValue.makeNode("<http://www.opengis.net/def/crs/EPSG/0/27700> POINT(10 10)", WKTDatatype.INSTANCE);
        GetSRIDFF instance = new GetSRIDFF();
        NodeValue expResult = NodeValue.makeString("http://www.opengis.net/def/crs/EPSG/0/27700");
        NodeValue result = instance.exec(v);
        assertEquals(expResult, result);
    }

    /**
     * Test of exec method, of class GetSRIDFF.
     */
    @Test
    public void testExec_implied() {
        System.out.println("exec_implied");
        NodeValue v = NodeValue.makeNode("POINT(10 10)", WKTDatatype.INSTANCE);
        GetSRIDFF instance = new GetSRIDFF();
        NodeValue expResult = NodeValue.makeString(SRS_URI.DEFAULT_WKT_CRS84);
        NodeValue result = instance.exec(v);
        assertEquals(expResult, result);
    }

}
