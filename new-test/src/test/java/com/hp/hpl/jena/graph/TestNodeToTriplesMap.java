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

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.junit.Test;

import com.hp.hpl.jena.graph.Triple.*;
import com.hp.hpl.jena.mem.NodeToTriplesMap;
import com.hp.hpl.jena.testing_framework.GraphTestUtils;

/**
 * TestNodeToTriplesMap: added, post-hoc, by kers once NTM got rather
 * complicated. So these tests may be (are, at the moment) incomplete.
 */
public class TestNodeToTriplesMap {

	protected NodeToTriplesMap ntS = new NodeToTriplesMap(Field.fieldSubject,
			Field.fieldPredicate, Field.fieldObject);

	protected NodeToTriplesMap ntP = new NodeToTriplesMap(Field.fieldPredicate,
			Field.fieldObject, Field.fieldSubject);

	protected NodeToTriplesMap ntO = new NodeToTriplesMap(Field.fieldObject,
			Field.fieldPredicate, Field.fieldSubject);

	protected static final Node x = GraphTestUtils.node("x");

	protected static final Node y = GraphTestUtils.node("y");

	@Test
	public void testZeroSize() {
		testZeroSize("fresh NTM", ntS);
	}

	protected void testZeroSize(String title, NodeToTriplesMap nt) {
		assertEquals(title + " should have size 0", 0, nt.size());
		assertEquals(title + " should be isEmpty()", true, nt.isEmpty());
		assertEquals(title + " should have empty domain", false, nt.domain()
				.hasNext());
	}

	@Test
	public void testAddOne() {
		ntS.add(GraphTestUtils.triple("x P y"));
		testJustOne(x, ntS);
	}

	@Test
	public void testAddOneTwice() {
		addTriples(ntS, "x P y; x P y");
		testJustOne(x, ntS);
	}

	protected void testJustOne(Node x, NodeToTriplesMap nt) {
		assertEquals(1, nt.size());
		assertEquals(false, nt.isEmpty());
		assertEquals(just(x), GraphTestUtils.iteratorToSet(nt.domain()));
	}

	@Test
	public void testAddTwoUnshared() {
		addTriples(ntS, "x P a; y Q b");
		assertEquals(2, ntS.size());
		assertEquals(false, ntS.isEmpty());
		assertEquals(both(x, y), GraphTestUtils.iteratorToSet(ntS.domain()));
	}

	@Test
	public void testAddTwoShared() {
		addTriples(ntS, "x P a; x Q b");
		assertEquals(2, ntS.size());
		assertEquals(false, ntS.isEmpty());
		assertEquals(just(x), GraphTestUtils.iteratorToSet(ntS.domain()));
	}

	@Test
	public void testClear() {
		addTriples(ntS, "x P a; x Q b; y R z");
		ntS.clear();
		testZeroSize("cleared NTM", ntS);
	}

	@Test
	public void testAllIterator() {
		String triples = "x P b; y P d; y P f";
		addTriples(ntS, triples);
		assertEquals(GraphTestUtils.tripleSet(triples),
				GraphTestUtils.iteratorToSet(ntS.iterateAll()));
	}

	@Test
	public void testOneIterator() {
		addTriples(ntS, "x P b; y P d; y P f");
		assertEquals(GraphTestUtils.tripleSet("x P b"), ntS.iterator(x, null)
				.toSet());
		assertEquals(GraphTestUtils.tripleSet("y P d; y P f"),
				ntS.iterator(y, null).toSet());
	}

	@Test
	public void testRemove() {
		addTriples(ntS, "x P b; y P d; y R f");
		ntS.remove(GraphTestUtils.triple("y P d"));
		assertEquals(2, ntS.size());
		assertEquals(GraphTestUtils.tripleSet("x P b; y R f"), ntS.iterateAll()
				.toSet());
	}

	@Test
	public void testRemoveByIterator() {
		addTriples(ntS, "x nice a; a nasty b; x nice c");
		addTriples(ntS, "y nice d; y nasty e; y nice f");
		Iterator<Triple> it = ntS.iterateAll();
		while (it.hasNext()) {
			Triple t = it.next();
			if (t.getPredicate().equals(GraphTestUtils.node("nasty")))
				it.remove();
		}
		assertEquals(
				GraphTestUtils
						.tripleSet("x nice a; x nice c; y nice d; y nice f"),
				ntS.iterateAll().toSet());
	}

