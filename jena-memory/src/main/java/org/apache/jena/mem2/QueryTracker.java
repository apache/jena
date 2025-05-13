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

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import org.apache.jena.atlas.logging.Log;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.Triple;

/**
 * Tracks query patterns and results to optimize memory usage based on access patterns.
 */
public class QueryTracker {
    
    /** 
     * Represents a query pattern with variables replaced with placeholders.
     */
    private static class PatternKey {
        private final boolean hasSubject;
        private final boolean hasPredicate;
        private final boolean hasObject;
        
        public PatternKey(Triple pattern) {
            this.hasSubject = !pattern.getSubject().isVariable();
            this.hasPredicate = !pattern.getPredicate().isVariable();
            this.hasObject = !pattern.getObject().isVariable();
        }
        
        @Override
        public int hashCode() {
            return (hasSubject ? 1 : 0) | 
                   (hasPredicate ? 2 : 0) | 
                   (hasObject ? 4 : 0);
        }
        
        @Override
        public boolean equals(Object obj) {
            if (this == obj) return true;
            if (obj == null || getClass() != obj.getClass()) return false;
            PatternKey other = (PatternKey) obj;
            return hasSubject == other.hasSubject && 
                   hasPredicate == other.hasPredicate && 
                   hasObject == other.hasObject;
        }
        
        @Override
        public String toString() {
            return String.format("Pattern[s=%s, p=%s, o=%s]", 
                                hasSubject ? "bound" : "var", 
                                hasPredicate ? "bound" : "var", 
                                hasObject ? "bound" : "var");
        }
    }
    
    /** Statistics for a query pattern */
    private static class PatternStats {
        private final AtomicLong queryCount = new AtomicLong(0);
        private final AtomicLong resultCount = new AtomicLong(0);
        private final AtomicLong totalResultTime = new AtomicLong(0);
        private volatile long lastAccessTime = System.currentTimeMillis();
        
        public void incrementQueryCount() {
            queryCount.incrementAndGet();
            lastAccessTime = System.currentTimeMillis();
        }
        
        public void incrementResultCount(long resultTime) {
            resultCount.incrementAndGet();
            totalResultTime.addAndGet(resultTime);
        }
        
        public long getQueryCount() {
            return queryCount.get();
        }
        
        public long getResultCount() {
            return resultCount.get();
        }
        
        public double getAverageResultTime() {
            long count = resultCount.get();
            return count > 0 ? (double) totalResultTime.get() / count : 0;
        }
        
        public long getLastAccessTime() {
            return lastAccessTime;
        }
    }
    
    // Maps pattern types to statistics
    private final Map<PatternKey, PatternStats> patternStats = new ConcurrentHashMap<>();
    
    // Node access frequency tracking
    private final Map<Node, AtomicLong> nodeAccessCount = new ConcurrentHashMap<>();
    
    // Lock for statistics updates
    private final ReadWriteLock lock = new ReentrantReadWriteLock();
    
    // Configuration
    private int maxTrackedNodes = 100_000;
    private long cleanupIntervalMs = 60_000; // 1 minute
    private long lastCleanupTime = System.currentTimeMillis();
    
    /**
     * Create a new query tracker with default settings.
     */
    public QueryTracker() {
        // Use defaults
    }
    
    /**
     * Track a query pattern.
     * 
     * @param pattern The triple pattern being queried
     */
    public void trackPattern(Triple pattern) {
        if (pattern == null) return;
        
        PatternKey key = new PatternKey(pattern);
        PatternStats stats = patternStats.computeIfAbsent(key, k -> new PatternStats());
        stats.incrementQueryCount();
        
        // Track node access for bound nodes
        trackNodeAccess(pattern.getSubject());
        trackNodeAccess(pattern.getPredicate());
        trackNodeAccess(pattern.getObject());
        
        // Perform periodic cleanup if needed
        maybeCleanup();
    }
    
    /**
     * Track a query result.
     * 
     * @param pattern The triple pattern that was queried
     * @param result The result triple
     */
    public void trackResult(Triple pattern, Triple result) {
        if (pattern == null || result == null) return;
        
        PatternKey key = new PatternKey(pattern);
        PatternStats stats = patternStats.get(key);
        if (stats != null) {
            stats.incrementResultCount(1); // Simple timing for now
        }
    }
    
