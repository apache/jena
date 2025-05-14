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

package org.apache.jena.mem2.cache;

import java.util.Optional;
import java.util.function.Function;

import org.apache.jena.mem2.MemoryOptimizedGraphConfiguration;
import org.apache.jena.mem2.MemoryOptimizedGraphConfiguration.MemoryStrategy;
import org.apache.jena.sys.JenaSystem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Factory for creating node caches with different memory strategies.
 */
public class NodeCacheFactory {
    
    private static final Logger LOG = LoggerFactory.getLogger(NodeCacheFactory.class);
    
    static {
        JenaSystem.init();
    }
    
    // Factory methods for different cache types, customizable via JVM properties
    private static Function<Integer, NodeCache> heapCacheFactory = 
        capacity -> new HeapNodeCache(capacity);
    
    private static Function<Integer, NodeCache> offHeapCacheFactory = 
        capacity -> new OffHeapNodeCache(capacity);
    
    private static Function<Integer, NodeCache> hybridCacheFactory = 
        capacity -> new HybridNodeCache(capacity);
    
    /**
     * Create a node cache with the specified configuration.
     * 
     * @param config The configuration
     * @return A node cache implementation
     */
    public static NodeCache createNodeCache(MemoryOptimizedGraphConfiguration config) {
        // Calculate capacity based on configuration
        int capacity = config.getNodeCacheSize();
        
        // Check system property to override default behavior
        String cacheType = System.getProperty("jena.memory.nodeCache.type");
        if (cacheType != null) {
            if (cacheType.equalsIgnoreCase("heap")) {
                LOG.debug("Using heap node cache due to system property override");
                return heapCacheFactory.apply(capacity);
            } else if (cacheType.equalsIgnoreCase("offheap")) {
                LOG.debug("Using off-heap node cache due to system property override");
                return offHeapCacheFactory.apply(capacity);
            } else if (cacheType.equalsIgnoreCase("hybrid")) {
                LOG.debug("Using hybrid node cache due to system property override");
                return hybridCacheFactory.apply(capacity);
            } else {
                LOG.warn("Unknown cache type '{}' specified in system property, using configured type", 
                    cacheType);
            }
        }
        
        // Use strategy from configuration
        MemoryStrategy strategy = config.getMemoryStrategy();
        switch (strategy) {
            case HEAP:
                return heapCacheFactory.apply(capacity);
            case OFF_HEAP:
                return offHeapCacheFactory.apply(capacity);
            case HYBRID:
                return hybridCacheFactory.apply(capacity);
            default:
                LOG.warn("Unknown memory strategy: {}, falling back to HEAP", strategy);
                return heapCacheFactory.apply(capacity);
        }
    }
    
    /**
     * Set a custom factory for heap node caches.
     * 
     * @param factory The factory function to use
     */
    public static void setHeapCacheFactory(Function<Integer, NodeCache> factory) {
        heapCacheFactory = Optional.ofNullable(factory).orElse(HeapNodeCache::new);
    }
    
    /**
     * Set a custom factory for off-heap node caches.
     * 
     * @param factory The factory function to use
     */
    public static void setOffHeapCacheFactory(Function<Integer, NodeCache> factory) {
        offHeapCacheFactory = Optional.ofNullable(factory).orElse(OffHeapNodeCache::new);
    }
    
    /**
     * Set a custom factory for hybrid node caches.
     * 
     * @param factory The factory function to use
     */
    public static void setHybridCacheFactory(Function<Integer, NodeCache> factory) {
        hybridCacheFactory = Optional.ofNullable(factory).orElse(HybridNodeCache::new);
    }
}