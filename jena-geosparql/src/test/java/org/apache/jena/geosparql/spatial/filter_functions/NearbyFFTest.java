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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.apache.jena.datatypes.xsd.XSDDatatype;
import org.apache.jena.geosparql.configuration.GeoSPARQLConfig;
import org.apache.jena.geosparql.implementation.GeometryWrapper;
import org.apache.jena.geosparql.implementation.datatype.WKTDatatype;
import org.apache.jena.geosparql.implementation.vocabulary.Unit_URI;
import org.apache.jena.geosparql.spatial.SpatialIndexTestData;
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
public class NearbyFFTest {

    public NearbyFFTest() {
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
     * Test of exec method, of class NearbyFF.
     */
    @Test
    public void testExec() {
        System.out.println("exec");
        NodeValue v1 = NodeValue.makeNode("<http://www.opengis.net/def/crs/EPSG/0/4326> POINT(10.0 20.0)", WKTDatatype.INSTANCE);
        NodeValue v2 = NodeValue.makeNode("<http://www.opengis.net/def/crs/EPSG/0/4326> POINT(10.0 20.0001)", WKTDatatype.INSTANCE);
        NodeValue v3 = NodeValue.makeFloat(50);
        NodeValue v4 = NodeValue.makeNode(NodeFactory.createURI(Unit_URI.KILOMETER_URL));
        NearbyFF instance = new NearbyFF();
        NodeValue expResult = NodeValue.makeBoolean(true);
        NodeValue result = instance.exec(v1, v2, v3, v4);

        //System.out.println("Exp: " + expResult);
        //System.out.println("Res: " + result);
        assertEquals(expResult, result);
    }

    /**
     * Test of exec method, of class NearbyFF.
     */
    @Test
    public void testExec2() {
        System.out.println("exec2");
        NodeValue v1 = NodeValue.makeNode("<http://www.opengis.net/def/crs/EPSG/0/4326> POINT(10.0 20.0)", WKTDatatype.INSTANCE);
        NodeValue v2 = NodeValue.makeNode("<http://www.opengis.net/def/crs/EPSG/0/4326> POINT(10.0 20.0001)", WKTDatatype.INSTANCE);
        NodeValue v3 = NodeValue.makeFloat(50);
        NodeValue v4 = NodeValue.makeString(Unit_URI.KILOMETER_URL);
        NearbyFF instance = new NearbyFF();
        NodeValue expResult = NodeValue.makeBoolean(true);
        NodeValue result = instance.exec(v1, v2, v3, v4);

        //System.out.println("Exp: " + expResult);
        //System.out.println("Res: " + result);
        assertEquals(expResult, result);
    }

    /**
     * Test of exec method, of class NearbyFF.
     */
    @Test
    public void testExec_Paris_London() {
        System.out.println("exec_Paris_London");
        NodeValue v1 = SpatialIndexTestData.PARIS_GEOMETRY_WRAPPER.asNodeValue();
        NodeValue v2 = SpatialIndexTestData.LONDON_GEOMETRY_WRAPPER.asNodeValue();
        NodeValue v3 = NodeValue.makeFloat(345);
        NodeValue v4 = NodeValue.makeString(Unit_URI.KILOMETER_URL);
        NearbyFF instance = new NearbyFF();
        NodeValue expResult = NodeValue.makeBoolean(true);
        NodeValue result = instance.exec(v1, v2, v3, v4);

        //System.out.println("Exp: " + expResult);
        //System.out.println("Res: " + result);
        assertEquals(expResult, result);
    }

    /**
     * Test of exec method, of class NearbyFF.
     */
    @Test
    public void testExec_Paris_London_fail() {
        System.out.println("exec_Paris_London_fail");
        NodeValue v1 = SpatialIndexTestData.PARIS_GEOMETRY_WRAPPER.asNodeValue();
        NodeValue v2 = SpatialIndexTestData.LONDON_GEOMETRY_WRAPPER.asNodeValue();
        NodeValue v3 = NodeValue.makeFloat(200);
        NodeValue v4 = NodeValue.makeString(Unit_URI.KILOMETER_URL);
        NearbyFF instance = new NearbyFF();
        NodeValue expResult = NodeValue.makeBoolean(false);
        NodeValue result = instance.exec(v1, v2, v3, v4);

        //System.out.println("Exp: " + expResult);
        //System.out.println("Res: " + result);
        assertEquals(expResult, result);
    }

    /**
     * Test of exec method, of class NearbyFF.
     */
    @Test
    public void testExec_fail() {
        System.out.println("exec_fail");
        NodeValue v1 = NodeValue.makeNode("<http://www.opengis.net/def/crs/EPSG/0/4326> POINT(10.0 20.0)", WKTDatatype.INSTANCE);
        NodeValue v2 = NodeValue.makeNode("<http://www.opengis.net/def/crs/EPSG/0/4326> POINT(10.0 30.0)", WKTDatatype.INSTANCE);
        NodeValue v3 = NodeValue.makeFloat(50);
        NodeValue v4 = NodeValue.makeNode(NodeFactory.createURI(Unit_URI.KILOMETER_URL));
        NearbyFF instance = new NearbyFF();
        NodeValue expResult = NodeValue.makeBoolean(false);
        NodeValue result = instance.exec(v1, v2, v3, v4);

        //System.out.println("Exp: " + expResult);
        //System.out.println("Res: " + result);
        assertEquals(expResult, result);
    }

    /**
     * Test of relate method, of class NearbyFF.
     */
    @Test
    public void testRelate() {
        System.out.println("relate");
        GeometryWrapper geometry1 = GeometryWrapper.extract("<http://www.opengis.net/def/crs/EPSG/0/4326> POINT(10.0 20.0)", WKTDatatype.URI);
        GeometryWrapper geometry2 = GeometryWrapper.extract("<http://www.opengis.net/def/crs/EPSG/0/4326> POINT(10.0 20.0001)", WKTDatatype.URI);;
        double radius = 5.0;
        String unitsURI = Unit_URI.KILOMETER_URL;
        boolean expResult = true;
        boolean result = NearbyFF.relate(geometry1, geometry2, radius, unitsURI);

        //System.out.println("Exp: " + expResult);
        //System.out.println("Res: " + result);
        assertEquals(expResult, result);
    }

    /**
     * Test of relate method, of class NearbyFF.
     */
    @Test
    public void testRelate_OSGB() {
        System.out.println("relate_OSGB");
        GeometryWrapper geometry1 = GeometryWrapper.extract("<http://www.opengis.net/def/crs/EPSG/0/27700> POINT(0.0 0.0)", WKTDatatype.URI);
        GeometryWrapper geometry2 = GeometryWrapper.extract("<http://www.opengis.net/def/crs/EPSG/0/27700> POINT(10000.0 0.0)", WKTDatatype.URI);;
        double radius = 10.1;
        String unitsURI = Unit_URI.KILOMETER_URL;
        boolean expResult = true;
        boolean result = NearbyFF.relate(geometry1, geometry2, radius, unitsURI);

        //System.out.println("Exp: " + expResult);
        //System.out.println("Res: " + result);
        assertEquals(expResult, result);
    }

    /**
     * Test of relate method, of class NearbyFF.
     */
    @Test
    public void testRelate_OSGB_fail() {
        System.out.println("relate_OSGB_fail");
        GeometryWrapper geometry1 = GeometryWrapper.extract("<http://www.opengis.net/def/crs/EPSG/0/27700> POINT(0.0 0.0)", WKTDatatype.URI);
        GeometryWrapper geometry2 = GeometryWrapper.extract("<http://www.opengis.net/def/crs/EPSG/0/27700> POINT(10000.0 0.0)", WKTDatatype.URI);;
        double radius = 9.9;
        String unitsURI = Unit_URI.KILOMETER_URL;
        boolean expResult = false;
        boolean result = NearbyFF.relate(geometry1, geometry2, radius, unitsURI);

        //System.out.println("Exp: " + expResult);
        //System.out.println("Res: " + result);
        assertEquals(expResult, result);
    }

    /**
     * Test of relate method, of class NearbyFF.
     */
    @Test
    public void testRelate_fail() {
        System.out.println("relate_fail");
        GeometryWrapper geometry1 = GeometryWrapper.extract("<http://www.opengis.net/def/crs/EPSG/0/4326> POINT(10.0 20.0)", WKTDatatype.URI);
        GeometryWrapper geometry2 = GeometryWrapper.extract("<http://www.opengis.net/def/crs/EPSG/0/4326> POINT(10.0 30.0)", WKTDatatype.URI);;
        double radius = 5.0;
        String unitsURI = Unit_URI.KILOMETER_URL;
        boolean expResult = false;
        boolean result = NearbyFF.relate(geometry1, geometry2, radius, unitsURI);

        //System.out.println("Exp: " + expResult);
        //System.out.println("Res: " + result);
        assertEquals(expResult, result);
    }

    /**
     * Test of exec method, of class NearbyFF.
     */
    @Test(expected = ExprEvalException.class)
    public void testExec_pos0_fail() {
        System.out.println("exec_pos0_fail");
        NodeValue v1 = NodeValue.makeString("<http://www.opengis.net/def/crs/EPSG/0/4326> POINT(10.0 20.0)");
        NodeValue v2 = NodeValue.makeNode("<http://www.opengis.net/def/crs/EPSG/0/4326> POINT(10.0 20.0001)", WKTDatatype.INSTANCE);
        NodeValue v3 = NodeValue.makeFloat(50);
        NodeValue v4 = NodeValue.makeNode(NodeFactory.createURI(Unit_URI.KILOMETER_URL));
        NearbyFF instance = new NearbyFF();
        NodeValue expResult = NodeValue.makeBoolean(true);
        NodeValue result = instance.exec(v1, v2, v3, v4);

        //System.out.println("Exp: " + expResult);
        //System.out.println("Res: " + result);
        //assertEquals(expResult, result);
    }

    /**
     * Test of exec method, of class NearbyFF.
     */
    @Test(expected = ExprEvalException.class)
    public void testExec_pos1_fail() {
        System.out.println("exec_pos1_fail");
        NodeValue v1 = NodeValue.makeNode("<http://www.opengis.net/def/crs/EPSG/0/4326> POINT(10.0 20.0)", WKTDatatype.INSTANCE);
        NodeValue v2 = NodeValue.makeString("<http://www.opengis.net/def/crs/EPSG/0/4326> POINT(10.0 20.0001)");
        NodeValue v3 = NodeValue.makeFloat(50);
        NodeValue v4 = NodeValue.makeNode(NodeFactory.createURI(Unit_URI.KILOMETER_URL));
        NearbyFF instance = new NearbyFF();
        NodeValue expResult = NodeValue.makeBoolean(true);
        NodeValue result = instance.exec(v1, v2, v3, v4);

        //System.out.println("Exp: " + expResult);
        //System.out.println("Res: " + result);
        //assertEquals(expResult, result);
    }

    /**
     * Test of exec method, of class NearbyFF.
     */
    @Test(expected = ExprEvalException.class)
    public void testExec_pos2_fail() {
        System.out.println("exec_pos2_fail");
        NodeValue v1 = NodeValue.makeNode("<http://www.opengis.net/def/crs/EPSG/0/4326> POINT(10.0 20.0)", WKTDatatype.INSTANCE);
        NodeValue v2 = NodeValue.makeNode("<http://www.opengis.net/def/crs/EPSG/0/4326> POINT(10.0 20.0001)", WKTDatatype.INSTANCE);
        NodeValue v3 = NodeValue.makeString("50");
        NodeValue v4 = NodeValue.makeNode(NodeFactory.createURI(Unit_URI.KILOMETER_URL));
        NearbyFF instance = new NearbyFF();
        NodeValue expResult = NodeValue.makeBoolean(true);
        NodeValue result = instance.exec(v1, v2, v3, v4);

        //System.out.println("Exp: " + expResult);
        //System.out.println("Res: " + result);
        //assertEquals(expResult, result);
    }

    /**
     * Test of exec method, of class NearbyFF.
     */
    @Test(expected = ExprEvalException.class)
    public void testExec_pos3_fail() {
        System.out.println("exec_pos3_fail");
        NodeValue v1 = NodeValue.makeNode("<http://www.opengis.net/def/crs/EPSG/0/4326> POINT(10.0 20.0)", WKTDatatype.INSTANCE);
        NodeValue v2 = NodeValue.makeNode("<http://www.opengis.net/def/crs/EPSG/0/4326> POINT(10.0 20.0001)", WKTDatatype.INSTANCE);
        NodeValue v3 = NodeValue.makeFloat(50);
        NodeValue v4 = NodeValue.makeInteger(20);
        NearbyFF instance = new NearbyFF();
        NodeValue expResult = NodeValue.makeBoolean(true);
        NodeValue result = instance.exec(v1, v2, v3, v4);

        //System.out.println("Exp: " + expResult);
        //System.out.println("Res: " + result);
        //assertEquals(expResult, result);
    }

    /**
     * Test of exec method, of class NearbyFF.
     */
    @Test
    public void testExec_query() {
        System.out.println("exec_query");

        Dataset dataset = SpatialIndexTestData.createTestDataset();

        String query = "PREFIX spatialF: <http://jena.apache.org/function/spatial#>\n"
                + "\n"
                + "SELECT ?result\n"
                + "WHERE{\n"
                + "    BIND( \"<http://www.opengis.net/def/crs/EPSG/0/4326> POINT(51.50853 -0.12574)\"^^<http://www.opengis.net/ont/geosparql#wktLiteral> AS ?geom1)"
                + "    BIND( \"<http://www.opengis.net/def/crs/EPSG/0/4326> POINT(48.857487 2.373047)\"^^<http://www.opengis.net/ont/geosparql#wktLiteral> AS ?geom2)"
                + "    BIND( spatialF:nearby(?geom1, ?geom2, 345, <http://www.opengis.net/def/uom/OGC/1.0/kilometer>) AS ?result) \n"
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

        List<Literal> expResults = Arrays.asList(ResourceFactory.createTypedLiteral(Boolean.TRUE.toString(), XSDDatatype.XSDboolean));

        //System.out.println("Exp: " + expResults);
        //System.out.println("Res: " + results);
        assertEquals(expResults, results);
    }

    /**
     * Test of exec method, of class NearbyFF.
     */
    @Test
    public void testExec_query_false() {
        System.out.println("exec_query_false");

        Dataset dataset = SpatialIndexTestData.createTestDataset();

        String query = "PREFIX spatialF: <http://jena.apache.org/function/spatial#>\n"
                + "\n"
                + "SELECT ?result\n"
                + "WHERE{\n"
                + "    BIND( \"<http://www.opengis.net/def/crs/EPSG/0/4326> POINT(51.50853 -0.12574)\"^^<http://www.opengis.net/ont/geosparql#wktLiteral> AS ?geom1)"
                + "    BIND( \"<http://www.opengis.net/def/crs/EPSG/0/4326> POINT(48.857487 2.373047)\"^^<http://www.opengis.net/ont/geosparql#wktLiteral> AS ?geom2)"
                + "    BIND( spatialF:nearby(?geom1, ?geom2, 200, <http://www.opengis.net/def/uom/OGC/1.0/kilometer>) AS ?result) \n"
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

        List<Literal> expResults = Arrays.asList(ResourceFactory.createTypedLiteral(Boolean.FALSE.toString(), XSDDatatype.XSDboolean));

        //System.out.println("Exp: " + expResults);
        //System.out.println("Res: " + results);
        assertEquals(expResults, results);
    }
}
