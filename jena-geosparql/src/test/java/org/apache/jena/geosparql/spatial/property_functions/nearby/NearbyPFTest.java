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
package org.apache.jena.geosparql.spatial.property_functions.nearby;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.apache.jena.geosparql.configuration.GeoSPARQLConfig;
import org.apache.jena.geosparql.implementation.GeometryWrapper;
import org.apache.jena.geosparql.implementation.vocabulary.SpatialExtension;
import org.apache.jena.geosparql.implementation.vocabulary.Unit_URI;
import org.apache.jena.geosparql.spatial.ConvertLatLon;
import org.apache.jena.geosparql.spatial.SearchEnvelope;
import org.apache.jena.geosparql.spatial.SpatialIndexTestData;
import org.apache.jena.geosparql.spatial.property_functions.SpatialArguments;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.query.Dataset;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QueryExecutionFactory;
import org.apache.jena.query.QuerySolution;
import org.apache.jena.query.ResultSet;
import org.apache.jena.rdf.model.Literal;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.sparql.expr.ExprEvalException;
import org.apache.jena.sparql.expr.NodeValue;
import org.apache.jena.sparql.pfunction.PropFuncArg;
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
public class NearbyPFTest {

    public NearbyPFTest() {
    }

    @BeforeClass
    public static void setUpClass() {
        GeoSPARQLConfig.setupNoIndex();
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
     * Test of extractObjectArguments method, of class NearbyPF.
     */
    @Test
    public void testExtractObjectArguments_5args() {

        Node predicate = NodeFactory.createURI(SpatialExtension.NEARBY_PROP);

        float lat = 0;
        float lon = 1;
        float radius = 5000;
        String unitsURI = Unit_URI.METRE_URL;
        int limit = 10;

        Literal geometry = ConvertLatLon.toLiteral(lat, lon);
        List<Node> objectNodes = Arrays.asList(NodeValue.makeFloat(lat).asNode(), NodeValue.makeFloat(lon).asNode(), NodeValue.makeFloat(radius).asNode(), NodeFactory.createURI(unitsURI), NodeValue.makeInteger(limit).asNode());
        PropFuncArg object = new PropFuncArg(objectNodes);

        GeometryWrapper geometryWrapper = GeometryWrapper.extract(geometry);
        SearchEnvelope searchEnvelope = SearchEnvelope.build(geometryWrapper, SpatialIndexTestData.WGS_84_SRS_INFO, radius, unitsURI);

        NearbyPF instance = new NearbyPF();
        SpatialArguments expResult = new SpatialArguments(limit, geometryWrapper, searchEnvelope);
        SpatialArguments result = instance.extractObjectArguments(predicate, object, SpatialIndexTestData.WGS_84_SRS_INFO);
        assertEquals(expResult, result);
    }

    /**
     * Test of extractObjectArguments method, of class NearbyPF.
     */
    @Test
    public void testExtractObjectArguments_4args() {

        Node predicate = NodeFactory.createURI(SpatialExtension.NEARBY_PROP);

        float lat = 0;
        float lon = 1;
        float radius = 5000;
        String unitsURI = Unit_URI.METRE_URL;
        int limit = -1;

        Literal geometry = ConvertLatLon.toLiteral(lat, lon);
        List<Node> objectNodes = Arrays.asList(NodeValue.makeFloat(lat).asNode(), NodeValue.makeFloat(lon).asNode(), NodeValue.makeFloat(radius).asNode(), NodeFactory.createURI(unitsURI));
        PropFuncArg object = new PropFuncArg(objectNodes);

        GeometryWrapper geometryWrapper = GeometryWrapper.extract(geometry);
        SearchEnvelope searchEnvelope = SearchEnvelope.build(geometryWrapper, SpatialIndexTestData.WGS_84_SRS_INFO, radius, unitsURI);

        NearbyPF instance = new NearbyPF();
        SpatialArguments expResult = new SpatialArguments(limit, geometryWrapper, searchEnvelope);
        SpatialArguments result = instance.extractObjectArguments(predicate, object, SpatialIndexTestData.WGS_84_SRS_INFO);
        assertEquals(expResult, result);
    }

    /**
     * Test of extractObjectArguments method, of class NearbyPF.
     */
    @Test
    public void testExtractObjectArguments_3args() {

        Node predicate = NodeFactory.createURI(SpatialExtension.NEARBY_PROP);

        float lat = 0;
        float lon = 1;
        float radius = 5;
        String unitsURI = Unit_URI.KILOMETER_URL;
        int limit = -1;

        Literal geometry = ConvertLatLon.toLiteral(lat, lon);
        List<Node> objectNodes = Arrays.asList(NodeValue.makeFloat(lat).asNode(), NodeValue.makeFloat(lon).asNode(), NodeValue.makeFloat(radius).asNode());
        PropFuncArg object = new PropFuncArg(objectNodes);

        GeometryWrapper geometryWrapper = GeometryWrapper.extract(geometry);
        SearchEnvelope searchEnvelope = SearchEnvelope.build(geometryWrapper, SpatialIndexTestData.WGS_84_SRS_INFO, radius, unitsURI);

        NearbyPF instance = new NearbyPF();
        SpatialArguments expResult = new SpatialArguments(limit, geometryWrapper, searchEnvelope);
        SpatialArguments result = instance.extractObjectArguments(predicate, object, SpatialIndexTestData.WGS_84_SRS_INFO);
        assertEquals(expResult, result);
    }

    /**
     * Test of extractObjectArguments method, of class NearbyPF.
     */
    @Test(expected = ExprEvalException.class)
    public void testExtractObjectArguments_2args_fail() {

        Node predicate = NodeFactory.createURI(SpatialExtension.NEARBY_PROP);

        float lat = 0;
        float lon = 1;
        float radius = 5;
        String unitsURI = Unit_URI.KILOMETER_URL;
        int limit = -1;

        Literal geometry = ConvertLatLon.toLiteral(lat, lon);
        List<Node> objectNodes = Arrays.asList(NodeValue.makeFloat(lat).asNode(), NodeValue.makeFloat(lon).asNode());
        PropFuncArg object = new PropFuncArg(objectNodes);

        GeometryWrapper geometryWrapper = GeometryWrapper.extract(geometry);
        SearchEnvelope searchEnvelope = SearchEnvelope.build(geometryWrapper, SpatialIndexTestData.WGS_84_SRS_INFO, radius, unitsURI);

        NearbyPF instance = new NearbyPF();
        SpatialArguments expResult = new SpatialArguments(limit, geometryWrapper, searchEnvelope);
        SpatialArguments result = instance.extractObjectArguments(predicate, object, SpatialIndexTestData.WGS_84_SRS_INFO);
        //assertEquals(expResult, result);
    }

    /**
     * Test of extractObjectArguments method, of class NearbyPF.
     */
    @Test(expected = ExprEvalException.class)
    public void testExtractObjectArguments_6args_fail() {

        Node predicate = NodeFactory.createURI(SpatialExtension.NEARBY_PROP);

        float lat = 0;
        float lon = 1;
        float radius = 5000;
        String unitsURI = Unit_URI.METRE_URL;
        int limit = 10;

        Literal geometry = ConvertLatLon.toLiteral(lat, lon);
        List<Node> objectNodes = Arrays.asList(NodeValue.makeFloat(lat).asNode(), NodeValue.makeFloat(lon).asNode(), NodeValue.makeFloat(radius).asNode(), NodeFactory.createURI(unitsURI), NodeValue.makeInteger(limit).asNode(), NodeValue.makeBoolean(false).asNode());
        PropFuncArg object = new PropFuncArg(objectNodes);

        GeometryWrapper geometryWrapper = GeometryWrapper.extract(geometry);
        SearchEnvelope searchEnvelope = SearchEnvelope.build(geometryWrapper, SpatialIndexTestData.WGS_84_SRS_INFO, radius, unitsURI);

        NearbyPF instance = new NearbyPF();
        SpatialArguments expResult = new SpatialArguments(limit, geometryWrapper, searchEnvelope);
        SpatialArguments result = instance.extractObjectArguments(predicate, object, SpatialIndexTestData.WGS_84_SRS_INFO);
        //assertEquals(expResult, result);
    }

    /**
     * Test of extractObjectArguments method, of class NearbyPF.
     */
    @Test(expected = ExprEvalException.class)
    public void testExtractObjectArguments_5args_pos0_fail() {

        Node predicate = NodeFactory.createURI(SpatialExtension.NEARBY_PROP);

        float lat = 0;
        float lon = 1;
        float radius = 5000;
        String unitsURI = Unit_URI.METRE_URL;
        int limit = 10;

        Literal geometry = ConvertLatLon.toLiteral(lat, lon);
        List<Node> objectNodes = Arrays.asList(NodeValue.makeString("e").asNode(), NodeValue.makeFloat(lon).asNode(), NodeValue.makeFloat(radius).asNode(), NodeFactory.createURI(unitsURI), NodeValue.makeInteger(limit).asNode());
        PropFuncArg object = new PropFuncArg(objectNodes);

        GeometryWrapper geometryWrapper = GeometryWrapper.extract(geometry);
        SearchEnvelope searchEnvelope = SearchEnvelope.build(geometryWrapper, SpatialIndexTestData.WGS_84_SRS_INFO, radius, unitsURI);

        NearbyPF instance = new NearbyPF();
        SpatialArguments expResult = new SpatialArguments(limit, geometryWrapper, searchEnvelope);
        SpatialArguments result = instance.extractObjectArguments(predicate, object, SpatialIndexTestData.WGS_84_SRS_INFO);
        //assertEquals(expResult, result);
    }

    /**
     * Test of extractObjectArguments method, of class NearbyPF.
     */
    @Test(expected = ExprEvalException.class)
    public void testExtractObjectArguments_5args_pos1_fail() {

        Node predicate = NodeFactory.createURI(SpatialExtension.NEARBY_PROP);

        float lat = 0;
        float lon = 1;
        float radius = 5000;
        String unitsURI = Unit_URI.METRE_URL;
        int limit = 10;

        Literal geometry = ConvertLatLon.toLiteral(lat, lon);
        List<Node> objectNodes = Arrays.asList(NodeValue.makeFloat(lat).asNode(), NodeValue.makeString("e").asNode(), NodeValue.makeFloat(radius).asNode(), NodeFactory.createURI(unitsURI), NodeValue.makeInteger(limit).asNode());
        PropFuncArg object = new PropFuncArg(objectNodes);

        GeometryWrapper geometryWrapper = GeometryWrapper.extract(geometry);
        SearchEnvelope searchEnvelope = SearchEnvelope.build(geometryWrapper, SpatialIndexTestData.WGS_84_SRS_INFO, radius, unitsURI);

        NearbyPF instance = new NearbyPF();
        SpatialArguments expResult = new SpatialArguments(limit, geometryWrapper, searchEnvelope);
        SpatialArguments result = instance.extractObjectArguments(predicate, object, SpatialIndexTestData.WGS_84_SRS_INFO);
        //assertEquals(expResult, result);
    }

    /**
     * Test of extractObjectArguments method, of class NearbyPF.
     */
    @Test(expected = ExprEvalException.class)
    public void testExtractObjectArguments_5args_pos2_fail() {

        Node predicate = NodeFactory.createURI(SpatialExtension.NEARBY_PROP);

        float lat = 0;
        float lon = 1;
        float radius = 5000;
        String unitsURI = Unit_URI.METRE_URL;
        int limit = 10;

        Literal geometry = ConvertLatLon.toLiteral(lat, lon);
        List<Node> objectNodes = Arrays.asList(NodeValue.makeFloat(lat).asNode(), NodeValue.makeFloat(lon).asNode(), NodeValue.makeString("5000").asNode(), NodeFactory.createURI(unitsURI), NodeValue.makeInteger(limit).asNode());
        PropFuncArg object = new PropFuncArg(objectNodes);

        GeometryWrapper geometryWrapper = GeometryWrapper.extract(geometry);
        SearchEnvelope searchEnvelope = SearchEnvelope.build(geometryWrapper, SpatialIndexTestData.WGS_84_SRS_INFO, radius, unitsURI);

        NearbyPF instance = new NearbyPF();
        SpatialArguments expResult = new SpatialArguments(limit, geometryWrapper, searchEnvelope);
        SpatialArguments result = instance.extractObjectArguments(predicate, object, SpatialIndexTestData.WGS_84_SRS_INFO);
        //assertEquals(expResult, result);
    }

    /**
     * Test of extractObjectArguments method, of class NearbyPF.
     */
    @Test(expected = ExprEvalException.class)
    public void testExtractObjectArguments_5args_pos3_fail() {

        Node predicate = NodeFactory.createURI(SpatialExtension.NEARBY_PROP);

        float lat = 0;
        float lon = 1;
        float radius = 5000;
        String unitsURI = Unit_URI.METRE_URL;
        int limit = 10;

        Literal geometry = ConvertLatLon.toLiteral(lat, lon);
        List<Node> objectNodes = Arrays.asList(NodeValue.makeFloat(lat).asNode(), NodeValue.makeFloat(lon).asNode(), NodeValue.makeFloat(radius).asNode(), NodeValue.makeString(unitsURI).asNode(), NodeValue.makeInteger(limit).asNode());
        PropFuncArg object = new PropFuncArg(objectNodes);

        GeometryWrapper geometryWrapper = GeometryWrapper.extract(geometry);
        SearchEnvelope searchEnvelope = SearchEnvelope.build(geometryWrapper, SpatialIndexTestData.WGS_84_SRS_INFO, radius, unitsURI);

        NearbyPF instance = new NearbyPF();
        SpatialArguments expResult = new SpatialArguments(limit, geometryWrapper, searchEnvelope);
        SpatialArguments result = instance.extractObjectArguments(predicate, object, SpatialIndexTestData.WGS_84_SRS_INFO);
        //assertEquals(expResult, result);
    }

    /**
     * Test of extractObjectArguments method, of class NearbyPF.
     */
    @Test(expected = ExprEvalException.class)
    public void testExtractObjectArguments_5args_pos4_fail() {

        Node predicate = NodeFactory.createURI(SpatialExtension.NEARBY_PROP);

        float lat = 0;
        float lon = 1;
        float radius = 5000;
        String unitsURI = Unit_URI.METRE_URL;
        int limit = 10;

        Literal geometry = ConvertLatLon.toLiteral(lat, lon);
        List<Node> objectNodes = Arrays.asList(NodeValue.makeFloat(lat).asNode(), NodeValue.makeFloat(lon).asNode(), NodeValue.makeFloat(radius).asNode(), NodeFactory.createURI(unitsURI), NodeValue.makeString("10").asNode());
        PropFuncArg object = new PropFuncArg(objectNodes);

        GeometryWrapper geometryWrapper = GeometryWrapper.extract(geometry);
        SearchEnvelope searchEnvelope = SearchEnvelope.build(geometryWrapper, SpatialIndexTestData.WGS_84_SRS_INFO, radius, unitsURI);

        NearbyPF instance = new NearbyPF();
        SpatialArguments expResult = new SpatialArguments(limit, geometryWrapper, searchEnvelope);
        SpatialArguments result = instance.extractObjectArguments(predicate, object, SpatialIndexTestData.WGS_84_SRS_INFO);
        //assertEquals(expResult, result);
    }

    /**
     * Test of execEvaluated method, of class NearbyPF.
     */
    @Test
    public void testExecEvaluated() {


        Dataset dataset = SpatialIndexTestData.createTestDataset();

        String query = "PREFIX spatial: <http://jena.apache.org/spatial#>\n"
                + "\n"
                + "SELECT ?subj\n"
                + "WHERE{\n"
                + "    ?subj spatial:nearby(48.857487 2.373047 350) .\n"
                + "}ORDER by ?subj";

        List<Resource> result = new ArrayList<>();
        try (QueryExecution qe = QueryExecutionFactory.create(query, dataset)) {
            ResultSet rs = qe.execSelect();
            while (rs.hasNext()) {
                QuerySolution qs = rs.nextSolution();
                Resource feature = qs.getResource("subj");
                result.add(feature);
            }
        }

        List<Resource> expResult = Arrays.asList(SpatialIndexTestData.LONDON_FEATURE);
        assertEquals(expResult, result);
    }

    /**
     * Test of execEvaluated method, of class NearbyPF.<br>
     * Close enough for first filter but rejected by second filter.
     */
    @Test
    public void testExecEvaluated_fail() {


        Dataset dataset = SpatialIndexTestData.createTestDataset();

        String query = "PREFIX spatial: <http://jena.apache.org/spatial#>\n"
                + "\n"
                + "SELECT ?subj\n"
                + "WHERE{\n"
                + "    ?subj spatial:nearby(48.857487 2.373047 340) .\n"
                + "}ORDER by ?subj";

        List<Resource> result = new ArrayList<>();
        try (QueryExecution qe = QueryExecutionFactory.create(query, dataset)) {
            ResultSet rs = qe.execSelect();
            while (rs.hasNext()) {
                QuerySolution qs = rs.nextSolution();
                Resource feature = qs.getResource("subj");
                result.add(feature);
            }
        }

        List<Resource> expResult = new ArrayList<>();
        assertEquals(expResult, result);
    }
}
