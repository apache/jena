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

package org.apache.jena.mem2.store;

import java.util.Optional;
import java.util.function.Function;

import org.apache.jena.mem2.MemoryOptimizedGraphConfiguration;
import org.apache.jena.mem2.MemoryOptimizedGraphConfiguration.MemoryStrategy;
import org.apache.jena.mem2.store.internal.HeapTripleStore;
import org.apache.jena.mem2.store.internal.HybridTripleStore;
import org.apache.jena.mem2.store.internal.OffHeapTripleStore;
import org.apache.jena.sys.JenaSystem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Factory for creating triple stores with different memory strategies.
 */
public class StoreFactory {
    
    private static final Logger LOG = LoggerFactory.getLogger(StoreFactory.class);
    
    static {
        JenaSystem.init();
    }
    
    // Factory methods for different store types, customizable via JVM properties
    private static Function<MemoryOptimizedGraphConfiguration, TripleStore> heapStoreFactory = 
        HeapTripleStore::new;
    
    private static Function<MemoryOptimizedGraphConfiguration, TripleStore> offHeapStoreFactory = 
        OffHeapTripleStore::new;
    
    private static Function<MemoryOptimizedGraphConfiguration, TripleStore> hybridStoreFactory = 
        HybridTripleStore::new;
    
    /**
     * Create a triple store with the specified configuration.
     * 
     * @param config The configuration for the triple store
     * @return A triple store implementation
     */
    public static TripleStore createTripleStore(MemoryOptimizedGraphConfiguration config) {
        // Check system property to override default behavior
        String storeType = System.getProperty("jena.memory.store.type");
        if (storeType != null) {
            if (storeType.equalsIgnoreCase("heap")) {
                LOG.debug("Using heap store due to system property override");
                return heapStoreFactory.apply(config);
            } else if (storeType.equalsIgnoreCase("offheap")) {
                LOG.debug("Using off-heap store due to system property override");
                return offHeapStoreFactory.apply(config);
            } else if (storeType.equalsIgnoreCase("hybrid")) {
                LOG.debug("Using hybrid store due to system property override");
                return hybridStoreFactory.apply(config);
            } else {
                LOG.warn("Unknown store type '{}' specified in system property, using configured type", 
                    storeType);
            }
        }
        
        // Use strategy from configuration
        MemoryStrategy strategy = config.getMemoryStrategy();
        switch (strategy) {
            case HEAP:
                return heapStoreFactory.apply(config);
            case OFF_HEAP:
                return offHeapStoreFactory.apply(config);
            case HYBRID:
                return hybridStoreFactory.apply(config);
            default:
                LOG.warn("Unknown memory strategy: {}, falling back to HEAP", strategy);
                return heapStoreFactory.apply(config);
        }
    }
    
    /**
     * Set a custom factory for heap stores.
     * 
     * @param factory The factory function to use
     */
    public static void setHeapStoreFactory(Function<MemoryOptimizedGraphConfiguration, TripleStore> factory) {
        heapStoreFactory = Optional.ofNullable(factory).orElse(HeapTripleStore::new);
    }
    
    /**
     * Set a custom factory for off-heap stores.
     * 
     * @param factory The factory function to use
     */
    public static void setOffHeapStoreFactory(Function<MemoryOptimizedGraphConfiguration, TripleStore> factory) {
        offHeapStoreFactory = Optional.ofNullable(factory).orElse(OffHeapTripleStore::new);
    }
    
    /**
     * Set a custom factory for hybrid stores.
     * 
     * @param factory The factory function to use
     */
    public static void setHybridStoreFactory(Function<MemoryOptimizedGraphConfiguration, TripleStore> factory) {
        hybridStoreFactory = Optional.ofNullable(factory).orElse(HybridTripleStore::new);
    }
}