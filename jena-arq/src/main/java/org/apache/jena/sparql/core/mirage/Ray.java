package org.apache.jena.sparql.core.mirage;

import java.util.function.Function;
import java.util.stream.Stream;

import org.apache.jena.graph.Node;
import org.apache.jena.sparql.core.Quad;

public interface Ray extends Function<Quad, Stream<Quad>> {

	/**
	 * Return a node stream which indicates the graphs this Ray might return.
	 */
	Stream<Node> listGraphNodes();
}
