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

package org.apache.jena.ontapi.impl.repositories;

import org.apache.jena.graph.Graph;
import org.apache.jena.graph.Node;
import org.apache.jena.ontapi.GraphMaker;
import org.apache.jena.ontapi.GraphRepository;
import org.apache.jena.ontapi.UnionGraph;
import org.apache.jena.ontapi.utils.Graphs;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * A {@code GraphRepository} implementation that supports persistence via {@link GraphMaker}.
 * Note that this repository does not permit modification of underlying storage ({@code source}).
 * Removing graph from the {@code source} is not reflected by this repository,
 * so it will still contain reference to the graph, even the graph is no longer available via {@link GraphMaker}.
 */
public class PersistentGraphRepository implements GraphRepository {
    protected final GraphMaker source;
    protected final Map<String, Graph> graphs;

    public PersistentGraphRepository(GraphMaker source) {
        this.source = Objects.requireNonNull(source);
        this.graphs = loadGraphs(source);
    }

    protected static Map<String, Graph> loadGraphs(GraphMaker source) {
        var res = new HashMap<String, Graph>();
        source.names().forEach(id -> {
            var g = source.openGraph(id);
            var name = Graphs.findOntologyNameNode(g).map(Node::toString).orElse(id);
            res.put(name, g);
        });
        return res;
    }

    /**
     * Provides access to the underlying storage.
     * @return {@link GraphMaker}
     */
    public GraphMaker getGraphMaker() {
        return source;
    }

    @Override
    public Graph get(String id) {
        return graphs.computeIfAbsent(id, source::openGraph);
    }

    @Override
    public Graph put(String id, Graph graph) {
        Set<Graph> graphs = source.graphs().collect(Collectors.toSet());
        if (graph instanceof UnionGraph) {
            if (Graphs.dataGraphs(graph).anyMatch(it -> !graphs.contains(it))) {
                throw new IllegalArgumentException(
                        "Operation 'put' is not supported for the given UnionGraph (id = " + id + "):" +
                                "it contains subgraphs that are not managed by the underlying storage");
            }
        } else if (!graphs.contains(graph)) {
            throw new IllegalArgumentException(
                    "Operation 'put' is not supported for the given Graph (id = " + id + "):" +
                            "it is not managed by the underlying storage");
        }
        var prev = this.graphs.entrySet().stream()
                .filter(it -> graph.equals(it.getValue()))
                .findFirst();
        prev.map(Map.Entry::getKey).ifPresent(this.graphs::remove);
        this.graphs.put(id, graph);
        return prev.map(Map.Entry::getValue).orElse(null);
    }

    @Override
    public Graph remove(String id) {
        // do not remove from the storage,
        // otherwise remapping will not be possible
        return graphs.remove(id);
    }

    @Override
    public void clear() {
        // do not clear the storage
        graphs.clear();
    }

    @Override
    public Stream<String> ids() {
        return graphs.keySet().stream();
    }

    @Override
    public Stream<Graph> graphs() {
        return graphs.values().stream();
    }

    @Override
    public boolean contains(String id) {
        return graphs.containsKey(id) || source.hasGraph(id);
    }

}
