/*
 * Copyright 2019 the original author or authors.
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
package org.apache.jena.geosparql.spatial.property_functions.box;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.apache.jena.geosparql.configuration.GeoSPARQLConfig;
import org.apache.jena.geosparql.implementation.GeometryWrapper;
import org.apache.jena.geosparql.implementation.vocabulary.SpatialExtension;
import org.apache.jena.geosparql.spatial.ConvertLatLon;
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
import org.apache.jena.sparql.expr.NodeValue;
import org.apache.jena.sparql.pfunction.PropFuncArg;
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
public class WithinBoxPFTest {

    public WithinBoxPFTest() {
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
     * Test of checkSecondFilter method, of class WithinBoxPF.
     */
    @Test
    public void testCheckSecondFilter() {
        System.out.println("checkSecondFilter");

        WithinBoxPF instance = new WithinBoxPF();

        //Property Function
        Node predicate = NodeFactory.createURI(SpatialExtension.WITHIN_BOX_PROP);

        //Geometry and Envelope parameters
        float lat = 1;
        float lon = 1;
        float latMin = 0;
        float lonMin = 0;
        float latMax = 2;
        float lonMax = 2;

        Literal targetGeometry = ConvertLatLon.toLiteral(lat, lon);

        List<Node> objectNodes = Arrays.asList(NodeValue.makeFloat(latMin).asNode(), NodeValue.makeFloat(lonMin).asNode(), NodeValue.makeFloat(latMax).asNode(), NodeValue.makeFloat(lonMax).asNode());
        PropFuncArg object = new PropFuncArg(objectNodes);

        //Function arguments
        SpatialArguments spatialArguments = instance.extractObjectArguments(predicate, object, SpatialIndexTestData.WGS_84_SRS_INFO);
        GeometryWrapper targetGeometryWrapper = GeometryWrapper.extract(targetGeometry);

        //Test arguments
        boolean expResult = true;
        boolean result = instance.checkSecondFilter(spatialArguments, targetGeometryWrapper);

        //System.out.println("Exp: " + expResult);
        //System.out.println("Res: " + result);
        assertEquals(expResult, result);
    }

    /**
     * Test of checkSecondFilter method, of class WithinBoxPF.
     */
    @Test
    public void testCheckSecondFilter_fail() {
        System.out.println("checkSecondFilter_fail");

        WithinBoxPF instance = new WithinBoxPF();

        //Property Function
        Node predicate = NodeFactory.createURI(SpatialExtension.WITHIN_BOX_PROP);

        //Geometry and Envelope parameters
        float lat = 5;
        float lon = 5;
        float latMin = 0;
        float lonMin = 0;
        float latMax = 2;
        float lonMax = 2;

        Literal targetGeometry = ConvertLatLon.toLiteral(lat, lon);

        List<Node> objectNodes = Arrays.asList(NodeValue.makeFloat(latMin).asNode(), NodeValue.makeFloat(lonMin).asNode(), NodeValue.makeFloat(latMax).asNode(), NodeValue.makeFloat(lonMax).asNode());
        PropFuncArg object = new PropFuncArg(objectNodes);

        //Function arguments
        SpatialArguments spatialArguments = instance.extractObjectArguments(predicate, object, SpatialIndexTestData.WGS_84_SRS_INFO);
        GeometryWrapper targetGeometryWrapper = GeometryWrapper.extract(targetGeometry);

        //Test arguments
        boolean expResult = false;
        boolean result = instance.checkSecondFilter(spatialArguments, targetGeometryWrapper);

        //System.out.println("Exp: " + expResult);
        //System.out.println("Res: " + result);
        assertEquals(expResult, result);
    }

    /**
     * Test of execEvaluated method, of class WithinBoxPF.
     */
    @Test
    public void testExecEvaluated() {
        System.out.println("execEvaluated");

        Dataset dataset = SpatialIndexTestData.createTestDataset();

        String query = "PREFIX spatial: <http://jena.apache.org/spatial#>\n"
                + "\n"
                + "SELECT ?subj\n"
                + "WHERE{\n"
                + "    ?subj spatial:withinBox(51.4 -0.13 51.6 -0.12) .\n"
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

        //System.out.println("Exp: " + expResult);
        //System.out.println("Res: " + result);
        assertEquals(expResult, result);
    }

}
