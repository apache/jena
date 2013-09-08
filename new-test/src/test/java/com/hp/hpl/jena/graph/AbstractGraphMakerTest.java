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

package com.hp.hpl.jena.graph;

import static org.junit.Assert.*;
import java.util.Set;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.hp.hpl.jena.graph.Graph;
import com.hp.hpl.jena.graph.GraphMaker;
import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.graph.Triple;
import com.hp.hpl.jena.shared.AlreadyExistsException;
import com.hp.hpl.jena.shared.DoesNotExistException;
import com.hp.hpl.jena.testing_framework.GraphTestUtils;
import com.hp.hpl.jena.testing_framework.TestUtils;

/**
 * Abstract base class for testing graph factories. Subclasses define the method
 * <code>getGraphFactory()</code> which supplies a new graph factory to be
 * tested: ATGF invokes that during <code>setUp</code> and closes it in
 * <code>tearDown</code>.
 * <p>
 * This bunch of tests is not remotely exhaustive, but it was sufficent to drive
 * the development of the first full graph factory. (Although at the time it
 * wasn't abstract.)
 */

public abstract class AbstractGraphMakerTest {

	public abstract GraphMaker getGraphMaker();

	private GraphMaker graphMaker;

	@Before
	public void setUp() {
		graphMaker = getGraphMaker();
	}

	@After
	public void tearDown() {
		graphMaker.close();
	}

	/**
	 * A trivial test that getGraph delivers a proper graph, not cheating with
	 * null, and that getGraph() "always" delivers the same Graph.
	 */
	@Test
	public void testGetGraph() {
		Graph g1 = graphMaker.getGraph();
		assertFalse("should deliver a Graph", g1 == null);
		assertSame(g1, graphMaker.getGraph());
		g1.close();
	}

	@Test
	public void testCreateGraph() {
		TestUtils.assertDiffer("each created graph must differ",
				graphMaker.createGraph(), graphMaker.createGraph());
	}

	@Test
	public void testAnyName() {
		graphMaker.createGraph("plain").close();
		graphMaker.createGraph("with.dot").close();
		graphMaker.createGraph("http://electric-hedgehog.net/topic#marker")
				.close();
	}

	/**
	 * Test that we can't create a graph with the same name twice.
	 */
	@Test
	public void testCannotCreateTwice() {
		String name = jName("bonsai");
		graphMaker.createGraph(name, true);
		try {
			graphMaker.createGraph(name, true);
			fail("should not be able to create " + name + " twice");
		} catch (AlreadyExistsException e) {
		}
	}

	private String jName(String name) {
		return "jena-test-AbstractTestGraphMaker-" + name;
	}

	@Test
	public void testCanCreateTwice() {
		String name = jName("bridge");
		Graph g1 = graphMaker.createGraph(name, true);
		Graph g2 = graphMaker.createGraph(name, false);
		assertTrue("graphs should be the same", sameGraph(g1, g2));
		Graph g3 = graphMaker.createGraph(name);
		assertTrue("graphs should be the same", sameGraph(g1, g3));
	}

	/**
	 * Test that we cannot open a graph that does not exist.
	 */
	@Test
	public void testCannotOpenUncreated() {
		String name = jName("noSuchGraph");
		try {
			graphMaker.openGraph(name, true);
			fail(name + " should not exist");
		} catch (DoesNotExistException e) {
		}
	}

	/**
	 * Test that we *can* open a graph that hasn't been created
	 */
	@Test
	public void testCanOpenUncreated() {
		String name = jName("willBeCreated");
		Graph g1 = graphMaker.openGraph(name);
		g1.close();
		graphMaker.openGraph(name, true);
	}

	/**
	 * Utility - test that a graph with the given name exists.
	 */
	private void testExists(String name) {
		assertTrue(name + " should exist", graphMaker.hasGraph(name));
	}

	/**
	 * Utility - test that no graph with the given name exists.
	 */
	private void testDoesNotExist(String name) {
		assertFalse(name + " should exist", graphMaker.hasGraph(name));
	}

	/**
	 * Test that we can find a graph once its been created. We need to know if
	 * two graphs are "the same" here: we have a temporary work-around but it is
	 * not sound.
	 * 
	 */
	@Test
	public void testCanFindCreatedGraph() {
		String alpha = jName("alpha"), beta = jName("beta");
		Graph g1 = graphMaker.createGraph(alpha, true);
		Graph h1 = graphMaker.createGraph(beta, true);
		Graph g2 = graphMaker.openGraph(alpha, true);
		Graph h2 = graphMaker.openGraph(beta, true);
		assertTrue("should find alpha", sameGraph(g1, g2));
		assertTrue("should find beta", sameGraph(h1, h2));
	}

