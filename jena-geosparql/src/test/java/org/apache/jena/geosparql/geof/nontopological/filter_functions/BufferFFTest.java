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
import org.apache.jena.geosparql.implementation.vocabulary.Unit_URI;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.sparql.expr.ExprEvalException;
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
public class BufferFFTest {

    public BufferFFTest() {
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
     * Test of exec method, of class BufferFF.
     */
    @Test
    public void testExec_Projection_Linear() {

        NodeValue v1 = NodeValue.makeNode("<http://www.opengis.net/def/crs/EPSG/0/27700> POINT(60 60)", WKTDatatype.INSTANCE);
        NodeValue v2 = NodeValue.makeDecimal(20);
        NodeValue v3 = NodeValue.makeNode(NodeFactory.createURI(Unit_URI.METRE_URL));
        BufferFF instance = new BufferFF();
        NodeValue expResult = NodeValue.makeNode("<http://www.opengis.net/def/crs/EPSG/0/27700> POLYGON((80 60, 79.615706 56.098194, 78.477591 52.346331, 76.629392 48.888595, 74.142136 45.857864, 71.111405 43.370608, 67.653669 41.522409, 63.901806 40.384294, 60 40, 56.098194 40.384294, 52.346331 41.522409, 48.888595 43.370608, 45.857864 45.857864, 43.370608 48.888595, 41.522409 52.346331, 40.384294 56.098194, 40 60, 40.384294 63.901806, 41.522409 67.653669, 43.370608 71.111405, 45.857864 74.142136, 48.888595 76.629392, 52.346331 78.477591, 56.098194 79.615706, 60 80, 63.901806 79.615706, 67.653669 78.477591, 71.111405 76.629392, 74.142136 74.142136, 76.629392 71.111405, 78.477591 67.653669, 79.615706 63.901806, 80 60))", WKTDatatype.INSTANCE);
        NodeValue result = instance.exec(v1, v2, v3);
        assertEquals(expResult, result);
    }

    /**
     * Test of exec method, of class BufferFF.
     */
    @Test(expected = ExprEvalException.class)
    public void testExec_Projection_NonLinear_exception() {

        NodeValue v1 = NodeValue.makeNode("<http://www.opengis.net/def/crs/EPSG/0/27700> POINT(60 60)", WKTDatatype.INSTANCE);
        NodeValue v2 = NodeValue.makeDecimal(0.0002);
        NodeValue v3 = NodeValue.makeNode(NodeFactory.createURI(Unit_URI.DEGREE_URL));
        BufferFF instance = new BufferFF();
        NodeValue result = instance.exec(v1, v2, v3);
    }

    /**
     * Test of exec method, of class BufferFF.
     */
    @Test(expected = ExprEvalException.class)
    public void testExec_Geographic_Linear_Exception() {

        NodeValue v1 = NodeValue.makeNode("<http://www.opengis.net/def/crs/EPSG/0/4326> POLYGON((49.9 -7.5, 50.0 -7.5, 50.0 -7.4, 49.9 -7.4, 49.9 -7.5))", WKTDatatype.INSTANCE);
        NodeValue v2 = NodeValue.makeDecimal(20);
        NodeValue v3 = NodeValue.makeNode(NodeFactory.createURI(Unit_URI.METRE_URL));
        BufferFF instance = new BufferFF();
        NodeValue result = instance.exec(v1, v2, v3);
    }

    /**
     * Test of exec method, of class BufferFF.
     */
    @Test
    public void testExec_Geographic_NonLinear() {

        NodeValue v1 = NodeValue.makeNode("<http://www.opengis.net/def/crs/EPSG/0/4326> POLYGON((49.9 -7.5, 50.0 -7.5, 50.0 -7.4, 49.9 -7.4, 49.9 -7.5))", WKTDatatype.INSTANCE);
        NodeValue v2 = NodeValue.makeDecimal(0.0002);
        NodeValue v3 = NodeValue.makeNode(NodeFactory.createURI(Unit_URI.DEGREE_URL));
        BufferFF instance = new BufferFF();
        NodeValue expResult = NodeValue.makeNode("<http://www.opengis.net/def/crs/EPSG/0/4326> POLYGON((49.8998 -7.5, 49.899804 -7.500039, 49.899815 -7.500077, 49.899834 -7.500111, 49.899859 -7.500141, 49.899889 -7.500166, 49.899923 -7.500185, 49.899961 -7.500196, 49.9 -7.5002, 50 -7.5002, 50.000039 -7.500196, 50.000077 -7.500185, 50.000111 -7.500166, 50.000141 -7.500141, 50.000166 -7.500111, 50.000185 -7.500077, 50.000196 -7.500039, 50.0002 -7.5, 50.0002 -7.4, 50.000196 -7.399961, 50.000185 -7.399923, 50.000166 -7.399889, 50.000141 -7.399859, 50.000111 -7.399834, 50.000077 -7.399815, 50.000039 -7.399804, 50 -7.3998, 49.9 -7.3998, 49.899961 -7.399804, 49.899923 -7.399815, 49.899889 -7.399834, 49.899859 -7.399859, 49.899834 -7.399889, 49.899815 -7.399923, 49.899804 -7.399961, 49.8998 -7.4, 49.8998 -7.5))", WKTDatatype.INSTANCE);
        NodeValue result = instance.exec(v1, v2, v3);
        assertEquals(expResult, result);
    }

}
