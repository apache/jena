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

package org.apache.jena.mem2.collection;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.jena.graph.Node;
import org.apache.jena.graph.Triple;

/**
 * Factory for creating optimized map and set implementations used in the
 * memory-optimized graph storage.
 */
public class JenaMapFactory {
    
    // System property to control whether to use concurrent collections
    private static final boolean USE_CONCURRENT_MAPS = 
        Boolean.parseBoolean(System.getProperty("jena.memory.concurrentMaps", "true"));
    
    // System property to control the concurrency level for concurrent collections
    private static final int DEFAULT_CONCURRENCY_LEVEL = 
        Integer.parseInt(System.getProperty("jena.memory.concurrencyLevel", "16"));
    
    /**
     * Private constructor to prevent instantiation.
     */
    private JenaMapFactory() {
        // Do not instantiate
    }
    
    /**
     * Create a map optimized for storing Node to Node mappings.
     */
    public static <K, V> Map<K, V> createNodeMap(int initialCapacity, float loadFactor) {
        return createMap(initialCapacity, loadFactor, false);
    }
    
    /**
     * Create a concurrent map optimized for storing Node to Node mappings.
     */
    public static <K, V> Map<K, V> createConcurrentNodeMap(int initialCapacity, float loadFactor) {
        return createMap(initialCapacity, loadFactor, true);
    }
    
    /**
     * Create a map optimized for storing Triple to Triple mappings.
     */
    public static Map<Triple, Triple> createTripleMap(int initialCapacity, float loadFactor) {
        return createMap(initialCapacity, loadFactor, false);
    }
    
    /**
     * Create a concurrent map optimized for storing Triple to Triple mappings.
     */
    public static Map<Triple, Triple> createConcurrentTripleMap(int initialCapacity, float loadFactor) {
        return createMap(initialCapacity, loadFactor, true);
    }
    
    /**
     * Create a set optimized for storing Node objects.
     */
    public static <T> Set<T> createNodeSet(int initialCapacity, float loadFactor) {
        return createMap(initialCapacity, loadFactor, false).keySet(true);
    }
    
    /**
     * Create a concurrent set optimized for storing Node objects.
     */
    public static <T> Set<T> createConcurrentNodeSet(int initialCapacity, float loadFactor) {
        return createMap(initialCapacity, loadFactor, true).keySet(true);
    }
    
    /**
     * Create a map with the specified configuration.
     */
    @SuppressWarnings("unchecked")
    private static <K, V> Map<K, V> createMap(int initialCapacity, float loadFactor, boolean concurrent) {
        if (USE_CONCURRENT_MAPS || concurrent) {
            // Create a concurrent map - this will be thread-safe but slightly slower for single-threaded use
            return new ConcurrentHashMap<>(initialCapacity, loadFactor, DEFAULT_CONCURRENCY_LEVEL);
        } else {
            // Create a standard map - faster for single-threaded use but not thread-safe
            return new HashMap<>(initialCapacity, loadFactor);
        }
    }
}