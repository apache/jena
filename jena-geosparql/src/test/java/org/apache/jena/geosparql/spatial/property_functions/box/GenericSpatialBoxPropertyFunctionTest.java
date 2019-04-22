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

import org.apache.jena.geosparql.spatial.property_functions.box.WithinBoxPF;
import org.apache.jena.geosparql.spatial.property_functions.box.GenericSpatialBoxPropertyFunction;
import org.apache.jena.geosparql.implementation.GeometryWrapper;
import org.apache.jena.geosparql.implementation.vocabulary.SpatialExtension;
import org.apache.jena.geosparql.spatial.ConvertLatLonBox;
import org.apache.jena.geosparql.spatial.SearchEnvelope;
import org.apache.jena.geosparql.spatial.SpatialIndexTestData;
import org.apache.jena.geosparql.spatial.property_functions.SpatialArguments;
import java.util.Arrays;
import java.util.List;
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
public class GenericSpatialBoxPropertyFunctionTest {

    public GenericSpatialBoxPropertyFunctionTest() {
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
     * GenericSpatialBoxPropertyFunction.
     */
    @Test
    public void testExtractObjectArguments_5args() {
        System.out.println("extractObjectArguments_5args");

        GenericSpatialBoxPropertyFunction instance = new WithinBoxPF();

        Node predicate = NodeFactory.createURI(SpatialExtension.WITHIN_BOX_PROP);

        //Geometry and Envelope parameters
        float latMin = 0;
        float lonMin = 1;
        float latMax = 2;
        float lonMax = 3;
        int limit = 10;

        List<Node> objectNodes = Arrays.asList(NodeValue.makeFloat(latMin).asNode(), NodeValue.makeFloat(lonMin).asNode(), NodeValue.makeFloat(latMax).asNode(), NodeValue.makeFloat(lonMax).asNode(), NodeValue.makeInteger(limit).asNode());
        PropFuncArg object = new PropFuncArg(objectNodes);

        //Function arguments
        Literal geometry = ConvertLatLonBox.toLiteral(latMin, lonMin, latMax, lonMax);
        GeometryWrapper geometryWrapper = GeometryWrapper.extract(geometry);
        SearchEnvelope searchEnvelope = SearchEnvelope.build(geometryWrapper, SpatialIndexTestData.WGS_84_SRS_INFO);

        SpatialArguments expResult = new SpatialArguments(limit, geometryWrapper, searchEnvelope);
        SpatialArguments result = instance.extractObjectArguments(predicate, object, SpatialIndexTestData.WGS_84_SRS_INFO);

        //System.out.println("Exp: " + expResult);
        //System.out.println("Res: " + result);
        assertEquals(expResult, result);
    }

    /**
     * Test of extractObjectArguments method, of class
     * GenericSpatialBoxPropertyFunction.
     */
    @Test
    public void testExtractObjectArguments_4args() {
        System.out.println("extractObjectArguments_4args");

        GenericSpatialBoxPropertyFunction instance = new WithinBoxPF();

        Node predicate = NodeFactory.createURI(SpatialExtension.WITHIN_BOX_PROP);

        //Geometry and Envelope parameters
        float latMin = 0;
        float lonMin = 1;
        float latMax = 2;
        float lonMax = 3;
        int limit = -1;

        List<Node> objectNodes = Arrays.asList(NodeValue.makeFloat(latMin).asNode(), NodeValue.makeFloat(lonMin).asNode(), NodeValue.makeFloat(latMax).asNode(), NodeValue.makeFloat(lonMax).asNode());
        PropFuncArg object = new PropFuncArg(objectNodes);

        //Function arguments
        Literal geometry = ConvertLatLonBox.toLiteral(latMin, lonMin, latMax, lonMax);
        GeometryWrapper geometryWrapper = GeometryWrapper.extract(geometry);
        SearchEnvelope searchEnvelope = SearchEnvelope.build(geometryWrapper, SpatialIndexTestData.WGS_84_SRS_INFO);

        SpatialArguments expResult = new SpatialArguments(limit, geometryWrapper, searchEnvelope);
        SpatialArguments result = instance.extractObjectArguments(predicate, object, SpatialIndexTestData.WGS_84_SRS_INFO);

        //System.out.println("Exp: " + expResult);
        //System.out.println("Res: " + result);
        assertEquals(expResult, result);
    }

    /**
     * Test of extractObjectArguments method, of class
     * GenericSpatialBoxPropertyFunction.
     */
    @Test(expected = ExprEvalException.class)
    public void testExtractObjectArguments_3args_fail() {
        System.out.println("extractObjectArguments_3args_fail");

        GenericSpatialBoxPropertyFunction instance = new WithinBoxPF();

        Node predicate = NodeFactory.createURI(SpatialExtension.WITHIN_BOX_PROP);

        //Geometry and Envelope parameters
        float latMin = 0;
        float lonMin = 1;
        float latMax = 2;
        float lonMax = 3;
        int limit = 10;

        List<Node> objectNodes = Arrays.asList(NodeValue.makeFloat(latMin).asNode(), NodeValue.makeFloat(lonMin).asNode(), NodeValue.makeFloat(latMax).asNode());
        PropFuncArg object = new PropFuncArg(objectNodes);

        //Function arguments
        Literal geometry = ConvertLatLonBox.toLiteral(latMin, lonMin, latMax, lonMax);
        GeometryWrapper geometryWrapper = GeometryWrapper.extract(geometry);
        SearchEnvelope searchEnvelope = SearchEnvelope.build(geometryWrapper, SpatialIndexTestData.WGS_84_SRS_INFO);

        SpatialArguments expResult = new SpatialArguments(limit, geometryWrapper, searchEnvelope);
        SpatialArguments result = instance.extractObjectArguments(predicate, object, SpatialIndexTestData.WGS_84_SRS_INFO);

        //System.out.println("Exp: " + expResult);
        //System.out.println("Res: " + result);
        //assertEquals(expResult, result);
    }

    /**
     * Test of extractObjectArguments method, of class
     * GenericSpatialBoxPropertyFunction.
     */
    @Test(expected = ExprEvalException.class)
    public void testExtractObjectArguments_6args_fail() {
        System.out.println("extractObjectArguments_6args_fail");

        GenericSpatialBoxPropertyFunction instance = new WithinBoxPF();

        Node predicate = NodeFactory.createURI(SpatialExtension.WITHIN_BOX_PROP);

        //Geometry and Envelope parameters
        float latMin = 0;
        float lonMin = 1;
        float latMax = 2;
        float lonMax = 3;
        int limit = 10;

        List<Node> objectNodes = Arrays.asList(NodeValue.makeFloat(latMin).asNode(), NodeValue.makeFloat(lonMin).asNode(), NodeValue.makeFloat(latMax).asNode(), NodeValue.makeFloat(lonMax).asNode(), NodeValue.makeInteger(limit).asNode(), NodeValue.makeBoolean(false).asNode());
        PropFuncArg object = new PropFuncArg(objectNodes);

        //Function arguments
        Literal geometry = ConvertLatLonBox.toLiteral(latMin, lonMin, latMax, lonMax);
        GeometryWrapper geometryWrapper = GeometryWrapper.extract(geometry);
        SearchEnvelope searchEnvelope = SearchEnvelope.build(geometryWrapper, SpatialIndexTestData.WGS_84_SRS_INFO);

        SpatialArguments expResult = new SpatialArguments(limit, geometryWrapper, searchEnvelope);
        SpatialArguments result = instance.extractObjectArguments(predicate, object, SpatialIndexTestData.WGS_84_SRS_INFO);

        //System.out.println("Exp: " + expResult);
        //System.out.println("Res: " + result);
        //assertEquals(expResult, result);
    }

    /**
     * Test of extractObjectArguments method, of class
     * GenericSpatialBoxPropertyFunction.
     */
    @Test(expected = ExprEvalException.class)
    public void testExtractObjectArguments_5args_pos0_fail() {
        System.out.println("extractObjectArguments_5args_pos0_fail");

        GenericSpatialBoxPropertyFunction instance = new WithinBoxPF();

        Node predicate = NodeFactory.createURI(SpatialExtension.WITHIN_BOX_PROP);

        //Geometry and Envelope parameters
        float latMin = 0;
        float lonMin = 1;
        float latMax = 2;
        float lonMax = 3;
        int limit = 10;

        List<Node> objectNodes = Arrays.asList(NodeValue.makeString("0").asNode(), NodeValue.makeFloat(lonMin).asNode(), NodeValue.makeFloat(latMax).asNode(), NodeValue.makeFloat(lonMax).asNode(), NodeValue.makeInteger(limit).asNode());
        PropFuncArg object = new PropFuncArg(objectNodes);

        //Function arguments
        Literal geometry = ConvertLatLonBox.toLiteral(latMin, lonMin, latMax, lonMax);
        GeometryWrapper geometryWrapper = GeometryWrapper.extract(geometry);
        SearchEnvelope searchEnvelope = SearchEnvelope.build(geometryWrapper, SpatialIndexTestData.WGS_84_SRS_INFO);

        SpatialArguments expResult = new SpatialArguments(limit, geometryWrapper, searchEnvelope);
        SpatialArguments result = instance.extractObjectArguments(predicate, object, SpatialIndexTestData.WGS_84_SRS_INFO);

        //System.out.println("Exp: " + expResult);
        //System.out.println("Res: " + result);
        //assertEquals(expResult, result);
    }

    /**
     * Test of extractObjectArguments method, of class
     * GenericSpatialBoxPropertyFunction.
     */
    @Test(expected = ExprEvalException.class)
    public void testExtractObjectArguments_5args_pos1_fail() {
        System.out.println("extractObjectArguments_5args_pos1_fail");

        GenericSpatialBoxPropertyFunction instance = new WithinBoxPF();

        Node predicate = NodeFactory.createURI(SpatialExtension.WITHIN_BOX_PROP);

        //Geometry and Envelope parameters
        float latMin = 0;
        float lonMin = 1;
        float latMax = 2;
        float lonMax = 3;
        int limit = 10;

        List<Node> objectNodes = Arrays.asList(NodeValue.makeFloat(latMin).asNode(), NodeValue.makeString("1").asNode(), NodeValue.makeFloat(latMax).asNode(), NodeValue.makeFloat(lonMax).asNode(), NodeValue.makeInteger(limit).asNode());
        PropFuncArg object = new PropFuncArg(objectNodes);

        //Function arguments
        Literal geometry = ConvertLatLonBox.toLiteral(latMin, lonMin, latMax, lonMax);
        GeometryWrapper geometryWrapper = GeometryWrapper.extract(geometry);
        SearchEnvelope searchEnvelope = SearchEnvelope.build(geometryWrapper, SpatialIndexTestData.WGS_84_SRS_INFO);

        SpatialArguments expResult = new SpatialArguments(limit, geometryWrapper, searchEnvelope);
        SpatialArguments result = instance.extractObjectArguments(predicate, object, SpatialIndexTestData.WGS_84_SRS_INFO);

        //System.out.println("Exp: " + expResult);
        //System.out.println("Res: " + result);
        //assertEquals(expResult, result);
    }

    /**
     * Test of extractObjectArguments method, of class
     * GenericSpatialBoxPropertyFunction.
     */
    @Test(expected = ExprEvalException.class)
    public void testExtractObjectArguments_5args_pos2_fail() {
        System.out.println("extractObjectArguments_5args_pos2_fail");

        GenericSpatialBoxPropertyFunction instance = new WithinBoxPF();

        Node predicate = NodeFactory.createURI(SpatialExtension.WITHIN_BOX_PROP);

        //Geometry and Envelope parameters
        float latMin = 0;
        float lonMin = 1;
        float latMax = 2;
        float lonMax = 3;
        int limit = 10;

        List<Node> objectNodes = Arrays.asList(NodeValue.makeFloat(latMin).asNode(), NodeValue.makeFloat(lonMin).asNode(), NodeValue.makeString("2").asNode(), NodeValue.makeFloat(lonMax).asNode(), NodeValue.makeInteger(limit).asNode());
        PropFuncArg object = new PropFuncArg(objectNodes);

        //Function arguments
        Literal geometry = ConvertLatLonBox.toLiteral(latMin, lonMin, latMax, lonMax);
        GeometryWrapper geometryWrapper = GeometryWrapper.extract(geometry);
        SearchEnvelope searchEnvelope = SearchEnvelope.build(geometryWrapper, SpatialIndexTestData.WGS_84_SRS_INFO);

        SpatialArguments expResult = new SpatialArguments(limit, geometryWrapper, searchEnvelope);
        SpatialArguments result = instance.extractObjectArguments(predicate, object, SpatialIndexTestData.WGS_84_SRS_INFO);

        //System.out.println("Exp: " + expResult);
        //System.out.println("Res: " + result);
        //assertEquals(expResult, result);
    }

    /**
     * Test of extractObjectArguments method, of class
     * GenericSpatialBoxPropertyFunction.
     */
    @Test(expected = ExprEvalException.class)
    public void testExtractObjectArguments_5args_pos3_fail() {
        System.out.println("extractObjectArguments_5args_pos3_fail");

        GenericSpatialBoxPropertyFunction instance = new WithinBoxPF();

        Node predicate = NodeFactory.createURI(SpatialExtension.WITHIN_BOX_PROP);

        //Geometry and Envelope parameters
        float latMin = 0;
        float lonMin = 1;
        float latMax = 2;
        float lonMax = 3;
        int limit = 10;

        List<Node> objectNodes = Arrays.asList(NodeValue.makeFloat(latMin).asNode(), NodeValue.makeFloat(lonMin).asNode(), NodeValue.makeFloat(latMax).asNode(), NodeValue.makeString("3").asNode(), NodeValue.makeInteger(limit).asNode());
        PropFuncArg object = new PropFuncArg(objectNodes);

        //Function arguments
        Literal geometry = ConvertLatLonBox.toLiteral(latMin, lonMin, latMax, lonMax);
        GeometryWrapper geometryWrapper = GeometryWrapper.extract(geometry);
        SearchEnvelope searchEnvelope = SearchEnvelope.build(geometryWrapper, SpatialIndexTestData.WGS_84_SRS_INFO);

        SpatialArguments expResult = new SpatialArguments(limit, geometryWrapper, searchEnvelope);
        SpatialArguments result = instance.extractObjectArguments(predicate, object, SpatialIndexTestData.WGS_84_SRS_INFO);

        //System.out.println("Exp: " + expResult);
        //System.out.println("Res: " + result);
        //assertEquals(expResult, result);
    }

    /**
     * Test of extractObjectArguments method, of class
     * GenericSpatialBoxPropertyFunction.
     */
    @Test(expected = ExprEvalException.class)
    public void testExtractObjectArguments_5args_pos4_fail() {
        System.out.println("extractObjectArguments_5args_pos4_fail");

        GenericSpatialBoxPropertyFunction instance = new WithinBoxPF();

        Node predicate = NodeFactory.createURI(SpatialExtension.WITHIN_BOX_PROP);

        //Geometry and Envelope parameters
        float latMin = 0;
        float lonMin = 1;
        float latMax = 2;
        float lonMax = 3;
        int limit = 10;

        List<Node> objectNodes = Arrays.asList(NodeValue.makeFloat(latMin).asNode(), NodeValue.makeFloat(lonMin).asNode(), NodeValue.makeFloat(latMax).asNode(), NodeValue.makeFloat(lonMax).asNode(), NodeValue.makeString("10").asNode());
        PropFuncArg object = new PropFuncArg(objectNodes);

        //Function arguments
        Literal geometry = ConvertLatLonBox.toLiteral(latMin, lonMin, latMax, lonMax);
        GeometryWrapper geometryWrapper = GeometryWrapper.extract(geometry);
        SearchEnvelope searchEnvelope = SearchEnvelope.build(geometryWrapper, SpatialIndexTestData.WGS_84_SRS_INFO);

        SpatialArguments expResult = new SpatialArguments(limit, geometryWrapper, searchEnvelope);
        SpatialArguments result = instance.extractObjectArguments(predicate, object, SpatialIndexTestData.WGS_84_SRS_INFO);

        //System.out.println("Exp: " + expResult);
        //System.out.println("Res: " + result);
        //assertEquals(expResult, result);
    }
}
