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

import org.apache.jena.graph.Capabilities;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.Triple;
import org.apache.jena.graph.impl.GraphWithPerform;
import org.apache.jena.mem.GraphMemBase;
import org.apache.jena.mem2.store.TripleStore;
import org.apache.jena.util.iterator.ExtendedIterator;

import java.util.stream.Stream;

/**
 * A graph that stores triples in memory. This class is not thread-safe.
 * All triples are stored in a {@link TripleStore}.
 * <p>
 * Implementation mus always comply to term-equality semantics. The characteristics of the
 * implementations always have handlesLiteralTyping() == false.
 */
public class GraphMem2 extends GraphMemBase implements GraphWithPerform {

    final TripleStore tripleStore;

    public GraphMem2(TripleStore tripleStore) {
        super();
        this.tripleStore = tripleStore;
    }

    /**
     * Subclasses over-ride this method to release any resources they no
     * longer need once fully closed.
     */
    @Override
    public void destroy() {
        this.tripleStore.clear();
    }

    /**
     * Remove all the statements from this graph.
     */
    @Override
    public void clear() {
        super.clear(); /* deletes all triples and sends notifications*/
        this.tripleStore.clear();
    }

    /**
     * Add a triple to the graph without notifying. The default implementation throws an
     * AddDeniedException; subclasses must override if they want to be able to
     * add triples.
     *
     * @param t triple to add
     */
    @Override
    public void performAdd(final Triple t) {
        tripleStore.add(t);
    }

    /**
     * Remove a triple from the triple store. The default implementation throws
     * a DeleteDeniedException; subclasses must override if they want to be able
     * to remove triples.
     *
     * @param t triple to delete
     */
    @Override
    public void performDelete(Triple t) {
        tripleStore.remove(t);
    }

    /**
     * Returns a {@link Stream} of all triples in the graph.
     * Note: {@link Stream#parallel()} is supported.
     *
     * @return a stream  of triples in this graph.
     */
    @Override
    public Stream<Triple> stream() {
        return this.tripleStore.stream();
    }

    /**
     * Returns a {@link Stream} of Triples matching a pattern.
     * Note: {@link Stream#parallel()} is supported.
     *
     * @param sm subject node match pattern
     * @param pm predicate node match pattern
     * @param om object node match pattern
     * @return a stream  of triples in this graph matching the pattern.
     */
    @Override
    public Stream<Triple> stream(final Node sm, final Node pm, final Node om) {
        return this.tripleStore.stream(Triple.createMatch(sm, pm, om));
    }

    /**
     * Returns an {@link ExtendedIterator} of all triples in the graph matching the given triple match.
     */
    @Override
    public ExtendedIterator<Triple> graphBaseFind(Triple tripleMatch) {
        return this.tripleStore.find(tripleMatch);
    }

    /**
     * Answer true if the graph contains any triple matching <code>t</code>.
     * The default implementation uses <code>find</code> and checks to see
     * if the iterator is non-empty.
     *
     * @param tripleMatch triple match pattern, which may be contained
     */
    @Override
    public boolean graphBaseContains(final Triple tripleMatch) {
        return this.tripleStore.contains(tripleMatch);
    }

    /**
     * Answer the number of triples in this graph. Default implementation counts its
     * way through the results of a findAll. Subclasses must override if they want
     * size() to be efficient.
     */
    @Override
    public int graphBaseSize() {
        return this.tripleStore.countTriples();
    }

    @Override
    public Capabilities getCapabilities() {
        if (capabilities == null) capabilities = new Capabilities() {
            @Override
            public boolean sizeAccurate() {
                return true;
            }

            @Override
            public boolean addAllowed() {
                return true;
            }

            @Override
            public boolean deleteAllowed() {
                return true;
            }

            @Override
            public boolean handlesLiteralTyping() {
                return false;
            }
        };
        return capabilities;
    }
}
