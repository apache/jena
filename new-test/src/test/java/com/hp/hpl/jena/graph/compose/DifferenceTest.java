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

package com.hp.hpl.jena.graph.compose;

import static com.hp.hpl.jena.testing_framework.GraphTestUtils.*;
import static org.junit.Assert.*;

import org.junit.Test;

import com.hp.hpl.jena.graph.Graph;
import com.hp.hpl.jena.graph.compose.Difference;
import com.hp.hpl.jena.sparql.graph.GraphFactory;
import com.hp.hpl.jena.testing_framework.AbstractGraphProducer;
import com.hp.hpl.jena.testing_framework.GraphProducerInterface;

@SuppressWarnings("deprecation")
public class DifferenceTest extends AbstractDyadicTest {
	private GraphProducerInterface graphProducer = new AbstractGraphProducer() {
		@Override
		protected Graph createNewGraph() {
			// Graph g1 = graphWith(GraphFactory.createGraphMem(),
			// "x R y; p R q");
			// Graph g2 = graphWith(GraphFactory.createGraphMem(),
			// "r A s; x R y");
			// Graph g1 = graphWith(GraphFactory.createGraphMem(),
			// "x R y; p R q");
			// Graph g2 = graphWith(GraphFactory.createGraphMem(),
			// "r A s; x R y");
			return new Difference(GraphFactory.createGraphMem(),
					GraphFactory.createGraphMem());
		}

	};

	@Override
	public GraphProducerInterface getGraphProducer() {
		return graphProducer;
	}

	@Test
	public void testDifference() {
		Graph g1 = graphWith("x R y; p R q");
		Graph g2 = graphWith("r A s; x R y");
		Graph d = new Difference(g1, g2);
		assertOmits("Difference", d, "x R y");
		assertContains("Difference", "p R q", d);
		assertOmits("Difference", d, "r A s");
		if (d.size() != 1)
			fail("oops: size of difference is not 1");
		d.add(triple("cats eat cheese"));
		assertContains("Difference.L", "cats eat cheese", g1);
		assertOmits("Difference.R", g2, "cats eat cheese");
	}

}
