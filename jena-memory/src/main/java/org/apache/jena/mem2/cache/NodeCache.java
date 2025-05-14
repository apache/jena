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

import org.apache.jena.graph.Node;
import org.apache.jena.mem2.MemoryStats;

/**
 * Interface for node caching implementations.
 * <p>
 * A node cache reduces memory usage by ensuring that identical nodes
 * share the same memory location, effectively implementing node interning.
 * Different implementations may use different storage strategies (on-heap,
 * off-heap, hybrid) to optimize for different workloads and memory constraints.
 */
public interface NodeCache {
    
    /**
     * Get or add a node to the cache.
     * <p>
     * If the node already exists in the cache, return the cached instance.
     * Otherwise, add the node to the cache and return it.
     * 
     * @param node The node to get or add
     * @return The cached node (which may be the same as the input node)
     */
    Node getOrAdd(Node node);
    
    /**
     * Check if a node is in the cache.
     * 
     * @param node The node to check
     * @return True if the node is in the cache, false otherwise
     */
    boolean contains(Node node);
    
    /**
     * Remove a node from the cache.
     * 
     * @param node The node to remove
     * @return True if the node was in the cache and was removed,
     *         false if the node was not in the cache
     */
    boolean remove(Node node);
    
    /**
     * Clear all nodes from the cache.
     */
    void clear();
    
    /**
     * Get the number of nodes in the cache.
     * 
     * @return The number of nodes
     */
    int size();
    
    /**
     * Get memory usage statistics for this cache.
     * 
     * @return Memory statistics
     */
    MemoryStats getMemoryStats();
    
    /**
     * Optimize the cache for memory usage or performance.
     * <p>
     * This may involve reorganizing data structures, changing storage strategies,
     * or other optimizations based on observed access patterns.
     */
    void optimize();
    
    /**
     * Close the cache and release any resources.
     */
    void close();
    
    /**
     * Check if the cache is closed.
     * 
     * @return True if the cache is closed, false otherwise
     */
    boolean isClosed();
}