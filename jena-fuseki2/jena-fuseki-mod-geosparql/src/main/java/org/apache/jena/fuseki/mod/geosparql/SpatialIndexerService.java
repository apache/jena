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
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.jena.atlas.io.IO;
import org.apache.jena.atlas.lib.DateTimeUtils;
import org.apache.jena.fuseki.server.Endpoint;
import org.apache.jena.fuseki.servlets.BaseActionREST;
import org.apache.jena.fuseki.servlets.HttpAction;
import org.apache.jena.fuseki.servlets.ServletOps;
import org.apache.jena.geosparql.spatial.SpatialIndex;
import org.apache.jena.geosparql.spatial.SpatialIndexConstants;
import org.apache.jena.geosparql.spatial.index.v2.SpatialIndexLib;
import org.apache.jena.geosparql.spatial.index.v2.SpatialIndexPerGraph;
import org.apache.jena.geosparql.spatial.index.v2.SpatialIndexerComputation;
import org.apache.jena.geosparql.spatial.task.BasicTask;
import org.apache.jena.geosparql.spatial.task.BasicTask.TaskListener;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.query.Query;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.riot.WebContent;
import org.apache.jena.riot.web.HttpNames;
import org.apache.jena.sparql.core.DatasetGraph;
import org.apache.jena.sparql.core.Quad;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.engine.binding.Binding;
import org.apache.jena.sparql.exec.QueryExec;
import org.apache.jena.sparql.exec.RowSet;
import org.apache.jena.sparql.syntax.syntaxtransform.QueryTransformOps;
import org.apache.jena.sparql.util.Context;
import org.apache.jena.system.Txn;
import org.apache.jena.web.HttpSC;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonWriter;

import jakarta.servlet.AsyncContext;
import jakarta.servlet.AsyncEvent;
import jakarta.servlet.AsyncListener;
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
public class SpatialIndexerService extends BaseActionREST {
    private static final Logger logger = LoggerFactory.getLogger(SpatialIndexerService.class);

    /** Help class for SSE listeners per endpoint. */
    private class EndpointClients {
        Map<AsyncContext, Runnable> eventListeners = Collections.synchronizedMap(new IdentityHashMap<>());
    }

    /** Gson configured to not emit newlines (pretty printing = off). */
    private static Gson gsonForSse = new Gson();

    /** Registered clients listening to server side events for indexer status updates. */
    private Map<Endpoint, EndpointClients> listenersByEndpoint = Collections.synchronizedMap(new IdentityHashMap<>());


    /** Constants for serving graph listings via sparql. */
    private static final Var graphVar = Var.alloc("g");
    private static final Var keywordVar = Var.alloc("keyword");
    private static final Query allGraphsQuery = QueryFactory.create("SELECT ?g { GRAPH ?g { } } ORDER BY ASC(?g)");
    private static final Query graphsByKeywordQuery = QueryFactory.create("SELECT ?g { GRAPH ?g { } FILTER(contains(lcase(str(?g)), ?keyword)) } ORDER BY ASC(?g)");

    public SpatialIndexerService() {}

