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

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

import org.apache.jena.graph.Node;
import org.apache.jena.mem2.MemoryStats;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A hybrid node cache that combines on-heap and off-heap storage strategies.
 * <p>
 * This implementation uses a dual-layer caching approach:
 * <ul>
 * <li>Frequently accessed nodes are stored on-heap for fast access</li>
 * <li>Less frequently accessed nodes are stored off-heap to reduce GC pressure</li>
 * </ul>
 * <p>
 * The cache monitors access patterns and automatically migrates nodes between
 * layers based on usage frequency.
 */
public class HybridNodeCache implements NodeCache {
    
    private static final Logger LOG = LoggerFactory.getLogger(HybridNodeCache.class);
    
    // Access frequency thresholds for migration
    private static final int HOT_NODE_THRESHOLD = 10;  // Keep on-heap if accessed at least this many times
    private static final int COLD_NODE_THRESHOLD = 2;  // Move to off-heap if accessed less than this many times
    private static final long ACCESS_WINDOW_MS = 60_000; // 1 minute window for tracking
    
    // Primary caches
    private final HeapNodeCache heapCache;
    private final OffHeapNodeCache offHeapCache;
    
    // Access tracking
    private final Map<Node, AccessRecord> accessRecords = new ConcurrentHashMap<>();
    private long lastCleanupTime = System.currentTimeMillis();
    
    // Migration tracking
    private final AtomicLong migrationsToHeap = new AtomicLong(0);
    private final AtomicLong migrationsToOffHeap = new AtomicLong(0);
    
    // Cache statistics
    private final AtomicLong cacheHits = new AtomicLong(0);
    private final AtomicLong cacheMisses = new AtomicLong(0);
    
    // State
    private final AtomicBoolean closed = new AtomicBoolean(false);
    
    /**
     * Access record for a node.
     */
    private static class AccessRecord {
        long lastAccessTime;
        int accessCount;
        boolean isOnHeap;
        
        AccessRecord(boolean isOnHeap) {
            this.lastAccessTime = System.currentTimeMillis();
            this.accessCount = 1;
            this.isOnHeap = isOnHeap;
        }
        
        void recordAccess() {
            this.lastAccessTime = System.currentTimeMillis();
            this.accessCount++;
        }
        
        boolean isHot() {
            return accessCount >= HOT_NODE_THRESHOLD;
        }
        
        boolean isCold() {
            return accessCount <= COLD_NODE_THRESHOLD;
        }
        
        boolean isStale(long currentTime, long windowMs) {
            return currentTime - lastAccessTime > windowMs;
        }
    }
    
    /**
     * Create a new hybrid node cache with the specified capacity.
     * 
     * @param capacity The total capacity (divided between heap and off-heap)
     */
    public HybridNodeCache(int capacity) {
        // Allocate capacity between the two caches
        int heapCapacity = capacity / 4;  // 25% on-heap
        int offHeapCapacity = capacity - heapCapacity;  // 75% off-heap
        
        this.heapCache = new HeapNodeCache(heapCapacity);
        this.offHeapCache = new OffHeapNodeCache(offHeapCapacity);
        
        LOG.debug("Created HybridNodeCache with capacity: {} (heap: {}, off-heap: {})", 
            capacity, heapCapacity, offHeapCapacity);
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
        
        // Check if we have an access record for this node
        AccessRecord record = accessRecords.get(node);
        
        if (record != null) {
            // Node is known to the cache
            record.recordAccess();
            
            // Check if the node is in the appropriate cache based on its access pattern
            if (record.isOnHeap) {
                // Node should be in heap cache
                Node cachedNode = heapCache.getOrAdd(node);
                cacheHits.incrementAndGet();
                
                // If it's become cold, consider moving to off-heap
                if (record.isCold()) {
                    considerMigrateToOffHeap(node, record);
                }
                
                return cachedNode;
            } else {
                // Node should be in off-heap cache
                if (offHeapCache.contains(node)) {
                    cacheHits.incrementAndGet();
                    
                    // If it's become hot, consider moving to heap
                    if (record.isHot()) {
                        considerMigrateToHeap(node, record);
                    }
                    
                    return node;
                } else if (heapCache.contains(node)) {
                    // Node is in the wrong cache, fix the record
                    record.isOnHeap = true;
                    cacheHits.incrementAndGet();
                    return node;
                }
            }
        }
        
        // Node is not in the cache or record is inconsistent
        cacheMisses.incrementAndGet();
        
        // Choose the initial cache based on a simple heuristic
        // (e.g., literals that might be repeated often go to heap)
        boolean preferHeap = shouldPreferHeap(node);
        
        Node cachedNode;
        if (preferHeap) {
            cachedNode = heapCache.getOrAdd(node);
            accessRecords.put(cachedNode, new AccessRecord(true));
        } else {
            cachedNode = offHeapCache.getOrAdd(node);
            accessRecords.put(cachedNode, new AccessRecord(false));
        }
        
        // Periodically clean up stale access records
        maybeCleanupRecords();
        
        return cachedNode;
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
        
        return heapCache.contains(node) || offHeapCache.contains(node);
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
        
        // Remove from access tracking
        accessRecords.remove(node);
        
        // Remove from both caches
        boolean removedFromHeap = heapCache.remove(node);
        boolean removedFromOffHeap = offHeapCache.remove(node);
        
        return removedFromHeap || removedFromOffHeap;
    }
    
    /**
     * Clear all nodes from the cache.
     */
    @Override
    public void clear() {
        checkNotClosed();
        
        heapCache.clear();
        offHeapCache.clear();
        accessRecords.clear();
        
        LOG.debug("Cleared HybridNodeCache");
    }
    
