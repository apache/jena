/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 *
 */
package org.apache.jena.reasoner.rulesys.impl;

import java.lang.reflect.Field;
import java.util.List;

import junit.framework.TestCase;
import junit.framework.TestSuite;

import org.junit.Test;

import org.apache.jena.graph.Factory;
import org.apache.jena.graph.Graph;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.graph.Triple;
import org.apache.jena.reasoner.rulesys.FBRuleInfGraph;
import org.apache.jena.reasoner.rulesys.FBRuleReasoner;
import org.apache.jena.reasoner.rulesys.Rule;
import org.apache.jena.util.iterator.ExtendedIterator;
import org.apache.jena.vocabulary.RDF;
import org.apache.jena.vocabulary.RDFS;

public class TestLPBRuleEngine extends TestCase {
	public static TestSuite suite() {
		return new TestSuite(TestLPBRuleEngine.class);
	}

	protected Node a = NodeFactory.createURI("a");
	protected Node p = NodeFactory.createURI("p");
	protected Node C1 = NodeFactory.createURI("C1");
	protected Node C2 = NodeFactory.createURI("C2");
	protected Node ty = RDF.Nodes.type;

	public FBRuleReasoner createReasoner(List<Rule> rules) {
		FBRuleReasoner reasoner = new FBRuleReasoner(rules);
		reasoner.tablePredicate(RDFS.Nodes.subClassOf);
		reasoner.tablePredicate(RDF.Nodes.type);
		reasoner.tablePredicate(p);
		return reasoner;
	}

	@Test
	public void testTabledGoalsCacheHits() throws Exception {
		Graph data = Factory.createGraphMem();
		data.add(new Triple(a, ty, C1));
		List<Rule> rules = Rule
				.parseRules("[r1:  (?x p ?t) <- (?x rdf:type C1), makeInstance(?x, p, C2, ?t)]"
						+ "[r2:  (?t rdf:type C2) <- (?x rdf:type C1), makeInstance(?x, p, C2, ?t)]");

		FBRuleInfGraph infgraph = (FBRuleInfGraph) createReasoner(rules).bind(
				data);

		LPBRuleEngine engine = getEngineForGraph(infgraph);
		assertEquals(0, engine.activeInterpreters.size());
		assertEquals(0, engine.tabledGoals.size());

		ExtendedIterator<Triple> it = infgraph.find(a, ty, C1);
		while (it.hasNext()) {
			it.next();
			// FIXME: Why do I need to consume all from the iterator
			// to avoid leaking activeInterpreters? Calling .close()
			// below should have been enough.
		}
		it.close();
		// how many were cached
		assertEquals(1, engine.tabledGoals.size());
		// and no leaks of activeInterpreters
		assertEquals(0, engine.activeInterpreters.size());

		// Now ask again:
		it = infgraph.find(a, ty, C1);
		while (it.hasNext()) {
			it.next();
		}
		it.close();
		// if it was a cache hit, no change here:
		assertEquals(1, engine.tabledGoals.size());
		assertEquals(0, engine.activeInterpreters.size());
	}

	@Test
	public void testTabledGoalsLeak() throws Exception {
		Graph data = Factory.createGraphMem();
		data.add(new Triple(a, ty, C1));
		List<Rule> rules = Rule
				.parseRules("[r1:  (?x p ?t) <- (?x rdf:type C1), makeInstance(?x, p, C2, ?t)]"
						+ "[r2:  (?t rdf:type C2) <- (?x rdf:type C1), makeInstance(?x, p, C2, ?t)]");

		FBRuleInfGraph infgraph = (FBRuleInfGraph) createReasoner(rules).bind(
				data);

		LPBRuleEngine engine = getEngineForGraph(infgraph);
		assertEquals(0, engine.activeInterpreters.size());
		assertEquals(0, engine.tabledGoals.size());

		ExtendedIterator<Triple> it = infgraph.find(a, ty, C1);
		it.close();
		// how many were cached
		assertEquals(1, engine.tabledGoals.size());
		// and no leaks of activeInterpreters
		assertEquals(0, engine.activeInterpreters.size());

		// Now ask again:
		it = infgraph.find(a, ty, C1);
		it.close();

		// if it was a cache hit, no change here:
		assertEquals(1, engine.tabledGoals.size());
		assertEquals(0, engine.activeInterpreters.size());

		//the cached generator should not have any consumingCP left
		for(Generator generator : engine.tabledGoals.asMap().values()){
			assertEquals(0, generator.consumingCPs.size());
		}

	}

	@Test
	public void testSaturateTabledGoals() throws Exception {
		final int MAX = 1024;
		// Set the cache size very small just for this test
		System.setProperty("jena.rulesys.lp.max_cached_tabled_goals", "" + MAX);
		try {
			Graph data = Factory.createGraphMem();
			data.add(new Triple(a, ty, C1));
			List<Rule> rules = Rule
					.parseRules("[r1:  (?x p ?t) <- (?x rdf:type C1), makeInstance(?x, p, C2, ?t)]"
							+ "[r2:  (?t rdf:type C2) <- (?x rdf:type C1), makeInstance(?x, p, C2, ?t)]");

			FBRuleInfGraph infgraph = (FBRuleInfGraph) createReasoner(rules)
					.bind(data);

			LPBRuleEngine engine = getEngineForGraph(infgraph);
			assertEquals(0, engine.activeInterpreters.size());
			assertEquals(0, engine.tabledGoals.size());

			// JENA-901
			// Let's ask about lots of unknown subjects
			for (int i = 0; i < MAX * 128; i++) {
				Node test = NodeFactory.createURI("test" + i);
				ExtendedIterator<Triple> it = infgraph.find(test, ty, C2);
				assertFalse(it.hasNext());
				it.close();
			}

			// Let's see how many were cached
			assertEquals(MAX, engine.tabledGoals.size());
			// and no leaks of activeInterpreters (this will happen if we forget
			// to call hasNext above)
			assertEquals(0, engine.activeInterpreters.size());
		} finally {
			System.clearProperty("jena.rulesys.lp.max_cached_tabled_goals");

		}
	}

	/**
	 * Use introspection to get to the LPBRuleEngine.
	 * <p>
	 * We are crossing package boundaries and therefore this test would always
	 * be in the wrong package for either FBRuleInfGraph or LPBRuleEngine.
	 * <p>
	 * <strong>This method should only be used for test purposes.</strong>
	 *
	 * @param infgraph
	 * @return
	 * @throws SecurityException
	 * @throws NoSuchFieldException
	 * @throws IllegalArgumentException
	 * @throws IllegalAccessException
	 */
	private LPBRuleEngine getEngineForGraph(FBRuleInfGraph infgraph)
			throws NoSuchFieldException, SecurityException,
			IllegalArgumentException, IllegalAccessException {
		Field bEngine = FBRuleInfGraph.class.getDeclaredField("bEngine");
		bEngine.setAccessible(true);
		LPBRuleEngine engine = (LPBRuleEngine) bEngine.get(infgraph);
		return engine;
	}

}
