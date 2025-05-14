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

import java.util.Iterator;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Consumer;

import org.apache.jena.atlas.iterator.Iter;
import org.apache.jena.graph.*;
import org.apache.jena.graph.impl.GraphBase;
import org.apache.jena.mem2.collection.TripleTable;
import org.apache.jena.mem2.collection.JenaMapFactory;
import org.apache.jena.mem2.store.StoreFactory;
import org.apache.jena.mem2.store.TripleStore;
import org.apache.jena.shared.AddDeniedException;
import org.apache.jena.shared.DeleteDeniedException;
import org.apache.jena.shared.PrefixMapping;
import org.apache.jena.util.iterator.ExtendedIterator;
import org.apache.jena.util.iterator.WrappedIterator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A memory-optimized implementation of the Jena Graph interface.
 * <p>
 * This implementation addresses Jena's memory management issues through:
 * <ul>
 * <li>Off-heap storage for large datasets</li>
 * <li>Compressed node encoding for reduced memory footprint</li>
 * <li>Memory-efficient indexing structures</li>
 * <li>Fine-grained control over memory allocation and release</li>
 * <li>Optimized garbage collection patterns</li>
 * </ul>
 */
public class MemoryOptimizedGraph extends GraphBase {
    
    private static final Logger LOG = LoggerFactory.getLogger(MemoryOptimizedGraph.class);
    
    // Triple storage - uses different backend implementations
    private final TripleStore tripleStore;
    
    // Statistics
    private final AtomicLong addCount = new AtomicLong(0);
    private final AtomicLong deleteCount = new AtomicLong(0);
    private final AtomicLong findCount = new AtomicLong(0);
    
    // Configuration
    private final MemoryOptimizedGraphConfiguration config;
    
    /**
     * Create a memory-optimized graph with default configuration.
     */
    public MemoryOptimizedGraph() {
        this(new MemoryOptimizedGraphConfiguration());
    }
    
    /**
     * Create a memory-optimized graph with the specified configuration.
     * 
     * @param config The configuration for this graph
     */
    public MemoryOptimizedGraph(MemoryOptimizedGraphConfiguration config) {
        super();
        this.config = config;
        
        // Initialize triple store based on configuration
        this.tripleStore = StoreFactory.createTripleStore(config);
        
        LOG.debug("Created new MemoryOptimizedGraph with configuration: {}", config);
    }
    
    /**
     * Get the configuration for this graph.
     */
    public MemoryOptimizedGraphConfiguration getConfiguration() {
        return config;
    }
    
    /**
     * Get the number of triples in this graph.
     */
    @Override
    protected int graphBaseSize() {
        return tripleStore.size();
    }
    
    /**
     * Add a triple to this graph.
     */
    @Override
    public void performAdd(Triple triple) {
        if (triple == null) {
            throw new NullPointerException("Cannot add null triple");
        }
        
        try {
            if (tripleStore.add(triple)) {
                addCount.incrementAndGet();
                notifyAddTriple(this, triple);
            }
        } catch (Exception e) {
            throw new AddDeniedException("Failed to add triple: " + triple, e);
        }
    }
    
    /**
     * Delete a triple from this graph.
     */
    @Override
    public void performDelete(Triple triple) {
        if (triple == null) {
            throw new NullPointerException("Cannot delete null triple");
        }
        
        try {
            if (tripleStore.delete(triple)) {
                deleteCount.incrementAndGet();
                notifyDeleteTriple(this, triple);
            }
        } catch (Exception e) {
            throw new DeleteDeniedException("Failed to delete triple: " + triple, e);
        }
    }
    
    /**
     * Find triples matching the pattern.
     */
    @Override
    protected ExtendedIterator<Triple> graphBaseFind(Triple pattern) {
        findCount.incrementAndGet();
        
        Node s = pattern.getSubject();
        Node p = pattern.getPredicate();
        Node o = pattern.getObject();
        
        // Determine the most efficient access pattern based on the variables
        ExtendedIterator<Triple> it = WrappedIterator.create(tripleStore.find(s, p, o));
        
        // Apply any additional filtering or processing
        if (config.isEnableQueryTracking()) {
            it = it.filterKeep(triple -> {
                config.getQueryTracker().trackResult(pattern, triple);
                return true;
            });
        }
        
        return it;
    }
    
    /**
     * Get memory usage statistics for this graph.
     */
    public MemoryStats getMemoryStats() {
        return tripleStore.getMemoryStats();
    }
    
    /**
     * Clear this graph, removing all triples.
     */
    @Override
    public void clear() {
        // Clear the triple store
        tripleStore.clear();
        
        // Reset statistics
        addCount.set(0);
        deleteCount.set(0);
        
        // Notify listeners
        notifyEvent(GraphEvents.removeAll);
    }
    
    /**
     * Close this graph, releasing all resources.
     */
    @Override
    public void close() {
        try {
            // Close the triple store
            tripleStore.close();
            super.close();
            LOG.debug("Closed MemoryOptimizedGraph");
        } catch (Exception e) {
            LOG.error("Error closing MemoryOptimizedGraph", e);
        }
    }
    
    /**
     * Get the number of add operations performed on this graph.
     */
    public long getAddCount() {
        return addCount.get();
    }
    
    /**
     * Get the number of delete operations performed on this graph.
     */
    public long getDeleteCount() {
        return deleteCount.get();
    }
    
    /**
     * Get the number of find operations performed on this graph.
     */
    public long getFindCount() {
        return findCount.get();
    }
    
    /**
     * Create a new transaction for this graph.
     */
    @Override
    public GraphExtract getExtract() {
        return new GraphExtract(this);
    }
    
    /**
     * Create a memory-optimized graph with the same contents as this graph.
     */
    @Override
    public Graph copy() {
        MemoryOptimizedGraph result = new MemoryOptimizedGraph(config);
        result.getPrefixMapping().setNsPrefixes(getPrefixMapping());
        
        // Copy all triples
        find().forEachRemaining(result::add);
        
        return result;
    }
}