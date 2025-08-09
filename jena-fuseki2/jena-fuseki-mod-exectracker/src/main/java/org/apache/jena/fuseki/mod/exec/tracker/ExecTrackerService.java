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

package org.apache.jena.fuseki.mod.exec.tracker;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.io.IOUtils;
import org.apache.jena.fuseki.FusekiException;
import org.apache.jena.fuseki.server.Endpoint;
import org.apache.jena.fuseki.servlets.BaseActionREST;
import org.apache.jena.fuseki.servlets.HttpAction;
import org.apache.jena.riot.WebContent;
import org.apache.jena.sparql.exec.tracker.BasicTaskExec;
import org.apache.jena.sparql.exec.tracker.TaskEventHistory;
import org.apache.jena.sparql.exec.tracker.TaskListener;
import org.apache.jena.sparql.util.Context;
import org.apache.jena.web.HttpSC;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.internal.bind.JsonTreeWriter;
import com.google.gson.stream.JsonWriter;

import jakarta.servlet.AsyncContext;
import jakarta.servlet.AsyncEvent;
import jakarta.servlet.AsyncListener;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * REST action handler for listing running query executions and stopping them.
 */
public class ExecTrackerService extends BaseActionREST {
    private static final Logger logger = LoggerFactory.getLogger(ExecTrackerService.class);

    // Gson for formatting server side event (SSE) JSON.
    // Note: Pretty printing breaks SSE events due to newlines!
    private static Gson gsonForSseEvents = new Gson();

    /** Helper class to track SSE clients. */
    private class Clients {
        // Lock to prevent concurrent addition/removal of listeners while broadcasting events.
        Object listenerLock = new Object();

        // The endpoint of the clients.
        Endpoint endpoint;

        // Single listener on an TaskTrackerRegistry. - Not needed here; this listener initialized during FMOD init.
        Runnable taskTrackerListenerDisposer;

        // Web clients on the ExecTracker.
        Map<AsyncContext, Runnable> eventListeners = Collections.synchronizedMap(new IdentityHashMap<>()); // new ConcurrentHashMap<>();

        // The history tracker connected to the taskTracker.
        // TaskEventHistory historyTracker;
    }

    /** Registered clients listening to server side events for indexer status updates. */
    private Map<TaskEventHistory, Clients> trackerToClients = new ConcurrentHashMap<>(); // Collections.synchronizedMap(new IdentityHashMap<>());

    public ExecTrackerService() {}

    private static long getExecId(HttpAction action) {
        String str = action.getRequest().getParameter("requestId");
        Objects.requireNonNull(str);
        long result = Long.parseLong(str);
        return result;
    }

    /**
     * The GET command can serve: the website, the notification stream from task execution
     * and the latest task execution status.
     */
    @Override
    protected void doGet(HttpAction action) {
        String rawCommand = action.getRequestParameter("command");
        String command = Optional.ofNullable(rawCommand).orElse("page");
        switch (command) {
        case "page": servePage(action); break;
        case "events": serveEvents(action); break;
        case "status": serveStatus(action); break;
        case "stop": stopExec(action); break;
        default:
            throw new UnsupportedOperationException("Unsupported operation: " + command);
        }
    }

    protected void stopExec(HttpAction action) {
        checkIsAbortAllowed(action);

        long execId = getExecId(action);

        TaskEventHistory taskEventHistory = requireTaskEventHistory(action);
        BasicTaskExec task = taskEventHistory.getTaskBySerialId(execId);

        if (task != null) {
            try {
                task.abort();
            } catch (Throwable t) {
                logger.warn("Exception raised during abort.", t);
            }
            logger.info("Sending stop request to execution: " + execId);
        } else {
            logger.warn("No such execution to abort: " + execId);
        }

        respond(action, HttpSC.OK_200, WebContent.contentTypeTextPlain, "Abort request accepted.");
    }

