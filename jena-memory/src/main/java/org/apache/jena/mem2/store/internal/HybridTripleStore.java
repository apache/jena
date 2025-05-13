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

package org.apache.jena.mem2.store.internal;

import java.time.Instant;
import java.util.Iterator;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

import org.apache.jena.graph.Node;
import org.apache.jena.graph.Triple;
import org.apache.jena.mem2.MemoryOptimizedGraphConfiguration;
import org.apache.jena.mem2.MemoryStats;
import org.apache.jena.mem2.QueryTracker;
import org.apache.jena.mem2.store.TripleStore;
import org.apache.jena.mem2.store.StoreFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A hybrid triple store implementation that combines on-heap and off-heap storage
 * to optimize performance and memory usage based on access patterns.
 * <p>
 * This implementation uses on-heap storage for frequently accessed data and
 * off-heap storage for less frequently accessed data to optimize overall performance.
 */
public class HybridTripleStore implements TripleStore {
    
    private static final Logger LOG = LoggerFactory.getLogger(HybridTripleStore.class);
    
    // Configuration
    private final MemoryOptimizedGraphConfiguration config;
    
    // Delegate stores
    private final TripleStore heapStore;
    private final TripleStore offHeapStore;
    
    // Access tracking for migration decisions
    private final QueryTracker queryTracker;
    
    // Migration thresholds
    private static final int HOT_NODE_THRESHOLD = 100;
    private static final int COLD_NODE_THRESHOLD = 10;
    
    // Migration tracking
    private final AtomicLong migrationsToHeap = new AtomicLong(0);
    private final AtomicLong migrationsToOffHeap = new AtomicLong(0);
    
    // State tracking
    private final AtomicBoolean closed = new AtomicBoolean(false);
    
    /**
     * Create a new hybrid triple store with the specified configuration.
     * 
     * @param config The configuration for this store
     */
    public HybridTripleStore(MemoryOptimizedGraphConfiguration config) {
        this.config = config;
        
        // Enable query tracking for migration decisions
        this.config.setEnableQueryTracking(true);
        this.queryTracker = config.getQueryTracker();
        
        // Create delegate stores
        MemoryOptimizedGraphConfiguration heapConfig = new MemoryOptimizedGraphConfiguration()
            .setMemoryStrategy(MemoryOptimizedGraphConfiguration.MemoryStrategy.HEAP)
            .setInitialCapacity(config.getInitialCapacity())
            .setLoadFactor(config.getLoadFactor());
        
        MemoryOptimizedGraphConfiguration offHeapConfig = new MemoryOptimizedGraphConfiguration()
            .setMemoryStrategy(MemoryOptimizedGraphConfiguration.MemoryStrategy.OFF_HEAP)
            .setInitialCapacity(config.getInitialCapacity())
            .setLoadFactor(config.getLoadFactor());
        
        this.heapStore = new HeapTripleStore(heapConfig);
        this.offHeapStore = new OffHeapTripleStore(offHeapConfig);
        
        LOG.debug("Created HybridTripleStore with config: {}", config);
    }
    
    /**
     * Add a triple to the store.
     */
    @Override
    public boolean add(Triple triple) {
        checkNotClosed();
        
        // Determine which store to use based on access patterns
        if (isHotTriple(triple)) {
            return heapStore.add(triple);
        } else {
            return offHeapStore.add(triple);
        }
    }
    
    /**
     * Delete a triple from the store.
     */
    @Override
    public boolean delete(Triple triple) {
        checkNotClosed();
        
        // Try to delete from both stores
        boolean heapResult = heapStore.delete(triple);
        boolean offHeapResult = offHeapStore.delete(triple);
        
        return heapResult || offHeapResult;
    }
    
    /**
     * Check if a triple exists in the store.
     */
    @Override
    public boolean contains(Triple triple) {
        checkNotClosed();
        
        // Check both stores
        return heapStore.contains(triple) || offHeapStore.contains(triple);
    }
    
