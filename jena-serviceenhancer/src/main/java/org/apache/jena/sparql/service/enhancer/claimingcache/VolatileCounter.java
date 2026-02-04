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

/**
 * A counter that can be accessed by multiple threads.
 * Synchronization must be ensured extrinsically, such as using synchronized blocks or within
 * ConcurrentHashMap.compute.
 */
class VolatileCounter {
    private volatile int value ;

    public VolatileCounter(int value) {
        this.value = value;
    }

    public VolatileCounter inc() { ++value; return this; }
    public VolatileCounter dec() { --value; return this; }
    public int get() { return value; }

    @Override
    public String toString() {
        return "Volatile counter " + System.identityHashCode(this) + " has value " + value;
    }
}
