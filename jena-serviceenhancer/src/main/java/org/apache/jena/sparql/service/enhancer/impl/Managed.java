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

import java.util.Objects;
import java.util.function.Consumer;

import org.apache.jena.shared.ClosedException;

public class Managed<T> {
    private T resource;
    private Consumer<T> closer;

    private Object lock = new Object();
    private volatile boolean isClosed = false;

    public Managed(Consumer<T> closer) {
        super();
        this.closer = closer;
    }

    public static <T> Managed<T> of(Consumer<T> closer) {
        Objects.requireNonNull(closer);
        return new Managed<>(closer);
    }

    public T get() {
        checkOpen();
        return resource;
    }

    protected void checkOpen() {
        if (isClosed) {
            throw new ClosedException(null, null);
        }
    }

    public void set(T newResource) {
        checkOpen();
        synchronized (lock) {
            checkOpen();
            if (resource != newResource) {
                if (resource != null) {
                    try {
                        closer.accept(resource);
                    } finally {
                        resource = newResource;
                    }
                } else {
                    resource = newResource;
                }
            }
        }
    }

    public void close() {
        if (!isClosed) {
            synchronized (lock) {
                if (!isClosed) {
                    try {
                        if (resource != null) {
                            closer.accept(resource);
                        }
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    } finally {
                        isClosed = true;
                        resource = null;
                    }
                }
            }
        }
    }
}
