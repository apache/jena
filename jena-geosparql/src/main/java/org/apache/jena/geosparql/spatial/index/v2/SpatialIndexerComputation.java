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
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.Callable;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import org.apache.jena.geosparql.spatial.SpatialIndexException;
import org.apache.jena.graph.Graph;
import org.apache.jena.graph.Node;
import org.apache.jena.query.TxnType;
import org.apache.jena.sparql.core.DatasetGraph;
import org.apache.jena.sparql.core.Quad;
import org.apache.jena.sparql.engine.iterator.Abortable;
import org.apache.jena.system.AutoTxn;
import org.apache.jena.system.Txn;
import org.locationtech.jts.index.strtree.STRtree;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/** Low level class to compute geo indexes for a given set of graphs. */
public class SpatialIndexerComputation
    implements Callable<SpatialIndexPerGraph>, Abortable
{
    private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    private DatasetGraph datasetGraph;
    private List<Node> graphNodes;
    private ExecutorService executorService;
    private String srsURI;
    private int threadCount;

    private final AtomicBoolean requestingCancel = new AtomicBoolean();
    private volatile boolean cancelOnce = false;
    private Object cancelLock = new Object();

    private List<Future<Entry<Node, STRtree>>> futures = new ArrayList<>();

    private static boolean logProgress = false;

    /**
     * Create an instance of a spatial indexer computation.
     *
     * @param datasetGraph The dataset graph for which to index the spatial data.
     * @param srsURI       The spatial reference system against which to index the data.
     * @param graphNodes   The names of the physical graphs which to process. May use Quad.defaultGraphIRI but not e.g. Quad.unionGraph.
     * @param threadCount  Maximum number of threads to use for indexing. Must be at least 1.
     */
    public SpatialIndexerComputation(DatasetGraph datasetGraph, String srsURI, List<Node> graphNodes, int threadCount) {
        this.datasetGraph = datasetGraph;
        this.graphNodes = graphNodes;
        this.srsURI = srsURI;
        this.threadCount = threadCount;

        if (threadCount <= 0) {
            throw new IllegalArgumentException("Thread count must be greater than 0.");
        }
    }

    public String getSrsURI() {
        return srsURI;
    }

    public List<Node> getGraphNodes() {
        return graphNodes;
    }

    private Entry<Node, STRtree> indexOneGraph(Node graphNode) throws SpatialIndexException {
        if (logProgress) {
            LOGGER.info("Started spatial index build for graph {} ...", graphNode);
        }

        STRtree tree = null;

        try (AutoTxn txn = Txn.autoTxn(datasetGraph, TxnType.READ)) {
            Graph graph = Quad.isDefaultGraph(graphNode) // XXX would getGraph work with the default graph URIs?
                ? datasetGraph.getDefaultGraph()
                : datasetGraph.getGraph(graphNode);
            if (graph != null) { // May be null if the requested graph does not exist (possibly due to a dynamic dataset)
                tree = STRtreeUtils.buildSpatialIndexTree(graph, srsURI);
            }

            // XXX This commit is a workaround for DatasetGraphText.abort() causing a NPE in
            // during multi-threaded spatial index computation.
            // This is an issue related to the transaction mechanics of DatasetGraphText.
            txn.commit();
        }

        if (logProgress) {
            LOGGER.info("Completed spatial index for graph {}", graphNode);
        }
        return Map.entry(graphNode, tree);
    }

    /** Returns a {@link SpatialIndexPerGraph} instance with the configured SRS. */
    @Override
    public SpatialIndexPerGraph call() throws InterruptedException, ExecutionException {
        try {
            return callActual();
        } finally {
            cleanUp();
        }
    }

    protected SpatialIndexPerGraph callActual() throws InterruptedException, ExecutionException {
        synchronized (cancelLock) {
            if (executorService != null) {
                throw new IllegalStateException("Task already running.");
            }
            executorService = Executors.newFixedThreadPool(threadCount);
        }

        // Beware: We expect special graphNodes such as Quad.unionGraph to have been resolved
        // to the explicit graph names before coming here.
        for (Node graphNode : graphNodes) {
            checkRequestingCancel();
            Future<Entry<Node, STRtree>> future = executorService.submit(() -> indexOneGraph(graphNode));
            futures.add(future);
        }

        // Collect all futures into a map.
        Map<Node, STRtree> treeMap = new LinkedHashMap<Node, STRtree>();
        for (Future<Entry<Node, STRtree>> future : futures) {
            Entry<Node, STRtree> entry = future.get();
            Node graphNode = entry.getKey();
            STRtree tree = entry.getValue();
            if (tree != null) {
                treeMap.put(graphNode, tree);
            }
        }

        STRtreePerGraph trees = new STRtreePerGraph();
        trees.setTrees(treeMap);
        SpatialIndexPerGraph result = new SpatialIndexPerGraph(trees);
        return result;
    }

    protected void cleanUp() {
        if (executorService != null) {
            executorService.shutdownNow();
            try {
                executorService.awaitTermination(5, TimeUnit.SECONDS);
            } catch (InterruptedException e) {
                LOGGER.warn("Abandoning an executor service that failed to stop.", e);
            }
        }
    }

    protected void checkRequestingCancel() {
        boolean isCancelled = requestingCancel();
        if (isCancelled) {
            throw new CancellationException();
        }
    }

    @Override
    public void abort() {
        synchronized (cancelLock) {
            requestingCancel.set(true);
            if (!cancelOnce) {
                requestCancel();
                cancelOnce = true;
            }
        }
    }

    protected void requestCancel() { }

    private boolean requestingCancel() {
        return (requestingCancel != null && requestingCancel.get()) || Thread.interrupted() ;
    }

    protected void performRequestingCancel() {
        // Tasks will be aborted on close - so nothing to do here.
    }
}
