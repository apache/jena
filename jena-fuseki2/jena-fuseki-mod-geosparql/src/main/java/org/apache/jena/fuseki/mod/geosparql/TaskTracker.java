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

import java.time.Duration;
import java.time.Instant;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.concurrent.atomic.AtomicLong;

import org.apache.jena.sparql.SystemARQ;
import org.apache.jena.sparql.util.Context;
import org.apache.jena.sparql.util.Symbol;

/**
 * A simple tracker for abortable tasks.
 * Task registration accepts an arbitrary object and a runnable for aborting the task.
 * The task itself is responsible for deregistration on its termination.
 * A record with starting and termination time is maintained by this class.
 */
public class TaskTracker {
    /** Record for when a task is submitted to the tracker. */
    public record StartRecord(long requestId, Instant timestamp, Object requestObject, Runnable abortAction) {}

    /** Record for when a task is completed. */
    public record CompletionRecord(StartRecord start, Instant timestamp, Throwable throwable) {
        public Duration duration() {
            return Duration.between(start.timestamp, timestamp);
        }

        public boolean isSuccess() {
            return throwable == null;
        }
    }

    protected AtomicLong nextId = new AtomicLong();
    protected ConcurrentMap<Long, StartRecord> idToStartRecord = new ConcurrentHashMap<>();
    protected int maxHistorySize = 1000;
    protected ConcurrentMap<Instant, CompletionRecord> history = new ConcurrentSkipListMap<>();

    public ConcurrentMap<Instant, CompletionRecord> getHistory() {
        return history;
    }

    public void setMaxHistorySize(int maxHistorySize) {
        this.maxHistorySize = maxHistorySize;
    }

    public long put(Object requestObject, Runnable abortAction) {
        long result = nextId.getAndIncrement();
        StartRecord record = new StartRecord(result, Instant.now(), requestObject, abortAction);
        idToStartRecord.put(result, record);
        return result;
    }

    protected void trimHistory() {
        if (history.size() >= maxHistorySize) {
            Iterator<Entry<Instant, CompletionRecord>> it = history.entrySet().iterator();
            while (history.size() >= maxHistorySize && it.hasNext()) {
                it.next();
                it.remove();
            }
        }
    }

    public CompletionRecord remove(long id, Throwable t) {
        StartRecord startRecord = idToStartRecord.remove(id);
        CompletionRecord result = null;
        if (startRecord != null) {
            trimHistory();
            Instant now = Instant.now();
            result = new CompletionRecord(startRecord, now, t);
            history.put(now, result);
        }
        return result;
    }

    @Override
    public String toString() {
        return "Active: " + idToStartRecord.size() + ", History: " + history.size() + "/" + maxHistorySize;
    }

    public static final Symbol symTaskTracker = SystemARQ.allocSymbol("taskTracker");

    public static TaskTracker getTracker(Context context) {
        return context.get(symTaskTracker);
    }

    public static TaskTracker requireTracker(Context context) {
        TaskTracker result = getTracker(context);
        Objects.requireNonNull("No ExecTracker registered in context");
        return result;
    }

    public static TaskTracker ensureTracker(Context context) {
        TaskTracker result = context.get(symTaskTracker);
        if (result == null) {
            synchronized (context) {
                result = context.get(symTaskTracker);
                if (result == null) {
                    result = new TaskTracker();
                    context.set(symTaskTracker, result);
                }
            }
        }
        return result;
    }
}
