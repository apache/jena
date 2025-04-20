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

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentHashMap.KeySetView;
import java.util.stream.Stream;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.jena.atlas.iterator.Iter;
import org.apache.jena.atlas.lib.DateTimeUtils;
import org.apache.jena.fuseki.FusekiException;
import org.apache.jena.fuseki.servlets.BaseActionREST;
import org.apache.jena.fuseki.servlets.HttpAction;
import org.apache.jena.geosparql.spatial.SpatialIndex;
import org.apache.jena.geosparql.spatial.SpatialIndexException;
import org.apache.jena.geosparql.spatial.index.v2.STRtreePerGraph;
import org.apache.jena.geosparql.spatial.index.v2.SpatialIndexIoKryo;
import org.apache.jena.geosparql.spatial.index.v2.SpatialIndexPerGraph;
import org.apache.jena.geosparql.spatial.index.v2.SpatialIndexUtils;
import org.apache.jena.geosparql.spatial.index.v2.SpatialIndexerComputation;
import org.apache.jena.geosparql.spatial.task.AbortableThread;
import org.apache.jena.geosparql.spatial.task.TaskControl;
import org.apache.jena.geosparql.spatial.task.TaskControlOverAbortableThread;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.riot.WebContent;
import org.apache.jena.riot.web.HttpNames;
import org.apache.jena.sparql.core.DatasetGraph;
import org.apache.jena.sparql.core.Quad;
import org.apache.jena.sparql.util.Context;
import org.apache.jena.system.Txn;
import org.apache.jena.web.HttpSC;
import org.locationtech.jts.index.strtree.STRtree;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;

import jakarta.servlet.AsyncContext;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * Spatial index (re)computation service.
 *
 * Supports two types of tasks that are executed concurrently. For a given data set, only a single
 * task can be active at a given time. The status of the most recent task can be queried via the
 * rest API. Task execution also broadcasts start/abort/termination events via server side events
 * (SSE).
 *
 * <ul>
 *   <li>Updating/replacing the graphs of a spatial index.</li>
 *   <li>Removal of graphs from a spatial index that are absent in the corresponding data set.</li>
 * </ul>
 */
public class SpatialIndexComputeService extends BaseActionREST {
    private static final Logger logger = LoggerFactory.getLogger(SpatialIndexComputeService.class);

    private static Gson gson = new Gson();

    /** Registered clients listening to server side events for indexer status updates. */
    private KeySetView<AsyncContext, ?> eventListeners = ConcurrentHashMap.newKeySet();

    public SpatialIndexComputeService() {}

    private static List<Node> extractGraphs(DatasetGraph dsg, HttpAction action) {
        String uris = action.getRequest().getParameter(HttpNames.paramGraph);
        Collection<String> strs;
        if (uris == null || uris.isBlank()) {
            strs = List.of(Quad.defaultGraphIRI.toString(), Quad.unionGraph.toString());
        } else {
            TypeToken<List<String>> typeToken = new TypeToken<List<String>>(){};
            strs = gson.fromJson(uris, typeToken);
        }
        List<Node> rawGraphNodes = strs.stream().map(NodeFactory::createURI).distinct().toList();
        // If the set of specified graphs is empty then index all.
        if (rawGraphNodes.isEmpty()) {
            rawGraphNodes = List.of(Quad.defaultGraphIRI, Quad.unionGraph);
        }
        List<Node> expandedGraphNodes = rawGraphNodes.stream()
            .flatMap(node -> expandUnionGraphNode(dsg, node).stream()).distinct().toList();
        return expandedGraphNodes;
    }

    private static boolean isReplaceMode(HttpAction action) {
        String str = action.getRequest().getParameter("replaceMode");
        boolean result = (str == null || str.isBlank()) ? false : Boolean.parseBoolean(str);
        return result;
    }

    private static int getThreadCount(HttpAction action) {
        String str = action.getRequest().getParameter("maxThreadCount");
        int result = (str == null || str.isBlank()) ? 1 : Integer.parseInt(str);

        if (result == 0) {
            result = Runtime.getRuntime().availableProcessors();
        }

        return result;
    }

