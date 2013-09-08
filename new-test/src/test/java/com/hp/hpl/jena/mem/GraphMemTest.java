/*
    Licensed to the Apache Software Foundation (ASF) under one
    or more contributor license agreements.  See the NOTICE file
    distributed with this work for additional information
    regarding copyright ownership.  The ASF licenses this file
    to you under the Apache License, Version 2.0 (the
    "License"); you may not use this file except in compliance
    with the License.  You may obtain a copy of the License at

        http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.
 */
package com.hp.hpl.jena.mem;

import static com.hp.hpl.jena.testing_framework.GraphTestUtils.graphAdd;
import static com.hp.hpl.jena.testing_framework.GraphTestUtils.triple;
import static org.junit.Assert.*;

import java.util.Iterator;

import org.junit.Test;

import com.hp.hpl.jena.graph.AbstractGraphTest;
import com.hp.hpl.jena.graph.Graph;
import com.hp.hpl.jena.graph.GraphStatisticsHandler;
import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.graph.Node_URI;
import com.hp.hpl.jena.graph.Triple;
import com.hp.hpl.jena.graph.TripleMatch;
import com.hp.hpl.jena.shared.JenaException;
import com.hp.hpl.jena.testing_framework.GraphProducerInterface;
import com.hp.hpl.jena.util.iterator.ExtendedIterator;

public class GraphMemTest extends AbstractGraphTest {

	GraphProducerInterface graphProducer = new GraphMemSuite.GraphProducer();

	@Override
	public GraphProducerInterface getGraphProducer() {
		return graphProducer;
	}

	@Test
	public void testHasStatistics() {
		GraphStatisticsHandler h = graphProducer.newGraph()
				.getStatisticsHandler();
		assertNotNull(h);
	}

	@Test
	public void testContainsConcreteDoesntUseFind() {
		Graph g = new GraphMemWithoutFind();
		graphAdd(g, "x P y; a Q b");
		assertTrue(g.contains(triple("x P y")));
		assertTrue(g.contains(triple("a Q b")));
		assertFalse(g.contains(triple("a P y")));
		assertFalse(g.contains(triple("y R b")));
	}

	protected final class GraphMemWithoutFind extends GraphMem {
		@Override
		public ExtendedIterator<Triple> graphBaseFind(TripleMatch t) {
			throw new JenaException("find is Not Allowed");
		}
	}

	@Test
	public void testUnnecessaryMatches() {
		Node special = new Node_URI("eg:foo") {
			@Override
			public boolean matches(Node s) {
				fail("Matched called superfluously.");
				return true;
			}
		};
		Graph g = graphWithTxn("x p y");
		g.add(new Triple(special, special, special));
		exhaust(g.find(special, Node.ANY, Node.ANY));
		exhaust(g.find(Node.ANY, special, Node.ANY));
		exhaust(g.find(Node.ANY, Node.ANY, special));

	}

	protected void exhaust(Iterator<?> it) {
		while (it.hasNext())
			it.next();
	}

}
