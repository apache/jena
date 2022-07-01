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
import java.util.function.Function;

import org.apache.jena.atlas.lib.Trie;

/**
 * Abstract base class for registries with exact and prefix lookup.
 * <p>
 * The lookup ({@link #find}) is by exact match then by longest prefix. e.g. a registration of
 * "http://someHost/" or "http://someHost/dataset" will apply to
 * "http://someHost/dataset/sparql" and "http://someHost/dataset/update" but not to
 *  "https://someHost/..." which uses "https".
 */
public abstract class AbstractRegistryWithPrefix<X, T> {

    private final Map<String, T> exactMap = new ConcurrentHashMap<>();
    private final Trie<T> trie = new Trie<>();
    private final Function<X, String> generateKey;

    protected AbstractRegistryWithPrefix(Function<X, String> genKey) {
        this.generateKey = genKey;
    }

    public void add(X service, T value) {
        String key = generateKey.apply(service);
        exactMap.put(key, value);
    }

    /** Add a prefix. The prefix must end in "/" */
    public void addPrefix(X service, T value) {
        String key = generateKey.apply(service);
        if ( ! key.endsWith("/") )
            throw new IllegalArgumentException("Prefix must end in \"/\"");

        //if ( key.endsWith("/") )
            trie.add(key, value);
        // A prefix is also an exact match.
        // Exact matches take precedence on lookup.
        exactMap.put(key, value);
    }

    /** Returns the T with either an exact match or the longest prefix for the find key. */
    public T find(X findRef) {
        String findKey = generateKey.apply(findRef);
        T item = exactMap.get(findKey);
        if ( item == null )
            item = trie.longestMatch(findKey);
        return item;
    }

    private T findPrefix(X findRef) {
        String findKey = generateKey.apply(findRef);
        if ( trie.isEmpty() )
            return null;
        T hc = trie.longestMatch(findKey);
        return hc;
    }

    public void remove(X findRef) {
        String key = generateKey.apply(findRef);
        exactMap.remove(key);
        trie.remove(key);
    }

    public void clear() {
        exactMap.clear();
        trie.clear();
    }
}
