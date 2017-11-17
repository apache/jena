package org.apache.jena.sparql.util;

import static org.apache.jena.sparql.core.Quad.ANY;
import static org.apache.jena.sparql.core.Quad.isDefaultGraph;

import java.util.Iterator;
import java.util.function.Function;

import org.apache.jena.graph.Graph;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.compose.Difference;
import org.apache.jena.sparql.core.DatasetGraph;

public class DifferenceDatasetGraph extends ViewDatasetGraph {

	public DifferenceDatasetGraph(DatasetGraph left, DatasetGraph right, Context c) {
		super(left, right, c);
	}
	
	private Graph difference(Function<DatasetGraph, Graph> op) {
	    return join(Difference::new, op);
	}

	@Override
	public Graph getDefaultGraph() {
		return difference(DatasetGraph::getDefaultGraph);
	}

	@Override
	public Graph getGraph(Node graphNode) {
		return isDefaultGraph(graphNode)
				? getDefaultGraph()
				: getRight().containsGraph(graphNode)
						? difference(dsg -> dsg.getGraph(graphNode))
						: getLeft().getGraph(graphNode);
	}

	@Override
	public boolean containsGraph(Node graphNode) {
		return getLeft().containsGraph(graphNode);
	}

	@Override
	public Iterator<Node> listGraphNodes() {
		return getLeft().listGraphNodes();
	}

	@Override
	public boolean contains(Node g, Node s, Node p, Node o) {
	    return both(dsg -> dsg.contains(g, s, p, o));
	}

	@Override
	public boolean isEmpty() {
		return getLeft().isEmpty() || getLeft() == getRight() || !contains(ANY);
	}

	@Override
	public long size() {
		return getLeft().size();
	}
}
