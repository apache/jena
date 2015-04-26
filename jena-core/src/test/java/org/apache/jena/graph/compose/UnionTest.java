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

package org.apache.jena.graph.compose;

import static org.apache.jena.testing_framework.GraphHelper.*;
import static org.junit.Assert.*;

import java.util.HashMap;
import java.util.Map;

import org.junit.runner.RunWith;
import org.xenei.junit.contract.Contract;
import org.xenei.junit.contract.ContractImpl;
import org.xenei.junit.contract.ContractSuite;
import org.xenei.junit.contract.ContractTest;
import org.apache.jena.graph.Graph;
import org.apache.jena.graph.GraphStatisticsHandler;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.Triple;
import org.apache.jena.graph.compose.Union;
import org.apache.jena.graph.impl.GraphBase;
import org.apache.jena.testing_framework.AbstractGraphProducer;
import org.xenei.junit.contract.IProducer;
import org.apache.jena.util.iterator.ExtendedIterator;

@RunWith(ContractSuite.class)
@ContractImpl(Union.class)
public class UnionTest {
	
	protected IProducer<Union> graphProducer;
	
	public UnionTest() {
		graphProducer = new AbstractGraphProducer<Union>() {
			private Map<Graph, Graph[]> map = new HashMap<Graph, Graph[]>();

			@Override
			protected Union createNewGraph() {
				Graph[] g = { memGraph(), memGraph() };
				Union u = new Union(g[0], g[1]);
				map.put(u, g);
				return u;
			}

			@Override
			public Graph[] getDependsOn(Graph d) {
				Graph[] dg = map.get(d);
				if (dg == null) {
					throw new IllegalStateException("graph not in map");
				}
				return dg;
			}

			@Override
			public Graph[] getNotDependsOn(Graph g) {
				return new Graph[] { memGraph() };
			}

			@Override
			protected void afterClose(Graph g) {
				map.remove(g);
			}
		};

	}

	@Contract.Inject
	public final IProducer<Union> getUnionTestProducer() {
		return graphProducer;
	}

	@ContractTest
	public void testUnion() {
		Graph g1 = graphWith("x R y; p R q");
		Graph g2 = graphWith("r Foo s; x R y");
		Union u = new Union(g1, g2);
		assertContains("Union", "x R y", u);
		assertContains("Union", "p R q", u);
		assertContains("Union", "r Foo s", u);
		if (u.size() != 3)
			fail("oops: size of union is not 3");
		u.add(triple("cats eat cheese"));
		assertContains("Union", "cats eat cheese", u);
		if (contains(g1, "cats eat cheese") == false
				&& contains(g2, "cats eat cheese") == false)
			fail("oops: neither g1 nor g2 contains `cats eat cheese`");
	}

	static class AnInteger {
		public int value = 0;

		public AnInteger(int value) {
			this.value = value;
		}
	}

	@ContractTest
	public void testUnionValues() {
		testUnion(0, 0, 0, 0);
	}

	@ContractTest
	public void testCopiesSingleNonZeroResult() {
		testUnion(1, 1, 0, 0);
		testUnion(1, 0, 1, 0);
		testUnion(1, 0, 0, 1);
		testUnion(1, 1, 0, 0);
		testUnion(2, 0, 2, 0);
		testUnion(4, 0, 0, 4);
	}

	@ContractTest
	public void testResultIsSumOfBaseResults() {
		testUnion(3, 1, 2, 0);
		testUnion(5, 1, 0, 4);
		testUnion(6, 0, 2, 4);
		testUnion(7, 1, 2, 4);
		testUnion(3, 0, 2, 1);
		testUnion(5, 4, 1, 0);
		testUnion(6, 2, 2, 2);
		testUnion(7, 6, 0, 1);
	}

	@ContractTest
	public void testUnknownOverrulesAll() {
		testUnion(-1, -1, 0, 0);
		testUnion(-1, 0, -1, 0);
		testUnion(-1, 0, 0, -1);
		testUnion(-1, -1, 1, 1);
		testUnion(-1, 1, -1, 1);
		testUnion(-1, 1, 1, -1);
	}

	/**
	 * Asserts that the statistic obtained by probing the three-element union
	 * with statistics <code>av</code>, <code>bv</code>, and <code>cv</code> is
	 * <code>expected</code>.
	 */
	private void testUnion(int expected, int av, int bv, int cv) {
		AnInteger a = new AnInteger(av), b = new AnInteger(bv), c = new AnInteger(
				cv);
		Graph g1 = graphWithGivenStatistic(a);
		Graph g2 = graphWithGivenStatistic(b);
		Graph g3 = graphWithGivenStatistic(c);
		Graph[] graphs = new Graph[] { g1, g2, g3 };
		MultiUnion mu = new MultiUnion(graphs);
		GraphStatisticsHandler gs = new MultiUnion.MultiUnionStatisticsHandler(
				mu);
		assertEquals(expected, gs.getStatistic(Node.ANY, Node.ANY, Node.ANY));
	}

	static Graph graphWithGivenStatistic(final AnInteger x) {
		return new GraphBase() {
			@Override
			protected ExtendedIterator<Triple> graphBaseFind(Triple t) {
				throw new RuntimeException("should never be called");
			}

			@Override
			protected GraphStatisticsHandler createStatisticsHandler() {
				return new GraphStatisticsHandler() {
					@Override
					public long getStatistic(Node S, Node P, Node O) {
						return x.value;
					}
				};
			}
		};
	}
}
