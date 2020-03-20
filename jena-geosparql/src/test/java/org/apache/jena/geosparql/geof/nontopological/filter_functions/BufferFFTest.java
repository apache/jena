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

import org.apache.jena.geosparql.implementation.GeometryWrapper;
import org.apache.jena.geosparql.implementation.datatype.WKTDatatype;
import org.apache.jena.geosparql.implementation.vocabulary.SRS_URI;
import org.apache.jena.geosparql.implementation.vocabulary.Unit_URI;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.sparql.expr.NodeValue;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Assert;
import static org.junit.Assert.assertEquals;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.opengis.geometry.MismatchedDimensionException;
import org.opengis.referencing.operation.TransformException;
import org.opengis.util.FactoryException;

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
    @Test
    public void testExec_Projection_NonLinear() {

        NodeValue v1 = NodeValue.makeNode("<http://www.opengis.net/def/crs/EPSG/0/27700> POINT(60 60)", WKTDatatype.INSTANCE);
        NodeValue v2 = NodeValue.makeDecimal(0.0002);
        NodeValue v3 = NodeValue.makeNode(NodeFactory.createURI(Unit_URI.DEGREE_URL));
        BufferFF instance = new BufferFF();
        NodeValue expResult = NodeValue.makeNode("<http://www.opengis.net/def/crs/EPSG/0/27700> POLYGON((74.40146 58.902956, 73.79184 54.591588, 72.686764 50.428725, 71.039028 46.753001, 68.992545 43.553727, 66.627526 40.936656, 64.024179 39.007539, 61.199008 37.98857, 58.359636 37.752618, 55.586275 38.405436, 52.942632 39.830584, 50.652834 42.123122, 48.700379 45.06086, 47.148974 48.527356, 46.062326 52.406168, 45.584349 56.686608, 45.61833 61.040732, 46.227974 65.352098, 47.33307 69.514956, 48.980819 73.190674, 51.027306 76.38994, 53.392322 79.007004, 55.995657 80.936115, 58.820809 81.955081, 61.660158 82.191031, 64.433497 81.538215, 67.07712 80.113072, 69.366904 77.82054, 71.319354 74.882809, 72.870762 71.416321, 73.957423 67.537515, 74.435419 63.257079, 74.40146 58.902956))", WKTDatatype.INSTANCE);
        NodeValue result = instance.exec(v1, v2, v3);
        assertEquals(expResult, result);
    }

    /**
     * Test of exec method, of class BufferFF.
     */
    @Test
    public void testExec_Geographic_Linear() {

        NodeValue v1 = NodeValue.makeNode("<http://www.opengis.net/def/crs/EPSG/0/4326> POLYGON((49.9 -7.5, 50.0 -7.5, 50.0 -7.4, 49.9 -7.4, 49.9 -7.5))", WKTDatatype.INSTANCE);
        NodeValue v2 = NodeValue.makeDecimal(20);
        NodeValue v3 = NodeValue.makeNode(NodeFactory.createURI(Unit_URI.METRE_URL));
        BufferFF instance = new BufferFF();
        NodeValue expResult = NodeValue.makeNode("<http://www.opengis.net/def/crs/EPSG/0/4326> POLYGON((49.89982 -7.5, 49.899824 -7.500054, 49.899834 -7.500106, 49.89985 -7.500155, 49.899873 -7.500197, 49.8999 -7.500231, 49.899931 -7.500257, 49.899965 -7.500273, 49.9 -7.500278, 50 -7.500279, 50.000035 -7.500274, 50.000069 -7.500258, 50.0001 -7.500232, 50.000127 -7.500197, 50.00015 -7.500155, 50.000166 -7.500107, 50.000176 -7.500055, 50.00018 -7.5, 50.00018 -7.4, 50.000176 -7.399945, 50.000166 -7.399893, 50.000149 -7.399845, 50.000127 -7.399803, 50.0001 -7.399768, 50.000069 -7.399742, 50.000035 -7.399726, 50 -7.399721, 49.9 -7.399722, 49.899965 -7.399727, 49.899931 -7.399743, 49.8999 -7.399769, 49.899873 -7.399803, 49.89985 -7.399845, 49.899834 -7.399894, 49.899824 -7.399946, 49.89982 -7.4, 49.89982 -7.5))", WKTDatatype.INSTANCE);
        NodeValue result = instance.exec(v1, v2, v3);
        assertEquals(expResult, result);
    }

    /**
     * Test of exec method, of class BufferFF.
     *
     * @throws org.opengis.referencing.operation.TransformException
     * @throws org.opengis.util.FactoryException
     */
    @Test
    public void testExec_Geographic_Linear2() throws MismatchedDimensionException, TransformException, FactoryException {

        //Test that buffering with Geographic geometry and Linear distance yields similar results to Projected geometry and Linear distance.
        GeometryWrapper originalGeometryWrapper = GeometryWrapper.extract("<http://www.opengis.net/def/crs/EPSG/0/27700> POLYGON((0.0 0.0, 0.0 100.0, 100.0 100.0, 100.0 0.0, 0.0 0.0))", WKTDatatype.URI);

        //Convert a projected GeometryWrapper to Geographic. Apply linear buffering. Convert back again.
        NodeValue v1a = originalGeometryWrapper.transform(SRS_URI.WGS84_CRS).asNodeValue();
        NodeValue v2 = NodeValue.makeDecimal(20);
        NodeValue v3 = NodeValue.makeNode(NodeFactory.createURI(Unit_URI.METRE_URL));
        BufferFF instance = new BufferFF();
        NodeValue bufferedGeographicNodeValue = instance.exec(v1a, v2, v3);
        GeometryWrapper bufferedGeographic = GeometryWrapper.extract(bufferedGeographicNodeValue);
        String resultLexicalForm = bufferedGeographic.transform(SRS_URI.OSGB36_CRS).asLiteral().getLexicalForm();

        //Apply linear buffering to projected GeometryWrapper.
        NodeValue v1b = originalGeometryWrapper.asNodeValue();
        NodeValue bufferedProjectedNodeValue = instance.exec(v1b, v2, v3);
        GeometryWrapper bufferedProjected = GeometryWrapper.extract(bufferedProjectedNodeValue);
        String expResultLexicalForm = bufferedProjected.asLiteral().getLexicalForm();

        //Unpack the values into arrays so can do comparison with tolerance.
        String[] resultsStr = resultLexicalForm.substring(resultLexicalForm.indexOf("((") + 2, resultLexicalForm.indexOf("))")).split(", ");
        String[] expResultsStr = expResultLexicalForm.substring(expResultLexicalForm.indexOf("((") + 2, expResultLexicalForm.indexOf("))")).split(", ");

        double[] results = new double[resultsStr.length * 2];
        int i = 0;
        for (String result : resultsStr) {
            String[] res = result.split(" ");
            results[i] = Double.parseDouble(res[0]);
            i++;
            results[i] = Double.parseDouble(res[1]);
            i++;
        }

        double[] expResults = new double[expResultsStr.length * 2];
        int j = 0;
        for (String result : expResultsStr) {
            String[] res = result.split(" ");
            expResults[j] = Double.parseDouble(res[0]);
            j++;
            expResults[j] = Double.parseDouble(res[1]);
            j++;
        }

        //Test accuracy of buffering to within 0.12m. Some error from coordinate and unit transformations.
        Assert.assertArrayEquals(expResults, results, 0.12);
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
