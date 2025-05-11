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
import java.util.Objects;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.jena.sparql.exec.tracker.ExecTracker;
import org.apache.jena.sparql.exec.tracker.ExecTracker.CompletionRecord;
import org.apache.jena.sparql.exec.tracker.ExecTracker.StartRecord;

import com.google.gson.stream.JsonWriter;

public class ExecTrackerWriter {

    protected int maxHistorySize;

    /**
     * Create a writer for writing out ExecTracker state as JSON.
     *
     * @param maxHistorySize Caps the number of completed tasks to write out.
     */
    public ExecTrackerWriter(int maxHistorySize) {
        super();
        this.maxHistorySize = maxHistorySize;
    }

    public void writeStatusObject(JsonWriter writer, ExecTracker execTracker) throws IOException {
        writer.beginObject();
        writeStatusMembers(writer, execTracker);
        writer.endObject();
    }

    public void writeStatusMembers(JsonWriter writer, ExecTracker execTracker) throws IOException {
        writer.name("runningTasks");
        writer.beginArray();
        for (StartRecord item : execTracker.getActiveTasks().values()) {
            writeStartRecordObject(writer, item);
        }
        writer.endArray();

        writer.name("completedTasks");
        writer.beginArray();
        Iterable<CompletionRecord> recentHistory = () -> execTracker.getHistory().descendingMap().values().stream().limit(maxHistorySize).iterator();
        for (CompletionRecord item : recentHistory) {
            writeCompletionRecordObject(writer, item);
        }
        writer.endArray();
    }

    public static void writeStartRecordObject(JsonWriter writer, StartRecord item) throws IOException {
        writer.beginObject();
        writeStartRecordMembers(writer, item);
        writer.endObject();
    }

    public static void writeStartRecordMembers(JsonWriter writer, StartRecord item) throws IOException {
        writer.name("type");
        writer.value("StartRecord");

        writer.name("requestId");
        writer.value(item.requestId());

        writer.name("payload");
        writePayloadObject(writer, item);

        writer.name("timestamp");
        writer.value(item.timestamp().toEpochMilli());
    }

    public static void writePayloadObject(JsonWriter writer, StartRecord item) throws IOException {
        writer.beginObject();
        writePayloadMembers(writer, item);
        writer.endObject();
    }

    public static void writePayloadMembers(JsonWriter writer, StartRecord item) throws IOException {
        String label = Objects.toString(item.requestObject());
        writer.name("label");
        writer.value(label);
    }

    public static void writeCompletionRecordObject(JsonWriter writer, CompletionRecord item) throws IOException {
        writer.beginObject();
        writeCompletionRecordMembers(writer, item);
        writer.endObject();
    }

    public static void writeCompletionRecordMembers(JsonWriter writer, CompletionRecord item) throws IOException {
        writer.name("type");
        writer.value("CompletionRecord");

        writer.name("startRecord");
        writeStartRecordObject(writer, item.start());

        Throwable throwable = item.throwable();
        if (throwable != null) {
            String errorMessage = ExceptionUtils.getStackTrace(throwable);
            writer.name("error");
            writer.value(errorMessage);
        }

        writer.name("timestamp");
        writer.value(item.timestamp().toEpochMilli());
    }
}
