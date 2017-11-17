package org.apache.jena.sparql.util;

import java.util.Iterator;
import java.util.function.Function;

import org.apache.jena.atlas.iterator.Iter;
import org.apache.jena.graph.Graph;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.compose.Union;
import org.apache.jena.sparql.core.DatasetGraph;
import org.apache.jena.sparql.core.Quad;

public class UnionDatasetGraph extends ViewDatasetGraph {

    public UnionDatasetGraph(DatasetGraph left, DatasetGraph right, Context c) {
        super(left, right, c);
    }

    private Graph union(Function<DatasetGraph, Graph> op) {
        return join(Union::new, op);
    }

    <T> Iter<T> fromEach(Function<DatasetGraph, Iterator<T>> op) {
        return join(Iter::concat, op).distinct();
    }

    @Override
    public Graph getDefaultGraph() {
        return union(DatasetGraph::getDefaultGraph);
    }

    @Override
    public Graph getGraph(Node graphNode) {
        return union(dsg -> dsg.getGraph(graphNode));
    }

    @Override
    public Graph getUnionGraph() {
        return union(DatasetGraph::getUnionGraph);
    }

    @Override
    public boolean containsGraph(Node graphNode) {
        return either(dsg -> dsg.containsGraph(graphNode));
    }

    @Override
    public Iterator<Node> listGraphNodes() {
        return fromEach(DatasetGraph::listGraphNodes);
    }

    @Override
    public Iterator<Quad> find(Node g, Node s, Node p, Node o) {
        return fromEach(dsg -> dsg.find(g, s, p, o));
    }

    @Override
    public Iterator<Quad> findNG(Node g, Node s, Node p, Node o) {
        return fromEach(dsg -> dsg.findNG(g, s, p, o));
    }

    @Override
    public boolean contains(Node g, Node s, Node p, Node o) {
        return either(dsg -> dsg.contains(g, s, p, o));
    }

    @Override
    public boolean isEmpty() {
        return both(DatasetGraph::isEmpty);
    }
}
