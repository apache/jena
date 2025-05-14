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

import java.lang.invoke.MethodHandles;
import java.nio.file.Path;
import java.util.Collection;
import java.util.Objects;
import java.util.Set;

import org.apache.jena.geosparql.implementation.SRSInfo;
import org.apache.jena.geosparql.implementation.registry.SRSRegistry;
import org.apache.jena.geosparql.implementation.vocabulary.SRS_URI;
import org.apache.jena.geosparql.spatial.SpatialIndex;
import org.apache.jena.geosparql.spatial.SpatialIndexException;
import org.apache.jena.graph.Graph;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.query.TxnType;
import org.apache.jena.sparql.core.DatasetGraph;
import org.apache.jena.system.AutoTxn;
import org.apache.jena.system.Txn;
import org.locationtech.jts.geom.Envelope;
import org.locationtech.jts.index.strtree.STRtree;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * SpatialIndex for testing bounding box collisions between geometries within a
 * Dataset.<br>
 * Queries must be performed using the same SRS URI as the SpatialIndex.<br>
 * The SpatialIndex is added to the Dataset Context when it is built.<br>
 * QueryRewriteIndex is also stored in the SpatialIndex as its content is
 * Dataset specific.
 *
 */
public class SpatialIndexPerGraph implements SpatialIndex {

    private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    private transient final SRSInfo srsInfo;
    private STRtreePerGraph index;
    private Path location;

    public SpatialIndexPerGraph(STRtreePerGraph index) {
        this(SRS_URI.DEFAULT_WKT_CRS84, index, null);
    }

    public SpatialIndexPerGraph(String srsUri, STRtreePerGraph index, Path location) {
        this(
            SRSRegistry.getSRSInfo(srsUri),
            index,
            null);
    }

    public SpatialIndexPerGraph(SRSInfo srsInfo, STRtreePerGraph index, Path location) {
        super();
        this.srsInfo = Objects.requireNonNull(srsInfo);
        this.index = Objects.requireNonNull(index);
        this.location = location;
    }

    public STRtreePerGraph getIndex() {
        return index;
    }

    /**
     *
     * @return Information about the SRS used by the SpatialIndex.
     */
    @Override
    public SRSInfo getSrsInfo() {
        return srsInfo;
    }

    /**
     *
     * @return True if the SpatialIndex is empty.
     */
    @Override
    public boolean isEmpty() {
        return index.isEmpty();
    }

    /**
     * Returns the number of items in the index.
     */
    @Override
    public long getSize() {
        return index.size();
    }

    @Override
    public Collection<Node> query(Envelope searchEnvelope, Node graph) {
        return index.queryOneGraph(searchEnvelope, graph);
    }

    @Override
    public Path getLocation() {
        return location;
    }

    @Override
    public void setLocation(Path location) {
        this.location = location;
    }

    @Override
    public String toString() {
        return "SpatialIndex{" + "srsInfo=" + srsInfo + ", index=" + index + ", file=" + location + '}';
    }

    /**
     * Recompute and replace the spatial index trees for the given named graphs.
     *
     * @param index   the spatial index to modify
     * @param datasetGraph the dataset containing the named graphs
     * @param graphNames  the named graphs
     * @throws SpatialIndexException
     */
    public static void recomputeIndexForGraphs(SpatialIndexPerGraph index,
                                                       DatasetGraph datasetGraph,
                                                       Set<String> graphNames) throws SpatialIndexException {
        STRtreePerGraph trees = index.getIndex();
        try (AutoTxn txn = Txn.autoTxn(datasetGraph, TxnType.READ)) {
            for (String graphName : graphNames) {
                Node g = graphName == null ? null : NodeFactory.createURI(graphName);
                if (trees.contains(g)) {
                    LOGGER.info("recomputing spatial index for graph: {}", graphName);
                } else {
                    LOGGER.info("computing spatial index for graph: {}", graphName);
                }
                Graph namedGraph = datasetGraph.getGraph(g);
                STRtree indexTree = STRtreeUtils.buildSpatialIndexTree(namedGraph, index.getSrsInfo().getSrsURI());
                STRtree oldIndexTree = trees.setTree(g, indexTree);
                if (oldIndexTree != null) {
                    LOGGER.info("replaced spatial index for graph: {}", graphName);
                } else {
                    LOGGER.info("added spatial index for graph: {}", graphName);
                }
            }
            txn.commit();
        }
    }
}
