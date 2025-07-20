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

package org.apache.jena.fuseki.mod.geosparql;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.function.Predicate;

import org.junit.jupiter.api.Test;

import org.apache.jena.geosparql.implementation.vocabulary.SRS_URI;
import org.apache.jena.geosparql.spatial.SpatialIndexException;
import org.apache.jena.geosparql.spatial.index.v2.GeometryGenerator;
import org.apache.jena.geosparql.spatial.index.v2.GeometryGenerator.GeometryType;
import org.apache.jena.geosparql.spatial.index.v2.SpatialIndexLib;
import org.apache.jena.geosparql.spatial.index.v2.SpatialIndexPerGraph;
import org.apache.jena.geosparql.spatial.index.v2.SpatialIndexerComputation;
import org.apache.jena.graph.Graph;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.sparql.core.DatasetGraph;
import org.apache.jena.sparql.core.DatasetGraphFactory;
import org.apache.jena.sparql.core.Quad;
import org.locationtech.jts.geom.Envelope;

/** Test cases that check for whether the correct graphs are indexed - also when a user is only authorized to a certain subset of graphs. */
public class TestSpatialIndexerTasks {

    private static final Node graph(int i) {
        return NodeFactory.createURI("http://www.example.org/graph" + i);
    }

    private static final Node dg = Quad.defaultGraphIRI;
    private static final Node g1 = graph(1);
    private static final Node g2 = graph(2);
    private static final Node g3 = graph(3);
    private static final Node g4 = graph(4);

    private static final Set<Node> allGraphs = Set.of(Quad.defaultGraphIRI, g1, g2, g3, g4);

    /** Test data: spatial data in 1 default graph (dg) and 4 named graphs (g1, g2, g3, 4) */
    private static DatasetGraph createTestData() {
        Map<GeometryType, Number> conf = GeometryGenerator.createConfig(1);
        DatasetGraph dsg = DatasetGraphFactory.create();
        for (int i = 0; i < 5; ++i) {
            Node graph = i == 0
                ? dg
                : graph(i);
            Graph g = dsg.getGraph(graph);
            Envelope env = new Envelope(-175, 175, -85, 85);
            GeometryGenerator.generateGraph(g, env, conf);
        }
        return dsg;
    }

    /** Helper method: Replace a prior index with the given one. Returns the new spatial index. */
    private static SpatialIndexPerGraph replaceSpatialIndex(DatasetGraph dsg, Predicate<Node> isAuthorizedGraph, String srs, Collection<Node> replaceGraphs) {
        List<Node> graphNodes = List.copyOf(replaceGraphs);
        SpatialIndexerComputation computation = new SpatialIndexerComputation(dsg, srs, graphNodes, 1);
        SpatialIndexLib.createIndexerTask(dsg, isAuthorizedGraph, computation, null, null, true).run();
        SpatialIndexPerGraph spatialIndex = SpatialIndexLib.getSpatialIndex(dsg);
        SpatialIndexLib.setSpatialIndex(dsg, spatialIndex);

        // Access the spatial index via context to test the machinery.
        SpatialIndexPerGraph result = SpatialIndexLib.getSpatialIndex(dsg);
        return result;
    }

    /** Helper method: Get or create a spatial index and update the requested graphs in it. Returns the new spatial index. */
    private static SpatialIndexPerGraph updateSpatialIndex(DatasetGraph dsg, Predicate<Node> isAuthorizedGraph, String srs, Collection<Node> updateGraphs) {
        List<Node> graphNodes = List.copyOf(updateGraphs);
        SpatialIndexerComputation computation = new SpatialIndexerComputation(dsg, srs, graphNodes, 1);
        SpatialIndexLib.createIndexerTask(dsg, isAuthorizedGraph, computation, null, null, false).run();
        SpatialIndexPerGraph spatialIndex = SpatialIndexLib.getSpatialIndex(dsg);
        return spatialIndex;
    }

    /** Helper method: run a clean task and return the resulting index from the context. */
    private static SpatialIndexPerGraph clean(DatasetGraph dsg, Predicate<Node> isAuthorizedGraph) {
        SpatialIndexLib.createCleanTask(dsg, isAuthorizedGraph, null).run();
        SpatialIndexPerGraph spatialIndex = SpatialIndexLib.getSpatialIndex(dsg);
        return spatialIndex;
    }

    @Test
    public void testDsgIndexUpdate() throws SpatialIndexException {
        DatasetGraph dsg = createTestData();

        Set<Node> initialGraphs = Set.of(g2, g3, g4);
        Set<Node> updateGraphs = Set.of(dg, g1);

        replaceSpatialIndex(dsg, null, SRS_URI.DEFAULT_WKT_CRS84, initialGraphs);
        SpatialIndexPerGraph spatialIndex = updateSpatialIndex(dsg, null, SRS_URI.DEFAULT_WKT_CRS84, updateGraphs);

        Set<Node> indexedGraphNodes = spatialIndex.getIndex().getTreeMap().keySet();
        assertEquals(allGraphs, indexedGraphNodes);
    }

