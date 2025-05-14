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

import java.time.Instant;

/**
 * Memory usage statistics for optimized graph storage.
 */
public class MemoryStats {
    
    // On-heap memory usage
    private final long onHeapBytesUsed;
    private final long onHeapBytesReserved;
    
    // Off-heap memory usage
    private final long offHeapBytesUsed;
    private final long offHeapBytesReserved;
    
    // Node statistics
    private final long nodeCount;
    private final long cachedNodeCount;
    
    // Triple statistics
    private final long tripleCount;
    
    // Index statistics
    private final long indexEntryCount;
    private final long indexSizeBytes;
    
    // GC statistics
    private final long gcCount;
    private final long gcTimeMs;
    
    // Timestamp
    private final Instant timestamp;
    
    /**
     * Create a new memory stats instance.
     * 
     * @param builder The builder to use
     */
    private MemoryStats(Builder builder) {
        this.onHeapBytesUsed = builder.onHeapBytesUsed;
        this.onHeapBytesReserved = builder.onHeapBytesReserved;
        this.offHeapBytesUsed = builder.offHeapBytesUsed;
        this.offHeapBytesReserved = builder.offHeapBytesReserved;
        this.nodeCount = builder.nodeCount;
        this.cachedNodeCount = builder.cachedNodeCount;
        this.tripleCount = builder.tripleCount;
        this.indexEntryCount = builder.indexEntryCount;
        this.indexSizeBytes = builder.indexSizeBytes;
        this.gcCount = builder.gcCount;
        this.gcTimeMs = builder.gcTimeMs;
        this.timestamp = builder.timestamp != null ? builder.timestamp : Instant.now();
    }
    
    /**
     * Get the number of bytes used on the heap.
     */
    public long getOnHeapBytesUsed() {
        return onHeapBytesUsed;
    }
    
    /**
     * Get the number of bytes reserved on the heap.
     */
    public long getOnHeapBytesReserved() {
        return onHeapBytesReserved;
    }
    
    /**
     * Get the number of bytes used off the heap.
     */
    public long getOffHeapBytesUsed() {
        return offHeapBytesUsed;
    }
    
    /**
     * Get the number of bytes reserved off the heap.
     */
    public long getOffHeapBytesReserved() {
        return offHeapBytesReserved;
    }
    
    /**
     * Get the total number of bytes used (on-heap + off-heap).
     */
    public long getTotalBytesUsed() {
        return onHeapBytesUsed + offHeapBytesUsed;
    }
    
    /**
     * Get the total number of bytes reserved (on-heap + off-heap).
     */
    public long getTotalBytesReserved() {
        return onHeapBytesReserved + offHeapBytesReserved;
    }
    
    /**
     * Get the percentage of reserved memory that is used.
     */
    public double getMemoryUtilization() {
        long reserved = getTotalBytesReserved();
        return reserved > 0 ? (double) getTotalBytesUsed() / reserved : 0.0;
    }
    
    /**
     * Get the number of nodes.
     */
    public long getNodeCount() {
        return nodeCount;
    }
    
    /**
     * Get the number of cached nodes.
     */
    public long getCachedNodeCount() {
        return cachedNodeCount;
    }
    
    /**
     * Get the node cache hit ratio.
     */
    public double getNodeCacheHitRatio() {
        return nodeCount > 0 ? (double) cachedNodeCount / nodeCount : 0.0;
    }
    
    /**
     * Get the number of triples.
     */
    public long getTripleCount() {
        return tripleCount;
    }
    
    /**
     * Get the number of index entries.
     */
    public long getIndexEntryCount() {
        return indexEntryCount;
    }
    
    /**
     * Get the size of the index in bytes.
     */
    public long getIndexSizeBytes() {
        return indexSizeBytes;
    }
    
    /**
     * Get the number of bytes per triple (total memory / triple count).
     */
    public double getBytesPerTriple() {
        return tripleCount > 0 ? (double) getTotalBytesUsed() / tripleCount : 0.0;
    }
    
