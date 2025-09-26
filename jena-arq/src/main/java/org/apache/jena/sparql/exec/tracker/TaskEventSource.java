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

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TaskEventSource {
    private static final Logger logger = LoggerFactory.getLogger(TaskEventSource.class);

    // LinkedHashMap to retain listener order.
    protected Map<TaskListener<?>, TaskListener<BasicTaskExec>> listenersByType =
            Collections.synchronizedMap(new LinkedHashMap<>());

    public <Y extends BasicTaskExec> Runnable addListener(Class<Y> clz, TaskListener<? super Y> listener) {
        listenersByType.compute(listener, (k, v) -> {
            if (v != null) {
                throw new RuntimeException("Listener already registered");
            }
            return new TaskListenerTypeAdapter<>(clz, listener);
        });
        return () -> listenersByType.remove(listener);
    }

    protected void advertiseStateChange(BasicTaskExec task) {
        for (TaskListener<BasicTaskExec> listener : listenersByType.values()) {
            try {
                listener.onStateChange(task);
            } catch (Throwable t) {
                logger.warn("Failure while notifying listener.", t);
            }
        }
    }

    class TaskListenerTypeAdapter<Y extends BasicTaskExec>
        implements TaskListener<BasicTaskExec>
    {
        protected Class<Y> clz;
        protected TaskListener<? super Y> delegate;

        public TaskListenerTypeAdapter(Class<Y> clz, TaskListener<? super Y> delegate) {
            super();
            this.clz = clz;
            this.delegate = delegate;
        }

        @Override
        public void onStateChange(BasicTaskExec task) {
            if (clz.isInstance(task)) {
                Y obj = clz.cast(task);
                delegate.onStateChange(obj);
            }
        }
    }
}
