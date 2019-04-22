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
package org.apache.jena.geosparql.spatial.property_functions.cardinal;

import org.apache.jena.geosparql.spatial.property_functions.cardinal.NorthPF;
import org.apache.jena.geosparql.implementation.GeometryWrapper;
import org.apache.jena.geosparql.implementation.vocabulary.SpatialExtension;
import org.apache.jena.geosparql.spatial.ConvertLatLon;
import org.apache.jena.geosparql.spatial.SpatialIndexTestData;
import org.apache.jena.geosparql.spatial.property_functions.SpatialArguments;
import java.util.Arrays;
import java.util.List;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.rdf.model.Literal;
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
public class GenericCardinalGeomPropertyFunctionTest {

    public GenericCardinalGeomPropertyFunctionTest() {
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
     * Test of checkSecondFilter method, of class
     * GenericCardinalGeomPropertyFunction.
     */
    @Test
    public void testCheckSecondFilter() {
        System.out.println("checkSecondFilter");

        //Property Function
        Node predicate = NodeFactory.createURI(SpatialExtension.NORTH_GEOM_PROP);

        //Geometry and Envelope parameters
        float lat = 0;
        float lon = 1;
        Literal targetGeometry = ConvertLatLon.toLiteral(lat, lon);

        List<Node> objectNodes = Arrays.asList(NodeValue.makeFloat(lat).asNode(), NodeValue.makeFloat(lon).asNode());
        PropFuncArg object = new PropFuncArg(objectNodes);

        GeometryWrapper targetGeometryWrapper = GeometryWrapper.extract(targetGeometry);
        NorthPF instance = new NorthPF();
        SpatialArguments spatialArgumemts = instance.extractObjectArguments(predicate, object, SpatialIndexTestData.WGS_84_SRS_INFO);

        boolean expResult = true;
        boolean result = instance.checkSecondFilter(spatialArgumemts, targetGeometryWrapper);

        //System.out.println("Exp: " + expResult);
        //System.out.println("Res: " + result);
        assertEquals(expResult, result);
    }

    /**
     * Test of checkSecondFilter method, of class
     * GenericCardinalGeomPropertyFunction.
     */
    @Test
    public void testCheckSecondFilter_fail() {
        System.out.println("checkSecondFilter_fail");

        //Property Function
        Node predicate = NodeFactory.createURI(SpatialExtension.NORTH_GEOM_PROP);

        //Geometry and Envelope parameters
        float lat = 0;
        float lon = 1;
        Literal targetGeometry = ConvertLatLon.toLiteral(lat - 10f, lon);

        List<Node> objectNodes = Arrays.asList(NodeValue.makeFloat(lat).asNode(), NodeValue.makeFloat(lon).asNode());
        PropFuncArg object = new PropFuncArg(objectNodes);

        GeometryWrapper targetGeometryWrapper = GeometryWrapper.extract(targetGeometry);
        NorthPF instance = new NorthPF();
        SpatialArguments spatialArgumemts = instance.extractObjectArguments(predicate, object, SpatialIndexTestData.WGS_84_SRS_INFO);

        boolean expResult = false;
        boolean result = instance.checkSecondFilter(spatialArgumemts, targetGeometryWrapper);

        //System.out.println("Exp: " + expResult);
        //System.out.println("Res: " + result);
        assertEquals(expResult, result);
    }

}
