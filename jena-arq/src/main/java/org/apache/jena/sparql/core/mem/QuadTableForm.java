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

import static java.util.Arrays.asList;
import static java.util.Arrays.stream;
import static java.util.EnumSet.copyOf;
import static org.apache.jena.graph.Node.ANY;
import static org.apache.jena.sparql.core.mem.TupleSlot.*;

import java.util.List;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Stream;

import org.apache.jena.graph.Node;
import org.apache.jena.graph.Triple;
import org.apache.jena.sparql.core.Quad;

/**
 * Six covering table forms and machinery to determine which of them is best suited to answer a given query. Please
 * notice that the individual values of this {@code enum} are what implement the various interfaces named in the
 * signature of this type. In particular, any value from this {@code enum} is a complete implementation of
 * {@link QuadTable}. {@link HexTable} binds up all these six forms into a single implementation of {@code QuadTable}
 * that selects the most useful table form(s) for any given operation.
 *
 * @see HexTable
 */
public enum QuadTableForm implements Supplier<QuadTable>,Predicate<Set<TupleSlot>> {

	/**
	 * Graph-subject-predicate-object.
	 */
	GSPO(asList(GRAPH, SUBJECT, PREDICATE, OBJECT)) {
		@Override
		public PMapQuadTable get() {
			return new PMapQuadTable(name()) {

				@Override
				protected Quad quad(final Node g, final Node s, final Node p, final Node o) {
					return Quad.create(g, s, p, o);
				}

				@Override
				public Stream<Quad> find(final Node g, final Node s, final Node p, final Node o) {
					return _find(g, s, p, o);
				}

				@Override
				public void add(final Quad q) {
					_add(q.getGraph(), q.getSubject(), q.getPredicate(), q.getObject());
				}

				@Override
				public void delete(final Quad q) {
					_delete(q.getGraph(), q.getSubject(), q.getPredicate(), q.getObject());
				}

				@Override
				public Stream<Node> listGraphNodes() {
					return local().get().entryStream().map(Entry::getKey);
				}
			};
		}
	},

	/**
	 * Graph-object-predicate-subject.
	 */
	GOPS(asList(GRAPH, OBJECT, PREDICATE, SUBJECT)) {
		@Override
		public PMapQuadTable get() {
			return new PMapQuadTable(name()) {

				@Override
				protected Quad quad(final Node g, final Node o, final Node p, final Node s) {
					return Quad.create(g, s, p, o);
				}

				@Override
				public Stream<Quad> find(final Node g, final Node s, final Node p, final Node o) {
					return _find(g, o, p, s);
				}

				@Override
				public void add(final Quad q) {
					_add(q.getGraph(), q.getObject(), q.getPredicate(), q.getSubject());
				}

				@Override
				public void delete(final Quad q) {
					_delete(q.getGraph(), q.getObject(), q.getPredicate(), q.getSubject());
				}
			};

		}
	},

	/**
	 * Subject-predicate-object-graph.
	 */
	SPOG(asList(SUBJECT, PREDICATE, OBJECT, GRAPH)) {
		@Override
		public PMapQuadTable get() {
			return new PMapQuadTable(name()) {

				@Override
				protected Quad quad(final Node s, final Node p, final Node o, final Node g) {
					return Quad.create(g, s, p, o);
				}

				@Override
				public Stream<Quad> find(final Node g, final Node s, final Node p, final Node o) {
					return _find(s, p, o, g);
				}

				@Override
				public Stream<Quad> findInUnionGraph(final Node s, final Node p, final Node o) {
				    final AtomicReference<Triple> mostRecentlySeen = new AtomicReference<>();
				    return find(ANY, s, p, o).map(Quad::asTriple).filter(t->{
				        return !mostRecentlySeen.getAndSet(t).equals(t);
				    }).map(t->Quad.create(Quad.unionGraph, t)) ;
				}

				@Override
				public void add(final Quad q) {
					_add(q.getSubject(), q.getPredicate(), q.getObject(), q.getGraph());
				}

				@Override
				public void delete(final Quad q) {
					_delete(q.getSubject(), q.getPredicate(), q.getObject(), q.getGraph());
				}
			};
		}
	},

