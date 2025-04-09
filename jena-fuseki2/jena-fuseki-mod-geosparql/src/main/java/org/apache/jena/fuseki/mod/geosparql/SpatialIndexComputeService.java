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

import static java.lang.String.format;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.time.Instant;
import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentHashMap.KeySetView;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.jena.atlas.io.IOX;
import org.apache.jena.atlas.lib.DateTimeUtils;
import org.apache.jena.fuseki.FusekiException;
import org.apache.jena.fuseki.servlets.BaseActionREST;
import org.apache.jena.fuseki.servlets.HttpAction;
import org.apache.jena.geosparql.spatial.SpatialIndex;
import org.apache.jena.geosparql.spatial.SpatialIndexException;
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
import org.apache.jena.sparql.util.Context;
import org.apache.jena.web.HttpSC;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import jakarta.servlet.AsyncContext;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * Spatial index (re)computation service.
 */
public class SpatialIndexComputeService extends BaseActionREST {
    private static final Logger logger = LoggerFactory.getLogger(SpatialIndexComputeService.class);

    private Gson gson = new Gson();
    private KeySetView<AsyncContext, ?> eventListeners = ConcurrentHashMap.newKeySet();

    public SpatialIndexComputeService() {}

    private static Set<String> getGraphs(DatasetGraph dsg, HttpAction action) {
        String[] uris = action.getRequest().getParameterValues(HttpNames.paramGraph);
        return uris == null ? Set.of(): new LinkedHashSet<>(Arrays.asList(uris));
    }

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

