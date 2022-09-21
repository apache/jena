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

public class TestLPBRuleEngineLeak extends TestCase {
	public static TestSuite suite() {
		return new TestSuite(TestLPBRuleEngineLeak.class);
	}

	protected Node a = NodeFactory.createURI("a");
	protected Node b = NodeFactory.createURI("b");
	protected Node nohit = NodeFactory.createURI("nohit");
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
	public void testNotLeakingActiveInterpreters() throws Exception {
		Graph data = Factory.createGraphMem();
		data.add(Triple.create(a, ty, C1));
		data.add(Triple.create(b, ty, C1));
		List<Rule> rules = Rule
				.parseRules("[r1:  (?x p ?t) <- (?x rdf:type C1), makeInstance(?x, p, C2, ?t)]"
						+ "[r2:  (?t rdf:type C2) <- (?x rdf:type C1), makeInstance(?x, p, C2, ?t)]");

		FBRuleInfGraph infgraph = (FBRuleInfGraph) createReasoner(rules).bind(
				data);

		LPBRuleEngine engine = getEngineForGraph(infgraph);
		assertEquals(0, engine.activeInterpreters.size());
		assertEquals(0, engine.tabledGoals.size());

		// we ask for a non-hit -- it works, but only because we call it.hasNext()
		ExtendedIterator<Triple> it = infgraph.find(nohit, ty, C1);
		assertFalse(it.hasNext());
		it.close();
		assertEquals(0, engine.activeInterpreters.size());

		// and again.
		// Ensure this is not cached by asking for a different triple pattern
		ExtendedIterator<Triple> it2 = infgraph.find(nohit, ty, C2);
		// uuups, forgot to call it.hasNext(). But .close() should tidy
		it2.close();
		assertEquals(0, engine.activeInterpreters.size());

		
		// OK, let's ask for something that is in the graph
		
		ExtendedIterator<Triple> it3 = infgraph.find(a, ty, C1);
		assertTrue(it3.hasNext());
		assertEquals(a, it3.next().getMatchSubject());
		
		// .. and what if we forget to call next() to consume b?
		// (e.g. return from a method with the first hit)
		
		// this should be enough
		it3.close();
		// without leaks of activeInterpreters
		assertEquals(0, engine.activeInterpreters.size());
	}
	
	@Test
	public void testTabledGoalsCacheHits() throws Exception {
		Graph data = Factory.createGraphMem();
		data.add(Triple.create(a, ty, C1));
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