	/**
	 * Object-subject-graph-predicate.
	 */
	OSGP(asList(OBJECT, SUBJECT, GRAPH, PREDICATE)) {
		@Override
		public PMapQuadTable get() {
			return new PMapQuadTable(name()) {

				@Override
				protected Quad quad(final Node o, final Node s, final Node g, final Node p) {
					return Quad.create(g, s, p, o);
				}

				@Override
				public Stream<Quad> find(final Node g, final Node s, final Node p, final Node o) {
					return _find(o, s, g, p);
				}

				@Override
				public void add(final Quad q) {
					_add(q.getObject(), q.getSubject(), q.getGraph(), q.getPredicate());
				}

				@Override
				public void delete(final Quad q) {
					_delete(q.getObject(), q.getSubject(), q.getGraph(), q.getPredicate());
				}
			};
		}
	},

	/**
	 * Predicate-graph-subject-object.
	 */
	PGSO(asList(PREDICATE, GRAPH, SUBJECT, OBJECT)) {
		@Override
		public PMapQuadTable get() {
			return new PMapQuadTable(name()) {

				@Override
				protected Quad quad(final Node p, final Node g, final Node s, final Node o) {
					return Quad.create(g, s, p, o);
				}

				@Override
				public Stream<Quad> find(final Node g, final Node s, final Node p, final Node o) {
					return _find(p, g, s, o);
				}

				@Override
				public void add(final Quad q) {
					_add(q.getPredicate(), q.getGraph(), q.getSubject(), q.getObject());
				}

				@Override
				public void delete(final Quad q) {
					_delete(q.getPredicate(), q.getGraph(), q.getSubject(), q.getObject());
				}
			};
		}
	},

	/**
	 * Object-predicate-subject-graph.
	 */
	OPSG(asList(OBJECT, PREDICATE, SUBJECT, GRAPH)) {
		@Override
		public PMapQuadTable get() {
			return new PMapQuadTable(name()) {

				@Override
				protected Quad quad(final Node o, final Node p, final Node s, final Node g) {
					return Quad.create(g, s, p, o);
				}

				@Override
				public Stream<Quad> find(final Node g, final Node s, final Node p, final Node o) {
					return _find(o, p, s, g);
				}

				@Override
				public Stream<Quad> findInUnionGraph(final Node s, final Node p, final Node o) {
					final AtomicReference<Triple> mostRecentlySeen = new AtomicReference<>();
					return find(ANY, s, p, o).map(Quad::asTriple).filter(t->{
						return !mostRecentlySeen.getAndSet(t).equals(t);
					}).map(t->Quad.create(Quad.unionGraph, t)) ;
				}

				@Override
				public void add(final Quad q) {
					_add(q.getObject(), q.getPredicate(), q.getSubject(), q.getGraph());
				}

				@Override
				public void delete(final Quad q) {
					_delete(q.getObject(), q.getPredicate(), q.getSubject(), q.getGraph());
				}
			};
		}
	};

	private QuadTableForm(final List<TupleSlot> fp) {
		this.fullpattern = fp;
	}

	/**
	 * The full pattern of this table form.
	 */
	public final List<TupleSlot> fullpattern;

	/**
	 * @param pattern
	 * @return whether this table form avoids traversal for a query of this pattern
	 */
	@Override
	public boolean test(final Set<TupleSlot> pattern) {
		for (byte i = 4; i > 0; i--) {
			// copy into a set because order does not matter for this comparison: the ordering of tuples is
			// handled by individual table forms
			final Set<TupleSlot> prefix = copyOf(fullpattern.subList(0, i));
			if (prefix.equals(pattern)) return true;
		}
		return false;
	}

	/**
	 * @param pattern
	 * @return the most appropriate choice of table form for that query
	 */
	public static QuadTableForm chooseFrom(final Set<TupleSlot> pattern) {
		return tableForms().filter(f -> f.test(pattern)).findFirst().orElse(GSPO);
	}

	/**
	 * @return a stream of these table forms
	 */
	public static Stream<QuadTableForm> tableForms() {
		return stream(values());
	}
}
