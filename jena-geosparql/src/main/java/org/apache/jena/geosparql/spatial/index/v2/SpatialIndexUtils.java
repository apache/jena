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

import org.apache.jena.geosparql.configuration.GeoSPARQLOperations;
import org.apache.jena.geosparql.implementation.SRSInfo;
import org.apache.jena.geosparql.implementation.registry.SRSRegistry;
import org.apache.jena.geosparql.spatial.SpatialIndex;
import org.apache.jena.geosparql.spatial.SpatialIndexException;
import org.apache.jena.graph.Graph;
import org.apache.jena.graph.Node;
import org.apache.jena.query.Dataset;
import org.apache.jena.query.DatasetFactory;
import org.apache.jena.query.TxnType;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.sparql.core.DatasetGraph;
import org.apache.jena.sparql.core.NamedGraph;
import org.apache.jena.sparql.engine.ExecutionContext;
import org.apache.jena.sparql.util.Context;
import org.apache.jena.sparql.util.Symbol;
import org.apache.jena.system.AutoTxn;
import org.apache.jena.system.Txn;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** ARQ-level utils for Dataset and Context. */
public class SpatialIndexUtils {
    private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    public static final Symbol SPATIAL_INDEX_SYMBOL = Symbol.create("http://jena.apache.org/spatial#index");

    /** Symbol for a running task in a dataset's context. */
    public static final Symbol SPATIAL_INDEX_TASK_SYMBOL = Symbol.create("http://jena.apache.org/spatial#indexTask");
    public static final Symbol symSrsUri = Symbol.create("http://jena.apache.org/spatial#srsURI");

    /**
     * Set the SpatialIndex into the Context of the Dataset for later retrieval
     * and use in spatial functions.
     *
     * @param dataset
     * @param spatialIndex
     */
    public static final void setSpatialIndex(Dataset dataset, SpatialIndex spatialIndex) {
        Context cxt = dataset.getContext();
        setSpatialIndex(cxt, spatialIndex);
    }

    public static final void setSpatialIndex(DatasetGraph datasetGraph, SpatialIndex spatialIndex) {
        Context cxt = datasetGraph.getContext();
        setSpatialIndex(cxt, spatialIndex);
    }

    public static final void setSpatialIndex(Context context, SpatialIndex spatialIndex) {
        context.set(SPATIAL_INDEX_SYMBOL, spatialIndex);
    }

    public static final SpatialIndex getSpatialIndex(Context cxt) {
        return cxt.get(SPATIAL_INDEX_SYMBOL);
    }

    /**
     *
     * @param execCxt
     * @return True if a SpatialIndex is defined in the ExecutionContext.
     */
    public static final boolean isDefined(ExecutionContext execCxt) {
        Context context = execCxt.getContext();
        return context.isDefined(SPATIAL_INDEX_SYMBOL);
    }

    /**
     * Retrieve the SpatialIndex from the Context.
     *
     * @param execCxt
     * @return SpatialIndex contained in the Context.
     * @throws SpatialIndexException
     */
    public static final SpatialIndex retrieve(ExecutionContext execCxt) throws SpatialIndexException {
        Context context = execCxt.getContext();
        SpatialIndex spatialIndex = (SpatialIndex) context.get(SPATIAL_INDEX_SYMBOL, null);
        if (spatialIndex == null) {
            throw new SpatialIndexException("Dataset Context does not contain SpatialIndex.");
        }
        return spatialIndex;
    }

    /**
     * Wrap Model in a Dataset and build SpatialIndex.
     *
     * @param model
     * @param srsURI
     * @return Dataset with default Model and SpatialIndex in Context.
     * @throws SpatialIndexException
     */
    public static final Dataset wrapModel(Model model, String srsURI) throws SpatialIndexException {
        Dataset dataset = DatasetFactory.createTxnMem();
        dataset.setDefaultModel(model);
        buildSpatialIndex(dataset.asDatasetGraph(), srsURI);
        return dataset;
    }

    /**
     * Wrap Model in a Dataset and build SpatialIndex.
     *
     * @param model
     * @return Dataset with default Model and SpatialIndex in Context.
     * @throws SpatialIndexException
     */
    public static final Dataset wrapModel(Model model) throws SpatialIndexException {
        Dataset dataset = DatasetFactory.createTxnMem();
        dataset.setDefaultModel(model);
        String srsURI = GeoSPARQLOperations.findModeSRS(dataset);
        buildSpatialIndex(dataset.asDatasetGraph(), srsURI);
        return dataset;
    }

