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

package org.apache.jena.mem2;

import java.io.Serializable;

/**
 * Configuration options for the memory-optimized graph implementation.
 */
public class MemoryOptimizedGraphConfiguration implements Serializable {
    private static final long serialVersionUID = 1L;
    
    /** Default initial capacity for triple tables */
    public static final int DEFAULT_INITIAL_CAPACITY = 1000;
    
    /** Default load factor for triple tables */
    public static final float DEFAULT_LOAD_FACTOR = 0.75f;
    
    /** Default memory allocation strategy */
    public static final MemoryStrategy DEFAULT_MEMORY_STRATEGY = MemoryStrategy.HYBRID;
    
    /** Default node cache size */
    public static final int DEFAULT_NODE_CACHE_SIZE = 100_000;
    
    /** Default setting for using off-heap storage */
    public static final boolean DEFAULT_USE_OFF_HEAP = true;
    
    /** Default setting for enabling query tracking */
    public static final boolean DEFAULT_ENABLE_QUERY_TRACKING = false;
    
    /** Default setting for enabling memory monitoring */
    public static final boolean DEFAULT_ENABLE_MEMORY_MONITORING = true;
    
    /** Default setting for automatic memory optimization */
    public static final boolean DEFAULT_AUTO_OPTIMIZE = true;
    
    /** Default setting for the transaction buffer size (in triples) */
    public static final int DEFAULT_TRANSACTION_BUFFER_SIZE = 10_000;
    
    /** Memory allocation strategies */
    public enum MemoryStrategy {
        /** Use heap memory only */
        HEAP,
        /** Use off-heap memory only */
        OFF_HEAP,
        /** Use both heap and off-heap memory, optimizing based on access patterns */
        HYBRID
    }
    
    // Configuration fields
    private int initialCapacity = DEFAULT_INITIAL_CAPACITY;
    private float loadFactor = DEFAULT_LOAD_FACTOR;
    private MemoryStrategy memoryStrategy = DEFAULT_MEMORY_STRATEGY;
    private int nodeCacheSize = DEFAULT_NODE_CACHE_SIZE;
    private boolean useOffHeap = DEFAULT_USE_OFF_HEAP;
    private boolean enableQueryTracking = DEFAULT_ENABLE_QUERY_TRACKING;
    private boolean enableMemoryMonitoring = DEFAULT_ENABLE_MEMORY_MONITORING;
    private boolean autoOptimize = DEFAULT_AUTO_OPTIMIZE;
    private int transactionBufferSize = DEFAULT_TRANSACTION_BUFFER_SIZE;
    private QueryTracker queryTracker = null;
    
    /**
     * Create a configuration with default settings.
     */
    public MemoryOptimizedGraphConfiguration() {
        // Use defaults
    }
    
    /**
     * Get the initial capacity for triple tables.
     */
    public int getInitialCapacity() {
        return initialCapacity;
    }
    
    /**
     * Set the initial capacity for triple tables.
     */
    public MemoryOptimizedGraphConfiguration setInitialCapacity(int initialCapacity) {
        this.initialCapacity = initialCapacity;
        return this;
    }
    
    /**
     * Get the load factor for triple tables.
     */
    public float getLoadFactor() {
        return loadFactor;
    }
    
    /**
     * Set the load factor for triple tables.
     */
    public MemoryOptimizedGraphConfiguration setLoadFactor(float loadFactor) {
        this.loadFactor = loadFactor;
        return this;
    }
    
    /**
     * Get the memory allocation strategy.
     */
    public MemoryStrategy getMemoryStrategy() {
        return memoryStrategy;
    }
    
    /**
     * Set the memory allocation strategy.
     */
    public MemoryOptimizedGraphConfiguration setMemoryStrategy(MemoryStrategy memoryStrategy) {
        this.memoryStrategy = memoryStrategy;
        return this;
    }
    
    /**
     * Get the node cache size.
     */
    public int getNodeCacheSize() {
        return nodeCacheSize;
    }
    
    /**
     * Set the node cache size.
     */
    public MemoryOptimizedGraphConfiguration setNodeCacheSize(int nodeCacheSize) {
        this.nodeCacheSize = nodeCacheSize;
        return this;
    }
    
    /**
     * Check if off-heap storage is enabled.
     */
    public boolean isUseOffHeap() {
        return useOffHeap;
    }
    
    /**
     * Set whether to use off-heap storage.
     */
    public MemoryOptimizedGraphConfiguration setUseOffHeap(boolean useOffHeap) {
        this.useOffHeap = useOffHeap;
        return this;
    }
    
    /**
     * Check if query tracking is enabled.
     */
    public boolean isEnableQueryTracking() {
        return enableQueryTracking;
    }
    
    /**
     * Set whether to enable query tracking.
     */
    public MemoryOptimizedGraphConfiguration setEnableQueryTracking(boolean enableQueryTracking) {
        this.enableQueryTracking = enableQueryTracking;
        if (enableQueryTracking && queryTracker == null) {
            queryTracker = new QueryTracker();
        }
        return this;
    }
    
    /**
     * Get the query tracker.
     */
    public QueryTracker getQueryTracker() {
        if (queryTracker == null) {
            queryTracker = new QueryTracker();
        }
        return queryTracker;
    }
    
    /**
     * Check if memory monitoring is enabled.
     */
    public boolean isEnableMemoryMonitoring() {
        return enableMemoryMonitoring;
    }
    
    /**
     * Set whether to enable memory monitoring.
     */
    public MemoryOptimizedGraphConfiguration setEnableMemoryMonitoring(boolean enableMemoryMonitoring) {
        this.enableMemoryMonitoring = enableMemoryMonitoring;
        return this;
    }
    
    /**
     * Check if automatic memory optimization is enabled.
     */
    public boolean isAutoOptimize() {
        return autoOptimize;
    }
    
    /**
     * Set whether to enable automatic memory optimization.
     */
    public MemoryOptimizedGraphConfiguration setAutoOptimize(boolean autoOptimize) {
        this.autoOptimize = autoOptimize;
        return this;
    }
    
    /**
     * Get the transaction buffer size.
     */
    public int getTransactionBufferSize() {
        return transactionBufferSize;
    }
    
    /**
     * Set the transaction buffer size.
     */
    public MemoryOptimizedGraphConfiguration setTransactionBufferSize(int transactionBufferSize) {
        this.transactionBufferSize = transactionBufferSize;
        return this;
    }
    
    @Override
    public String toString() {
        return "MemoryOptimizedGraphConfiguration [" +
            "initialCapacity=" + initialCapacity +
            ", loadFactor=" + loadFactor +
            ", memoryStrategy=" + memoryStrategy +
            ", nodeCacheSize=" + nodeCacheSize +
            ", useOffHeap=" + useOffHeap +
            ", enableQueryTracking=" + enableQueryTracking +
            ", enableMemoryMonitoring=" + enableMemoryMonitoring +
            ", autoOptimize=" + autoOptimize +
            ", transactionBufferSize=" + transactionBufferSize +
            "]";
    }
}