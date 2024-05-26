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

import static java.util.stream.Collectors.toSet;
import static org.apache.jena.graph.Node.ANY;
import static org.apache.jena.graph.NodeFactory.createBlankNode;
import static org.apache.jena.sparql.core.mem.TupleSlot.*;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Stream;

import org.apache.jena.graph.Node;
import org.apache.jena.sparql.core.Quad;
import org.junit.Test;
import static org.junit.Assert.* ;

public class TestHexTable extends AbstractTestQuadTable {

	@Test
	public void testListGraphNodes() {
		final int nodesToTry = 50;
		final HexTable index = new HexTable();
		final Set<Node> graphNodes = new HashSet<>(nodesToTry);
		index.begin(null);
		for (int i = 0; i < nodesToTry; i++) {
			final Node node = createBlankNode();
			index.add(Quad.create(node, node, node, node));
			graphNodes.add(node);
			assertEquals(graphNodes, index.listGraphNodes().collect(toSet()));
		}
		index.end();
	}

	@Test
	public void checkConcreteQueries() {
		queryPatterns().filter(p -> !allWildcardQuery.equals(p)).map(TestHexTable::exampleFrom).forEach(testQuery -> {
			final HexTable index = new HexTable();
			index.begin(null);
			// add our sample quad
			index.add(testTuple());
			// add a noise quad from which our sample should be distinguished
			final Node node = createBlankNode();
			final Quad noiseQuad = Quad.create(node, node, node, node);
			index.add(noiseQuad);
			index.commit();

			index.begin(null);
			Set<Quad> contents = index
					.find(testQuery.getGraph(), testQuery.getSubject(), testQuery.getPredicate(), testQuery.getObject())
					.collect(toSet());
			assertEquals(Set.of(testTuple()), contents);
			// both Node.ANY and null should work as wildcards
			contents = index.find(null, ANY, null, ANY).collect(toSet());
			assertEquals(Set.of(testTuple(), noiseQuad), contents);
			index.end();
		});
	}

	private static Quad exampleFrom(final Set<TupleSlot> pattern) {
		return Quad.create(pattern.contains(GRAPH) ? sampleNode : ANY, pattern.contains(SUBJECT) ? sampleNode : ANY,
				pattern.contains(PREDICATE) ? sampleNode : ANY, pattern.contains(OBJECT) ? sampleNode : ANY);
	}

	private final HexTable testTable = new HexTable();

	@Override
	protected QuadTable table() {
		return testTable;
	}

	@Override
	protected Stream<Quad> tuples() {
		return table().find(ANY, ANY, ANY, ANY);
	}
}