    /**
     * Build Spatial Index from all graphs in Dataset.<br>
     * Dataset contains SpatialIndex in Context.<br>
     * SRS URI based on most frequent found in Dataset.
     *
     * @param datasetGraph
     * @return SpatialIndex constructed.
     * @throws SpatialIndexException
     */
    public static SpatialIndex buildSpatialIndex(DatasetGraph datasetGraph) throws SpatialIndexException {
        // XXX Dataset wrapping due to legacy code
        Dataset dataset = DatasetFactory.wrap(datasetGraph);
        String srsURI = GeoSPARQLOperations.findModeSRS(dataset);
        SpatialIndex spatialIndex = buildSpatialIndex(datasetGraph, srsURI);
        return spatialIndex;
    }

    /**
     * Build Spatial Index from all graphs in Dataset.<br>
     * Dataset contains SpatialIndex in Context.
     *
     * @param datasetGraph
     * @param srsURI
     * @return SpatialIndex constructed.
     * @throws SpatialIndexException
     */
    public static SpatialIndexPerGraph buildSpatialIndex(DatasetGraph datasetGraph, String srsURI) throws SpatialIndexException {
        return buildSpatialIndexPerGraph(datasetGraph, srsURI);
    }

//    public static SpatialIndexPerGraph buildSpatialIndex(DatasetGraph datasetGraph, String srsURI, boolean indexTreePerGraph) throws SpatialIndexException {
//        SpatialIndexPerGraph result = indexTreePerGraph
//            ? buildSpatialIndexPerGraph(datasetGraph, srsURI)
//            : buildSpatialIndexUnion(datasetGraph, srsURI);
//        return result;
//    }

//    public static SpatialIndexPerGraph buildSpatialIndexUnion(DatasetGraph datasetGraph, String srsURI) throws SpatialIndexException {
//        // we always compute an index tree DGT for the default graph
//        // if an index per named graph NG is enabled, we compute a separate index tree NGT for each NG, otherwise all
//        // items will be indexed in the default graph index tree DGT
//
//        STRtree treePerGraph;
//        LOGGER.info("Building Spatial Index - Started");
//        try (AutoTxn txn = Txn.begin(datasetGraph, TxnType.READ)) {
//            treePerGraph = STRtreeUtils.buildSpatialIndexTreeUnion(datasetGraph, srsURI);
//        }
//        LOGGER.info("Building Spatial Index - Completed");
//
//        SRSInfo srsInfo = SRSRegistry.getSRSInfo(srsURI);
//        STRtreePerGraph trees = new STRtreePerGraph(treePerGraph);
//        SpatialIndexPerGraph index = new SpatialIndexPerGraph(srsInfo, trees, null);
//        setSpatialIndex(datasetGraph, index);
//        return index;
//    }

    public static SpatialIndexPerGraph buildSpatialIndexPerGraph(DatasetGraph datasetGraph, String srsURI) throws SpatialIndexException {
        // we always compute an index tree DGT for the default graph
        // if an index per named graph NG is enabled, we compute a separate index tree NGT for each NG, otherwise all
        // items will be indexed in the default graph index tree DGT

        STRtreePerGraph treePerGraph;
        LOGGER.info("Building Spatial Index - Started");
        try (AutoTxn txn = Txn.autoTxn(datasetGraph, TxnType.READ)) {
            treePerGraph = STRtreeUtils.buildSpatialIndexTree(datasetGraph, srsURI);
            txn.commit();
        }
        LOGGER.info("Building Spatial Index - Completed");

        SRSInfo srsInfo = SRSRegistry.getSRSInfo(srsURI);
        SpatialIndexPerGraph index = new SpatialIndexPerGraph(srsInfo, treePerGraph, null);
        setSpatialIndex(datasetGraph, index);
        return index;
    }

    public static Node unwrapGraphName(Graph graph) {
        Node graphNode = graph instanceof NamedGraph namedGraph
            ? namedGraph.getGraphName()
            : null;
        return graphNode;
    }
}
