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

package org.apache.jena.sparql.graph;

import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiConsumer;

import org.apache.jena.shared.PrefixMapping;
import org.apache.jena.shared.impl.PrefixMappingImpl;

/**
 * A {@link PrefixMapping} implemented as a pair of in-memory maps.
 *
 * @implNote
 * {@link PrefixMappingImpl} is the long time implementation.
 * This class should be exactly the same within the {@link PrefixMappingBase} framework.
 */
public class PrefixMappingMem extends PrefixMappingBase {

    private Map<String, String> prefixToUri = new ConcurrentHashMap<>();
    private Map<String, String> uriToPrefix = new ConcurrentHashMap<>();

    public PrefixMappingMem() {}

    @Override
    protected void add(String prefix, String uri) {
        prefixToUri.put(prefix, uri);
        uriToPrefix.put(uri, prefix);
    }

    /** See notes on reverse mappings in {@link PrefixMappingBase}.
     * This is a complete implementation.
     * <p>
     * Test {@code AbstractTestPrefixMapping.testSecondPrefixDeletedUncoversPreviousMap}.
     */
    @Override
    protected void remove(String prefix) {
        String u = prefixToUri(prefix);
        if ( u == null )
            return;
        String p = findReverseMapping(u, prefix);
        prefixToUri.remove(prefix);
        uriToPrefix.remove(u);
        // Reverse mapping.
        if ( p != null )
            uriToPrefix.put(u, p);
    }

    // Find a prefix for a uri that isn't the supplied prefix.
    protected String findReverseMapping(String uri, String prefixExclude) {
        Objects.requireNonNull(prefixExclude);
        for ( Map.Entry<String, String> e : prefixToUri.entrySet() ) {
            String p = e.getKey();
            String u = e.getValue();
            if ( uri.equals(u) && ! prefixExclude.equals(p) )
                return p;
        }
        return null;
    }

    @Override
    protected void clear() {
        prefixToUri.clear();
        uriToPrefix.clear();
    }

    @Override
    protected boolean isEmpty() {
        return prefixToUri.isEmpty();
    }

    @Override
    protected int size() {
        return prefixToUri.size();
    }

    @Override
    protected String prefixToUri(String prefix) {
        return prefixToUri.get(prefix);
    }

    @Override
    protected String uriToPrefix(String uri) {
        return uriToPrefix.get(uri);
    }

    @Override
    protected Map<String, String> asMap() {
        return prefixToUri;
    }

    @Override
    protected Map<String, String> asMapCopy() {
        return Map.copyOf(prefixToUri);
    }

    @Override
    protected void apply(BiConsumer<String, String> action) {
        prefixToUri.forEach(action);
    }
}
