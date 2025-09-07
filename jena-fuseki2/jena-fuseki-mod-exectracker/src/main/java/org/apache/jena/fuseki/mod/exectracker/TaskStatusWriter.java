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

package org.apache.jena.fuseki.mod.exectracker;

import java.io.IOException;
import java.util.Map.Entry;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.jena.sparql.exec.tracker.BasicTaskExec;
import org.apache.jena.sparql.exec.tracker.TaskEventHistory;

import com.google.gson.stream.JsonWriter;

public class TaskStatusWriter {

    protected int maxHistorySize;
    protected boolean allowAbort;

    /**
     * Create a writer for writing out ExecTracker state as JSON.
     *
     * @param maxHistorySize Caps the number of completed tasks to write out.
     */
    public TaskStatusWriter(int maxHistorySize, boolean allowAbort) {
        super();
        this.maxHistorySize = maxHistorySize;
        this.allowAbort = allowAbort;
    }

    public void writeStatusObject(JsonWriter writer, TaskEventHistory execTracker) throws IOException {
        writer.beginObject();
        writeStatusMembers(writer, execTracker);
        writer.endObject();
    }

    public void writeStatusMembers(JsonWriter writer, TaskEventHistory execTracker) throws IOException {
        writer.name("runningTasks");
        writer.beginArray();
        for (Entry<Long, BasicTaskExec> entry : execTracker.getActiveTasks().entrySet()) {
            long id = entry.getKey();
            BasicTaskExec item = entry.getValue();
            writer.beginObject();
            writeStartRecordMembers(writer, id, item);
            writeCanAbort(writer, allowAbort);
            writer.endObject();
        }
        writer.endArray();

        writer.name("completedTasks");
        writer.beginArray();
        Iterable<Entry<Long, BasicTaskExec>> recentHistory = () ->
            execTracker.getHistory().stream().limit(maxHistorySize).iterator();
        for (Entry<Long, BasicTaskExec> entry : recentHistory) {
            long id = entry.getKey();
            BasicTaskExec item = entry.getValue();
            writeCompletionRecordObject(writer, id, item);
        }
        writer.endArray();
    }

    public static void writeStartRecordObject(JsonWriter writer, Long id, BasicTaskExec item) throws IOException {
        writer.beginObject();
        writeStartRecordMembers(writer, id, item);
        writer.endObject();
    }

    public static void writeStartRecordMembers(JsonWriter writer, Long id, BasicTaskExec item) throws IOException {
        writer.name("type");
        writer.value("StartRecord");

        writer.name("requestId");
        // long id = System.identityHashCode(item);
        writer.value(id);

        writer.name("payload");
        writePayloadObject(writer, item);

        writer.name("timestamp");
        writer.value(item.getStartTime());
    }

    public static void writeCanAbort(JsonWriter writer, Boolean canAbort) throws IOException {
        if (canAbort != null) {
            writer.name("canAbort");
            writer.value(canAbort);
        }
    }

    public static void writePayloadObject(JsonWriter writer, BasicTaskExec item) throws IOException {
        writer.beginObject();
        writePayloadMembers(writer, item);
        writer.endObject();
    }

    public static void writePayloadMembers(JsonWriter writer, BasicTaskExec item) throws IOException {
        // XXX Change to description
        String label = item.getLabel();
        writer.name("label");
        writer.value(label);
    }

    public static void writeCompletionRecordObject(JsonWriter writer, long id, BasicTaskExec item) throws IOException {
        writer.beginObject();
        writeCompletionRecordMembers(writer, id, item);
        writer.endObject();
    }

    public static void writeCompletionRecordMembers(JsonWriter writer, long id, BasicTaskExec item) throws IOException {
        writer.name("type");
        writer.value("CompletionRecord");

        writer.name("startRecord");
        writeStartRecordObject(writer, id, item);

        Throwable throwable = item.getThrowable();
        if (throwable != null) {
            String errorMessage = ExceptionUtils.getStackTrace(throwable);
            writer.name("error");
            writer.value(errorMessage);
        }

        writer.name("timestamp");
        writer.value(item.getFinishTime());
    }
}
