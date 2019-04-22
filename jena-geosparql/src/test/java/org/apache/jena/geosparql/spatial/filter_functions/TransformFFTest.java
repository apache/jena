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
package org.apache.jena.geosparql.spatial.filter_functions;

import org.apache.jena.geosparql.spatial.filter_functions.TransformFF;
import org.apache.jena.geosparql.configuration.GeoSPARQLConfig;
import org.apache.jena.geosparql.implementation.datatype.GMLDatatype;
import org.apache.jena.geosparql.implementation.datatype.WKTDatatype;
import org.apache.jena.geosparql.implementation.vocabulary.SRS_URI;
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
public class TransformFFTest {

    public TransformFFTest() {
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
     * Test of exec method, of class TransformFF.
     */
    @Test
    public void testExec_string_string() {
        System.out.println("exec_string");
        NodeValue v1 = NodeValue.makeNode("<gml:Point xmlns:gml=\"http://www.opengis.net/ont/gml\" srsName=\"http://www.opengis.net/def/crs/EPSG/0/4326\"><gml:pos>0 10</gml:pos></gml:Point>", GMLDatatype.INSTANCE);
        NodeValue v2 = NodeValue.makeString(WKTDatatype.URI);
        NodeValue v3 = NodeValue.makeString(SRS_URI.DEFAULT_WKT_CRS84);
        TransformFF instance = new TransformFF();
        NodeValue expResult = NodeValue.makeNode("POINT(10 0)", WKTDatatype.INSTANCE);
        NodeValue result = instance.exec(v1, v2, v3);
        assertEquals(expResult, result);
    }

    /**
     * Test of exec method, of class TransformFF.
     */
    @Test
    public void testExec_string_URI() {
        System.out.println("exec_string_URI");
        NodeValue v1 = NodeValue.makeNode("<gml:Point xmlns:gml=\"http://www.opengis.net/ont/gml\" srsName=\"http://www.opengis.net/def/crs/EPSG/0/4326\"><gml:pos>0 10</gml:pos></gml:Point>", GMLDatatype.INSTANCE);
        NodeValue v2 = NodeValue.makeString(WKTDatatype.URI);
        NodeValue v3 = NodeValue.makeNode(NodeFactory.createURI(SRS_URI.DEFAULT_WKT_CRS84));
        TransformFF instance = new TransformFF();
        NodeValue expResult = NodeValue.makeNode("POINT(10 0)", WKTDatatype.INSTANCE);
        NodeValue result = instance.exec(v1, v2, v3);
        assertEquals(expResult, result);
    }

    /**
     * Test of exec method, of class TransformFF.
     */
    @Test
    public void testExec_URI_string() {
        System.out.println("exec_URI_string");
        NodeValue v1 = NodeValue.makeNode("<gml:Point xmlns:gml=\"http://www.opengis.net/ont/gml\" srsName=\"http://www.opengis.net/def/crs/EPSG/0/4326\"><gml:pos>0 10</gml:pos></gml:Point>", GMLDatatype.INSTANCE);
        NodeValue v2 = NodeValue.makeNode(NodeFactory.createURI(WKTDatatype.URI));
        NodeValue v3 = NodeValue.makeString(SRS_URI.DEFAULT_WKT_CRS84);
        TransformFF instance = new TransformFF();
        NodeValue expResult = NodeValue.makeNode("POINT(10 0)", WKTDatatype.INSTANCE);
        NodeValue result = instance.exec(v1, v2, v3);
        assertEquals(expResult, result);
    }

    /**
     * Test of exec method, of class TransformFF.
     */
    @Test
    public void testExec_URI_URI() {
        System.out.println("exec_URI_URI");
        NodeValue v1 = NodeValue.makeNode("<gml:Point xmlns:gml=\"http://www.opengis.net/ont/gml\" srsName=\"http://www.opengis.net/def/crs/EPSG/0/4326\"><gml:pos>0 10</gml:pos></gml:Point>", GMLDatatype.INSTANCE);
        NodeValue v2 = NodeValue.makeNode(NodeFactory.createURI(WKTDatatype.URI));
        NodeValue v3 = NodeValue.makeNode(NodeFactory.createURI(SRS_URI.DEFAULT_WKT_CRS84));
        TransformFF instance = new TransformFF();
        NodeValue expResult = NodeValue.makeNode("POINT(10 0)", WKTDatatype.INSTANCE);
        NodeValue result = instance.exec(v1, v2, v3);
        assertEquals(expResult, result);
    }

    /**
     * Test of exec method, of class TransformFF.
     */
    @Test(expected = ExprEvalException.class)
    public void testExec_fail() {
        System.out.println("exec_fail");
        NodeValue v1 = NodeValue.makeNode("<gml:Point xmlns:gml=\"http://www.opengis.net/ont/gml\" srsName=\"http://www.opengis.net/def/crs/EPSG/0/4326\"><gml:pos>0 10</gml:pos></gml:Point>", GMLDatatype.INSTANCE);
        NodeValue v2 = NodeValue.makeInteger(8);
        NodeValue v3 = NodeValue.makeNode(NodeFactory.createURI(SRS_URI.DEFAULT_WKT_CRS84));
        TransformFF instance = new TransformFF();
        NodeValue expResult = NodeValue.makeNode("POINT(10 0)", WKTDatatype.INSTANCE);
        NodeValue result = instance.exec(v1, v2, v3);
        assertEquals(expResult, result);
    }

    /**
     * Test of exec method, of class TransformFF.
     */
    @Test(expected = ExprEvalException.class)
    public void testExec_fail2() {
        System.out.println("exec_fail2");
        NodeValue v1 = NodeValue.makeNode("<gml:Point xmlns:gml=\"http://www.opengis.net/ont/gml\" srsName=\"http://www.opengis.net/def/crs/EPSG/0/4326\"><gml:pos>0 10</gml:pos></gml:Point>", GMLDatatype.INSTANCE);
        NodeValue v2 = NodeValue.makeNode(NodeFactory.createURI(WKTDatatype.URI));
        NodeValue v3 = NodeValue.makeInteger(8);
        TransformFF instance = new TransformFF();
        NodeValue expResult = NodeValue.makeNode("POINT(10 0)", WKTDatatype.INSTANCE);
        NodeValue result = instance.exec(v1, v2, v3);
        assertEquals(expResult, result);
    }

    /**
     * Test of exec method, of class TransformFF.
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
                + "    BIND(<http://www.opengis.net/def/crs/OGC/1.3/CRS84> AS ?srsURI)"
                + "    BIND( spatialF:transform(?geomLit, ?datatypeURI, ?srsURI) AS ?result) \n"
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

        List<Literal> expResults = Arrays.asList(ResourceFactory.createTypedLiteral("POINT(10 0)", WKTDatatype.INSTANCE));

        //System.out.println("Exp: " + expResults);
        //System.out.println("Res: " + results);
        assertEquals(expResults, results);
    }
}
