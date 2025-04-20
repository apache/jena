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
import java.util.Objects;
import java.util.function.Consumer;

public class TaskControlOverAbortableThread<S>
    implements TaskControl<S>
{
    protected String label;
    protected S source;
    protected AbortableThread<?> thread;

    public TaskControlOverAbortableThread(String label) {
        super();
        this.label = label;
    }

    public void setThread(AbortableThread<?> thread) {
        this.thread = thread;
    }

    public void setSource(S source) {
        this.source = source;
    }

    @Override
    public String getLabel() {
        return label;
    }

    @Override
    public S getSource() {
        return source;
    }

    protected AbortableThread<?> requireThread() {
        Objects.requireNonNull(thread);
        return thread;
    }

    @Override
    public Instant getStartTime() {
        return requireThread().getStartTime();
    }

    @Override
    public Instant getEndTime() {
        return requireThread().getEndTime();
    }

    @Override
    public Instant getCancelTime() {
        return requireThread().getCancelTime();
    }

    @Override
    public void abort() {
        requireThread().cancel();
    }

    @Override
    public boolean isAborting() {
        return requireThread().isCancelled();
    }

    @Override
    public boolean isComplete() {
        return !requireThread().isAlive();
    }

    @Override
    public Throwable getThrowable() {
        return requireThread().getThrowable();
    }

    @Override
    public Registration whenComplete(Consumer<Throwable> action) {
        // Note: The thread's result value is discarded here.
        return requireThread().whenComplete((v, t) -> action.accept(t));
    }

    @Override
    public String getStatusMessage() {
        return requireThread().getStatusMessage();
    }
}
