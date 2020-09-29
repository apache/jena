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
package org.apache.jena.geosparql.spatial.property_functions.cardinal;

import java.util.Arrays;
import java.util.List;
import org.apache.jena.geosparql.implementation.GeometryWrapper;
import org.apache.jena.geosparql.implementation.vocabulary.SpatialExtension;
import org.apache.jena.geosparql.spatial.ConvertLatLon;
import org.apache.jena.geosparql.spatial.SearchEnvelope;
import org.apache.jena.geosparql.spatial.SpatialIndexTestData;
import org.apache.jena.geosparql.spatial.property_functions.SpatialArguments;
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
public class GenericCardinalPropertyFunctionTest {

    public GenericCardinalPropertyFunctionTest() {
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
     * GenericCardinalPropertyFunction.
     */
    @Test
    public void testExtractObjectArguments_3args() {


        Node predicate = NodeFactory.createURI(SpatialExtension.NORTH_PROP);

        float lat = 0;
        float lon = 1;
        int limit = 10;

        Literal geometry = ConvertLatLon.toLiteral(lat, lon);
        List<Node> objectNodes = Arrays.asList(NodeValue.makeFloat(lat).asNode(), NodeValue.makeFloat(lon).asNode(), NodeValue.makeInteger(limit).asNode());
        PropFuncArg object = new PropFuncArg(objectNodes);

        GeometryWrapper geometryWrapper = GeometryWrapper.extract(geometry);

        NorthPF instance = new NorthPF();
        SearchEnvelope searchEnvelope = instance.buildSearchEnvelope(geometryWrapper, SpatialIndexTestData.WGS_84_SRS_INFO);
        SpatialArguments expResult = new SpatialArguments(limit, geometryWrapper, searchEnvelope);
        SpatialArguments result = instance.extractObjectArguments(predicate, object, SpatialIndexTestData.WGS_84_SRS_INFO);
        assertEquals(expResult, result);
    }

    /**
     * Test of extractObjectArguments method, of class
     * GenericCardinalPropertyFunction.
     */
    @Test
    public void testExtractObjectArguments_2args() {


        Node predicate = NodeFactory.createURI(SpatialExtension.NORTH_PROP);

        float lat = 0;
        float lon = 1;
        int limit = -1;

        Literal geometry = ConvertLatLon.toLiteral(lat, lon);
        List<Node> objectNodes = Arrays.asList(NodeValue.makeFloat(lat).asNode(), NodeValue.makeFloat(lon).asNode());
        PropFuncArg object = new PropFuncArg(objectNodes);

        GeometryWrapper geometryWrapper = GeometryWrapper.extract(geometry);

        NorthPF instance = new NorthPF();
        SearchEnvelope searchEnvelope = instance.buildSearchEnvelope(geometryWrapper, SpatialIndexTestData.WGS_84_SRS_INFO);
        SpatialArguments expResult = new SpatialArguments(limit, geometryWrapper, searchEnvelope);
        SpatialArguments result = instance.extractObjectArguments(predicate, object, SpatialIndexTestData.WGS_84_SRS_INFO);
        assertEquals(expResult, result);
    }

    /**
     * Test of extractObjectArguments method, of class
     * GenericCardinalPropertyFunction.
     */
    @Test(expected = ExprEvalException.class)
    public void testExtractObjectArguments_1args_fail() {


        Node predicate = NodeFactory.createURI(SpatialExtension.NORTH_PROP);

        float lat = 0;
        float lon = 1;
        int limit = -1;

        Literal geometry = ConvertLatLon.toLiteral(lat, lon);
        List<Node> objectNodes = Arrays.asList(NodeValue.makeFloat(lat).asNode());
        PropFuncArg object = new PropFuncArg(objectNodes);

        GeometryWrapper geometryWrapper = GeometryWrapper.extract(geometry);

        NorthPF instance = new NorthPF();
        SearchEnvelope searchEnvelope = instance.buildSearchEnvelope(geometryWrapper, SpatialIndexTestData.WGS_84_SRS_INFO);
        SpatialArguments expResult = new SpatialArguments(limit, geometryWrapper, searchEnvelope);
        SpatialArguments result = instance.extractObjectArguments(predicate, object, SpatialIndexTestData.WGS_84_SRS_INFO);
        assertEquals(expResult, result);
    }

    /**
     * Test of extractObjectArguments method, of class
     * GenericCardinalPropertyFunction.
     */
    @Test(expected = ExprEvalException.class)
    public void testExtractObjectArguments_4args_fail() {


        Node predicate = NodeFactory.createURI(SpatialExtension.NORTH_PROP);

        float lat = 0;
        float lon = 1;
        int limit = 10;

        Literal geometry = ConvertLatLon.toLiteral(lat, lon);
        List<Node> objectNodes = Arrays.asList(NodeValue.makeFloat(lat).asNode(), NodeValue.makeFloat(lon).asNode(), NodeValue.makeInteger(limit).asNode(), NodeValue.makeBoolean(false).asNode());
        PropFuncArg object = new PropFuncArg(objectNodes);

        GeometryWrapper geometryWrapper = GeometryWrapper.extract(geometry);

        NorthPF instance = new NorthPF();
        SearchEnvelope searchEnvelope = instance.buildSearchEnvelope(geometryWrapper, SpatialIndexTestData.WGS_84_SRS_INFO);
        SpatialArguments expResult = new SpatialArguments(limit, geometryWrapper, searchEnvelope);
        SpatialArguments result = instance.extractObjectArguments(predicate, object, SpatialIndexTestData.WGS_84_SRS_INFO);
        assertEquals(expResult, result);
    }

    /**
     * Test of extractObjectArguments method, of class
     * GenericCardinalPropertyFunction.
     */
    @Test(expected = ExprEvalException.class)
    public void testExtractObjectArguments_3args_pos0_fail() {


        Node predicate = NodeFactory.createURI(SpatialExtension.NORTH_PROP);

        float lat = 0;
        float lon = 1;
        int limit = 10;

        Literal geometry = ConvertLatLon.toLiteral(lat, lon);
        List<Node> objectNodes = Arrays.asList(NodeValue.makeString("e").asNode(), NodeValue.makeFloat(lon).asNode(), NodeValue.makeInteger(limit).asNode());
        PropFuncArg object = new PropFuncArg(objectNodes);

        GeometryWrapper geometryWrapper = GeometryWrapper.extract(geometry);

        NorthPF instance = new NorthPF();
        SearchEnvelope searchEnvelope = instance.buildSearchEnvelope(geometryWrapper, SpatialIndexTestData.WGS_84_SRS_INFO);
        SpatialArguments expResult = new SpatialArguments(limit, geometryWrapper, searchEnvelope);
        SpatialArguments result = instance.extractObjectArguments(predicate, object, SpatialIndexTestData.WGS_84_SRS_INFO);
        assertEquals(expResult, result);
    }

    /**
     * Test of extractObjectArguments method, of class
     * GenericCardinalPropertyFunction.
     */
    @Test(expected = ExprEvalException.class)
    public void testExtractObjectArguments_3args_pos1_fail() {


        Node predicate = NodeFactory.createURI(SpatialExtension.NORTH_PROP);

        float lat = 0;
        float lon = 1;
        int limit = 10;

        Literal geometry = ConvertLatLon.toLiteral(lat, lon);
        List<Node> objectNodes = Arrays.asList(NodeValue.makeFloat(lat).asNode(), NodeValue.makeString("e").asNode(), NodeValue.makeInteger(limit).asNode());
        PropFuncArg object = new PropFuncArg(objectNodes);

        GeometryWrapper geometryWrapper = GeometryWrapper.extract(geometry);

        NorthPF instance = new NorthPF();
        SearchEnvelope searchEnvelope = instance.buildSearchEnvelope(geometryWrapper, SpatialIndexTestData.WGS_84_SRS_INFO);
        SpatialArguments expResult = new SpatialArguments(limit, geometryWrapper, searchEnvelope);
        SpatialArguments result = instance.extractObjectArguments(predicate, object, SpatialIndexTestData.WGS_84_SRS_INFO);
        assertEquals(expResult, result);
    }

    /**
     * Test of extractObjectArguments method, of class
     * GenericCardinalPropertyFunction.
     */
    @Test(expected = ExprEvalException.class)
    public void testExtractObjectArguments_3args_pos2_fail() {


        Node predicate = NodeFactory.createURI(SpatialExtension.NORTH_PROP);

        float lat = 0;
        float lon = 1;
        int limit = 10;

        Literal geometry = ConvertLatLon.toLiteral(lat, lon);
        List<Node> objectNodes = Arrays.asList(NodeValue.makeFloat(lat).asNode(), NodeValue.makeFloat(lon).asNode(), NodeValue.makeString("10").asNode());
        PropFuncArg object = new PropFuncArg(objectNodes);

        GeometryWrapper geometryWrapper = GeometryWrapper.extract(geometry);

        NorthPF instance = new NorthPF();
        SearchEnvelope searchEnvelope = instance.buildSearchEnvelope(geometryWrapper, SpatialIndexTestData.WGS_84_SRS_INFO);
        SpatialArguments expResult = new SpatialArguments(limit, geometryWrapper, searchEnvelope);
        SpatialArguments result = instance.extractObjectArguments(predicate, object, SpatialIndexTestData.WGS_84_SRS_INFO);
        assertEquals(expResult, result);
    }
}
