package org.apache.jena.sparql.core.mirage;

import java.time.Duration;
import java.time.Instant;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Stream;

import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.sparql.core.Quad;

public class RayTime implements Ray {

	public static final Node TIME = NodeFactory.createURI("urn:time:");

	public static final Node INSTANT = NodeFactory.createURI("urn:time:instant");
	
	public static final Node STARTED = NodeFactory.createURI("urn:time:started");
	
	public static final Node ELAPSED = NodeFactory.createURI("urn:time:elapsed");

	public static final Node NOW = NodeFactory.createURI("urn:time:now");
	
	public static final Instant TIMESTAMP = Instant.now();
	
	public final Map<Node, Function<Quad, Stream<Quad>>> predicates;
	
	public static Node replaceAny(final Node n, final Node o) {
		return (n == Node.ANY ? o : n);
	}
	
	public RayTime() {
		predicates = new HashMap<>();
		predicates.put(NOW, (q) -> {
			return Stream.of(new Quad(replaceAny(q.getGraph(), TIME), replaceAny(q.getSubject(), INSTANT), NOW, NodeFactory.createLiteral(Instant.now().toString())));
		});
		predicates.put(STARTED, (q) -> {
			return Stream.of(new Quad(replaceAny(q.getGraph(), TIME), replaceAny(q.getSubject(), INSTANT), STARTED, NodeFactory.createLiteral(TIMESTAMP.toString())));
		});
		predicates.put(ELAPSED, (q) -> {
			return Stream.of(new Quad(replaceAny(q.getGraph(), TIME), replaceAny(q.getSubject(), INSTANT), ELAPSED, NodeFactory.createLiteral(Duration.between(TIMESTAMP, Instant.now()).toString())));
		});
	}
	
	@Override
	public Stream<Node> listGraphNodes() {
		return Stream.of(TIME);
	}

	@Override
	public Stream<Quad> apply(final Quad quad) {
		return quad.getPredicate() == Node.ANY
			? predicates
				.values()
				.parallelStream()
				.flatMap((f) -> {return f.apply(quad);})
				.distinct()
			: predicates.getOrDefault(quad.getPredicate(), (q) -> {return Stream.empty();}).apply(quad);
	}

}