    public void serveWebSite(HttpAction action) {
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
        String rawCommand = action.getRequestParameter("command");
        String command = Optional.ofNullable(rawCommand).orElse("none");
        switch (command) {
        case "index": doIndex(action); break;
        case "status": serveStatus(action); break;
        case "cancel": doStop(action); break;
        default: throw new UnsupportedOperationException("Unsupported operation: " + command);
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

    public void serveStatus(HttpAction action) {
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

    public TaskControl<?> scheduleTask(HttpAction action, SpatialIndexerComputation indexComputation, Path targetFile) {
        DatasetGraph dsg = action.getDataset();
        Context cxt = dsg.getContext();

        TaskControlOverAbortableThread<Thread> taskCtl = new TaskControlOverAbortableThread<>("Spatial Indexer Task");

        cxt.compute(SpatialIndexUtils.SPATIAL_INDEX_TASK_SYMBOL, (key, priorTaskObj) -> {
            TaskControl<?> priorTask = (TaskControl<?>)priorTaskObj;
            if (priorTask != null && !priorTask.isComplete()) {
                throw new RuntimeException("A spatial indexing task is already active for this dataset. Wait for completion or abort it.");
            }

            AbortableThread<?> thread = new AbortableThread<Object>() {
                @Override
                public void runActual() throws Exception {
                    broadcastTaskStart(getStartTime());
                    if (logger.isInfoEnabled()) {
                        logger.info("Indexing process started.");
                    }

                    // Uncomment to test artifical delays.
                    // Thread.sleep(5000);

                    SpatialIndexPerGraph index = indexComputation.call();
                    if (targetFile != null) {
                        index.setLocation(targetFile);
                        action.log.info("writing spatial index to disk at {}", targetFile.toAbsolutePath());
                        SpatialIndexIoKryo.save(targetFile, index);
                    }
                    if (logger.isInfoEnabled()) {
                        logger.info("Indexing process completed successfully.");
                    }
                }

                public void requestCancel() {
                    broadcastTaskAbort(getCancelTime());
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
                            broadcastTaskEnd(endTime, throwable);
                        } finally {
                            if (logger.isInfoEnabled()) {
                                logger.info("Indexing process terminated.");
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

    protected void doIndex(HttpAction action) {
        DatasetGraph dsg = action.getDataset();
        Set<String> graphs;
        action.beginRead();
        try {
            graphs = getGraphs(dsg, action);
        } finally {
            action.end();
        }

        SpatialIndex indexTmp = SpatialIndexUtils.getSpatialIndex(dsg.getContext());
        SpatialIndexPerGraph index = (SpatialIndexPerGraph)indexTmp;
        if (index == null) { // error: no spatial index has been configured
            String msg = format("[%d] no spatial index has been configured for the dataset", action.id);
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
            Path oldLocation = index.getLocation();
            if (oldLocation == null) {
                action.log.warn("Skipping write: Spatial index write requested, but the spatial index was configured without a file location" +
                        " and no file param has been provided to the request neither. Skipping");
            }
            action.log.info(format("[%d] spatial index: computation started", action.id));

            String srsURI = index.getSrsInfo().getSrsURI();
            boolean parallel = true;
            List<Node> graphNodes = graphs.stream().map(NodeFactory::createURI).toList();
            SpatialIndexerComputation task = new SpatialIndexerComputation(dsg, srsURI, graphNodes, parallel);
            scheduleTask(action, task, oldLocation);
        }

        action.log.info(format("[%d] spatial index: computation finished", action.id));
        action.setResponseStatus(HttpSC.OK_200);
        action.setResponseContentType(WebContent.contentTypeTextPlain);
        try {
            action.getResponseOutputStream().print("Spatial index computation task accepted at " + DateTimeUtils.nowAsXSDDateTimeString());
        } catch (IOException e) {
            throw new FusekiException(e);
        }
    }

    public static boolean saveIndexCarefully(Path file, SpatialIndexPerGraph index, Logger log) throws SpatialIndexException {
        file = file.toAbsolutePath();
        Path tmpFile = IOX.uniqueDerivedPath(file, null);

        try {
            try (OutputStream out = new BufferedOutputStream(Files.newOutputStream(tmpFile))) {
                SpatialIndexIoKryo.writeToOutputStream(out, index);
            }
            Files.move(tmpFile, file, StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.REPLACE_EXISTING);
            return true;
        } catch (IOException ex) {
            log.error("Failed to save spatial index.", ex);
            throw new SpatialIndexException("Save Exception: " + ex.getMessage(), ex);
        } finally {
            log.info("Saving Spatial Index - Completed: {}", file);
        }
    }

    protected void broadcastTaskStart(Instant time) {
        JsonObject json = new JsonObject();
        json.addProperty("isIndexing", true);
        json.addProperty("time", time.toEpochMilli());
        // TODO We should have a task ID.
        broadcastJson(json);
    }
    protected void broadcastTaskAbort(Instant time) {
        JsonObject json = new JsonObject();
        json.addProperty("isIndexing", true);
        json.addProperty("isAborting", true);
        json.addProperty("time", time.toEpochMilli());
        // TODO We should have a task ID.
        broadcastJson(json);
    }

    protected void broadcastTaskEnd(Instant time, Throwable throwable) {
        JsonObject json = new JsonObject();
        json.addProperty("isIndexing", false);
        json.addProperty("time", time.toEpochMilli());
        if (throwable != null) {
            json.addProperty("error", ExceptionUtils.getStackTrace(throwable));
        }
        broadcastJson(json);
    }

    protected void broadcastJson(JsonElement jsonData) {
        String str = gson.toJson(jsonData);
        broadcastLine(str);
    }

    /** Argument is expected to not contain newline characters. */
    protected void broadcastLine(String line) {
        Iterator<AsyncContext> it = eventListeners.getMap().keySet().iterator();
        while (it.hasNext()) {
            AsyncContext context = it.next();
            try {
                PrintWriter writer = context.getResponse().getWriter();
                // SSE protocol mandantes format "data: payload\n\n".
                writer.println("data: " + line);
                writer.println();
                writer.flush();
            } catch (IOException e) {
                it.remove();
                context.complete();
            }
        }
    }
}
