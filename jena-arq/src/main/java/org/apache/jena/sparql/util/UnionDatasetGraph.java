package org.apache.jena.sparql.util;

import java.util.Iterator;
import java.util.function.Function;

import org.apache.jena.ext.com.google.common.collect.Iterators;
import org.apache.jena.graph.Graph;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.compose.Union;
import org.apache.jena.sparql.core.DatasetGraph;
import org.apache.jena.sparql.core.Quad;

public class UnionDatasetGraph extends ViewDatasetGraph {

    public UnionDatasetGraph(DatasetGraph left, DatasetGraph right) {
        this(left, right, Context.emptyContext);
    }

    public UnionDatasetGraph(DatasetGraph left, DatasetGraph right, Context context) {
        super(left, right);
    }

    private Graph union(Function<DatasetGraph, Graph> op) {
        return new Union(op.apply(getLeft()), op.apply(getRight()));
    }

    <T> Iterator<T> fromEach(Function<DatasetGraph, Iterator<T>> op) {
        return Iterators.concat(op.apply(getLeft()), op.apply(getRight()));
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
    public Iterator<Quad> find() {
        return fromEach(DatasetGraph::find);
    }

    @Override
    public Iterator<Quad> find(Quad quad) {
        return fromEach(dsg -> dsg.find(quad));
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
    public boolean contains(Quad quad) {
        return either(dsg -> dsg.contains(quad));
    }

    @Override
    public boolean isEmpty() {
        return both(DatasetGraph::isEmpty);
    }

    @Override
    public long size() {
        return getLeft().size() + getRight().size();
    }
}
