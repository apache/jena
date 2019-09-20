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
import static org.junit.Assert.fail;

import java.util.HashMap;
import java.util.Map;

import org.apache.jena.graph.Graph;
import org.apache.jena.testing_framework.AbstractGraphProducer;
import org.junit.runner.RunWith;
import org.xenei.junit.contract.*;

@RunWith(ContractSuite.class)
@ContractImpl(Union.class)
public class UnionTest {
	
	protected IProducer<Union> graphProducer;
	
	public UnionTest() {
		graphProducer = new AbstractGraphProducer<Union>() {
			private Map<Graph, Graph[]> map = new HashMap<>();

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
}