    /**
     * Get the number of nodes in the cache.
     */
    @Override
    public int size() {
        return heapCache.size() + offHeapCache.size();
    }
    
    /**
     * Get memory usage statistics for this cache.
     */
    @Override
    public MemoryStats getMemoryStats() {
        MemoryStats heapStats = heapCache.getMemoryStats();
        MemoryStats offHeapStats = offHeapCache.getMemoryStats();
        
        return MemoryStats.builder()
            .timestamp(Instant.now())
            .onHeapBytesUsed(heapStats.getOnHeapBytesUsed())
            .onHeapBytesReserved(heapStats.getOnHeapBytesReserved())
            .offHeapBytesUsed(offHeapStats.getOffHeapBytesUsed())
            .offHeapBytesReserved(offHeapStats.getOffHeapBytesReserved())
            .nodeCount(heapStats.getNodeCount() + offHeapStats.getNodeCount())
            .cachedNodeCount(heapStats.getCachedNodeCount() + offHeapStats.getCachedNodeCount())
            .gcCount(heapStats.getGcCount())
            .gcTimeMs(heapStats.getGcTimeMs())
            .build();
    }
    
    /**
     * Optimize the cache for memory usage.
     */
    @Override
    public void optimize() {
        checkNotClosed();
        
        // Perform migration based on current access patterns
        performBulkMigration();
        
        // Optimize individual caches
        heapCache.optimize();
        offHeapCache.optimize();
        
        // Clean up stale access records
        cleanupRecords();
        
        LOG.debug("Optimized HybridNodeCache, current stats: {}", getMemoryStats());
    }
    
    /**
     * Close the cache and release resources.
     */
    @Override
    public void close() {
        if (closed.compareAndSet(false, true)) {
            heapCache.close();
            offHeapCache.close();
            accessRecords.clear();
            LOG.debug("Closed HybridNodeCache");
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
     * Consider migrating a node from heap to off-heap.
     */
    private void considerMigrateToOffHeap(Node node, AccessRecord record) {
        // This is a simplified implementation
        // A real implementation would consider memory pressure, etc.
        if (record != null && record.isOnHeap && record.isCold()) {
            // Move to off-heap
            offHeapCache.getOrAdd(node);
            record.isOnHeap = false;
            migrationsToOffHeap.incrementAndGet();
            
            // We don't remove from heap cache immediately to avoid thrashing
            // It will be evicted eventually if not accessed
        }
    }
    
    /**
     * Consider migrating a node from off-heap to heap.
     */
    private void considerMigrateToHeap(Node node, AccessRecord record) {
        // This is a simplified implementation
        if (record != null && !record.isOnHeap && record.isHot()) {
            // Move to heap
            heapCache.getOrAdd(node);
            record.isOnHeap = true;
            migrationsToHeap.incrementAndGet();
            
            // We keep the node in off-heap too for now
            // It will be evicted on the next optimization if necessary
        }
    }
    
    /**
     * Determine if a node should initially be stored on heap.
     */
    private boolean shouldPreferHeap(Node node) {
        // This is a simple heuristic - a real implementation would be more sophisticated
        if (node.isLiteral()) {
            // Small literals that might be repeated often
            String lex = node.getLiteralLexicalForm();
            return lex.length() < 32;
        } else if (node.isURI()) {
            // Common predicates are good to keep on heap
            String uri = node.getURI();
            return uri.contains("type") || 
                   uri.contains("label") || 
                   uri.contains("comment");
        }
        
        return false;
    }
    
    /**
     * Clean up stale access records if needed.
     */
    private void maybeCleanupRecords() {
        long now = System.currentTimeMillis();
        if (now - lastCleanupTime > ACCESS_WINDOW_MS) {
            cleanupRecords();
            lastCleanupTime = now;
        }
    }
    
    /**
     * Clean up stale access records.
     */
    private void cleanupRecords() {
        long now = System.currentTimeMillis();
        
        // Remove stale records
        accessRecords.entrySet().removeIf(entry -> {
            AccessRecord record = entry.getValue();
            return record.isStale(now, ACCESS_WINDOW_MS * 2);
        });
    }
    
    /**
     * Perform bulk migration of nodes between caches.
     */
    private void performBulkMigration() {
        // Find hot nodes in off-heap that should be moved to heap
        Map<Node, AccessRecord> nodesToHeap = new HashMap<>();
        accessRecords.forEach((node, record) -> {
            if (!record.isOnHeap && record.isHot()) {
                nodesToHeap.put(node, record);
            }
        });
        
        // Find cold nodes in heap that should be moved to off-heap
        Map<Node, AccessRecord> nodesToOffHeap = new HashMap<>();
        accessRecords.forEach((node, record) -> {
            if (record.isOnHeap && record.isCold()) {
                nodesToOffHeap.put(node, record);
            }
        });
        
        // Perform migrations
        nodesToHeap.forEach((node, record) -> {
            heapCache.getOrAdd(node);
            record.isOnHeap = true;
            migrationsToHeap.incrementAndGet();
        });
        
        nodesToOffHeap.forEach((node, record) -> {
            offHeapCache.getOrAdd(node);
            record.isOnHeap = false;
            migrationsToOffHeap.incrementAndGet();
        });
        
        LOG.debug("Bulk migration: {} nodes to heap, {} nodes to off-heap", 
            nodesToHeap.size(), nodesToOffHeap.size());
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
     * Get the number of migrations from heap to off-heap.
     */
    public long getMigrationsToOffHeap() {
        return migrationsToOffHeap.get();
    }
    
    /**
     * Get the number of migrations from off-heap to heap.
     */
    public long getMigrationsToHeap() {
        return migrationsToHeap.get();
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