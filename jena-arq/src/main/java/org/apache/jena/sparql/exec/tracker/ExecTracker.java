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

import java.time.Duration;
import java.time.Instant;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentNavigableMap;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.concurrent.atomic.AtomicLong;

import org.apache.jena.sparql.SystemARQ;
import org.apache.jena.sparql.util.Context;
import org.apache.jena.sparql.util.Symbol;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ExecTracker {
    private static final Logger logger = LoggerFactory.getLogger(ExecTracker.class);


    private Set<ExecTrackerListener> eventListeners = Collections.synchronizedSet(Collections.newSetFromMap(new IdentityHashMap<>()));

    protected AtomicLong nextId = new AtomicLong();
    protected ConcurrentNavigableMap<Long, StartRecord> idToStartRecord = new ConcurrentSkipListMap<>();
    protected int maxHistorySize = 1000;
    protected ConcurrentNavigableMap<Long, FinishRecord> history = new ConcurrentSkipListMap<>();

    public ConcurrentNavigableMap<Long, StartRecord> getActiveTasks() {
        return idToStartRecord;
    }

    public ConcurrentNavigableMap<Long, FinishRecord> getHistory() {
        return history;
    }

    public void setMaxHistorySize(int maxHistorySize) {
        this.maxHistorySize = maxHistorySize;
    }

    public long put(Object requestObject, Runnable abortAction) {
        long result = nextId.getAndIncrement();
        StartRecord record = new StartRecord(result, Instant.now(), requestObject, abortAction);
        idToStartRecord.put(result, record);
        broadcastStartEvent(record);
        return result;
    }

    protected void trimHistory() {
        if (history.size() >= maxHistorySize) {
            Iterator<Entry<Long, FinishRecord>> it = history.entrySet().iterator();
            while (history.size() >= maxHistorySize && it.hasNext()) {
                it.next();
                it.remove();
            }
        }
    }

    public FinishRecord remove(long id, Throwable t) {
        StartRecord startRecord = idToStartRecord.remove(id);
        FinishRecord result = null;
        if (startRecord != null) {
            trimHistory();
            Instant now = Instant.now();
            result = new FinishRecord(startRecord, now, t);
            // long requestId = startRecord.requestId();
            // history.put(now, result);
            history.put(id, result);
            broadcastCompletionEvent(result);
        }
        return result;
    }

    protected void broadcastStartEvent(StartRecord startRecord) {
        for (ExecTrackerListener listener : eventListeners) {
            try {
                listener.onQueryExecStart(startRecord);
            } catch (Throwable t) {
                if (logger.isWarnEnabled()) {
                    logger.warn("Failure during event handler.", t);
                }
            }
        }
    }

    protected void broadcastCompletionEvent(FinishRecord completionRecord) {
        for (ExecTrackerListener listener : eventListeners) {
            try {
                listener.onQueryExecFinish(completionRecord);
            } catch (Throwable t) {
                if (logger.isWarnEnabled()) {
                    logger.warn("Failure during event handler.", t);
                }
            }
        }
    }

    public Runnable addListener(ExecTrackerListener listener) {
        Objects.requireNonNull(listener);
        eventListeners.add(listener);
        return () -> eventListeners.remove(listener);
    }

    public Set<ExecTrackerListener> getEventListeners() {
        return eventListeners;
    }

    @Override
    public String toString() {
        return "Active: " + idToStartRecord.size() + ", History: " + history.size() + "/" + maxHistorySize;
    }

    // --- ARQ Integration ---

    public static final Symbol symTracker = SystemARQ.allocSymbol("execTracker");

    public static ExecTracker getTracker(Context context) {
        return context.get(symTracker);
    }

    public static ExecTracker requireTracker(Context context) {
        ExecTracker result = getTracker(context);
        Objects.requireNonNull("No ExecTracker registered in context");
        return result;
    }

    public static ExecTracker ensureTracker(Context context) {
        ExecTracker result = context.computeIfAbsent(symTracker, sym -> new ExecTracker());
        return result;
    }
}
