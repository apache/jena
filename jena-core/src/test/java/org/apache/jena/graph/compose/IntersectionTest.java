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
import org.apache.jena.graph.GraphUtil;
import org.apache.jena.testing_framework.AbstractGraphProducer;
import org.xenei.junit.contract.IProducer;

@RunWith(ContractSuite.class)
@ContractImpl(Intersection.class)
public class IntersectionTest {

	protected IProducer<Intersection> graphProducer;
	
	public IntersectionTest() {
		super();
		graphProducer = new AbstractGraphProducer<Intersection>() {
			private Map<Graph, Graph[]> dependencyGraph = new HashMap<>();

			@Override
			protected Intersection createNewGraph() {
				Graph g1 = memGraph();
				Graph g2 = memGraph();
				Intersection retval = new Intersection(g1, g2);
				dependencyGraph.put(retval, new Graph[] { g1, g2 });
				return retval;
			}

			@Override
			public Graph[] getDependsOn(Graph g) {
				Graph[] dg = dependencyGraph.get(g);
				if (dg == null) {
					throw new IllegalStateException("graph missing from map");
				}
				return dg;
			}

			@Override
			public Graph[] getNotDependsOn(Graph g) {
				return new Graph[] { memGraph() };
			}

			@Override
			protected void afterClose(Graph g) {
				dependencyGraph.remove(g);
			}
		};
	}

	@Contract.Inject
	public final IProducer<Intersection> getIntersectionTestProducer() {
		return graphProducer;
	}

	@ContractTest
	public void testIntersection() {
		Graph g1 = graphWith( "x R y; p R q");
		Graph g2 = graphWith( "r Foo s; x R y");
		Intersection i = new Intersection(g1, g2);
		assertContains("Intersection", "x R y", i);
		assertOmits("Intersection", i, "p R q");
		assertOmits("Intersection", i, "r Foo s");
		if (i.size() != 1)
			fail("oops: size of intersection is not 1");
		i.add(triple("cats eat cheese"));
		assertContains("Intersection.L", "cats eat cheese", g1);
		assertContains("Intersection.R", "cats eat cheese", g2);
	}

	@ContractTest
	public void testDeleteDoesNotUpdateR() {
		Graph L = graphWith("a pings b; b pings c; c pings a");
		Graph R = graphWith("c pings a; b pings c; x captures y");
		Graph join = new Intersection(L, R);
		GraphUtil.deleteFrom(L, R);
		assertIsomorphic("R should not change",
				graphWith("c pings a; b pings c; x captures y"), R);
		assertIsomorphic(graphWith("a pings b"), L);
	}
}
