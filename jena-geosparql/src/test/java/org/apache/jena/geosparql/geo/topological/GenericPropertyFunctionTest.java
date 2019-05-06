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
package org.apache.jena.geosparql.geo.topological;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.apache.jena.geosparql.configuration.GeoSPARQLConfig;
import static org.apache.jena.geosparql.geo.topological.QueryRewriteTestData.FEATURE_A;
import static org.apache.jena.geosparql.geo.topological.QueryRewriteTestData.FEATURE_B;
import static org.apache.jena.geosparql.geo.topological.QueryRewriteTestData.FEATURE_D;
import static org.apache.jena.geosparql.geo.topological.QueryRewriteTestData.GEOMETRY_A;
import static org.apache.jena.geosparql.geo.topological.QueryRewriteTestData.GEOMETRY_B;
import static org.apache.jena.geosparql.geo.topological.QueryRewriteTestData.GEOMETRY_C_BLANK;
import static org.apache.jena.geosparql.geo.topological.QueryRewriteTestData.GEOMETRY_D;
import static org.apache.jena.geosparql.geo.topological.QueryRewriteTestData.GEOMETRY_F;
import static org.apache.jena.geosparql.geo.topological.QueryRewriteTestData.GEO_FEATURE_Y;
import static org.apache.jena.geosparql.geo.topological.QueryRewriteTestData.GEO_FEATURE_Z;
import static org.apache.jena.geosparql.geo.topological.QueryRewriteTestData.TEST_SRS_URI;
import org.apache.jena.geosparql.geo.topological.property_functions.simple_features.SfContainsPF;
import org.apache.jena.geosparql.geo.topological.property_functions.simple_features.SfDisjointPF;
import org.apache.jena.geosparql.implementation.index.IndexConfiguration.IndexOption;
import org.apache.jena.geosparql.implementation.index.QueryRewriteIndex;
import org.apache.jena.geosparql.implementation.vocabulary.Geo;
import org.apache.jena.geosparql.spatial.SpatialIndex;
import org.apache.jena.geosparql.spatial.SpatialIndexException;
import org.apache.jena.graph.BlankNodeId;
import org.apache.jena.graph.Graph;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.query.Dataset;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QueryExecutionFactory;
import org.apache.jena.query.QuerySolution;
import org.apache.jena.query.ResultSet;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.vocabulary.RDF;
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
public class GenericPropertyFunctionTest {

    private static Model model;
    private static Dataset dataset;

    public GenericPropertyFunctionTest() {
    }

