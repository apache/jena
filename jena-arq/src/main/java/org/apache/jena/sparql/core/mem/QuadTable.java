/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.jena.sparql.core.mem;

import static org.apache.jena.graph.Node.ANY;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Stream;

import org.apache.jena.graph.Node;
import org.apache.jena.graph.Triple;
import org.apache.jena.sparql.core.Quad;

/**
 * A simplex or multiplex table of {@link Quad}s. Implementations may wish to override {@link #listGraphNodes()} with a
 * more efficient implementation.
 *
 */
public interface QuadTable extends TupleTable<Quad> {

	/**
	 * Search the table using a pattern of slots. {@link Node#ANY} or <code>null</code> will work as a wildcard.
	 *
	 * @param g the graph node of the pattern
	 * @param s the subject node of the pattern
	 * @param p the predicate node of the pattern
	 * @param o the object node of the pattern
	 * @return an {@link Stream} of matched quads
	 */
	Stream<Quad> find(Node g, Node s, Node p, Node o);

	/**
	 * Discover the graphs named in the table
	 *
	 * @return an {@link Stream} of graph names used in this table
	 */
	default Stream<Node> listGraphNodes() {
		return find(ANY, ANY, ANY, ANY).map(Quad::getGraph).distinct();
	}

	@Override
	default void clear() {
		find(ANY, ANY, ANY, ANY).forEach(this::delete);
	}

	default Stream<Quad> findInUnionGraph(final Node s, final Node p, final Node o) {
		final Set<Triple> seen = new HashSet<>();
		return find(ANY, s, p, o).sequential()
		    .filter(q -> !q.isDefaultGraph())
		    .map(Quad::asTriple)
		    .filter(seen::add)
		    .map(t -> Quad.create(Quad.unionGraph, t)) ;
	}
}