    protected void servePage(HttpAction action) {
        // Serves the minimal graphql ui
        String resourceName = "exec-tracker/index.html";
        String str = null;
        try (InputStream in = ExecTrackerService.class.getClassLoader().getResourceAsStream(resourceName)) {
            str = IOUtils.toString(in, StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new FusekiException(e);
        }

        if (str == null) {
            respond(action, HttpSC.INTERNAL_SERVER_ERROR_500, WebContent.contentTypeTextPlain,
                "Failed to load classpath resource " + resourceName);
        } else {
            respond(action, HttpSC.OK_200, WebContent.contentTypeHTML, str);
        }
    }

    protected TaskEventHistory requireTaskEventHistory(HttpAction action) {
        Context cxt = action.getEndpoint().getContext();
        TaskEventHistory taskEventHistory = TaskEventHistory.require(cxt);
        return taskEventHistory;
    }

    protected Runnable registerTaskEventListener(TaskEventHistory taskEventHistory, Clients clients) {
        // Register the SSE handler to the history tracker
        InternalListener listener = new InternalListener(taskEventHistory, clients);
        Runnable disposeTaskEventListener = taskEventHistory.addListener(BasicTaskExec.class, listener);
        return disposeTaskEventListener;
    }

    protected class InternalListener implements TaskListener<BasicTaskExec> {
        protected TaskEventHistory taskEvenHistory;
        protected Clients clients;

        Long getTaskId(BasicTaskExec task) {
            long taskId = taskEvenHistory.getId(task);
            Long serialId = taskEvenHistory.getSerialId(taskId);
            return serialId;
        }

        public InternalListener(TaskEventHistory taskEventHistory, Clients clients) {
            super();
            this.taskEvenHistory = taskEventHistory;
            this.clients = clients;
        }

        @Override
        public void onStateChange(BasicTaskExec task) {
            switch (task.getTaskState()) {
            case STARTING: onStart(task); break;
            case TERMINATED: onTerminated(task); break;
            default: // ignored
            }
        }

        public void onStart(BasicTaskExec startRecord) {
            Long serialId = getTaskId(startRecord);
            if (serialId != null) {
                try (JsonTreeWriter writer = new JsonTreeWriter()) {
                    writer.beginObject();
                    TaskStatusWriter.writeStartRecordMembers(writer, serialId, startRecord);
                    TaskStatusWriter.writeCanAbort(writer, isAbortAllowed(clients.endpoint));
                    writer.endObject();
                    JsonElement json = writer.get();
                    synchronized (clients.listenerLock) {
                        Iterator<Entry<AsyncContext, Runnable>> it = clients.eventListeners.entrySet().iterator();
                        broadcastJson(it, json);
                    }
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        }

        public void onTerminated(BasicTaskExec endRecord) {
            Long serialId = getTaskId(endRecord);
            if (serialId != null) {
                try (JsonTreeWriter writer = new JsonTreeWriter()) {
                    TaskStatusWriter.writeCompletionRecordObject(writer, serialId, endRecord);
                    JsonElement json = writer.get();
                    synchronized (clients.listenerLock) {
                        Iterator<Entry<AsyncContext, Runnable>> it = clients.eventListeners.entrySet().iterator();
                        broadcastJson(it, json);
                    }
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        }
    }
    protected void serveEvents(HttpAction action) {
        HttpServletRequest request = action.getRequest();
        HttpServletResponse response = action.getResponse();
        Endpoint endpoint = action.getEndpoint();

        response.setContentType("text/event-stream");
        response.setCharacterEncoding("UTF-8");
        response.setHeader("Cache-Control", "no-cache");

        AsyncContext asyncContext = request.startAsync();
        asyncContext.setTimeout(0);

        TaskEventHistory taskTracker = requireTaskEventHistory(action);

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
            trackerToClients.compute(taskTracker, (et, clts) -> {
                Clients r = clts;
                if (clts != null) {
                    synchronized (clts.listenerLock) {
                        // Remove the listener for the async context.
                        clts.eventListeners.remove(asyncContext);

                        // If no more listeners remain then dispose the exec tracker listener.
                        if (clts.eventListeners.isEmpty()) {
                            clts.taskTrackerListenerDisposer.run();
                            r = null;
                        }
                    }
                }
                return r;
            });
        };

        // Atomically set up the new listener.
        trackerToClients.compute(taskTracker, (et, clients) -> {
            if (clients == null) {
                clients = new Clients();
                clients.endpoint = endpoint;
                Runnable disposer = registerTaskEventListener(taskTracker, clients);
                // clients.eventListeners.put(asyncContext, disposer);
                clients.taskTrackerListenerDisposer = disposer;
            }
            synchronized (clients.listenerLock) {
                clients.eventListeners.put(asyncContext, disposeSseListener[0]);
            }
            return clients;
        });
    }

    /** Check whether abort is allowed in the action's endpoint context. */
    protected static boolean isAbortAllowed(HttpAction action) {
        Endpoint endpoint = action.getEndpoint();
        return isAbortAllowed(endpoint);
    }

    /** Check whether abort is allowed in the endpoint's context. */
    protected static boolean isAbortAllowed(Endpoint endpoint) {
        Context cxt = endpoint == null ? null : endpoint.getContext();
        return isAbortAllowed(cxt);
    }

    /** Check whether abort is allowed in the context. True if not explicitly prohibited. */
    protected static boolean isAbortAllowed(Context cxt) {
        boolean result = cxt == null ? true : cxt.isTrueOrUndef(FMod_ExecTracker.symAllowAbort);
        return result;
    }

    public static void checkIsAbortAllowed(HttpAction action) {
        boolean isAbortAllowed = isAbortAllowed(action);
        if (!isAbortAllowed) {
            throw new UnsupportedOperationException("Abort is not allowed.");
        }
    }

    public static void setAllowAbort(Context cxt, Boolean value) {
        cxt.set(FMod_ExecTracker.symAllowAbort, value);
    }

    /**
     * Serves a JSON object with the running and recently completed tasks.
     */
    protected void serveStatus(HttpAction action) {
        boolean isAbortAllowed = isAbortAllowed(action);

        TaskEventHistory taskEventHistory = requireTaskEventHistory(action);

        action.setResponseStatus(HttpSC.OK_200);
        action.setResponseContentType(WebContent.contentTypeJSON);
        try {
            OutputStream out = action.getResponseOutputStream();
            JsonWriter writer = gsonForSseEvents.newJsonWriter(new OutputStreamWriter(out));
            new TaskStatusWriter(100, isAbortAllowed).writeStatusObject(writer, taskEventHistory);
            writer.flush();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static void respond(HttpAction action, int status, String contentType, String value) {
        action.setResponseStatus(status);
        action.setResponseContentType(contentType);
        try {
            OutputStream out = action.getResponseOutputStream();
            IOUtils.write(value, out, StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    protected void broadcastJson(Iterator<Entry<AsyncContext, Runnable>> it, JsonElement jsonData) {
        String str = gsonForSseEvents.toJson(jsonData);
        broadcastLine(it, str);
    }

    /**
     * Broadcast a payload to all registered listeners.
     * @param payload A string without newline characters.
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
}
