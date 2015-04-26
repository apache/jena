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

import java.util.HashMap;
import java.util.Map;

import org.junit.runner.RunWith;
import org.xenei.junit.contract.Contract;
import org.xenei.junit.contract.ContractImpl;
import org.xenei.junit.contract.ContractSuite;
import org.xenei.junit.contract.ContractTest;
import org.apache.jena.graph.Graph;
import org.apache.jena.testing_framework.AbstractGraphProducer;
import org.xenei.junit.contract.IProducer;

@RunWith(ContractSuite.class)
@ContractImpl(Delta.class)
public class DeltaTest  {

	protected IProducer<Delta> graphProducer;
	
	public DeltaTest() {
		super();
		graphProducer = new AbstractGraphProducer<Delta>() {
			private Map<Graph, Graph> map = new HashMap<Graph, Graph>();

			@Override
			protected Delta createNewGraph() {
				Graph g = memGraph();
				Delta d = new Delta(g);
				map.put(d, g);
				return d;
			}

			@Override
			public Graph[] getDependsOn(Graph d) {
//				Delta dl = (Delta)d;
//				Graph g = map.get(d);
//				if (g == null) {
//					throw new IllegalStateException("graph missing from map");
//				}
//				return new Graph[] { g,  (Graph)dl.getL(), (Graph)dl.getR() };
				return null;
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
	public final IProducer<Delta> getDeltaTestProducer() {
		return graphProducer;
	}

	@ContractTest
	public void testDelta() {
		Graph x = graphWith(getDeltaTestProducer().newInstance(), "x R y");
		assertContains("x", "x R y", x);
		x.delete(triple("x R y"));
		assertOmits("x", x, "x R y");
		/* */
		Graph base = graphWith("x R y; p S q; I like cheese; pins pop balloons");
		Delta delta = new Delta(base);
		assertContainsAll("Delta", delta,
				"x R y; p S q; I like cheese; pins pop balloons");
		assertContainsAll("Base", base,
				"x R y; p S q; I like cheese; pins pop balloons");
		/* */
		delta.add(triple("pigs fly winglessly"));
		delta.delete(triple("I like cheese"));
		/* */
		assertContainsAll("changed Delta", delta,
				"x R y; p S q; pins pop balloons; pigs fly winglessly");
		assertOmits("changed delta", delta, "I like cheese");
		assertContains("delta additions", "pigs fly winglessly",
				delta.getAdditions());
		assertOmits("delta additions", delta.getAdditions(), "I like cheese");
		assertContains("delta deletions", "I like cheese", delta.getDeletions());
		assertOmits("delta deletions", delta.getDeletions(),
				"pigs fly winglessly");
	}

}
