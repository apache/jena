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

package org.apache.jena.sparql.exec.tracker;

import java.util.Map;
import java.util.NoSuchElementException;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.jena.sparql.SystemARQ;
import org.apache.jena.sparql.core.DatasetGraph;
import org.apache.jena.sparql.exec.QueryExec;
import org.apache.jena.sparql.exec.UpdateExec;
import org.apache.jena.sparql.util.Context;
import org.apache.jena.sparql.util.Symbol;

/**
 * A broker that is both sink and source for task events.
 *
 * A broker can connect to other ones
 * using {@link #connect(TaskEventBroker)} and disconnect from
 * them all using {@link #disconnectFromAll()}.
 */
public class TaskEventBroker
    extends TaskEventSource
    implements TaskListener<BasicTaskExec>
{
    private Map<TaskListener<?>, Runnable> upstreamRegistrations = new ConcurrentHashMap<>();

    public Runnable connect(TaskEventBroker upstream) {
        Runnable unregisterFromBase = upstream.addListener(BasicTaskExec.class, this);
        Runnable unregisterFromThis = upstreamRegistrations.computeIfAbsent(upstream, u -> {
            return () -> {
                unregisterFromBase.run();
                upstreamRegistrations.remove(upstream);
            };
        });
        return unregisterFromThis;
    }

    @Override
    public void onStateChange(BasicTaskExec task) {
        advertiseStateChange(task);
    }

    public void disconnectFromAll() {
        upstreamRegistrations.values().forEach(Runnable::run);
    }

    public static QueryExec track(QueryExec queryExec) {
        Context cxt = queryExec.getContext();
        return track(cxt, queryExec);
    }

    /**
     * If there is a taskTracker in the context then return a {@link QueryExecTask}.
     * Otherwise return the provided query exec.
     */
    public static QueryExec track(Context cxt, QueryExec queryExec) {
        TaskEventBroker registry = get(cxt);
        QueryExec result = (registry == null)
            ? queryExec
            : QueryExecTask.create(queryExec, registry);
        return result;
    }

    public static UpdateExec track(UpdateExec updateExec) {
        Context cxt = updateExec.getContext();
        return track(cxt, updateExec);
    }

    /**
     * If there is a taskTracker in the context then return a {@link QueryExecTask}.
     * Otherwise return the provided query exec.
     */
    public static UpdateExec track(Context cxt, UpdateExec updateExec) {
        TaskEventBroker registry = get(cxt);
        return track(registry, updateExec);
    }

    public static UpdateExec track(TaskEventBroker tracker, UpdateExec updateExec) {
        UpdateExec result = (tracker == null)
            ? updateExec
            : UpdateExecTask.create(updateExec, tracker);
        return result;
    }

    // ----- ARQ Integration -----

    public static final Symbol symTaskEventBroker = SystemARQ.allocSymbol("taskEventBroker");

    public static TaskEventBroker get(DatasetGraph dsg) {
        return dsg == null ? null : get(dsg.getContext());
    }

    public static TaskEventBroker get(Context context) {
        return context == null ? null : context.get(symTaskEventBroker);
    }

    public static void remove(Context context) {
        if (context != null) {
            context.remove(symTaskEventBroker);
        }
    }

    /** Get an existing TaskEventBroker or atomically create a new one. */
    public static TaskEventBroker getOrCreate(Context context) {
        TaskEventBroker result = context.computeIfAbsent(symTaskEventBroker, sym -> new TaskEventBroker());
        return result;
    }

    /** Get an existing TaskEventBroker or fail with a {@link NoSuchElementException}. */
    public static TaskEventBroker require(Context context) {
        TaskEventBroker result = get(context);
        if (result == null) {
            throw new NoSuchElementException("No task event broker in context.");
        }
        return result;
    }
}
