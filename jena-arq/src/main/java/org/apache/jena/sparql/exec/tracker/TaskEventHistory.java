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
import java.util.NoSuchElementException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.ConcurrentNavigableMap;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.concurrent.atomic.AtomicLong;

import org.apache.jena.sparql.SystemARQ;
import org.apache.jena.sparql.util.Context;
import org.apache.jena.sparql.util.Symbol;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TaskEventHistory
    extends TaskEventBroker
{
    private static final Logger logger = LoggerFactory.getLogger(TaskEventHistory.class);

    // Relabel tasks by a sequential ids.
    // XXX Id allocation could be factored out in a central place. Fuseki allocates IDs too.
    protected AtomicLong nextSerial = new AtomicLong();
    protected Map<Long, Long> taskIdToSerial = new ConcurrentHashMap<>();
    protected ConcurrentNavigableMap<Long, BasicTaskExec> serialToTask = new ConcurrentSkipListMap<>();

    protected int maxHistorySize = 1000;

    // History is indexed by serial id.
    protected ConcurrentLinkedDeque<Entry<Long, BasicTaskExec>> history = new ConcurrentLinkedDeque<>();

    public BasicTaskExec getByTaskId(long taskId) {
        Long serial = taskIdToSerial.get(taskId);
        BasicTaskExec result = getTaskBySerialId(serial);
        return result;
    }

    public BasicTaskExec getTaskBySerialId(long serialId) {
        return serialToTask.get(serialId);
    }

    public ConcurrentNavigableMap<Long, BasicTaskExec> getActiveTasks() {
        return serialToTask;
    }

    public ConcurrentLinkedDeque<Entry<Long, BasicTaskExec>> getHistory() {
        return history;
    }

    public void setMaxHistorySize(int maxHistorySize) {
        this.maxHistorySize = maxHistorySize;
        trimHistory();
    }

    @Override
    public void onStateChange(BasicTaskExec task) {
        switch(task.getTaskState()) {
        case STARTING: put(task); break;
        case TERMINATED: remove(task); break;
        default: break;
        }
    }

    public long getId(BasicTaskExec task) {
        long id = System.identityHashCode(task);
        return id;
    }

    public Long getSerialId(long taskId) {
        return taskIdToSerial.get(taskId);
    }

    public void put(BasicTaskExec newTask) {
        long taskId = getId(newTask);
        boolean[] accepted = {false};
        taskIdToSerial.compute(taskId, (_taskId, oldSerial) -> {
            if (oldSerial != null) {
                BasicTaskExec oldTask = serialToTask.get(oldSerial);
                if (oldTask != newTask) {
                    // Distinct tasks with the same id - should never happen.
                    logger.warn("Rejected task tracking because of a hash clash.");
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
        if (history.size() > maxHistorySize) {
            Iterator<Entry<Long, BasicTaskExec>> it = history.descendingIterator();
            while (history.size() >= maxHistorySize && it.hasNext()) {
                // Note: No need to clean up taskIdToSerial here.
                //       Before items are added to the history, their serial id mapping is removed.
                it.next();
                it.remove();
            }
        }
    }

    public boolean remove(BasicTaskExec task) {
        long searchTaskId = getId(task);

        // Advertise state change before removing the task entry!
        Long foundTaskId = taskIdToSerial.get(searchTaskId);
        if (foundTaskId != null) {
            advertiseStateChange(task);
        }

        Long[] foundSerial = {null};
        taskIdToSerial.compute(searchTaskId, (_taskId, serial) -> {
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
            history.addFirst(Map.entry(foundSerial[0], task));
            trimHistory();
        }
        return result;
    }

    public void clear() {
        history.clear();
    }

    @Override
    public String toString() {
        return "Active: " + serialToTask.size() + ", History: " + history.size() + "/" + maxHistorySize;
    }

    // --- ARQ Integration ---

    public static final Symbol symTaskEventHistory = SystemARQ.allocSymbol("taskEventHistory");

    public static TaskEventHistory get(Context context) {
        return context == null ? null : context.get(symTaskEventHistory);
    }

    public static TaskEventHistory getOrCreate(Context context) {
        TaskEventHistory result = context.computeIfAbsent(symTaskEventHistory, sym -> new TaskEventHistory());
        return result;
    }

    public static TaskEventHistory require(Context context) {
        TaskEventHistory result = get(context);
        if (result == null) {
            throw new NoSuchElementException("No TaskEventHistory registered in context");
        }
        return result;
    }
}
