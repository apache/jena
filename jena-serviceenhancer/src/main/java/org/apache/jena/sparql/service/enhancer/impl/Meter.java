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

import java.util.Deque;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.atomic.AtomicLong;

public class Meter {
    private Deque<Entry<Long, Long>> data;
    private long total = 0;
    private int maxDataSize;

    private long lastTick = -1;
    private AtomicLong counter = new AtomicLong();

    public Meter(int maxDataSize) {
        super();
        if (maxDataSize < 1) {
            throw new IllegalArgumentException("Data size must be at least 1.");
        }
        this.maxDataSize = maxDataSize;
    }

    public void inc() {
        counter.incrementAndGet();
    }

    public void tick() {
        long time = System.currentTimeMillis();
        long value = counter.getAndSet(0);

        if (data.size() >= maxDataSize) {
            Entry<Long, Long> e = data.removeFirst();
            total -= e.getValue();
        }

        total += value;
        data.add(Map.entry(time, value));
        lastTick = time;
    }
}
