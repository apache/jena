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

package org.apache.jena.mem2.collection;

import java.util.Iterator;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Predicate;

import org.apache.jena.atlas.iterator.Iter;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.Triple;
import org.apache.jena.mem2.MemoryOptimizedGraphConfiguration;
import org.apache.jena.util.iterator.ExtendedIterator;
import org.apache.jena.util.iterator.NiceIterator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A memory-efficient triple table that optimizes storage and lookup of RDF triples.
 * <p>
 * This implementation uses specialized data structures to reduce memory footprint
 * and improve performance for different access patterns (S, P, O, SP, SO, PO, SPO).
 */
public class TripleTable {
    
    private static final Logger LOG = LoggerFactory.getLogger(TripleTable.class);
    
    // Configuration
    private final MemoryOptimizedGraphConfiguration config;
    
    // Triple storage and indexing
    private final Map<Triple, Triple> tripleMap;
    
    // Index for subject -> (predicate -> set of objects)
    private final Map<Node, Map<Node, Set<Node>>> spo;
    
    // Index for predicate -> (object -> set of subjects)
    private final Map<Node, Map<Node, Set<Node>>> pos;
    
    // Index for object -> (subject -> set of predicates)
    private final Map<Node, Map<Node, Set<Node>>> osp;
    
    // Statistics
    private final AtomicInteger size = new AtomicInteger(0);
    
    /**
     * Create a new triple table with the specified configuration.
     * 
     * @param config The configuration for this triple table
     */
    public TripleTable(MemoryOptimizedGraphConfiguration config) {
        this.config = config;
        
        // Initialize the triple map and indexes based on configuration
        int initialCapacity = config.getInitialCapacity();
        float loadFactor = config.getLoadFactor();
        
        this.tripleMap = JenaMapFactory.createConcurrentTripleMap(initialCapacity, loadFactor);
        this.spo = JenaMapFactory.createConcurrentNodeMap(initialCapacity, loadFactor);
        this.pos = JenaMapFactory.createConcurrentNodeMap(initialCapacity, loadFactor);
        this.osp = JenaMapFactory.createConcurrentNodeMap(initialCapacity, loadFactor);
        
        LOG.debug("Created TripleTable with initial capacity: {}", initialCapacity);
    }
    
    /**
     * Add a triple to the table.
     * 
     * @param triple The triple to add
     * @return True if the triple was added, false if it was already present
     */
    public boolean add(Triple triple) {
        // Early check for duplicates
        if (tripleMap.containsKey(triple)) {
            return false;
        }
        
        // Store the triple in the map
        tripleMap.put(triple, triple);
        
        // Extract nodes
        Node s = triple.getSubject();
        Node p = triple.getPredicate();
        Node o = triple.getObject();
        
        // Update SPO index
        addToIndex(spo, s, p, o);
        
        // Update POS index
        addToIndex(pos, p, o, s);
        
        // Update OSP index
        addToIndex(osp, o, s, p);
        
        // Update size
        size.incrementAndGet();
        
        return true;
    }
    
    /**
     * Remove a triple from the table.
     * 
     * @param triple The triple to remove
     * @return True if the triple was removed, false if it was not present
     */
    public boolean remove(Triple triple) {
        // Check if the triple exists
        if (!tripleMap.containsKey(triple)) {
            return false;
        }
        
        // Remove from the map
        tripleMap.remove(triple);
        
        // Extract nodes
        Node s = triple.getSubject();
        Node p = triple.getPredicate();
        Node o = triple.getObject();
        
        // Remove from SPO index
        removeFromIndex(spo, s, p, o);
        
        // Remove from POS index
        removeFromIndex(pos, p, o, s);
        
        // Remove from OSP index
        removeFromIndex(osp, o, s, p);
        
        // Update size
        size.decrementAndGet();
        
        return true;
    }
    
    /**
     * Check if the table contains a triple.
     * 
     * @param triple The triple to check
     * @return True if the triple is present, false otherwise
     */
    public boolean contains(Triple triple) {
        return tripleMap.containsKey(triple);
    }
    
    /**
     * Find triples matching the pattern.
     * 
     * @param s The subject (null for any)
     * @param p The predicate (null for any)
     * @param o The object (null for any)
     * @return An iterator over matching triples
     */
    public ExtendedIterator<Triple> find(Node s, Node p, Node o) {
        // Determine the most efficient access pattern
        if (s == null && p == null && o == null) {
            // Return all triples
            return toExtendedIterator(tripleMap.keySet().iterator());
        } else if (s != null && p == null && o == null) {
            // S pattern
            return findBySubject(s);
        } else if (s == null && p != null && o == null) {
            // P pattern
            return findByPredicate(p);
        } else if (s == null && p == null && o != null) {
            // O pattern
            return findByObject(o);
        } else if (s != null && p != null && o == null) {
            // SP pattern
            return findBySubjectPredicate(s, p);
        } else if (s != null && p == null && o != null) {
            // SO pattern
            return findBySubjectObject(s, o);
        } else if (s == null && p != null && o != null) {
            // PO pattern
            return findByPredicateObject(p, o);
        } else {
            // SPO pattern - check for a specific triple
            Triple t = Triple.create(s, p, o);
            if (contains(t)) {
                return toExtendedIterator(Iter.singleton(t));
            } else {
                return NiceIterator.emptyIterator();
            }
        }
    }
    
