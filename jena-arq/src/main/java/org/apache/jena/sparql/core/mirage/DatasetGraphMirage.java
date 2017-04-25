package org.apache.jena.sparql.core.mirage;

import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.jena.graph.Graph;
import org.apache.jena.graph.Node;
import org.apache.jena.query.ReadWrite;
import org.apache.jena.shared.JenaException;
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
	
	public static final String NODE_ANY_URI = "urn:jena:node/any";
	
	protected final Set<Ray> rays;
	
	protected final Context context;

	protected final ThreadLocal<ReadWrite> type;
	
	protected final ThreadLocal<Set<Ray>> children;
	
	protected volatile Boolean closed = false;
	
	public DatasetGraphMirage(final Context context) {
		super();

		// TODO Implement this as a TreeSet to enable ordering and thus possible optimisation in short circuit streams.
		rays = ConcurrentHashMap.newKeySet(256);
		
		this.context = context;
		
		type = new ThreadLocal<>();
		
		children = ThreadLocal.withInitial(HashSet::new);
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
			.peek((node) -> {LOGGER.debug("node={}", node);});
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

	protected void exceptionIfClosed() {
		if (closed) {
			throw new JenaException(DatasetGraphMirage.class + " is closed");
		}
	}

	/**
	 * Return the per thread transaction type.
	 */
	protected ReadWrite getType() {
		return type.get();
	}
	
	/**
	 * Return the per thread children.
	 */
	protected Set<Ray> getChildren() {
		return children.get();
	}
	
	protected Ray begin(final Ray ray) {
		LOGGER.debug("begin(ray=[{}])", ray);
		if (!isInTransaction()) {
			throw new JenaException("No parent transaction");
		}
		try {
			if (!children.get().contains(ray)) {
				if (ray.supportsTransactions()) {
					ray.begin(type.get());
				}
				children.get().add(ray);
				LOGGER.debug("begin(ray=[{}]) added ray", ray);
			}
			return ray;
		} catch (final Exception exception) {
			throw new JenaException(exception);
		}

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
			.flatMap((ray) -> {
				return begin(ray).listGraphNodes();
			})
			.distinct()
			.peek((node) -> {LOGGER.debug("listGraphNodes() node=[{}]", node);})
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
		return true;
	}

	@Override
	public boolean supportsTransactionAbort() {
		return false;
	}
	
	/*
	 * Transactional.
	 */

	@Override
	public void begin(final ReadWrite readWrite) {
		if (isInTransaction()) {
			throw new JenaException("Already in a transaction " + type);
		}
		type.set(readWrite);
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
		if (Objects.equals(getType(), ReadWrite.WRITE)) {
			LOGGER.warn("End without commit/abort");
		}
		List<Exception> threw = new LinkedList<>();
		getChildren()
			.forEach((ray) -> {
				try {
					if (ray.supportsTransactions()) {
						ray.end();
					}
				} catch (final Exception exception) {
					threw.add(exception);
				}
			});
		getChildren().clear();
		children.remove();
		type.remove();
		if (!threw.isEmpty()) {
			final JenaException jenaException = new JenaException();
			threw.forEach((exception) -> {jenaException.addSuppressed(exception);});
			throw jenaException;
		}
	}

	@Override
	public boolean isInTransaction() {
		return getType() != null;
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