    @BeforeClass
    public static void setUpClass() throws SpatialIndexException {
        GeoSPARQLConfig.setup(IndexOption.MEMORY, Boolean.TRUE);
        model = QueryRewriteTestData.createTestData();
        dataset = SpatialIndex.wrapModel(model, TEST_SRS_URI);
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
     * Test of queryRewrite method, of class GenericPropertyFunction.
     */
    @Test
    public void testQueryRewrite_geometry_geometry() {
        System.out.println("queryRewrite_geometry_geometry");
        Graph graph = model.getGraph();
        Node subject = GEOMETRY_A.asNode();
        Node predicate = Geo.SF_CONTAINS_NODE;
        Node object = GEOMETRY_B.asNode();
        GenericPropertyFunction instance = new SfContainsPF();
        QueryRewriteIndex queryRewriteIndex = QueryRewriteIndex.createDefault();
        Boolean expResult = true;
        Boolean result = instance.queryRewrite(graph, subject, predicate, object, queryRewriteIndex);

        //System.out.println("Exp: " + expResult);
        //System.out.println("Res: " + result);
        assertEquals(expResult, result);
    }

    /**
     * Test of queryRewrite method, of class GenericPropertyFunction.
     */
    @Test
    public void testQueryRewrite_geometry_geometry_blank() {
        System.out.println("queryRewrite_geometry_geometry_blank");
        Graph graph = model.getGraph();
        Node subject = GEOMETRY_A.asNode();
        Node predicate = Geo.SF_CONTAINS_NODE;
        Node object = GEOMETRY_C_BLANK.asNode();
        GenericPropertyFunction instance = new SfContainsPF();
        QueryRewriteIndex queryRewriteIndex = QueryRewriteIndex.createDefault();
        Boolean expResult = true;
        Boolean result = instance.queryRewrite(graph, subject, predicate, object, queryRewriteIndex);

        //System.out.println("Exp: " + expResult);
        //System.out.println("Res: " + result);
        assertEquals(expResult, result);
    }

    /**
     * Test of queryRewrite method, of class GenericPropertyFunction.
     */
    @Test
    public void testQueryRewrite_blank() {
        System.out.println("queryRewrite_blank");

        Graph graph = model.getGraph();

        Boolean expResult = true;
        BlankNodeId id = GEOMETRY_C_BLANK.asNode().getBlankNodeId();
        Node node = NodeFactory.createBlankNode(id);

        Boolean result = graph.contains(node, RDF.type.asNode(), Geo.GEOMETRY_NODE);

        //System.out.println("Exp: " + expResult);
        //System.out.println("Res: " + result);
        assertEquals(expResult, result);
    }

    /**
     * Test of queryRewrite method, of class GenericPropertyFunction.
     */
    @Test
    public void testQueryRewrite_geometry_geometry_disabled() {
        System.out.println("queryRewrite_geometry_geometry_disabled");
        GeoSPARQLConfig.setup(IndexOption.MEMORY, Boolean.FALSE);
        Graph graph = model.getGraph();
        Node subject = GEOMETRY_A.asNode();
        Node predicate = Geo.SF_CONTAINS_NODE;
        Node object = FEATURE_B.asNode();
        GenericPropertyFunction instance = new SfContainsPF();
        QueryRewriteIndex queryRewriteIndex = QueryRewriteIndex.createDefault();
        Boolean expResult = false;
        Boolean result = instance.queryRewrite(graph, subject, predicate, object, queryRewriteIndex);

        GeoSPARQLConfig.setup(IndexOption.MEMORY, Boolean.TRUE);
        //System.out.println("Exp: " + expResult);
        //System.out.println("Res: " + result);
        assertEquals(expResult, result);
    }

    /**
     * Test of queryRewrite method, of class GenericPropertyFunction.
     */
    @Test
    public void testQueryRewrite_feature_geometry() {
        System.out.println("queryRewrite_feature_geometry");
        Graph graph = model.getGraph();
        Node subject = FEATURE_A.asNode();
        Node predicate = Geo.SF_CONTAINS_NODE;
        Node object = GEOMETRY_B.asNode();
        GenericPropertyFunction instance = new SfContainsPF();
        QueryRewriteIndex queryRewriteIndex = QueryRewriteIndex.createDefault();
        Boolean expResult = true;
        Boolean result = instance.queryRewrite(graph, subject, predicate, object, queryRewriteIndex);

        //System.out.println("Exp: " + expResult);
        //System.out.println("Res: " + result);
        assertEquals(expResult, result);
    }

    /**
     * Test of queryRewrite method, of class GenericPropertyFunction.
     */
    @Test
    public void testQueryRewrite_feature_feature() {
        System.out.println("queryRewrite_feature_feature");
        Graph graph = model.getGraph();
        Node subject = FEATURE_A.asNode();
        Node predicate = Geo.SF_CONTAINS_NODE;
        Node object = FEATURE_B.asNode();
        GenericPropertyFunction instance = new SfContainsPF();
        QueryRewriteIndex queryRewriteIndex = QueryRewriteIndex.createDefault();
        Boolean expResult = true;
        Boolean result = instance.queryRewrite(graph, subject, predicate, object, queryRewriteIndex);

        //System.out.println("Exp: " + expResult);
        //System.out.println("Res: " + result);
        assertEquals(expResult, result);
    }

    /**
     * Test of queryRewrite method, of class GenericPropertyFunction.
     */
    @Test
    public void testQueryRewrite_geometry_feature() {
        System.out.println("queryRewrite_geometry_feature");
        Graph graph = model.getGraph();
        Node subject = GEOMETRY_A.asNode();
        Node predicate = Geo.SF_CONTAINS_NODE;
        Node object = FEATURE_B.asNode();
        GenericPropertyFunction instance = new SfContainsPF();
        QueryRewriteIndex queryRewriteIndex = QueryRewriteIndex.createDefault();
        Boolean expResult = true;
        Boolean result = instance.queryRewrite(graph, subject, predicate, object, queryRewriteIndex);

        //System.out.println("Exp: " + expResult);
        //System.out.println("Res: " + result);
        assertEquals(expResult, result);
    }

    /**
     * Test of queryRewrite method, of class GenericPropertyFunction.
     */
    @Test
    public void testQueryRewrite_geometry_feature_disabled() {
        System.out.println("queryRewrite_geometry_feature_disabled");
        GeoSPARQLConfig.setup(IndexOption.MEMORY, Boolean.FALSE);
        Graph graph = model.getGraph();
        Node subject = GEOMETRY_A.asNode();
        Node predicate = Geo.SF_CONTAINS_NODE;
        Node object = FEATURE_B.asNode();
        GenericPropertyFunction instance = new SfContainsPF();
        QueryRewriteIndex queryRewriteIndex = QueryRewriteIndex.createDefault();
        Boolean expResult = false;
        Boolean result = instance.queryRewrite(graph, subject, predicate, object, queryRewriteIndex);

        GeoSPARQLConfig.setup(IndexOption.MEMORY, Boolean.TRUE);
        //System.out.println("Exp: " + expResult);
        //System.out.println("Res: " + result);
        assertEquals(expResult, result);
    }

    /**
     * Test of queryRewrite method, of class GenericPropertyFunction.
     */
    @Test
    public void testQueryRewrite_geometry_geometry_false() {
        System.out.println("queryRewrite_geometry_geometry_false");
        Graph graph = model.getGraph();
        Node subject = GEOMETRY_A.asNode();
        Node predicate = Geo.SF_CONTAINS_NODE;
        Node object = GEOMETRY_D.asNode();
        GenericPropertyFunction instance = new SfContainsPF();
        QueryRewriteIndex queryRewriteIndex = QueryRewriteIndex.createDefault();
        Boolean expResult = false;
        Boolean result = instance.queryRewrite(graph, subject, predicate, object, queryRewriteIndex);

        //System.out.println("Exp: " + expResult);
        //System.out.println("Res: " + result);
        assertEquals(expResult, result);
    }

    /**
     * Test of queryRewrite method, of class GenericPropertyFunction.
     */
    @Test
    public void testQueryRewrite_feature_geometry_false() {
        System.out.println("queryRewrite_feature_geometry_false");
        Graph graph = model.getGraph();
        Node subject = FEATURE_A.asNode();
        Node predicate = Geo.SF_CONTAINS_NODE;
        Node object = GEOMETRY_D.asNode();
        GenericPropertyFunction instance = new SfContainsPF();
        QueryRewriteIndex queryRewriteIndex = QueryRewriteIndex.createDefault();
        Boolean expResult = false;
        Boolean result = instance.queryRewrite(graph, subject, predicate, object, queryRewriteIndex);

        //System.out.println("Exp: " + expResult);
        //System.out.println("Res: " + result);
        assertEquals(expResult, result);
    }

    /**
     * Test of queryRewrite method, of class GenericPropertyFunction.
     */
    @Test
    public void testQueryRewrite_feature_feature_false() {
        System.out.println("queryRewrite_feature_feature_false");
        Graph graph = model.getGraph();
        Node subject = FEATURE_A.asNode();
        Node predicate = Geo.SF_CONTAINS_NODE;
        Node object = FEATURE_D.asNode();
        GenericPropertyFunction instance = new SfContainsPF();
        QueryRewriteIndex queryRewriteIndex = QueryRewriteIndex.createDefault();
        Boolean expResult = false;
        Boolean result = instance.queryRewrite(graph, subject, predicate, object, queryRewriteIndex);

        //System.out.println("Exp: " + expResult);
        //System.out.println("Res: " + result);
        assertEquals(expResult, result);
    }

    /**
     * Test of queryRewrite method, of class GenericPropertyFunction.
     */
    @Test
    public void testQueryRewrite_geometry_feature_false() {
        System.out.println("queryRewrite_geometry_feature_false");
        Graph graph = model.getGraph();
        Node subject = GEOMETRY_A.asNode();
        Node predicate = Geo.SF_CONTAINS_NODE;
        Node object = FEATURE_D.asNode();
        GenericPropertyFunction instance = new SfContainsPF();
        QueryRewriteIndex queryRewriteIndex = QueryRewriteIndex.createDefault();
        Boolean expResult = false;
        Boolean result = instance.queryRewrite(graph, subject, predicate, object, queryRewriteIndex);

        //System.out.println("Exp: " + expResult);
        //System.out.println("Res: " + result);
        assertEquals(expResult, result);
    }

    /**
     * Test of queryRewrite method, of class GenericPropertyFunction.
     */
    @Test
    public void testQueryRewrite_geometry_geometry_asserted() {
        System.out.println("queryRewrite_geometry__geometry_asserted");

        Graph graph = model.getGraph();
        Node subject = GEOMETRY_A.asNode();
        Node predicate = Geo.SF_CONTAINS_NODE;
        Node object = GEOMETRY_F.asNode();
        GenericPropertyFunction instance = new SfContainsPF();
        QueryRewriteIndex queryRewriteIndex = QueryRewriteIndex.createDefault();
        Boolean expResult = true;
        Boolean result = instance.queryRewrite(graph, subject, predicate, object, queryRewriteIndex);

        //System.out.println("Exp: " + expResult);
        //System.out.println("Res: " + result);
        assertEquals(expResult, result);
    }

    /**
     * Test of queryRewrite method, of class GenericPropertyFunction.
     */
    @Test
    public void testQueryRewrite_geometry_geometry_asserted_disabled() {
        System.out.println("queryRewrite_geometry__geometry_asserted_disabled");
        GeoSPARQLConfig.setup(IndexOption.MEMORY, Boolean.FALSE);
        Graph graph = model.getGraph();
        Node subject = GEOMETRY_A.asNode();
        Node predicate = Geo.SF_CONTAINS_NODE;
        Node object = GEOMETRY_F.asNode();
        GenericPropertyFunction instance = new SfContainsPF();
        QueryRewriteIndex queryRewriteIndex = QueryRewriteIndex.createDefault();
        Boolean expResult = true;
        Boolean result = instance.queryRewrite(graph, subject, predicate, object, queryRewriteIndex);

        GeoSPARQLConfig.setup(IndexOption.MEMORY, Boolean.TRUE);
        //System.out.println("Exp: " + expResult);
        //System.out.println("Res: " + result);
        assertEquals(expResult, result);
    }

    /**
     * Test of queryRewrite method, of class GenericPropertyFunction.
     */
    @Test
    public void testQueryRewrite_feature_feature_disjoint() {
        System.out.println("queryRewrite_feature_feature_disjoint");
        Graph graph = model.getGraph();
        Node subject = FEATURE_A.asNode();
        Node predicate = Geo.SF_DISJOINT_NODE;
        Node object = FEATURE_D.asNode();
        GenericPropertyFunction instance = new SfDisjointPF();
        QueryRewriteIndex queryRewriteIndex = QueryRewriteIndex.createDefault();
        Boolean expResult = true;
        Boolean result = instance.queryRewrite(graph, subject, predicate, object, queryRewriteIndex);

        //System.out.println("Exp: " + expResult);
        //System.out.println("Res: " + result);
        assertEquals(expResult, result);
    }

    /**
     * Test of queryRewrite method, of class GenericPropertyFunction.
     */
    @Test
    public void testQueryRewrite_feature_feature_disjoint_false() {
        System.out.println("queryRewrite_feature_feature_disjoint_false");
        Graph graph = model.getGraph();
        Node subject = FEATURE_A.asNode();
        Node predicate = Geo.SF_DISJOINT_NODE;
        Node object = FEATURE_B.asNode();
        GenericPropertyFunction instance = new SfDisjointPF();
        QueryRewriteIndex queryRewriteIndex = QueryRewriteIndex.createDefault();
        Boolean expResult = false;
        Boolean result = instance.queryRewrite(graph, subject, predicate, object, queryRewriteIndex);

        //System.out.println("Exp: " + expResult);
        //System.out.println("Res: " + result);
        assertEquals(expResult, result);
    }

    /**
     * Test of execEvaluated method, of class GenericPropertyFunction.
     */
    @Test
    public void testExecEvaluated_unbound() {
        System.out.println("execEvaluated_unbound");

        String query = "PREFIX geo: <http://www.opengis.net/ont/geosparql#>\n"
                + "\n"
                + "SELECT ?subj ?obj\n"
                + "WHERE{\n"
                + "    ?subj geo:sfContains ?obj .\n"
                + "}ORDER by ?subj ?obj";

        List<Resource> subjects = new ArrayList<>();
        List<Resource> objects = new ArrayList<>();
        try (QueryExecution qe = QueryExecutionFactory.create(query, dataset)) {
            ResultSet rs = qe.execSelect();
            while (rs.hasNext()) {
                QuerySolution qs = rs.nextSolution();
                Resource subject = qs.getResource("subj");
                subjects.add(subject);

                Resource object = qs.getResource("obj");
                objects.add(object);
            }
        }

        //Blank nodes limit a value check.
        boolean expResult = true;
        boolean result = subjects.size() == 25 && objects.size() == 25;

        //System.out.println("Exp: " + expResult);
        //System.out.println("Res: " + result);
        //System.out.println("Subjects: " + subjects);
        //System.out.println("Objects: " + objects);
        assertEquals(expResult, result);
    }

    /**
     * Test of execEvaluated method, of class GenericPropertyFunction.
     */
    @Test
    public void testExecEvaluated_subject_bound() {
        System.out.println("execEvaluated_subject_bound");

        String query = "PREFIX geo: <http://www.opengis.net/ont/geosparql#>\n"
                + "\n"
                + "SELECT ?obj\n"
                + "WHERE{\n"
                + "    BIND(<http://example.org#FeatureA> AS ?subj) \n"
                + "    ?subj geo:sfContains ?obj .\n"
                + "}ORDER by ?obj";

        List<Resource> objects = new ArrayList<>();
        try (QueryExecution qe = QueryExecutionFactory.create(query, dataset)) {
            ResultSet rs = qe.execSelect();
            while (rs.hasNext()) {
                QuerySolution qs = rs.nextSolution();

                Resource result = qs.getResource("obj");
                objects.add(result);
            }
        }

        //Blank nodes limit a value check.
        int expResult = 6;
        int result = objects.size();

        //System.out.println("Exp: " + expResult);
        //System.out.println("Res: " + result);
        //System.out.println("Objects: " + objects);
        assertEquals(expResult, result);
    }

    /**
     * Test of execEvaluated method, of class GenericPropertyFunction.
     */
    @Test
    public void testExecEvaluated_subject_bound_geometry() {
        System.out.println("execEvaluated_subject_bound");

        String query = "PREFIX geo: <http://www.opengis.net/ont/geosparql#>\n"
                + "\n"
                + "SELECT ?obj\n"
                + "WHERE{\n"
                + "    BIND(<http://example.org#GeometryA> AS ?subj) \n"
                + "    ?subj geo:sfContains ?obj .\n"
                + "}ORDER by ?obj";

        List<Resource> objects = new ArrayList<>();
        try (QueryExecution qe = QueryExecutionFactory.create(query, dataset)) {
            ResultSet rs = qe.execSelect();
            while (rs.hasNext()) {
                QuerySolution qs = rs.nextSolution();

                Resource result = qs.getResource("obj");
                objects.add(result);
            }
        }

//Blank nodes limit a value check.
        int expResult = 7;
        int result = objects.size();

        System.out.println("Exp: " + expResult);
        System.out.println("Res: " + result);
        System.out.println("Objects: " + objects);
        assertEquals(expResult, result);
    }

    /**
     * Test of execEvaluated method, of class GenericPropertyFunction.
     */
    @Test
    public void testExecEvaluated_object_bound() {
        System.out.println("execEvaluated_object_bound");

        String query = "PREFIX geo: <http://www.opengis.net/ont/geosparql#>\n"
                + "\n"
                + "SELECT ?subj\n"
                + "WHERE{\n"
                + "    BIND(<http://example.org#FeatureB> AS ?obj) \n"
                + "    ?subj geo:sfContains ?obj .\n"
                + "}ORDER by ?subj";

        List<Resource> results = new ArrayList<>();
        try (QueryExecution qe = QueryExecutionFactory.create(query, dataset)) {
            ResultSet rs = qe.execSelect();
            while (rs.hasNext()) {
                QuerySolution qs = rs.nextSolution();

                Resource result = qs.getResource("subj");
                results.add(result);
            }
        }

        List<Resource> expResults = Arrays.asList(FEATURE_A, FEATURE_B, GEOMETRY_A, GEOMETRY_B);

        //System.out.println("Exp: " + expResults);
        //System.out.println("Res: " + results);
        assertEquals(expResults, results);
    }

    /**
     * Test of execEvaluated method, of class GenericPropertyFunction.
     */
    @Test
    public void testExecEvaluated_both_bound() {
        System.out.println("execEvaluated_both_bound");

        String query = "PREFIX geo: <http://www.opengis.net/ont/geosparql#>\n"
                + "\n"
                + "SELECT ?subj ?obj\n"
                + "WHERE{\n"
                + "    BIND(<http://example.org#FeatureA> AS ?subj) \n"
                + "    BIND(<http://example.org#FeatureB> AS ?obj) \n"
                + "    ?subj geo:sfContains ?obj .\n"
                + "}ORDER by ?subj ?obj";

        List<Resource> subjects = new ArrayList<>();
        List<Resource> objects = new ArrayList<>();
        try (QueryExecution qe = QueryExecutionFactory.create(query, dataset)) {
            ResultSet rs = qe.execSelect();
            while (rs.hasNext()) {
                QuerySolution qs = rs.nextSolution();
                Resource subject = qs.getResource("subj");
                subjects.add(subject);

                Resource object = qs.getResource("obj");
                objects.add(object);
            }
        }

        boolean expResult = true;
        List<Resource> expSubjects = Arrays.asList(FEATURE_A);
        List<Resource> expObjects = Arrays.asList(FEATURE_B);
        boolean result = subjects.equals(expSubjects) && objects.equals(expObjects);

        //System.out.println("Exp: " + expResult);
        //System.out.println("Res: " + result);
        assertEquals(expResult, result);
    }

    /**
     * Test of execEvaluated method, of class GenericPropertyFunction.
     */
    @Test
    public void testExecEvaluated_both_bound_geo() {
        System.out.println("execEvaluated_both_bound_geo");

        String query = "PREFIX geo: <http://www.opengis.net/ont/geosparql#>\n"
                + "\n"
                + "SELECT ?subj ?obj\n"
                + "WHERE{\n"
                + "    BIND(<http://example.org#GeoFeatureY> AS ?subj) \n"
                + "    BIND(<http://example.org#GeoFeatureZ> AS ?obj) \n"
                + "    ?subj geo:sfContains ?obj .\n"
                + "}ORDER by ?subj ?obj";

        List<Resource> subjects = new ArrayList<>();
        List<Resource> objects = new ArrayList<>();
        try (QueryExecution qe = QueryExecutionFactory.create(query, dataset)) {
            ResultSet rs = qe.execSelect();
            while (rs.hasNext()) {
                QuerySolution qs = rs.nextSolution();
                Resource subject = qs.getResource("subj");
                subjects.add(subject);

                Resource object = qs.getResource("obj");
                objects.add(object);
            }
        }

        boolean expResult = true;
        List<Resource> expSubjects = Arrays.asList(GEO_FEATURE_Y);
        List<Resource> expObjects = Arrays.asList(GEO_FEATURE_Z);
        boolean result = subjects.equals(expSubjects) && objects.equals(expObjects);

        //System.out.println("Exp: " + expResult);
        //System.out.println("Res: " + result);
        assertEquals(expResult, result);
    }

}
