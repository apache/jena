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

import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentNavigableMap;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.concurrent.atomic.AtomicLong;

import org.apache.jena.sparql.SystemARQ;
import org.apache.jena.sparql.util.Context;
import org.apache.jena.sparql.util.Symbol;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HistoryTrackerRegistry
    extends TaskTrackerRegistry
{
    // TODO Ideally there would be something like derivedTracker.disconnect();

    private static final Logger logger = LoggerFactory.getLogger(HistoryTrackerRegistry.class);

    public static final Symbol symTaskHistoryTrackerRegistry = SystemARQ.allocSymbol("taskHistoryTrackerRegistry");

    public static HistoryTrackerRegistry get(Context context) {
        return context == null ? null : context.get(symTaskHistoryTrackerRegistry);
    }

    public static HistoryTrackerRegistry getOrSet(Context context) {
        HistoryTrackerRegistry result = context == null ? null : context.computeIfAbsent(symTaskHistoryTrackerRegistry, sym -> new HistoryTrackerRegistry());
        return result;
    }

    public static HistoryTrackerRegistry require(Context context) {
        HistoryTrackerRegistry result = get(context);
        if (result == null) {
            throw new RuntimeException("No exec listener in context.");
        }
        return result;
    }

    // Relabel tasks by a sequential ids.
    protected AtomicLong nextSerial = new AtomicLong();
    protected Map<Long, Long> taskIdToSerial = new ConcurrentHashMap<>();
    protected ConcurrentNavigableMap<Long, BasicTaskExec> serialToTask = new ConcurrentSkipListMap<>();

    // Indexed by serial
    protected int maxHistorySize = 1000;
    protected ConcurrentNavigableMap<Long, BasicTaskExec> history = new ConcurrentSkipListMap<>();

    public BasicTaskExec getByTaskId(long taskId) {
        Long serial = taskIdToSerial.get(taskId);
        BasicTaskExec result = serialToTask.get(serial);
        return result;
    }

    public ConcurrentNavigableMap<Long, BasicTaskExec> getActiveTasks() {
        return serialToTask;
    }

    public ConcurrentNavigableMap<Long, BasicTaskExec> getHistory() {
        return history;
    }

    public void setMaxHistorySize(int maxHistorySize) {
        this.maxHistorySize = maxHistorySize;
    }

    @Override
    public void onStateChange(BasicTaskExec task) {
        switch(task.getState()) {
        case STARTING: put(task); break;
        case TERMINATED: remove(task); break;
        default: break;
        }
    }

    protected long getId(BasicTaskExec task) {
        long id = System.identityHashCode(task);
        return id;
    }

    public void put(BasicTaskExec newTask) {
        long taskId = getId(newTask);
        boolean[] accepted = {false};
        taskIdToSerial.compute(taskId, (_taskId, oldSerial) -> {
            if (oldSerial != null) {
                BasicTaskExec oldTask = serialToTask.get(oldSerial);
                if (oldTask != newTask) {
                    logger.warn("Rejected task tracking because of a very rare hash clash.");
                } else {
                    logger.warn("Task was already added.");
                }
                return oldSerial;
            }

            accepted[0] = true;
            long r = nextSerial.incrementAndGet();
            serialToTask.put(r, newTask);
            return r;
        });
        if (accepted[0]) {
            advertiseStateChange(newTask);
        }
    }

    protected void trimHistory() {
        if (history.size() >= maxHistorySize) {
            Iterator<Entry<Long, BasicTaskExec>> it = history.entrySet().iterator();
            while (history.size() >= maxHistorySize && it.hasNext()) {
                Entry<Long, BasicTaskExec> task = it.next();
                long taskId = getId(task.getValue());
                taskIdToSerial.remove(taskId);
                it.remove();
            }
        }
    }

    public boolean remove(BasicTaskExec task) {
        long taskId = getId(task);
        Long[] foundSerial = {null};
        taskIdToSerial.compute(taskId, (_taskId, serial) -> {
            if (serial != null) {
                serialToTask.compute(serial, (s, oldTask) -> {
                    if (oldTask == task) {
                        foundSerial[0] = s;
                        return null;
                    }
                    return oldTask;
                });
                return foundSerial[0] != null ? null : serial;
            }
            return serial;
        });

        boolean result = foundSerial[0] != null;
        if (result) {
            history.put(foundSerial[0], task);
            advertiseStateChange(task);
        }
        return result;
    }

    @Override
    public String toString() {
        return "Active: " + serialToTask.size() + ", History: " + history.size() + "/" + maxHistorySize;
    }

    // --- ARQ Integration ---

    public static final Symbol symTracker = SystemARQ.allocSymbol("execTracker");

    public static HistoryTrackerRegistry getTracker(Context context) {
        return context.get(symTracker);
    }

    public static HistoryTrackerRegistry requireTracker(Context context) {
        HistoryTrackerRegistry result = getTracker(context);
        Objects.requireNonNull("No ExecTracker registered in context");
        return result;
    }

    public static HistoryTrackerRegistry ensureTracker(Context context) {
        HistoryTrackerRegistry result = context.computeIfAbsent(symTracker, sym -> new HistoryTrackerRegistry());
        return result;
    }
}
