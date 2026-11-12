/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 *
 *   SPDX-License-Identifier: Apache-2.0
 */

package org.apache.jena.sparql.service.enhancer.impl;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/** Utility wrapper for an ExecutorService to add synchronous API that abstracts away the Future. */
public class ExecutorServiceWrapperSync {
    protected ExecutorService executorService;

    public ExecutorServiceWrapperSync() {
        this(null);
    }

    public ExecutorServiceWrapperSync(ExecutorService es) {
        super();
        this.executorService = es;
    }

    public ExecutorService getExecutorService() {
        return executorService;
    }

    public void submit(Runnable runnable) {
        submit(() -> { runnable.run(); return null; });
    }

    public <T> T submit(Callable<T> callable) {
        if (executorService == null) {
            synchronized (this) {
                if (executorService == null) {
                    executorService = Executors.newSingleThreadExecutor();
                }
            }
        }
        T result = submit(executorService, callable);
        return result;
    }

    /** Execute the callable on the executor service and return its result. */
    public static <T> T submit(ExecutorService executorService, Callable<T> callable) {
        try {
            Future<T> future = executorService.submit(callable);
            T result = future.get();
            return result;
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException(e);
        }
    }
}
