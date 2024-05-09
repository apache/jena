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
import org.apache.jena.graph.GraphListener;
import org.apache.jena.graph.GraphMemFactory;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.Triple;
import org.apache.jena.ontapi.OntJenaException;
import org.apache.jena.ontapi.UnionGraph;
import org.apache.jena.ontapi.impl.GraphListenerBase;
import org.apache.jena.ontapi.impl.OntModelEvent;
import org.apache.jena.ontapi.utils.Graphs;
import org.apache.jena.ontapi.utils.Iterators;
import org.apache.jena.vocabulary.OWL2;
import org.apache.jena.vocabulary.RDF;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class OntUnionGraphListener extends GraphListenerBase implements UnionGraph.EventManager {

    private final List<GraphListener> inactive = new ArrayList<>();
    final OntUnionGraphRepository ontGraphRepository;

    protected OntUnionGraphListener(OntUnionGraphRepository ontGraphRepository) {
        this.ontGraphRepository = Objects.requireNonNull(ontGraphRepository);
    }

    @Override
    public void off() {
        inactive.addAll(listeners);
        listeners.clear();
    }

    @Override
    public void on() {
        listeners.addAll(inactive);
        inactive.clear();
    }

    @Override
    public void onAddTriple(UnionGraph graph, Triple triple) {
        if (isNameTriple(triple)) {
            if (!graph.getBaseGraph().contains(triple)) {
                OntUnionGraphRepository.checkIDCanBeChanged(graph);
            }
        }
        listeners(UnionGraph.Listener.class).forEach(it -> it.onAddTriple(graph, triple));
    }

    @Override
    public void onDeleteTriple(UnionGraph graph, Triple triple) {
        if (isNameTriple(triple)) {
            if (graph.getBaseGraph().contains(triple)) {
                OntUnionGraphRepository.checkIDCanBeChanged(graph);
            }
        }
        listeners(UnionGraph.Listener.class).forEach(it -> it.onDeleteTriple(graph, triple));
    }

    @Override
    public void onAddSubGraph(UnionGraph graph, Graph subGraph) {
        if (Graphs.isOntGraph(Graphs.getPrimary(subGraph))) {
            Node ontology = Graphs.findOntologyNameNode(Graphs.getPrimary(subGraph))
                    .orElseThrow(() -> new OntJenaException.IllegalArgument("Unnamed or misconfigured graph is specified"));
            if (!ontology.isURI()) {
                throw new OntJenaException.IllegalArgument("Anonymous graph specified");
            }
        }
        listeners(UnionGraph.Listener.class).forEach(it -> it.onAddSubGraph(graph, subGraph));
    }

    @Override
    public void onClear(UnionGraph graph) {
        OntUnionGraphRepository.checkIDCanBeChanged(graph);
        Graphs.findOntologyNameNode(graph.getBaseGraph()).ifPresent(ontGraphRepository::remove);
    }

    @Override
    public void notifySubGraphAdded(UnionGraph thisGraph, Graph subGraph) {
        if (Graphs.isOntGraph(Graphs.getPrimary(subGraph))) {
            Graph ontSubGraphBase = OntUnionGraphRepository.getBase(subGraph);
            Node ontSubGraphIri = Graphs.findOntologyNameNode(ontSubGraphBase)
                    .filter(Node::isURI)
                    .orElseThrow(() -> new IllegalStateException("Expected to be named"));
            Graph thisOntBaseGraph = thisGraph.getBaseGraph();
            Node ontology = Graphs.ontologyNode(thisOntBaseGraph)
                    .orElseGet(() -> Graphs.createOntologyHeaderNode(thisOntBaseGraph, null));
            thisOntBaseGraph.add(ontology, OWL2.imports.asNode(), ontSubGraphIri);

            UnionGraph ontSubGraph = ontGraphRepository.put(subGraph);
            if (subGraph != ontSubGraph) {
                Graph justAddedGraph = thisGraph.subGraphs()
                        .filter(it -> OntUnionGraphRepository.graphEquals(it, subGraph))
                        .findFirst()
                        .orElseThrow(() -> new IllegalStateException("Where is just added graph?"));
                // addSubGraph is a recursive method, so off listening
                try {
                    thisGraph.getEventManager().off();
                    thisGraph.removeSubGraph(justAddedGraph).addSubGraph(ontSubGraph);
                } finally {
                    thisGraph.getEventManager().on();
                }
            }
        }
        listeners(UnionGraph.Listener.class).forEach(it -> it.notifySubGraphAdded(thisGraph, subGraph));
    }

    @Override
    public void notifySuperGraphAdded(UnionGraph graph, UnionGraph superGraph) {
        Node superGraphOntology = Graphs.ontologyNode(superGraph.getBaseGraph()).orElse(null);
        if (superGraphOntology != null) {
            Node graphName = Graphs.findOntologyNameNode(graph.getBaseGraph()).filter(Node::isURI).orElse(null);
            if (graphName != null) {
                superGraph.getBaseGraph().add(superGraphOntology, OWL2.imports.asNode(), graphName);
                ontGraphRepository.put(superGraph);
            }
        }
        listeners(UnionGraph.Listener.class).forEach(it -> it.notifySuperGraphAdded(graph, superGraph));
    }

    @Override
    public void onRemoveSubGraph(UnionGraph graph, Graph subGraph) {
        listeners(UnionGraph.Listener.class).forEach(it -> it.onRemoveSubGraph(graph, graph));
    }

    @Override
    public void notifySubGraphRemoved(UnionGraph graph, Graph subGraph) {
        if (subGraph instanceof UnionGraph && Graphs.isOntGraph(((UnionGraph) subGraph).getBaseGraph())) {
            Graph ontSubGraphBase = ((UnionGraph) subGraph).getBaseGraph();
            Node ontSubGraphIri = Graphs.findOntologyNameNode(ontSubGraphBase)
                    .filter(Node::isURI).orElse(null);
            if (ontSubGraphIri != null) {
                Graph thisOntBaseGraph = graph.getBaseGraph();
                Node ontology = Graphs.ontologyNode(thisOntBaseGraph)
                        .orElseGet(() -> Graphs.createOntologyHeaderNode(thisOntBaseGraph, null));
                thisOntBaseGraph.delete(ontology, OWL2.imports.asNode(), ontSubGraphIri);
                List<Graph> toDetach = graph.subGraphs()
                        .filter(it -> it instanceof UnionGraph)
                        .filter(
                                it -> Graphs.findOntologyNameNode(((UnionGraph) it).getBaseGraph())
                                        .filter(Node::isURI)
                                        .filter(ontSubGraphIri::equals)
                                        .isPresent()
                        )
                        .toList();
                try {
                    graph.getEventManager().off();
                    toDetach.forEach(graph::removeSubGraph);
                } finally {
                    graph.getEventManager().on();
                }
            }
        }
        listeners(UnionGraph.Listener.class).forEach(it -> it.notifySubGraphRemoved(graph, graph));
    }

    @Override
    protected void addTripleEvent(Graph g, Triple t) {
        UnionGraph thisGraph = (UnionGraph) g;
        if (isNameTriple(t)) {
            ontGraphRepository.remap(thisGraph);
        } else if (isImportTriple(t, thisGraph.getBaseGraph())) {
            UnionGraph.EventManager manager = thisGraph.getEventManager();
            try {
                manager.off();
                UnionGraph add = ontGraphRepository.get(t.getObject());
                thisGraph.addSubGraphIfAbsent(add);
            } catch (Exception ex) {
                // rollback the addition of an import statement
                thisGraph.getBaseGraph().delete(t);
                throw ex;
            } finally {
                manager.on();
            }
        }
    }

    @Override
    protected void deleteTripleEvent(Graph g, Triple t) {
        UnionGraph thisGraph = (UnionGraph) g;
        if (isNameTriple(t)) {
            ontGraphRepository.remap(thisGraph);
        } else if (isImportTriple(t, thisGraph.getBaseGraph())) {
            UnionGraph.EventManager manager = thisGraph.getEventManager();
            try {
                manager.off();
                UnionGraph toRemove = ontGraphRepository.get(t.getObject());
                thisGraph.removeSubGraph(toRemove);
            } finally {
                manager.on();
            }
        }
    }

    @Override
    public void notifyEvent(Graph source, Object event) {
        UnionGraph graph = (UnionGraph) source;
        handleChangeIDEvents(graph, event);
        handleAddDataGraphEvents(graph, event);
        handleReadDataGraphEvents(graph, event);
        handleDeleteDataGraphEvents(graph, event);
        super.notifyEvent(source, event);
    }

    private void handleReadDataGraphEvents(UnionGraph graph, Object event) {
        if (OntModelEvent.isEventOfType(event, OntModelEvent.START_READ_DATA_GRAPH)) {
            // we do not know the incoming data, so the required ontology is not used
            OntUnionGraphRepository.checkIDCanBeChanged(graph);
        }
        if (OntModelEvent.isEventOfType(event, OntModelEvent.FINISH_READ_DATA_GRAPH)) {
            ontGraphRepository.remap(graph);
            // put to process imports
            ontGraphRepository.put(graph);
        }
    }

    private void handleChangeIDEvents(UnionGraph graph, Object event) {
        if (OntModelEvent.isEventOfType(event, OntModelEvent.START_CHANGE_ID)) {
            OntUnionGraphRepository.checkIDCanBeChanged(graph);
        }
        if (OntModelEvent.isEventOfType(event, OntModelEvent.FINISH_CHANGE_ID)) {
            ontGraphRepository.remap(graph);
        }
    }

    private void handleAddDataGraphEvents(UnionGraph graph, Object event) {
        if (OntModelEvent.isEventOfType(event, OntModelEvent.START_ADD_DATA_GRAPH)) {
            Graph data = (Graph) ((OntModelEvent) event).getContent();
            Node prevName = Graphs.findOntologyNameNode(graph.getBaseGraph()).orElse(null);

            Graph header = GraphMemFactory.createDefaultGraph();
            Iterators.forEach(Graphs.listOntHeaderTriples(graph.getBaseGraph()), header::add);
            Iterators.forEach(Graphs.listOntHeaderTriples(data), header::add);

            Node newName = Graphs.findOntologyNameNode(header).orElse(null);
            if (newName == null) {
                throw new OntJenaException.IllegalArgument("Cancel. Adding data will result in invalid ontology ID");
            }
            if (!Objects.equals(newName, prevName)) {
                OntUnionGraphRepository.checkIDCanBeChanged(graph);
            }
        }
        if (OntModelEvent.isEventOfType(event, OntModelEvent.FINISH_ADD_DATA_GRAPH)) {
            ontGraphRepository.remap(graph);
            // put to process imports
            ontGraphRepository.put(graph);
        }
    }

    private void handleDeleteDataGraphEvents(UnionGraph graph, Object event) {
        if (OntModelEvent.isEventOfType(event, OntModelEvent.START_DELETE_DATA_GRAPH)) {
            Graph data = (Graph) ((OntModelEvent) event).getContent();
            Graph header = GraphMemFactory.createDefaultGraph();
            Iterators.forEach(Graphs.listOntHeaderTriples(graph.getBaseGraph()), header::add);
            Iterators.forEach(Graphs.listOntHeaderTriples(data), header::delete);

            Node newName = Graphs.findOntologyNameNode(header).orElse(null);
            if (newName == null) {
                throw new OntJenaException.IllegalArgument("Cancel. Deleting data will result in invalid ontology ID");
            }
            Node prevName = Graphs.findOntologyNameNode(graph.getBaseGraph()).orElse(null);
            if (!Objects.equals(newName, prevName)) {
                OntUnionGraphRepository.checkIDCanBeChanged(graph);
            }
        }
        if (OntModelEvent.isEventOfType(event, OntModelEvent.FINISH_DELETE_DATA_GRAPH)) {
            OntUnionGraphRepository.removeUnusedImportSubGraphs(graph);
            ontGraphRepository.remap(graph);
        }
    }

    private boolean isNameTriple(Triple t) {
        return t.getPredicate().equals(RDF.type.asNode()) && t.getObject().equals(OWL2.Ontology.asNode()) ||
                t.getPredicate().equals(OWL2.versionIRI.asNode()) && t.getObject().isURI();
    }

    private boolean isImportTriple(Triple t, Graph g) {
        if (!t.getObject().isURI() || !OWL2.imports.asNode().equals(t.getPredicate())) {
            return false;
        }
        Node subject = Graphs.ontologyNode(g).orElse(null);
        return t.getSubject().equals(subject);
    }
}
