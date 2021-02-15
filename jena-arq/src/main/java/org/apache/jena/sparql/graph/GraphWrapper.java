/**
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

package org.apache.jena.sparql.graph;

import org.apache.jena.graph.*;
import org.apache.jena.shared.AddDeniedException;
import org.apache.jena.shared.DeleteDeniedException;
import org.apache.jena.shared.PrefixMapping;
import org.apache.jena.util.iterator.ExtendedIterator;

/** Graph wrapper */
public class GraphWrapper implements Graph
{
    // WrappedGraph is a GraphWithPerform
    // This is a pure wrapper of Graph.
    final private Graph graph;

    public GraphWrapper(Graph graph) { this.graph = graph; }

    public Graph get() { return graph; }

    @Override
    public void add(Triple t) throws AddDeniedException {
        get().add(t);
    }

    @Override
    public boolean dependsOn(Graph other) {
        return get().dependsOn(other);
    }

    @Override
    public TransactionHandler getTransactionHandler() {
        return get().getTransactionHandler();
    }

    @Override
    public Capabilities getCapabilities() {
        return get().getCapabilities();
    }

    @Override
    public GraphEventManager getEventManager() {
        return get().getEventManager();
    }

    @Override
    public PrefixMapping getPrefixMapping() {
        return get().getPrefixMapping();
    }

    @Override
    public void delete(Triple t) throws DeleteDeniedException {
        get().delete(t);
    }

    @Override
    public ExtendedIterator<Triple> find(Triple triple) {
        return get().find(triple);
    }

    @Override
    public ExtendedIterator<Triple> find(Node s, Node p, Node o) {
        return get().find(s, p, o);
    }

    @Override
    public boolean isIsomorphicWith(Graph g) {
        return get().isIsomorphicWith(g);
    }

    @Override
    public boolean contains(Node s, Node p, Node o) {
        return get().contains(s, p, o);
    }

    @Override
    public boolean contains(Triple t) {
        return get().contains(t);
    }

    @Override
    public void close() {
        get().close();
    }

    @Override
    public boolean isEmpty() {
        return get().isEmpty();
    }

    @Override
    public int size() {
        return get().size();
    }

    @Override
    public boolean isClosed() {
        return get().isClosed();
    }

    @Override
    public void clear() {
        get().clear();
    }

    @Override
    public void remove(Node s, Node p, Node o) {
        get().remove(s, p, o);
    }
}

