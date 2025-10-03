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

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Stream;

import org.apache.jena.atlas.iterator.Iter;
import org.apache.jena.geosparql.configuration.GeoSPARQLOperations;
import org.apache.jena.geosparql.implementation.SRSInfo;
import org.apache.jena.geosparql.implementation.registry.SRSRegistry;
import org.apache.jena.geosparql.spatial.SpatialIndex;
import org.apache.jena.geosparql.spatial.SpatialIndexConstants;
import org.apache.jena.geosparql.spatial.SpatialIndexException;
import org.apache.jena.geosparql.spatial.task.TaskThread;
import org.apache.jena.geosparql.spatial.task.BasicTask;
import org.apache.jena.geosparql.spatial.task.BasicTask.TaskListener;
import org.apache.jena.graph.Graph;
import org.apache.jena.graph.Node;
import org.apache.jena.query.Dataset;
import org.apache.jena.query.DatasetFactory;
import org.apache.jena.query.TxnType;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.sparql.core.DatasetGraph;
import org.apache.jena.sparql.core.NamedGraph;
import org.apache.jena.sparql.core.Quad;
import org.apache.jena.sparql.engine.ExecutionContext;
import org.apache.jena.sparql.util.Context;
import org.apache.jena.system.AutoTxn;
import org.apache.jena.system.Txn;
import org.locationtech.jts.index.strtree.STRtree;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** ARQ-level utils for Dataset and Context. */
public class SpatialIndexLib {

    private static final Logger logger = LoggerFactory.getLogger(SpatialIndexLib.class);

    /** Set the preferred SRS for the given (dataset) context. */
    public static final String getPreferredSRS(Context context) {
        return context == null ? null : context.getAsString(SpatialIndexConstants.symSrsUri);
    }