    /**
     * Find triples by subject.
     */
    private ExtendedIterator<Triple> findBySubject(Node s) {
        Map<Node, Set<Node>> poMap = spo.get(s);
        if (poMap == null) {
            return NiceIterator.emptyIterator();
        }
        
        Iterator<Triple> it = Iter.flatMap(poMap.entrySet().iterator(), 
            entry -> {
                Node p = entry.getKey();
                Set<Node> objects = entry.getValue();
                return Iter.map(objects.iterator(), 
                    o -> Triple.create(s, p, o));
            });
        
        return toExtendedIterator(it);
    }
    
    /**
     * Find triples by predicate.
     */
    private ExtendedIterator<Triple> findByPredicate(Node p) {
        Map<Node, Set<Node>> osMap = pos.get(p);
        if (osMap == null) {
            return NiceIterator.emptyIterator();
        }
        
        Iterator<Triple> it = Iter.flatMap(osMap.entrySet().iterator(), 
            entry -> {
                Node o = entry.getKey();
                Set<Node> subjects = entry.getValue();
                return Iter.map(subjects.iterator(), 
                    s -> Triple.create(s, p, o));
            });
        
        return toExtendedIterator(it);
    }
    
    /**
     * Find triples by object.
     */
    private ExtendedIterator<Triple> findByObject(Node o) {
        Map<Node, Set<Node>> spMap = osp.get(o);
        if (spMap == null) {
            return NiceIterator.emptyIterator();
        }
        
        Iterator<Triple> it = Iter.flatMap(spMap.entrySet().iterator(), 
            entry -> {
                Node s = entry.getKey();
                Set<Node> predicates = entry.getValue();
                return Iter.map(predicates.iterator(), 
                    p -> Triple.create(s, p, o));
            });
        
        return toExtendedIterator(it);
    }
    
    /**
     * Find triples by subject and predicate.
     */
    private ExtendedIterator<Triple> findBySubjectPredicate(Node s, Node p) {
        Map<Node, Set<Node>> poMap = spo.get(s);
        if (poMap == null) {
            return NiceIterator.emptyIterator();
        }
        
        Set<Node> objects = poMap.get(p);
        if (objects == null) {
            return NiceIterator.emptyIterator();
        }
        
        Iterator<Triple> it = Iter.map(objects.iterator(), 
            o -> Triple.create(s, p, o));
        
        return toExtendedIterator(it);
    }
    
    /**
     * Find triples by subject and object.
     */
    private ExtendedIterator<Triple> findBySubjectObject(Node s, Node o) {
        Map<Node, Set<Node>> spMap = osp.get(o);
        if (spMap == null) {
            return NiceIterator.emptyIterator();
        }
        
        Set<Node> predicates = spMap.get(s);
        if (predicates == null) {
            return NiceIterator.emptyIterator();
        }
        
        Iterator<Triple> it = Iter.map(predicates.iterator(), 
            p -> Triple.create(s, p, o));
        
        return toExtendedIterator(it);
    }
    
    /**
     * Find triples by predicate and object.
     */
    private ExtendedIterator<Triple> findByPredicateObject(Node p, Node o) {
        Map<Node, Set<Node>> osMap = pos.get(p);
        if (osMap == null) {
            return NiceIterator.emptyIterator();
        }
        
        Set<Node> subjects = osMap.get(o);
        if (subjects == null) {
            return NiceIterator.emptyIterator();
        }
        
        Iterator<Triple> it = Iter.map(subjects.iterator(), 
            s -> Triple.create(s, p, o));
        
        return toExtendedIterator(it);
    }
    
    /**
     * Add a node to the index.
     */
    private <A, B, C> void addToIndex(Map<A, Map<B, Set<C>>> index, A a, B b, C c) {
        Map<B, Set<C>> bcMap = index.computeIfAbsent(a, 
            k -> JenaMapFactory.createConcurrentNodeMap(config.getInitialCapacity(), config.getLoadFactor()));
        
        Set<C> cSet = bcMap.computeIfAbsent(b, 
            k -> JenaMapFactory.createConcurrentNodeSet(config.getInitialCapacity(), config.getLoadFactor()));
        
        cSet.add(c);
    }
    
    /**
     * Remove a node from the index.
     */
    private <A, B, C> void removeFromIndex(Map<A, Map<B, Set<C>>> index, A a, B b, C c) {
        Map<B, Set<C>> bcMap = index.get(a);
        if (bcMap == null) {
            return;
        }
        
        Set<C> cSet = bcMap.get(b);
        if (cSet == null) {
            return;
        }
        
        cSet.remove(c);
        
        // Clean up empty sets and maps
        if (cSet.isEmpty()) {
            bcMap.remove(b);
            
            if (bcMap.isEmpty()) {
                index.remove(a);
            }
        }
    }
    
    /**
     * Convert an iterator to an extended iterator.
     */
    private <T> ExtendedIterator<T> toExtendedIterator(Iterator<T> it) {
        return new NiceIterator<T>() {
            
            @Override
            public boolean hasNext() {
                return it.hasNext();
            }
            
            @Override
            public T next() {
                if (!hasNext()) {
                    throw new NoSuchElementException();
                }
                return it.next();
            }
            
            @Override
            public void remove() {
                it.remove();
            }
        };
    }
    
    /**
     * Get the number of triples in the table.
     */
    public int size() {
        return size.get();
    }
    
    /**
     * Clear the table.
     */
    public void clear() {
        tripleMap.clear();
        spo.clear();
        pos.clear();
        osp.clear();
        size.set(0);
    }
    
    /**
     * Get an iterator over all triples that match the filter.
     * 
     * @param filter The filter to apply
     * @return An iterator over matching triples
     */
    public ExtendedIterator<Triple> find(Predicate<Triple> filter) {
        Iterator<Triple> it = Iter.filter(tripleMap.keySet().iterator(), filter);
        return toExtendedIterator(it);
    }
}