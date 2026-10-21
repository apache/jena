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

package org.apache.jena.sparql.service.enhancer.claimingcache;

import java.util.function.Consumer;

/**
 * Abstracts synchronization for running actions atomically.
 * Examples for typical implementations:
 * <pre>
 * // Example 1
 * synchronized (object) {
 *   action.run();
 * }
 *
 * // Example 2
 * concurrentHashMap.compute(key, (k, v) -> {
 *   action.run();
 *   retun null;
 * });
 *
 * // Example 3
 * lock.lock();
 * try {
 *   action.run();
 * } finally {
 *   lock.unlock();
 * }
 * </pre>
 *
 */
public interface Synchronizer
    extends Consumer<Runnable>
{
}
