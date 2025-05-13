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

import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.lang.management.MemoryUsage;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

import org.apache.jena.graph.Node;
import org.apache.jena.mem2.MemoryStats;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * An efficient on-heap node cache implementation.
 * <p>
 * This implementation uses a concurrent hash map to store nodes,
 * ensuring that identical nodes share the same memory location.
 * While it doesn't use off-heap memory, it's optimized for memory
 * efficiency and concurrent access.
 */
public class HeapNodeCache implements NodeCache {
    
    private static final Logger LOG = LoggerFactory.getLogger(HeapNodeCache.class);
    
    // Node storage
    private final Map<Node, Node> nodeMap;
    
    // Memory monitoring
    private final MemoryMXBean memoryMXBean = ManagementFactory.getMemoryMXBean();
    private long heapBytesAtStartup;
    
    // Cache statistics
    private final AtomicLong cacheHits = new AtomicLong(0);
    private final AtomicLong cacheMisses = new AtomicLong(0);
    
    // State
    private final AtomicBoolean closed = new AtomicBoolean(false);
    
    /**
     * Create a new heap node cache with the specified initial capacity.
     * 
     * @param initialCapacity The initial capacity for the cache
     */
    public HeapNodeCache(int initialCapacity) {
        // Initialize the node map
        this.nodeMap = new ConcurrentHashMap<>(initialCapacity);
        
        // Initialize heapBytesAtStartup to initial heap usage
        MemoryUsage heapUsage = memoryMXBean.getHeapMemoryUsage();
        this.heapBytesAtStartup = heapUsage.getUsed();
        
        LOG.debug("Created HeapNodeCache with initial capacity: {}", initialCapacity);
    }
    
    /**
     * Get or add a node to the cache.
     */
    @Override
    public Node getOrAdd(Node node) {
        if (node == null) {
            return null;
        }
        
        checkNotClosed();
        
        // Check if the node is already in the cache
        Node cachedNode = nodeMap.get(node);
        if (cachedNode != null) {
            cacheHits.incrementAndGet();
            return cachedNode;
        }
        
        // Not in cache, add it
        cacheMisses.incrementAndGet();
        
        // Use computeIfAbsent to handle concurrent additions
        return nodeMap.computeIfAbsent(node, n -> n);
    }
    
    /**
     * Check if a node is in the cache.
     */
    @Override
    public boolean contains(Node node) {
        if (node == null) {
            return false;
        }
        
        checkNotClosed();
        
        return nodeMap.containsKey(node);
    }
    
    /**
     * Remove a node from the cache.
     */
    @Override
    public boolean remove(Node node) {
        if (node == null) {
            return false;
        }
        
        checkNotClosed();
        
        return nodeMap.remove(node) != null;
    }
    
    /**
     * Clear all nodes from the cache.
     */
    @Override
    public void clear() {
        checkNotClosed();
        
        nodeMap.clear();
        
        LOG.debug("Cleared HeapNodeCache");
    }
    
    /**
     * Get the number of nodes in the cache.
     */
    @Override
    public int size() {
        return nodeMap.size();
    }
    
    /**
     * Get memory usage statistics for this cache.
     */
    @Override
    public MemoryStats getMemoryStats() {
        MemoryUsage heapUsage = memoryMXBean.getHeapMemoryUsage();
        
        return MemoryStats.builder()
            .timestamp(Instant.now())
            .onHeapBytesUsed(estimateHeapUsage())
            .onHeapBytesReserved(heapUsage.getCommitted())
            .offHeapBytesUsed(0) // Heap cache doesn't use off-heap memory
            .offHeapBytesReserved(0)
            .nodeCount(nodeMap.size())
            .cachedNodeCount(nodeMap.size())
            .gcCount(ManagementFactory.getGarbageCollectorMXBeans()
                .stream()
                .mapToLong(gc -> gc.getCollectionCount())
                .sum())
            .gcTimeMs(ManagementFactory.getGarbageCollectorMXBeans()
                .stream()
                .mapToLong(gc -> gc.getCollectionTime())
                .sum())
            .build();
    }
    
    /**
     * Optimize the cache for memory usage.
     */
    @Override
    public void optimize() {
        checkNotClosed();
        
        // Request garbage collection
        System.gc();
        
        LOG.debug("Optimized HeapNodeCache, current stats: {}", getMemoryStats());
    }
    
    /**
     * Close the cache and release resources.
     */
    @Override
    public void close() {
        if (closed.compareAndSet(false, true)) {
            clear();
            LOG.debug("Closed HeapNodeCache");
        }
    }
    
    /**
     * Check if the cache is closed.
     */
    @Override
    public boolean isClosed() {
        return closed.get();
    }
    
    /**
     * Throw an exception if the cache is closed.
     */
    private void checkNotClosed() {
        if (closed.get()) {
            throw new IllegalStateException("Node cache has been closed");
        }
    }
    
    /**
     * Estimate the heap usage of this cache.
     */
    private long estimateHeapUsage() {
        // This is a rough estimation
        // A more accurate approach would use a memory measurement tool like JOL
        int nodeCount = nodeMap.size();
        long bytesPerNode = 64; // Rough estimate including overhead
        
        return nodeCount * bytesPerNode;
    }
    
    /**
     * Get cache hit statistics.
     */
    public long getCacheHits() {
        return cacheHits.get();
    }
    
    /**
     * Get cache miss statistics.
     */
    public long getCacheMisses() {
        return cacheMisses.get();
    }
    
    /**
     * Get the cache hit ratio.
     */
    public double getCacheHitRatio() {
        long hits = cacheHits.get();
        long total = hits + cacheMisses.get();
        return total > 0 ? (double)hits / total : 0.0;
    }
}