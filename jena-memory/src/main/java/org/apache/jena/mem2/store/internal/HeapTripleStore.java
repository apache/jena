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

import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.lang.management.MemoryUsage;
import java.time.Instant;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Function;

import org.apache.jena.atlas.iterator.Iter;
import org.apache.jena.atlas.lib.Pair;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.Triple;
import org.apache.jena.mem2.MemoryOptimizedGraphConfiguration;
import org.apache.jena.mem2.MemoryStats;
import org.apache.jena.mem2.store.TripleStore;
import org.apache.jena.util.iterator.ExtendedIterator;
import org.apache.jena.util.iterator.NiceIterator;
import org.apache.jena.util.iterator.WrappedIterator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A memory-efficient triple store implementation that stores all data on the Java heap
 * with optimizations for minimizing memory footprint.
 */
public class HeapTripleStore implements TripleStore {
    
    private static final Logger LOG = LoggerFactory.getLogger(HeapTripleStore.class);
    
    // Configuration
    private final MemoryOptimizedGraphConfiguration config;
    
    // Triple count
    private int count = 0;
    
    // Indexes for different access patterns (SPO, POS, OSP)
    private final Map<Triple, Triple> tripleMap;
    
    // Index for subject -> (predicate -> set of objects)
    private final Map<Node, Map<Node, TripleSet>> spo;
    
    // Index for predicate -> (object -> set of subjects)
    private final Map<Node, Map<Node, TripleSet>> pos;
    
    // Index for object -> (subject -> set of predicates)
    private final Map<Node, Map<Node, TripleSet>> osp;
    
    // Node cache to prevent duplicate storage of identical nodes
    private final Map<Node, Node> nodeCache;
    
    // State tracking
    private final AtomicBoolean closed = new AtomicBoolean(false);
    
    // Memory monitoring
    private final MemoryMXBean memoryMXBean = ManagementFactory.getMemoryMXBean();
    private long heapBytesAtStartup;
    
    /**
     * Create a new heap triple store with the specified configuration.
     * 
     * @param config The configuration for this store
     */
    public HeapTripleStore(MemoryOptimizedGraphConfiguration config) {
        this.config = config;
        
        // Initialize heapBytesAtStartup to initial heap usage
        MemoryUsage heapUsage = memoryMXBean.getHeapMemoryUsage();
        this.heapBytesAtStartup = heapUsage.getUsed();
        
        // Initialize the triple map and indexes based on configuration
        int initialCapacity = config.getInitialCapacity();
        float loadFactor = config.getLoadFactor();
        boolean concurrent = true; // Use concurrent maps for thread safety
        
        if (concurrent) {
            this.tripleMap = new ConcurrentHashMap<>(initialCapacity, loadFactor);
            this.spo = new ConcurrentHashMap<>(initialCapacity, loadFactor);
            this.pos = new ConcurrentHashMap<>(initialCapacity, loadFactor);
            this.osp = new ConcurrentHashMap<>(initialCapacity, loadFactor);
            this.nodeCache = new ConcurrentHashMap<>(config.getNodeCacheSize(), loadFactor);
        } else {
            this.tripleMap = new HashMap<>(initialCapacity, loadFactor);
            this.spo = new HashMap<>(initialCapacity, loadFactor);
            this.pos = new HashMap<>(initialCapacity, loadFactor);
            this.osp = new HashMap<>(initialCapacity, loadFactor);
            this.nodeCache = new HashMap<>(config.getNodeCacheSize(), loadFactor);
        }
        
        LOG.debug("Created HeapTripleStore with config: {}", config);
    }
    
