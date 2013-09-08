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

package com.hp.hpl.jena.graph.impl;

/**
 Tests that check GraphMem and WrappedGraph for correctness against the Graph
 and reifier test suites.
 */

import static com.hp.hpl.jena.testing_framework.GraphTestUtils.*;
import org.junit.Test;

import com.hp.hpl.jena.graph.AbstractGraphTest;
import com.hp.hpl.jena.graph.Factory;
import com.hp.hpl.jena.graph.Graph;
import com.hp.hpl.jena.graph.impl.WrappedGraph;
import com.hp.hpl.jena.testing_framework.GraphProducerInterface;

public class WrappedGraphTest extends AbstractGraphTest {

	GraphProducerInterface graphProducer = new WrappedGraphSuite.GraphProducer();

	@Override
	public GraphProducerInterface getGraphProducer() {
		return graphProducer;
	}

	/**
	 * Trivial [incomplete] test that a Wrapped graph pokes through to the
	 * underlying graph. Really want something using mock classes. Will think
	 * about it.
	 */
	@Test
	public void testWrappedSame() {
		Graph m = Factory.createGraphMem();
		Graph w = new WrappedGraph(m);
		graphAdd(m, "a trumps b; c eats d");
		assertIsomorphic(m, w);
		graphAdd(w, "i write this; you read that");
		assertIsomorphic(w, m);
	}

}
