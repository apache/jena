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
        System.out.println("exec_Projection_Linear");
        NodeValue v1 = NodeValue.makeNode("<http://www.opengis.net/def/crs/EPSG/0/27700> POINT(60 60)", WKTDatatype.INSTANCE);
        NodeValue v2 = NodeValue.makeDecimal(20);
        NodeValue v3 = NodeValue.makeNode(NodeFactory.createURI(Unit_URI.METRE_URL));
        BufferFF instance = new BufferFF();
        NodeValue expResult = NodeValue.makeNode("<http://www.opengis.net/def/crs/EPSG/0/27700> POLYGON((80 60, 79.61570560806462 56.09819355967743, 78.47759065022574 52.3463313526982, 76.62939224605091 48.88859533960796, 74.14213562373095 45.85786437626905, 71.11140466039204 43.370607753949095, 67.6536686473018 41.522409349774264, 63.90180644032257 40.38429439193539, 60 40, 56.09819355967743 40.38429439193539, 52.34633135269821 41.522409349774264, 48.88859533960796 43.370607753949095, 45.85786437626905 45.85786437626905, 43.370607753949095 48.88859533960796, 41.522409349774264 52.346331352698215, 40.384294391935384 56.09819355967745, 40 60.000000000000014, 40.3842943919354 63.90180644032259, 41.52240934977428 67.65366864730181, 43.37060775394911 71.11140466039207, 45.85786437626908 74.14213562373098, 48.88859533960799 76.62939224605093, 52.34633135269824 78.47759065022575, 56.098193559677476 79.61570560806462, 60.00000000000005 80, 63.901806440322616 79.6157056080646, 67.65366864730186 78.47759065022572, 71.1114046603921 76.62939224605087, 74.142135623731 74.14213562373091, 76.62939224605094 71.11140466039198, 78.47759065022576 67.65366864730173, 79.61570560806462 63.90180644032249, 80 60))", WKTDatatype.INSTANCE);
        NodeValue result = instance.exec(v1, v2, v3);
        assertEquals(expResult, result);
    }

    /**
     * Test of exec method, of class BufferFF.
     */
    @Test
    public void testExec_Projection_NonLinear() {
        System.out.println("exec_Projection_NonLinear");
        NodeValue v1 = NodeValue.makeNode("<http://www.opengis.net/def/crs/EPSG/0/27700> POINT(60 60)", WKTDatatype.INSTANCE);
        NodeValue v2 = NodeValue.makeDecimal(0.0002);
        NodeValue v3 = NodeValue.makeNode(NodeFactory.createURI(Unit_URI.DEGREE_URL));
        BufferFF instance = new BufferFF();
        NodeValue expResult = NodeValue.makeNode("<http://www.opengis.net/def/crs/EPSG/0/27700> POLYGON((74.39204761973815 58.93046388030052, 73.7935792641947 54.61624912638217, 72.66504878236447 50.508902633562684, 71.04982384038158 46.76626767590642, 69.00997585331788 43.53217199537903, 66.62389473285293 40.930900496430695, 63.98327645310201 39.062419002875686, 61.18959919176996 37.9985325448215, 58.35022347670747 37.78012590389699, 55.57426621468039 38.41559237986803, 52.96840719494503 39.880511234514415, 50.63278924539918 42.11858620401472, 48.65716962714214 45.04380894638598, 47.117470596858766 48.54376436397433, 46.07286171038868 52.48395072482526, 45.56348600285128 56.71294852718711, 45.60891742008971 61.06823955103755, 46.20740876765922 65.3824523454532, 47.335959049582016 69.48979424312711, 48.951197585556656 73.23242266196758, 50.99105089070508 76.4665108602494, 53.37712824344635 79.06777506601065, 56.01773424394196 80.93625058047473, 58.811392582952976 82.00013326201588, 61.650745614490006 82.2185389213264, 64.42667988454923 81.58307440485805, 67.03251910372637 80.11816014815122, 69.36812345974613 77.88009171932936, 71.34373776020948 74.95487646199763, 72.88344055786729 71.45492832921445, 73.92806172382552 67.51474795676768, 74.4374563532765 63.28575392253697, 74.39204761973815 58.93046388030052))", WKTDatatype.INSTANCE);
        NodeValue result = instance.exec(v1, v2, v3);
        assertEquals(expResult, result);
    }

    /**
     * Test of exec method, of class BufferFF.
     */
    @Test
    public void testExec_Geographic_Linear() {
        System.out.println("exec_Geographic_Linear");
        NodeValue v1 = NodeValue.makeNode("<http://www.opengis.net/def/crs/EPSG/0/4326> POLYGON((49.9 -7.5, 50.0 -7.5, 50.0 -7.4, 49.9 -7.4, 49.9 -7.5))", WKTDatatype.INSTANCE);
        NodeValue v2 = NodeValue.makeDecimal(20);
        NodeValue v3 = NodeValue.makeNode(NodeFactory.createURI(Unit_URI.METRE_URL));
        BufferFF instance = new BufferFF();
        NodeValue expResult = NodeValue.makeNode("<http://www.opengis.net/def/crs/EPSG/0/4326> POLYGON((49.899820141481335 -7.499999814065095, 49.89982357682339 -7.50005416286721, 49.89983379760518 -7.500106428518575, 49.899850410726465 -7.5001546008335165, 49.8998727772313 -7.500196827054945, 49.899900036882244 -7.500231483111882, 49.89993114124498 -7.500257236082699, 49.89996489401149 -7.5002730954617265, 49.89999999701054 -7.500278451257391, 50.00000000229664 -7.500279028565501, 50.00003507526559 -7.500273671158249, 50.000068801491004 -7.500257805818185, 50.00009988601251 -7.500232041693716, 50.000127135299024 -7.500197368014575, 50.000149503076514 -7.500155116111152, 50.000166130501974 -7.500106908297218, 50.000176379140875 -7.50005459557864, 50.00017985548134 -7.500000186579973, 50.000179851963594 -7.399999813406841, 50.00017637555365 -7.3999454044350905, 50.00016612672241 -7.399893091825297, 50.00014949900459 -7.399844884274894, 50.000127130870865 -7.399802632850422, 50.000099881211085 -7.399767959909134, 50.00006879635382 -7.399742196803749, 50.00003506988892 -7.399726332762439, 49.999999996835406 -7.399720976906853, 49.90000000247568 -7.399721554226337, 49.89996489939464 -7.39972690847078, 49.89993114639072 -7.399742766550671, 49.899900041693975 -7.399768518501266, 49.899872781671114 -7.399803173818511, 49.89985041481116 -7.399845399558502, 49.8998338013984 -7.399893571606822, 49.899823580424936 -7.399945837145772, 49.89982014501383 -7.400000185916873, 49.899820141481335 -7.499999814065095))", WKTDatatype.INSTANCE);
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
        System.out.println("exec_Geographic_Linear2");

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

        //System.out.println("Exp: " + Arrays.toString(expResults));
        //System.out.println("Res: " + Arrays.toString(results));
        //Test accuracy of buffering to within 0.1m. Some error from coordinate transformations.
        Assert.assertArrayEquals(expResults, results, 0.1);
    }

    /**
     * Test of exec method, of class BufferFF.
     */
    @Test
    public void testExec_Geographic_NonLinear() {
        System.out.println("exec_Geographic_NonLinear");
        NodeValue v1 = NodeValue.makeNode("<http://www.opengis.net/def/crs/EPSG/0/4326> POLYGON((49.9 -7.5, 50.0 -7.5, 50.0 -7.4, 49.9 -7.4, 49.9 -7.5))", WKTDatatype.INSTANCE);
        NodeValue v2 = NodeValue.makeDecimal(0.0002);
        NodeValue v3 = NodeValue.makeNode(NodeFactory.createURI(Unit_URI.DEGREE_URL));
        BufferFF instance = new BufferFF();
        NodeValue expResult = NodeValue.makeNode("<http://www.opengis.net/def/crs/EPSG/0/4326> POLYGON((49.8998 -7.5, 49.89980384294392 -7.500039018064403, 49.899815224093494 -7.500076536686473, 49.89983370607754 -7.500111114046604, 49.89985857864376 -7.500141421356237, 49.899888885953395 -7.500166293922461, 49.89992346331353 -7.500184775906503, 49.89996098193559 -7.500196157056081, 49.9 -7.5002, 50 -7.5002, 50.000039018064406 -7.500196157056081, 50.00007653668647 -7.500184775906503, 50.000111114046604 -7.500166293922461, 50.00014142135624 -7.500141421356237, 50.00016629392246 -7.500111114046604, 50.000184775906504 -7.500076536686473, 50.00019615705608 -7.500039018064403, 50.0002 -7.5, 50.0002 -7.4, 50.00019615705608 -7.399960981935597, 50.000184775906504 -7.399923463313527, 50.00016629392246 -7.399888885953397, 50.00014142135624 -7.399858578643763, 50.000111114046604 -7.39983370607754, 50.00007653668647 -7.399815224093498, 50.000039018064406 -7.3998038429439195, 50 -7.3998, 49.9 -7.3998, 49.89996098193559 -7.3998038429439195, 49.89992346331353 -7.399815224093498, 49.899888885953395 -7.39983370607754, 49.89985857864376 -7.399858578643763, 49.89983370607754 -7.399888885953397, 49.899815224093494 -7.399923463313527, 49.89980384294392 -7.399960981935597, 49.8998 -7.4, 49.8998 -7.5))", WKTDatatype.INSTANCE);
        NodeValue result = instance.exec(v1, v2, v3);
        assertEquals(expResult, result);
    }

}