	/**
	 * Weak test for "same graph": adding this to one is visible in t'other.
	 * Stopgap for use in testCanFindCreatedGraph. TODO: clean that test up
	 * (left over from RDB days)
	 */
	private boolean sameGraph(Graph g1, Graph g2) {
		Node S = GraphTestUtils.node("S"), P = GraphTestUtils.node("P"), O = GraphTestUtils
				.node("O");
		g1.add(Triple.create(S, P, O));
		g2.add(Triple.create(O, P, S));
		return g2.contains(S, P, O) && g1.contains(O, P, S);
	}

	/**
	 * Test that we can remove a graph from the factory without disturbing
	 * another graph's binding.
	 */
	@Test
	public void testCanRemoveGraph() {
		String alpha = jName("bingo"), beta = jName("brillo");
		graphMaker.createGraph(alpha, true);
		graphMaker.createGraph(beta, true);
		testExists(alpha);
		testExists(beta);
		graphMaker.removeGraph(alpha);
		testExists(beta);
		testDoesNotExist(alpha);
	}

	@Test
	public void testHasnt() {
		assertFalse("no such graph", graphMaker.hasGraph("john"));
		assertFalse("no such graph", graphMaker.hasGraph("paul"));
		assertFalse("no such graph", graphMaker.hasGraph("george"));
		/* */
		graphMaker.createGraph("john", true);
		assertTrue("john now exists", graphMaker.hasGraph("john"));
		assertFalse("no such graph", graphMaker.hasGraph("paul"));
		assertFalse("no such graph", graphMaker.hasGraph("george"));
		/* */
		graphMaker.createGraph("paul", true);
		assertTrue("john still exists", graphMaker.hasGraph("john"));
		assertTrue("paul now exists", graphMaker.hasGraph("paul"));
		assertFalse("no such graph", graphMaker.hasGraph("george"));
		/* */
		graphMaker.removeGraph("john");
		assertFalse("john has been removed", graphMaker.hasGraph("john"));
		assertTrue("paul still exists", graphMaker.hasGraph("paul"));
		assertFalse("no such graph", graphMaker.hasGraph("george"));
	}

	@Test
	public void testCarefulClose() {
		Graph x = graphMaker.createGraph("x");
		Graph y = graphMaker.openGraph("x");
		x.add(GraphTestUtils.triple("a BB c"));
		x.close();
		y.add(GraphTestUtils.triple("p RR q"));
		y.close();
	}

	/**
	 * Test that a maker with no graphs lists no names.
	 */
	@Test
	public void testListNoGraphs() {
		Set<String> s = graphMaker.listGraphs().toSet();
		if (s.size() > 0)
			fail("found names from 'empty' graph maker: " + s);
	}

	/**
	 * Test that a maker with three graphs inserted lists those three grapsh; we
	 * don't mind what order they appear in. We also use funny names to ensure
	 * that the spelling that goes in is the one that comes out [should really
	 * be in a separate test].
	 */
	@Test
	public void testListThreeGraphs() {
		String x = "x", y = "y/sub", z = "z:boo";
		Graph X = graphMaker.createGraph(x);
		Graph Y = graphMaker.createGraph(y);
		Graph Z = graphMaker.createGraph(z);
		Set<String> wanted = TestUtils.setOfStrings(x + " " + y + " " + z);
		assertEquals(wanted,
				GraphTestUtils.iteratorToSet(graphMaker.listGraphs()));
		X.close();
		Y.close();
		Z.close();
	}

	/**
	 * Test that a maker with some things put in and then some removed gets the
	 * right things listed.
	 */
	@Test
	public void testListAfterDelete() {
		String x = "x_y", y = "y//zub", z = "a:b/c";
		Graph X = graphMaker.createGraph(x);
		Graph Y = graphMaker.createGraph(y);
		Graph Z = graphMaker.createGraph(z);
		graphMaker.removeGraph(x);
		Set<String> s = GraphTestUtils.iteratorToSet(graphMaker.listGraphs());
		assertEquals(TestUtils.setOfStrings(y + " " + z), s);
		X.close();
		Y.close();
		Z.close();
	}

}
