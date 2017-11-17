package org.apache.jena.sparql.util;

import static org.apache.jena.atlas.iterator.Iter.filter;

import java.util.Iterator;
import java.util.function.Function;

import org.apache.jena.graph.Graph;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.compose.Intersection;
import org.apache.jena.sparql.core.DatasetGraph;

public class IntersectionDatasetGraph extends ViewDatasetGraph {

	public IntersectionDatasetGraph(DatasetGraph left, DatasetGraph right, Context c) {
		super(left, right, c);
	}

	Graph intersect(Function<DatasetGraph, Graph> op) {
	    return join(Intersection::new, op);
	}

	@Override
	public Graph getDefaultGraph() {
		return intersect(DatasetGraph::getDefaultGraph);
	}

	@Override
	public Graph getGraph(Node graphNode) {
		return intersect(dsg -> dsg.getGraph(graphNode));
	}

	@Override
	public boolean containsGraph(Node graphNode) {
		return both(dsg -> dsg.containsGraph(graphNode));
	}

	@Override
	public Iterator<Node> listGraphNodes() {
		return filter(getLeft().listGraphNodes(), getRight()::containsGraph);
	}

	@Override
	public boolean contains(Node g, Node s, Node p, Node o) {
		return both(dsg -> dsg.contains(g, s, p, o));
	}
}
