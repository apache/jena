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

import java.util.Iterator;

import org.apache.jena.graph.Node;
import org.apache.jena.graph.Triple;
import org.apache.jena.mem2.MemoryStats;

/**
 * Interface for triple stores that manage memory efficiently.
 */
public interface TripleStore {
    
    /**
     * Add a triple to the store.
     * 
     * @param triple The triple to add
     * @return True if the triple was added, false if it was already present
     */
    boolean add(Triple triple);
    
    /**
     * Delete a triple from the store.
     * 
     * @param triple The triple to delete
     * @return True if the triple was deleted, false if it was not present
     */
    boolean delete(Triple triple);
    
    /**
     * Check if a triple exists in the store.
     * 
     * @param triple The triple to check
     * @return True if the triple exists, false otherwise
     */
    boolean contains(Triple triple);
    
    /**
     * Find triples matching the pattern.
     * 
     * @param s The subject node (or null for any subject)
     * @param p The predicate node (or null for any predicate)
     * @param o The object node (or null for any object)
     * @return An iterator over matching triples
     */
    Iterator<Triple> find(Node s, Node p, Node o);
    
    /**
     * Get the number of triples in the store.
     * 
     * @return The number of triples
     */
    int size();
    
    /**
     * Clear all triples from the store.
     */
    void clear();
    
    /**
     * Close the store and release all resources.
     */
    void close();
    
    /**
     * Get memory usage statistics for this store.
     * 
     * @return Memory usage statistics
     */
    MemoryStats getMemoryStats();
    
    /**
     * Optimize the store's memory usage.
     * This may involve compacting data structures, freeing unused memory,
     * or other optimizations to reduce memory footprint.
     */
    void optimize();
    
    /**
     * Check if the store has been closed.
     * 
     * @return True if the store has been closed, false otherwise
     */
    boolean isClosed();
}