    /**
     * Add a triple to the store.
     */
    @Override
    public boolean add(Triple triple) {
        checkNotClosed();
        
        // Early check for duplicates using the triple map
        if (tripleMap.containsKey(triple)) {
            return false;
        }
        
        // Internalize the nodes to reduce memory usage
        Node s = internNode(triple.getSubject());
        Node p = internNode(triple.getPredicate());
        Node o = internNode(triple.getObject());
        
        // Create a new triple with the internalized nodes
        Triple internedTriple = Triple.create(s, p, o);
        
        // Check again for duplicates after internalization
        if (tripleMap.putIfAbsent(internedTriple, internedTriple) != null) {
            return false;
        }
        
        // Update SPO index
        Map<Node, TripleSet> poMap = spo.computeIfAbsent(s, k -> createNodeMap());
        TripleSet oSet = poMap.computeIfAbsent(p, k -> new TripleSet());
        oSet.add(internedTriple);
        
        // Update POS index
        Map<Node, TripleSet> osMap = pos.computeIfAbsent(p, k -> createNodeMap());
        TripleSet sSet = osMap.computeIfAbsent(o, k -> new TripleSet());
        sSet.add(internedTriple);
        
        // Update OSP index
        Map<Node, TripleSet> spMap = osp.computeIfAbsent(o, k -> createNodeMap());
        TripleSet pSet = spMap.computeIfAbsent(s, k -> new TripleSet());
        pSet.add(internedTriple);
        
        // Update count
        count++;
        
        return true;
    }
    
    /**
     * Delete a triple from the store.
     */
    @Override
    public boolean delete(Triple triple) {
        checkNotClosed();
        
        // Check if the triple exists in the store
        Triple internedTriple = tripleMap.remove(triple);
        if (internedTriple == null) {
            return false;
        }
        
        // Get the internalized nodes from the stored triple
        Node s = internedTriple.getSubject();
        Node p = internedTriple.getPredicate();
        Node o = internedTriple.getObject();
        
        // Remove from SPO index
        removeFromIndex(spo, s, p, internedTriple, (map, key) -> {
            TripleSet set = map.get(key);
            if (set != null) {
                set.remove(internedTriple);
                return set.isEmpty();
            }
            return false;
        });
        
        // Remove from POS index
        removeFromIndex(pos, p, o, internedTriple, (map, key) -> {
            TripleSet set = map.get(key);
            if (set != null) {
                set.remove(internedTriple);
                return set.isEmpty();
            }
            return false;
        });
        
        // Remove from OSP index
        removeFromIndex(osp, o, s, internedTriple, (map, key) -> {
            TripleSet set = map.get(key);
            if (set != null) {
                set.remove(internedTriple);
                return set.isEmpty();
            }
            return false;
        });
        
        // Update count
        count--;
        
        return true;
    }
    
    /**
     * Check if a triple exists in the store.
     */
    @Override
    public boolean contains(Triple triple) {
        checkNotClosed();
        return tripleMap.containsKey(triple);
    }
    
    /**
     * Find triples matching the pattern.
     */
    @Override
    public Iterator<Triple> find(Node s, Node p, Node o) {
        checkNotClosed();
        
        // Determine the most efficient access pattern based on the variables
        if (s == null && p == null && o == null) {
            // All variables - return all triples
            return getAllTriples();
        } else if (s != null && p == null && o == null) {
            // Only subject is bound - use SPO index
            return getBySubject(s);
        } else if (s == null && p != null && o == null) {
            // Only predicate is bound - use POS index
            return getByPredicate(p);
        } else if (s == null && p == null && o != null) {
            // Only object is bound - use OSP index
            return getByObject(o);
        } else if (s != null && p != null && o == null) {
            // Subject and predicate are bound - use SPO index
            return getBySubjectPredicate(s, p);
        } else if (s != null && p == null && o != null) {
            // Subject and object are bound - use OSP index
            return getByObjectSubject(o, s);
        } else if (s == null && p != null && o != null) {
            // Predicate and object are bound - use POS index
            return getByPredicateObject(p, o);
        } else {
            // All three are bound - check for a specific triple
            Triple triple = Triple.create(s, p, o);
            if (contains(triple)) {
                return Iter.singleton(triple);
            } else {
                return Iter.nullIterator();
            }
        }
    }
    
