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

public interface BasicTaskInfo {
    /** The state of the task. */
    TaskState getTaskState();

    /** Time stamp for when the task object was created. */
    long getCreationTime();

    /** Time stamp for when the task was started. Returns -1 if was not started yet.*/
    long getStartTime();

    /**
     * Time stamp for when the task completed. Returns -1 if it has not finished yet.
     */
    long getFinishTime();

    /** Time stamp for when the task was cancelled. Returns -1 if not aborted. */
    long getAbortTime();

    /**
     * Return a description suitable for presentation to users.
     * This might be a less technical description than what is returned by toString().
     */
    String getLabel();

    String getStatusMessage();

    /**
     * If this method returns a non-null result then the task is considered to have failed.
     * A non-null result does not imply that the task is already in TERMINATED state.
     */
    Throwable getThrowable();

    /** Whether abort has been called. */
    default boolean isAborting() {
        return getAbortTime() >= 0;
    }

    default boolean isTerminated() {
        TaskState state = getTaskState();
        return TaskState.TERMINATED.equals(state);
    }

}
