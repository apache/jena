package org.apache.jena.sparql.core.mirage;

import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.jena.graph.Graph;
import org.apache.jena.graph.Node;
import org.apache.jena.query.ReadWrite;
import org.apache.jena.shared.Lock;
import org.apache.jena.sparql.core.DatasetGraph;
import org.apache.jena.sparql.core.GraphView;
import org.apache.jena.sparql.core.Quad;
import org.apache.jena.sparql.util.Context;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DatasetGraphMirage implements DatasetGraph {

	private static final Logger LOGGER = LoggerFactory.getLogger(DatasetGraphMirage.class);
	
	public static final Quad QUAD_ANY = new Quad(Node.ANY, Node.ANY, Node.ANY, Node.ANY);
	
	protected final Set<Ray> rays;
	
	protected final Context context;
	
	public DatasetGraphMirage(final Context context) {
		super();

		// TODO Implement this as a TreeSet to enable ordering and thus possible optimisation in short circuit streams.
		rays = ConcurrentHashMap.newKeySet(256);
		
		this.context = context;
	}
	
	public void addRay(final Ray ray) {
		getRays().add(ray);
	}
	
	public void removeRay(final Ray ray) {
		getRays().remove(ray);
	}
	
	protected Set<Ray> getRays() {
		return rays;
	}
	
	protected Stream<Ray> sequentialStreamRays() {
		return getRays().stream();
	}
	
	protected Stream<Ray> parallelStreamRays() {
		return getRays().parallelStream();
	}

	protected Stream<Ray> streamRays() {
		return parallelStreamRays();
	}

	protected Stream<Node> raysGraphs() {
		return streamRays()
			.flatMap(Ray::listGraphNodes)
			.distinct()
			.peek((node) -> {LOGGER.info("node={}", node);});
	}
	
	/**
	 * Stream the Mirage based on the given Quad.
	 */
	public Stream<Quad> mirageStream(final Quad quad) {
		return streamRays()
			.flatMap(
				(ray) -> {
					return ray.apply(quad);
				}
			)
			.distinct();
	}

	public Stream<Quad> mirageFilter(final Stream<Quad> stream, final Predicate<Quad> predicate) {
		return stream.filter(predicate::test); 
	}
	
	public boolean mirageMatchAll(final Stream<Quad> stream, final Predicate<Quad> predicate) {
		return stream.allMatch(predicate::test);
	}
	
	public boolean mirageMatchAny(final Stream<Quad> stream, final Predicate<Quad> predicate) {
		return stream.anyMatch(predicate::test);
	}
	
	/*
	 * DatasetGraph
	 */
	
	@Override
	public Graph getDefaultGraph() {
		return GraphView.createDefaultGraph(this);
	}

	@Override
	public Graph getGraph(final Node graphNode) {
		return GraphView.createNamedGraph(this, graphNode);
	}

	@Override
	public boolean containsGraph(final Node graphNode) {
		LOGGER.debug("containsGraph(node=[{}])", graphNode);
		return raysGraphs()
			.anyMatch(graphNode::matches);
	}

	@Override
	public void setDefaultGraph(Graph g) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void addGraph(Node graphName, Graph graph) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void removeGraph(Node graphName) {
		throw new UnsupportedOperationException();
	}

	@Override
	public Iterator<Node> listGraphNodes() {
		return streamRays()
			.flatMap(Ray::listGraphNodes)
			.distinct()
			.iterator();
	}

	@Override
	public void add(Quad quad) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void delete(Quad quad) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void add(Node g, Node s, Node p, Node o) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void delete(Node g, Node s, Node p, Node o) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void deleteAny(Node g, Node s, Node p, Node o) {
		throw new UnsupportedOperationException();
	}
	
	@Override
	public Iterator<Quad> find() {
		return mirageStream(QUAD_ANY).iterator();
	}

	@Override
	public Iterator<Quad> find(final Quad quad) {
		return mirageFilter(mirageStream(quad), (q) -> {return q.matches(quad.getGraph(), q.getSubject(), q.getPredicate(), q.getObject());}).iterator();
	}

	@Override
	public Iterator<Quad> find(Node g, Node s, Node p, Node o) {
		return mirageFilter(mirageStream(new Quad(g, s, p, o)), (q) -> {return q.matches(g, s, p, o);}).iterator();
	}

	@Override
	public Iterator<Quad> findNG(Node g, Node s, Node p, Node o) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean contains(Node g, Node s, Node p, Node o) {
		return mirageMatchAny(mirageStream(new Quad(g, s, p, o)), (q) -> {return q.matches(g, s, p, o);});
	}

	@Override
	public boolean contains(Quad quad) {
		return mirageMatchAny(mirageStream(quad), (q) -> {return quad.matches(q.getGraph(), q.getSubject(), q.getPredicate(), q.getObject());});
	}

	@Override
	public void clear() {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean isEmpty() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public Lock getLock() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Context getContext() {
		return context;
	}

	/**
	 * Return -1 as the size because we do not know what is in the Mirage.
	 */
	@Override
	public long size() {
		return -1;
	}

	@Override
	public void close() {
	}

	@Override
	public boolean supportsTransactions() {
		return false;
	}

	@Override
	public boolean supportsTransactionAbort() {
		return false;
	}
	
	/*
	 * Transactional.
	 */

	@Override
	public void begin(ReadWrite readWrite) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void commit() {
		throw new UnsupportedOperationException();
	}

	@Override
	public void abort() {
		throw new UnsupportedOperationException();
	}

	@Override
	public void end() {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean isInTransaction() {
		return false;
	}

	/*
	 * Object
	 */
	
	@Override
	public String toString() {
		return streamRays()
			.map(Ray::toString)
			.collect(Collectors.joining("\n", "Rays [\n", "]"));
	}

	
}
