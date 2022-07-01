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

import org.apache.jena.datatypes.DatatypeFormatException;
import org.apache.jena.datatypes.xsd.XSDDatatype;
import static org.apache.jena.geosparql.geo.topological.QueryRewriteTestData.FEATURE_B;
import static org.apache.jena.geosparql.geo.topological.QueryRewriteTestData.GEOMETRY_B;
import static org.apache.jena.geosparql.geo.topological.QueryRewriteTestData.GEO_FEATURE_LITERAL;
import static org.apache.jena.geosparql.geo.topological.QueryRewriteTestData.GEO_FEATURE_Y;
import static org.apache.jena.geosparql.geo.topological.QueryRewriteTestData.LITERAL_B;
import org.apache.jena.geosparql.implementation.vocabulary.SpatialExtension;
import org.apache.jena.graph.Graph;
import org.apache.jena.graph.Node;
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
public class SpatialObjectGeometryLiteralTest {

    private static Model MODEL;

    public SpatialObjectGeometryLiteralTest() {
    }

    @BeforeClass
    public static void setUpClass() {
        MODEL = QueryRewriteTestData.createTestData();
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
     * Test of retrieve method, of class SpatialObjectGeometryLiteral.
     */
    @Test
    public void testRetrieve() {

        Graph graph = MODEL.getGraph();
        Node targetSpatialObject = null;
        SpatialObjectGeometryLiteral instance = SpatialObjectGeometryLiteral.retrieve(graph, targetSpatialObject);

        boolean expResult = false;
        boolean result = instance.isValid();
        assertEquals(expResult, result);
    }

    /**
     * Test of retrieve method, of class SpatialObjectGeometryLiteral.
     */
    @Test
    public void testRetrieveGeometryLiteral_geometry() {

        Graph graph = MODEL.getGraph();
        Resource targetSpatialObject = GEOMETRY_B;
        SpatialObjectGeometryLiteral expResult = new SpatialObjectGeometryLiteral(GEOMETRY_B.asNode(), LITERAL_B.asNode());
        SpatialObjectGeometryLiteral result = SpatialObjectGeometryLiteral.retrieve(graph, targetSpatialObject.asNode());
        assertEquals(expResult, result);
    }

    /**
     * Test of retrieve method, of class SpatialObjectGeometryLiteral.
     */
    @Test
    public void testRetrieveGeometryLiteral_feature() {

        Resource targetSpatialObject = FEATURE_B;
        SpatialObjectGeometryLiteral expResult = new SpatialObjectGeometryLiteral(FEATURE_B.asNode(), LITERAL_B.asNode());
        SpatialObjectGeometryLiteral result = SpatialObjectGeometryLiteral.retrieve(MODEL.getGraph(), targetSpatialObject.asNode());
        assertEquals(expResult, result);
    }

    /**
     * Test of retrieve method, of class SpatialObjectGeometryLiteral.
     */
    @Test
    public void testRetrieveGeometryLiteral_missing_property() {

        Resource targetSpatialObject = ResourceFactory.createResource("http://example.org#GeometryE");

        SpatialObjectGeometryLiteral instance = SpatialObjectGeometryLiteral.retrieve(MODEL.getGraph(), targetSpatialObject.asNode());

        boolean expResult = false;
        boolean result = instance.isValid();
        assertEquals(expResult, result);
    }

    /**
     * Test of retrieve method, of class SpatialObjectGeometryLiteral.
     */
    @Test
    public void testRetrieveGeometryLiteral_not_feature_geometry() {

        Resource targetSpatialObject = ResourceFactory.createResource("http://example.org#X");

        SpatialObjectGeometryLiteral instance = SpatialObjectGeometryLiteral.retrieve(MODEL.getGraph(), targetSpatialObject.asNode());

        boolean expResult = false;
        boolean result = instance.isValid();
        assertEquals(expResult, result);
    }

    /**
     * Test of retrieve method, of class SpatialObjectGeometryLiteral.
     */
    @Test
    public void testRetrieveGeometryLiteral_feature_lat_lon() {

        Resource targetSpatialObject = GEO_FEATURE_Y;
        SpatialObjectGeometryLiteral expResult = new SpatialObjectGeometryLiteral(GEO_FEATURE_Y.asNode(), GEO_FEATURE_LITERAL.asNode());
        SpatialObjectGeometryLiteral result = SpatialObjectGeometryLiteral.retrieve(MODEL.getGraph(), targetSpatialObject.asNode());
        assertEquals(expResult, result);
    }

    /**
     * Test of retrieve method, of class SpatialObjectGeometryLiteral for
     * multiple geo:lat properties.
     */
    @Test(expected = DatatypeFormatException.class)
    public void testRetrieveGeometryLiteral_multiple_lat() {

        Model model = ModelFactory.createDefaultModel();
        Resource feature = model.createResource("http://example.org#GeoFeatureX");
        feature.addLiteral(SpatialExtension.GEO_LAT_PROP, ResourceFactory.createTypedLiteral("0.0", XSDDatatype.XSDfloat));
        feature.addLiteral(SpatialExtension.GEO_LAT_PROP, ResourceFactory.createTypedLiteral("1.0", XSDDatatype.XSDfloat));
        feature.addLiteral(SpatialExtension.GEO_LON_PROP, ResourceFactory.createTypedLiteral("60.0", XSDDatatype.XSDfloat));
        SpatialObjectGeometryLiteral.retrieve(model.getGraph(), feature.asNode());
    }

    /**
     * Test of retrieve method, of class SpatialObjectGeometryLiteral for
     * multiple geo:lon properties.
     */
    @Test(expected = DatatypeFormatException.class)
    public void testRetrieveGeometryLiteral_multiple_lon() {

        Model model = ModelFactory.createDefaultModel();
        Resource feature = model.createResource("http://example.org#GeoFeatureX");
        feature.addLiteral(SpatialExtension.GEO_LAT_PROP, ResourceFactory.createTypedLiteral("0.0", XSDDatatype.XSDfloat));
        feature.addLiteral(SpatialExtension.GEO_LON_PROP, ResourceFactory.createTypedLiteral("60.0", XSDDatatype.XSDfloat));
        feature.addLiteral(SpatialExtension.GEO_LON_PROP, ResourceFactory.createTypedLiteral("70.0", XSDDatatype.XSDfloat));
        SpatialObjectGeometryLiteral.retrieve(model.getGraph(), feature.asNode());
    }

    /**
     * Test of retrieve method, of class SpatialObjectGeometryLiteral for
     * multiple geo:lat and geo:lon properties.
     */
    @Test(expected = DatatypeFormatException.class)
    public void testRetrieveGeometryLiteral_multiple_lat_lon() {

        Model model = ModelFactory.createDefaultModel();
        Resource feature = model.createResource("http://example.org#GeoFeatureX");
        feature.addLiteral(SpatialExtension.GEO_LAT_PROP, ResourceFactory.createTypedLiteral("0.0", XSDDatatype.XSDfloat));
        feature.addLiteral(SpatialExtension.GEO_LAT_PROP, ResourceFactory.createTypedLiteral("1.0", XSDDatatype.XSDfloat));
        feature.addLiteral(SpatialExtension.GEO_LON_PROP, ResourceFactory.createTypedLiteral("60.0", XSDDatatype.XSDfloat));
        feature.addLiteral(SpatialExtension.GEO_LON_PROP, ResourceFactory.createTypedLiteral("70.0", XSDDatatype.XSDfloat));
        SpatialObjectGeometryLiteral.retrieve(model.getGraph(), feature.asNode());
    }
}
