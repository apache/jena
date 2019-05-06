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
package org.apache.jena.geosparql.spatial.property_functions;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.apache.jena.datatypes.xsd.XSDDatatype;
import org.apache.jena.geosparql.configuration.GeoSPARQLConfig;
import org.apache.jena.geosparql.implementation.vocabulary.SpatialExtension;
import org.apache.jena.geosparql.spatial.SpatialIndex;
import org.apache.jena.geosparql.spatial.SpatialIndexException;
import org.apache.jena.geosparql.spatial.SpatialIndexTestData;
import org.apache.jena.query.Dataset;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QueryExecutionFactory;
import org.apache.jena.query.QuerySolution;
import org.apache.jena.query.ResultSet;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.ResourceFactory;
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
public class GenericSpatialPropertyFunctionTest {

    public GenericSpatialPropertyFunctionTest() {
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
     * Test of execEvaluated method, of class GenericSpatialPropertyFunction.
     */
    @Test
    public void testExecEvaluated_Nearby_none() {
        System.out.println("execEvaluated_Nearby_none");

        Dataset dataset = SpatialIndexTestData.createTestDataset();

        String query = "PREFIX spatial: <http://jena.apache.org/spatial#>\n"
                + "\n"
                + "SELECT ?subj\n"
                + "WHERE{\n"
                + "    ?subj spatial:nearby(48.857487 2.373047 200) .\n"
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

    /**
     * Test of execEvaluated method, of class GenericSpatialPropertyFunction.
     */
    @Test
    public void testExecEvaluated_Nearby_one() {
        System.out.println("execEvaluated_Nearby_one");

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
     * Test of execEvaluated method, of class GenericSpatialPropertyFunction.
     */
    @Test
    public void testExecEvaluated_Nearby_one_bound() {
        System.out.println("execEvaluated_Nearby_one_bound");

        Dataset dataset = SpatialIndexTestData.createTestDataset();

        String query = "PREFIX spatial: <http://jena.apache.org/spatial#>\n"
                + "\n"
                + "SELECT ?subj\n"
                + "WHERE{\n"
                + "    BIND(<http://example.org/Feature#London> AS ?subj) \n"
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
     * Test of execEvaluated method, of class GenericSpatialPropertyFunction.
     */
    @Test
    public void testExecEvaluated_Nearby_one_bound_fail() {
        System.out.println("execEvaluated_Nearby_one_bound_fail");

        Dataset dataset = SpatialIndexTestData.createTestDataset();

        String query = "PREFIX spatial: <http://jena.apache.org/spatial#>\n"
                + "\n"
                + "SELECT ?subj\n"
                + "WHERE{\n"
                + "    BIND(<http://example.org/Feature#NewYork> AS ?subj) \n"
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

        List<Resource> expResult = new ArrayList<>();
        assertEquals(expResult, result);
    }

    /**
     * Test of execEvaluated method, of class GenericSpatialPropertyFunction.
     */
    @Test
    public void testExecEvaluated_Nearby_geo() throws SpatialIndexException {
        System.out.println("execEvaluated_Nearby_geo");

        Model model = ModelFactory.createDefaultModel();
        Resource geoFeature = ResourceFactory.createResource("http://example.org/GeoFeatureX");
        model.add(geoFeature, SpatialExtension.GEO_LAT_PROP, ResourceFactory.createTypedLiteral("0.0", XSDDatatype.XSDfloat));
        model.add(geoFeature, SpatialExtension.GEO_LON_PROP, ResourceFactory.createTypedLiteral("0.0", XSDDatatype.XSDfloat));
        Dataset dataset = SpatialIndex.wrapModel(model);

        String query = "PREFIX spatial: <http://jena.apache.org/spatial#>\n"
                + "\n"
                + "SELECT ?subj\n"
                + "WHERE{\n"
                + "    ?subj spatial:nearby(0.0 0.0 10) .\n"
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

        List<Resource> expResult = Arrays.asList(geoFeature);
        assertEquals(expResult, result);
    }

}
