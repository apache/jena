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
package org.apache.jena.geosparql.spatial.index.v2;

import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.jena.atlas.iterator.Iter;
import org.apache.jena.atlas.iterator.IteratorCloseable;
import org.apache.jena.geosparql.spatial.SpatialIndexException;
import org.apache.jena.geosparql.spatial.SpatialIndexFindUtils;
import org.apache.jena.geosparql.spatial.SpatialIndexItem;
import org.apache.jena.graph.Graph;
import org.apache.jena.graph.Node;
import org.apache.jena.sparql.core.DatasetGraph;
import org.apache.jena.sparql.core.Quad;
import org.locationtech.jts.index.strtree.STRtree;

public class STRtreeUtils {
    static final int MINIMUM_CAPACITY = 2;

    public static STRtree buildSpatialIndexTree(Graph graph, String srsURI) throws SpatialIndexException {
        try {
            STRtree tree;
            IteratorCloseable<SpatialIndexItem> it = SpatialIndexFindUtils.findIndexItems(graph, srsURI);
            try {
                tree = buildSpatialIndexTree(it);
            } finally {
                it.close();
            }
            return tree;
        } catch (Throwable e) {
            throw new SpatialIndexException("Spatial index construction failed.", e);
        }
    }

    // XXX This method overlaps function-wise with SpatialIndexerComputation. Consolidate?
    public static STRtreePerGraph buildSpatialIndexTree(DatasetGraph datasetGraph, String srsURI) throws SpatialIndexException {
        Map<Node, STRtree> treeMap = new LinkedHashMap<>();

        // Process default graph.
        // LOGGER.info("building spatial index for default graph ...");
        Graph defaultGraph = datasetGraph.getDefaultGraph();
        STRtree defaultGraphTree = buildSpatialIndexTree(defaultGraph, srsURI);
        treeMap.put(Quad.defaultGraphIRI, defaultGraphTree);

        // Process named graphs.
        Iterator<Node> graphIter = datasetGraph.listGraphNodes();
        try {
            while (graphIter.hasNext()) {
                Node graphNode = graphIter.next();
                // LOGGER.info("building spatial index for graph {} ...", graphNode);
                Graph namedGraph = datasetGraph.getGraph(graphNode);
                treeMap.put(graphNode, buildSpatialIndexTree(namedGraph, srsURI));
            }
        } finally {
            Iter.close(graphIter);
        }

        return new STRtreePerGraph(treeMap);
    }

    /**
     * Create an STRtree from the elements of the given iterator.
     * It's the caller's responsibility to close the iterator if needed.
     */
    public static STRtree buildSpatialIndexTree(Iterator<SpatialIndexItem> it) throws SpatialIndexException {
        // Collecting items into a list in order to assist tree construction with a known size.
        List<SpatialIndexItem> items = Iter.toList(it);
        STRtree tree = buildSpatialIndexTree(items);
        return tree;
    }

    public static STRtree buildSpatialIndexTree(Collection<SpatialIndexItem> items) throws SpatialIndexException {
        STRtree tree = new STRtree(Math.max(MINIMUM_CAPACITY, items.size()));
        addToTree(tree, items.iterator());
        tree.build();
        return tree;
    }

    /**
     * Accumulate the elements of the given iterator into an existing (unbuilt) STRtree.
     * It's the caller's responsibility to close the iterator if needed.
     */
    public static void addToTree(STRtree treeAcc, Iterator<SpatialIndexItem> it) throws SpatialIndexException {
        it.forEachRemaining(item -> treeAcc.insert(item.getEnvelope(), item.getItem()));
    }
}