    /**
     * Track node access.
     * 
     * @param node The node being accessed
     */
    private void trackNodeAccess(Node node) {
        if (node == null || node.isVariable()) return;
        
        lock.readLock().lock();
        try {
            AtomicLong count = nodeAccessCount.get(node);
            if (count != null) {
                count.incrementAndGet();
                return;
            }
        } finally {
            lock.readLock().unlock();
        }
        
        // Node not in map, try to add it with write lock
        lock.writeLock().lock();
        try {
            // Double-check to avoid race condition
            nodeAccessCount.computeIfAbsent(node, n -> new AtomicLong(1));
        } finally {
            lock.writeLock().unlock();
        }
    }
    
    /**
     * Get the access count for a node.
     * 
     * @param node The node to check
     * @return The number of times the node has been accessed, or 0 if not tracked
     */
    public long getNodeAccessCount(Node node) {
        if (node == null) return 0;
        
        AtomicLong count = nodeAccessCount.get(node);
        return count != null ? count.get() : 0;
    }
    
    /**
     * Check if a node is "hot" (frequently accessed).
     * 
     * @param node The node to check
     * @param threshold The access count threshold to consider a node "hot"
     * @return true if the node is frequently accessed
     */
    public boolean isHotNode(Node node, long threshold) {
        return getNodeAccessCount(node) >= threshold;
    }
    
    /**
     * Perform cleanup if needed to prevent memory leaks.
     */
    private void maybeCleanup() {
        long now = System.currentTimeMillis();
        if (now - lastCleanupTime < cleanupIntervalMs) {
            return;
        }
        
        // Try to get write lock without blocking
        if (lock.writeLock().tryLock()) {
            try {
                // Double-check time after acquiring lock
                if (now - lastCleanupTime < cleanupIntervalMs) {
                    return;
                }
                
                // Clean up old pattern stats
                patternStats.entrySet().removeIf(entry -> 
                    now - entry.getValue().getLastAccessTime() > cleanupIntervalMs * 10);
                
                // If node map is too large, remove least accessed nodes
                if (nodeAccessCount.size() > maxTrackedNodes) {
                    // This is a simple approach - in a real implementation,
                    // we would use a priority queue or similar structure
                    // to efficiently find the least accessed nodes
                    nodeAccessCount.entrySet().stream()
                        .sorted(Map.Entry.comparingByValue((a, b) -> Long.compare(a.get(), b.get())))
                        .limit(nodeAccessCount.size() - maxTrackedNodes / 2)
                        .forEach(entry -> nodeAccessCount.remove(entry.getKey()));
                }
                
                lastCleanupTime = now;
            } catch (Exception e) {
                Log.warn(this, "Error during query tracker cleanup", e);
            } finally {
                lock.writeLock().unlock();
            }
        }
    }
    
    /**
     * Get the maximum number of nodes to track.
     */
    public int getMaxTrackedNodes() {
        return maxTrackedNodes;
    }
    
    /**
     * Set the maximum number of nodes to track.
     */
    public void setMaxTrackedNodes(int maxTrackedNodes) {
        this.maxTrackedNodes = maxTrackedNodes;
    }
    
    /**
     * Get the cleanup interval in milliseconds.
     */
    public long getCleanupIntervalMs() {
        return cleanupIntervalMs;
    }
    
    /**
     * Set the cleanup interval in milliseconds.
     */
    public void setCleanupIntervalMs(long cleanupIntervalMs) {
        this.cleanupIntervalMs = cleanupIntervalMs;
    }
    
    /**
     * Get a summary of the query statistics.
     */
    public String getStatsSummary() {
        StringBuilder sb = new StringBuilder();
        sb.append("Query Tracker Statistics:\n");
        sb.append("Pattern Stats:\n");
        
        patternStats.forEach((key, stats) -> {
            sb.append(String.format("  %s: queries=%d, results=%d, avgTime=%.2fms\n",
                key, stats.getQueryCount(), stats.getResultCount(), stats.getAverageResultTime()));
        });
        
        sb.append("Tracked Nodes: ").append(nodeAccessCount.size());
        
        return sb.toString();
    }
}