    @Test
    public void testDsgIndexReplace() throws SpatialIndexException {
        DatasetGraph dsg = createTestData();

        SpatialIndexLib.buildSpatialIndex(dsg, SRS_URI.DEFAULT_WKT_CRS84);

        Set<Node> expectedGraphSet = Set.of(g1, g2);
        SpatialIndexPerGraph spatialIndex = replaceSpatialIndex(dsg, null, SRS_URI.DEFAULT_WKT_CRS84, expectedGraphSet);

        Set<Node> actualGraphSet = spatialIndex.getIndex().getTreeMap().keySet();
        assertEquals(expectedGraphSet, actualGraphSet);
    }

    @Test
    public void testDsgIndexClean() throws InterruptedException, ExecutionException, SpatialIndexException {
        DatasetGraph dsg = createTestData();

        SpatialIndexLib.buildSpatialIndex(dsg, SRS_URI.DEFAULT_WKT_CRS84);

        Set.of(dg, g1, g2, g3).forEach(dsg::removeGraph);

        SpatialIndexPerGraph spatialIndex = clean(dsg, null);
        Set<Node> actual = spatialIndex.getIndex().getTreeMap().keySet();
        Set<Node> expected = Set.of(dg, g4);;
        assertEquals(expected, actual);
    }

    @Test
    public void testProtectedDsgIndexUpdate() throws SpatialIndexException {
        DatasetGraph dsg = createTestData();

        // User can only see g3 and g4 and requests to update both of them.
        Set<Node> authorizedGraph = Set.of(g3, g4);
        Set<Node> initialGraphs = Set.of(dg, g1, g2, g3);
        Set<Node> updateGraphs = Set.of(g3, g4);

        replaceSpatialIndex(dsg, null, SRS_URI.DEFAULT_WKT_CRS84, initialGraphs);
        SpatialIndexPerGraph spatialIndex = updateSpatialIndex(dsg, authorizedGraph::contains, SRS_URI.DEFAULT_WKT_CRS84, updateGraphs);

        Set<Node> indexedGraphNodes = spatialIndex.getIndex().getTreeMap().keySet();
        assertEquals(allGraphs, indexedGraphNodes);
    }

    @Test
    public void testProtectedDsgIndexReplace() {
        DatasetGraph dsg = createTestData();

        // User can only see g3 and g4 and requests to replace index with only g4 - so g3 should get lost.
        Set<Node> authorizedGraphs = Set.of(g3, g4);

        Set<Node> initialGraphs = Set.of(dg, g1, g2, g3);
        Set<Node> replaceGraphs = Set.of(g4);
        Set<Node> expectedGraphs = Set.of(dg, g1, g2, g4);

        replaceSpatialIndex(dsg, null, SRS_URI.DEFAULT_WKT_CRS84, initialGraphs);
        SpatialIndexPerGraph spatialIndex = replaceSpatialIndex(dsg, authorizedGraphs::contains, SRS_URI.DEFAULT_WKT_CRS84, replaceGraphs);

        Set<Node> indexedGraphNodes = spatialIndex.getIndex().getTreeMap().keySet();
        assertEquals(expectedGraphs, indexedGraphNodes);
    }

    @Test
    public void testProtectedDsgCleanAuthorized() throws SpatialIndexException {
        DatasetGraph dsg = createTestData();

        // User is authorized for g3 and g4 - but g4 gets removed -> g4 should become removed.
        Set<Node> authorizedGraphs = Set.of(g3, g4);

        SpatialIndexLib.buildSpatialIndex(dsg, SRS_URI.DEFAULT_WKT_CRS84);
        Set.of(dg, g4).forEach(dsg::removeGraph);

        SpatialIndexPerGraph spatialIndex = clean(dsg, authorizedGraphs::contains);
        Set<Node> actual = spatialIndex.getIndex().getTreeMap().keySet();
        Set<Node> expected = Set.of(dg, g1, g2, g3);
        assertEquals(expected, actual);
    }

    @Test
    public void testProtectedDsgCleanUnauthorized() throws SpatialIndexException {
        DatasetGraph dsg = createTestData();
        SpatialIndexLib.buildSpatialIndex(dsg, SRS_URI.DEFAULT_WKT_CRS84);
        Set.of(dg, g1, g3).forEach(dsg::removeGraph);

        // Among the authorized graphs, only g4 has been removed and can thus be cleaned up.
        Set<Node> authorizedGraphs = Set.of(g3, g4);

        SpatialIndexPerGraph spatialIndex = clean(dsg, authorizedGraphs::contains);
        Set<Node> actual = spatialIndex.getIndex().getTreeMap().keySet();
        Set<Node> expected = Set.of(dg, g1, g2, g4);
        assertEquals(expected, actual);
    }
}
