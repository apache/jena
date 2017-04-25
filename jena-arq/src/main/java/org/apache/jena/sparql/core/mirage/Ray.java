package org.apache.jena.sparql.core.mirage;

import java.util.function.Function;
import java.util.stream.Stream;

import org.apache.jena.graph.Node;
import org.apache.jena.query.ReadWrite;
import org.apache.jena.sparql.core.Quad;
import org.apache.jena.sparql.core.Transactional;

public interface Ray extends Function<Quad, Stream<Quad>>, Transactional {

	default Boolean supportsTransactions() {
		return false;
	}
	
	default Boolean supportsTransactionAbort() {
		return false;
	}
	
	/**
	 * Return a node stream which indicates the graphs this Ray might return.
	 */
	Stream<Node> listGraphNodes();

	/**
	 * Apply the given Quad and return a Quad Stream.
	 */
	@Override
	Stream<Quad> apply(Quad t);

	@Override
	default void begin(ReadWrite readWrite) {
		throw new UnsupportedOperationException();
	}

	@Override
	default void commit() {
		throw new UnsupportedOperationException();
	}

	@Override
	default void abort() {
		throw new UnsupportedOperationException();
	}

	@Override
	default void end() {
		throw new UnsupportedOperationException();
	}

	@Override
	default boolean isInTransaction() {
		return false;
	}
	
	
}