    /**
     * Get the number of triples in the store.
     */
    @Override
    public int size() {
        return count;
    }
    
    /**
     * Clear all triples from the store.
     */
    @Override
    public void clear() {
        checkNotClosed();
        
        tripleMap.clear();
        spo.clear();
        pos.clear();
        osp.clear();
        count = 0;
        
        // We keep the node cache for reuse
    }
    
    /**
     * Close the store and release all resources.
     */
    @Override
    public void close() {
        if (closed.compareAndSet(false, true)) {
            clear();
            nodeCache.clear();
            LOG.debug("Closed HeapTripleStore");
        }
    }
    
    /**
     * Get memory usage statistics for this store.
     */
    @Override
    public MemoryStats getMemoryStats() {
        MemoryUsage heapUsage = memoryMXBean.getHeapMemoryUsage();
        
        return MemoryStats.builder()
            .timestamp(Instant.now())
            .onHeapBytesUsed(heapUsage.getUsed() - heapBytesAtStartup)
            .onHeapBytesReserved(heapUsage.getCommitted())
            .offHeapBytesUsed(0) // Heap store doesn't use off-heap memory
            .offHeapBytesReserved(0)
            .nodeCount(nodeCache.size())
            .cachedNodeCount(nodeCache.size())
            .tripleCount(count)
            .indexEntryCount(estimateIndexEntryCount())
            .indexSizeBytes(estimateIndexSizeBytes())
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
     * Optimize the store's memory usage.
     */
    @Override
    public void optimize() {
        checkNotClosed();
        
        // Request garbage collection
        System.gc();
        
        LOG.debug("Optimized HeapTripleStore, current stats: {}", getMemoryStats());
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
     * Internalize a node to reduce memory usage.
     */
    private Node internNode(Node node) {
        if (node == null) {
            return null;
        }
        
        return nodeCache.computeIfAbsent(node, Function.identity());
    }
    
    /**
     * Create a new node map with the appropriate implementation.
     */
    private <K, V> Map<K, V> createNodeMap() {
        // Use concurrent maps for thread safety
        return new ConcurrentHashMap<>(config.getInitialCapacity(), config.getLoadFactor());
    }
    
    /**
     * Remove a triple from an index.
     */
    private <T, U> void removeFromIndex(Map<T, Map<U, TripleSet>> index, T key1, U key2, 
                                      Triple triple, java.util.function.BiPredicate<Map<U, TripleSet>, U> emptyPredicate) {
        Map<U, TripleSet> map = index.get(key1);
        if (map != null) {
            if (emptyPredicate.test(map, key2)) {
                map.remove(key2);
                if (map.isEmpty()) {
                    index.remove(key1);
                }
            }
        }
    }
    
    /**
     * Get an iterator over all triples in the store.
     */
    private Iterator<Triple> getAllTriples() {
        return tripleMap.keySet().iterator();
    }
    
    /**
     * Get an iterator over all triples with the given subject.
     */
    private Iterator<Triple> getBySubject(Node s) {
        Map<Node, TripleSet> poMap = spo.get(s);
        if (poMap == null) {
            return Iter.nullIterator();
        }
        
        return Iter.flatMap(poMap.values().iterator(), 
            ts -> ts.iterator());
    }
    
    /**
     * Get an iterator over all triples with the given predicate.
     */
    private Iterator<Triple> getByPredicate(Node p) {
        Map<Node, TripleSet> osMap = pos.get(p);
        if (osMap == null) {
            return Iter.nullIterator();
        }
        
        return Iter.flatMap(osMap.values().iterator(), 
            ts -> ts.iterator());
    }
    
    /**
     * Get an iterator over all triples with the given object.
     */
    private Iterator<Triple> getByObject(Node o) {
        Map<Node, TripleSet> spMap = osp.get(o);
        if (spMap == null) {
            return Iter.nullIterator();
        }
        
        return Iter.flatMap(spMap.values().iterator(), 
            ts -> ts.iterator());
    }
    
    /**
     * Get an iterator over all triples with the given subject and predicate.
     */
    private Iterator<Triple> getBySubjectPredicate(Node s, Node p) {
        Map<Node, TripleSet> poMap = spo.get(s);
        if (poMap == null) {
            return Iter.nullIterator();
        }
        
        TripleSet oSet = poMap.get(p);
        if (oSet == null) {
            return Iter.nullIterator();
        }
        
        return oSet.iterator();
    }
    
    /**
     * Get an iterator over all triples with the given predicate and object.
     */
    private Iterator<Triple> getByPredicateObject(Node p, Node o) {
        Map<Node, TripleSet> osMap = pos.get(p);
        if (osMap == null) {
            return Iter.nullIterator();
        }
        
        TripleSet sSet = osMap.get(o);
        if (sSet == null) {
            return Iter.nullIterator();
        }
        
        return sSet.iterator();
    }
    
    /**
     * Get an iterator over all triples with the given object and subject.
     */
    private Iterator<Triple> getByObjectSubject(Node o, Node s) {
        Map<Node, TripleSet> spMap = osp.get(o);
        if (spMap == null) {
            return Iter.nullIterator();
        }
        
        TripleSet pSet = spMap.get(s);
        if (pSet == null) {
            return Iter.nullIterator();
        }
        
        return pSet.iterator();
    }
    
    /**
     * Estimate the number of entries in all indexes.
     */
    private long estimateIndexEntryCount() {
        // Count entries in all indexes
        long spoEntries = countMapOfMaps(spo);
        long posEntries = countMapOfMaps(pos);
        long ospEntries = countMapOfMaps(osp);
        
        return spoEntries + posEntries + ospEntries;
    }
    
    /**
     * Count the number of entries in a map of maps.
     */
    private <T, U, V> long countMapOfMaps(Map<T, Map<U, V>> map) {
        long count = 0;
        for (Map<U, V> innerMap : map.values()) {
            count += innerMap.size();
        }
        return count;
    }
    
    /**
     * Estimate the size of the indexes in bytes.
     */
    private long estimateIndexSizeBytes() {
        // Simple estimation based on average sizes
        long entryCount = estimateIndexEntryCount();
        long bytesPerEntry = 40; // Rough estimate including overhead
        
        return entryCount * bytesPerEntry;
    }
    
    /**
     * A memory-efficient set implementation for storing triples.
     */
    private static class TripleSet {
        private Triple[] elements;
        private int size;
        
        public TripleSet() {
            this.elements = new Triple[8]; // Start small
            this.size = 0;
        }
        
        /**
         * Add a triple to the set.
         */
        public synchronized boolean add(Triple triple) {
            // Check if already present
            for (int i = 0; i < size; i++) {
                if (elements[i].equals(triple)) {
                    return false;
                }
            }
            
            // Ensure capacity
            if (size == elements.length) {
                elements = Arrays.copyOf(elements, elements.length * 2);
            }
            
            // Add element
            elements[size++] = triple;
            return true;
        }
        
        /**
         * Remove a triple from the set.
         */
        public synchronized boolean remove(Triple triple) {
            // Find the element
            for (int i = 0; i < size; i++) {
                if (elements[i].equals(triple)) {
                    // Remove by shifting
                    System.arraycopy(elements, i + 1, elements, i, size - i - 1);
                    elements[--size] = null; // Avoid memory leak
                    return true;
                }
            }
            return false;
        }
        
        /**
         * Check if the set is empty.
         */
        public boolean isEmpty() {
            return size == 0;
        }
        
        /**
         * Get the size of the set.
         */
        public int size() {
            return size;
        }
        
        /**
         * Get an iterator over the set.
         */
        public Iterator<Triple> iterator() {
            return new Iterator<Triple>() {
                private int index = 0;
                
                @Override
                public boolean hasNext() {
                    return index < size;
                }
                
                @Override
                public Triple next() {
                    return elements[index++];
                }
            };
        }
    }
}