    /**
     * Get the number of garbage collections.
     */
    public long getGcCount() {
        return gcCount;
    }
    
    /**
     * Get the time spent in garbage collection in milliseconds.
     */
    public long getGcTimeMs() {
        return gcTimeMs;
    }
    
    /**
     * Get the timestamp when these statistics were collected.
     */
    public Instant getTimestamp() {
        return timestamp;
    }
    
    /**
     * Create a new builder for memory stats.
     */
    public static Builder builder() {
        return new Builder();
    }
    
    /**
     * Builder for memory stats.
     */
    public static class Builder {
        private long onHeapBytesUsed;
        private long onHeapBytesReserved;
        private long offHeapBytesUsed;
        private long offHeapBytesReserved;
        private long nodeCount;
        private long cachedNodeCount;
        private long tripleCount;
        private long indexEntryCount;
        private long indexSizeBytes;
        private long gcCount;
        private long gcTimeMs;
        private Instant timestamp;
        
        /**
         * Set the number of bytes used on the heap.
         */
        public Builder onHeapBytesUsed(long onHeapBytesUsed) {
            this.onHeapBytesUsed = onHeapBytesUsed;
            return this;
        }
        
        /**
         * Set the number of bytes reserved on the heap.
         */
        public Builder onHeapBytesReserved(long onHeapBytesReserved) {
            this.onHeapBytesReserved = onHeapBytesReserved;
            return this;
        }
        
        /**
         * Set the number of bytes used off the heap.
         */
        public Builder offHeapBytesUsed(long offHeapBytesUsed) {
            this.offHeapBytesUsed = offHeapBytesUsed;
            return this;
        }
        
        /**
         * Set the number of bytes reserved off the heap.
         */
        public Builder offHeapBytesReserved(long offHeapBytesReserved) {
            this.offHeapBytesReserved = offHeapBytesReserved;
            return this;
        }
        
        /**
         * Set the number of nodes.
         */
        public Builder nodeCount(long nodeCount) {
            this.nodeCount = nodeCount;
            return this;
        }
        
        /**
         * Set the number of cached nodes.
         */
        public Builder cachedNodeCount(long cachedNodeCount) {
            this.cachedNodeCount = cachedNodeCount;
            return this;
        }
        
        /**
         * Set the number of triples.
         */
        public Builder tripleCount(long tripleCount) {
            this.tripleCount = tripleCount;
            return this;
        }
        
        /**
         * Set the number of index entries.
         */
        public Builder indexEntryCount(long indexEntryCount) {
            this.indexEntryCount = indexEntryCount;
            return this;
        }
        
        /**
         * Set the size of the index in bytes.
         */
        public Builder indexSizeBytes(long indexSizeBytes) {
            this.indexSizeBytes = indexSizeBytes;
            return this;
        }
        
        /**
         * Set the number of garbage collections.
         */
        public Builder gcCount(long gcCount) {
            this.gcCount = gcCount;
            return this;
        }
        
        /**
         * Set the time spent in garbage collection in milliseconds.
         */
        public Builder gcTimeMs(long gcTimeMs) {
            this.gcTimeMs = gcTimeMs;
            return this;
        }
        
        /**
         * Set the timestamp when these statistics were collected.
         */
        public Builder timestamp(Instant timestamp) {
            this.timestamp = timestamp;
            return this;
        }
        
        /**
         * Build the memory stats object.
         */
        public MemoryStats build() {
            return new MemoryStats(this);
        }
    }
    
    @Override
    public String toString() {
        return String.format(
            "MemoryStats[onHeap=%,d/%,d bytes, offHeap=%,d/%,d bytes, " +
            "nodes=%,d (cached=%,d), triples=%,d, " +
            "indexEntries=%,d (%,d bytes), " +
            "gc=%d (%,d ms), timestamp=%s]",
            onHeapBytesUsed, onHeapBytesReserved,
            offHeapBytesUsed, offHeapBytesReserved,
            nodeCount, cachedNodeCount, tripleCount,
            indexEntryCount, indexSizeBytes,
            gcCount, gcTimeMs, timestamp);
    }
}