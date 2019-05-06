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

import org.apache.jena.datatypes.xsd.XSDDatatype;
import org.apache.jena.geosparql.geo.topological.property_functions.geometry_property.CoordinateDimensionPF;
import org.apache.jena.geosparql.implementation.datatype.WKTDatatype;
import org.apache.jena.geosparql.implementation.vocabulary.Geo;
import org.apache.jena.graph.Graph;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.ResourceFactory;
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
public class GenericGeometryPropertyFunctionTest {

    private static final Model MODEL = ModelFactory.createDefaultModel();
    private static final Resource GEOMETRY_A = ResourceFactory.createResource("http://example.org#GeometryA");
    private static final Resource GEOMETRY_B = ResourceFactory.createResource("http://example.org#GeometryB");
    private static final Resource GEOMETRY_C = ResourceFactory.createResource("http://example.org#GeometryC");
    private static final Resource GEOMETRY_D_BLANK = ResourceFactory.createResource();

    public GenericGeometryPropertyFunctionTest() {
    }

    @BeforeClass
    public static void setUpClass() {
        MODEL.add(GEOMETRY_A, Geo.HAS_SERIALIZATION_PROP, ResourceFactory.createTypedLiteral("<http://www.opengis.net/def/crs/EPSG/0/27700> POINT(11 11)", WKTDatatype.INSTANCE));
        MODEL.add(GEOMETRY_C, Geo.COORDINATE_DIMENSION_PROP, ResourceFactory.createTypedLiteral("3", XSDDatatype.XSDinteger));
        MODEL.add(GEOMETRY_D_BLANK, Geo.HAS_SERIALIZATION_PROP, ResourceFactory.createTypedLiteral("<http://www.opengis.net/def/crs/EPSG/0/27700> POINT Z(5 5 5)", WKTDatatype.INSTANCE));
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
     * Test of getGeometryLiteral method, of class
     * GenericGeometryPropertyFunction.
     */
    @Test
    public void testGetGeometryLiteral() {
        System.out.println("getGeometryLiteral");
        Graph graph = MODEL.getGraph();
        Node subject = GEOMETRY_A.asNode();
        Node predicate = Geo.COORDINATE_DIMENSION_NODE;

        GenericGeometryPropertyFunction instance = new CoordinateDimensionPF();
        Node expResult = NodeFactory.createLiteral("2", XSDDatatype.XSDinteger);
        Node result = instance.getGeometryLiteral(subject, predicate, graph);

        //System.out.println("Exp: " + expResult);
        //System.out.println("Res: " + result);
        assertEquals(expResult, result);
    }

    /**
     * Test of getGeometryLiteral method, of class
     * GenericGeometryPropertyFunction.
     */
    @Test
    public void testGetGeometryLiteral_false() {
        System.out.println("getGeometryLiteral_false");
        Graph graph = MODEL.getGraph();
        Node subject = GEOMETRY_B.asNode();
        Node predicate = Geo.COORDINATE_DIMENSION_NODE;

        GenericGeometryPropertyFunction instance = new CoordinateDimensionPF();
        Node expResult = null;
        Node result = instance.getGeometryLiteral(subject, predicate, graph);

        //System.out.println("Exp: " + expResult);
        //System.out.println("Res: " + result);
        assertEquals(expResult, result);
    }

    /**
     * Test of getGeometryLiteral method, of class
     * GenericGeometryPropertyFunction.
     */
    @Test
    public void testGetGeometryLiteral_asserted() {
        System.out.println("getGeometryLiteral_asserted");
        Graph graph = MODEL.getGraph();
        Node subject = GEOMETRY_C.asNode();
        Node predicate = Geo.COORDINATE_DIMENSION_NODE;

        GenericGeometryPropertyFunction instance = new CoordinateDimensionPF();
        Node expResult = NodeFactory.createLiteral("3", XSDDatatype.XSDinteger);
        Node result = instance.getGeometryLiteral(subject, predicate, graph);

        //System.out.println("Exp: " + expResult);
        //System.out.println("Res: " + result);
        assertEquals(expResult, result);
    }

    /**
     * Test of getGeometryLiteral method, of class
     * GenericGeometryPropertyFunction.
     */
    @Test
    public void testGetGeometryLiteral_blank() {
        System.out.println("getGeometryLiteral_blank");
        Graph graph = MODEL.getGraph();
        Node subject = GEOMETRY_D_BLANK.asNode();
        Node predicate = Geo.COORDINATE_DIMENSION_NODE;

        GenericGeometryPropertyFunction instance = new CoordinateDimensionPF();
        Node expResult = NodeFactory.createLiteral("3", XSDDatatype.XSDinteger);
        Node result = instance.getGeometryLiteral(subject, predicate, graph);

        //System.out.println("Exp: " + expResult);
        //System.out.println("Res: " + result);
        assertEquals(expResult, result);
    }

}
