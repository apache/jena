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
package org.apache.jena.geosparql.geo.topological.property_functions.geometry_property;

import org.apache.jena.geosparql.implementation.GeometryWrapper;
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
public class IsEmptyPFTest {

    public IsEmptyPFTest() {
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
     * Test of applyPredicate method, of class IsEmptyPF.
     */
    @Test
    public void testApplyPredicate_false() {
        System.out.println("applyPredicate_false");
        GeometryWrapper geometryWrapper = GeometryWrapper.extract("<http://www.opengis.net/def/crs/EPSG/0/27700> POINT(90 60)", WKTDatatype.URI);
        IsEmptyPF instance = new IsEmptyPF();
        NodeValue expResult = NodeValue.makeNodeBoolean(false);
        NodeValue result = instance.applyPredicate(geometryWrapper);
        assertEquals(expResult, result);
    }

    /**
     * Test of applyPredicate method, of class IsEmptyPF.
     */
    @Test
    public void testApplyPredicate_true() {
        System.out.println("applyPredicate_true");
        GeometryWrapper geometryWrapper = GeometryWrapper.extract("<http://www.opengis.net/def/crs/EPSG/0/27700> POINT EMPTY", WKTDatatype.URI);
        IsEmptyPF instance = new IsEmptyPF();
        NodeValue expResult = NodeValue.makeNodeBoolean(true);
        NodeValue result = instance.applyPredicate(geometryWrapper);
        assertEquals(expResult, result);
    }

}
