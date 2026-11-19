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

package org.apache.jena.sparql.service.enhancer.concurrent;

import java.util.concurrent.locks.Lock;

public class AutoLock implements AutoCloseable {
    private final Lock lock;

    private AutoLock(Lock lock) {
        this.lock = lock;
    }

    /**
     * Immediately attempts to acquire the lock and returns
     * an auto-closeable AutoLock instance for use with try-with-resources.
     */
    public static AutoLock lock(Lock lock) {
        lock.lock();
        return new AutoLock(lock);
    }

    @Override
    public void close() {
        lock.unlock();
    }
}
