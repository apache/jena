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

import java.time.Instant;
import java.util.function.Consumer;

/** An outside view of a running task */
public interface TaskControl<S> {
    @FunctionalInterface
    interface Registration {
        void dispose();
    }

    String getLabel();
    S getSource();
    void abort();
    boolean isAborting();

    boolean isComplete();
    Throwable getThrowable(); // Only meaningful if isComplete is true.

    Instant getStartTime();
    Instant getEndTime();
    Instant getCancelTime();

    /** Get the last status message of the task. May be null. */
    String getStatusMessage();

    /**
     * Registered actions are run only once, then the registration is removed automatically.
     *
     * @param action The action is invoked with any thrown exception - null if there was none.
     * @return A registration that can be used to unregister the listener early.
     */
    Registration whenComplete(Consumer<Throwable> action);
}
