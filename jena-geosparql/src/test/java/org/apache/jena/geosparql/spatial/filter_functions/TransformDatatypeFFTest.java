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
package org.apache.jena.geosparql.spatial.filter_functions;

import org.apache.jena.geosparql.spatial.filter_functions.TransformDatatypeFF;
import org.apache.jena.geosparql.configuration.GeoSPARQLConfig;
import org.apache.jena.geosparql.implementation.datatype.GMLDatatype;
import org.apache.jena.geosparql.implementation.datatype.WKTDatatype;
import org.apache.jena.geosparql.spatial.SpatialIndexTestData;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.query.Dataset;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QueryExecutionFactory;
import org.apache.jena.query.QuerySolution;
import org.apache.jena.query.ResultSet;
import org.apache.jena.rdf.model.Literal;
import org.apache.jena.rdf.model.ResourceFactory;
import org.apache.jena.sparql.expr.ExprEvalException;
import org.apache.jena.sparql.expr.NodeValue;
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
public class TransformDatatypeFFTest {

    public TransformDatatypeFFTest() {
        GeoSPARQLConfig.setupNoIndex();
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
     * Test of exec method, of class TransformDatatypeFF.
     */
    @Test
    public void testExec_string() {
        System.out.println("exec_string");
        NodeValue v1 = NodeValue.makeNode("<gml:Point xmlns:gml=\"http://www.opengis.net/ont/gml\" srsName=\"http://www.opengis.net/def/crs/EPSG/0/4326\"><gml:pos>0 10</gml:pos></gml:Point>", GMLDatatype.INSTANCE);
        NodeValue v2 = NodeValue.makeString(WKTDatatype.URI);
        TransformDatatypeFF instance = new TransformDatatypeFF();
        NodeValue expResult = NodeValue.makeNode("<http://www.opengis.net/def/crs/EPSG/0/4326> POINT(0 10)", WKTDatatype.INSTANCE);
        NodeValue result = instance.exec(v1, v2);
        assertEquals(expResult, result);
    }

    /**
     * Test of exec method, of class TransformDatatypeFF.
     */
    @Test
    public void testExec_URI() {
        System.out.println("exec_URI");
        NodeValue v1 = NodeValue.makeNode("<gml:Point xmlns:gml=\"http://www.opengis.net/ont/gml\" srsName=\"http://www.opengis.net/def/crs/EPSG/0/4326\"><gml:pos>0 10</gml:pos></gml:Point>", GMLDatatype.INSTANCE);
        NodeValue v2 = NodeValue.makeNode(NodeFactory.createURI(WKTDatatype.URI));
        TransformDatatypeFF instance = new TransformDatatypeFF();
        NodeValue expResult = NodeValue.makeNode("<http://www.opengis.net/def/crs/EPSG/0/4326> POINT(0 10)", WKTDatatype.INSTANCE);
        NodeValue result = instance.exec(v1, v2);
        assertEquals(expResult, result);
    }

    /**
     * Test of exec method, of class TransformDatatypeFF.
     */
    @Test(expected = ExprEvalException.class)
    public void testExec_fail() {
        System.out.println("exec_fail");
        NodeValue v1 = NodeValue.makeNode("<gml:Point xmlns:gml=\"http://www.opengis.net/ont/gml\" srsName=\"http://www.opengis.net/def/crs/EPSG/0/4326\"><gml:pos>0 10</gml:pos></gml:Point>", GMLDatatype.INSTANCE);
        NodeValue v2 = NodeValue.makeInteger(8);
        TransformDatatypeFF instance = new TransformDatatypeFF();
        NodeValue expResult = NodeValue.makeNode("<http://www.opengis.net/def/crs/EPSG/0/4326> POINT(0 10)", WKTDatatype.INSTANCE);
        NodeValue result = instance.exec(v1, v2);
        assertEquals(expResult, result);
    }

    /**
     * Test of exec method, of class TransformDatatypeFF.
     */
    @Test
    public void testExec_query() {
        System.out.println("exec_query");

        Dataset dataset = SpatialIndexTestData.createTestDataset();

        String query = "PREFIX spatialF: <http://jena.apache.org/function/spatial#>\n"
                + "\n"
                + "SELECT ?result\n"
                + "WHERE{\n"
                + "    BIND(\"<gml:Point xmlns:gml=\\\"http://www.opengis.net/ont/gml\\\" srsName=\\\"http://www.opengis.net/def/crs/EPSG/0/4326\\\"><gml:pos>0 10</gml:pos></gml:Point>\"^^<http://www.opengis.net/ont/geosparql#gmlLiteral> AS ?geomLit)"
                + "    BIND(<http://www.opengis.net/ont/geosparql#wktLiteral> AS ?datatypeURI)"
                + "    BIND( spatialF:transformDatatype(?geomLit, ?datatypeURI) AS ?result) \n"
                + "}ORDER by ?result";

        List<Literal> results = new ArrayList<>();
        try (QueryExecution qe = QueryExecutionFactory.create(query, dataset)) {
            ResultSet rs = qe.execSelect();
            while (rs.hasNext()) {
                QuerySolution qs = rs.nextSolution();
                Literal result = qs.getLiteral("result");
                results.add(result);
            }
        }

        List<Literal> expResults = Arrays.asList(ResourceFactory.createTypedLiteral("<http://www.opengis.net/def/crs/EPSG/0/4326> POINT(0 10)", WKTDatatype.INSTANCE));

        //System.out.println("Exp: " + expResults);
        //System.out.println("Res: " + results);
        assertEquals(expResults, results);
    }
}