    private static List<Node> expandUnionGraphNode(DatasetGraph dsg, Node node) {
        return Quad.isUnionGraph(node)
            ? listGraphNodes(dsg)
            : List.of(node);
    }

    private static List<Node> listGraphNodes(DatasetGraph dsg) {
        try (Stream<Node> s = Iter.asStream(dsg.listGraphNodes())) {
            return s.toList();
        }
    }

    /**
     * The GET command can serve: the website, the notification stream from task execution
     * and the latest task execution status.
     */
    @Override
    protected void doGet(HttpAction action) {
        String rawCommand = action.getRequestParameter("command");
        String command = Optional.ofNullable(rawCommand).orElse("website");
        switch (command) {
        case "website": serveWebSite(action); break;
        case "events": serveEvents(action); break;
        case "status": serveStatus(action); break;
        default:
            throw new UnsupportedOperationException("Unsupported operation: " + command);
        }
    }

    protected void serveWebSite(HttpAction action) {
        // Serves the minimal graphql ui
        String resourceName = "spatial-indexer/index.html";
        String str = null;
        try (InputStream in = SpatialIndexComputeService.class.getClassLoader().getResourceAsStream(resourceName)) {
            str = IOUtils.toString(in, StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new FusekiException(e);
        }

        if (str == null) {
            action.setResponseStatus(HttpSC.INTERNAL_SERVER_ERROR_500);
            action.setResponseContentType(WebContent.contentTypeTextPlain);
            str = "Failed to load classpath resource " + resourceName;
        } else {
            action.setResponseStatus(HttpSC.OK_200);
            action.setResponseContentType(WebContent.contentTypeHTML);
        }
        try (OutputStream out = action.getResponseOutputStream()) {
            IOUtils.write(str, out, StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new FusekiException(e);
        }
    }

    protected TaskControl<?> getActiveTask(HttpAction action) {
        DatasetGraph dsg = action.getDataset();
        Context cxt = dsg.getContext();
        TaskControl<?> activeTask = cxt.get(SpatialIndexUtils.SPATIAL_INDEX_TASK_SYMBOL);
        return activeTask;
    }

    /**
     * Post request: Handle API call.
     * Request is rejected if there is an already running task.
     */
    @Override
    protected void doPost(HttpAction action) {
        try {
            String rawCommand = action.getRequestParameter("command");
            String command = Optional.ofNullable(rawCommand).orElse("none");
            switch (command) {
            case "index": doIndex(action); break;
            case "clean": doClean(action); break;
            case "status": serveStatus(action); break;
            case "cancel": doStop(action); break;
            default: throw new UnsupportedOperationException("Unsupported operation: " + command);
            }
        } catch (Throwable t) {
            String str = ExceptionUtils.getStackTrace(t);
            action.log.error("An unexpected error occurred.", t);
            // FusekiException
            setResponse(action, HttpSC.INTERNAL_SERVER_ERROR_500, WebContent.contentTypeTextPlain, str);
        }
    }

    protected void serveEvents(HttpAction action) {
        HttpServletRequest request = action.getRequest();
        HttpServletResponse response = action.getResponse();

        response.setContentType("text/event-stream");
        response.setCharacterEncoding("UTF-8");
        response.setHeader("Cache-Control", "no-cache");

        final AsyncContext asyncContext = request.startAsync();
        asyncContext.setTimeout(0);
        eventListeners.add(asyncContext);
    }

    /**
     * Remove all graphs from the index for which there is no corresponding graph in the dataset.
     */
    protected void doClean(HttpAction action) throws Exception {
        DatasetGraph dsg = action.getDataset();
        Context cxt = dsg.getContext();

        TaskControlOverAbortableThread<Thread> taskCtl = new TaskControlOverAbortableThread<>("Spatial Indexer Task");

        cxt.compute(SpatialIndexUtils.SPATIAL_INDEX_TASK_SYMBOL, (key, priorTaskObj) -> {
            TaskControl<?> priorTask = (TaskControl<?>)priorTaskObj;
            if (priorTask != null && !priorTask.isComplete()) {
                throw new RuntimeException("A spatial indexing task is already active for this dataset. Wait for completion or abort it.");
            }

            AbortableThread<?> thread = new AbortableThread<>() {
                @Override
                public void runActual() throws Exception {
                    broadcastTaskStart(getStartTime(), null);

                    SpatialIndex spatialIndexRaw = SpatialIndexUtils.getSpatialIndex(cxt);
                    if (spatialIndexRaw == null) {
                        throw new SpatialIndexException("No spatial index available on current dataset.");
                    } else if (spatialIndexRaw instanceof SpatialIndexPerGraph spatialIndex) {
                        STRtreePerGraph perGraphIndex = spatialIndex.getIndex();
                        Map<Node, STRtree> treeMap = perGraphIndex.getTreeMap();
                        Set<Node> graphNodes = new LinkedHashSet<>(Txn.calculateRead(dsg, () -> listGraphNodes(dsg)));
                        // int initialGraphCount = treeMap.keySet().size();
                        List<Node> indexGraphNodes = new ArrayList<>(treeMap.keySet());

                        int cleanCount = 0;
                        for (Node node : indexGraphNodes) {
                            if (!(node == null || Quad.isDefaultGraph(node)) && !graphNodes.contains(node)) {
                                perGraphIndex.removeTree(node);
                                ++cleanCount;
                                // System.out.println("Removed: [" + node + "] " + (node == null));
                            }
                        }

                        int finalGraphCount = treeMap.keySet().size();
                        Path targetFile = spatialIndex.getLocation();
                        // SpatialIndexUtils.setSpatialIndex(cxt, newIndex);
                        if (cleanCount > 0 && targetFile != null) {
                            // newIndex.setLocation(targetFile);
                            action.log.info("Writing spatial index of {} graphs (cleaned: {}) to disk at path {}", finalGraphCount, cleanCount, targetFile.toAbsolutePath());
                            SpatialIndexIoKryo.save(targetFile, spatialIndex);
                        }
                        String statusMsg = String.format("Updated spatial index of %d graphs (cleaned: %d)", finalGraphCount, cleanCount);
                        setStatusMessage(statusMsg);
                        if (logger.isInfoEnabled()) {
                            logger.info("Indexing of {} graphs completed successfully.", finalGraphCount);
                        }
                    } else {
                        throw new SpatialIndexException("Unsupported spatial index type for cleaning.");
                    }
                }

                @Override
                protected void doAfterRun() {
                    broadcastTaskEnd(getEndTime(), getThrowable(), getStatusMessage());
                }
            };

            taskCtl.setThread(thread);
            taskCtl.setSource(thread);
            thread.start();
            return taskCtl;
        });
    }

    protected void doStop(HttpAction action) {
        TaskControl<?> task = getActiveTask(action);
        String state;
        if (task != null) {
            state = "true";
            // Add a completion listener to return when done.
            task.abort();
            Thread thread = (Thread)task.getSource();
            try {
                thread.join();
            } catch (InterruptedException e) {
                // TODO Raise some HTTP error
                throw new RuntimeException(e);
            }
        } else {
            state = "false";
        }

        action.setResponseStatus(HttpSC.OK_200);
        action.setResponseContentType(WebContent.contentTypeJSON);
        try {
            action.getResponseOutputStream().print(String.format("{ \"stopped\": %s }", state));
        } catch (IOException e) {
            throw new FusekiException(e);
        }
    }

    /**
     * Serves a JSON object that captures the current indexing status.
     *
     * <pre>
     * {
     *   isIndexing: Boolean, // Whether an index task is running.
     *   isAborting: Boolean, // Iff isIndexing==true: Whether abort has been requested.
     *   error: String,       // Iff isIndexing==false: An error message of the most recently terminated indexing task.
     *   message: String      // Status message of the most recent indexing task (running or terminated).
     *   time: long           // EpochMillis of the last status update.
     * }
     * </pre>
     */
    protected void serveStatus(HttpAction action) {
        TaskControl<?> task = getActiveTask(action);

        JsonObject status = new JsonObject();
        Instant time;
        if (task == null) {
            status.addProperty("isIndexing", false);
            time = Instant.ofEpochMilli(0);
        } else {
            status.addProperty("isIndexing", !task.isComplete());
            if (!task.isComplete()) {
                status.addProperty("isAborting", task.isAborting());
                time = !task.isAborting() ? task.getStartTime() : task.getCancelTime();
            } else {
                time = task.getEndTime();
            }
            Throwable throwable = task.getThrowable();
            if (throwable != null) {
                String msg = ExceptionUtils.getStackTrace(throwable);
                status.addProperty("error", msg);
            }

            String msg = task.getStatusMessage();
            if (msg != null) {
                status.addProperty("message", msg);
            }
        }
        status.addProperty("time", time.toEpochMilli());

        action.setResponseStatus(HttpSC.OK_200);
        action.setResponseContentType(WebContent.contentTypeJSON);
        try {
            String str = gson.toJson(status);
            action.getResponseOutputStream().println(str);
        } catch (IOException e) {
            throw new FusekiException(e);
        }
    }

    protected TaskControl<?> scheduleTask(HttpAction action, SpatialIndexerComputation indexComputation, Path targetFile, boolean isReplaceTask) {
        DatasetGraph dsg = action.getDataset();
        Context cxt = dsg.getContext();

        TaskControlOverAbortableThread<Thread> taskCtl = new TaskControlOverAbortableThread<>("Spatial Indexer Task");

        cxt.compute(SpatialIndexUtils.SPATIAL_INDEX_TASK_SYMBOL, (key, priorTaskObj) -> {
            TaskControl<?> priorTask = (TaskControl<?>)priorTaskObj;
            if (priorTask != null && !priorTask.isComplete()) {
                throw new RuntimeException("A spatial indexing task is already active for this dataset. Wait for completion or abort it.");
            }

            long graphCount = indexComputation.getGraphNodes().size();

            AbortableThread<?> thread = new AbortableThread<Object>() {
                @Override
                public void runActual() throws Exception {
                    broadcastTaskStart(getStartTime(), null);
                    if (logger.isInfoEnabled()) {
                        String replaceMsg = isReplaceTask ? "The resulting index will REPLACE a prior index." : "A prior index will be UPDATED with the newly indexed graphs.";
                        logger.info("Indexing of {} graphs started. " + replaceMsg, graphCount);
                    }

                    // Uncomment to test artificial delays.
                    // Thread.sleep(5000);

                    SpatialIndexPerGraph newIndex = indexComputation.call();

                    // If NOT in replace mode, add all graph-indexes from the previous index
                    if (!isReplaceTask) {
                        SpatialIndexPerGraph oldIndex = (SpatialIndexPerGraph)SpatialIndexUtils.getSpatialIndex(cxt);
                        Map<Node, STRtree> treeMap = oldIndex.getIndex().getTreeMap();
                        treeMap.forEach((name, tree) -> {
                            if (!newIndex.getIndex().contains(name)) {
                                newIndex.getIndex().setTree(name, tree);
                            }
                        });
                    }

                    SpatialIndexUtils.setSpatialIndex(cxt, newIndex);
                    if (targetFile != null) {
                        newIndex.setLocation(targetFile);
                        action.log.info("Writing spatial index of {} graphs to disk at path {}", graphCount, targetFile.toAbsolutePath());
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
                    broadcastTaskAbort(getCancelTime(), null);
                    indexComputation.abort();
                    super.requestCancel(); // Interrupt
                }

                @Override
                protected void doAfterRun() {
                    try {
                        indexComputation.close();
                    } finally {
                        try {
                            Throwable throwable = getThrowable();
                            Instant endTime = getEndTime();
                            broadcastTaskEnd(endTime, throwable, getStatusMessage());
                        } finally {
                            if (logger.isInfoEnabled()) {
                                logger.info("Indexing task of {} graphs terminated.", graphCount);
                            }
                        }
                    }
                }
            };

            taskCtl.setThread(thread);
            taskCtl.setSource(thread);
            thread.start();
            return taskCtl;
        });

        return taskCtl;
    }

    protected void doIndex(HttpAction action) throws Exception {
        DatasetGraph dsg = action.getDataset();
        SpatialIndex index = SpatialIndexUtils.getSpatialIndex(dsg.getContext());

        if (index == null) { // error: no spatial index has been configured
            // XXX Could still allow for creating an in-memory-only index.
            String msg = String.format("[%d] no spatial index has been configured for the dataset", action.id);
            action.log.error(msg);
            action.setResponseStatus(HttpSC.SERVICE_UNAVAILABLE_503);
            action.setResponseContentType(WebContent.contentTypeTextPlain);
            try {
                action.getResponseWriter().println(msg);
            } catch (IOException e) {
                throw new FusekiException(e);
            }
            return;
        } else {
            boolean isReplaceMode = isReplaceMode(action);
            int threadCount = getThreadCount(action);

            // Only SpatialIndexPerGraph can be updated.
            // Check if the index can be updated. If not then raise an exception
            // that informs that only replace mode can be used in this situation.
            if (!isReplaceMode && !(index instanceof SpatialIndexPerGraph)) {
                throw new RuntimeException("Cannot update existing spatial index because its type is unsupported. Consider replacing the index.");
            }

            Path oldLocation = index.getLocation();
            if (oldLocation == null) {
                action.log.warn("Spatial index will not be persisted because no file location was configured.");
            }

            String srsURI = index.getSrsInfo().getSrsURI();

            List<Node> graphNodes = Txn.calculateRead(dsg, () -> extractGraphs(dsg, action));
            SpatialIndexerComputation task = new SpatialIndexerComputation(dsg, srsURI, graphNodes, threadCount);

            action.log.info(String.format("[%d] spatial index: computation request accepted.", action.id));
            scheduleTask(action, task, oldLocation, isReplaceMode);

            setResponse(action, HttpSC.OK_200, WebContent.contentTypeTextPlain, "Spatial index computation task accepted at " + DateTimeUtils.nowAsXSDDateTimeString());
        }
    }

    protected void setResponse(HttpAction action, int statusCode, String contentType, String payload) {
        action.setResponseStatus(statusCode);
        action.setResponseContentType(contentType);
        try (OutputStream out = action.getResponseOutputStream()) {
            IOUtils.write(payload, out, StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new FusekiException(e);
        }
    }

    protected void broadcastTaskStart(Instant time, String msg) {
        JsonObject json = new JsonObject();
        json.addProperty("isIndexing", true);
        json.addProperty("time", time.toEpochMilli());
        if (msg != null) {
            json.addProperty("message", msg);
        }
        broadcastJson(json);
    }
    protected void broadcastTaskAbort(Instant time, String msg) {
        JsonObject json = new JsonObject();
        json.addProperty("isIndexing", true);
        json.addProperty("isAborting", true);
        json.addProperty("time", time.toEpochMilli());
        if (msg != null) {
            json.addProperty("message", msg);
        }
        broadcastJson(json);
    }

    protected void broadcastTaskEnd(Instant time, Throwable throwable, String msg) {
        JsonObject json = new JsonObject();
        json.addProperty("isIndexing", false);
        json.addProperty("time", time.toEpochMilli());
        if (msg != null) {
            json.addProperty("message", msg);
        }
        if (throwable != null) {
            json.addProperty("error", ExceptionUtils.getStackTrace(throwable));
        }
        broadcastJson(json);
    }

    protected void broadcastJson(JsonElement jsonData) {
        String str = gson.toJson(jsonData);
        broadcastLine(str);
    }

    /**
     * Broadcast a payload to all registered listeners.
     * @param payload A string without newline characters.
     */
    protected void broadcastLine(String payload) {
        Iterator<AsyncContext> it = eventListeners.getMap().keySet().iterator();
        while (it.hasNext()) {
            AsyncContext context = it.next();
            try {
                PrintWriter writer = context.getResponse().getWriter();
                // Format demanded by server side events is: "data: <payload>\n\n".
                writer.println("data: " + payload);
                writer.println();
                writer.flush();
            } catch (IOException e) {
                it.remove();
                context.complete();
            }
        }
    }
}
