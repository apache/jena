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

package com.hp.hpl.jena.util.cache;

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RandCache implements Cache, CacheControl {
    int size;
    int threshhold;
    boolean enabled = true;
                                               // so we can identify caches
    String name;                               // e.g. when logging

    HashMap<Object, Object> map;
    Collection<Object> collection;

    protected static Logger logger = LoggerFactory.getLogger(RandCache.class);
    
    long gets = 0;
    long puts = 0;
    long hits = 0;

    /** Creates new RandCache */
    RandCache(String name, int size) {
        this.size = size;
        try {
            map = new HashMap<>(size * 100 / 75);  // based on .75 loadfactor
        } catch (IllegalArgumentException e) {
            if ("Illegal load factor: NaN".equals(e.getMessage())) {
                // This strange construction needs explanation.
                // When we implemented XSDbase64Binary/XSDhexBinary support involving use
                // of byte[] we started seeing this error here. Since the default loadfactor
                // is a static final constant in HashMap this should never be possible.
                // It only happens under JDK 1.4.1 not under 1.3.1 nor 1.4.2.
                // The retry, however does seem to work and hence gives us a work around
                // which is completely mysterious but at least enables the unit tests to pass.
                //   - der 4/5/04
                logger.warn("Detected a NaN anomaly believed to be due to use of JDK 1.4.1");
                map = new HashMap<>(size*100/75, 0.75f);
            } else {
                throw e;
            }
        }
        threshhold = size;
        if (threshhold < 2) {
            throw new Error("Cache size too small: " + size);
        }
        collection = map.values();
    }

    @Override
    public synchronized Object get(Object key) {
        if (enabled) {
            if (gets == Long.MAX_VALUE) {
                forgetStats();
            }
            gets++;
            Object result = map.get(key);
            if (result != null) {
                hits++;
            }
            return result;
        } else {
            return null;
        }
    }

    @Override
    public synchronized void put(Object key, Object value) {

        // don't allow null values
        if (value == null) {
            throw new NullPointerException();
        }

        if (enabled) {
            if (puts == Long.MAX_VALUE) {
                forgetStats();
            }
            puts++;
            if (map.size() >= threshhold) {
                makeSpace();
            }
            map.put(key, value);
        }
    }

    protected void makeSpace() {
        Iterator<Object> iter = collection.iterator();

        // we are going to remove every 3rd member of the cache
        int size = map.size();
        int i = 3;
        while (i < size ) {
            iter.next();
            iter.remove();
            iter.next();
            iter.next();
            i = i + 3;
        }
    }

    @Override
    public synchronized boolean getEnabled() {
        return enabled;
    }

    @Override
    public synchronized boolean setEnabled(boolean enabled) {
        boolean result = enabled;
        this.enabled = enabled;
        return result;
    }

    @Override
    public synchronized void clear() {
        map.clear();
    }

    @Override
    public synchronized long getHits() {
        return hits;
    }

    @Override
    public synchronized long getGets() {
        return gets;
    }

    @Override
    public synchronized long getPuts() {
        return puts;
    }

    protected void forgetStats() {
        gets = gets/2;
        puts = puts/2;
        hits = hits/2;
    }

}