    /** Set a preferred SRS for the given (dataset) context. This SRS will be used when constructing a spatial index for the first time. */
    public static final void setPreferredSRS(Context context, String srsUri) {
        context.set(SpatialIndexConstants.symSrsUri, srsUri);
    }

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
        context.set(SpatialIndexConstants.symSpatialIndex, spatialIndex);
    }

    public static final <T extends SpatialIndex> T getSpatialIndex(Context cxt) {
        return cxt == null ? null : cxt.get(SpatialIndexConstants.symSpatialIndex);
    }

    public static final <T extends SpatialIndex> T getSpatialIndex(DatasetGraph dsg) {
        return getSpatialIndex(dsg.getContext());
    }

    /**
     *
     * @param execCxt
     * @return True if a SpatialIndex is defined in the ExecutionContext.
     */
    public static final boolean isDefined(ExecutionContext execCxt) {
        Context context = execCxt.getContext();
        return context.isDefined(SpatialIndexConstants.symSpatialIndex);
    }

    /**
     * Get the SpatialIndex from the Context. Fail if absent.
     *
     * @param execCxt
     * @return SpatialIndex contained in the Context.
     * @throws SpatialIndexException
     */
    public static final SpatialIndex require(ExecutionContext execCxt) throws SpatialIndexException {
        Context context = execCxt.getContext();
        SpatialIndex spatialIndex = (SpatialIndex) context.get(SpatialIndexConstants.symSpatialIndex, null);
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
        SpatialIndex spatialIndex = buildSpatialIndex(datasetGraph, null);
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

    public static SpatialIndexPerGraph buildSpatialIndexPerGraph(DatasetGraph datasetGraph, String srsURI) throws SpatialIndexException {
        Objects.requireNonNull(datasetGraph);

        if (srsURI == null) {
            // XXX Dataset wrapping due to legacy code.
            Dataset dataset = DatasetFactory.wrap(datasetGraph);
            srsURI = GeoSPARQLOperations.findModeSRS(dataset);
        }

        // XXX SpatialIndexerComputation could be adapted to run the code below if just 1 thread is requested.
        // SpatialIndexerComputation computation = new SpatialIndexerComputation(datasetGraph, srsURI, null, 1);

        STRtreePerGraph treePerGraph;
        logger.info("Building Spatial Index - Started");
        try (AutoTxn txn = Txn.autoTxn(datasetGraph, TxnType.READ)) {
            treePerGraph = STRtreeUtils.buildSpatialIndexTree(datasetGraph, srsURI);
            txn.commit();
        }
        logger.info("Building Spatial Index - Completed");

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

    public static BasicTask scheduleOnceIndexTask(DatasetGraph dsg, SpatialIndexerComputation indexComputation, Path targetFile, boolean isReplaceTask,
            TaskListener<BasicTask> taskListener) {
        Context cxt = dsg.getContext();

        BasicTask task = cxt.compute(SpatialIndexConstants.symSpatialIndexTask, (key, priorTaskObj) -> {
            BasicTask priorTask = (BasicTask)priorTaskObj;
            if (priorTask != null && !priorTask.isTerminated()) {
                throw new RuntimeException("A spatial indexing task is already active for this dataset. Wait for completion or abort it.");
            }

            TaskThread thread = createIndexerTask(dsg, null, indexComputation, taskListener, targetFile, isReplaceTask);
            thread.start();
            return thread;
        });

        return task;
    }

    public static TaskThread createIndexerTask(DatasetGraph dsg, Predicate<Node> isAuthorizedGraph, SpatialIndexerComputation indexComputation, TaskListener<BasicTask> taskListener, Path targetFile, boolean isReplaceTask) {
        Context cxt = dsg.getContext();
        long graphCount = indexComputation.getGraphNodes().size();
        boolean isEffectiveUpdate = !isReplaceTask || isAuthorizedGraph != null;

        TaskThread thread = new TaskThread("Spatial Indexer Task", taskListener) {
            @Override
            public void runActual() throws Exception {
                // Prevent deletions of graphs from the index which a user cannot see due to access restrictions.
                // For this we need the physical graph list.
                // Replace task on a visible subset is an update.
                // With removal of all graphs in visible but not in selected.

                if (logger.isInfoEnabled()) {
                    String replaceMsg = isReplaceTask ? "The resulting index will REPLACE a prior index." : "A prior index will be UPDATED with the newly indexed graphs.";
                    logger.info("Indexing of {} graphs started. " + replaceMsg, graphCount);
                }

                SpatialIndexPerGraph rawOldIndex = SpatialIndexLib.getSpatialIndex(cxt);

                // Check that prior and new SRS are consistent.
                if (isEffectiveUpdate) {
                    String priorSrs = Optional.ofNullable(rawOldIndex)
                            .map(SpatialIndex::getSrsInfo).map(SRSInfo::getSrsURI).orElse(null);
                    String requestedSrs = indexComputation.getSrsURI();

                    if (priorSrs != null && !priorSrs.equals(requestedSrs)) {
                        throw new IllegalArgumentException("The SRS of the update request is inconistent with the SRS of the index: index SRS: " + priorSrs + ", requested SRS: " + requestedSrs);
                    }
                }

                // Uncomment to test artificial delays.
                // Thread.sleep(5000);

                SpatialIndexPerGraph newIndex = indexComputation.call();

                // If NOT in replace mode, add all graph-indexes from the previous index
                if (isEffectiveUpdate) {
                    // Copy the old index into a new one.
                    SpatialIndexPerGraph oldIndex = rawOldIndex;
                    if (oldIndex != null) {
                        Map<Node, STRtree> oldTreeMap = oldIndex.getIndex().getTreeMap();

                        oldTreeMap.forEach((name, tree) -> {
                            boolean isGraphNotSelectedForUpdate = !newIndex.getIndex().contains(name);

                            boolean addGraph = false;
                            if (isReplaceTask) {
                                boolean isNonAuthorizedGraph = isAuthorizedGraph != null && !isAuthorizedGraph.test(name);
                                if (isNonAuthorizedGraph) {
                                    addGraph = true;
                                }
                            } else {
                                addGraph = isGraphNotSelectedForUpdate;
                            }

                            if (addGraph) {
                                newIndex.getIndex().setTree(name, tree);
                            }
                        });
                    }
                }

                SpatialIndexLib.setSpatialIndex(cxt, newIndex);
                if (targetFile != null) {
                    newIndex.setLocation(targetFile);
                    logger.info("Writing spatial index of {} graphs to disk at path {}", graphCount, targetFile.toAbsolutePath());
                    SpatialIndexIoKryo.save(targetFile, newIndex);
                }
                String statusMsg = String.format("Updated spatial index with %d graphs.", graphCount);
                setStatusMessage(statusMsg);
                if (logger.isInfoEnabled()) {
                    logger.info("Indexing of {} graphs completed successfully.", graphCount);
                }
            }

            @Override
            public void requestCancel() {
                indexComputation.abort();
                super.requestCancel(); // Interrupt
            }
        };

        return thread;
    }

    /**
     * Attempt to start a spatial index task that cleans the index of graphs not present in the given dataset.
     * This method fails if there is already another spatial index task running.
     */
    public static BasicTask scheduleOnceCleanTask(DatasetGraph dsg, TaskListener<BasicTask> taskListener) {
        Context cxt = dsg.getContext();
        BasicTask task = cxt.compute(SpatialIndexConstants.symSpatialIndexTask, (key, priorTaskObj) -> {
            BasicTask priorTask = (BasicTask) priorTaskObj;
            if (priorTask != null && !priorTask.isTerminated()) {
                throw new RuntimeException("A spatial indexing task is already active for this dataset. Wait for completion or abort it.");
            }

            TaskThread thread = createCleanTask(dsg, null, taskListener);
            thread.start();
            return thread;
        });
        return task;
    }

    public static TaskThread createCleanTask(DatasetGraph dsg, Predicate<Node> isAuthorizedGraph, TaskListener<BasicTask> taskListener) {
        Context cxt = dsg.getContext();

        TaskThread thread = new TaskThread("Clean action", taskListener) {
            @Override
            public void runActual() throws Exception {
                SpatialIndex spatialIndexRaw = SpatialIndexLib.getSpatialIndex(cxt);
                if (spatialIndexRaw == null) {
                    throw new SpatialIndexException("No spatial index available on current dataset.");
                } else if (spatialIndexRaw instanceof SpatialIndexPerGraph spatialIndex) {
                    // Prevent deletions of graphs from the index which a user cannot see due to access restrictions.
                    // For this we need the physical graph list.
                    Set<Node> physicalGraphs = Txn.calculateRead(dsg, () -> accGraphNodes(new LinkedHashSet<>(), dsg));

                    STRtreePerGraph perGraphIndex = spatialIndex.getIndex();
                    Map<Node, STRtree> treeMap = perGraphIndex.getTreeMap();
                    Set<Node> visibleGraphNodes = Txn.calculateRead(dsg, () -> accGraphNodes(new LinkedHashSet<>(), dsg));
                    if (physicalGraphs == null) {
                        physicalGraphs = visibleGraphNodes;
                        visibleGraphNodes = null;
                    }

                    List<Node> indexGraphNodes = new ArrayList<>(treeMap.keySet());

                    int cleanCount = 0;
                    for (Node node : indexGraphNodes) {
                        if (!(node == null || Quad.isDefaultGraph(node))) { // Can never delete the default graph.
                            // A graph is only subject to removal if it is not in the physical graph list.
                            if (!physicalGraphs.contains(node)) {
                                // If the graph subject to removal is not visible to the user then reject the removal
                                if (isAuthorizedGraph != null && !isAuthorizedGraph.test(node)) {
                                    // Prevent removal of a graph we are not authorized to.
                                    continue;
                                }
                                perGraphIndex.removeTree(node);
                                ++cleanCount;
                                // System.out.println("Removed: [" + node + "] " + (node == null));
                            }
                        }
                    }

                    int finalGraphCount = treeMap.keySet().size();
                    Path targetFile = spatialIndex.getLocation();
                    // SpatialIndexUtils.setSpatialIndex(cxt, newIndex);
                    if (cleanCount > 0 && targetFile != null) {
                        // newIndex.setLocation(targetFile);
                        logger.info("Writing spatial index of {} graphs (cleaned: {}) to disk at path {}", finalGraphCount, cleanCount, targetFile.toAbsolutePath());
                        SpatialIndexIoKryo.save(targetFile, spatialIndex);
                    }
                    String statusMsg = String.format("Updated spatial index of %d graphs (cleaned: %d)", finalGraphCount, cleanCount);
                    setStatusMessage(statusMsg);
                    logger.info("Indexing of {} graphs completed successfully.", finalGraphCount);
                } else {
                    throw new SpatialIndexException("Unsupported spatial index type for cleaning.");
                }
            }
        };
        return thread;
    }

    public static <C extends Collection<Node>> C accGraphNodes(C accGraphs, DatasetGraph dsg) {
        try (Stream<Node> s = Iter.asStream(dsg.listGraphNodes())) {
            s.forEach(accGraphs::add);
        }
        return accGraphs;
    }
}
