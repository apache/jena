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

package org.apache.jena.mem;

import org.apache.jena.graph.* ;
import org.apache.jena.graph.impl.AllCapabilities;
import org.apache.jena.graph.impl.TripleStore ;
import org.apache.jena.util.iterator.ExtendedIterator ;

import java.util.stream.Stream;

/**
 * In-memory, non-thread-safe, non-transactional graph.
 *
 * @deprecated This implementation of GraphMem will be replaced by a new
 *     implementation. Applications should be using
 *     {@link GraphMemFactory#createDefaultGraph()} for a general purpose graph or
 *     {@link GraphMemFactory#createGraphMem()} to specific this style of
 *     implementation.
 */
@Deprecated
public class GraphMem extends GraphMemBase {
    // Rename as GraphMemValue.

    /**
     This Graph's TripleStore. Visible for <i>read-only</i> purposes only.
     */
    public final TripleStore store;

    public GraphMem() {
        super();
        store = new GraphTripleStoreMem(this);
    }

    @Override
    protected void destroy() {
        store.close();
    }

    @Override public void performAdd( Triple t )
    { store.add( t ); }

    @Override public void performDelete( Triple t )
    { store.delete( t ); }

    @Override public int graphBaseSize()
    { return store.size(); }

    /**
     * Answer an ExtendedIterator over all the triples in this graph that match the
     * triple-pattern <code>m</code>. Delegated to the store.
     */
    @Override
    public ExtendedIterator<Triple> graphBaseFind(Triple m) {
        return store.find(m);
    }

    /**
     * Answer true iff this graph contains <code>t</code>. If <code>t</code> happens
     * to be concrete, then we hand responsibility over to the store. Otherwise we
     * use the default implementation.
     */
    @Override
    public boolean graphBaseContains(Triple t) {
        return t.isConcrete() ? store.contains(t) : store.containsMatch(t);
    }

    /**
     * Clear this GraphMem, ie remove all its triples (delegated to the store).
     */
    @Override
    public void clear() {
        clearStore();
        getEventManager().notifyEvent(this, GraphEvents.removeAll);
    }

    @Override
    public Stream<Triple> stream(Node s, Node p, Node o) {
        return store.stream(s, p, o);
    }

    /**
     * Clear this GraphMem, ie remove all its triples (delegated to the store).
     */
    public void clearStore() {
        store.clear();
    }

    @Override
    public Capabilities getCapabilities() {
        return AllCapabilities.updateAllowedWithValues;
    }
}
