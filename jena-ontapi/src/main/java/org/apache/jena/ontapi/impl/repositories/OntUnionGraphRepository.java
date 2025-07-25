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
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.ontapi.GraphRepository;
import org.apache.jena.ontapi.OntJenaException;
import org.apache.jena.ontapi.UnionGraph;
import org.apache.jena.ontapi.model.OntModel;
import org.apache.jena.ontapi.utils.Graphs;
import org.apache.jena.reasoner.InfGraph;
import org.apache.jena.vocabulary.OWL2;
import org.apache.jena.vocabulary.RDF;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashSet;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * A wrapper for {@link DocumentGraphRepository} that controls imports {@link OntModel} closure.
 */
public class OntUnionGraphRepository {
    private final GraphRepository repository;
    private final Function<Graph, UnionGraph> unionGraphFactory;
    private final Function<Node, Graph> baseGraphFactory;
    private final boolean ignoreUnresolvedImports;

    public OntUnionGraphRepository(GraphRepository repository,
                                   Function<Graph, UnionGraph> unionGraphFactory,
                                   Function<Node, Graph> baseGraphFactory,
                                   boolean ignoreUnresolvedImports) {
        this.repository = repository;
        this.unionGraphFactory = unionGraphFactory;
        this.baseGraphFactory = baseGraphFactory;
        this.ignoreUnresolvedImports = ignoreUnresolvedImports;
    }

    protected static Graph getBase(Graph graph) {
        return getRaw(graph instanceof UnionGraph ? ((UnionGraph) graph).getBaseGraph() : graph);
    }

    protected static Graph getRaw(Graph graph) {
        if (graph instanceof InfGraph) {
            return ((InfGraph) graph).getRawGraph();
        } else {
            return graph;
        }
    }

    protected static boolean graphEquals(Graph left, Graph right) {
        return left == right;
    }

    /**
     * Removes all subgraphs which are not connected to the parent by the {@code owl:imports} relationship.
     *
     * @param graph {@link UnionGraph}
     */
    public static void removeUnusedImportSubGraphs(UnionGraph graph) {
        Set<String> imports = Graphs.getImports(graph.getBaseGraph());
        Set<Graph> delete = graph.subGraphs().filter(it -> {
            String uri = Graphs.findOntologyNameNode(getBase(it)).filter(Node::isURI).map(Node::getURI).orElse(null);
            return uri != null && !imports.contains(uri);
        }).collect(Collectors.toSet());
        UnionGraph.EventManager events = graph.getEventManager();
        try {
            events.off();
            delete.forEach(graph::removeSubGraph);
        } finally {
            events.on();
        }
    }

    /**
     * Finds ont subgraph by its ontology name  ({@code owl:Ontology} or {@code owl:versionIRI}).
     *
     * @param graph {@link UnionGraph}
     * @param name  {@link Node}
     * @return {@link  Optional} wrapping subgraph
     */
    public static Optional<Graph> findSubGraphByOntName(UnionGraph graph, Node name) {
        return graph.subGraphs()
                .filter(it -> Graphs.findOntologyNameNode(getBase(it)).filter(name::equals).isPresent())
                .findFirst();
    }

    /**
     * Throws exception if graph's ontology name is in imports closure.
     *
     * @param graph {@link UnionGraph}
     */
    public static void checkIDCanBeChanged(UnionGraph graph) {
        Node name = Graphs.findOntologyNameNode(graph.getBaseGraph()).orElse(null);
        if (name == null || !name.isURI()) {
            return;
        }
        Set<String> parents = graph.superGraphs()
                .filter(it -> Graphs.hasImports(it.getBaseGraph(), name.getURI()))
                .map(it -> Graphs.findOntologyNameNode(it.getBaseGraph()).filter(x -> !name.equals(x)))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .map(Node::toString)
                .collect(Collectors.toSet());
        if (!parents.isEmpty()) {
            throw new OntJenaException.IllegalArgument(
                    "Can't change ontology ID <" + name + ">: it is used by <" + String.join(">, <", parents) + ">"
            );
        }
    }

    /**
     * @param node {@link Node} graph's ontology name ({@code owl:Ontology} or {@code owl:versionIRI}).
     * @return boolean
     */
    public boolean hasGraph(Node node) {
        Objects.requireNonNull(node);
        if (repository.contains(node.toString())) {
            return true;
        }
        return repository.ids().toList().stream().anyMatch(id -> {
            var g = repository.get(id);
            var header = Graphs.findOntologyNameNode(g).orElse(null);
            if (Objects.equals(header, node)) {
                repository.put(header.toString(), g);
                return true;
            }
            return false;
        });
    }

    /**
     * Returns the graph by its ontology name.
     *
     * @param name {@link Node}
     * @return {@link UnionGraph}, never {@code null}
     */
    public UnionGraph get(Node name) {
        return putGraph(repositoryGet(name), name.toString());
    }

    /**
     * Puts the graph into the repository returning {@link UnionGraph} wrapper.
     * All dependencies are processed.
     *
     * @param graph {@link Graph}
     * @return {@link UnionGraph}
     */
    public UnionGraph put(Graph graph) {
        return putGraph(graph, null);
    }