    /**
     * Extract the explicit set of graphs from the action w.r.t. the dataset graph.
     *
     * @param dsg The dataset graph.
     * @param action The HTTP action.
     * @param emptySelectionToAllGraphs Select all graphs if the request specifies an empty selection of graphs.
     * @return The explicit set of graphs w.r.t. the dataset graph.
     */
    private static Set<Node> extractGraphsFromRequest(DatasetGraph dsg, HttpAction action, boolean emptySelectionToAllGraphs) {
        String uris = action.getRequest().getParameter(HttpNames.paramGraph);
        Collection<String> strs;
        if (uris == null || uris.isBlank()) {
            strs = List.of(Quad.defaultGraphIRI.toString(), Quad.unionGraph.toString());
        } else {
            TypeToken<List<String>> typeToken = new TypeToken<>(){};
            strs = gsonForSse.fromJson(uris, typeToken);
        }
        List<Node> rawGraphNodes = strs.stream().map(NodeFactory::createURI).distinct().toList();
        // If the set of specified graphs is empty then index all.
        if (rawGraphNodes.isEmpty() && emptySelectionToAllGraphs) {
            rawGraphNodes = List.of(Quad.defaultGraphIRI, Quad.unionGraph);
        }

        Set<Node> uniqueGraphNodes = rawGraphNodes.stream()
            .flatMap(node -> expandUnionGraphNode(new ArrayList<>(), dsg, node).stream())
            .collect(Collectors.toCollection(LinkedHashSet::new));
        return uniqueGraphNodes;
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

    private static <C extends Collection<Node>> C expandUnionGraphNode(C accGraphs, DatasetGraph dsg, Node node) {
        if (Quad.isUnionGraph(node)) {
            SpatialIndexLib.accGraphNodes(accGraphs, dsg);
        } else {
            accGraphs.add(node);
        }
        return accGraphs;
    }

    /**
     * The GET command can serve: the website, the notification stream from task execution
     * and the latest task execution status.
     */
    @Override
    protected void doGet(HttpAction action) {
        String rawCommand = action.getRequestParameter("command");
        String command = Optional.ofNullable(rawCommand).orElse("webpage");
        switch (command) {
        case "webpage": serveWebPage(action); break;
        case "events": serveEvents(action); break;
        case "status": serveStatus(action); break;
        default:
            throw new UnsupportedOperationException("Unsupported command (via HTTP GET): " + command);
        }
    }

    protected void serveWebPage(HttpAction action) {
        // Serves the minimal graphql ui
        String resourceName = "spatial-indexer/index.html";
        String str = null;
        try (InputStream in = SpatialIndexerService.class.getClassLoader().getResourceAsStream(resourceName)) {
            str = IOUtils.toString(in, StandardCharsets.UTF_8);
        } catch (IOException e) {
            ServletOps.errorOccurred(e.getMessage(), e);
        }

        if (str == null) {
            ServletOps.error(HttpSC.INTERNAL_SERVER_ERROR_500, "Failed to load classpath resource " + resourceName);
        } else {
            action.setResponseStatus(HttpSC.OK_200);
            action.setResponseContentType(WebContent.contentTypeHTML);
            try (OutputStream out = action.getResponseOutputStream()) {
                IOUtils.write(str, out, StandardCharsets.UTF_8);
            } catch (IOException e) {
                ServletOps.errorOccurred(e);
            }
        }
    }

    protected BasicTask getActiveTask(HttpAction action) {
        DatasetGraph dsg = action.getDataset();
        Context cxt = dsg.getContext();
        BasicTask activeTask = cxt.get(SpatialIndexConstants.symSpatialIndexTask);
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
            case "cancel": doCancel(action); break;
            case "graphs": serveGraphs(action); break;
            default:
                throw new UnsupportedOperationException("Unsupported command (via HTTP POST): " + command);
            }
        } catch (Throwable t) {
            action.log.error("An unexpected error occurred.", t);
            ServletOps.errorOccurred(t);
        }
    }

    /**
     * Serves a JSON array of the graph IRIs visible to the authenticated user.
     * Non-IRI graph names are omitted in the output.
     *
     * Note: It is intentional to NOT expose a full SPARQL endpoint for security reasons.
     */
    protected void serveGraphs(HttpAction action) throws IOException {
        long offset = Optional.ofNullable(action.getRequestParameter("offset")).map(Long::parseLong).orElse(Query.NOLIMIT);
        long limit = Optional.ofNullable(action.getRequestParameter("limit")).map(Long::parseLong).orElse(Query.NOLIMIT);
        String keyword = action.getRequestParameter("keyword");

        Query query;
        if (keyword != null) {
            query = graphsByKeywordQuery.cloneQuery();
            query = QueryTransformOps.syntaxSubstitute(query, Map.of(keywordVar, NodeFactory.createLiteralString(keyword)));
        } else {
            query = allGraphsQuery.cloneQuery();
        }

        if (offset != Query.NOLIMIT || limit != Query.NOLIMIT) {
            query.setLimit(limit);
            query.setOffset(offset);
        }

        action.beginRead();
        try {
            DatasetGraph dsg = action.getActiveDSG();
            // XXX Make tracked should the exec tracker PR become available.
            try (QueryExec qe = QueryExec.dataset(dsg).query(query).build()) {
                try (JsonWriter jsonWriter = gsonForSse.newJsonWriter(
                        new OutputStreamWriter(action.getResponseOutputStream(), StandardCharsets.UTF_8))) {
                    RowSet rs = qe.select();
                    jsonWriter.beginArray();
                    jsonWriter.value(Quad.defaultGraphIRI.getURI());
                    jsonWriter.value(Quad.unionGraph.getURI());
                    while (rs.hasNext()) {
                        Binding b = rs.next();
                        Node n = b.get(graphVar);
                        if (n.isURI()) {
                            String uri = n.getURI();
                            jsonWriter.value(uri);
                        }
                    }
                    jsonWriter.endArray();
                    jsonWriter.flush();
                }
            }
        } finally {
            action.endRead();
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

        Endpoint endpoint = action.getEndpoint();

        Runnable[] disposeSseListener = {null};

        // Detect when client disconnects
        asyncContext.addListener(new AsyncListener() {
            @Override
            public void onComplete(AsyncEvent event) {
                disposeSseListener[0].run();
            }

            @Override
            public void onTimeout(AsyncEvent event) {
                disposeSseListener[0].run();
            }

            @Override
            public void onError(AsyncEvent event) {
                disposeSseListener[0].run();
            }

            @Override
            public void onStartAsync(AsyncEvent event) {
                // No-op
            }
        });

        disposeSseListener[0] = () -> {
            listenersByEndpoint.compute(endpoint, (et, clts) -> {
                EndpointClients r = clts;
                if (clts != null) {
                    // Remove the listener for the async context.
                    // If no more listeners remain then dispose the exec tracker listener.
                    clts.eventListeners.remove(asyncContext);
                    if (clts.eventListeners.isEmpty()) {
                        r = null;
                    }
                    return r;
                }
                return r;
            });
        };

        // Atomically set up the new listener.
        listenersByEndpoint.compute(endpoint, (et, clients) -> {
            if (clients == null) {
                clients = new EndpointClients();
            }
                clients.eventListeners.put(asyncContext, disposeSseListener[0]);
            return clients;
        });
    }

    /**
     * Remove all graphs from the index for which there is no corresponding graph in the dataset.
     */
    protected void doClean(HttpAction action) throws Exception {
        DatasetGraph dsg = action.getDataset();
        Endpoint endpoint = action.getEndpoint();

        TaskListener<BasicTask> taskListener = task -> {
            switch (task.getTaskState()) {
            case STARTING: {
                JsonObject json = toJsonTaskStart(task.getStartTime(), null);
                broadcastJson(endpoint, json);
                break;
            }
            case TERMINATED: {
                JsonObject json = toJsonTaskEnd(task.getEndTime(), task.getThrowable(), task.getStatusMessage());
                broadcastJson(endpoint, json);
                break;
            }
            default:
                break;
            }
        };

        try {
            SpatialIndexLib.scheduleOnceCleanTask(dsg, taskListener);
        } catch (Exception e) {
            ServletOps.errorOccurred(e.getMessage(), e);
        }
    }

    /** Send a stop request to a running task. Does not wait for the task to terminate. */
    protected void doCancel(HttpAction action) {
        BasicTask task = getActiveTask(action);
        String state;
        if (task != null) {
            state = "true";
            task.abort();
        } else {
            state = "false";
        }

        String jsonStr = String.format("{ \"stopped\": %s }", state);
        successJson(action, jsonStr);
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
        BasicTask task = getActiveTask(action);

        JsonObject status = new JsonObject();
        long time;
        if (task == null) {
            status.addProperty("isIndexing", false);
            time = 0;
        } else {
            status.addProperty("isIndexing", !task.isTerminated());
            if (!task.isTerminated()) {
                status.addProperty("isAborting", task.isAborting());
                time = !task.isAborting() ? task.getStartTime() : task.getAbortTime();
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
        status.addProperty("time", time);


        String jsonStr = gsonForSse.toJson(status);
        successJson(action, jsonStr);
    }

    protected BasicTask scheduleIndexTask(HttpAction action, SpatialIndexerComputation indexComputation, Path targetFile, boolean isReplaceTask) {
        Endpoint endpoint = action.getEndpoint();
        DatasetGraph dsg = action.getDataset();

        long graphCount = indexComputation.getGraphNodes().size();

        TaskListener<BasicTask> taskListener = new TaskListener<>() {
            @Override
            public void onStateChange(BasicTask task) {
                switch (task.getTaskState()) {
                case STARTING: {
                    JsonObject json = toJsonTaskStart(task.getStartTime(), null);
                    broadcastJson(endpoint, json);
                    break;
                }
                case ABORTING: {
                    JsonObject json = toJsonTaskAbort(task.getAbortTime(), null);
                    broadcastJson(endpoint, json);
                    break;
                }
                case TERMINATED: {
                    Throwable throwable = task.getThrowable();
                    long endTime = task.getEndTime();
                    JsonObject json = toJsonTaskEnd(endTime, throwable, task.getStatusMessage());
                    broadcastJson(endpoint, json);
                    if (logger.isInfoEnabled()) {
                        logger.info("Indexing task of {} graphs terminated.", graphCount);
                    }
                    break;
                }
                default:
                    break;
                }
            }
        };

        return SpatialIndexLib.scheduleOnceIndexTask(dsg, indexComputation, targetFile, isReplaceTask, taskListener);
    }

    protected void doIndex(HttpAction action) throws Exception {
        DatasetGraph dsg = action.getDataset();
        SpatialIndex index = SpatialIndexLib.getSpatialIndex(dsg.getContext());

        if (index == null) { // error: no spatial index has been configured
            // XXX Could still allow for creating an ad-hoc in-memory-only index.
            String msg = String.format("[%d] No spatial index has been configured for the dataset", action.id);
            action.log.error(msg);
            ServletOps.error(HttpSC.SERVICE_UNAVAILABLE_503, msg);
        } else {
            boolean isReplaceMode = isReplaceMode(action);
            boolean isUpdateMode = !isReplaceMode;

            int threadCount = getThreadCount(action);

            // Only SpatialIndexPerGraph can be updated.
            // Check if the index can be updated.
            // If not then raise an exception
            // that informs that only replace mode can be used in this situation.
            if (!(index instanceof SpatialIndexPerGraph)) {
                if (isUpdateMode) {
                    throw new RuntimeException("Cannot update existing spatial index because its type is unsupported. Consider replacing the index.");
                }
            }

            Path oldLocation = index.getLocation();
            if (oldLocation == null) {
                action.log.warn("Spatial index will not be persisted because no file location was configured.");
            }

            String srsURI = index.getSrsInfo().getSrsURI();

            List<Node> graphNodes = new ArrayList<>(Txn.calculateRead(dsg, () -> extractGraphsFromRequest(dsg, action, isUpdateMode)));
            SpatialIndexerComputation task = new SpatialIndexerComputation(dsg, srsURI, graphNodes, threadCount);

            action.log.info(String.format("[%d] spatial index: computation request accepted.", action.id));

            try {
                scheduleIndexTask(action, task, oldLocation, isReplaceMode);
                successText(action, "Spatial index computation task accepted at " + DateTimeUtils.nowAsXSDDateTimeString());
            } catch (Exception e) {
                ServletOps.errorOccurred(e.getMessage(), e);
            }
        }
    }

    protected static JsonObject toJsonTaskStart(long timeInMillis, String msg) {
        JsonObject json = new JsonObject();
        json.addProperty("isIndexing", true);
        json.addProperty("time", timeInMillis);
        if (msg != null) {
            json.addProperty("message", msg);
        }
        return json;
    }
    protected static JsonObject toJsonTaskAbort(long timeInMillis, String msg) {
        JsonObject json = new JsonObject();
        json.addProperty("isIndexing", true);
        json.addProperty("isAborting", true);
        json.addProperty("time", timeInMillis);
        if (msg != null) {
            json.addProperty("message", msg);
        }
        return json;
    }

    protected static JsonObject toJsonTaskEnd(long timeInMillis, Throwable throwable, String msg) {
        JsonObject json = new JsonObject();
        json.addProperty("isIndexing", false);
        json.addProperty("time", timeInMillis);
        if (msg != null) {
            json.addProperty("message", msg);
        }
        if (throwable != null) {
            json.addProperty("error", ExceptionUtils.getStackTrace(throwable));
        }
        return json;
    }

    protected void broadcastJson(Endpoint endpoint, JsonElement jsonData) {
        EndpointClients clients = listenersByEndpoint.get(endpoint);
        if (clients != null) {
            Iterator<Entry<AsyncContext, Runnable>> it = clients.eventListeners.entrySet().iterator();
            broadcastJson(it, jsonData);
        }
    }

    protected void broadcastJson(Iterator<Entry<AsyncContext, Runnable>> it, JsonElement jsonData) {
        String str = gsonForSse.toJson(jsonData);
        broadcastLine(it, str);
    }

    /**
     * Broadcast a payload to all listeners in the given iterator.
     * On failure, the listener is removed from the iterator.
     */
    protected void broadcastLine(Iterator<Entry<AsyncContext, Runnable>> it, String payload) {
        while (it.hasNext()) {
            Entry<AsyncContext, Runnable> e = it.next();
            AsyncContext context = e.getKey();
            Runnable unregister = e.getValue();
            try {
                PrintWriter writer = context.getResponse().getWriter();
                // Format demanded by server side events is: "data: <payload>\n\n".
                writer.println("data: " + payload);
                writer.println();
                writer.flush();
            } catch (Throwable x) {
                it.remove(); // Remove first so that unregister does not cause concurrent modification.
                logger.warn("Broadcast failed.", x);
                try {
                    unregister.run();
                } finally {
                    context.complete();
                }
            }
        }
    }

    protected static void successText(HttpAction action, String jsonStr) {
        successStringUtf8(action, WebContent.contentTypeTextPlain, jsonStr);
    }

    protected static void successJson(HttpAction action, String jsonStr) {
        successStringUtf8(action, WebContent.contentTypeJSON, jsonStr);
    }

    protected static void successStringUtf8(HttpAction action, String contentType, String str) {
        action.setResponseContentType(contentType);
        action.setResponseCharacterEncoding(WebContent.charsetUTF8);
        action.setResponseStatus(HttpSC.OK_200);
        try {
            action.getResponseOutputStream().println(str);
        } catch (IOException e) {
            IO.exception(e);
        }
        return;
    }
}
