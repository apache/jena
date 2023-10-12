/*
 * Copyright 2018 the original author or authors.
 * See the notice.md file distributed with this work for additional
 * information regarding copyright ownership.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.jena.ext.io.github.galbiston.expiring_map;

import java.util.Collections;
import java.util.SortedSet;
import java.util.TimerTask;
import java.util.TreeSet;
import java.util.concurrent.ConcurrentHashMap;

/**
 *
 *
 */
public class ExpiringMapCleaner extends TimerTask {

    //private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
    private final SortedSet<KeyTimestampPair> tracking = Collections.synchronizedSortedSet(new TreeSet<>());
    private final ConcurrentHashMap<Object, Long> refresh = new ConcurrentHashMap<>();
    private final ExpiringMap<? , ? > map;
    private long expiryInterval;

    public ExpiringMapCleaner(ExpiringMap<?,?> map, long expiryInterval) {
        this.map = map;
        this.expiryInterval = expiryInterval;
    }

    public ExpiringMapCleaner(ExpiringMapCleaner mapCleaner) {
        this.map = mapCleaner.map;
        this.expiryInterval = mapCleaner.expiryInterval;
        tracking.addAll(mapCleaner.tracking);
        refresh.putAll(mapCleaner.refresh);
    }

    @Override
    public void run() {

        //LOGGER.info("Run Start - Tracker: {}, Refresh: {}, Map: {}", tracking.size(), refresh.size(), map.size());
        long thresholdTimestamp = System.currentTimeMillis() - expiryInterval;
        boolean isEarlier = true;
        while (isEarlier) {
            if (tracking.isEmpty()) {
                return;
            }

            KeyTimestampPair current = tracking.first();
            isEarlier = current.isEarlier(thresholdTimestamp);
            if (isEarlier) {
                Object key = current.getKey();
                tracking.remove(current);
                if (refresh.containsKey(key)) {
                    //Check whether the refresh is still valid.
                    Long timestamp = refresh.get(key);
                    if (thresholdTimestamp < timestamp) {
                        tracking.add(new KeyTimestampPair(key, timestamp));
                    }
                    refresh.remove(key);
                } else {
                    map.remove(key);
                }

            }
        }
        //LOGGER.info("Run End - Tracker: {}, Refresh: {}, Map: {}", tracking.size(), refresh.size(), map.size());
    }

    public synchronized void refresh(Object key) {
        refresh.put(key, System.currentTimeMillis());
    }

    public synchronized void put(Object key) {
        tracking.add(new KeyTimestampPair(key, System.currentTimeMillis()));
    }

    public synchronized void setExpiryInterval(long expiryInterval) {
        this.expiryInterval = expiryInterval;
    }

    public synchronized void clear() {
        tracking.clear();
    }

}
