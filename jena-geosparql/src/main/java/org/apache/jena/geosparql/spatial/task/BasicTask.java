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

package org.apache.jena.geosparql.spatial.task;

import org.apache.jena.sparql.engine.iterator.Abortable;

/** An outside view of a running task */
public interface BasicTask extends Abortable {

    public interface TaskListener<T extends BasicTask> {
        void onStateChange(T task);
    }

    TaskState getTaskState();

    /** A label for the task. */
    String getLabel();

    @Override
    void abort();

    long getCreationTime();
    long getStartTime();
    long getEndTime();
    long getAbortTime();

    /** If non null, the throwable that is the cause for an exceptional termination of the task. */
    Throwable getThrowable();

    /** Get the last status message of the task. May be null. */
    String getStatusMessage();

    /** Whether abort has been called. */
    // XXX this might be different from whether the task actually transitioned into aborting state.
    boolean isAborting();

    default boolean isTerminated() {
        TaskState state = getTaskState();
        return TaskState.TERMINATED.equals(state);
    }
}
