package org.apache.jena.sparql.util;

import static org.apache.jena.atlas.iterator.Iter.iter;
import static org.apache.jena.ext.com.google.common.collect.Iterators.concat;
import static org.apache.jena.sparql.core.Quad.ANY;
import static org.apache.jena.sparql.core.Quad.defaultGraphIRI;
import static org.apache.jena.sparql.util.graph.GraphUtils.triples2quads;

import java.util.Iterator;

import org.apache.jena.graph.Graph;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.compose.Difference;
import org.apache.jena.graph.compose.MultiUnion;
import org.apache.jena.sparql.core.DatasetGraph;
import org.apache.jena.sparql.core.Quad;

public class DifferenceDatasetGraph extends ViewDatasetGraph {

	public DifferenceDatasetGraph(DatasetGraph left, DatasetGraph right) {
		this(left, right, Context.emptyContext);
	}

	public DifferenceDatasetGraph(DatasetGraph left, DatasetGraph right, Context c) {
		super(left, right, c);
	}

	@Override
	public Graph getDefaultGraph() {
		return new Difference(getRight().getDefaultGraph(), getLeft().getDefaultGraph());
	}

	@Override
	public Graph getGraph(Node graphNode) {
		return Quad.isDefaultGraph(graphNode)
				? getDefaultGraph()
				: getRight().containsGraph(graphNode)
						? new Difference(getLeft().getGraph(graphNode), getRight().getGraph(graphNode))
						: getLeft().getGraph(graphNode);
	}

	@Override
	public Graph getUnionGraph() {
		return new MultiUnion(iter(listGraphNodes()).map(this::getGraph));
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
	public Iterator<Quad> find() {
		return find(ANY);
	}

	@Override
	public Iterator<Quad> find(Quad q) {
		return find(q.getGraph(), q.getSubject(), q.getPredicate(), q.getObject());
	}

	@Override
	public Iterator<Quad> find(Node g, Node s, Node p, Node o) {
		return g.isConcrete()
				? findInOneGraph(g, s, p, o)
				: concat(findNG(null, s, p, o), findInOneGraph(defaultGraphIRI, s, p, o));
	}

	@Override
	public Iterator<Quad> findNG(Node g, Node s, Node p, Node o) {
		return g.isConcrete()
				? findInOneGraph(g, s, p, o)
				: concat(iter(listGraphNodes()).map(gn -> findInOneGraph(gn, s, p, o)));
	}

	private Iterator<Quad> findInOneGraph(Node g, Node s, Node p, Node o) {
		return triples2quads(g, getGraph(g).find(s, p, o));
	}

	@Override
	public boolean contains(Node g, Node s, Node p, Node o) {
		return getLeft().contains(g, s, p, o) && !getRight().contains(g, s, p, o);
	}

	@Override
	public boolean contains(Quad q) {
		return contains(q.getGraph(), q.getSubject(), q.getPredicate(), q.getObject());
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