    /**
     * Synchronizes graph's ontology name with the underlying storage.
     * Graph's identifier in the storage must match ontology name ({@code owl:Ontology} or {@code owl:versionIRI}).
     * If there is no ontology name, the graph will be removed from the {@code repository}.
     *
     * @param graph {@link UnionGraph}
     * @return {@code true} if graph's identifier has been changed, {@code false} if no change is made
     */
    public boolean remap(UnionGraph graph) {
        String newName = Graphs.findOntologyNameNode(getBase(graph)).map(Node::toString).orElse(null);
        if (newName != null && repository.contains(newName)) {
            return false;
        }
        String prevName = repository.ids()
                .filter(name -> graphEquals(graph, repositoryGerOrNull(name)))
                .findFirst()
                .orElse(null);
        if (Objects.equals(newName, prevName)) {
            return false;
        }
        repository.remove(prevName);
        if (newName != null) {
            repository.put(newName, graph);
        }
        return true;
    }

    protected void remove(Node name) {
        repository.remove(name.toString());
    }

    protected UnionGraph putGraph(Graph root, String rootGraphId) {
        Node ontologyName = Graphs.findOntologyNameNode(getBase(root)).orElse(null);
        if (ontologyName == null) {
            throw new OntJenaException.IllegalArgument(
                    "Unnamed graph specified" +
                            (rootGraphId != null ? ", root graph = <" + rootGraphId + ">" : "")
            );
        }
        if (rootGraphId != null && !rootGraphId.equals(ontologyName.toString())) {
            throw new OntJenaException.IllegalState(
                    "Wrong mapping. Expected <" + rootGraphId + ">, but found <" + ontologyName + ">"
            );
        }
        UnionGraph res = findOrPut(root, ontologyName);

        Set<UnionGraph> seen = new HashSet<>();
        Deque<UnionGraph> queue = new ArrayDeque<>();
        queue.add(res);

        while (!queue.isEmpty()) {
            UnionGraph current = queue.removeFirst();
            if (!seen.add(current)) {
                continue;
            }
            Node currentName = Graphs.findOntologyNameNode(current.getBaseGraph()).orElse(null);
            if (currentName == null) {
                continue;
            }
            UnionGraph parent = findOrPut(current, currentName);
            Graphs.getImports(parent.getBaseGraph()).forEach(uri -> {
                UnionGraph u = putSubGraph(parent, uri);
                queue.add(u);
            });
            parent.superGraphs().forEach(queue::add);
        }
        return res;
    }

    private UnionGraph putSubGraph(UnionGraph parent, String uri) {
        Node name = NodeFactory.createURI(uri);
        Graph sub = findSubGraphByOntName(parent, name).orElse(null);
        UnionGraph u = findOrPut(sub, name);
        if (graphEquals(sub, u)) {
            return u;
        }
        UnionGraph.EventManager events = parent.getEventManager();
        try {
            events.off();
            if (sub != null) {
                parent.removeSubGraph(sub);
            }
            parent.addSubGraphIfAbsent(u);
        } finally {
            events.on();
        }
        return u;
    }

    protected UnionGraph findOrPut(Graph graph, Node ontologyName) {
        String graphId = ontologyName.toString();
        if (repository.contains(graphId)) {
            Graph found = repositoryGet(ontologyName);
            if (graph != null && !graphEquals(getBase(graph), getBase(found))) {
                throw new OntJenaException.IllegalArgument(
                        "Another graph with name <" + graphId + "> is already in the hierarchy"
                );
            }
            graph = found;
        } else if (graph == null) {
            graph = newGraph(ontologyName);
        }
        graph = getRaw(graph);
        UnionGraph union = graph instanceof UnionGraph ? (UnionGraph) graph : unionGraphFactory.apply(graph);
        attachListener(union);
        repository.put(graphId, union);
        return union;
    }

    protected Graph repositoryGet(Node name) {
        try {
            return repository.get(name.toString());
        } catch (Exception ex) {
            if (ignoreUnresolvedImports) {
                Graph res = newGraph(name);
                repository.put(name.toString(), res);
                return res;
            }
            throw ex;
        }
    }

    private Graph repositoryGerOrNull(String name) {
        try {
            return repository.get(name);
        } catch (Exception ex) {
            return null;
        }
    }

    private Graph newGraph(Node name) {
        Graph res = baseGraphFactory.apply(name);
        res.add(name, RDF.type.asNode(), OWL2.Ontology.asNode());
        return res;
    }

    protected void attachListener(UnionGraph res) {
        UnionGraph.EventManager manager = res.getEventManager();
        if (manager.listeners(OntUnionGraphListener.class).noneMatch(it -> isSameRepository(it.ontGraphRepository))) {
            manager.register(new OntUnionGraphListener(this));
        }
    }

    protected boolean isSameRepository(OntUnionGraphRepository repository) {
        return this.repository == repository.repository;
    }

}
