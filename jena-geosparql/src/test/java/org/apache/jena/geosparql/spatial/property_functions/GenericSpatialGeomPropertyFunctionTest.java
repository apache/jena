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

import java.util.Arrays;
import java.util.List;
import org.apache.jena.geosparql.implementation.GeometryWrapper;
import org.apache.jena.geosparql.implementation.vocabulary.SpatialExtension;
import org.apache.jena.geosparql.spatial.ConvertLatLon;
import org.apache.jena.geosparql.spatial.SearchEnvelope;
import org.apache.jena.geosparql.spatial.SpatialIndexTestData;
import org.apache.jena.geosparql.spatial.property_functions.box.IntersectBoxGeomPF;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.rdf.model.Literal;
import org.apache.jena.sparql.expr.ExprEvalException;
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
public class GenericSpatialGeomPropertyFunctionTest {

    public GenericSpatialGeomPropertyFunctionTest() {
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
     * Test of extractObjectArguments method, of class
     * GenericSpatialGeomPropertyFunction.
     */
    @Test
    public void testExtractObjectArguments_2args() {
        System.out.println("extractObjectArguments_2args");

        Node predicate = NodeFactory.createURI(SpatialExtension.INTERSECT_BOX_GEOM_PROP);

        //Geometry and Envelope parameters
        float lat = 0.0f;
        float lon = 1.0f;
        int limit = 10;

        Literal geometry = ConvertLatLon.toLiteral(lat, lon);

        List<Node> objectNodes = Arrays.asList(geometry.asNode(), NodeValue.makeInteger(limit).asNode());
        PropFuncArg object = new PropFuncArg(objectNodes);

        //Function arguments
        GeometryWrapper geometryWrapper = GeometryWrapper.extract(geometry);
        SearchEnvelope searchEnvelope = SearchEnvelope.build(geometryWrapper, SpatialIndexTestData.WGS_84_SRS_INFO);

        GenericSpatialGeomPropertyFunction instance = new IntersectBoxGeomPF();
        SpatialArguments expResult = new SpatialArguments(limit, geometryWrapper, searchEnvelope);
        SpatialArguments result = instance.extractObjectArguments(predicate, object, SpatialIndexTestData.WGS_84_SRS_INFO);
        assertEquals(expResult, result);
    }

    /**
     * Test of extractObjectArguments method, of class
     * GenericSpatialGeomPropertyFunction.
     */
    @Test
    public void testExtractObjectArguments_1args() {
        System.out.println("extractObjectArguments_1args");

        Node predicate = NodeFactory.createURI(SpatialExtension.INTERSECT_BOX_GEOM_PROP);

        //Geometry and Envelope parameters
        float lat = 0.0f;
        float lon = 1.0f;
        int limit = -1;

        Literal geometry = ConvertLatLon.toLiteral(lat, lon);

        List<Node> objectNodes = Arrays.asList(geometry.asNode());
        PropFuncArg object = new PropFuncArg(objectNodes);

        //Function arguments
        GeometryWrapper geometryWrapper = GeometryWrapper.extract(geometry);
        SearchEnvelope searchEnvelope = SearchEnvelope.build(geometryWrapper, SpatialIndexTestData.WGS_84_SRS_INFO);

        GenericSpatialGeomPropertyFunction instance = new IntersectBoxGeomPF();
        SpatialArguments expResult = new SpatialArguments(limit, geometryWrapper, searchEnvelope);
        SpatialArguments result = instance.extractObjectArguments(predicate, object, SpatialIndexTestData.WGS_84_SRS_INFO);
        assertEquals(expResult, result);
    }

    /**
     * Test of extractObjectArguments method, of class
     * GenericSpatialGeomPropertyFunction.
     */
    @Test(expected = ExprEvalException.class)
    public void testExtractObjectArguments_0args_fail() {
        System.out.println("extractObjectArguments_0args_fail");

        Node predicate = NodeFactory.createURI(SpatialExtension.INTERSECT_BOX_GEOM_PROP);

        //Geometry and Envelope parameters
        float lat = 0.0f;
        float lon = 1.0f;
        int limit = 10;

        Literal geometry = ConvertLatLon.toLiteral(lat, lon);

        List<Node> objectNodes = Arrays.asList();
        PropFuncArg object = new PropFuncArg(objectNodes);

        //Function arguments
        GeometryWrapper geometryWrapper = GeometryWrapper.extract(geometry);
        SearchEnvelope searchEnvelope = SearchEnvelope.build(geometryWrapper, SpatialIndexTestData.WGS_84_SRS_INFO);

        GenericSpatialGeomPropertyFunction instance = new IntersectBoxGeomPF();
        SpatialArguments expResult = new SpatialArguments(limit, geometryWrapper, searchEnvelope);
        SpatialArguments result = instance.extractObjectArguments(predicate, object, SpatialIndexTestData.WGS_84_SRS_INFO);
        assertEquals(expResult, result);
    }

    /**
     * Test of extractObjectArguments method, of class
     * GenericSpatialGeomPropertyFunction.
     */
    @Test(expected = ExprEvalException.class)
    public void testExtractObjectArguments_3args_fail() {
        System.out.println("extractObjectArguments_3args_fail");

        Node predicate = NodeFactory.createURI(SpatialExtension.INTERSECT_BOX_GEOM_PROP);

        //Geometry and Envelope parameters
        float lat = 0.0f;
        float lon = 1.0f;
        int limit = 10;

        Literal geometry = ConvertLatLon.toLiteral(lat, lon);

        List<Node> objectNodes = Arrays.asList(geometry.asNode(), NodeValue.makeInteger(limit).asNode(), NodeValue.makeBoolean(false).asNode());
        PropFuncArg object = new PropFuncArg(objectNodes);

        //Function arguments
        GeometryWrapper geometryWrapper = GeometryWrapper.extract(geometry);
        SearchEnvelope searchEnvelope = SearchEnvelope.build(geometryWrapper, SpatialIndexTestData.WGS_84_SRS_INFO);

        GenericSpatialGeomPropertyFunction instance = new IntersectBoxGeomPF();
        SpatialArguments expResult = new SpatialArguments(limit, geometryWrapper, searchEnvelope);
        SpatialArguments result = instance.extractObjectArguments(predicate, object, SpatialIndexTestData.WGS_84_SRS_INFO);
        assertEquals(expResult, result);
    }

    /**
     * Test of extractObjectArguments method, of class
     * GenericSpatialGeomPropertyFunction.
     */
    @Test(expected = ExprEvalException.class)
    public void testExtractObjectArguments_2args_pos0_fail() {
        System.out.println("extractObjectArguments_2args_pos0_fail");

        Node predicate = NodeFactory.createURI(SpatialExtension.INTERSECT_BOX_GEOM_PROP);

        //Geometry and Envelope parameters
        float lat = 0.0f;
        float lon = 1.0f;
        int limit = 10;

        Literal geometry = ConvertLatLon.toLiteral(lat, lon);

        List<Node> objectNodes = Arrays.asList(NodeValue.makeString("Geometry").asNode(), NodeValue.makeInteger(limit).asNode());
        PropFuncArg object = new PropFuncArg(objectNodes);

        //Function arguments
        GeometryWrapper geometryWrapper = GeometryWrapper.extract(geometry);
        SearchEnvelope searchEnvelope = SearchEnvelope.build(geometryWrapper, SpatialIndexTestData.WGS_84_SRS_INFO);

        GenericSpatialGeomPropertyFunction instance = new IntersectBoxGeomPF();
        SpatialArguments expResult = new SpatialArguments(limit, geometryWrapper, searchEnvelope);
        SpatialArguments result = instance.extractObjectArguments(predicate, object, SpatialIndexTestData.WGS_84_SRS_INFO);
        assertEquals(expResult, result);
    }

    /**
     * Test of extractObjectArguments method, of class
     * GenericSpatialGeomPropertyFunction.
     */
    @Test(expected = ExprEvalException.class)
    public void testExtractObjectArguments_2args_pos1_fail() {
        System.out.println("extractObjectArguments_2args_pos1_fail");

        Node predicate = NodeFactory.createURI(SpatialExtension.INTERSECT_BOX_GEOM_PROP);

        //Geometry and Envelope parameters
        float lat = 0.0f;
        float lon = 1.0f;
        int limit = 10;

        Literal geometry = ConvertLatLon.toLiteral(lat, lon);

        List<Node> objectNodes = Arrays.asList(geometry.asNode(), NodeValue.makeString("10").asNode());
        PropFuncArg object = new PropFuncArg(objectNodes);

        //Function arguments
        GeometryWrapper geometryWrapper = GeometryWrapper.extract(geometry);
        SearchEnvelope searchEnvelope = SearchEnvelope.build(geometryWrapper, SpatialIndexTestData.WGS_84_SRS_INFO);

        GenericSpatialGeomPropertyFunction instance = new IntersectBoxGeomPF();
        SpatialArguments expResult = new SpatialArguments(limit, geometryWrapper, searchEnvelope);
        SpatialArguments result = instance.extractObjectArguments(predicate, object, SpatialIndexTestData.WGS_84_SRS_INFO);
        assertEquals(expResult, result);
    }
}