    /**
     * Find triples matching the pattern.
     */
    @Override
    public Iterator<Triple> find(Node s, Node p, Node o) {
        checkNotClosed();
        
        // Track query pattern for future optimization
        queryTracker.trackPattern(Triple.create(s, p, o));
        
        // Query both stores and concatenate results
        Iterator<Triple> heapResults = heapStore.find(s, p, o);
        Iterator<Triple> offHeapResults = offHeapStore.find(s, p, o);
        
        // During iteration, consider migrating triples between stores
        // This is a simplified implementation - in reality, migration would be
        // more sophisticated and possibly done in a background process
        return new Iterator<Triple>() {
            private Iterator<Triple> current = heapResults;
            private boolean usingHeapStore = true;
            
            @Override
            public boolean hasNext() {
                if (current.hasNext()) {
                    return true;
                } else if (usingHeapStore) {
                    current = offHeapResults;
                    usingHeapStore = false;
                    return current.hasNext();
                }
                return false;
            }
            
            @Override
            public Triple next() {
                Triple triple = current.next();
                
                // Consider migration (in a real implementation, this would be more sophisticated)
                if (usingHeapStore) {
                    if (!isHotTriple(triple)) {
                        // Consider migrating from heap to off-heap
                        // This is just a placeholder - actual migration would be more complex
                        // and would likely happen in a background process
                        migrationsToOffHeap.incrementAndGet();
                    }
                } else {
                    if (isHotTriple(triple)) {
                        // Consider migrating from off-heap to heap
                        migrationsToHeap.incrementAndGet();
                    }
                }
                
                return triple;
            }
        };
    }
    
    /**
     * Get the number of triples in the store.
     */
    @Override
    public int size() {
        return heapStore.size() + offHeapStore.size();
    }
    
    /**
     * Clear all triples from the store.
     */
    @Override
    public void clear() {
        checkNotClosed();
        
        heapStore.clear();
        offHeapStore.clear();
    }
    
    /**
     * Close the store and release all resources.
     */
    @Override
    public void close() {
        if (closed.compareAndSet(false, true)) {
            heapStore.close();
            offHeapStore.close();
            LOG.debug("Closed HybridTripleStore");
        }
    }
    
    /**
     * Get memory usage statistics for this store.
     */
    @Override
    public MemoryStats getMemoryStats() {
        MemoryStats heapStats = heapStore.getMemoryStats();
        MemoryStats offHeapStats = offHeapStore.getMemoryStats();
        
        return MemoryStats.builder()
            .timestamp(Instant.now())
            .onHeapBytesUsed(heapStats.getOnHeapBytesUsed())
            .onHeapBytesReserved(heapStats.getOnHeapBytesReserved())
            .offHeapBytesUsed(offHeapStats.getOffHeapBytesUsed())
            .offHeapBytesReserved(offHeapStats.getOffHeapBytesReserved())
            .nodeCount(heapStats.getNodeCount() + offHeapStats.getNodeCount())
            .cachedNodeCount(heapStats.getCachedNodeCount() + offHeapStats.getCachedNodeCount())
            .tripleCount(heapStats.getTripleCount() + offHeapStats.getTripleCount())
            .indexEntryCount(heapStats.getIndexEntryCount() + offHeapStats.getIndexEntryCount())
            .indexSizeBytes(heapStats.getIndexSizeBytes() + offHeapStats.getIndexSizeBytes())
            .gcCount(heapStats.getGcCount())
            .gcTimeMs(heapStats.getGcTimeMs())
            .build();
    }
    
    /**
     * Optimize the store's memory usage.
     */
    @Override
    public void optimize() {
        checkNotClosed();
        
        // This is a simplified implementation - in reality, this would:
        // 1. Analyze access patterns
        // 2. Migrate data between stores based on analysis
        // 3. Optimize both stores
        
        // Optimize delegate stores
        heapStore.optimize();
        offHeapStore.optimize();
        
        LOG.debug("Optimized HybridTripleStore, current stats: {}", getMemoryStats());
    }
    
    /**
     * Check if the store has been closed.
     */
    @Override
    public boolean isClosed() {
        return closed.get();
    }
    
    /**
     * Throw an exception if the store has been closed.
     */
    private void checkNotClosed() {
        if (closed.get()) {
            throw new IllegalStateException("Triple store has been closed");
        }
    }
    
    /**
     * Check if a triple is "hot" (frequently accessed).
     */
    private boolean isHotTriple(Triple triple) {
        if (triple == null) {
            return false;
        }
        
        // A triple is hot if any of its nodes is hot
        return isHotNode(triple.getSubject()) ||
               isHotNode(triple.getPredicate()) ||
               isHotNode(triple.getObject());
    }
    
    /**
     * Check if a node is "hot" (frequently accessed).
     */
    private boolean isHotNode(Node node) {
        if (node == null) {
            return false;
        }
        
        return queryTracker.isHotNode(node, HOT_NODE_THRESHOLD);
    }
    
    /**
     * Get the number of triple migrations from heap to off-heap.
     */
    public long getMigrationsToOffHeap() {
        return migrationsToOffHeap.get();
    }
    
    /**
     * Get the number of triple migrations from off-heap to heap.
     */
    public long getMigrationsToHeap() {
        return migrationsToHeap.get();
    }
}