	@Test
	public void testIteratorWIthPatternOnEmpty() {
		assertEquals(GraphTestUtils.tripleSet(""),
				ntS.iterateAll(GraphTestUtils.triple("a P b")).toSet());
	}

	@Test
	public void testIteratorWIthPatternOnSomething() {
		addTriples(ntS, "x P a; y P b; y R c");
		assertEquals(GraphTestUtils.tripleSet("x P a"),
				ntS.iterateAll(GraphTestUtils.triple("x P ??")).toSet());
		assertEquals(GraphTestUtils.tripleSet("y P b; y R c"),
				ntS.iterateAll(GraphTestUtils.triple("y ?? ??")).toSet());
		assertEquals(GraphTestUtils.tripleSet("x P a; y P b"),
				ntS.iterateAll(GraphTestUtils.triple("?? P ??")).toSet());
		assertEquals(GraphTestUtils.tripleSet("y R c"),
				ntS.iterateAll(GraphTestUtils.triple("?? ?? c")).toSet());
	}

	@Test
	public void testUnspecificRemoveS() {
		addTriples(ntS, "x P a; y Q b; z R c");
		ntS.remove(GraphTestUtils.triple("x P a"));
		assertEquals(GraphTestUtils.tripleSet("y Q b; z R c"), ntS.iterateAll()
				.toSet());
	}

	@Test
	public void testUnspecificRemoveP() {
		addTriples(ntP, "x P a; y Q b; z R c");
		ntP.remove(GraphTestUtils.triple("y Q b"));
		assertEquals(GraphTestUtils.tripleSet("x P a; z R c"), ntP.iterateAll()
				.toSet());
	}

	@Test
	public void testUnspecificRemoveO() {
		addTriples(ntO, "x P a; y Q b; z R c");
		ntO.remove(GraphTestUtils.triple("z R c"));
		assertEquals(GraphTestUtils.tripleSet("x P a; y Q b"), ntO.iterateAll()
				.toSet());
	}

	@Test
	public void testAddBooleanResult() {
		assertEquals(true, ntS.add(GraphTestUtils.triple("x P y")));
		assertEquals(false, ntS.add(GraphTestUtils.triple("x P y")));
		/* */
		assertEquals(true, ntS.add(GraphTestUtils.triple("y Q z")));
		assertEquals(false, ntS.add(GraphTestUtils.triple("y Q z")));
		/* */
		assertEquals(true, ntS.add(GraphTestUtils.triple("y R s")));
		assertEquals(false, ntS.add(GraphTestUtils.triple("y R s")));
	}

	@Test
	public void testRemoveBooleanResult() {
		assertEquals(false, ntS.remove(GraphTestUtils.triple("x P y")));
		ntS.add(GraphTestUtils.triple("x P y"));
		assertEquals(false, ntS.remove(GraphTestUtils.triple("x Q y")));
		assertEquals(true, ntS.remove(GraphTestUtils.triple("x P y")));
		assertEquals(false, ntS.remove(GraphTestUtils.triple("x P y")));
	}

	@Test
	public void testContains() {
		addTriples(ntS, "x P y; a P b");
		assertTrue(ntS.contains(GraphTestUtils.triple("x P y")));
		assertTrue(ntS.contains(GraphTestUtils.triple("a P b")));
		assertFalse(ntS.contains(GraphTestUtils.triple("x P z")));
		assertFalse(ntS.contains(GraphTestUtils.triple("y P y")));
		assertFalse(ntS.contains(GraphTestUtils.triple("x R y")));
		assertFalse(ntS.contains(GraphTestUtils.triple("e T f")));
		assertFalse(ntS.contains(GraphTestUtils.triple("_x F 17")));
	}

	// TODO more here

	protected void addTriples(NodeToTriplesMap nt, String facts) {
		Triple[] t = GraphTestUtils.tripleArray(facts);
		for (int i = 0; i < t.length; i += 1)
			nt.add(t[i]);
	}

	protected static <T> Set<T> just(T x) {
		Set<T> result = new HashSet<T>();
		result.add(x);
		return result;
	}

	protected static Set<Object> both(Object x, Object y) {
		Set<Object> result = just(x);
		result.add(y);
		return result;
	}

}
