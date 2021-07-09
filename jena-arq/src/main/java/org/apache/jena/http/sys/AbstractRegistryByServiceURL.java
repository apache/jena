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

package org.apache.jena.http.sys;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.jena.atlas.lib.Trie;

/**
 * Abstract base class for registries with exact and prefix lookup..
 * <p>
 * The lookup ({@link #find}) is by exact match then by longest prefix. e.g. a registration of
 * "http://someHost/" or "http://someHost/dataset" will apply to
 * "http://someHost/dataset/sparql" and "http://someHost/dataset/update" but not to
 *  "https://someHost/..." which uses "https".
 */
public abstract class AbstractRegistryByServiceURL<T> {

    private final Map<String, T> exactMap = new ConcurrentHashMap<>();
    private final Trie<T> trie = new Trie<>();

    protected AbstractRegistryByServiceURL() { }

    public void add(String key, T value) {
        exactMap.put(key, value);
    }

    public void addPrefix(String key, T value) {
        if ( ! key.endsWith("/") )
            throw new IllegalArgumentException("Prefix must end in \"/\"");
        trie.add(key, value);
    }

    /** Returns the T with either an exact match or the longest prefix for the find key. */
    public T find(String findKey) {
        T item = findExact(findKey);
        if ( item == null )
            item = trie.longestMatch(findKey);
        return item;
    }

    private T findExact(String findKey) {
        return exactMap.get(findKey);
    }

    private T findPrefix(String findKey) {
        if ( trie.isEmpty() )
            return null;
        T hc = trie.longestMatch(findKey);
        return hc;
    }

    public void remove(String key) {
        exactMap.remove(key);
        trie.remove(key);
    }

    public void clear() {
        exactMap.clear();
        trie.clear();
    }
}
