/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.jena.graph.impl;

import java.util.Collection ;
import java.util.HashSet ;
import java.util.Set ;

import org.apache.jena.graph.Node ;
import org.apache.jena.graph.Triple ;
import org.apache.jena.util.iterator.ExtendedIterator ;

/**
 * A simple graph implementation that wraps a collection of triples.
 *
 * This is intended to be used in places where a graph is required but
 * iteration is the only expected operation. All graph operations are supported
 * but many are not efficient and will be slow on large collections. In these
 * cases a memory based graph will be more efficient.
 * <p>
 * This implementation:
 * <ul>
 * <li>
 * Does not support deleting triples from the iterator
 * </li></ul>
 * @deprecated To be removed.
 */
@Deprecated(forRemoval = true)
public class CollectionGraph extends GraphBase
{
    static boolean tripleContained(Triple patternTriple, Triple dataTriple) {
        return equalNode(patternTriple.getSubject(), dataTriple.getSubject())
               && equalNode(patternTriple.getPredicate(), dataTriple.getPredicate())
               && equalNode(patternTriple.getObject(), dataTriple.getObject());
    }

    private static boolean equalNode(Node m, Node n) {
        return (m == null) || (m == Node.ANY) || m.equals(n);
    }

    // the collection
    private final Collection<Triple> triples;
    private final boolean uniqueOnly;

    /**
     * Construct an empty graph using an empty HashSet. Iterator deletion is
     * supported.
     */
    public CollectionGraph() {
        this(new HashSet<>());
    }

    /**
     * Construct a graph from a collection. Iterator deletion is not supported.
     *
     * @param triples The collection of triples.
     */
    public CollectionGraph(final Collection<Triple> triples) {
        this.triples = triples;
        this.uniqueOnly = triples instanceof Set;
    }

    @Override
    protected ExtendedIterator<Triple> graphBaseFind(final Triple m) {
        ExtendedIterator<Triple> iter = SimpleEventManager.notifyingRemove(this, triples.iterator());
        return iter.filterKeep(t -> tripleContained(m, t));
    }

    @Override
    public void performAdd(final Triple t) {
        if ( uniqueOnly || !triples.contains(t) ) {
            triples.add(t);
        }
    }

    @Override
    public void performDelete(final Triple t) {
        triples.remove(t);
    }
}
